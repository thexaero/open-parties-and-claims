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

package xaero.pac.common.server.claims.player;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.*;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObject;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public final class ServerPlayerClaimInfo extends PlayerClaimInfo<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> implements IServerPlayerClaimInfo<PlayerDimensionClaims>, ObjectManagerIOExpirableObject {
	
	private IPlayerConfig playerConfig;
	private boolean dirty;
	protected boolean beenUsed;
	private long lastConfirmedActivity;
	private boolean hasBeenActive;
	private boolean replacementInProgress;
	private final Deque<PlayerClaimReplaceSpreadoutTask> replaceTaskQueue;

	public ServerPlayerClaimInfo(IPlayerConfig playerConfig, String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims,
								 ServerPlayerClaimInfoManager manager, Deque<PlayerClaimReplaceSpreadoutTask> replaceSpreadoutTasks) {
		super(username, playerId, claims, manager);
		this.playerConfig = playerConfig;
		this.replaceTaskQueue = replaceSpreadoutTasks;
		if(manager.getExpirationHandler() != null)
			confirmActivity(manager.getExpirationHandler().getServerInfo());
	}
	
	@Override
	public void onClaim(IPlayerConfigManager configManager, ResourceLocation dimension, PlayerChunkClaim claim, int x, int z) {
		super.onClaim(configManager, dimension, claim, x, z);
		if(claim.isForceloadable())
			manager.getTicketManager().addTicket(configManager, dimension, playerId, x, z);
		setDirty(true);
		beenUsed = true;
		if(manager.isLoaded())
			manager.getClaimsManager().getClaimsManagerSynchronizer().trySyncClaimLimits(configManager, playerId);
	}
	
	@Override
	public void onUnclaim(IPlayerConfigManager configManager, ResourceLocation dimension, PlayerChunkClaim claim, int x, int z) {
		super.onUnclaim(configManager, dimension, claim, x, z);
		if(claim.isForceloadable())
			manager.getTicketManager().removeTicket(configManager, dimension, playerId, x, z);
		setDirty(true);
		beenUsed = true;
		if(manager.isLoaded())
			manager.getClaimsManager().getClaimsManagerSynchronizer().trySyncClaimLimits(configManager, playerId);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		if(dirty && !manager.isLoaded())
			return;
		if(!this.dirty && dirty)
			manager.addToSave(this);
		this.dirty = dirty;
		
	}

	@Override
	public String getFileName() {
		return playerId.toString();
	}
	
	@Override
	public void setPlayerUsername(String playerUsername) {
		boolean changed = !Objects.equals(getPlayerUsername(), playerUsername);
		super.setPlayerUsername(playerUsername);
		if(changed) {
			if(beenUsed)
				setDirty(true);
			manager.getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersClaimOwnerPropertiesUpdate(this);
		}
	}

	@Override
	protected Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionClaimCountStream() {
		return getStream();
	}

	@Override
	protected Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionForceloadCountStream() {
		boolean unclaimableForceloadsAllowed = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID) || ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() && ServerConfig.CONFIG.allowExistingForceloadsInUnclaimableDimensions.get();
		return claims.entrySet().stream().filter(e -> unclaimableForceloadsAllowed || manager.isClaimable(e.getKey()));
	}

	@Nonnull
	@Override
	public Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getStream() {
		boolean unclaimableClaimsAllowed = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID) || ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get();
		return claims.entrySet().stream().filter(e -> unclaimableClaimsAllowed || manager.isClaimable(e.getKey()));
	}

	@Override
	public Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getFullStream(){
		//stream of all dimensions even they're effectively empty due to !ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions and being unclaimable
		return claims.entrySet().stream();
	}

	@Override
	public void confirmActivity(ServerInfo serverInfo) {
		lastConfirmedActivity = serverInfo.getUseTime();
		hasBeenActive = false;
	}
	
	public void setLastConfirmedActivity(long lastActiveTime) {
		this.lastConfirmedActivity = lastActiveTime;
	}
	
	public boolean hasBeenActive() {
		return hasBeenActive;
	}
	
	public void registerActivity() {
		hasBeenActive = true;
	}
	
	public long getLastConfirmedActivity() {
		return lastConfirmedActivity;
	}

	@Override
	public String getClaimsName() {
		return playerConfig.getEffective(PlayerConfigOptions.CLAIMS_NAME);
	}

	@Override
	public int getClaimsColor() {
		return playerConfig.getEffective(PlayerConfigOptions.CLAIMS_COLOR);
	}

	@Nullable
	@Override
	public String getClaimsName(int subConfigIndex) {
		IPlayerConfig subConfig = playerConfig.getEffectiveSubConfig(subConfigIndex);
		if(subConfig.getSubIndex() != subConfigIndex)
			return null;
		return subConfig.getRaw(PlayerConfigOptions.CLAIMS_NAME);
	}

	@Nullable
	@Override
	public Integer getClaimsColor(int subConfigIndex) {
		IPlayerConfig subConfig = playerConfig.getEffectiveSubConfig(subConfigIndex);
		if(subConfig.getSubIndex() != subConfigIndex)
			return null;
		return subConfig.getRaw(PlayerConfigOptions.CLAIMS_COLOR);
	}

	@Nullable
	@Override
	public String getClaimsName(@Nonnull String subId) {
		IPlayerConfig subConfig = playerConfig.getSubConfig(subId);
		if(subConfig == null)
			return null;
		return subConfig.getRaw(PlayerConfigOptions.CLAIMS_NAME);
	}

	@Override
	public Integer getClaimsColor(@Nonnull String subId) {
		IPlayerConfig subConfig = playerConfig.getSubConfig(subId);
		if(subConfig == null)
			return null;
		return subConfig.getRaw(PlayerConfigOptions.CLAIMS_COLOR);
	}

	@Override
	public boolean isReplacementInProgress() {
		return replacementInProgress;
	}

	@Override
	public void setReplacementInProgress(boolean replacementInProgress) {
		this.replacementInProgress = replacementInProgress;
	}

	@Override
	public boolean hasReplacementTasks(){
		return replacementInProgress || !replaceTaskQueue.isEmpty();
	}

	@Override
	public void addReplacementTask(PlayerClaimReplaceSpreadoutTask task, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData){
		if(!replacementInProgress)
			manager.getClaimsManager().getClaimReplaceTaskHandler().addTask(task, serverData);
		else
			replaceTaskQueue.add(task);
	}

	@Override
	public PlayerClaimReplaceSpreadoutTask removeNextReplacementTask(){
		return replaceTaskQueue.removeFirst();
	}

	@Override
	public IPlayerConfig getConfig() {
		return playerConfig;
	}

}
