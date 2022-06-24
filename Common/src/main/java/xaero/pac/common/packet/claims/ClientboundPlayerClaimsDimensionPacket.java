/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypackets.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPlayerClaimsDimensionPacket extends LazyPacket<LazyPacket.Encoder<ClientboundPlayerClaimsDimensionPacket>, ClientboundPlayerClaimsDimensionPacket>{
	
	public static final Encoder<ClientboundPlayerClaimsDimensionPacket> ENCODER = new Encoder<>();

	private final ResourceLocation dimension;

	public ClientboundPlayerClaimsDimensionPacket(ResourceLocation dimension) {
		super();
		this.dimension = dimension;
	}

	@Override
	protected Encoder<ClientboundPlayerClaimsDimensionPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundPlayerClaimsDimensionPacket> encoder, FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		if(dimension != null)
			nbt.putString("d", dimension.toString());//4096
		u.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundPlayerClaimsDimensionPacket> {

		@Override
		public ClientboundPlayerClaimsDimensionPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(524288));
				if(nbt == null)
					return null;
				String dimensionString = nbt.contains("d") ? nbt.getString("d") : "";
				if(dimensionString.length() > 2048)
					return null;
				return new ClientboundPlayerClaimsDimensionPacket(dimensionString.isEmpty() ? null : new ResourceLocation(dimensionString));
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPlayerClaimsDimensionPacket> {
		
		@Override
		public void accept(ClientboundPlayerClaimsDimensionPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onDimension(t.dimension);
		}
		
	}
	
}
