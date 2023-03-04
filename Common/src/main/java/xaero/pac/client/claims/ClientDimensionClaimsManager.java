/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.claims;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.client.claims.player.ClientPlayerClaimInfoManager;
import xaero.pac.common.claims.DimensionClaimsManager;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.util.linked.LinkedChain;

public final class ClientDimensionClaimsManager extends DimensionClaimsManager<ClientPlayerClaimInfoManager, ClientRegionClaims> implements IClientDimensionClaimsManager<ClientRegionClaims>{

	public ClientDimensionClaimsManager(ResourceLocation dimension, Long2ObjectMap<ClientRegionClaims> regions, LinkedChain<ClientRegionClaims> linkedRegions) {
		super(dimension, regions, linkedRegions);
	}

	@Override
	public ClientRegionClaims create(ResourceLocation dimension, int x, int z, RegionClaimsPaletteStorage storage) {
		return ClientRegionClaims.Builder.begin().setDimension(dimension).setX(x).setZ(z).setStorage(storage).build();
	}

	public void unclaimRegion(int x, int z, ClientPlayerClaimInfoManager playerClaimInfoManager, IPlayerConfigManager configManager) {
		ClientRegionClaims region = getRegion(x, z);
		if(region != null){
			region.onRegionClaim(null, playerClaimInfoManager, configManager, false);
			removeRegion(x, z);
		}
	}

	public void claimRegion(int x, int z, RegionClaimsPaletteStorage regionStorage, ClientPlayerClaimInfoManager playerClaimInfoManager, IPlayerConfigManager configManager) {
		ClientRegionClaims region = getRegion(x, z);
		ClientRegionClaims newRegion = create(getDimension(), x, z, regionStorage);
		newRegion.onRegionClaim(region, playerClaimInfoManager, configManager, true);
		setRegion(x, z, newRegion);
	}

}
