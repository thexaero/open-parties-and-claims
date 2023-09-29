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

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketConsumerForge<P> implements BiConsumer<P, CustomPayloadEvent.Context> {

	private final BiConsumer<P, ServerPlayer> serverHandler;
	private final Consumer<P> clientHandler;

	public PacketConsumerForge(BiConsumer<P, ServerPlayer> serverHandler,
							   Consumer<P> clientHandler) {
		this.serverHandler = serverHandler;
		this.clientHandler = clientHandler;
	}

	@Override
	public void accept(P msg, CustomPayloadEvent.Context context) {
		if(msg == null) {
			context.setPacketHandled(true);
			return;
		}
		NetworkDirection networkDirection = context.getDirection();
		if(clientHandler != null && networkDirection == NetworkDirection.PLAY_TO_CLIENT) {
			context.enqueueWork(
					() -> clientHandler.accept(msg)
			);
		} else if(serverHandler != null && networkDirection == NetworkDirection.PLAY_TO_SERVER) {
			ServerPlayer sender = context.getSender();
			context.enqueueWork(
					() -> serverHandler.accept(msg, sender)
			);
		}
		context.setPacketHandled(true);
	}

}
