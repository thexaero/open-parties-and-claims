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

package xaero.pac.common.server.claims.sync.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.ServerDimensionClaimsManager;
import xaero.pac.common.server.claims.ServerRegionClaims;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;

import java.util.Iterator;
import java.util.Set;

public class ClaimsManagerPlayerDimensionRegionSync {

	private final ServerDimensionClaimsManager dimensionClaims;
	private final Iterator<ServerRegionClaims> iterator;
	private final Set<PlayerChunkClaim> allowedClaimStates;
	
	ClaimsManagerPlayerDimensionRegionSync(ServerDimensionClaimsManager dimensionClaims, Set<PlayerChunkClaim> allowedClaimStates) {
		super();
		this.dimensionClaims = dimensionClaims;
		this.allowedClaimStates = allowedClaimStates;
		this.iterator = dimensionClaims.iterator();
	}
	
	public int handle(IServerData<?,?> serverData, ServerPlayer player, ClaimsManagerSynchronizer synchronizer, int limit) {
		if(iterator.hasNext()) {
			int count = 0;
			while(iterator.hasNext()) {
				ServerRegionClaims region = iterator.next();
				boolean shouldSkip = false;
				if(this.allowedClaimStates != null){
					shouldSkip = true;
					for(PlayerChunkClaim allowedState : this.allowedClaimStates) {
						if (region.containsState(allowedState)) {
							shouldSkip = false;
							break;
						}
					}
				}
				if(!shouldSkip) {
					int paletteInts[] = region.getSyncablePaletteArray();
					long[] storageDataCopy = region.getSyncableStorageData();
					int storageBits = region.getSyncableStorageBits();
					synchronizer.syncRegionClaimsToClient(region.getX(), region.getZ(), paletteInts, storageDataCopy, storageBits, player);
				}
				count++;
				if(count >= limit)
					break;
			}
			return count;
		}
		return 0;
	}
	
	public ResourceLocation getDim() {
		return dimensionClaims.getDimension();
	}
	
}
