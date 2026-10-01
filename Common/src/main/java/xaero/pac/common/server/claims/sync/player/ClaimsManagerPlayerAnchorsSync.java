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

package xaero.pac.common.server.claims.sync.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.ClaimingAnchor;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.claims.ClientboundClaimAnchorsPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerDimensionClaims;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ClaimsManagerPlayerAnchorsSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private Iterator<ServerPlayerClaimInfo> toSync;
	private ServerPlayerClaimInfo currentClaimInfo;
	private Iterator<ServerPlayerDimensionClaims> dimensionClaimsIterator;
	private ServerPlayerDimensionClaims currentDimensionClaims;
	private Iterator<ClaimingAnchor> currentAnchorIterator;
	private final ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync;

	private ClaimsManagerPlayerAnchorsSync(
			Iterator<ServerPlayerClaimInfo> toSync,
			ClaimsManagerSynchronizer synchronizer,
			ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync
	) {
		super(synchronizer);
		this.toSync = toSync;
		this.subClaimPropertiesSync = subClaimPropertiesSync;
	}

	@Override
	public void onTick(
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData,
			ServerPlayer player,
			int limit
	){
		if(!ServerConfig.CONFIG.anchorBasedClaiming.get()){
			toSync = null;
			return;
		}
		List<ChunkPos> packetBuilder = new ArrayList<>(ClientboundClaimAnchorsPacket.MAX_ANCHORS);
		int canSync = limit;
		while(canSync > 0 && (dimensionClaimsIterator != null || toSync.hasNext())){
			if(dimensionClaimsIterator == null) {
				currentClaimInfo = toSync.next();
				dimensionClaimsIterator = currentClaimInfo.getTypedStream()
						.map(Map.Entry::getValue)
						.map(d -> (ServerPlayerDimensionClaims)d)
						.toList()
						.iterator();
				if(!dimensionClaimsIterator.hasNext()){
					dimensionClaimsIterator = null;
					continue;
				}
			}
			if(currentAnchorIterator == null){
				currentDimensionClaims = dimensionClaimsIterator.next();
				currentAnchorIterator = currentDimensionClaims.getAnchorIterator();
			}
			while(canSync > 0 && currentAnchorIterator.hasNext()){
				ClaimingAnchor anchor = currentAnchorIterator.next();
				buildClaimAnchorsPacket(packetBuilder, anchor, player);
				canSync--;
			}
			if(!currentAnchorIterator.hasNext()) {
				if(!packetBuilder.isEmpty()) {
					synchronizer.syncClaimAnchors(player, currentClaimInfo.getPlayerId(), currentDimensionClaims.getDimension(), packetBuilder, true);
					packetBuilder.clear();
				}
				currentAnchorIterator = null;
				if(!dimensionClaimsIterator.hasNext())
					dimensionClaimsIterator = null;
			}
		}
		if(!packetBuilder.isEmpty())
			synchronizer.syncClaimAnchors(player, currentClaimInfo.getPlayerId(), currentDimensionClaims.getDimension(), packetBuilder, true);
	}

	private void buildClaimAnchorsPacket(List<ChunkPos> packetBuilder, ClaimingAnchor anchor, ServerPlayer player) {
		packetBuilder.add(anchor.getPos());
		if (packetBuilder.size() == ClientboundClaimAnchorsPacket.MAX_ANCHORS) {
			synchronizer.syncClaimAnchors(player, currentClaimInfo.getPlayerId(), currentDimensionClaims.getDimension(), packetBuilder, true);
			packetBuilder.clear();
		}
	}

	@Override
	public void onLazyPacketsDropped() {
		toSync = null;
	}

	public boolean isFinished(){
		return subClaimPropertiesSync.isFinished() && (toSync == null || !toSync.hasNext());
	}

	@Override
	public boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player) {
		return started && subClaimPropertiesSync.isFinished() && !isFinished();
	}

	public static final class Builder {

		private ClaimsManagerSynchronizer synchronizer;
		private ServerPlayer player;
		private ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync;

		private Builder(){}

		public Builder setDefault() {
			setSynchronizer(null);
			setPlayer(null);
			setSubClaimPropertiesSync(null);
			return this;
		}

		public Builder setSynchronizer(ClaimsManagerSynchronizer synchronizer) {
			this.synchronizer = synchronizer;
			return this;
		}

		public Builder setPlayer(ServerPlayer player) {
			this.player = player;
			return this;
		}

		public Builder setSubClaimPropertiesSync(ClaimsManagerPlayerSubClaimPropertiesSync subClaimPropertiesSync) {
			this.subClaimPropertiesSync = subClaimPropertiesSync;
			return this;
		}

		public ClaimsManagerPlayerAnchorsSync build(){
			if(synchronizer == null || player == null || subClaimPropertiesSync == null)
				throw new IllegalStateException();
			Iterator<ServerPlayerClaimInfo> toSync = synchronizer.getPlayerClaimInfoToSync(player);
			return new ClaimsManagerPlayerAnchorsSync(toSync, synchronizer, subClaimPropertiesSync);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
