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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import xaero.pac.OpenPartiesAndClaims;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketHandlerForge implements IPacketHandler {

	private static final int PROTOCOL_VERSION = 1004000;
	public static final SimpleChannel NETWORK = ChannelBuilder.named(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION).networkProtocolVersion(PROTOCOL_VERSION).optional().simpleChannel();

	@Override
	public void onServerAboutToStart() {
	}

	@Override
	public <P> void register(int index, Class<P> type,
							 BiConsumer<P, FriendlyByteBuf> encoder,
							 Function<FriendlyByteBuf, P> decoder,
							 BiConsumer<P, ServerPlayer> serverHandler,
							 Consumer<P> clientHandler) {
		PacketConsumerForge<P> consumer = new PacketConsumerForge<P>(serverHandler, clientHandler);
		if((serverHandler == null) != (clientHandler == null))
			NETWORK.messageBuilder(type, index, clientHandler != null ? NetworkDirection.PLAY_TO_CLIENT : NetworkDirection.PLAY_TO_SERVER).consumerNetworkThread(consumer).decoder(decoder).encoder(encoder).add();
		else
			NETWORK.messageBuilder(type, index).consumerNetworkThread(consumer).decoder(decoder).encoder(encoder).add();
	}

	@Override
	public void sendToServer(Object packet) {
		NETWORK.send(packet, PacketDistributor.SERVER.noArg());
	}

	@Override
	public void sendToPlayer(ServerPlayer player, Object packet) {
		NETWORK.send(packet, PacketDistributor.PLAYER.with(player));
	}

}
