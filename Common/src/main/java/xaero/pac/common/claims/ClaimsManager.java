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

package xaero.pac.common.claims;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.PlayerClaimInfo;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public abstract class ClaimsManager
<
	PCI extends PlayerClaimInfo<PCI, M>,
	M extends PlayerClaimInfoManager<PCI, M>,
	WRC extends RegionClaims<M, WRC>,
	WCM extends DimensionClaimsManager<M, WRC>
> implements IClaimsManager<PCI, WCM> {
	
	protected final M playerClaimInfoManager;
	protected final IPlayerConfigManager<?> configManager;
	private Map<ResourceLocation, WCM> dimensions;
	private Int2ObjectMap<PlayerChunkClaim> indexToClaimState;
	private Map<PlayerChunkClaim, PlayerChunkClaim> claimStates;
	protected final ClaimsManagerTracker claimsManagerTracker;
	
	protected ClaimsManager(M playerClaimInfoManager, IPlayerConfigManager<?> configManager,
							Map<ResourceLocation, WCM> dimensions, Int2ObjectMap<PlayerChunkClaim> indexToClaimState, Map<PlayerChunkClaim, PlayerChunkClaim> claimStates, ClaimsManagerTracker claimsManagerTracker) {
		super();
		this.playerClaimInfoManager = playerClaimInfoManager;
		this.configManager = configManager;
		this.dimensions = dimensions;
		this.indexToClaimState = indexToClaimState;
		this.claimStates = claimStates;
		this.claimsManagerTracker = claimsManagerTracker;
	}

	protected void addClaimState(PlayerChunkClaim claim){
		claimStates.put(claim, claim);
		indexToClaimState.put(claim.getSyncIndex(), claim);
	}

	protected void reset() {
		indexToClaimState.clear();
		dimensions.clear();
		claimStates.clear();
		indexToClaimState = new Int2ObjectOpenHashMap<>();
		dimensions = new HashMap<>();
		claimStates = new HashMap<>();
		playerClaimInfoManager.clear();
	}

	protected WCM ensureDimension(ResourceLocation dim) {
		return dimensions.computeIfAbsent(dim, d -> create(d, new Long2ObjectOpenHashMap<>()));
	}

	@Nullable
	@Override
	public WCM getDimension(@Nonnull ResourceLocation dimension) {
		return dimensions.get(dimension);
	}

	@Nonnull
	@Override
	public Stream<WCM> getDimensionStream(){
		return dimensions.values().stream();
	}
	
	protected abstract void onClaimStateCreation(PlayerChunkClaim created);
	
	public PlayerChunkClaim getClaimState(UUID id, boolean forceload) {
		PlayerChunkClaim potentialState = new PlayerChunkClaim(id, forceload, claimStates.size());
		PlayerChunkClaim originalState = claimStates.get(potentialState);
		if(originalState == null) {
			originalState = potentialState;
			claimStates.put(originalState, originalState);
			indexToClaimState.put(originalState.getSyncIndex(), originalState);
			onClaimStateCreation(originalState);
		}
		return originalState;
	}

	public PlayerChunkClaim getClaimStateBySyncIndex(int syncIndex){
		return indexToClaimState.get(syncIndex);
	}

	public Stream<PlayerChunkClaim> getClaimStatesStream(){
		return claimStates.keySet().stream();
	}
	
	protected abstract WCM create(ResourceLocation dimension, Long2ObjectMap<WRC> claims);

	public PlayerChunkClaim claim(ResourceLocation dimension, UUID id, int x, int z, boolean forceload) {
		WCM dimensionClaims = ensureDimension(dimension);
		PlayerChunkClaim claim = getClaimState(id, forceload);//no duplicates
		return dimensionClaims.claim(x, z, claim, playerClaimInfoManager, configManager);
	}
	
	public void unclaim(ResourceLocation dimension, int x, int z) {
		WCM dimensionClaims = ensureDimension(dimension);
		dimensionClaims.unclaim(x, z, playerClaimInfoManager, configManager);
	}

	@Override
	public boolean hasPlayerInfo(@Nonnull UUID playerId) {
		return playerClaimInfoManager.hasInfo(playerId);
	}

	@Nonnull
	@Override
	public PCI getPlayerInfo(@Nonnull UUID playerId) {
		return playerClaimInfoManager.getInfo(playerId);
	}

	@Nonnull
	@Override
	public Stream<PCI> getPlayerInfoStream() {
		return playerClaimInfoManager.getInfoStream();
	}

	public Iterator<PCI> getPlayerInfoIterator(){
		return playerClaimInfoManager.iterator();
	}

	@Nullable
	@Override
	public PlayerChunkClaim get(@Nonnull ResourceLocation dimension, int x, int z) {
		WCM dimensionClaims = ensureDimension(dimension);
		return dimensionClaims.get(x, z);
	}

	@Nullable
	@Override
	public PlayerChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPos chunkPos) {
		return get(dimension, chunkPos.x, chunkPos.z);
	}

	@Nullable
	@Override
	public PlayerChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull BlockPos blockPos) {
		return get(dimension, blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}
	
	@Nonnull
	@Override
	public ClaimsManagerTracker getTracker() {
		return claimsManagerTracker;
	}
	
	public M getPlayerClaimInfoManager() {
		return playerClaimInfoManager;
	}
	
	public int getClaimStateCount() {
		return claimStates.size();
	}
	
	public abstract static class Builder
	<
		PCI extends PlayerClaimInfo<PCI, M>,
		M extends PlayerClaimInfoManager<PCI, M>,
		WRC extends RegionClaims<M, WRC>,
		WCM extends DimensionClaimsManager<M, WRC>,
		B extends Builder<PCI, M, WRC, WCM, B>
	>  {

		protected final B self;
		protected M playerClaimInfoManager;
		protected Map<ResourceLocation, WCM> dimensions;
		protected Map<PlayerChunkClaim, PlayerChunkClaim> claimStates;
		
		@SuppressWarnings("unchecked")
		protected Builder() {
			this.self = (B) this;
		}
		
		public B setDefault() {
			setPlayerClaimInfoManager(null);
			setDimensions(null);
			setClaimStates(null);
			return self;
		}
		
		protected B setPlayerClaimInfoManager(M playerClaimInfoManager) {
			this.playerClaimInfoManager = playerClaimInfoManager;
			return self;
		}
		
		public B setDimensions(Map<ResourceLocation, WCM> dimensions) {
			this.dimensions = dimensions;
			return self;
		}
		
		public B setClaimStates(Map<PlayerChunkClaim, PlayerChunkClaim> claimStates) {
			this.claimStates = claimStates;
			return self;
		}
		
		public ClaimsManager<PCI, M, WRC, WCM> build() {
			if(playerClaimInfoManager == null)
				throw new IllegalStateException();
			if(dimensions == null)
				dimensions = new HashMap<>();
			return buildInternally(new HashMap<>(), new ClaimsManagerTracker(new HashSet<>()), new Int2ObjectOpenHashMap<>());
		}
		
		protected abstract ClaimsManager<PCI, M, WRC, WCM> buildInternally(Map<PlayerChunkClaim, PlayerChunkClaim> claimStates, ClaimsManagerTracker claimsManagerTracker, Int2ObjectMap<PlayerChunkClaim> indexToClaimState);
		
	}
	
	public static enum Action {
		CLAIM,
		UNCLAIM,
		FORCELOAD,
		UNFORCELOAD
	}
	
}
