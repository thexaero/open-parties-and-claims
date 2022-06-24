/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.client.claims;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.player.ClientPlayerClaimInfo;
import xaero.pac.client.claims.player.ClientPlayerClaimInfoManager;
import xaero.pac.client.claims.tracker.result.ClaimsManagerClaimResultTracker;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.request.ClaimActionRequest;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.packet.claims.ServerboundClaimActionRequestPacket;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class ClientClaimsManager extends ClaimsManager<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager, ClientRegionClaims, ClientDimensionClaimsManager> implements IClientClaimsManager<PlayerChunkClaim, ClientPlayerClaimInfo, ClientDimensionClaimsManager> {

	private Int2ObjectMap<PlayerChunkClaim> indexToClaimState;
	private final ClaimsManagerClaimResultTracker claimResultTracker;
	private boolean loading;
	private int loadingClaimCount;
	private int loadingForceloadCount;
	private int claimLimit;
	private int forceloadLimit;
	private int maxClaimDistance;
	
	private ClientClaimsManager(ClientPlayerClaimInfoManager playerClaimInfoManager,
			IPlayerConfigManager<?> configManager, Map<ResourceLocation, ClientDimensionClaimsManager> dimensions, 
			Map<PlayerChunkClaim, PlayerChunkClaim> claimStates, ClaimsManagerTracker claimsManagerTracker, 
			Int2ObjectMap<PlayerChunkClaim> indexToClaimState, ClaimsManagerClaimResultTracker claimResultTracker) {
		super(playerClaimInfoManager, configManager, dimensions, claimStates, claimsManagerTracker);
		this.indexToClaimState = indexToClaimState;
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

	@Override
	public void addClaimState(PlayerChunkClaim claim) {
		claimStates.put(claim, claim);
		indexToClaimState.put(claim.getSyncIndex(), claim);
	}
	
	public PlayerChunkClaim getClaimState(int index) {
		return indexToClaimState.get(index);
	}

	@Override
	protected ClientDimensionClaimsManager create(ResourceLocation dimension,
			Long2ObjectMap<ClientRegionClaims> claims) {
		return new ClientDimensionClaimsManager(dimension, claims);
	}

	@Override
	public void reset() {
		super.reset();
		indexToClaimState.clear();
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
			setPlayerClaimInfoManager(new ClientPlayerClaimInfoManager(new HashMap<>()));
			return (ClientClaimsManager) super.build();
		}

		@Override
		protected ClientClaimsManager buildInternally(Map<PlayerChunkClaim, PlayerChunkClaim> claimStates, ClaimsManagerTracker claimsManagerTracker) {
			return new ClientClaimsManager(playerClaimInfoManager, null, dimensions, claimStates, claimsManagerTracker, 
					new Int2ObjectOpenHashMap<>(), ClaimsManagerClaimResultTracker.Builder.begin().build());
		}
		
	}

}
