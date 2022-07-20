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

package xaero.pac.client.claims;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.player.ClientPlayerClaimInfo;
import xaero.pac.client.claims.player.ClientPlayerClaimInfoManager;
import xaero.pac.client.claims.tracker.result.ClaimsManagerClaimResultTracker;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.request.ClaimActionRequest;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.packet.claims.ServerboundClaimActionRequestPacket;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientClaimsManager extends ClaimsManager<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager, ClientRegionClaims, ClientDimensionClaimsManager> implements IClientClaimsManager<PlayerChunkClaim, ClientPlayerClaimInfo, ClientDimensionClaimsManager> {

	private final ClaimsManagerClaimResultTracker claimResultTracker;
	private boolean loading;
	private int loadingClaimCount;
	private int loadingForceloadCount;
	private boolean alwaysUseLoadingValues;
	private int claimLimit;
	private int forceloadLimit;
	private int maxClaimDistance;
	private boolean adminMode;
	
	private ClientClaimsManager(ClientPlayerClaimInfoManager playerClaimInfoManager,
			IPlayerConfigManager<?> configManager, Map<ResourceLocation, ClientDimensionClaimsManager> dimensions,
			Int2ObjectMap<PlayerChunkClaim> indexToClaimState, Map<PlayerChunkClaim, PlayerChunkClaim> claimStates, ClaimsManagerTracker claimsManagerTracker,
			ClaimsManagerClaimResultTracker claimResultTracker) {
		super(playerClaimInfoManager, configManager, dimensions, indexToClaimState, claimStates, claimsManagerTracker);
		this.claimResultTracker = claimResultTracker;
	}
	
	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	@Override
	public boolean isLoading() {
		return loading;
	}

	@Override
	public void setLoadingClaimCount(int loadingClaimCount) {
		this.loadingClaimCount = loadingClaimCount;
	}

	@Override
	public int getLoadingClaimCount() {
		return loadingClaimCount;
	}

	@Override
	public void setLoadingForceloadCount(int loadingForceloadCount) {
		this.loadingForceloadCount = loadingForceloadCount;
	}

	@Override
	public int getLoadingForceloadCount() {
		return loadingForceloadCount;
	}

	public void setAlwaysUseLoadingValues(boolean alwaysUseLoadingValues) {
		this.alwaysUseLoadingValues = alwaysUseLoadingValues;
	}

	@Override
	public boolean getAlwaysUseLoadingValues() {
		return alwaysUseLoadingValues;
	}

	@Override
	public void setClaimLimit(int claimLimit) {
		this.claimLimit = claimLimit;
	}

	@Override
	public int getClaimLimit() {
		return claimLimit;
	}

	@Override
	public void setForceloadLimit(int forceloadLimit) {
		this.forceloadLimit = forceloadLimit;
	}

	@Override
	public int getForceloadLimit() {
		return forceloadLimit;
	}

	@Override
	public void setMaxClaimDistance(int maxClaimDistance) {
		this.maxClaimDistance = maxClaimDistance;
	}

	@Override
	public int getMaxClaimDistance() {
		return maxClaimDistance;
	}

	public boolean isAdminMode() {
		return adminMode;
	}

	public void setAdminMode(boolean adminMode) {
		this.adminMode = adminMode;
	}

	@Override
	public void addClaimState(PlayerChunkClaim claim) {
		super.addClaimState(claim);
	}

	@Override
	public PlayerChunkClaim claim(ResourceLocation dimension, UUID id, int x, int z, boolean forceload) {
		PlayerChunkClaim newClaim = super.claim(dimension, id, x, z, forceload);
		claimsManagerTracker.onChunkChange(dimension, x, z, newClaim);
		return newClaim;
	}

	@Override
	public void unclaim(ResourceLocation dimension, int x, int z) {
		super.unclaim(dimension, x, z);
		claimsManagerTracker.onChunkChange(dimension, x, z, null);
	}

	public void unclaimRegion(ResourceLocation dimension, int x, int z){
		ClientDimensionClaimsManager dimensionClaims = ensureDimension(dimension);
		dimensionClaims.unclaimRegion(x, z, playerClaimInfoManager, configManager);
		claimsManagerTracker.onWholeRegionChange(dimension, x, z);
	}

	public void claimRegion(ResourceLocation dimension, int x, int z, RegionClaimsPaletteStorage regionStorage){
		ClientDimensionClaimsManager dimensionClaims = ensureDimension(dimension);
		dimensionClaims.claimRegion(x, z, regionStorage, playerClaimInfoManager, configManager);
		claimsManagerTracker.onWholeRegionChange(dimension, x, z);
	}

	@Override
	protected ClientDimensionClaimsManager create(ResourceLocation dimension,
			Long2ObjectMap<ClientRegionClaims> claims) {
		return new ClientDimensionClaimsManager(dimension, claims, new LinkedChain<>());
	}

	@Override
	public void reset() {
		super.reset();
		adminMode = false;
	}

	@Override
	public void requestClaim(int x, int z, boolean byServer) {
		requestAreaClaim(x, z, x, z, byServer);
	}

	@Override
	public void requestUnclaim(int x, int z, boolean byServer){
		requestAreaUnclaim(x, z, x, z, byServer);
	}

	@Override
	public void requestForceload(int x, int z, boolean enable, boolean byServer){
		requestAreaForceload(x, z, x, z, enable, byServer);
	}

	@Override
	public void requestAreaClaim(int left, int top, int right, int bottom, boolean byServer){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new ServerboundClaimActionRequestPacket(new ClaimActionRequest(Action.CLAIM, left, top, right, bottom, byServer)));
	}

	@Override
	public void requestAreaUnclaim(int left, int top, int right, int bottom, boolean byServer){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new ServerboundClaimActionRequestPacket(new ClaimActionRequest(Action.UNCLAIM, left, top, right, bottom, byServer)));
	}

	@Override
	public void requestAreaForceload(int left, int top, int right, int bottom, boolean enable, boolean byServer){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new ServerboundClaimActionRequestPacket(new ClaimActionRequest(enable ? Action.FORCELOAD : Action.UNFORCELOAD, left, top, right, bottom, byServer)));
	}

	@Override
	protected void onClaimStateCreation(PlayerChunkClaim created) {
	}

	@Nonnull
	@Override
	public ClaimsManagerClaimResultTracker getClaimResultTracker() {
		return claimResultTracker;
	}
	
	public final static class Builder extends ClaimsManager.Builder<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager, ClientRegionClaims, ClientDimensionClaimsManager, Builder>{
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
		@Override
		public ClientClaimsManager build() {
			setPlayerClaimInfoManager(new ClientPlayerClaimInfoManager(new HashMap<>(), new LinkedChain<>()));
			return (ClientClaimsManager) super.build();
		}

		@Override
		protected ClientClaimsManager buildInternally(Map<PlayerChunkClaim, PlayerChunkClaim> claimStates, ClaimsManagerTracker claimsManagerTracker, Int2ObjectMap<PlayerChunkClaim> indexToClaimState) {
			return new ClientClaimsManager(playerClaimInfoManager, null, dimensions,
					indexToClaimState, claimStates, claimsManagerTracker, ClaimsManagerClaimResultTracker.Builder.begin().build());
		}
		
	}

}
