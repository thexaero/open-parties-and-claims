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

package xaero.pac.common.packet.claims.owned;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypackets.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPlayerClaimsOwnedClaimPacket extends LazyPacket<LazyPacket.Encoder<ClientboundPlayerClaimsOwnedClaimPacket>, ClientboundPlayerClaimsOwnedClaimPacket> {

	public static final Encoder<ClientboundPlayerClaimsOwnedClaimPacket> ENCODER = new Encoder<>();

	private final int x;
	private final int z;
	private final boolean forceLoaded;

	public ClientboundPlayerClaimsOwnedClaimPacket(int x, int z, boolean forceLoaded) {
		super();
		this.x = x;
		this.z = z;
		this.forceLoaded = forceLoaded;
	}

	@Override
	protected Encoder<ClientboundPlayerClaimsOwnedClaimPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundPlayerClaimsOwnedClaimPacket> encoder, FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("x", x);
		nbt.putInt("z", z);
		nbt.putBoolean("f", forceLoaded);
		u.writeNbt(nbt);
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %d, %s]", x, z, forceLoaded);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundPlayerClaimsOwnedClaimPacket> {

		@Override
		public ClientboundPlayerClaimsOwnedClaimPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(1024));
				if(nbt == null)
					return null;
				int x = nbt.getInt("x");
				int z = nbt.getInt("z");
				boolean forceload = nbt.getBoolean("f");
				return new ClientboundPlayerClaimsOwnedClaimPacket(x, z, forceload);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPlayerClaimsOwnedClaimPacket> {
		
		@Override
		public void accept(ClientboundPlayerClaimsOwnedClaimPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler()
			.onOwnedClaim(t.x, t.z, t.forceLoaded);
		}
		
	}

}
