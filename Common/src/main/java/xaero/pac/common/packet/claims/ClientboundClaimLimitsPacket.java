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
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypackets.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimLimitsPacket extends LazyPacket<LazyPacket.Encoder<ClientboundClaimLimitsPacket>, ClientboundClaimLimitsPacket> {
	
	public static final Encoder<ClientboundClaimLimitsPacket> ENCODER = new Encoder<>();
	
	private final int loadingClaimCount;
	private final int loadingForceloadCount;
	private final int claimLimit;
	private final int forceloadLimit;
	private final int maxClaimDistance;

	public ClientboundClaimLimitsPacket(int loadingClaimCount, int loadingForceloadCount, int claimLimit,
			int forceloadLimit, int maxClaimDistance) {
		super();
		this.loadingClaimCount = loadingClaimCount;
		this.loadingForceloadCount = loadingForceloadCount;
		this.claimLimit = claimLimit;
		this.forceloadLimit = forceloadLimit;
		this.maxClaimDistance = maxClaimDistance;
	}

	@Override
	protected void writeOnPrepare(LazyPacket.Encoder<ClientboundClaimLimitsPacket> encoder, FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("cc", loadingClaimCount);
		tag.putInt("fc", loadingForceloadCount);
		tag.putInt("cl", claimLimit);
		tag.putInt("fl", forceloadLimit);
		tag.putInt("d", maxClaimDistance);
		u.writeNbt(tag);
	}

	@Override
	protected Encoder<ClientboundClaimLimitsPacket> getEncoder() {
		return ENCODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimLimitsPacket> {
		
		@Override
		public ClientboundClaimLimitsPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag tag = input.readNbt(new NbtAccounter(2048));
				if(tag == null)
					return null;
				int loadingClaimCount = tag.getInt("cc");
				int loadingForceloadCount = tag.getInt("fc");
				int claimLimit = tag.getInt("cl");
				int forceloadLimit = tag.getInt("fl");
				int maxClaimDistance = tag.getInt("d");
				return new ClientboundClaimLimitsPacket(loadingClaimCount, loadingForceloadCount, claimLimit, forceloadLimit, maxClaimDistance);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimLimitsPacket> {
		
		@Override
		public void accept(ClientboundClaimLimitsPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimLimits(t.loadingClaimCount, t.loadingForceloadCount, t.claimLimit, t.forceloadLimit, t.maxClaimDistance);
		}
		
	}
	
}
