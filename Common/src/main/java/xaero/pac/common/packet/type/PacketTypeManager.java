/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024-2025, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.packet.type;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.misc.MapFactory;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used on platforms that don't handle packet types like Forge
 */
public class PacketTypeManager {

	private final Int2ObjectOpenHashMap<PacketType<?>> index2Type;
	private final Map<Class<?>, PacketType<?>> class2Type;

	private PacketTypeManager(Int2ObjectOpenHashMap<PacketType<?>> index2Type, Map<Class<?>, PacketType<?>> class2Type){
		this.index2Type = index2Type;
		this.class2Type = class2Type;
	}

	public <P> void register(int index, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> serverHandler, Consumer<P> clientHandler) {
		PacketType<P> packetType = new PacketType<>(index, type, encoder, decoder, serverHandler, clientHandler);
		if(index2Type.containsKey(index))
			throw new IllegalArgumentException("duplicate index!");
		if(class2Type.containsKey(type))
			throw new IllegalArgumentException("duplicate packet class!");
		index2Type.put(index, packetType);
		class2Type.put(type, packetType);
	}

	public PacketType<?> getByIndex(int index){
		return index2Type.get(index);
	}

	public PacketType<?> getByClass(Class<?> clazz){
		return class2Type.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public <P> PacketType<P> getType(P message){
		return (PacketType<P>) class2Type.get(message.getClass());
	}

	public static final class Builder {

		private final Supplier<Int2ObjectOpenHashMap<PacketType<?>>> index2TypeFactory;
		private final MapFactory mapFactory;

		public Builder(Supplier<Int2ObjectOpenHashMap<PacketType<?>>> index2TypeFactory, MapFactory mapFactory) {
			this.index2TypeFactory = index2TypeFactory;
			this.mapFactory = mapFactory;
		}

		private Builder setDefault() {
			return this;
		}

		public PacketTypeManager build(){
			return new PacketTypeManager(index2TypeFactory.get(), mapFactory.get());
		}

		public static Builder begin(Supplier<Int2ObjectOpenHashMap<PacketType<?>>> index2TypeFactory, MapFactory mapFactory){
			return new Builder(index2TypeFactory, mapFactory).setDefault();
		}

	}

}
