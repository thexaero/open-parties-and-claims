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

package xaero.pac.client.claims;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.client.claims.player.ClientPlayerClaimInfoManager;
import xaero.pac.common.claims.DimensionClaimsManager;

public final class ClientDimensionClaimsManager extends DimensionClaimsManager<ClientPlayerClaimInfoManager, ClientRegionClaims> implements IClientDimensionClaimsManager<ClientRegionClaims>{

	public ClientDimensionClaimsManager(ResourceLocation dimension, Long2ObjectMap<ClientRegionClaims> claims) {
		super(dimension, claims);
	}

	@Override
	protected ClientRegionClaims create(ResourceLocation dimension, int x, int z) {
		return ClientRegionClaims.Builder.begin().setDimension(dimension).setX(x).setZ(z).build();
	}

}
