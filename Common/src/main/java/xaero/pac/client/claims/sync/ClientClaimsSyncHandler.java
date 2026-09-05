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

package xaero.pac.client.claims.sync;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.client.claims.player.ClientPlayerClaimInfo;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.claims.PlayerChunkClaimHolder;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.impersonation.SimplePlayerClaimImpersonationInfo;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.ClaimingModeLimits;
import xaero.pac.common.claims.player.mode.ClaimingModeSubInfo;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ClientClaimsSyncHandler {
	
	private final ClientClaimsManager claimsManager;
	private ResourceLocation dimensionSyncing;

	private ResourceLocation lastClaimUpdateDimension;
	private PlayerChunkClaim lastClaimUpdateState;
	private int lastClaimUpdateX;
	private int lastClaimUpdateZ;
	
	public ClientClaimsSyncHandler(ClientClaimsManager claimsManager) {
		super();
		this.claimsManager = claimsManager;
	}
	
	public void onPlayerInfo(UUID playerId, String username, Component partyName, boolean partyOwned) {
		claimsManager.getPlayerClaimInfoManager().updatePlayerInfo(playerId, username, partyName, partyOwned);
	}

	public void onSubClaimInfo(UUID playerId, int subConfigIndex, String claimsName, Integer claimsColor) {
		claimsManager.getPlayerClaimInfoManager().updateSubClaimInfo(playerId, subConfigIndex, claimsName, claimsColor);
	}
	
	public void onClaimState(PlayerChunkClaim claim) {
		claimsManager.addClaimState(claim);
	}
	
	public void onLoading(boolean start) {
		claimsManager.setLoading(start);
	}
	
	public void onClaimLimits(
			Collection<ClaimingModeLimits> limits,
			int maxClaimDistance,
			boolean alwaysUseLoadingValues
	) {
		for (ClaimingModeLimits modeLimit : limits)
			claimsManager.updateLimits(modeLimit);
		claimsManager.setMaxClaimDistance(maxClaimDistance);
		claimsManager.setAlwaysUseLoadingValues(alwaysUseLoadingValues);
	}

	public void onSubConfigIndices(Collection<ClaimingModeSubInfo> subInfoCollection){
		for (ClaimingModeSubInfo subInfo : subInfoCollection)
			claimsManager.updateSubInfo(subInfo);
	}

	public void onDimension(ResourceLocation dim) {
		this.dimensionSyncing = dim;
	}
	
	public void onClaimUpdate(ResourceLocation dimension, int x, int z, UUID playerId, int subConfigIndex, boolean forceload, int claimSyncIndex) {
		if(playerId != null) {
			if(claimsManager.getClaimStateBySyncIndex(claimSyncIndex) == null)
				claimsManager.addClaimState(new PlayerChunkClaim(playerId, subConfigIndex, forceload, claimSyncIndex));
			lastClaimUpdateState = claimsManager.claim(dimension, playerId, subConfigIndex, x, z, forceload);
		} else {
			claimsManager.unclaim(dimension, x, z);
			lastClaimUpdateState = null;
		}
		lastClaimUpdateDimension = dimension;
		lastClaimUpdateX = x;
		lastClaimUpdateZ = z;
	}

	public void onClaimUpdatePos(int x, int z) {
		if(lastClaimUpdateDimension == null)
			throw new IllegalStateException();
		if(lastClaimUpdateState != null)
			claimsManager.claim(lastClaimUpdateDimension, lastClaimUpdateState.getPlayerId(), lastClaimUpdateState.getSubConfigIndex(), x, z, lastClaimUpdateState.isForceloadable());
		else
			claimsManager.unclaim(lastClaimUpdateDimension, x, z);
		lastClaimUpdateX = x;
		lastClaimUpdateZ = z;
	}

	public void onClaimUpdateNextXPos() {
		onClaimUpdatePos(lastClaimUpdateX + 1, lastClaimUpdateZ);
	}

	public void onClaimUpdateNextZPos() {
		onClaimUpdatePos(lastClaimUpdateX, lastClaimUpdateZ + 1);
	}
	
	public void onRegion(int x, int z, int[] paletteInts, BitStorage storage) {
		Object2IntMap<PlayerChunkClaim> paletteHelper = new Object2IntOpenHashMap<>();
		ArrayList<PlayerChunkClaimHolder> palette = Lists.newArrayList((PlayerChunkClaimHolder)null);
		for(int i = 0; i < paletteInts.length; i++){
			PlayerChunkClaim claim = claimsManager.getClaimStateBySyncIndex(paletteInts[i]);
			if(claim != null)//can be null
				paletteHelper.put(claim, palette.size()/*not i*/);
			palette.add(new PlayerChunkClaimHolder(claim));//possible that the storage still points at a null (based on sync mode)
		}
		RegionClaimsPaletteStorage newRegionStorage = new RegionClaimsPaletteStorage(paletteHelper, null, palette, storage, false);
		newRegionStorage.setNeedsHolderRecalculation(true);//will calculate holder data when there is an attempt to modify the region
		claimsManager.claimRegion(dimensionSyncing, x, z, newRegionStorage);
	}
	
	public void onClaimResult(AreaClaimResult result) {
		claimsManager.getClaimResultTracker().onClaimResult(result);
	}

	public void onClaimModes(
			boolean moderatorMode,
			boolean adminMode,
			ClaimingMode claimingMode,
			SimplePlayerClaimImpersonationInfo playerImpersonation
	) {
		claimsManager.setModeratorMode(moderatorMode);
		claimsManager.setAdminMode(adminMode);
		claimsManager.setClaimingMode(claimingMode);
		claimsManager.setPlayerImpersonationInfo(playerImpersonation);
	}

	public void onClaimStateRemoved(int syncIndex) {
		PlayerChunkClaim state = claimsManager.getClaimStateBySyncIndex(syncIndex);
		if(state != null)
			claimsManager.removeClaimState(state);
	}

	public void onRemoveSubClaim(UUID playerId, int subConfigIndex) {
		claimsManager.removeSubClaim(playerId, subConfigIndex);
	}

	public void onPartyGeneral(boolean partyOwnedClaims, UUID partyOwnerId) {
		claimsManager.setPartyOwnedClaims(partyOwnedClaims);
		claimsManager.setCurrentPartyOwner(partyOwnerId);
	}

	public void reset(){
		dimensionSyncing = null;
		lastClaimUpdateState = null;
		lastClaimUpdateDimension = null;
	}

	public void onClaimsReset(boolean notifyTracker) {
		claimsManager.reset(notifyTracker);
	}

	public void onDimensionSubConfigVisualChange(
			PlayerConfigClientStorage updatedSubConfig,
			PlayerConfigOptionSpec<?> option
	) {
		if(updatedSubConfig.getOwner() == null)//wilderness is not "visible" anyway
			return;
		PlayerConfigClientStorage rootConfig = updatedSubConfig.getMain();
		ClientPlayerClaimInfo playerClaimInfo = claimsManager.getPlayerInfo(updatedSubConfig.getOwner());
		boolean notManyClaims = playerClaimInfo.getClaimCount() < 1024;
		ClaimsManagerTracker tracker = claimsManager.getTracker();
		playerClaimInfo.getTypedStream().map(Map.Entry::getValue).forEach(dim -> {
			ResourceLocation dimensionId = dim.getDimension();
			String dimensionIdString = dimensionId.toString();
			PlayerConfigClientStorage dimSubConfig = dimensionIdString.equals(updatedSubConfig.getSubId()) ? updatedSubConfig ://for when it's already been deleted
					rootConfig.getEffectiveSubConfig(dimensionIdString);
			if(dimSubConfig != updatedSubConfig &&
					(updatedSubConfig != rootConfig || option != null && dimSubConfig.getOption(option).getValue() != null))
				return;
			if(notManyClaims) {
				BiConsumer<PlayerChunkClaim, ChunkPos> claimConsumer = (state, pos) ->
						tracker.onChunkChange(dimensionId, pos.x, pos.z, state);
				dim.getTypedStream().forEach(posList -> {
					PlayerChunkClaim state = posList.getClaimState();
					posList.getStream().forEach(pos -> claimConsumer.accept(state, pos));
				});
			} else
				tracker.onDimensionChange(dimensionId);
		});
	}

}
