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

package xaero.pac.common.server.claims;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.DimensionClaimsManager;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.util.linked.LinkedChain;

public final class ServerDimensionClaimsManager extends DimensionClaimsManager<ServerPlayerClaimInfoManager, ServerRegionClaims> implements IServerDimensionClaimsManager<ServerRegionClaims> {
	
	private final ServerClaimsManager manager;
	private final boolean playerClaimsSyncAllowed;

	public ServerDimensionClaimsManager(ResourceLocation dimension, Long2ObjectMap<ServerRegionClaims> claims, LinkedChain<ServerRegionClaims> linkedRegions, ServerClaimsManager manager, boolean playerClaimsSyncAllowed) {
		super(dimension, claims, linkedRegions);
		this.manager = manager;
		this.playerClaimsSyncAllowed = playerClaimsSyncAllowed;
	}

	@Override
	protected ServerRegionClaims create(ResourceLocation dimension, int x, int z, RegionClaimsPaletteStorage storage) {
		return ServerRegionClaims.Builder.begin().setPlayerClaimsSyncAllowed(playerClaimsSyncAllowed).setDimension(dimension).setManager(manager).setX(x).setZ(z).build();
	}

}
