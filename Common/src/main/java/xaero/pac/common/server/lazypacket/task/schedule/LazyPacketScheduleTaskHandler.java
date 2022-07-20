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

package xaero.pac.common.server.lazypacket.task.schedule;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.task.ServerPlayerSpreadoutTaskHandler;

import java.util.function.BiPredicate;
import java.util.function.Function;

public final class LazyPacketScheduleTaskHandler extends ServerPlayerSpreadoutTaskHandler<ILazyPacketScheduleTask> {

	private LazyPacketScheduleTaskHandler(Function<ServerPlayerData, ILazyPacketScheduleTask> playerTaskGetter, int perTickLimit, int perTickPerPlayerLimit, BiPredicate<IServerData<?, ?>, ServerPlayer> shouldWaitPredicate) {
		super(playerTaskGetter, perTickLimit, perTickPerPlayerLimit, shouldWaitPredicate);
	}

	public void onLazyPacketsDropped(ServerPlayer player){
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
		playerTaskGetter.apply(playerData).onLazyPacketsDropped();
	}

	public static final class Builder extends ServerPlayerSpreadoutTaskHandler.Builder<ILazyPacketScheduleTask, Builder> {

		public static Builder begin(){
			return new Builder().setDefault();
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setShouldWaitPredicate((sd, p) -> sd.getServerTickHandler().getLazyPacketSender().isClogged(p));
			return self;
		}

		@Override
		public LazyPacketScheduleTaskHandler build() {
			return (LazyPacketScheduleTaskHandler) super.build();
		}

		@Override
		protected LazyPacketScheduleTaskHandler buildInternally() {
			return new LazyPacketScheduleTaskHandler(playerTaskGetter, perTickLimit, perTickPerPlayerLimit, shouldWaitPredicate);
		}

	}

}
