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

package xaero.pac.common.server.claims.player.task;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.ClaimLocation;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.task.IServerSpreadoutQueuedTask;

import java.util.*;
import java.util.function.Predicate;

public class PlayerClaimReplaceSpreadoutTask implements IServerSpreadoutQueuedTask<PlayerClaimReplaceSpreadoutTask> {

	private final IPlayerClaimReplaceSpreadoutTaskCallback callback;
	private final UUID claimOwnerId;
	private final IPlayerChunkClaim with;
	private final Predicate<IPlayerChunkClaim> matcher;
	private boolean finished;
	private int totalCount;

	public PlayerClaimReplaceSpreadoutTask(IPlayerClaimReplaceSpreadoutTaskCallback callback, UUID claimOwnerId, Predicate<IPlayerChunkClaim> matcher, IPlayerChunkClaim with) {
		this.callback = callback;
		this.claimOwnerId = claimOwnerId;
		this.matcher = matcher;
		this.with = with;
	}

	@Override
	public void onQueued(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
				claimManager = serverData.getServerClaimsManager();
		IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = claimManager.getPlayerInfo(claimOwnerId);
		playerInfo.setReplacementInProgress(true);
	}

	@Override
	public boolean shouldWork(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, PlayerClaimReplaceSpreadoutTask holder) {
		return !shouldDrop(serverData, holder);
	}

	@Override
	public boolean shouldDrop(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, PlayerClaimReplaceSpreadoutTask holder) {
		return finished;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, PlayerClaimReplaceSpreadoutTask holder, int perTick, List<PlayerClaimReplaceSpreadoutTask> tasksToAdd) {
		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
				claimManager = serverData.getServerClaimsManager();

		IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = claimManager.getPlayerInfo(claimOwnerId);

		int tickCount = 0;
		ResultType resultType = null;
		if(with != null && with.getPlayerId().equals(claimOwnerId) && matcher.test(with)){
			//the new state should not match what is being replaced
			resultType = ResultType.FAILURE_STATE_MATCHES;
			finished = true;
		} else {
			Iterator<Map.Entry<ResourceLocation, IPlayerDimensionClaims<IPlayerClaimPosList>>> dimensionIterator = playerInfo.getFullStream().iterator();
			Iterator<IPlayerClaimPosList> claimPosListIterator = null;
			Iterator<ChunkPos> claimPosIterator = null;
			List<ClaimLocation> locations = new ArrayList<>(perTick);
			while (dimensionIterator.hasNext() && locations.size() < perTick) {
				Map.Entry<ResourceLocation, IPlayerDimensionClaims<IPlayerClaimPosList>> entry = dimensionIterator.next();
				ResourceLocation dimId = entry.getKey();
				IPlayerDimensionClaims<IPlayerClaimPosList> dim = entry.getValue();
				claimPosListIterator = dim.getStream().iterator();
				while (claimPosListIterator.hasNext() && locations.size() < perTick) {
					IPlayerClaimPosList claimPosList = claimPosListIterator.next();
					IPlayerChunkClaim claimState = claimPosList.getClaimState();
					if (matcher.test(claimState)) {
						claimPosIterator = claimPosList.getStream().iterator();
						while (claimPosIterator.hasNext() && locations.size() < perTick) {
							ChunkPos claimChunkPos = claimPosIterator.next();
							locations.add(new ClaimLocation(dimId, claimChunkPos.x, claimChunkPos.z));
							totalCount++;
							tickCount++;
						}
					}
				}
			}
			if (!dimensionIterator.hasNext() &&
					(
							claimPosListIterator == null
									||
							!claimPosListIterator.hasNext() &&
									(claimPosIterator == null || !claimPosIterator.hasNext())
					)
			) {
				resultType = ResultType.SUCCESS;
				finished = true;
			}
			if (with == null)
				locations.forEach(cl -> claimManager.unclaim(cl.getDimId(), cl.getChunkX(), cl.getChunkZ()));
			else
				locations.forEach(cl -> claimManager.claim(cl.getDimId(), with.getPlayerId(), with.getSubConfigIndex(), cl.getChunkX(), cl.getChunkZ(), with.isForceloadable()));
		}
		if(finished) {
			callback.onFinish(resultType, tickCount, totalCount, serverData);
			playerInfo.setReplacementInProgress(false);
			//queueing the next task
			if(playerInfo.hasReplacementTasks())
				tasksToAdd.add(playerInfo.removeNextReplacementTask());
		} else
			callback.onWork(tickCount);
	}

	public enum ResultType {

		SUCCESS(true, false, new TranslatableComponent("gui.xaero_claims_replacement_success")),
		FAILURE_STATE_MATCHES(false, true, new TranslatableComponent("gui.xaero_claims_replacement_state_matches_matcher"));

		private final boolean success;
		private final boolean failure;
		private final Component message;

		ResultType(boolean success, boolean failure, Component message) {
			this.success = success;
			this.failure = failure;
			this.message = message;
		}

		public Component getMessage() {
			return message;
		}

		public boolean isFailure() {
			return failure;
		}

		public boolean isSuccess() {
			return success;
		}

	}

}
