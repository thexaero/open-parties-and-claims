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

package xaero.pac.common.server.claims.player.expiration;

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.claims.player.task.IPlayerClaimReplaceSpreadoutTaskCallback;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectExpirationHandler;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.Objects;

public final class ServerPlayerClaimsExpirationHandler extends ObjectExpirationHandler<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> {
	
	private final ServerClaimsManager claimsManager;
	private final MinecraftServer server;

	protected ServerPlayerClaimsExpirationHandler(ServerClaimsManager claimsManager, ServerInfo serverInfo, MinecraftServer server, ServerPlayerClaimInfoManager manager, long liveCheckInterval, long expirationTime,
			String checkingMessage) {
		super(serverInfo, manager, liveCheckInterval, expirationTime, checkingMessage);
		this.claimsManager = claimsManager;
		this.server = server;
	}

	@Override
	protected void handle(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		super.handle(serverData);
	}

	@Override
	public void preExpirationCheck(ServerPlayerClaimInfo playerInfo) {
	}

	@Override
	public boolean checkIfActive(ServerPlayerClaimInfo playerInfo) {
		if(
				Objects.equals(PlayerConfig.EXPIRED_CLAIM_UUID, playerInfo.getPlayerId()) ||
				Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, playerInfo.getPlayerId()) ||
				server.getPlayerList().getPlayer(playerInfo.getPlayerId()) != null)//player is logged in
			return true;
		return false;
	}

	@Override
	public boolean expire(ServerPlayerClaimInfo playerInfo, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) {
		PlayerChunkClaim toReplaceWith = ServerConfig.CONFIG.playerClaimsConvertExpiredClaims.get() ?
			new PlayerChunkClaim(PlayerConfig.EXPIRED_CLAIM_UUID, -1, false, 0) : null;
		SpreadoutTaskCallback spreadoutTaskCallback = new SpreadoutTaskCallback(playerInfo);
		playerInfo.addReplacementTask(new PlayerClaimReplaceSpreadoutTask(spreadoutTaskCallback, playerInfo.getPlayerId(), s -> true, toReplaceWith), serverData);
		return false;
	}

	public static final class Builder extends ObjectExpirationHandler.Builder<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, Builder>{

		private ServerClaimsManager claimsManager;
		private MinecraftServer server;
		
		public Builder setDefault() {
			super.setDefault();
			setClaimsManager(null);
			setServer(null);
			setCheckingMessage("Checking for expired player claims...");
			setExpirationTime((long) ServerConfig.CONFIG.playerClaimsExpirationTime.get() * 60 * 60 * 1000);
			setLiveCheckInterval((long) ServerConfig.CONFIG.playerClaimsExpirationCheckInterval.get() * 60000);
			return this;
		}
		
		public Builder setClaimsManager(ServerClaimsManager claimsManager) {
			this.claimsManager = claimsManager;
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public ServerPlayerClaimsExpirationHandler build() {
			if(server == null || claimsManager == null)
				throw new IllegalStateException();
			return (ServerPlayerClaimsExpirationHandler) super.build();
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}

		@Override
		protected ObjectExpirationHandler<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> buildInternally() {
			return new ServerPlayerClaimsExpirationHandler(claimsManager, serverInfo, server, manager, liveCheckInterval, expirationTime, checkingMessage);
		}
		
	}

	public class SpreadoutTaskCallback implements IPlayerClaimReplaceSpreadoutTaskCallback {

		private final ServerPlayerClaimInfo playerInfo;

		public SpreadoutTaskCallback(ServerPlayerClaimInfo playerInfo) {
			this.playerInfo = playerInfo;
		}

		@Override
		public void onWork(int tickCount) {
		}

		@Override
		public void onFinish(PlayerClaimReplaceSpreadoutTask.ResultType resultType, int tickCount, int totalCount, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) {
			if(resultType.isSuccess())
				manager.tryRemove(playerInfo.getPlayerId());
			onElementExpirationDone();
		}

	}

}
