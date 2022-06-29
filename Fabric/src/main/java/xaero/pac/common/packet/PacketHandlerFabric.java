/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketHandlerFabric implements IPacketHandler {

	private final Int2ObjectOpenHashMap<PacketType<?>> int2PacketType;
	private final Map<Class<?>, PacketType<?>> class2PacketType;

	private PacketHandlerFabric(Int2ObjectOpenHashMap<PacketType<?>> int2PacketType, Map<Class<?>, PacketType<?>> class2PacketType){
		this.int2PacketType = int2PacketType;
		this.class2PacketType = class2PacketType;
	}

	public void registerOnClient(){
		ClientPlayNetworking.registerGlobalReceiver(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION, new ClientPacketReceiver(this));
	}

	@Override
	public void onServerAboutToStart() {
		ServerPlayNetworking.registerGlobalReceiver(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION, new ServerPacketReceiver(this));
	}

	@Override
	public <P> void register(int index, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> serverHandler, Consumer<P> clientHandler) {
		PacketType<P> packetType = new PacketType<>(index, type, encoder, decoder, serverHandler, clientHandler);
		if(int2PacketType.containsKey(index))
			throw new IllegalArgumentException("duplicate index!");
		if(class2PacketType.containsKey(type))
			throw new IllegalArgumentException("duplicate packet class!");
		int2PacketType.put(index, packetType);
		class2PacketType.put(type, packetType);
	}

	@Override
	public <T> void sendToServer(T packet) {
		ClientPlayNetworking.send(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION, getPacketBuffer(packet));
	}

	@Override
	public <T> void sendToPlayer(ServerPlayer player, T packet) {
		ServerPlayNetworking.send(player, OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION, getPacketBuffer(packet));
	}

	private <T> FriendlyByteBuf getPacketBuffer(T packet){
		PacketType<?> packetTypePreCast = class2PacketType.get(packet.getClass());
		if(packetTypePreCast == null)
			throw new IllegalArgumentException("unregistered packet class!");
		@SuppressWarnings("unchecked")
		PacketType<T> packetType = (PacketType<T>) packetTypePreCast;
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		buffer.writeByte(packetType.getIndex());
		packetType.getEncoder().accept(packet, buffer);
		return buffer;
	}

	public PacketType<?> getPacketTypeByIndex(int index){
		return int2PacketType.get(index);
	}

	public static class Builder {

		private Builder(){}

		public Builder setDefault(){
			return this;
		}

		public PacketHandlerFabric build(){
			return new PacketHandlerFabric(new Int2ObjectOpenHashMap<>(), new HashMap<>());
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
