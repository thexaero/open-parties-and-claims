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

package xaero.pac.client.claims.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.util.linked.LinkedChain;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;

//only used by ClientClaimsManager
public final class ClientPlayerClaimInfoManager extends PlayerClaimInfoManager<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager> {

	public ClientPlayerClaimInfoManager(Map<UUID, ClientPlayerClaimInfo> storage, LinkedChain<ClientPlayerClaimInfo> linkedPlayerInfo) {
		super(storage, linkedPlayerInfo);
	}

	@Override
	protected ClientPlayerClaimInfo create(String username, UUID playerId,
			Map<ResourceLocation, PlayerDimensionClaims> claims) {
		return new ClientPlayerClaimInfo(username, playerId, claims, this);
	}
	
	public void updatePlayerInfo(UUID playerId, String username, String claimsName, int claimsColor, ClientClaimsManager claimsManager) {
		ClientPlayerClaimInfo playerInfo = getInfo(playerId);
		playerInfo.setPlayerUsername(username);
		playerInfo.setClaimsName(claimsName);
		int oldColor = playerInfo.getClaimsColor();
		playerInfo.setClaimsColor(claimsColor);
		if(oldColor != claimsColor) {
			boolean notManyClaims = playerInfo.getClaimCount() < 1024;
			ClaimsManagerTracker tracker = claimsManager.getTracker();
			playerInfo.getStream().map(Entry::getValue).forEach(dim -> {
				ResourceLocation dimensionId = dim.getDimension();
				if(notManyClaims) {
					BiConsumer<PlayerChunkClaim, ChunkPos> claimConsumer = (claim, pos) -> tracker.onChunkChange(dimensionId, pos.x, pos.z, claim);
					dim.getStream().forEach(posList -> {
						PlayerChunkClaim state = posList.getClaimState();
						posList.getStream().forEach(pos -> claimConsumer.accept(state, pos));
					});
				} else
					tracker.onDimensionChange(dimensionId);
			});
		}
	}

}
