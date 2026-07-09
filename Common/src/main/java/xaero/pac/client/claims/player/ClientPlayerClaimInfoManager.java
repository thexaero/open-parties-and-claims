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

package xaero.pac.client.claims.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
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
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

//only used by ClientClaimsManager
public final class ClientPlayerClaimInfoManager extends PlayerClaimInfoManager<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager, ClientClaimsManager> {

	private boolean partyOwnedClaims;

	public ClientPlayerClaimInfoManager(Map<UUID, ClientPlayerClaimInfo> storage, LinkedChain<ClientPlayerClaimInfo> linkedPlayerInfo) {
		super(storage, linkedPlayerInfo);
	}

	@Override
	protected ClientPlayerClaimInfo create(String username, UUID playerId,
			Map<ResourceLocation, PlayerDimensionClaims> claims) {
		return new ClientPlayerClaimInfo(username, playerId, claims, this, new Int2ObjectOpenHashMap<>());
	}

	@Override
	protected void onAdd(ClientPlayerClaimInfo playerInfo) {
		super.onAdd(playerInfo);
		updateSubClaimInfo(playerInfo.getPlayerId(), -1, "", 0);//ensuring something is always there for the main sub
	}

	public void updatePlayerInfo(UUID playerId, String username, Component partyName, boolean partyOwned) {
		ClientPlayerClaimInfo playerInfo = getInfo(playerId);
		playerInfo.setPlayerUsername(username);
		playerInfo.setPartyName(partyName);
		playerInfo.setPartyOwned(partyOwned);
	}

	public void updateSubClaimInfo(UUID playerId, int subConfigIndex, String claimsName, Integer claimsColor) {
		ClientPlayerClaimInfo playerInfo = getInfo(playerId);
		playerInfo.ensureSubClaim(subConfigIndex);
		playerInfo.setClaimsName(subConfigIndex, claimsName);
		Integer oldColor = playerInfo.getClaimsColor(subConfigIndex);
		playerInfo.setClaimsColor(subConfigIndex, claimsColor);
		if(!Objects.equals(oldColor, claimsColor)) {
			boolean notManyClaims = playerInfo.getClaimCount() < 1024;
			boolean isSub = subConfigIndex != -1;
			ClaimsManagerTracker tracker = claimsManager.getTracker();
			playerInfo.getTypedStream().map(Entry::getValue).forEach(dim -> {
				ResourceLocation dimensionId = dim.getDimension();
				if(notManyClaims) {
					BiConsumer<PlayerChunkClaim, ChunkPos> claimConsumer = (claim, pos) -> tracker.onChunkChange(dimensionId, pos.x, pos.z, claim);
					dim.getTypedStream().forEach(posList -> {
						PlayerChunkClaim state = posList.getClaimState();
						int claimSubConfigIndex = state.getSubConfigIndex();
						if(claimSubConfigIndex != subConfigIndex && (isSub || playerInfo.getClaimsColor(claimSubConfigIndex) != null))
							return;
						posList.getStream().forEach(pos -> claimConsumer.accept(state, pos));
					});
				} else
					tracker.onDimensionChange(dimensionId);
			});
		}
	}

	public void setPartyOwnedClaims(boolean partyOwnedClaims) {
		this.partyOwnedClaims = partyOwnedClaims;
	}

	@Override
	public boolean usingPartyOwnedClaims() {
		return partyOwnedClaims;
	}

}
