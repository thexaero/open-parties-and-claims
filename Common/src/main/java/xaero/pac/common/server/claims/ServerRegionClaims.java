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

package xaero.pac.common.server.claims;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.SimpleBitStorage;
import xaero.pac.common.claims.PlayerChunkClaimHolder;
import xaero.pac.common.claims.RegionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.HashMap;
import java.util.Objects;

public final class ServerRegionClaims extends RegionClaims<ServerPlayerClaimInfoManager, ServerRegionClaims> implements IServerRegionClaims {

	private final ServerClaimsManager manager;
	
	private final RegionClaimsPaletteStorage syncableStorage;

	private ServerRegionClaims(ResourceLocation dimension, int x, int z, 
			RegionClaimsPaletteStorage syncableStorage, RegionClaimsPaletteStorage storage,
			ServerClaimsManager manager) {
		super(dimension, x, z, storage);
		this.syncableStorage = syncableStorage;
		this.manager = manager;
	}

	@Override
	protected void set(int x, int z, PlayerChunkClaim value) {
		PlayerChunkClaim oldValueForSync = null;
		boolean shouldSync = manager != null && manager.isLoaded();
		if(shouldSync)
			oldValueForSync = get(x, z);
		super.set(x, z, value);
		if(syncableStorage != storage) {//true in an unclaimable dimension
			if(value != null && !Objects.equals(value.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID))
				value = null;
			if(shouldSync && oldValueForSync != null && !Objects.equals(oldValueForSync.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID))
				oldValueForSync = null;
			syncableStorage.set(x, z, value);
		}

		if(shouldSync && value != oldValueForSync)
			manager.getClaimsManagerSynchronizer().syncToPlayersClaimUpdate(dimension, (this.getX() << 5) | x, (this.getZ() << 5) | z, value, oldValueForSync);
	}

	public int[] getSyncablePaletteArray(){
		return syncableStorage.getPaletteArray();
	}
	
	public int getSyncableStorageBits() {
		return syncableStorage.getStorageBits();
	}
	
	public long[] getSyncableStorageData() {
		return syncableStorage.getStorageData();
	}
	
	public RegionClaimsPaletteStorage getSyncableStorage() {
		return syncableStorage;
	}

	public boolean containsState(PlayerChunkClaim state){
		return syncableStorage.containsState(state);
	}
	
	public static final class Builder extends RegionClaims.Builder<ServerPlayerClaimInfoManager, ServerRegionClaims, Builder>{

		private ServerClaimsManager manager;
		private boolean playerClaimsSyncAllowed;
		private RegionClaimsPaletteStorage syncableStorage;
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
		@Override
		public Builder setDefault() {
			super.setDefault();
			setManager(null);
			setPlayerClaimsSyncAllowed(true);
			return self;
		}
		
		public Builder setManager(ServerClaimsManager manager) {
			this.manager = manager;
			return self;
		}
		
		public Builder setPlayerClaimsSyncAllowed(boolean playerClaimsSyncAllowed) {
			this.playerClaimsSyncAllowed = playerClaimsSyncAllowed;
			return self;
		}
		
		@Override
		public ServerRegionClaims build() {
			syncableStorage = 
					new RegionClaimsPaletteStorage(new HashMap<>(), new IntArrayList(), Lists.newArrayList((PlayerChunkClaimHolder)null), new SimpleBitStorage(1, 1024), false);
			setStorage(playerClaimsSyncAllowed ? syncableStorage : 
				new RegionClaimsPaletteStorage(new HashMap<>(), null, Lists.newArrayList((PlayerChunkClaimHolder)null), new SimpleBitStorage(1, 1024), false));
			return (ServerRegionClaims) super.build();
		}

		@Override
		protected ServerRegionClaims buildInternally() {
			return new ServerRegionClaims(dimension, x, z, 
					syncableStorage, storage,
					manager);
		}
		
	}

}
