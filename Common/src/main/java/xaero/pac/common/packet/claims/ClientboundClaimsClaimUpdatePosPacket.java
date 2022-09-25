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
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimsClaimUpdatePosPacket extends LazyPacket<LazyPacket.Encoder<ClientboundClaimsClaimUpdatePosPacket>, ClientboundClaimsClaimUpdatePosPacket> {

	public static final Encoder<ClientboundClaimsClaimUpdatePosPacket> ENCODER = new Encoder<>();
	private final int x;
	private final int z;

	public ClientboundClaimsClaimUpdatePosPacket(int x, int z) {
		super();
		this.x = x;
		this.z = z;
	}

	@Override
	protected Encoder<ClientboundClaimsClaimUpdatePosPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundClaimsClaimUpdatePosPacket> encoder, FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("x", x);
		nbt.putInt("z", z);
		u.writeNbt(nbt);
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %d]", x, z);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimsClaimUpdatePosPacket> {

		@Override
		public ClientboundClaimsClaimUpdatePosPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(1024));
				if(nbt == null)
					return null;
				int x = nbt.getInt("x");
				int z = nbt.getInt("z");
				return new ClientboundClaimsClaimUpdatePosPacket(x, z);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimsClaimUpdatePosPacket> {
		
		@Override
		public void accept(ClientboundClaimsClaimUpdatePosPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimUpdatePos(t.x, t.z);
		}
		
	}

}
