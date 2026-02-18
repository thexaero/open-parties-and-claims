/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024-2026, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import xaero.pac.common.packet.payload.PacketPayload;
import xaero.pac.common.packet.type.PacketType;

import javax.annotation.Nonnull;

public class PacketPayloadHandler implements IPayloadHandler<PacketPayload<?>> {

	@Override
	public void handle(@Nonnull PacketPayload<?> payload, @Nonnull IPayloadContext context) {
		handleTyped(payload, context);
	}

	private <P> void handleTyped(PacketPayload<P> payload, IPayloadContext context){
		PacketType<P> packetType = payload.getPacketType();
		if(packetType == null)
			return;
		P packet = payload.getPacket();
		if(packet == null)
			return;
		if(context.flow() == PacketFlow.CLIENTBOUND) {
			if (packetType.getClientHandler() == null)
				return;
			context.enqueueWork(() -> packetType.getClientHandler().accept(packet));
			return;
		}
		if(packetType.getServerHandler() == null)
			return;
		if(context.flow() != PacketFlow.SERVERBOUND)
			return;
		ServerPlayer player = (ServerPlayer) context.player();
		context.enqueueWork(() -> packetType.getServerHandler().accept(packet, player));
	}

}
