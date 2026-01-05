/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2025, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.resources.Identifier;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

import java.util.UUID;
import java.util.function.Function;

public class ClientboundClaimsClaimUpdatePacket extends LazyPacket<ClientboundClaimsClaimUpdatePacket> {

	public static final Encoder<ClientboundClaimsClaimUpdatePacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final Identifier dimension;
	private final int x;
	private final int z;
	private final UUID playerId;
	private final int subConfigIndex;
	private final boolean forceLoaded;
	private final int claimSyncIndex;

	public ClientboundClaimsClaimUpdatePacket(Identifier dimension, int x, int z, UUID playerId, int subConfigIndex, boolean forceLoaded, int claimSyncIndex) {
		super();
		this.dimension = dimension;
		this.x = x;
		this.z = z;
		this.playerId = playerId;
		this.subConfigIndex = subConfigIndex;
		this.forceLoaded = forceLoaded;
		this.claimSyncIndex = claimSyncIndex;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimsClaimUpdatePacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("d", dimension.toString());
		nbt.putInt("x", x);
		nbt.putInt("z", z);
		if(playerId != null) {
			nbt.putInt("i", claimSyncIndex);
			XaeroNbtUtil.putUUID(nbt, "p", playerId);
			nbt.putInt("s", subConfigIndex);
			nbt.putBoolean("f", forceLoaded);
		}
		u.writeNbt(nbt);
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %d, %d, %s, %s, %d, %d]", dimension, x, z, playerId, forceLoaded, subConfigIndex, claimSyncIndex);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimsClaimUpdatePacket> {

		@Override
		public ClientboundClaimsClaimUpdatePacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 10000)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				String dimensionString = nbt.getStringOr("d", null);
				if(dimensionString == null || dimensionString.length() > 2048)
					return null;
				int x = nbt.getIntOr("x", 0);
				int z = nbt.getIntOr("z", 0);
				int claimStateIndex = -1;
				UUID playerId = null;
				int subConfigIndex = -1;
				boolean forceload = false;
				if(nbt.contains("p")) {
					claimStateIndex = nbt.getIntOr("i", 0);
					playerId = XaeroNbtUtil.getUUID(nbt, "p").orElse(null);
					subConfigIndex = nbt.getIntOr("s", 0);
					forceload = nbt.getBooleanOr("f", false);
				}
				return new ClientboundClaimsClaimUpdatePacket(Identifier.parse(dimensionString), x, z, playerId, subConfigIndex, forceload, claimStateIndex);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundClaimsClaimUpdatePacket> {
		
		@Override
		public void handle(ClientboundClaimsClaimUpdatePacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimUpdate(t.dimension, t.x, t.z, t.playerId, t.subConfigIndex, t.forceLoaded, t.claimSyncIndex);
		}
		
	}

}
