/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.task;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class ServerPlayerSpreadoutTaskHandler<T extends IServerPlayerSpreadoutTask> {

	protected final Function<ServerPlayerData, T> playerTaskGetter;
	protected  final int perTickLimit;
	protected  final int perTickPerPlayerLimit;
	protected  final BiPredicate<IServerData<?,?>, ServerPlayer> shouldWaitPredicate;

	protected ServerPlayerSpreadoutTaskHandler(Function<ServerPlayerData, T> playerTaskGetter, int perTickLimit, int perTickPerPlayerLimit, BiPredicate<IServerData<?, ?>, ServerPlayer> shouldWaitPredicate) {
		this.playerTaskGetter = playerTaskGetter;
		this.perTickLimit = perTickLimit;
		this.perTickPerPlayerLimit = perTickPerPlayerLimit;
		this.shouldWaitPredicate = shouldWaitPredicate;
	}

	public void onTick(PlayerList players, IServerData<?,?> serverData){
		int playerCount = 0;
		for(ServerPlayer player : players.getPlayers()) {
			ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(player);
			if(playerTaskGetter.apply(mainCap).shouldWork() && !shouldWaitPredicate.test(serverData, player))
				playerCount++;
		}
		if(playerCount == 0)
			return;
		int perPlayer = Math.min(perTickPerPlayerLimit, perTickLimit / playerCount);
		for(ServerPlayer player : players.getPlayers()) {
			ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(player);
			if(!shouldWaitPredicate.test(serverData, player))
				playerTaskGetter.apply(mainCap).onTick(serverData, player, perPlayer);
		}
	}

	public static abstract class Builder<T extends IServerPlayerSpreadoutTask, B extends Builder<T, B>> {

		protected final B self;
		protected Function<ServerPlayerData, T> playerTaskGetter;
		protected int perTickLimit;
		protected int perTickPerPlayerLimit;
		protected BiPredicate<IServerData<?,?>, ServerPlayer> shouldWaitPredicate;

		protected Builder(){
			@SuppressWarnings("unchecked")
			B self = (B) this;
			this.self = self;
		}

		public B setDefault(){
			setPlayerTaskGetter(null);
			setPerTickLimit(-1);
			setPerTickPerPlayerLimit(-1);
			setShouldWaitPredicate(null);
			return self;
		}

		public B setPlayerTaskGetter(Function<ServerPlayerData, T> playerTaskGetter) {
			this.playerTaskGetter = playerTaskGetter;
			return self;
		}

		public B setPerTickLimit(int perTickLimit) {
			this.perTickLimit = perTickLimit;
			return self;
		}

		public B setPerTickPerPlayerLimit(int perTickPerPlayerLimit) {
			this.perTickPerPlayerLimit = perTickPerPlayerLimit;
			return self;
		}

		public B setShouldWaitPredicate(BiPredicate<IServerData<?, ?>, ServerPlayer> shouldWaitPredicate) {
			this.shouldWaitPredicate = shouldWaitPredicate;
			return self;
		}

		public ServerPlayerSpreadoutTaskHandler<T> build(){
			if(playerTaskGetter == null || perTickLimit == -1 || perTickPerPlayerLimit == -1 || shouldWaitPredicate == null)
				throw new IllegalStateException();
			return buildInternally();
		}

		protected abstract ServerPlayerSpreadoutTaskHandler<T> buildInternally();

	}

	public static final class FinalBuilder extends Builder<IServerPlayerSpreadoutTask, FinalBuilder> {

		public static FinalBuilder begin(){
			return new FinalBuilder().setDefault();
		}

		@Override
		protected ServerPlayerSpreadoutTaskHandler<IServerPlayerSpreadoutTask> buildInternally() {
			return new ServerPlayerSpreadoutTaskHandler<>(playerTaskGetter, perTickLimit, perTickPerPlayerLimit, shouldWaitPredicate);
		}

	}

}
