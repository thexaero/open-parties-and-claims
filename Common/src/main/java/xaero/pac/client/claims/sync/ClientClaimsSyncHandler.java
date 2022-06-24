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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BitStorage;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.result.api.AreaClaimResult;

import java.util.UUID;

public class ClientClaimsSyncHandler {
	
	private final ClientClaimsManager claimsManager;
	private ResourceLocation dimensionSyncingOwned;
	private UUID ownerSyncingOwned;
	
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

	public void onOwner(UUID ownerId) {
		ownerSyncingOwned = ownerId;
	}
	
	public void onDimension(ResourceLocation dim) {
		this.dimensionSyncingOwned = dim;
	}
	
	public void onLoading(boolean start) {
		claimsManager.setLoading(start);
	}
	
	public void onClaimLimits(int loadingClaimCount, int loadingForceloadCount, int claimLimit,
			int forceloadLimit, int maxClaimDistance) {
		claimsManager.setLoadingClaimCount(loadingClaimCount);
		claimsManager.setLoadingForceloadCount(loadingForceloadCount);
		claimsManager.setClaimLimit(claimLimit);
		claimsManager.setForceloadLimit(forceloadLimit);
		claimsManager.setMaxClaimDistance(maxClaimDistance);
	}
	
	public void onOwnedClaim(int x, int z, boolean forceload) {
		if(dimensionSyncingOwned != null)
			onClaimUpdate(dimensionSyncingOwned, x, z, ownerSyncingOwned, forceload);
	}
	
	public void onClaimUpdate(ResourceLocation dimension, int x, int z, UUID playerId, boolean forceload) {
		PlayerChunkClaim newClaim = null;
		if(playerId != null)
			newClaim = claimsManager.claim(dimension, playerId, x, z, forceload);
		else
			claimsManager.unclaim(dimension, x, z);
		claimsManager.getTracker().onChunkChange(dimension, x, z, newClaim);
	}
	
	public void onRegion(int x, int z, int[] paletteInts, BitStorage storage) {
		int index = 0;
		for(int i = 0; i < 32; i++)
			for(int j = 0; j < 32; j++) {
				int paletteIndex = storage.get(index++);
				PlayerChunkClaim claimState = null;
				if(paletteIndex > 0) {
					int paletteElement = paletteInts[paletteIndex - 1];
					claimState = claimsManager.getClaimState(paletteElement);
				}
				int chunkX = (x << 5) | i;
				int chunkZ = (z << 5) | j;
				if(claimState == null)
					claimsManager.unclaim(dimensionSyncingOwned, chunkX, chunkZ);
				else
					claimsManager.claim(dimensionSyncingOwned, claimState.getPlayerId(), chunkX, chunkZ, claimState.isForceloadable());
			}
		claimsManager.getTracker().onWholeRegionChange(dimensionSyncingOwned, x, z);
	}
	
	public void onClaimResult(AreaClaimResult result) {
		claimsManager.getClaimResultTracker().onClaimResult(result);
	}

}
