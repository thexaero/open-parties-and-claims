/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class ClientPacketReceiver extends PacketReceiver<Object> implements ClientPlayNetworking.PlayChannelHandler {

	public ClientPacketReceiver(PacketHandlerFabric packetHandlerFabric) {
		super(packetHandlerFabric);
	}

	@Override
	public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		receive(client, buf, null);
	}

	@Override
	protected <T> boolean isCorrectSide(PacketType<T> packetType) {
		return packetType.getClientHandler() != null;
	}

	@Override
	protected <T> Runnable getTask(PacketType<T> packetType, T packet, Object context) {
		return () -> packetType.getClientHandler().accept(packet);
	}

}
