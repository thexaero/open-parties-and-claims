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
import xaero.pac.common.server.claims.*;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;

public final class ClaimsManagerPlayerSyncHandler {
	
	private final ServerPlayer player;
	private final ClaimsManagerSynchronizer synchronizer;
	private final List<ClaimsManagerPlayerDimensionSyncHandler> dimsToSync;
	private ClaimsManagerPlayerDimensionSyncHandler currentPrefix;
	private boolean started;

	private ClaimsManagerPlayerSyncHandler(ServerPlayer player, ClaimsManagerSynchronizer synchronizer, List<ClaimsManagerPlayerDimensionSyncHandler> dimsToSync) {
		super();
		this.player = player;
		this.synchronizer = synchronizer;
		this.dimsToSync = dimsToSync;
	}
	
	private void sendDimensionPrefix(ClaimsManagerPlayerDimensionSyncHandler dim) {
		if(dim != currentPrefix) {
			ResourceLocation dimLocation = dim == null ? null : dim.getDim();
			synchronizer.syncDimensionIdToClient(dimLocation, player);
			currentPrefix = dim;
		}
	}
	
	public boolean handle(int limit) {
		if(shouldSchedule()) {
			int count = 0;
			while(!dimsToSync.isEmpty()) {
				ClaimsManagerPlayerDimensionSyncHandler dim = dimsToSync.get(0);
				sendDimensionPrefix(dim);
				count += dim.handle(player, synchronizer, limit - count);
				if(count >= limit)
					break;
				dimsToSync.remove(0);
			}
			boolean done = dimsToSync.isEmpty();
			if(done) {
				sendDimensionPrefix(null);
				synchronizer.endSyncing(player);
			}
			return done;
		}
		return true;
	}
	
	public void start() {
		started = true;
		if(!shouldSchedule()) {
			sendDimensionPrefix(null);
			synchronizer.endSyncing(player);
		}
	}
	
	public boolean shouldSchedule() {
		return started && !dimsToSync.isEmpty();
	}
	
	public static final class Builder {

		private ServerPlayer player;
		private IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager;
		
		private Builder() {}
		
		private Builder setDefault() {
			setPlayer(null);
			setClaimsManager(null);
			return this;
		}
		
		public Builder setPlayer(ServerPlayer player) {
			this.player = player;
			return this;
		}
		
		public Builder setClaimsManager(
				IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager) {
			this.claimsManager = claimsManager;
			return this;
		}
		
		public ClaimsManagerPlayerSyncHandler build() {
			if(player == null || claimsManager == null)
				throw new IllegalStateException();
			List<ClaimsManagerPlayerDimensionSyncHandler> dimsToSync = new ArrayList<>();
			if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.ALL) {
				((ServerClaimsManager)(Object)claimsManager).getDimensionStream().forEach(dim -> {
					List<ServerRegionClaims> regionsToSync = new ArrayList<>(dim.getCount());
					dim.getRegionStream().forEach(regionsToSync::add);
					dimsToSync.add(new ClaimsManagerPlayerDimensionSyncHandler(dim.getDimension(), regionsToSync));
				});
			}
			return new ClaimsManagerPlayerSyncHandler(player, (ClaimsManagerSynchronizer) claimsManager.getClaimsManagerSynchronizer(), dimsToSync);
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
