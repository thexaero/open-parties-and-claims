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

package xaero.pac.common.server.player.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.Level;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.ClaimingModeLimits;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.impersonation.ServerPlayerClaimImpersonationInfo;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerClaimOwnerPropertiesSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerRegionSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerStateSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerSubClaimPropertiesSync;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.sync.player.PlayerFullPartySync;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sync.task.PlayerConfigSyncSpreadoutTask;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.data.config.PlayerConfigPermissionUpdateData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerPlayerData extends ServerPlayerDataAPI {
	
	//internal api

	private final IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
			serverData;
	private ServerPlayer player;//this can change!
	private boolean claimsModeratorMode;
	private boolean claimsAdminMode;
	private boolean claimsNonallyMode;
	private ClaimingMode claimingMode = null;
	private IPlayerChunkClaim lastClaimCheck;
	private ResourceKey<Level> lastClaimCheckDim;
	private Map<IClaimingModeAPI, ClaimingModeLimits> lastLimitsSync;
	private long lastClaimLimitsCheckTime;
	private boolean shouldResyncPlayerConfigs;
	private PartyMemberDynamicInfoSyncable oftenSyncedPartyMemberInfo;
	private PlayerFullPartySync playerFullPartySync;
	private ClaimsManagerPlayerClaimOwnerPropertiesSync claimsManagerPlayerClaimOwnerPropertiesSync;
	private ClaimsManagerPlayerSubClaimPropertiesSync claimsManagerPlayerSubClaimPropertiesSync;
	private ClaimsManagerPlayerStateSync claimsManagerPlayerStateSync;
	private ClaimsManagerPlayerRegionSync claimsManagerPlayerRegionSync;
	private PlayerClaimActionRequestHandler claimActionRequestHandler;
	private PlayerConfigSyncSpreadoutTask configSyncSpreadoutTask;
	private long lastSubConfigCreationTick;
	private ResourceLocation lastClaimUpdateDimension;
	private IPlayerChunkClaim lastClaimUpdateState;
	private int lastClaimUpdateX;
	private int lastClaimUpdateZ;
	private UUID lastOtherConfigRequest;
	private boolean hasMod;
	private boolean handledLogin;
	private long lastPartyClaimsSyncTime;
	private UUID lastPartyClaimsSyncPartyOwner;
	private long lastPartyOnlineUpdateTime;
	private UUID lastPartyOnlineUpdateOwner;
	private Map<PlayerConfigType, PlayerConfigPermissionUpdateData> playerConfigPermissionUpdateData;
	private long lastPlayerConfigPermissionUpdate;
	private boolean syncedConfigAdmin;
	private long allowedClaimAccessOverLimitTick;
	private long lastClaimsOverLimitMessageTime;
	private boolean partiesAdminMode;
	private NameAndId partiesImpersonatedPlayerProfile;
	private final ServerPlayerClaimImpersonationInfo claimsImpersonationInfo;
	private NameAndId claimTransferRequestSourcePlayerProfile;
	private UUID claimTransferRequestTargetPlayerId;
	private long claimTransferRequestTime;

	public ServerPlayerData(
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData,
			ServerPlayer player
	) {
		super();
		this.serverData = serverData;
		this.player = player;
		this.claimsImpersonationInfo = new ServerPlayerClaimImpersonationInfo(null, serverData);
	}

	public void setPlayer(ServerPlayer player) {
		this.player = player;
	}

	public void onLogin(
			PlayerFullPartySync playerFullPartySync,
						PlayerClaimActionRequestHandler claimActionRequestHandler,
						PlayerConfigSyncSpreadoutTask configSyncSpreadoutTask
	) {
		//won't be called for fake players, e.g. turtles from cc
		this.playerFullPartySync = playerFullPartySync;
		this.claimActionRequestHandler = claimActionRequestHandler;
		this.configSyncSpreadoutTask = configSyncSpreadoutTask;
	}

	public void setClaimSyncTasks(
			ClaimsManagerPlayerClaimOwnerPropertiesSync claimsManagerPlayerClaimOwnerPropertiesSync,
			ClaimsManagerPlayerSubClaimPropertiesSync claimsManagerPlayerSubClaimPropertiesSync,
			ClaimsManagerPlayerStateSync claimsManagerPlayerStateSyncHandler,
			ClaimsManagerPlayerRegionSync claimsManagerPlayerSyncHandler
	) {
		this.claimsManagerPlayerRegionSync = claimsManagerPlayerSyncHandler;
		this.claimsManagerPlayerStateSync = claimsManagerPlayerStateSyncHandler;
		this.claimsManagerPlayerClaimOwnerPropertiesSync = claimsManagerPlayerClaimOwnerPropertiesSync;
		this.claimsManagerPlayerSubClaimPropertiesSync = claimsManagerPlayerSubClaimPropertiesSync;
	}

	@Override
	public boolean isClaimsModeratorMode() {
		return claimsModeratorMode || isClaimsAdminMode();
	}

	@Override
	public boolean isClaimsAdminMode() {
		return claimsAdminMode;
	}

	@Override
	public boolean isClaimsNonallyMode() {
		return claimsNonallyMode;
	}

	@Nonnull
	@Override
	public ClaimingMode getClaimingMode() {
		if(claimingMode == null) {
			if(claimsImpersonationInfo.getPlayerId() == null &&
					serverData.getServerClaimsManager().getPermissionHandler().playerHasPartyClaimPermission(player))
				return (ClaimingMode) ClaimingModes.PARTY;
			return (ClaimingMode) ClaimingModes.PLAYER;
		}
		return claimingMode;
	}

	@Nullable
	@Override
	public ClaimingMode getRawClaimingMode() {
		return claimingMode;
	}

	@Deprecated
	@Override
	public boolean isClaimsServerMode() {
		return getClaimingMode() == ClaimingModes.SERVER;
	}

	public void setOftenSyncedPartyMemberInfo(PartyMemberDynamicInfoSyncable oftenSyncedPartyMemberInfo) {
		this.oftenSyncedPartyMemberInfo = oftenSyncedPartyMemberInfo;
	}

	public void setClaimsModeratorMode(boolean claimsModeratorMode) {
		this.claimsModeratorMode = claimsModeratorMode;
	}

	public void setClaimsAdminMode(boolean claimsAdminMode) {
		this.claimsAdminMode = claimsAdminMode;
	}
	
	public void setClaimsNonallyMode(boolean claimsNonallyMode) {
		this.claimsNonallyMode = claimsNonallyMode;
	}

	public void setClaimingMode(ClaimingMode claimingMode) {
		this.claimingMode = claimingMode;
	}

	public void setClaimingMode(IClaimingModeAPI claimingMode) {
		setClaimingMode((ClaimingMode) claimingMode);
	}

	public void setLastClaimCheck(IPlayerChunkClaim lastClaimCheck) {
		this.lastClaimCheck = lastClaimCheck;
	}

	public IPlayerChunkClaim getLastClaimCheck() {
		return lastClaimCheck;
	}
	
	public PartyMemberDynamicInfoSyncable getPartyMemberDynamicInfo() {
		return oftenSyncedPartyMemberInfo;
	}

	public PlayerFullPartySync getFullPartyPlayerSync() {
		return playerFullPartySync;
	}

	public ClaimsManagerPlayerRegionSync getClaimsManagerPlayerRegionSync() {
		return claimsManagerPlayerRegionSync;
	}

	public ClaimsManagerPlayerStateSync getClaimsManagerPlayerStateSync() {
		return claimsManagerPlayerStateSync;
	}

	public ClaimsManagerPlayerClaimOwnerPropertiesSync getClaimsManagerPlayerClaimOwnerPropertiesSync() {
		return claimsManagerPlayerClaimOwnerPropertiesSync;
	}

	public ClaimsManagerPlayerSubClaimPropertiesSync getClaimsManagerPlayerSubClaimPropertiesSync() {
		return claimsManagerPlayerSubClaimPropertiesSync;
	}

	public PlayerClaimActionRequestHandler getClaimActionRequestHandler() {
		return claimActionRequestHandler;
	}

	public PlayerConfigSyncSpreadoutTask getConfigSyncSpreadoutTask() {
		return configSyncSpreadoutTask;
	}

	public boolean checkAndSetClaimLimitsSync(Collection<ClaimingModeLimits> limits) {
		if(!checkClaimLimitsSync(limits))
			return false;
		setClaimLimitsSync(limits);
		return true;
	}

	public boolean checkClaimLimitsSync(Collection<ClaimingModeLimits> limits) {
		if(lastLimitsSync == null)
			return true;
		for (ClaimingModeLimits modeLimits : limits) {
			if(!modeLimits.equals(lastLimitsSync.get(modeLimits.mode)))
				return true;
		}
		return false;
	}

	public void setClaimLimitsSync(Collection<ClaimingModeLimits> limits) {
		if(lastLimitsSync == null)
			lastLimitsSync = new HashMap<>();
		lastLimitsSync.clear();
		for (ClaimingModeLimits modeLimits : limits)
			lastLimitsSync.put(modeLimits.mode, modeLimits);
	}

	public int getLastSyncedForceloadLimit(){
		if(lastLimitsSync == null)
			return 0;
		ClaimingModeLimits playerLimits = lastLimitsSync.get(ClaimingModes.PLAYER);
		return playerLimits.forceloadLimit;
	}

	public void setShouldResyncPlayerConfigs(boolean shouldResyncPlayerConfigs) {
		this.shouldResyncPlayerConfigs = shouldResyncPlayerConfigs;
	}

	public boolean shouldResyncPlayerConfigs() {
		return shouldResyncPlayerConfigs;
	}

	public long getLastSubConfigCreationTick() {
		return lastSubConfigCreationTick;
	}

	public void setLastSubConfigCreationTick(long lastSubConfigCreationTick) {
		this.lastSubConfigCreationTick = lastSubConfigCreationTick;
	}

	public ResourceLocation getLastClaimUpdateDimension() {
		return lastClaimUpdateDimension;
	}

	public IPlayerChunkClaim getLastClaimUpdateState() {
		return lastClaimUpdateState;
	}

	public int getLastClaimUpdateX() {
		return lastClaimUpdateX;
	}

	public int getLastClaimUpdateZ() {
		return lastClaimUpdateZ;
	}

	public void setLastClaimUpdate(ResourceLocation dimension, IPlayerChunkClaim state, int x, int z) {
		this.lastClaimUpdateDimension = dimension;
		this.lastClaimUpdateState = state;
		this.lastClaimUpdateX = x;
		this.lastClaimUpdateZ = z;
	}

	public UUID getLastOtherConfigRequest() {
		return lastOtherConfigRequest;
	}

	public void setLastOtherConfigRequest(UUID lastOtherConfigRequest) {
		this.lastOtherConfigRequest = lastOtherConfigRequest;
	}

	public void setHasMod(boolean hasMod) {
		this.hasMod = hasMod;
	}

	public boolean hasMod() {
		return hasMod;
	}

	public void setHandledLogin(boolean handledLogin) {
		this.handledLogin = handledLogin;
	}

	public boolean hasHandledLogin() {
		return handledLogin;
	}

	public void setLastClaimLimitsCheckTime(long lastClaimLimitsCheckTime) {
		this.lastClaimLimitsCheckTime = lastClaimLimitsCheckTime;
	}

	public long getLastClaimLimitsCheckTime() {
		return lastClaimLimitsCheckTime;
	}

	public void setLastPartyClaimsSync(long time, UUID owner) {
		this.lastPartyClaimsSyncTime = time;
		this.lastPartyClaimsSyncPartyOwner = owner;
	}

	public long getLastPartyClaimsSyncTime() {
		return lastPartyClaimsSyncTime;
	}

	public UUID getLastPartyClaimsSyncPartyOwner() {
		return lastPartyClaimsSyncPartyOwner;
	}

	public PlayerConfigPermissionUpdateData getPlayerConfigPermissionUpdateData(PlayerConfigType type) {
		if(playerConfigPermissionUpdateData == null)
			playerConfigPermissionUpdateData = new HashMap<>();
		return playerConfigPermissionUpdateData.computeIfAbsent(type, t -> new PlayerConfigPermissionUpdateData(t, this));
	}

	public void setSyncedConfigAdmin(boolean syncedConfigAdmin) {
		this.syncedConfigAdmin = syncedConfigAdmin;
	}

	public boolean getSyncedConfigAdmin() {
		return syncedConfigAdmin;
	}

	public long getLastPlayerConfigPermissionUpdate() {
		return lastPlayerConfigPermissionUpdate;
	}

	public void setLastPlayerConfigPermissionUpdate(long lastPlayerConfigPermissionUpdate) {
		this.lastPlayerConfigPermissionUpdate = lastPlayerConfigPermissionUpdate;
	}

	public void onTick(){
	}

	public long getLastPartyOnlineUpdateTime() {
		return this.lastPartyOnlineUpdateTime;
	}

	public UUID getLastPartyOnlineUpdateOwner() {
		return this.lastPartyOnlineUpdateOwner;
	}

	public void setLastPartyOnlineUpdate(long time, UUID partyOwner) {
		this.lastPartyOnlineUpdateTime = time;
		this.lastPartyOnlineUpdateOwner = partyOwner;
	}

	public void setAllowedClaimAccessOverLimitTick(long allowedClaimAccessOverLimitTick) {
		this.allowedClaimAccessOverLimitTick = allowedClaimAccessOverLimitTick;
	}

	public long getAllowedClaimAccessOverLimitTick() {
		return allowedClaimAccessOverLimitTick;
	}

	public void setLastClaimsOverLimitMessageTime(long lastClaimsOverLimitMessageTime) {
		this.lastClaimsOverLimitMessageTime = lastClaimsOverLimitMessageTime;
	}

	public long getLastClaimsOverLimitMessageTime() {
		return lastClaimsOverLimitMessageTime;
	}

	public boolean isPartiesAdminMode() {
		return partiesAdminMode;
	}

	public void setPartiesAdminMode(boolean partiesAdminMode) {
		this.partiesAdminMode = partiesAdminMode;
	}

	@Nonnull
	public ServerPlayerClaimImpersonationInfo getClaimsImpersonationInfo() {
		return claimsImpersonationInfo;
	}

	public NameAndId getClaimTransferRequestSourcePlayerProfile() {
		return claimTransferRequestSourcePlayerProfile;
	}

	public void setClaimTransferRequestSourcePlayerProfile(NameAndId claimTransferRequestSourcePlayerProfile) {
		this.claimTransferRequestSourcePlayerProfile = claimTransferRequestSourcePlayerProfile;
	}

	public UUID getClaimTransferRequestTargetPlayerId() {
		return claimTransferRequestTargetPlayerId;
	}

	public void setClaimTransferRequestTargetPlayerId(UUID claimTransferRequestTargetPlayerId) {
		this.claimTransferRequestTargetPlayerId = claimTransferRequestTargetPlayerId;
	}

	public long getClaimTransferRequestTime() {
		return claimTransferRequestTime;
	}

	public void setClaimTransferRequestTime(long claimTransferRequestTime) {
		this.claimTransferRequestTime = claimTransferRequestTime;
	}

	public void setPartiesImpersonatedPlayerProfile(NameAndId partiesImpersonatedPlayerProfile) {
		this.partiesImpersonatedPlayerProfile = partiesImpersonatedPlayerProfile;
	}

	public NameAndId getPartiesImpersonatedPlayerProfile() {
		return partiesImpersonatedPlayerProfile;
	}

	public UUID getPartiesImpersonatedPlayerId() {
		return partiesImpersonatedPlayerProfile == null ? null : partiesImpersonatedPlayerProfile.id();
	}

	public void setLastClaimCheckDim(ResourceKey<Level> lastClaimCheckDim) {
		this.lastClaimCheckDim = lastClaimCheckDim;
	}

	public ResourceKey<Level> getLastClaimCheckDim() {
		return lastClaimCheckDim;
	}

}
