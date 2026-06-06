/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.ClaimingModeLimits;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ClientboundClaimLimitsPacket extends LazyPacket<ClientboundClaimLimitsPacket> {
	
	public static final Encoder<ClientboundClaimLimitsPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final Collection<ClaimingModeLimits> limits;
	private final int maxClaimDistance;
	private final boolean alwaysUseLoadingValues;

	public ClientboundClaimLimitsPacket(
			Collection<ClaimingModeLimits> limits,
			int maxClaimDistance,
			boolean alwaysUseLoadingValues
	) {
		super();
		this.limits = limits;
		this.maxClaimDistance = maxClaimDistance;
		this.alwaysUseLoadingValues = alwaysUseLoadingValues;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		CompoundTag limitsTag = new CompoundTag();
		for (ClaimingModeLimits modeLimits : limits) {
			CompoundTag modeLimitsTag = new CompoundTag();
			modeLimitsTag.putInt("cc", modeLimits.claimCount);
			modeLimitsTag.putInt("fc", modeLimits.forceloadCount);
			if(modeLimits.claimLimit != -1)
				modeLimitsTag.putInt("cl", modeLimits.claimLimit);
			if(modeLimits.forceloadLimit != -1)
				modeLimitsTag.putInt("fl", modeLimits.forceloadLimit);
			limitsTag.put(modeLimits.mode.getId(), modeLimitsTag);
		}
		tag.put("l", limitsTag);
		tag.putInt("d", maxClaimDistance);
		tag.putBoolean("a", alwaysUseLoadingValues);
		u.writeNbt(tag);
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimLimitsPacket> getDecoder() {
		return DECODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimLimitsPacket> {
		
		@Override
		public ClientboundClaimLimitsPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 2048 * ClaimingModes.ALL_IMMUTABLE.size())
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				CompoundTag limitsTag = tag.getCompound("l");
				if(limitsTag.isEmpty())
					return null;
				List<ClaimingModeLimits> limits = new ArrayList<>();
				for (String modeId : limitsTag.getAllKeys()) {
					if(modeId.length() > 100){
						OpenPartiesAndClaims.LOGGER.info("Claiming mode ID string is too long!");
						return null;
					}
					CompoundTag modeLimitsTag = limitsTag.getCompound(modeId);
					if(modeLimitsTag.isEmpty())
						continue;
					ClaimingMode mode = (ClaimingMode) ClaimingModes.get(modeId);
					if(mode == null)
						continue;
					int claimCount = modeLimitsTag.getInt("cc");
					int forceloadCount = modeLimitsTag.getInt("fc");
					int claimLimit = -1;
					int forceloadLimit = -1;
					if(modeLimitsTag.contains("cl", Tag.TAG_INT))
						claimLimit = modeLimitsTag.getInt("cl");
					if(modeLimitsTag.contains("fl", Tag.TAG_INT))
						forceloadLimit = modeLimitsTag.getInt("fl");
					ClaimingModeLimits modeLimits = new ClaimingModeLimits(mode, claimCount, forceloadCount, claimLimit, forceloadLimit);
					limits.add(modeLimits);
				}
				int maxClaimDistance = tag.getInt("d");
				boolean alwaysUseLoadingValues = tag.getBoolean("a");
				return new ClientboundClaimLimitsPacket(
						limits,
						maxClaimDistance,
						alwaysUseLoadingValues
				);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundClaimLimitsPacket> {
		
		@Override
		public void handle(ClientboundClaimLimitsPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimLimits(
					t.limits, t.maxClaimDistance, t.alwaysUseLoadingValues
			);
		}
		
	}

}
