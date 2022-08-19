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
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.*;

public final class ClaimsManagerPlayerRegionSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private final List<ClaimsManagerPlayerDimensionRegionSync> dimsToSync;
	private ClaimsManagerPlayerDimensionRegionSync currentPrefix;
	private final ClaimsManagerPlayerStateSync stateSyncHandler;
	private boolean calledOnce;

	private ClaimsManagerPlayerRegionSync(ClaimsManagerSynchronizer synchronizer, List<ClaimsManagerPlayerDimensionRegionSync> dimsToSync, ClaimsManagerPlayerStateSync stateSyncHandler) {
		super(synchronizer);
		this.dimsToSync = dimsToSync;
		this.stateSyncHandler = stateSyncHandler;
	}
	
	private void sendDimensionPrefix(ServerPlayer player, ClaimsManagerPlayerDimensionRegionSync dim) {
		if(dim != currentPrefix) {
			ResourceLocation dimLocation = dim == null ? null : dim.getDim();
			synchronizer.syncDimensionIdToClient(dimLocation, player);
			currentPrefix = dim;
		}
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData, ServerPlayer player, int limit) {
		calledOnce = true;
		int count = 0;
		while(!dimsToSync.isEmpty()) {
			ClaimsManagerPlayerDimensionRegionSync dim = dimsToSync.get(0);
			sendDimensionPrefix(player, dim);
			count += dim.handle(serverData, player, synchronizer, limit - count);
			if(count >= limit)
				break;
			dimsToSync.remove(0);
		}
		//OpenPartiesAndClaims.LOGGER.info("scheduled regions " + count);
		boolean done = dimsToSync.isEmpty();
		if(done) {
			sendDimensionPrefix(player, null);
			synchronizer.endSyncing(player);
		}
	}

	@Override
	public void start(ServerPlayer player) {
		super.start(player);
		if(dimsToSync.isEmpty()) {
			sendDimensionPrefix(player, null);
			synchronizer.endSyncing(player);
		}
	}

	@Override
	public void onLazyPacketsDropped(){
		dimsToSync.clear();
	}

	@Override
	public boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData, ServerPlayer player) {
		return started && stateSyncHandler.isFinished() && (!calledOnce || !dimsToSync.isEmpty());
	}
	
	public static final class Builder {
		private IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager;
		private ClaimsManagerPlayerStateSync stateSyncHandler;
		private UUID playerId;

		private Builder() {}
		
		private Builder setDefault() {
			setClaimsManager(null);
			setPlayerId(null);
			return this;
		}
		
		public Builder setClaimsManager(
				IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager) {
			this.claimsManager = claimsManager;
			return this;
		}

		public Builder setStateSyncHandler(ClaimsManagerPlayerStateSync stateSyncHandler) {
			this.stateSyncHandler = stateSyncHandler;
			return this;
		}

		public Builder setPlayerId(UUID playerId) {
			this.playerId = playerId;
			return this;
		}

		private void addAllowedClaimsFor(UUID id, ResourceLocation dim, Set<PlayerChunkClaim> allowedClaimStates){
			IServerPlayerClaimInfo<?> playerClaimInfo = claimsManager.getPlayerInfo(id);
			IPlayerDimensionClaims<?> playerDimensionClaims = playerClaimInfo.getDimension(dim);
			if (playerDimensionClaims != null)
				playerDimensionClaims.getStream().forEach(posList -> allowedClaimStates.add((PlayerChunkClaim) posList.getClaimState()));
		}

		public ClaimsManagerPlayerRegionSync build() {
			if(claimsManager == null || stateSyncHandler == null || playerId == null)
				throw new IllegalStateException();
			List<ClaimsManagerPlayerDimensionRegionSync> dimsToSync = new ArrayList<>();

			if(ServerConfig.CONFIG.claimsSynchronization.get() != ServerConfig.ClaimsSyncType.NOT_SYNCED) {
				((ServerClaimsManager) (Object) claimsManager).getDimensionStream().forEach(dim -> {
					Set<PlayerChunkClaim> allowedClaimStates = null;
					if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.OWNED_ONLY){
						allowedClaimStates = new HashSet<>();
						//adding all claim states for the current player and the server claims
						//will be used when filtering the synced region claim data
						addAllowedClaimsFor(playerId, dim.getDimension(), allowedClaimStates);
						addAllowedClaimsFor(PlayerConfig.SERVER_CLAIM_UUID, dim.getDimension(), allowedClaimStates);
					}
					dimsToSync.add(new ClaimsManagerPlayerDimensionRegionSync(dim, allowedClaimStates));
				});
			}

			ClaimsManagerPlayerRegionSync result = new ClaimsManagerPlayerRegionSync((ClaimsManagerSynchronizer) claimsManager.getClaimsManagerSynchronizer(), dimsToSync, stateSyncHandler);
			return result;
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
