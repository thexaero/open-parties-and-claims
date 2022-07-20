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

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClaimRegionsStartPacket extends LazyPacket<LazyPacket.Encoder<ClaimRegionsStartPacket>, ClaimRegionsStartPacket> {
	
	public static final Encoder<ClaimRegionsStartPacket> ENCODER = new Encoder<>();

	public ClaimRegionsStartPacket() {
		super();
	}

	@Override
	protected void writeOnPrepare(LazyPacket.Encoder<ClaimRegionsStartPacket> encoder, FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		u.writeNbt(tag);
	}

	@Override
	protected Encoder<ClaimRegionsStartPacket> getEncoder() {
		return ENCODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClaimRegionsStartPacket> {
		
		@Override
		public ClaimRegionsStartPacket apply(FriendlyByteBuf input) {
			try {
				input.readNbt(new NbtAccounter(2048));
				return new ClaimRegionsStartPacket();
			} catch(Throwable t) {
				return null;
			}
		}
		
	}
	
	public static class ServerHandler implements BiConsumer<ClaimRegionsStartPacket,ServerPlayer> {
		
		@Override
		public void accept(ClaimRegionsStartPacket t, ServerPlayer serverPlayer) {
			ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(serverPlayer);
			mainCap.getClaimsManagerPlayerClaimPropertiesSync().start(serverPlayer);
			mainCap.getClaimsManagerPlayerStateSync().start(serverPlayer);
			mainCap.getClaimsManagerPlayerRegionSync().start(serverPlayer);
		}
		
	}

	public static class ClientHandler implements Consumer<ClaimRegionsStartPacket> {

		@Override
		public void accept(ClaimRegionsStartPacket t) {
			t.prepare();
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(t);
		}

	}
	
}
