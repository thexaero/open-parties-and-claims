/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.util.PacketConstants;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerLoginHandshakePacket {

	private final int networkVersion;

	public ServerLoginHandshakePacket(int networkVersion) {
		super();
		this.networkVersion = networkVersion;
	}
	
	public static class Codec implements BiConsumer<ServerLoginHandshakePacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ServerLoginHandshakePacket> {

		@Override
		public ServerLoginHandshakePacket apply(FriendlyByteBuf input) {
			CompoundTag nbt;
			try {
				nbt = input.readNbt();
				if(nbt == null)
					return null;
				int networkVersion = nbt.getIntOr("v", 0);
				return new ServerLoginHandshakePacket(networkVersion);
			} catch(Throwable t){
				//received from an older client mod considered version 0
				return new ServerLoginHandshakePacket(0);
			}
		}

		@Override
		public void accept(ServerLoginHandshakePacket t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("v", t.networkVersion);
			u.writeNbt(nbt);
		}
		
	}
	
	public static class ClientHandler implements Consumer<ServerLoginHandshakePacket> {
		
		@Override
		public void accept(ServerLoginHandshakePacket t) {
			if(t.networkVersion != PacketConstants.NETWORK_VERSION) {
				Minecraft.getInstance().getConnection().getConnection().disconnect(PacketConstants.NETWORK_VERSION_MISMATCH);
				return;
			}
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().reset(true);
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(t);
		}
		
	}

	public static class ServerHandler implements BiConsumer<ServerLoginHandshakePacket, ServerPlayer> {

		@Override
		public void accept(ServerLoginHandshakePacket t, ServerPlayer player) {
			if(t.networkVersion != PacketConstants.NETWORK_VERSION) {
				player.connection.disconnect(PacketConstants.NETWORK_VERSION_MISMATCH);
				return;
			}
			((ServerPlayerData)ServerPlayerData.from(player)).setHasMod(true);
		}

	}
	
}
