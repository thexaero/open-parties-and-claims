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

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import xaero.pac.common.packet.payload.PacketPayload;
import xaero.pac.common.server.world.ServerLevelHelper;

public class ServerPacketReceiverFabric extends ServerPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<PacketPayload<?>> {

	public ServerPacketReceiverFabric(PacketHandlerFabric packetHandlerFabric) {
		super(packetHandlerFabric);
	}

	@Override
	public void receive(PacketPayload<?> payload, ServerPlayNetworking.Context context) {
		receive(ServerLevelHelper.getServer(context.player()), payload, context.player());
	}
}
