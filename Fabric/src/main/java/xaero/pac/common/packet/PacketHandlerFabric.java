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

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.packet.payload.PacketPayload;
import xaero.pac.common.packet.payload.PacketPayloadCodec;
import xaero.pac.common.packet.type.PacketTypeManager;

public class PacketHandlerFabric extends PacketHandlerFull {

	private ClientPacketHandlerFabric clientPacketHandlerFabric;

	private PacketHandlerFabric(PacketTypeManager packetTypeManager){
		super(packetTypeManager);
	}

	private void setClientPacketHandlerFabric(ClientPacketHandlerFabric clientPacketHandlerFabric) {
		this.clientPacketHandlerFabric = clientPacketHandlerFabric;
	}

	public void registerOnClient(){
		clientPacketHandlerFabric.registerOnClient();
	}

	public void registerCommon() {
		PacketPayloadCodec payloadCodec = new PacketPayloadCodec();
		PayloadTypeRegistry.playS2C().register(PacketPayload.TYPE, payloadCodec);
		PayloadTypeRegistry.playC2S().register(PacketPayload.TYPE, payloadCodec);
		ServerPlayNetworking.registerGlobalReceiver(PacketPayload.TYPE, new ServerPacketReceiverFabric(this));
	}

	@Override
	public <T> void sendToServer(T packet) {
		clientPacketHandlerFabric.sendToServer(packet);
	}

	@Override
	public <T> void sendToPlayer(ServerPlayer player, T packet) {
		ServerPlayNetworking.send(player, createPayload(packet));
	}

	public static class Builder extends PacketHandlerFull.Builder {

		private Builder(){
			super();
		}

		public Builder setDefault(){
			return this;
		}

		public PacketHandlerFabric build(){
			return (PacketHandlerFabric) super.build();
		}

		@Override
		protected PacketHandlerFull buildInternal(PacketTypeManager packetTypeManager) {
			PacketHandlerFabric result = new PacketHandlerFabric(packetTypeManager);
			if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
				result.setClientPacketHandlerFabric(new ClientPacketHandlerFabric(result));
			return result;
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
