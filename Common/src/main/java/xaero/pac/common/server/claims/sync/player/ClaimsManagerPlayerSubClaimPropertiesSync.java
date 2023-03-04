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

package xaero.pac.common.server.claims.sync.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.claims.ClientboundSubClaimPropertiesPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ClaimsManagerPlayerSubClaimPropertiesSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private Iterator<ServerPlayerClaimInfo> toSync;
	private Iterator<IPlayerConfig> currentSubConfigIterator;
	private final ClaimsManagerPlayerClaimOwnerPropertiesSync claimOwnerPropertiesSync;

	private ClaimsManagerPlayerSubClaimPropertiesSync(Iterator<ServerPlayerClaimInfo> toSync, ClaimsManagerSynchronizer synchronizer, ClaimsManagerPlayerClaimOwnerPropertiesSync claimOwnerPropertiesSync) {
		super(synchronizer);
		this.toSync = toSync;
		this.claimOwnerPropertiesSync = claimOwnerPropertiesSync;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player, int limit){
		List<ClientboundSubClaimPropertiesPacket.SubClaimProperties> packetBuilder = new ArrayList<>(ClientboundSubClaimPropertiesPacket.MAX_PROPERTIES);
		int canSync = limit;
		while(canSync > 0 && (currentSubConfigIterator != null || toSync.hasNext())){
			if(currentSubConfigIterator == null) {
				ServerPlayerClaimInfo playerClaimInfo = toSync.next();
				IPlayerConfig playerConfig = playerClaimInfo.getConfig();
				currentSubConfigIterator = playerConfig.getSubConfigIterator();
				buildClaimPropertiesPacket(packetBuilder, playerConfig, player);
				canSync--;
			} else {
				while(canSync > 0 && currentSubConfigIterator.hasNext()){
					IPlayerConfig subConfig = currentSubConfigIterator.next();
					buildClaimPropertiesPacket(packetBuilder, subConfig, player);
					canSync--;
				}
				if(!currentSubConfigIterator.hasNext())
					currentSubConfigIterator = null;
			}
		}
		if(!packetBuilder.isEmpty())
			synchronizer.syncSubClaimProperties(packetBuilder, player);
	}

	private void buildClaimPropertiesPacket(List<ClientboundSubClaimPropertiesPacket.SubClaimProperties> packetBuilder, IPlayerConfig subConfig, ServerPlayer player) {
		ClientboundSubClaimPropertiesPacket.SubClaimProperties subClaimProperties = synchronizer.getSubClaimPropertiesForSync(subConfig, true);
		if(subClaimProperties != null) {
			packetBuilder.add(subClaimProperties);
			if (packetBuilder.size() == ClientboundSubClaimPropertiesPacket.MAX_PROPERTIES) {
				synchronizer.syncSubClaimProperties(packetBuilder, player);
				packetBuilder.clear();
			}
		}
	}

	@Override
	public void onLazyPacketsDropped() {
		toSync = null;
	}

	public boolean isFinished(){
		return claimOwnerPropertiesSync.isFinished() && (toSync == null || !toSync.hasNext());
	}

	@Override
	public boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player) {
		return started && claimOwnerPropertiesSync.isFinished() && !isFinished();
	}

	public static final class Builder {

		private ClaimsManagerSynchronizer synchronizer;
		private ServerPlayer player;
		private ClaimsManagerPlayerClaimOwnerPropertiesSync claimOwnerPropertiesSync;

		private Builder(){}

		public Builder setDefault() {
			setSynchronizer(null);
			setPlayer(null);
			setClaimOwnerPropertiesSync(null);
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

		public Builder setClaimOwnerPropertiesSync(ClaimsManagerPlayerClaimOwnerPropertiesSync claimOwnerPropertiesSync) {
			this.claimOwnerPropertiesSync = claimOwnerPropertiesSync;
			return this;
		}

		public ClaimsManagerPlayerSubClaimPropertiesSync build(){
			if(synchronizer == null || player == null || claimOwnerPropertiesSync == null)
				throw new IllegalStateException();
			Iterator<ServerPlayerClaimInfo> toSync = synchronizer.getClaimPropertiesToSync(player);
			return new ClaimsManagerPlayerSubClaimPropertiesSync(toSync, synchronizer, claimOwnerPropertiesSync);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
