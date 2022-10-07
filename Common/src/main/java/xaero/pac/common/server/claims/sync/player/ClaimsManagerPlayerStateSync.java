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
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.packet.claims.ClientboundClaimStatesPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimStateHolder;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class ClaimsManagerPlayerStateSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private final UUID playerId;
	private final ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync;
	private List<PlayerChunkClaim> packetBuilder;
	private Iterator<ServerClaimStateHolder> iterator;
	private final boolean ownedOnly;

	private ClaimsManagerPlayerStateSync(UUID playerId, ClaimsManagerSynchronizer synchronizer, ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync, Iterator<ServerClaimStateHolder> iterator, boolean ownedOnly) {
		super(synchronizer);
		this.playerId = playerId;
		this.subClaimPropertiesSync = subClaimPropertiesSync;
		this.iterator = iterator;
		this.ownedOnly = ownedOnly;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player, int limit){
		if(packetBuilder == null)
			packetBuilder = startClaimStateSync();
		int canSync = limit;
		while(iterator != null && iterator.hasNext() && canSync > 0){
			PlayerChunkClaim state = iterator.next().getState();
			if(!ownedOnly || state.getPlayerId().equals(playerId) || state.getPlayerId().equals(PlayerConfig.SERVER_CLAIM_UUID)) {
				continueClaimStateSync(packetBuilder, state, player);
				canSync -= 2;
			}
			canSync--;
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
		iterator = null;
	}

	public boolean isFinished(){
		return subClaimPropertiesSync.isFinished() && (iterator == null ||
				!iterator.hasNext());
	}

	@Override
	public boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player) {
		return started && subClaimPropertiesSync.isFinished() && !isFinished();
	}

	public static final class Builder {

		private ServerPlayer player;
		private ClaimsManagerSynchronizer synchronizer;
		private ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync;

		private Builder(){}

		public Builder setDefault() {
			setPlayer(null);
			setSynchronizer(null);
			setSubClaimPropertiesSync(null);
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

		public Builder setSubClaimPropertiesSync(ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync) {
			this.subClaimPropertiesSync = subClaimPropertiesSync;
			return this;
		}

		public ClaimsManagerPlayerStateSync build(){
			if(player == null || synchronizer == null || subClaimPropertiesSync == null)
				throw new IllegalStateException();
			boolean ownedOnly = ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.OWNED_ONLY;
			Iterator<ServerClaimStateHolder> iterator = synchronizer.getStateHolderIteratorForSync();
			return new ClaimsManagerPlayerStateSync(player.getUUID(), synchronizer, subClaimPropertiesSync, iterator, ownedOnly);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
