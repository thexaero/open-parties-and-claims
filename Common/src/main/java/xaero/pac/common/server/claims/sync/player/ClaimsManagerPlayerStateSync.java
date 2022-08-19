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

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.*;
import xaero.pac.common.packet.claims.ClientboundClaimStatesPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.*;

public final class ClaimsManagerPlayerStateSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private int syncedStateCount = 0;
	private final int totalToSync;
	private final ClaimsManagerPlayerClaimPropertiesSync claimPropertiesSync;
	private List<PlayerChunkClaim> packetBuilder;
	private Iterator<PlayerChunkClaim> specificStates;

	private ClaimsManagerPlayerStateSync(int totalToSync, ClaimsManagerSynchronizer synchronizer, ClaimsManagerPlayerClaimPropertiesSync claimPropertiesSync, Iterator<PlayerChunkClaim> specificStates) {
		super(synchronizer);
		this.totalToSync = totalToSync;
		this.claimPropertiesSync = claimPropertiesSync;
		this.specificStates = specificStates;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData, ServerPlayer player, int limit){
		if(packetBuilder == null)
			packetBuilder = startClaimStateSync();

		if(specificStates == null) {
			int syncUntil = Math.min(totalToSync, syncedStateCount + limit) - 1;
			for (int syncIndex = syncedStateCount; syncIndex <= syncUntil; syncIndex++) {
				PlayerChunkClaim state = synchronizer.getStateBySyncIndex(syncIndex);
				continueClaimStateSync(packetBuilder, state, player);
			}
			syncedStateCount = syncUntil + 1;
		} else {
			int canSync = limit;
			while(canSync > 0 && specificStates.hasNext()){
				PlayerChunkClaim state = specificStates.next();
				continueClaimStateSync(packetBuilder, state, player);
				canSync--;
			}
		}
		finalizeClaimStateSync(packetBuilder, player);

		if(isFinished())
			packetBuilder = null;
	}

	public List<PlayerChunkClaim> startClaimStateSync(){
		return new ArrayList<>(ClientboundClaimStatesPacket.MAX_STATES);
	}

	public void continueClaimStateSync(List<PlayerChunkClaim> packetBuilder, PlayerChunkClaim state, ServerPlayer player){
		packetBuilder.add(state);
		if(packetBuilder.size() == ClientboundClaimStatesPacket.MAX_STATES) {
			synchronizer.syncClaimStates(packetBuilder, player);
			packetBuilder.clear();
		}
	}

	public void finalizeClaimStateSync(List<PlayerChunkClaim> packetBuilder, ServerPlayer player){
		if(!packetBuilder.isEmpty()) {
			synchronizer.syncClaimStates(packetBuilder, player);
			packetBuilder.clear();
		}
	}

	@Override
	public void onLazyPacketsDropped() {
		syncedStateCount = totalToSync;
		specificStates = null;
	}

	public boolean isFinished(){
		return claimPropertiesSync.isFinished() &&
				(specificStates == null && syncedStateCount == totalToSync ||
						specificStates != null && !specificStates.hasNext());
	}

	@Override
	public boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData, ServerPlayer player) {
		return started && claimPropertiesSync.isFinished() && !isFinished();
	}

	public static final class Builder {

		private ServerPlayer player;
		private ClaimsManagerSynchronizer synchronizer;
		private ClaimsManagerPlayerClaimPropertiesSync claimPropertiesSync;

		private Builder(){}

		public Builder setDefault() {
			setPlayer(null);
			setSynchronizer(null);
			setClaimPropertiesSync(null);
			return this;
		}

		public Builder setPlayer(ServerPlayer player) {
			this.player = player;
			return this;
		}

		public Builder setSynchronizer(ClaimsManagerSynchronizer synchronizer) {
			this.synchronizer = synchronizer;
			return this;
		}

		public Builder setClaimPropertiesSync(ClaimsManagerPlayerClaimPropertiesSync claimPropertiesSync) {
			this.claimPropertiesSync = claimPropertiesSync;
			return this;
		}

		public ClaimsManagerPlayerStateSync build(){
			if(player == null || synchronizer == null || claimPropertiesSync == null)
				throw new IllegalStateException();
			int totalToSync = ServerConfig.CONFIG.claimsSynchronization.get() != ServerConfig.ClaimsSyncType.ALL ? 0 : synchronizer.getClaimStateCountToSync();
			Iterator<PlayerChunkClaim> specificStates = null;
			if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.OWNED_ONLY) {
				Set<PlayerChunkClaim> specificStatesSet = new HashSet<>();
				//just owned and server states
				synchronizer.getClaimPropertiesToSync(player).forEachRemaining(pi -> {
					pi.getStream().map(Map.Entry::getValue)
							.flatMap(PlayerDimensionClaims::getStream)
							.map(PlayerClaimPosList::getClaimState)
							.forEach(specificStatesSet::add);
				});
				specificStates = specificStatesSet.iterator();
			}
			return new ClaimsManagerPlayerStateSync(totalToSync, synchronizer, claimPropertiesSync, specificStates);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
