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

package xaero.pac.common.server.claims;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.claims.action.request.ClaimActionRequest;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.action.listener.ClaimActionListenerManager;
import xaero.pac.common.server.claims.action.listener.override.api.ClaimActionPermissionOverride;
import xaero.pac.common.server.claims.action.listener.override.api.ClaimActionPermissionOverrideType;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.task.PlayerAreaClaimActionSpreadoutTask;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.claims.protection.cache.TickCachedPlayerGroupCheck;
import xaero.pac.common.server.claims.protection.override.ChunkAccessOverriderManager;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.PlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ServerClaimsManager extends ClaimsManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, ServerRegionClaims, ServerDimensionClaimsManager, ServerClaimStateHolder> implements IServerClaimsManager<PlayerChunkClaim, ServerPlayerClaimInfo, ServerDimensionClaimsManager> {

	private MinecraftServer server;
	private final ClaimsManagerSynchronizer claimsManagerSynchronizer;
	private final ServerSpreadoutQueuedTaskHandler<PlayerAreaClaimActionSpreadoutTask> areClaimActionTaskHandler;
	private final ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler;
	private final ServerClaimsPermissionHandler permissionHandler;
	private final PlayerPartySystemManager partySystemManager;
	private final LinkedChain<ServerClaimStateHolder> linkedClaimStates;
	private final ClaimActionListenerManager actionListenerManager;
	private final ChunkAccessOverriderManager chunkAccessOverriderManager;
	private final TickCachedPlayerGroupCheck tickCachedReclaimabilityCheck;
	private ChunkProtection<ServerClaimsManager> chunkProtection;
	private boolean loaded;

	protected ServerClaimsManager(
			MinecraftServer server,
			ServerPlayerClaimInfoManager playerClaimInfoManager,
			IPlayerConfigManager configManager,
			Map<Identifier, ServerDimensionClaimsManager> dimensions,
			ClaimsManagerSynchronizer claimsManagerSynchronizer,
			Int2ObjectMap<PlayerChunkClaim> indexToClaimState,
			Map<PlayerChunkClaim, ServerClaimStateHolder> claimStates,
			ClaimsManagerTracker claimsManagerTracker,
			ServerSpreadoutQueuedTaskHandler<PlayerAreaClaimActionSpreadoutTask> areClaimActionTaskHandler,
			ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler,
			ServerClaimsPermissionHandler permissionHandler,
			PlayerPartySystemManager partySystemManager,
			LinkedChain<ServerClaimStateHolder> linkedClaimStates,
			ClaimActionListenerManager actionListenerManager,
			ChunkAccessOverriderManager chunkAccessOverriderManager,
			TickCachedPlayerGroupCheck tickCachedReclaimabilityCheck
	) {
		super(playerClaimInfoManager, configManager, dimensions, indexToClaimState, claimStates, claimsManagerTracker);
		this.server = server;
		this.claimsManagerSynchronizer = claimsManagerSynchronizer;
		this.areClaimActionTaskHandler = areClaimActionTaskHandler;
		this.claimReplaceTaskHandler = claimReplaceTaskHandler;
		this.permissionHandler = permissionHandler;
		this.partySystemManager = partySystemManager;
		this.linkedClaimStates = linkedClaimStates;
		this.actionListenerManager = actionListenerManager;
		this.chunkAccessOverriderManager = chunkAccessOverriderManager;
		this.tickCachedReclaimabilityCheck = tickCachedReclaimabilityCheck;
	}
	
	public void setExpirationHandler(ServerPlayerClaimsExpirationHandler expirationHandler) {
		this.playerClaimInfoManager.setExpirationHandler(expirationHandler);
	}

	public void setChunkProtection(ChunkProtection<ServerClaimsManager> chunkProtection) {
		if(this.chunkProtection != null)
			throw new IllegalStateException();
		this.chunkProtection = chunkProtection;
	}

	public ServerPlayerClaimsExpirationHandler.Builder beginExpirationHandlerBuilder() {
		return ServerPlayerClaimsExpirationHandler.Builder.begin().setManager(playerClaimInfoManager).setClaimsManager(this);
	}

	@Override
	protected ServerDimensionClaimsManager create(Identifier dimension,
												  Long2ObjectMap<ServerRegionClaims> claims) {
		boolean playerClaimsSyncAllowed = ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() || isClaimable(dimension);
		return new ServerDimensionClaimsManager(dimension, claims, new LinkedChain<>(), this, playerClaimsSyncAllowed);
	}

	@Override
	protected ServerClaimStateHolder createStateHolder(PlayerChunkClaim claim) {
		return new ServerClaimStateHolder(claim);
	}

	public long countStateRegions(PlayerChunkClaim state, int direction) {
		ServerClaimStateHolder stateHolder = claimStateHolders.get(state);
		stateHolder.countRegions(direction);
		if (stateHolder.getRegionCount() <= 0)
			removeClaimState(state);
		return stateHolder.getRegionCount();
	}

	private boolean withinDistance(int fromX, int fromZ, int x, int z) {
		int maxClaimDistance = ServerConfig.CONFIG.maxClaimDistance.get();
		return Math.abs(x - fromX) <= maxClaimDistance && Math.abs(z - fromZ) <= maxClaimDistance;
	}

	@Override
	public boolean isClaimable(@Nonnull Identifier dimension) {
		return playerClaimInfoManager.isClaimable(dimension);
	}

	@Override
	protected void onClaimStateAdded(ServerClaimStateHolder stateHolder) {
		linkedClaimStates.add(stateHolder);
	}

	@Override
	protected void removeClaimState(PlayerChunkClaim state) {
		linkedClaimStates.remove(claimStateHolders.get(state));
		super.removeClaimState(state);
		//removal is synced because the client can't know when to remove states while the initial sync is in progress
		claimsManagerSynchronizer.syncToPlayersRemoveClaimState(state);
	}

	@Nullable
	@Override
	public PlayerChunkClaim claim(@Nonnull Identifier dimension, @Nonnull UUID id, int subConfigIndex, int x, int z, boolean forceload) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return null;
		PlayerChunkClaim result = super.claim(dimension, id, subConfigIndex, x, z, forceload);
		if(loaded)
			claimsManagerTracker.onChunkChange(dimension, x, z, result);
		return result;
	}
	
	@Override
	public void unclaim(@Nonnull Identifier dimension, int x, int z) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		super.unclaim(dimension, x, z);
		if(loaded)
			claimsManagerTracker.onChunkChange(dimension, x, z, null);
	}

	private boolean canReclaim(PlayerChunkClaim currentClaim, UUID playerId, Identifier dimension) {
		return tickCachedReclaimabilityCheck.checkGroup(configManager, chunkProtection, null, playerId, currentClaim, dimension);
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToClaimHelper(@Nonnull Identifier dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean forceLoaded, boolean force, boolean isServer, int claimLimit, ClaimingAction action) {
		if(!force && action == ClaimingAction.CLAIM) {
			ClaimActionPermissionOverride permissionOverride =
					actionListenerManager.overrideClaimingActionPermission(playerId, dimension, x, z, ClaimingAction.CLAIM, this, server);
			if (permissionOverride.getType() != ClaimActionPermissionOverrideType.PASS) {
				if (permissionOverride.getType() == ClaimActionPermissionOverrideType.ALLOW)
					force = true;
				else {
					boolean interrupts = permissionOverride.getType() == ClaimActionPermissionOverrideType.INTERRUPT;
					return new ClaimResult<>(null, interrupts ? ClaimResult.Type.ADDON_INTERRUPTS : ClaimResult.Type.ADDON_FORBIDS, permissionOverride.getReason());
				}
			}
		}
		PlayerChunkClaim currentClaim = get(dimension, x, z);
		boolean claimCountUnaffected = false;
		if(currentClaim != null)
			claimCountUnaffected = Objects.equals(currentClaim.getPlayerId(), playerId);
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(playerId);
		if(!force && playerClaimInfo.isTransferInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.TRANSFER_IN_PROGRESS);
		if(!force && playerClaimInfo.isReplacementInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.REPLACEMENT_IN_PROGRESS);
		int claimCount = 0;
		if(!isServer){
			claimCount = playerClaimInfo.getClaimCount();
			if(!force && claimCount > claimLimit)
				return new ClaimResult<>(currentClaim, ClaimResult.Type.OVER_CLAIM_LIMIT);
		}
		boolean withinLimit = force || claimCountUnaffected || isServer || claimCount < claimLimit;
		if(withinLimit) {
			if(!claimCountUnaffected && !force && !canReclaim(currentClaim, playerId, dimension)){
				return new ClaimResult<>(currentClaim, currentClaim == null ?
						ClaimResult.Type.DIMENSION_NOT_RECLAIMABLE : ClaimResult.Type.ALREADY_CLAIMED);
			}
			PlayerChunkClaim claim = new PlayerChunkClaim(playerId, subConfigIndex, forceLoaded, 0);
			if(Objects.equals(claim, currentClaim))
				return new ClaimResult<>(currentClaim, ClaimResult.Type.ALREADY_CLAIMED);
			if(!force && !isServer && currentClaim == null && !ServerConfig.CONFIG.allowTouchingClaims.get())
				for (int i = -1; i < 2; i++) {
					for (int j = -1; j < 2; j++) {
						if(i == 0 && j == 0)
							continue;
						PlayerChunkClaim neighborClaim = get(dimension, x + i, z + j);
						if(neighborClaim != null && !neighborClaim.getPlayerId().equals(playerId))
							return new ClaimResult<>(currentClaim, ClaimResult.Type.CANT_TOUCH_OTHER);
					}
				}
			PlayerChunkClaim actualClaim = claim(dimension, claim.getPlayerId(), subConfigIndex, x, z, claim.isForceloadable());
			actionListenerManager.handleSuccessfulClaimingAction(playerId, dimension, x, z, action, this, server);
			return new ClaimResult<>(actualClaim, action.getSuccessType());
		} else {
			return new ClaimResult<>(currentClaim, ClaimResult.Type.CLAIM_LIMIT_REACHED);
		}
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToClaimTyped(@Nonnull Identifier dimension, @Nonnull UUID playerId, int subConfigIndex, @Nonnull Identifier fromDimension, int fromX, int fromZ, int x, int z, boolean force) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new ClaimResult<>(null, ClaimResult.Type.CLAIMS_ARE_DISABLED);
		boolean isServer = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID);
		if(!isServer && !isClaimable(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.UNCLAIMABLE_DIMENSION);
		if(!force && !fromDimension.equals(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.ANOTHER_DIMENSION);
		if(!force && !withinDistance(fromX, fromZ, x, z))
			return new ClaimResult<>(null, ClaimResult.Type.TOO_FAR);
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(playerId);
		if(playerClaimInfo.isAreaClaimTaskInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.AREA_ACTION_IN_PROGRESS);
		int claimLimit = getPlayerFullClaimLimit(playerId);
		return tryToClaimHelper(dimension, playerId, subConfigIndex, fromX, fromZ, x, z, false, force, isServer, claimLimit, ClaimingAction.CLAIM);
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToUnclaimHelper(@Nonnull Identifier dimension, @Nonnull UUID id, int fromX, int fromZ, int x, int z, boolean force) {
		PlayerChunkClaim currentClaim = get(dimension, x, z);
		if(currentClaim == null)
			return new ClaimResult<>(null, ClaimResult.Type.NOT_CLAIMED);
		if(!force) {
			ClaimActionPermissionOverride permissionOverride =
					actionListenerManager.overrideClaimingActionPermission(id, dimension, x, z, ClaimingAction.UNCLAIM, this, server);
			if (permissionOverride.getType() != ClaimActionPermissionOverrideType.PASS) {
				if (permissionOverride.getType() == ClaimActionPermissionOverrideType.ALLOW)
					force = true;
				else {
					boolean interrupts = permissionOverride.getType() == ClaimActionPermissionOverrideType.INTERRUPT;
					return new ClaimResult<>(null, interrupts ? ClaimResult.Type.ADDON_INTERRUPTS : ClaimResult.Type.ADDON_FORBIDS, permissionOverride.getReason());
				}
			}
		}
		if(!force && !Objects.equals(id, currentClaim.getPlayerId()))
			return new ClaimResult<>(currentClaim, ClaimResult.Type.NOT_CLAIMED_BY_USER);
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(id);
		if(!force && playerClaimInfo.isTransferInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.TRANSFER_IN_PROGRESS);
		if(!force && playerClaimInfo.isReplacementInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.REPLACEMENT_IN_PROGRESS);
	 	unclaim(dimension, x, z);
		actionListenerManager.handleSuccessfulClaimingAction(id, dimension, x, z, ClaimingAction.UNCLAIM, this, server);
	 	return new ClaimResult<>(null, ClaimResult.Type.SUCCESSFUL_UNCLAIM);
	}
	
	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToUnclaimTyped(@Nonnull Identifier dimension, @Nonnull UUID id, @Nonnull Identifier fromDimension, int fromX, int fromZ, int x, int z, boolean force) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new ClaimResult<>(null, ClaimResult.Type.CLAIMS_ARE_DISABLED);
		//boolean isServer = Objects.equals(id, PlayerConfig.SERVER_CLAIM_UUID);
		if(!force && !fromDimension.equals(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.ANOTHER_DIMENSION);
		if(!force && !withinDistance(fromX, fromZ, x, z))
			return new ClaimResult<>(null, ClaimResult.Type.TOO_FAR);
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(id);
		if(playerClaimInfo.isAreaClaimTaskInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.AREA_ACTION_IN_PROGRESS);
		return tryToUnclaimHelper(dimension, id, fromX, fromZ, x, z, force);
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToForceloadHelper(@Nonnull Identifier dimension, @Nonnull UUID id, int fromX, int fromZ, int x, int z, boolean enable, boolean force, boolean isServer, int claimLimit, int forceloadLimit) {
		PlayerChunkClaim currentClaim = get(dimension, x, z);
		if(!force) {
			ClaimActionPermissionOverride permissionOverride =
					actionListenerManager.overrideClaimingActionPermission(id, dimension, x, z, enable ? ClaimingAction.FORCELOAD : ClaimingAction.UNFORCELOAD, this, server);
			if (permissionOverride.getType() != ClaimActionPermissionOverrideType.PASS) {
				if (permissionOverride.getType() == ClaimActionPermissionOverrideType.ALLOW)
					force = true;
				else {
					boolean interrupts = permissionOverride.getType() == ClaimActionPermissionOverrideType.INTERRUPT;
					return new ClaimResult<>(null, interrupts ? ClaimResult.Type.ADDON_INTERRUPTS : ClaimResult.Type.ADDON_FORBIDS, permissionOverride.getReason());
				}
			}
		}
		if(currentClaim != null && (force || Objects.equals(currentClaim.getPlayerId(), id))) {
			if(currentClaim.isForceloadable() == enable)
				return new ClaimResult<>(currentClaim, enable ? ClaimResult.Type.ALREADY_FORCELOADABLE : ClaimResult.Type.ALREADY_UNFORCELOADED);
			ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(id);
			boolean withinLimit = force || isServer || !enable ||
					playerClaimInfo.getForceloadCount() < forceloadLimit;
			if(!withinLimit)
				return new ClaimResult<>(currentClaim, ClaimResult.Type.FORCELOAD_LIMIT_REACHED);

			ClaimingAction action = enable ? ClaimingAction.FORCELOAD : ClaimingAction.UNFORCELOAD;
			return tryToClaimHelper(dimension, currentClaim.getPlayerId(), currentClaim.getSubConfigIndex(), fromX, fromZ, x, z, enable, force, isServer, claimLimit, action);
		} else
		 	return new ClaimResult<>(currentClaim, ClaimResult.Type.NOT_CLAIMED_BY_USER_FORCELOAD);
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToForceloadTyped(@Nonnull Identifier dimension, @Nonnull UUID id, @Nonnull Identifier fromDimension, int fromX, int fromZ, int x, int z, boolean enable, boolean force) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new ClaimResult<>(null, ClaimResult.Type.CLAIMS_ARE_DISABLED);
		boolean isServer = Objects.equals(id, PlayerConfig.SERVER_CLAIM_UUID);
		if(enable && !isServer && !isClaimable(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.UNCLAIMABLE_DIMENSION);
		if(!force && !fromDimension.equals(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.ANOTHER_DIMENSION);
		if(!force && !withinDistance(fromX, fromZ, x, z))
			return new ClaimResult<>(null, ClaimResult.Type.TOO_FAR);
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(id);
		if(playerClaimInfo.isAreaClaimTaskInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.AREA_ACTION_IN_PROGRESS);
		int claimLimit = getPlayerFullClaimLimit(id);
		int forceloadLimit = getPlayerFullForceloadLimit(id);
		return tryToForceloadHelper(dimension, id, fromX, fromZ, x, z, enable, force, isServer, claimLimit, forceloadLimit);
	}

	@Deprecated
	public AreaClaimResult backwardsCompatibleClaimActionOverArea(Identifier dimension, UUID playerId, int subConfigIndex, int fromX, int fromZ, int left, int top, int right, int bottom, ClaimingAction action, boolean force){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new AreaClaimResult(Sets.newHashSet(ClaimResult.Type.CLAIMS_ARE_DISABLED), new HashSet<>(), dimension, left, top, right, bottom);
		Set<ClaimResult.Type> resultTypes = new HashSet<>();
		boolean isServer = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID);
		if(!isServer && (action == ClaimingAction.CLAIM || action == ClaimingAction.FORCELOAD) && !isClaimable(dimension)) {
			resultTypes.add(ClaimResult.Type.UNCLAIMABLE_DIMENSION);
			return new AreaClaimResult(resultTypes, new HashSet<>(), dimension, left, top, right, bottom);
		}
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(playerId);
		if(playerClaimInfo.isAreaClaimTaskInProgress()) {
			resultTypes.add(ClaimResult.Type.AREA_ACTION_IN_PROGRESS);
			return new AreaClaimResult(resultTypes, new HashSet<>(), dimension, left, top, right, bottom);
		}
		int effectiveLeft = left;
		int effectiveTop = top;
		int effectiveRight = right;
		int effectiveBottom = bottom;
		int maxRequestLength = 32;
		if(effectiveRight - effectiveLeft >= maxRequestLength)
			effectiveRight = effectiveLeft + maxRequestLength - 1;
		if(effectiveBottom - effectiveTop >= maxRequestLength)
			effectiveBottom = effectiveTop + maxRequestLength - 1;
		CompletableFuture<AreaClaimResult> resultFuture = new CompletableFuture<>();
		PlayerAreaClaimActionSpreadoutTask task = new PlayerAreaClaimActionSpreadoutTask(
				new ClaimActionRequest(action, dimension, effectiveLeft, effectiveTop, effectiveRight, effectiveBottom, null),
				force, playerId, subConfigIndex,
				dimension, fromX, fromZ, 100,
				resultFuture::complete
		);
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(server);
		while(task.shouldWork(serverData, task))
			task.onTick(serverData, task, 100, new ArrayList<>());
		return resultFuture.join();
	}

	public void tryClaimActionOverArea(Identifier dimension, UUID playerId, int subConfigIndex, Identifier fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, ClaimingAction action, boolean force, Consumer<AreaClaimResult> listener) {
		int maxChunksToAffect = force ? Integer.MAX_VALUE : ServerConfig.CONFIG.maxSingleClaimActionSize.get();
		tryClaimActionOverArea(dimension, playerId, subConfigIndex, fromDimension, fromX, fromZ, left, top, right, bottom, action, force, maxChunksToAffect, listener);
	}

	public void tryClaimActionOverArea(Identifier dimension, UUID playerId, int subConfigIndex, Identifier fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, ClaimingAction action, boolean force, int maxChunksToAffect, Consumer<AreaClaimResult> listener) {
		if(!ServerConfig.CONFIG.claimsEnabled.get()) {
			listener.accept(new AreaClaimResult(Sets.newHashSet(ClaimResult.Type.CLAIMS_ARE_DISABLED), new HashSet<>(), dimension, left, top, right, bottom));
			return;
		}
		Set<ClaimResult.Type> resultTypes = new HashSet<>();
		boolean isServer = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID);
		if(!isServer && (action == ClaimingAction.CLAIM || action == ClaimingAction.FORCELOAD) && !isClaimable(dimension)) {
			resultTypes.add(ClaimResult.Type.UNCLAIMABLE_DIMENSION);
			listener.accept(new AreaClaimResult(resultTypes, new HashSet<>(), dimension, left, top, right, bottom));
			return;
		}
		if(!force && !fromDimension.equals(dimension)) {
			resultTypes.add(ClaimResult.Type.ANOTHER_DIMENSION);
			listener.accept(new AreaClaimResult(resultTypes, new HashSet<>(), dimension, left, top, right, bottom));
			return;
		}
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(playerId);
		if(playerClaimInfo.isAreaClaimTaskInProgress()) {
			resultTypes.add(ClaimResult.Type.AREA_ACTION_IN_PROGRESS);
			listener.accept(new AreaClaimResult(resultTypes, new HashSet<>(), dimension, left, top, right, bottom));
			return;
		}
		IServerData<?, ?> serverData = ServerData.from(server);
		playerClaimInfo.addAreaClaimActionTask(
				new PlayerAreaClaimActionSpreadoutTask(
						new ClaimActionRequest(action, dimension, left, top, right, bottom, null),
						force, playerId, subConfigIndex,
						fromDimension, fromX, fromZ, maxChunksToAffect,
						listener
				),
				serverData
		);
	}

	@Override
	public void tryToClaimArea(@Nonnull Identifier dimension, @Nonnull UUID playerId, int subConfigIndex, @Nonnull Identifier fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, boolean force, @Nonnull Consumer<AreaClaimResult> listener) {
		tryClaimActionOverArea(dimension, playerId, subConfigIndex, fromDimension, fromX, fromZ, left, top, right, bottom, ClaimingAction.CLAIM, force, listener);
	}

	@Override
	public void tryToUnclaimArea(@Nonnull Identifier dimension, @Nonnull UUID id, @Nonnull Identifier fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, boolean force, @Nonnull Consumer<AreaClaimResult> listener) {
		tryClaimActionOverArea(dimension, id, -1, fromDimension, fromX, fromZ, left, top, right, bottom, ClaimingAction.UNCLAIM, force, listener);
	}

	@Override
	public void tryToForceloadArea(@Nonnull Identifier dimension, @Nonnull UUID id, @Nonnull Identifier fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, boolean enable, boolean force, @Nonnull Consumer<AreaClaimResult> listener) {
		tryClaimActionOverArea(dimension, id, -1, fromDimension, fromX, fromZ, left, top, right, bottom, enable ? ClaimingAction.FORCELOAD : ClaimingAction.UNFORCELOAD, force, listener);
	}

	@Nullable
	@Override
	public PlayerChunkClaim get(@Nonnull Identifier dimension, int x, int z) {
		PlayerChunkClaim actualClaim = super.get(dimension, x, z);
		//allowExistingClaimsInUnclaimableDimensions is applied here, not when loading the files, so that new changes to claims still affect the "ignored" claims, e.g. when a server claims a chunk claimed by player
		if(actualClaim == null || ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() || Objects.equals(actualClaim.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID) || Objects.equals(actualClaim.getPlayerId(), PlayerConfig.EXPIRED_CLAIM_UUID) || isClaimable(dimension))
			return actualClaim;
		else
			return null;
	}

	@Override
	public int getPlayerBaseClaimLimit(@Nonnull UUID playerId){
		return playerClaimInfoManager.getPlayerBaseLimit(
				playerId, null,
				ServerConfig.CONFIG.maxPlayerClaims, ServerConfig.CONFIG.claimBonusPerPartyMember,
				ServerConfig.CONFIG.claimBonusForPartyOwner, UsedPermissionNodes.MAX_PLAYER_CLAIMS
		);
	}

	@Override
	public int getPlayerBaseForceloadLimit(@Nonnull UUID playerId){
		return playerClaimInfoManager.getPlayerBaseLimit(
				playerId, null,
				ServerConfig.CONFIG.maxPlayerClaimForceloads, ServerConfig.CONFIG.forceloadBonusPerPartyMember,
				ServerConfig.CONFIG.forceloadBonusForPartyOwner, UsedPermissionNodes.MAX_PLAYER_FORCELOADS
		);
	}

	@Override
	public int getPlayerBaseClaimLimit(@Nonnull ServerPlayer player){
		return playerClaimInfoManager.getPlayerBaseLimit(
				null, player,
				ServerConfig.CONFIG.maxPlayerClaims, ServerConfig.CONFIG.claimBonusPerPartyMember,
				ServerConfig.CONFIG.claimBonusForPartyOwner, UsedPermissionNodes.MAX_PLAYER_CLAIMS
		);
	}

	@Override
	public int getPlayerBaseForceloadLimit(@Nonnull ServerPlayer player){
		return playerClaimInfoManager.getPlayerBaseLimit(
				null, player,
				ServerConfig.CONFIG.maxPlayerClaimForceloads, ServerConfig.CONFIG.forceloadBonusPerPartyMember,
				ServerConfig.CONFIG.forceloadBonusForPartyOwner, UsedPermissionNodes.MAX_PLAYER_FORCELOADS
		);
	}

	@Override
	public int getPlayerFullClaimLimit(@Nonnull UUID playerId) {
		IPlayerConfig config = configManager.getLoadedConfig(playerId);
		if(config.getType().isGlobal())
			return Integer.MAX_VALUE;
		return getPlayerBaseClaimLimit(playerId) + config.getEffective(PlayerConfigOptions.BONUS_CHUNK_CLAIMS);
	}

	@Override
	public int getPlayerFullClaimLimit(@Nonnull ServerPlayer player) {
		IPlayerConfig config = configManager.getLoadedConfig(player.getUUID());
		if(config.getType().isGlobal())
			return Integer.MAX_VALUE;
		return getPlayerBaseClaimLimit(player) + config.getEffective(PlayerConfigOptions.BONUS_CHUNK_CLAIMS);
	}

	@Override
	public int getPlayerFullForceloadLimit(@Nonnull UUID playerId) {
		IPlayerConfig config = configManager.getLoadedConfig(playerId);
		if(config.getType().isGlobal())
			return Integer.MAX_VALUE;
		return getPlayerBaseForceloadLimit(playerId) + config.getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);
	}

	@Override
	public int getPlayerFullForceloadLimit(@Nonnull ServerPlayer player) {
		IPlayerConfig config = configManager.getLoadedConfig(player.getUUID());
		if(config.getType().isGlobal())
			return Integer.MAX_VALUE;
		return getPlayerBaseForceloadLimit(player) + config.getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);
	}

	public Iterator<ServerClaimStateHolder> getClaimStateHolderIterator(){
		return linkedClaimStates.iterator();
	}
	
	@Override
	public ClaimsManagerSynchronizer getClaimsManagerSynchronizer() {
		return claimsManagerSynchronizer;
	}

	@Override
	public ServerSpreadoutQueuedTaskHandler<PlayerAreaClaimActionSpreadoutTask> getAreaClaimActionTaskHandler() {
		return areClaimActionTaskHandler;
	}

	@Override
	public ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> getClaimReplaceTaskHandler() {
		return claimReplaceTaskHandler;
	}

	@Override
	public ServerClaimsPermissionHandler getPermissionHandler() {
		return permissionHandler;
	}

	@Override
	public PlayerPartySystemManager getPartySystemManager() {
		return partySystemManager;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public boolean isLoaded() {
		return loaded;
	}
	
	public void onLoad() {
		loaded = true;
	}

	@Override
	public boolean claimUsesDimensionSubConfigs(IPlayerChunkClaimAPI claimState) {
		return configManager.getLoadedConfig(claimState == null ? null : claimState.getPlayerId()).getType().hasDimensionSubConfigs();
	}

	@Override
	public String getDimensionName(IPlayerChunkClaimAPI claimState, Identifier dimension) {
		IPlayerConfig effectiveConfig = configManager.getLoadedConfig(claimState == null ? null : claimState.getPlayerId());
		if(dimension != null)
			effectiveConfig = effectiveConfig.getEffectiveSubConfig(dimension.toString());
		return effectiveConfig.getEffective(PlayerConfigOptions.CLAIMS_NAME);
	}

	@Override
	protected MutableComponent constructPlayerClaimName(ServerPlayerClaimInfo playerClaimInfo, Component forceloadedComponent, boolean allowPartyNames) {
		if(allowPartyNames && ServerConfig.CONFIG.partyOwnedClaims.get()) {
			Component partyName = playerClaimInfo.fetchPartyName();
			if(partyName == null) {
				if(!playerClaimInfo.isPartyOwned())
					return super.constructPlayerClaimName(playerClaimInfo, forceloadedComponent, allowPartyNames);
				partyName = playerClaimInfo.getDefaultPartyName();
			}
			return Component.translatable(
					"gui.xaero_pac_title_party_claim",
					partyName, forceloadedComponent
			);
		}
		return super.constructPlayerClaimName(playerClaimInfo, forceloadedComponent, allowPartyNames);
	}

	public IPlayerConfigManager getConfigManager(){
		return configManager;
	}

	@Nonnull
	@Override
	public ClaimActionListenerManager getActionListenerManager() {
		return actionListenerManager;
	}

	@Nonnull
	@Override
	public ChunkAccessOverriderManager getChunkAccessOverriderManager() {
		return chunkAccessOverriderManager;
	}

	@Override
	public void onServerTick(){
		tickCachedReclaimabilityCheck.onServerTick();
	}

	public final static class Builder extends ClaimsManager.Builder<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, ServerRegionClaims, ServerDimensionClaimsManager, ServerClaimStateHolder, Builder>{

		private MinecraftServer server;
		private IPlayerConfigManager configManager;
		private ForceLoadTicketManager ticketManager;
		private ClaimsManagerSynchronizer claimsManagerSynchronizer;
		private ServerSpreadoutQueuedTaskHandler<PlayerAreaClaimActionSpreadoutTask> areaClaimActionTaskHandler;
		private ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler;
		private ServerClaimsPermissionHandler permissionHandler;
		private PlayerPartySystemManager partySystemManager;
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
		@Override
		public Builder setDefault() {
			super.setDefault();
			setServer(null);
			setTicketManager(null);
			setClaimsManagerSynchronizer(null);
			setConfigManager(null);
			setAreaClaimActionTaskHandler(null);
			setClaimReplaceTaskHandler(null);
			setPermissionHandler(null);
			setPartySystemManager(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setTicketManager(ForceLoadTicketManager ticketManager) {
			this.ticketManager = ticketManager;
			return this;
		}
		
		public Builder setClaimsManagerSynchronizer(ClaimsManagerSynchronizer claimsManagerSynchronizer) {
			this.claimsManagerSynchronizer = claimsManagerSynchronizer;
			return this;
		}

		public Builder setAreaClaimActionTaskHandler(ServerSpreadoutQueuedTaskHandler<PlayerAreaClaimActionSpreadoutTask> areaClaimActionTaskHandler) {
			this.areaClaimActionTaskHandler = areaClaimActionTaskHandler;
			return this;
		}

		public Builder setClaimReplaceTaskHandler(ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler) {
			this.claimReplaceTaskHandler = claimReplaceTaskHandler;
			return this;
		}

		public Builder setConfigManager(IPlayerConfigManager configManager) {
			this.configManager = configManager;
			return self;
		}

		public Builder setPermissionHandler(ServerClaimsPermissionHandler permissionHandler) {
			this.permissionHandler = permissionHandler;
			return self;
		}

		public Builder setPartySystemManager(PlayerPartySystemManager partySystemManager) {
			this.partySystemManager = partySystemManager;
			return self;
		}

		@Override
		public ServerClaimsManager build() {
			if(
					server == null || ticketManager == null || claimsManagerSynchronizer == null ||
							configManager == null || claimReplaceTaskHandler == null || permissionHandler == null ||
							partySystemManager == null || areaClaimActionTaskHandler == null
			)
				throw new IllegalStateException();
			ServerPlayerClaimInfoManager playerInfoManager = new ServerPlayerClaimInfoManager(server, configManager, ticketManager, new HashMap<>(), new LinkedChain<>());
			setPlayerClaimInfoManager(playerInfoManager);
			ServerClaimsManager result = (ServerClaimsManager) super.build();
			playerInfoManager.setClaimsManager(result);
			claimsManagerSynchronizer.setClaimsManager(result);

			result.getPlayerInfo(PlayerConfig.SERVER_CLAIM_UUID).setPlayerUsername("\"Server\"");
			result.getPlayerInfo(PlayerConfig.EXPIRED_CLAIM_UUID).setPlayerUsername("\"Expiration\"");
			return result;
		}

		@Override
		protected ServerClaimsManager buildInternally(Map<PlayerChunkClaim, ServerClaimStateHolder> claimStates, ClaimsManagerTracker claimsManagerTracker, Int2ObjectMap<PlayerChunkClaim> indexToClaimState) {
			LinkedChain<ServerClaimStateHolder> linkedClaimStates = new LinkedChain<>();
			claimStates.values().forEach(linkedClaimStates::add);
			ClaimActionListenerManager actionListenerManager = ClaimActionListenerManager.Builder.begin().build();
			ChunkAccessOverriderManager chunkAccessOverriderManager = ChunkAccessOverriderManager.Builder.begin().build();
			TickCachedPlayerGroupCheck tickCachedReclaimabilityCheck = TickCachedPlayerGroupCheck.Builder.begin()
					.setOption(PlayerConfigOptions.CLAIM_EXCEPTION_RECLAIMABLE)
					.build();
			return new ServerClaimsManager(
					server, playerClaimInfoManager, configManager, dimensions,
					claimsManagerSynchronizer, indexToClaimState, claimStates, claimsManagerTracker,
					areaClaimActionTaskHandler, claimReplaceTaskHandler, permissionHandler, partySystemManager,
					linkedClaimStates, actionListenerManager, chunkAccessOverriderManager, tickCachedReclaimabilityCheck
			);
		}
		
	}
	
}
