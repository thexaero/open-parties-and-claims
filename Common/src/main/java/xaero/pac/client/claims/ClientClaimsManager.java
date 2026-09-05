/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.IClientData;
import xaero.pac.client.claims.player.ClientPlayerClaimInfo;
import xaero.pac.client.claims.player.ClientPlayerClaimInfoManager;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.claims.player.mode.ClaimingModeClientHandlers;
import xaero.pac.client.claims.player.mode.ClientClaimingModeHandler;
import xaero.pac.client.claims.player.mode.ClientClaimingModeInfo;
import xaero.pac.client.claims.tracker.result.ClaimsManagerClaimResultTracker;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.client.parties.party.IClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientPartyMemberDynamicInfoSyncableStorage;
import xaero.pac.client.parties.party.IClientPartyStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.claims.ClaimStateHolder;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.claims.action.request.ClaimActionRequest;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.impersonation.SimplePlayerClaimImpersonationInfo;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.ClaimingModeLimits;
import xaero.pac.common.claims.player.mode.ClaimingModeSubInfo;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.claims.util.ClaimsConstants;
import xaero.pac.common.packet.claims.ServerboundClaimActionRequestPacket;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientClaimsManager extends ClaimsManager<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager, ClientRegionClaims, ClientDimensionClaimsManager, ClaimStateHolder> implements IClientClaimsManager<PlayerChunkClaim, ClientPlayerClaimInfo, ClientDimensionClaimsManager> {

	private IClientData<
			IPlayerConfigClientStorageManager<
					IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>
			>,
			IClientPartyStorage<
					IClientPartyAllyInfo,
					IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>,
					IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>
			>,
			IClientClaimsManager<
					IPlayerChunkClaim,
					IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
					IClientDimensionClaimsManager<IClientRegionClaims>
			>
			> clientData;
	private final ClaimsManagerClaimResultTracker claimResultTracker;
	private boolean loading;
	private final Map<IClaimingModeAPI, ClientClaimingModeInfo> claimingModeInfoMap;
	private boolean alwaysUseLoadingValues;
	private int maxClaimDistance;
	private boolean moderatorMode;
	private boolean adminMode;
	private IClaimingModeAPI claimingMode;
	private boolean partyOwnedClaims;
	private UUID currentPartyOwner;
	private SimplePlayerClaimImpersonationInfo playerImpersonationInfo;

	private ClientClaimsManager(
			ClientPlayerClaimInfoManager playerClaimInfoManager,
			IPlayerConfigManager configManager,
			Map<ResourceLocation, ClientDimensionClaimsManager> dimensions,
			Int2ObjectMap<PlayerChunkClaim> indexToClaimState,
			Map<PlayerChunkClaim, ClaimStateHolder> claimStates,
			ClaimsManagerTracker claimsManagerTracker,
			ClaimsManagerClaimResultTracker claimResultTracker,
			Map<IClaimingModeAPI, ClientClaimingModeInfo> claimingModeInfoMap,
			IClaimingModeAPI claimingMode,
			SimplePlayerClaimImpersonationInfo playerImpersonationInfo
	) {
		super(playerClaimInfoManager, configManager, dimensions, indexToClaimState, claimStates, claimsManagerTracker);
		this.claimResultTracker = claimResultTracker;
		this.claimingModeInfoMap = claimingModeInfoMap;
		this.claimingMode = claimingMode;
		this.playerImpersonationInfo = playerImpersonationInfo;
	}

	public void setClientData(
			IClientData<
					IPlayerConfigClientStorageManager<
							IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>
					>,
					IClientPartyStorage<
							IClientPartyAllyInfo,
							IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>,
							IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>
					>,
					IClientClaimsManager<
							IPlayerChunkClaim,
							IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IClientDimensionClaimsManager<IClientRegionClaims>
					>
			> clientData
	) {
		if(this.clientData != null)
			throw new IllegalStateException();
		this.clientData = clientData;
		this.playerClaimInfoManager.setClientData(clientData);
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	@Override
	public boolean isLoading() {
		return loading;
	}

	@Override
	public void updateLimits(ClaimingModeLimits limits) {
		claimingModeInfoMap.get(limits.mode).setLimits(limits);
	}

	@Override
	public int getLoadingClaimCount() {
		ClaimingModeLimits playerLimits = claimingModeInfoMap.get(ClaimingModes.PLAYER).getLimits();
		return playerLimits == null ? 0 : playerLimits.claimCount;
	}

	@Override
	public int getLoadingForceloadCount() {
		ClaimingModeLimits playerLimits = claimingModeInfoMap.get(ClaimingModes.PLAYER).getLimits();
		return playerLimits == null ? 0 : playerLimits.forceloadCount;
	}

	@Override
	public int getClaimCount() {
		return getClaimCount(ClaimingModes.PLAYER);
	}

	@Override
	public int getForceloadCount() {
		return getForceloadCount(ClaimingModes.PLAYER);
	}

	@Override
	public int getClaimCount(@Nonnull IClaimingModeAPI modeAPI) {
		ClaimingMode mode = (ClaimingMode) modeAPI;
		if(!loading && !alwaysUseLoadingValues) {
			UUID countsSourceId = mode == ClaimingModes.PLAYER ? Minecraft.getInstance().player.getUUID() :
					mode.getClientCountsSourceId();
			if(countsSourceId != null)
				return getPlayerInfo(countsSourceId).getClaimCount();
		}
		ClaimingModeLimits limits = claimingModeInfoMap.get(mode).getLimits();
		return limits == null ? 0 : limits.claimCount;
	}

	@Override
	public int getForceloadCount(@Nonnull IClaimingModeAPI modeAPI) {
		ClaimingMode mode = (ClaimingMode) modeAPI;
		if(!loading && !alwaysUseLoadingValues) {
			UUID countsSourceId = mode == ClaimingModes.PLAYER ? Minecraft.getInstance().player.getUUID() :
					mode.getClientCountsSourceId();
			if(countsSourceId != null)
				return getPlayerInfo(countsSourceId).getForceloadCount();
		}
		ClaimingModeLimits limits = claimingModeInfoMap.get(mode).getLimits();
		return limits == null ? 0 : limits.forceloadCount;
	}

	@Override
	public int getClaimLimit(@Nonnull IClaimingModeAPI mode) {
		ClaimingModeLimits limits = claimingModeInfoMap.get(mode).getLimits();
		return limits == null ? 0 : limits.claimLimit;
	}

	@Override
	public int getForceloadLimit(@Nonnull IClaimingModeAPI mode) {
		ClaimingModeLimits limits = claimingModeInfoMap.get(mode).getLimits();
		return limits == null ? 0 : limits.forceloadLimit;
	}

	public void setAlwaysUseLoadingValues(boolean alwaysUseLoadingValues) {
		this.alwaysUseLoadingValues = alwaysUseLoadingValues;
	}

	@Override
	public boolean getAlwaysUseLoadingValues() {
		return alwaysUseLoadingValues;
	}

	@Override
	public int getClaimLimit() {
		return getClaimLimit(ClaimingModes.PLAYER);
	}

	@Override
	public int getForceloadLimit() {
		return getForceloadLimit(ClaimingModes.PLAYER);
	}

	@Override
	public void setMaxClaimDistance(int maxClaimDistance) {
		this.maxClaimDistance = maxClaimDistance;
	}

	@Override
	public void setClaimingMode(IClaimingModeAPI mode) {
		this.claimingMode = mode;
	}

	@Override
	public int getMaxClaimDistance() {
		return maxClaimDistance;
	}

	public void setModeratorMode(boolean moderatorMode) {
		this.moderatorMode = moderatorMode;
	}

	@Override
	public boolean isModeratorMode() {
		return moderatorMode || isAdminMode();
	}

	@Override
	public boolean isAdminMode() {
		return adminMode;
	}

	public void setAdminMode(boolean adminMode) {
		this.adminMode = adminMode;
	}

	@Override
	@Deprecated
	public boolean isServerMode() {
		return getClaimingMode() == ClaimingModes.SERVER;
	}

	@Nonnull
	@Override
	public IClaimingModeAPI getClaimingMode() {
		return getEffectiveClaimingMode(claimingMode);
	}

	@Nullable
	@Override
	public IClaimingModeAPI getRawClaimingMode() {
		return claimingMode;
	}

	@Override
	public IClaimingModeAPI getEffectiveClaimingMode(IClaimingModeAPI selected) {
		if(selected == null){
			if(playerImpersonationInfo.getPlayerId() == null && partyOwnedClaims &&
					clientData.getPlayerConfigStorageManager().getPartyClaimsConfig().getPermissions().canClaimAs())
				return ClaimingModes.PARTY;
			return ClaimingModes.PLAYER;
		}
		return selected;
	}

	public void setPartyOwnedClaims(boolean partyOwnedClaims) {
		playerClaimInfoManager.setPartyOwnedClaims(partyOwnedClaims);
		this.partyOwnedClaims = partyOwnedClaims;
	}

	@Override
	public boolean usingPartyOwnedClaims() {
		return partyOwnedClaims;
	}

	@Override
	public boolean isInParty() {
		return getCurrentPartyOwner() != null;
	}

	@Override
	protected ClaimStateHolder createStateHolder(PlayerChunkClaim claim) {
		return new ClaimStateHolder(claim);
	}

	@Override
	public void addClaimState(PlayerChunkClaim claim) {
		super.addClaimState(claim);
	}

	@Override
	public void removeClaimState(PlayerChunkClaim state) {
		super.removeClaimState(state);
	}

	@Override
	public PlayerChunkClaim claim(ResourceLocation dimension, UUID id, int subConfigIndex, int x, int z, boolean forceload) {
		PlayerChunkClaim newClaim = super.claim(dimension, id, subConfigIndex, x, z, forceload);
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
	public void reset(boolean notifyTracker) {
		super.reset(notifyTracker);
		moderatorMode = false;
		adminMode = false;
		claimingMode = null;
		claimingModeInfoMap.values().forEach(ClientClaimingModeInfo::reset);
		maxClaimDistance = 0;
		alwaysUseLoadingValues = false;
		setPartyOwnedClaims(false);
		setCurrentPartyOwner(null);
		playerImpersonationInfo.reset();
	}

	@Override
	public void requestClaim(@Nonnull ResourceLocation dimension, int x, int z, @Nullable IClaimingModeAPI claimingModeAPI) {
		requestAreaClaim(dimension, x, z, x, z, claimingModeAPI);
	}

	@Override
	public void requestUnclaim(@Nonnull ResourceLocation dimension, int x, int z, @Nullable IClaimingModeAPI claimingModeAPI){
		requestAreaUnclaim(dimension, x, z, x, z, claimingModeAPI);
	}

	@Override
	public void requestForceload(@Nonnull ResourceLocation dimension, int x, int z, boolean enable, @Nullable IClaimingModeAPI claimingModeAPI){
		requestAreaForceload(dimension, x, z, x, z, enable, claimingModeAPI);
	}

	@Override
	public void requestAreaClaim(@Nonnull ResourceLocation dimension, int left, int top, int right, int bottom, @Nullable IClaimingModeAPI claimingModeAPI){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new ServerboundClaimActionRequestPacket(new ClaimActionRequest(ClaimingAction.CLAIM, dimension, left, top, right, bottom, (ClaimingMode) claimingModeAPI)));
	}

	@Override
	public void requestAreaUnclaim(@Nonnull ResourceLocation dimension, int left, int top, int right, int bottom, @Nullable IClaimingModeAPI claimingModeAPI){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new ServerboundClaimActionRequestPacket(new ClaimActionRequest(ClaimingAction.UNCLAIM, dimension, left, top, right, bottom, (ClaimingMode) claimingModeAPI)));
	}

	@Override
	public void requestAreaForceload(@Nonnull ResourceLocation dimension, int left, int top, int right, int bottom, boolean enable, @Nullable IClaimingModeAPI claimingModeAPI){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new ServerboundClaimActionRequestPacket(new ClaimActionRequest(enable ? ClaimingAction.FORCELOAD : ClaimingAction.UNFORCELOAD, dimension, left, top, right, bottom, (ClaimingMode) claimingModeAPI)));
	}

	@Override
	protected void onClaimStateAdded(ClaimStateHolder stateHolder) {
	}

	public void removeSubClaim(UUID playerId, int subConfigIndex) {
		getPlayerInfo(playerId).removeSubClaim(subConfigIndex);
	}

	@Override
	public PlayerChunkClaim getPotentialClaimStateReflection(){
		IClaimingModeAPI effectiveClaimingMode = getClaimingMode();
		ClientClaimingModeHandler claimingModeHandler = ClaimingModeClientHandlers.get(effectiveClaimingMode);
		boolean impersonating = effectiveClaimingMode.canBeImpersonated() && playerImpersonationInfo.getPlayerId() != null;
		UUID claimReflectionOwner;
		if(impersonating)
			claimReflectionOwner = playerImpersonationInfo.getClaimPlayerId(effectiveClaimingMode);
		else
			claimReflectionOwner = claimingModeHandler.getClaimReflectionOwnerGetter().apply(this);
		if(claimReflectionOwner == null)
			return null;
		int subIndex;
		if(impersonating)
			subIndex = playerImpersonationInfo.getSubIndex(effectiveClaimingMode);
		else
			subIndex = getCurrentSubConfigIndex(effectiveClaimingMode);
		return new PlayerChunkClaim(claimReflectionOwner, subIndex, false, 0);
	}

	@Override
	public int getCurrentSubConfigIndex() {
		return getCurrentSubConfigIndex(ClaimingModes.PLAYER);
	}

	@Override
	@Deprecated
	public int getCurrentServerSubConfigIndex() {
		return getCurrentSubConfigIndex(ClaimingModes.SERVER);
	}

	@Override
	public int getCurrentSubConfigIndex(IClaimingModeAPI claimingMode) {
		return claimingModeInfoMap.get(claimingMode).getSubInfo().getIndex();
	}

	@Nonnull
	@Override
	public String getCurrentSubConfigId(IClaimingModeAPI claimingMode) {
		return claimingModeInfoMap.get(claimingMode).getSubInfo().getId();
	}

	@Nonnull
	@Override
	public String getCurrentSubConfigId() {
		return getCurrentSubConfigId(ClaimingModes.PLAYER);
	}

	@Nonnull
	@Override
	@Deprecated
	public String getCurrentServerSubConfigId() {
		return getCurrentSubConfigId(ClaimingModes.SERVER);
	}

	@Override
	public void updateSubInfo(ClaimingModeSubInfo subInfo) {
		claimingModeInfoMap.get(subInfo.getMode()).setSubInfo(subInfo);
	}

	@Nonnull
	@Override
	public ClaimsManagerClaimResultTracker getClaimResultTracker() {
		return claimResultTracker;
	}

	@Override
	public boolean claimUsesDimensionSubConfigs(IPlayerChunkClaimAPI claimState) {
		IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> config =
				clientData.getPlayerConfigStorageManager().getGlobalConfigForClaimOwner(
						claimState == null ? null : claimState.getPlayerId()
				);
		return config != null && config.getType().hasDimensionSubConfigs();
	}

	@Override
	public String getDimensionName(IPlayerChunkClaimAPI claimState, ResourceLocation dimension) {
		IPlayerConfigClientStorage<?> rootConfig = clientData.getPlayerConfigStorageManager()
				.getGlobalConfigForClaimOwner(claimState == null ? null : claimState.getPlayerId());
		IPlayerConfigClientStorage<?> effectiveConfig = rootConfig;
		if(dimension != null)
			effectiveConfig = effectiveConfig.getEffectiveSubConfig(id2String.apply(dimension));
		String name = effectiveConfig.getOption(PlayerConfigOptions.CLAIMS_NAME).getValue();
		if(dimension != null && name == null)
			name = rootConfig.getOption(PlayerConfigOptions.CLAIMS_NAME).getValue();
		return name;
	}

	@Override
	public int getDimensionColor(IPlayerChunkClaimAPI claimState, ResourceLocation dimension) {
		IPlayerConfigClientStorage<?> rootConfig = clientData.getPlayerConfigStorageManager()
				.getGlobalConfigForClaimOwner(claimState == null ? null : claimState.getPlayerId());
		Integer color = null;
		if(dimension != null) {
			IPlayerConfigClientStorage<?> effectiveConfig = rootConfig.getEffectiveSubConfig(id2String.apply(dimension));
			color = effectiveConfig.getOption(PlayerConfigOptions.CLAIMS_COLOR).getValue();
		}
		if(color == null)
			color = rootConfig.getOption(PlayerConfigOptions.CLAIMS_COLOR).getValue();
		int defaultColorValue = PlayerConfigOptions.CLAIMS_COLOR.getDefaultValue();
		if(color == null)
			color = defaultColorValue;
		if(color == defaultColorValue && rootConfig.getType().isGlobal())
			return ClaimsConstants.GLOBAL_CLAIM_DEFAULT_COLOR;
		return color;
	}

	@Override
	protected MutableComponent constructPlayerClaimName(ClientPlayerClaimInfo playerClaimInfo, Component forceloadedComponent, boolean allowPartyNames) {
		if(allowPartyNames && partyOwnedClaims && playerClaimInfo.isPartyOwned() && playerClaimInfo.getPartyName() != null)
			return Component.translatable(
					"gui.xaero_pac_title_party_claim",
					playerClaimInfo.getPartyName(), forceloadedComponent
			);
		return super.constructPlayerClaimName(playerClaimInfo, forceloadedComponent, allowPartyNames);
	}

	@Override
	public void setCurrentPartyOwner(UUID currentPartyOwner) {
		this.currentPartyOwner = currentPartyOwner;
	}

	@Override
	public UUID getCurrentPartyOwner() {
		return currentPartyOwner;
	}

	public void setPlayerImpersonationInfo(SimplePlayerClaimImpersonationInfo playerImpersonationInfo) {
		this.playerImpersonationInfo = playerImpersonationInfo;
	}

	@Nonnull
	@Override
	public SimplePlayerClaimImpersonationInfo getPlayerImpersonationInfo() {
		return playerImpersonationInfo;
	}

	public final static class Builder extends ClaimsManager.Builder<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager, ClientRegionClaims, ClientDimensionClaimsManager, ClaimStateHolder, Builder>{
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
		@Override
		public ClientClaimsManager build() {
			setPlayerClaimInfoManager(new ClientPlayerClaimInfoManager(new HashMap<>(), new LinkedChain<>()));
			return (ClientClaimsManager) super.build();
		}

		@Override
		protected ClientClaimsManager buildInternally(Map<PlayerChunkClaim, ClaimStateHolder> claimStates, ClaimsManagerTracker claimsManagerTracker, Int2ObjectMap<PlayerChunkClaim> indexToClaimState) {
			Map<IClaimingModeAPI, ClientClaimingModeInfo> claimingModeInfoMap = new HashMap<>();
			for (IClaimingModeAPI claimingMode : ClaimingModes.ALL_IMMUTABLE.values()) {
				ClientClaimingModeInfo claimingModeInfo = new ClientClaimingModeInfo(claimingMode);
				claimingModeInfo.reset();
				claimingModeInfoMap.put(claimingMode, claimingModeInfo);
			}
			SimplePlayerClaimImpersonationInfo defaultClaimImpersonationInfo =
					new SimplePlayerClaimImpersonationInfo(null);
			ClientClaimsManager result = new ClientClaimsManager(
					playerClaimInfoManager, null, dimensions,
					indexToClaimState, claimStates, claimsManagerTracker,
					ClaimsManagerClaimResultTracker.Builder.begin().build(),
					claimingModeInfoMap, ClaimingModes.PLAYER, defaultClaimImpersonationInfo
			);
			playerClaimInfoManager.setClaimsManager(result);
			return result;
		}
		
	}

}
