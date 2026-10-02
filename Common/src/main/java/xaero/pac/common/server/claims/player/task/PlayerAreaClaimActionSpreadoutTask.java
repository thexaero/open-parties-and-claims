/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.player.task;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.claims.action.request.ClaimActionRequest;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.claims.util.ClaimsUtils;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.task.IServerSpreadoutQueuedTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerAreaClaimActionSpreadoutTask implements IServerSpreadoutQueuedTask<PlayerAreaClaimActionSpreadoutTask> {

	private final ClaimActionRequest actionRequest;
	private final boolean force;
	private final UUID playerId;
	private final int subConfigIndex;
	private final Identifier fromDimension;
	private final int fromX;
	private final int fromZ;
	private final Set<ClaimResult.Type> resultTypes = new HashSet<>();
	private final Set<Component> customReasons;
	private final Consumer<AreaClaimResult> resultListener;
	private final Predicate<ChunkPos> anchorFilter;
	private int chunksToAffect;
	private boolean finished;
	private int currentIndex = 0;
	private int anchorRange;
	private List<ChunkPos> overlappingAnchors;
	private int effectiveLeft;
	private int effectiveTop;
	private int effectiveRight;
	private int effectiveBottom;
	private int effectiveTotal;
	private int claimLimit;
	private int forceloadLimit;

	public PlayerAreaClaimActionSpreadoutTask(
			ClaimActionRequest actionRequest,
			boolean force,
			UUID playerId,
			int subConfigIndex,
			Identifier fromDimension,
			int fromX,
			int fromZ,
			int chunksToAffect,
			Predicate<ChunkPos> anchorFilter,
			Consumer<AreaClaimResult> resultListener
	) {
		this.actionRequest = actionRequest;
		this.force = force;
		this.playerId = playerId;
		this.subConfigIndex = subConfigIndex;
		this.fromDimension = fromDimension;
		this.fromX = fromX;
		this.fromZ = fromZ;
		this.anchorFilter = anchorFilter;
		this.customReasons = new HashSet<>();
		this.chunksToAffect = chunksToAffect;
		this.resultListener = resultListener;
		this.overlappingAnchors = new ArrayList<>();
	}

	@Override
	public void onQueued(IServerData<?, ?> serverData) {
		IServerClaimsManager<?, ?, ?> claimManager = serverData.getServerClaimsManager();
		IServerPlayerClaimInfo<?> playerInfo = claimManager.getPlayerInfo(playerId);
		playerInfo.setAreaClaimTaskInProgress(this);
	}

	@Override
	public boolean shouldWork(
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData,
			PlayerAreaClaimActionSpreadoutTask holder
	) {
		return !shouldDrop(serverData, holder);
	}

	@Override
	public boolean shouldDrop(
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData,
			PlayerAreaClaimActionSpreadoutTask holder
	) {
		return finished;
	}

	@Override
	public void onTick(
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData,
			PlayerAreaClaimActionSpreadoutTask holder,
			int perTick,
			List<PlayerAreaClaimActionSpreadoutTask> tasksToAdd
	) {
		IServerClaimsManager<?, ?, ?> claimManager = serverData.getServerClaimsManager();
		ClaimingAction action = actionRequest.getAction();
		Identifier dimension = actionRequest.getDimension();
		boolean isServer = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID);
		boolean usingAnchors = !isServer && (!force || action.getAlwaysChecksAnchors()) && ServerConfig.CONFIG.anchorBasedClaiming.get();
		if(currentIndex == 0) {
			effectiveLeft = actionRequest.getLeft();
			effectiveTop = actionRequest.getTop();
			effectiveRight = actionRequest.getRight();
			effectiveBottom = actionRequest.getBottom();
			if(!force) {
				int maxClaimDistance = ServerConfig.CONFIG.maxClaimDistance.get();
				boolean outOfBounds = false;
				if(effectiveLeft < fromX - maxClaimDistance) {
					effectiveLeft = fromX - maxClaimDistance;
					outOfBounds = true;
				}
				if(effectiveTop < fromZ - maxClaimDistance) {
					effectiveTop = fromZ - maxClaimDistance;
					outOfBounds = true;
				}
				if(effectiveRight > fromX + maxClaimDistance) {
					effectiveRight = fromX + maxClaimDistance;
					outOfBounds = true;
				}
				if(effectiveBottom > fromZ + maxClaimDistance) {
					effectiveBottom = fromZ + maxClaimDistance;
					outOfBounds = true;
				}
				if(outOfBounds)
					resultTypes.add(ClaimResult.Type.TOO_FAR);
			}
			if(effectiveLeft > effectiveRight || effectiveTop > effectiveBottom)
				effectiveTotal = 0;
			else
				effectiveTotal = (1 + effectiveRight - effectiveLeft) * (1 + effectiveBottom - effectiveTop);
			claimLimit = claimManager.getPlayerFullClaimLimit(playerId);
			forceloadLimit = claimManager.getPlayerFullForceloadLimit(playerId);
			if(usingAnchors && effectiveTotal > 0){
				anchorRange = claimManager.getPlayerFullAnchorRange(playerId, dimension);
				IServerPlayerClaimInfo<?> playerClaimInfo = claimManager.getPlayerInfo(playerId);
				IPlayerDimensionClaims<?> dimensionClaims = playerClaimInfo.getDimension(dimension);
				if(dimensionClaims != null){
					for (ChunkPos anchor : dimensionClaims.getAnchors()) {
						if(anchorFilter != null && !anchorFilter.test(anchor))
							continue;
						int anchorLeft = anchor.x() - anchorRange;
						if(anchorLeft > effectiveRight)
							continue;
						int anchorRight = anchor.x() + anchorRange;
						if(anchorRight < effectiveLeft)
							continue;
						int anchorTop = anchor.z() - anchorRange;
						if(anchorTop > effectiveBottom)
							continue;
						int anchorBottom = anchor.z() + anchorRange;
						if(anchorBottom < effectiveTop)
							continue;
						overlappingAnchors.add(anchor);
					}
				}
			}
		}
		int workUntilIndex = Math.min(currentIndex + perTick, effectiveTotal);
		int effectiveHeight = (1 + effectiveBottom - effectiveTop);
		for (; currentIndex < workUntilIndex; currentIndex++){
			int x = effectiveLeft + currentIndex / effectiveHeight;
			int z = effectiveTop + currentIndex % effectiveHeight;
			ClaimResult<PlayerChunkClaim> result;
			if(action == ClaimingAction.CLAIM) {
				if(usingAnchors && !ClaimsUtils.withinAnchorDistance(overlappingAnchors, anchorRange, x, z))
					result = new ClaimResult<>(null, ClaimResult.Type.ANCHOR_TOO_FAR);
				else
					result = claimManager.tryToClaimHelper(dimension, playerId, subConfigIndex, fromX, fromZ, x, z, false, force, isServer, claimLimit, action);
			} else if(action == ClaimingAction.UNCLAIM)
				result = claimManager.tryToUnclaimHelper(dimension, playerId, fromX, fromZ, x, z, force, false);
			else if(action == ClaimingAction.UNCLAIM_UNANCHORED) {
				if(!ClaimsUtils.withinAnchorDistance(overlappingAnchors, anchorRange, x, z)) {
					result = claimManager.tryToUnclaimHelper(dimension, playerId, x, z, x, z, false, true);
					if(result.getResultType().fail)
						result = new ClaimResult<>(null, ClaimResult.Type.CLAIM_STILL_ANCHORED);
				} else
					result = new ClaimResult<>(null, ClaimResult.Type.CLAIM_STILL_ANCHORED);
			}
			else if(action == ClaimingAction.FORCELOAD)
				result = claimManager.tryToForceloadHelper(dimension, playerId, fromX, fromZ, x, z, true, force, isServer, claimLimit, forceloadLimit);
			else if(action == ClaimingAction.UNFORCELOAD)
				result = claimManager.tryToForceloadHelper(dimension, playerId, fromX, fromZ, x, z, false, force, isServer, claimLimit, forceloadLimit);
			else {
				finish(serverData, tasksToAdd);
				return;
			}
			resultTypes.add(result.getResultType());
			if(result.getCustomReason() != null && customReasons.size() < 4)
				customReasons.add(result.getCustomReason());
			if(result.getResultType().success) {
				chunksToAffect--;
				if(chunksToAffect <= 0 && currentIndex != effectiveTotal - 1) {
					resultTypes.add(ClaimResult.Type.TOO_MANY_CHUNKS);
					finish(serverData, tasksToAdd);
					return;
				}
			}
			if(result.getResultType().interruptsAreaAction) {
				finish(serverData, tasksToAdd);
				return;
			}
		}
		if(currentIndex >= effectiveTotal)
			finish(serverData, tasksToAdd);
	}

	private void finish(IServerData<?, ?> serverData, List<PlayerAreaClaimActionSpreadoutTask> tasksToAdd){
		finished = true;
		IServerClaimsManager<?, ?, ?> claimManager = serverData.getServerClaimsManager();
		IServerPlayerClaimInfo<?> playerInfo = claimManager.getPlayerInfo(playerId);
		playerInfo.setAreaClaimTaskInProgress(null);
		Identifier dimension = actionRequest.getDimension();
		int left = actionRequest.getLeft();
		int top = actionRequest.getTop();
		int right = actionRequest.getRight();
		int bottom = actionRequest.getBottom();
		AreaClaimResult result = new AreaClaimResult(resultTypes, customReasons, dimension, left, top, right, bottom);
		resultListener.accept(result);
		//queueing the next task
		if(tasksToAdd != null && playerInfo.hasAreaClaimActionTasks())
			tasksToAdd.add(playerInfo.removeNextAreaClaimActionTask());
	}

	public boolean interrupt(IServerData<?, ?> serverData){
		if(actionRequest.getAction().isUninterruptible())
			return false;
		resultTypes.add(ClaimResult.Type.INTERRUPTED);
		finish(serverData, null);
		return true;
	}

}
