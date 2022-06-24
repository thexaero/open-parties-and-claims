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
import xaero.pac.common.server.claims.ServerRegionClaims;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;

import java.util.List;

public class ClaimsManagerPlayerDimensionSyncHandler {

	private final ResourceLocation dim;
	private final List<ServerRegionClaims> regionsToSync;
	
	ClaimsManagerPlayerDimensionSyncHandler(ResourceLocation dim, List<ServerRegionClaims> regionsToSync) {
		super();
		this.dim = dim;
		this.regionsToSync = regionsToSync;
	}
	
	public int handle(ServerPlayer player, ClaimsManagerSynchronizer synchronizer, int limit) {
		if(!regionsToSync.isEmpty()) {
			int count = 0;
			while(!regionsToSync.isEmpty()) {
				ServerRegionClaims region = regionsToSync.remove(regionsToSync.size() - 1);
				if(!region.isRemoved()) {
					int paletteInts[] = region.getSyncablePaletteArray();
					long[] storageDataCopy = region.getSyncableStorageData();
					int storageBits = region.getSyncableStorageBits();
					synchronizer.syncRegionClaimsToClient(region.getX(), region.getZ(), paletteInts, storageDataCopy, storageBits, player);
					count++;
					if(count >= limit)
						break;
				}
			}
			return count;
		}
		return 0;
	}
	
	public ResourceLocation getDim() {
		return dim;
	}
	
}
