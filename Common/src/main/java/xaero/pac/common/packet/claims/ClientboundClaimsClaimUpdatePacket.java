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
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypackets.LazyPacket;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimsClaimUpdatePacket extends LazyPacket<LazyPacket.Encoder<ClientboundClaimsClaimUpdatePacket>, ClientboundClaimsClaimUpdatePacket> {

	public static final Encoder<ClientboundClaimsClaimUpdatePacket> ENCODER = new Encoder<>();

	private final ResourceLocation dimension;
	private final int x;
	private final int z;
	private final UUID playerId;
	private final boolean forceLoaded;

	public ClientboundClaimsClaimUpdatePacket(ResourceLocation dimension, int x, int z, UUID playerId, boolean forceLoaded) {
		super();
		this.dimension = dimension;
		this.x = x;
		this.z = z;
		this.playerId = playerId;
		this.forceLoaded = forceLoaded;
	}

	@Override
	protected Encoder<ClientboundClaimsClaimUpdatePacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundClaimsClaimUpdatePacket> encoder, FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("d", dimension.toString());
		nbt.putInt("x", x);
		nbt.putInt("z", z);
		if(playerId != null) {
			nbt.putUUID("p", playerId);
			nbt.putBoolean("f", forceLoaded);
		}
		u.writeNbt(nbt);
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %d, %d, %s, %s]", dimension, x, z, playerId, forceLoaded);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimsClaimUpdatePacket> {

		@Override
		public ClientboundClaimsClaimUpdatePacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(8192));
				if(nbt == null)
					return null;
				String dimensionString = nbt.getString("d");
				if(dimensionString.isEmpty() || dimensionString.length() > 2048)
					return null;
				int x = nbt.getInt("x");
				int z = nbt.getInt("z");
				UUID playerId = null;
				boolean forceload = false;
				if(nbt.contains("p")) {
					playerId = nbt.getUUID("p");
					forceload = nbt.getBoolean("f");
				}
				return new ClientboundClaimsClaimUpdatePacket(new ResourceLocation(dimensionString), x, z, playerId, forceload);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimsClaimUpdatePacket> {
		
		@Override
		public void accept(ClientboundClaimsClaimUpdatePacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimUpdate(t.dimension, t.x, t.z, t.playerId, t.forceLoaded);
		}
		
	}

}
