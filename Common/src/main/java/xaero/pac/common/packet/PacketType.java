/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2024, Xaero <xaero1996@gmail.com> and contributors
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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketType<P> {

	private final int index;
	private final Class<P> type;
	private final BiConsumer<P, FriendlyByteBuf> encoder;
	private final Function<FriendlyByteBuf, P> decoder;
	private final BiConsumer<P, ServerPlayer> serverHandler;
	private final Consumer<P> clientHandler;

	public PacketType(int index, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> serverHandler, Consumer<P> clientHandler) {
		this.index = index;
		this.type = type;
		this.encoder = encoder;
		this.decoder = decoder;
		this.serverHandler = serverHandler;
		this.clientHandler = clientHandler;
	}

	public int getIndex() {
		return index;
	}

	public Class<P> getType() {
		return type;
	}

	public BiConsumer<P, FriendlyByteBuf> getEncoder() {
		return encoder;
	}

	public Function<FriendlyByteBuf, P> getDecoder() {
		return decoder;
	}

	public BiConsumer<P, ServerPlayer> getServerHandler() {
		return serverHandler;
	}

	public Consumer<P> getClientHandler() {
		return clientHandler;
	}

}
