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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import xaero.pac.OpenPartiesAndClaims;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketHandlerNeoForge implements IPacketHandler {

	private static final String PROTOCOL_VERSION = "1.4.0";

	private final Int2ObjectOpenHashMap<PacketType<?>> int2PacketType;
	private final Map<Class<?>, PacketType<?>> class2PacketType;

	public PacketHandlerNeoForge() {
		this.int2PacketType = new Int2ObjectOpenHashMap<>();
		this.class2PacketType = new HashMap<>();
	}

	public static void registerPayloadHandler(RegisterPayloadHandlerEvent event) {
		event.registrar(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION.getNamespace())
				.versioned(PROTOCOL_VERSION)
				.optional()
				.play(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION, new PacketPayloadReader(), new PacketPayloadHandler());
	}

	@Override
	public void onServerAboutToStart() {
	}

	@Override
	public <P> void register(int index, Class<P> type,
							 BiConsumer<P, FriendlyByteBuf> encoder,
							 Function<FriendlyByteBuf, P> decoder,
							 BiConsumer<P, ServerPlayer> serverHandler,
							 Consumer<P> clientHandler) {
		PacketType<P> packetType = new PacketType<>(index, type, encoder, decoder, serverHandler, clientHandler);
		int2PacketType.put(index, packetType);
		class2PacketType.put(type, packetType);
	}

	public PacketType<?> getByIndex(int index){
		return int2PacketType.get(index);
	}

	@SuppressWarnings("unchecked")
	public <P> PacketType<P> getByClass(Class<P> clazz){
		return (PacketType<P>) class2PacketType.get(clazz);
	}

	@SuppressWarnings("unchecked")
	private <P> PacketType<P> getType(P packet){
		return (PacketType<P>) getByClass(packet.getClass());
	}

	public static <P> void write(PacketType<P> type, P packet, FriendlyByteBuf buf){
		buf.writeByte(type.getIndex());
		type.getEncoder().accept(packet, buf);
	}

	@Override
	public <P> void sendToServer(P packet) {
		PacketDistributor.SERVER.noArg().send(new PacketPayload<>(getType(packet), packet));
	}

	@Override
	public <P> void sendToPlayer(ServerPlayer player, P packet) {
		PacketDistributor.PLAYER.with(player).send(new PacketPayload<>(getType(packet), packet));
	}

}
