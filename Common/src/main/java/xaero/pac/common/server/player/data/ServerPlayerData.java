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

package xaero.pac.common.server.player.data;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerClaimOwnerPropertiesSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerRegionSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerStateSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerSubClaimPropertiesSync;
import xaero.pac.common.server.parties.party.sync.player.PlayerFullPartySync;
import xaero.pac.common.server.player.config.sync.task.PlayerConfigSyncSpreadoutTask;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.UUID;

public class ServerPlayerData extends ServerPlayerDataAPI {
	
	//internal api

	private boolean claimsAdminMode;
	private boolean claimsNonallyMode;
	private boolean claimsServerMode;
	private IPlayerChunkClaim lastClaimCheck;
	private int lastBaseClaimLimitSync;//used for detecting limit changes based on FTB ranks
	private int lastBaseForceloadLimitSync;
	private boolean checkedBaseForceloadLimitOnce;
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
	private UUID lastOtherConfigRequest;
	private boolean hasMod;
	private boolean handledLogin;

	public ServerPlayerData() {
		super();
	}

	public void onLogin(PlayerFullPartySync playerFullPartySync, ClaimsManagerPlayerRegionSync claimsManagerPlayerSyncHandler,
						ClaimsManagerPlayerStateSync claimsManagerPlayerStateSyncHandler,
						ClaimsManagerPlayerClaimOwnerPropertiesSync claimsManagerPlayerClaimOwnerPropertiesSync,
						ClaimsManagerPlayerSubClaimPropertiesSync claimsManagerPlayerSubClaimPropertiesSync,
						PlayerClaimActionRequestHandler claimActionRequestHandler, PlayerConfigSyncSpreadoutTask configSyncSpreadoutTask) {
		//won't be called for fake players, e.g. turtles from cc
		this.playerFullPartySync = playerFullPartySync;
		this.claimsManagerPlayerRegionSync = claimsManagerPlayerSyncHandler;
		this.claimsManagerPlayerStateSync = claimsManagerPlayerStateSyncHandler;
		this.claimsManagerPlayerClaimOwnerPropertiesSync = claimsManagerPlayerClaimOwnerPropertiesSync;
		this.claimsManagerPlayerSubClaimPropertiesSync = claimsManagerPlayerSubClaimPropertiesSync;
		this.claimActionRequestHandler = claimActionRequestHandler;
		this.configSyncSpreadoutTask = configSyncSpreadoutTask;
	}

	@Override
	public boolean isClaimsAdminMode() {
		return claimsAdminMode;
	}

	@Override
	public boolean isClaimsNonallyMode() {
		return claimsNonallyMode;
	}

	@Override
	public boolean isClaimsServerMode() {
		return claimsServerMode;
	}

	public void setOftenSyncedPartyMemberInfo(PartyMemberDynamicInfoSyncable oftenSyncedPartyMemberInfo) {
		this.oftenSyncedPartyMemberInfo = oftenSyncedPartyMemberInfo;
	}
	
	public void setClaimsAdminMode(boolean claimsAdminMode) {
		this.claimsAdminMode = claimsAdminMode;
	}
	
	public void setClaimsNonallyMode(boolean claimsNonallyMode) {
		this.claimsNonallyMode = claimsNonallyMode;
	}

	public void setClaimsServerMode(boolean claimsServerMode) {
		this.claimsServerMode = claimsServerMode;
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

	public void setLastClaimLimitsSync(int lastBaseClaimLimitSync, int lastBaseForceloadLimitSync) {
		this.lastBaseClaimLimitSync = lastBaseClaimLimitSync;
		this.lastBaseForceloadLimitSync = lastBaseForceloadLimitSync;
	}

	public boolean checkBaseClaimLimitsSync(int currentBaseClaimLimit, int currentBaseForceloadLimit) {
		return lastBaseClaimLimitSync != currentBaseClaimLimit || lastBaseForceloadLimitSync != currentBaseForceloadLimit;
	}

	public boolean haveCheckedBaseForceloadLimitOnce() {
		return checkedBaseForceloadLimitOnce;
	}

	public void setCheckedBaseForceloadLimitOnce(){
		checkedBaseForceloadLimitOnce = true;
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

	public void setLastClaimUpdate(ResourceLocation dimension, IPlayerChunkClaim state) {
		this.lastClaimUpdateDimension = dimension;
		this.lastClaimUpdateState = state;
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

	public void onTick(){
	}

}
