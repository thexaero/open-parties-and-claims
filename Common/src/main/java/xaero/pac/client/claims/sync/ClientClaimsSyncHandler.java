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

package xaero.pac.client.claims.sync;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BitStorage;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.common.claims.PlayerChunkClaimHolder;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientClaimsSyncHandler {
	
	private final ClientClaimsManager claimsManager;
	private ResourceLocation dimensionSyncing;
	
	public ClientClaimsSyncHandler(ClientClaimsManager claimsManager) {
		super();
		this.claimsManager = claimsManager;
	}
	
	public void onPlayerInfo(UUID playerId, String username, String claimsName, int claimsColor) {
		claimsManager.getPlayerClaimInfoManager().updatePlayerInfo(playerId, username, claimsName, claimsColor, claimsManager);
	}
	
	public void onClaimState(PlayerChunkClaim claim) {
		claimsManager.addClaimState(claim);
	}
	
	public void onLoading(boolean start) {
		claimsManager.setLoading(start);
	}
	
	public void onClaimLimits(int loadingClaimCount, int loadingForceloadCount, int claimLimit,
			int forceloadLimit, int maxClaimDistance, boolean alwaysUseLoadingValues) {
		claimsManager.setLoadingClaimCount(loadingClaimCount);
		claimsManager.setLoadingForceloadCount(loadingForceloadCount);
		claimsManager.setClaimLimit(claimLimit);
		claimsManager.setForceloadLimit(forceloadLimit);
		claimsManager.setMaxClaimDistance(maxClaimDistance);
		claimsManager.setAlwaysUseLoadingValues(alwaysUseLoadingValues);
	}

	public void onDimension(ResourceLocation dim) {
		this.dimensionSyncing = dim;
	}
	
	public void onClaimUpdate(ResourceLocation dimension, int x, int z, UUID playerId, boolean forceload, int claimSyncIndex) {
		if(playerId != null) {
			if(claimsManager.getClaimStateBySyncIndex(claimSyncIndex) == null)
				claimsManager.addClaimState(new PlayerChunkClaim(playerId, forceload, claimSyncIndex));
			claimsManager.claim(dimension, playerId, x, z, forceload);
		} else
			claimsManager.unclaim(dimension, x, z);
	}
	
	public void onRegion(int x, int z, int[] paletteInts, BitStorage storage) {
		Map<PlayerChunkClaim, Integer> paletteHelper = new HashMap<>();
		ArrayList<PlayerChunkClaimHolder> palette = Lists.newArrayList((PlayerChunkClaimHolder)null);
		for(int i = 0; i < paletteInts.length; i++){
			PlayerChunkClaim claim = claimsManager.getClaimStateBySyncIndex(paletteInts[i]);
			if(claim != null)//can be null depending on sync mode
				paletteHelper.put(claim, palette.size()/*not i*/);
			palette.add(new PlayerChunkClaimHolder(claim, new byte[32]));
		}
		RegionClaimsPaletteStorage newRegionStorage = new RegionClaimsPaletteStorage(paletteHelper, null, palette, storage, false);
		newRegionStorage.setNeedsHolderRecalculation(true);//will calculate holder data when there is an attempt to modify the region
		claimsManager.claimRegion(dimensionSyncing, x, z, newRegionStorage);
	}
	
	public void onClaimResult(AreaClaimResult result) {
		claimsManager.getClaimResultTracker().onClaimResult(result);
	}

	public void onClaimModes(boolean adminMode, boolean serverMode) {
		claimsManager.setAdminMode(adminMode);
		claimsManager.setServerMode(serverMode);
	}

}
