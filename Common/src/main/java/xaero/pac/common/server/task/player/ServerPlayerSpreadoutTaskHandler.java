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

package xaero.pac.common.server.task.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.task.ServerSpreadoutTaskHandler;

import java.util.Iterator;
import java.util.function.Function;

public class ServerPlayerSpreadoutTaskHandler<T extends IServerPlayerSpreadoutTask> extends ServerSpreadoutTaskHandler<T, ServerPlayer> {

	protected ServerPlayerSpreadoutTaskHandler(Function<ServerPlayer, T> holderToTask, int perTickLimit, int perTickPerTaskLimit) {
		super(holderToTask, perTickLimit, perTickPerTaskLimit);
	}

	@Override
	protected Iterator<ServerPlayer> getTaskHolderIterator(IServerData<?, ?> serverData) {
		return serverData.getServer().getPlayerList().getPlayers().iterator();
	}

	@Override
	protected final boolean canDropTasks() {
		//can't be removing players
		return false;
	}

	public static abstract class Builder<T extends IServerPlayerSpreadoutTask, B extends Builder<T, B>> extends ServerSpreadoutTaskHandler.Builder<T, ServerPlayer, B> {

		private Function<ServerPlayerData, T> playerTaskGetter;

		protected Builder(){
		}

		@Override
		public B setDefault() {
			super.setDefault();
			setPlayerTaskGetter(null);
			return self;
		}

		@Override
		public final B setHolderToTask(Function<ServerPlayer, T> holderToTask) {
			if(holderToTask != null)
				throw new RuntimeException(new IllegalAccessException());
			return super.setHolderToTask(holderToTask);
		}

		public B setPlayerTaskGetter(Function<ServerPlayerData, T> playerTaskGetter) {
			this.playerTaskGetter = playerTaskGetter;
			return self;
		}

		@Override
		public ServerSpreadoutTaskHandler<T, ServerPlayer> build() {
			if(playerTaskGetter == null)
				throw new IllegalStateException();
			holderToTask = p -> {
				ServerPlayerData data = (ServerPlayerData) ServerPlayerData.from(p);
				return playerTaskGetter.apply(data);
			};
			return super.build();
		}
	}

	public static final class FinalBuilder extends Builder<IServerPlayerSpreadoutTask, FinalBuilder> {

		@Override
		protected ServerSpreadoutTaskHandler<IServerPlayerSpreadoutTask, ServerPlayer> buildInternally() {
			return new ServerPlayerSpreadoutTaskHandler<>(holderToTask, perTickLimit, perTickPerTaskLimit);
		}

		public static FinalBuilder begin(){
			return new FinalBuilder().setDefault();
		}

	}

}
