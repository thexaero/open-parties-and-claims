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

package xaero.pac.common.server.claims.player.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

public final class PlayerSubClaimTransferSpreadoutTask extends PlayerClaimReplaceSpreadoutTask {

	private PlayerSubClaimTransferSpreadoutTask(IPlayerClaimReplaceSpreadoutTaskCallback callback, UUID claimOwnerId, Predicate<IPlayerChunkClaim> matcher, IPlayerChunkClaim with) {
		super(callback, claimOwnerId, matcher, with);
	}

	public static final class Builder {

		private MinecraftServer server;
		private IServerPlayerClaimInfo<?> fromPlayerInfo;
		private IServerPlayerClaimInfo<?> toPlayerInfo;
		private String fromSubConfigId;
		private String toSubConfigId;
		private boolean forceloadable;
		private boolean last;
		private UUID originalRequesterUUID;

		private Builder(){}

		public Builder setDefault(){
			setServer(null);
			setFromPlayerInfo(null);
			setToPlayerInfo(null);
			setFromSubConfigId(null);
			setForceloadable(false);
			setLast(false);
			setOriginalRequesterUUID(null);
			setToSubConfigId(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setFromPlayerInfo(IServerPlayerClaimInfo<?> fromPlayerInfo) {
			this.fromPlayerInfo = fromPlayerInfo;
			return this;
		}

		public Builder setToPlayerInfo(IServerPlayerClaimInfo<?> toPlayerInfo) {
			this.toPlayerInfo = toPlayerInfo;
			return this;
		}

		public Builder setToSubConfigId(String toSubConfigId) {
			this.toSubConfigId = toSubConfigId;
			return this;
		}

		public Builder setFromSubConfigId(String fromSubConfigId) {
			this.fromSubConfigId = fromSubConfigId;
			return this;
		}

		public Builder setForceloadable(boolean forceloadable) {
			this.forceloadable = forceloadable;
			return this;
		}

		public Builder setLast(boolean last) {
			this.last = last;
			return this;
		}

		public Builder setOriginalRequesterUUID(UUID originalRequesterUUID) {
			this.originalRequesterUUID = originalRequesterUUID;
			return this;
		}

		public PlayerSubClaimTransferSpreadoutTask build(){
			if(server == null || fromPlayerInfo == null || toPlayerInfo == null || fromSubConfigId == null || toSubConfigId == null)
				throw new IllegalStateException();
			Callback callback = new Callback(server, originalRequesterUUID, last, fromPlayerInfo, toPlayerInfo);
			UUID claimOwnerId = fromPlayerInfo.getPlayerId();
			int fromSubConfigIndex = fromPlayerInfo.getConfig().getEffectiveSubConfig(fromSubConfigId).getSubIndex();
			int toSubConfigIndex = toPlayerInfo.getConfig().getEffectiveSubConfig(toSubConfigId).getSubIndex();
			Predicate<IPlayerChunkClaim> matcher = c -> c.getSubConfigIndex() == fromSubConfigIndex && c.isForceloadable() == forceloadable;
			IPlayerChunkClaim with = new PlayerChunkClaim(toPlayerInfo.getPlayerId(), toSubConfigIndex, forceloadable, 0);
			return new PlayerSubClaimTransferSpreadoutTask(callback, claimOwnerId, matcher, with);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

	private static final class Callback implements IPlayerClaimReplaceSpreadoutTaskCallback {

		private final MinecraftServer server;
		private final UUID originalRequesterUUID;
		private final boolean last;
		private final IServerPlayerClaimInfo<?> fromPlayerInfo;
		private final IServerPlayerClaimInfo<?> toPlayerInfo;

		public Callback(
				MinecraftServer server,
				UUID originalRequesterUUID,
				boolean last,
				IServerPlayerClaimInfo<?> fromPlayerInfo,
				IServerPlayerClaimInfo<?> toPlayerInfo
		) {
			this.server = server;
			this.originalRequesterUUID = originalRequesterUUID;
			this.last = last;
			this.fromPlayerInfo = fromPlayerInfo;
			this.toPlayerInfo = toPlayerInfo;
		}

		@Override
		public void onWork(int tickCount) {
		}

		@Override
		public void onFinish(ResultType resultType, int tickCount, int totalCount, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
			if(!last)
				return;
			ServerPlayer onlineRequester = originalRequesterUUID == null ? null : server.getPlayerList().getPlayer(originalRequesterUUID);
			notifyPlayerOfCompletion(onlineRequester, toPlayerInfo.getPlayerId().equals(originalRequesterUUID), resultType, serverData);
			if(!fromPlayerInfo.getPlayerId().equals(originalRequesterUUID)){
				ServerPlayer onlineFrom = server.getPlayerList().getPlayer(fromPlayerInfo.getPlayerId());
				notifyPlayerOfCompletion(onlineFrom, false, resultType, serverData);
			}
			if(!toPlayerInfo.getPlayerId().equals(originalRequesterUUID)) {
				ServerPlayer onlineTo = server.getPlayerList().getPlayer(toPlayerInfo.getPlayerId());
				notifyPlayerOfCompletion(onlineTo, true, resultType, serverData);
			}
			fromPlayerInfo.setTransferInProgress(false);
			toPlayerInfo.setTransferInProgress(false);
		}

		private void notifyPlayerOfCompletion(ServerPlayer onlinePlayer, boolean isTarget, ResultType resultType, IServerData<?, ?> serverData){
			if(onlinePlayer == null)
				return;
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			if (resultType.isSuccess()) {
				onlinePlayer.sendSystemMessage(adaptiveLocalizer.getFor(onlinePlayer,
						isTarget ? "gui.xaero_claims_transfer_success_to" : "gui.xaero_claims_transfer_success_from",
						fromPlayerInfo.getPlayerUsername(), toPlayerInfo.getPlayerUsername()
				));
				return;
			}
			onlinePlayer.sendSystemMessage(adaptiveLocalizer.getFor(onlinePlayer, resultType.getMessage()));
		}

	}

}
