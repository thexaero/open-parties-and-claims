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

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.packet.type.PacketType;
import xaero.pac.common.packet.type.PacketTypeManager;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Used on platforms that don't handle packet types like Forge
 */
public abstract class PacketHandlerFull implements IPacketHandler {

	protected final PacketTypeManager packetTypeManager;

	protected PacketHandlerFull(PacketTypeManager packetTypeManager) {
		this.packetTypeManager = packetTypeManager;
	}

	public <P> void register(int index, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> serverHandler, Consumer<P> clientHandler) {
		packetTypeManager.register(index, type, encoder, decoder, serverHandler, clientHandler);
	}

	public static <T> void encodePacket(PacketType<T> packetType, T packet, FriendlyByteBuf buffer) {
		if(packetType == null)
			throw new IllegalArgumentException("unregistered packet class!");
		buffer.writeByte(packetType.getIndex());
		packetType.getEncoder().accept(packet, buffer);
	}

	<T> FriendlyByteBuf getPacketBuffer(T packet){
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		encodePacket(packetTypeManager.getType(packet), packet, buffer);
		return buffer;
	}

	public PacketType<?> getPacketTypeByIndex(int index){
		return packetTypeManager.getByIndex(index);
	}

	public static abstract class Builder {

		protected Builder(){}

		public Builder setDefault(){
			return this;
		}

		public PacketHandlerFull build(){
			PacketTypeManager packetTypeManager = PacketTypeManager.Builder.begin(Int2ObjectOpenHashMap::new, HashMap::new).build();
			return buildInternal(packetTypeManager);
		}

		protected abstract PacketHandlerFull buildInternal(PacketTypeManager packetTypeManager);

	}

}
