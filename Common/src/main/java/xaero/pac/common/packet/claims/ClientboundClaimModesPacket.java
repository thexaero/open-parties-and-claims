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
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.impersonation.PlayerClaimImpersonationInfo;
import xaero.pac.common.claims.player.impersonation.SimplePlayerClaimImpersonationInfo;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimModesPacket {

	private final boolean moderatorMode;
	private final boolean adminMode;
	private final ClaimingMode claimingMode;
	private final SimplePlayerClaimImpersonationInfo claimsImpersonationInfo;

	public ClientboundClaimModesPacket(boolean moderatorMode, boolean adminMode, ClaimingMode claimingMode, SimplePlayerClaimImpersonationInfo claimsImpersonationInfo) {
		super();
		this.moderatorMode = moderatorMode;
		this.adminMode = adminMode;
		this.claimingMode = claimingMode;
		this.claimsImpersonationInfo = claimsImpersonationInfo;
	}
	
	public static class Codec implements BiConsumer<ClientboundClaimModesPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundClaimModesPacket> {
		
		@Override
		public ClientboundClaimModesPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				boolean moderatorMode = tag.getBooleanOr("mm", false);
				boolean adminMode = tag.getBooleanOr("am", false);
				ClaimingMode claimingMode = (ClaimingMode) ClaimingModes.get(tag.getStringOr("cm", ""));
				SimplePlayerClaimImpersonationInfo claimsImpersonationInfo;
				CompoundTag claimsImpersonationInfoTag = tag.getCompoundOrEmpty("cii");
				if(!claimsImpersonationInfoTag.isEmpty()) {
					UUID claimsImpersonatedPlayerId = XaeroNbtUtil.getUUID(claimsImpersonationInfoTag, "pi").orElse(null);
					claimsImpersonationInfo = new SimplePlayerClaimImpersonationInfo(claimsImpersonatedPlayerId);
					CompoundTag claimIdsTag = claimsImpersonationInfoTag.getCompoundOrEmpty("ci");
					for (String claimModeKey : claimIdsTag.keySet()) {
						IClaimingModeAPI claimMode = ClaimingModes.get(claimModeKey);
						if(claimMode == null)
							continue;
						claimsImpersonationInfo.setClaimPlayerId(claimMode, XaeroNbtUtil.getUUID(claimIdsTag, claimModeKey).get());
					}
					CompoundTag subIndicesTag = claimsImpersonationInfoTag.getCompoundOrEmpty("si");
					for (String claimModeKey : subIndicesTag.keySet()) {
						IClaimingModeAPI claimMode = ClaimingModes.get(claimModeKey);
						if(claimMode == null)
							continue;
						claimsImpersonationInfo.setSubIndex(claimMode, subIndicesTag.getIntOr(claimModeKey, 0));
					}
				} else
					claimsImpersonationInfo = new SimplePlayerClaimImpersonationInfo(null);
				return new ClientboundClaimModesPacket(moderatorMode, adminMode, claimingMode, claimsImpersonationInfo);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}

		@Override
		public void accept(ClientboundClaimModesPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("mm", t.moderatorMode);
			tag.putBoolean("am", t.adminMode);
			if(t.claimingMode != null)
				tag.putString("cm", t.claimingMode.getId());
			if(t.claimsImpersonationInfo != null) {
				CompoundTag claimsImpersonationInfoTag = new CompoundTag();
				if(t.claimsImpersonationInfo.getPlayerId() != null)
					XaeroNbtUtil.putUUID(claimsImpersonationInfoTag, "pi", t.claimsImpersonationInfo.getPlayerId());
				CompoundTag claimIdsTag = new CompoundTag();
				for (Map.Entry<ClaimingMode, UUID> claimIdEntry : t.claimsImpersonationInfo.getClaimPlayerIds().entrySet())
					XaeroNbtUtil.putUUID(claimIdsTag, claimIdEntry.getKey().getId(), claimIdEntry.getValue());
				claimsImpersonationInfoTag.put("ci", claimIdsTag);
				CompoundTag subIndicesTag = new CompoundTag();
				for (Map.Entry<ClaimingMode, Integer> subIndexEntry : t.claimsImpersonationInfo.getSubIndices().entrySet())
					subIndicesTag.putInt(subIndexEntry.getKey().getId(), subIndexEntry.getValue());
				claimsImpersonationInfoTag.put("si", subIndicesTag);
				tag.put("cii", claimsImpersonationInfoTag);
			}
			u.writeNbt(tag);
		}

	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimModesPacket> {
		
		@Override
		public void accept(ClientboundClaimModesPacket t) {
			if(t == null)
				return;
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimModes(t.moderatorMode, t.adminMode, t.claimingMode, t.claimsImpersonationInfo);
		}
		
	}

	public static ClientboundClaimModesPacket get(ServerPlayerDataAPI playerData){
		ServerPlayerData serverPlayerData = (ServerPlayerData) playerData;
		PlayerClaimImpersonationInfo serverClaimImpersonationInfo = serverPlayerData.getClaimsImpersonationInfo();
		SimplePlayerClaimImpersonationInfo packetPlayerClaimImpersonationInfo = serverClaimImpersonationInfo.createSnapshot();
		return new ClientboundClaimModesPacket(
				serverPlayerData.isClaimsModeratorMode(),
				serverPlayerData.isClaimsAdminMode(),
				(ClaimingMode) playerData.getRawClaimingMode(),
				packetPlayerClaimImpersonationInfo);
	}
	
}
