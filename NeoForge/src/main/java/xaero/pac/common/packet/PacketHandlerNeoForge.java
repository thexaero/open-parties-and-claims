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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.payload.PacketPayload;
import xaero.pac.common.packet.payload.PacketPayloadCodec;
import xaero.pac.common.packet.type.PacketTypeManager;

import java.util.HashMap;

public class PacketHandlerNeoForge extends PacketHandlerFull {

	private static final String PROTOCOL_VERSION = "1.10.0";

	public PacketHandlerNeoForge() {
		super(PacketTypeManager.Builder.begin(Int2ObjectOpenHashMap::new, HashMap::new).build());
	}

	public static void registerPayloadHandler(RegisterPayloadHandlersEvent event) {
		event.registrar(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION.getNamespace())
				.versioned(PROTOCOL_VERSION)
				.optional()
				.playBidirectional(PacketPayload.TYPE, new PacketPayloadCodec(), new PacketPayloadHandler());
	}

	public static void registerClientPayloadHandler(RegisterClientPayloadHandlersEvent event) {
		event.register(PacketPayload.TYPE, new PacketPayloadHandler());
	}

	@Override
	public <P> void sendToServer(P packet) {
		ClientPacketDistributor.sendToServer(createPayload(packet));
	}

	@Override
	public <P> void sendToPlayer(ServerPlayer player, P packet) {
		if(!player.connection.hasChannel(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION))
			return;
		PacketDistributor.sendToPlayer(player, createPayload(packet));
	}

}
