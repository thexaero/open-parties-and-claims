/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.ClaimingAnchor;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.PlayerClaimPosList;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerPlayerDimensionClaims extends PlayerDimensionClaims {

	public ServerPlayerDimensionClaims(UUID playerId, ResourceLocation dimension, Map<PlayerChunkClaim, PlayerClaimPosList> claimLists, Map<ChunkPos, ClaimingAnchor> anchors, ServerClaimsManager claimsManager) {
		super(playerId, dimension, claimLists, anchors, claimsManager);
	}

	@Override
	public boolean addAnchor(@Nonnull ChunkPos pos) {
		if(super.addAnchor(pos)) {
			ServerClaimsManager serverClaimsManager = (ServerClaimsManager) claimsManager;
			if(serverClaimsManager.isLoaded() && serverClaimsManager.usingAnchorBasedClaiming())
				serverClaimsManager.getClaimsManagerSynchronizer().syncClaimAnchors(null, playerId, dimension, List.of(pos), true);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAnchor(@Nonnull ChunkPos pos) {
		if(super.removeAnchor(pos)) {
			ServerClaimsManager serverClaimsManager = (ServerClaimsManager) claimsManager;
			if(serverClaimsManager.isLoaded() && serverClaimsManager.usingAnchorBasedClaiming())
				serverClaimsManager.getClaimsManagerSynchronizer().syncClaimAnchors(null, playerId, dimension, List.of(pos), false);
			return true;
		}
		return false;
	}

}
