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

package xaero.pac.common.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerLoginHandshakePacket {

	public ServerLoginHandshakePacket() {
		super();
	}
	
	public static class Codec implements BiConsumer<ServerLoginHandshakePacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ServerLoginHandshakePacket> {

		@Override
		public ServerLoginHandshakePacket apply(FriendlyByteBuf input) {
			return new ServerLoginHandshakePacket();
		}

		@Override
		public void accept(ServerLoginHandshakePacket t, FriendlyByteBuf u) {
		}
		
	}
	
	public static class ClientHandler implements Consumer<ServerLoginHandshakePacket> {
		
		@Override
		public void accept(ServerLoginHandshakePacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().reset();
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(t);
		}
		
	}

	public static class ServerHandler implements BiConsumer<ServerLoginHandshakePacket, ServerPlayer> {

		@Override
		public void accept(ServerLoginHandshakePacket t, ServerPlayer player) {
			((ServerPlayerData)ServerPlayerData.from(player)).setHasMod(true);
		}

	}
	
}
