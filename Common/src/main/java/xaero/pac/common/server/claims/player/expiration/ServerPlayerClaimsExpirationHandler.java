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

package xaero.pac.common.server.claims.player.expiration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectExpirationHandler;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.*;

public final class ServerPlayerClaimsExpirationHandler extends ObjectExpirationHandler<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager>{
	
	private final ServerClaimsManager claimsManager;
	private final MinecraftServer server;

	protected ServerPlayerClaimsExpirationHandler(ServerClaimsManager claimsManager, ServerInfo serverInfo, MinecraftServer server, ServerPlayerClaimInfoManager manager, long liveCheckInterval, int expirationTime,
			String checkingMessage) {
		super(serverInfo, manager, liveCheckInterval, expirationTime, checkingMessage);
		this.claimsManager = claimsManager;
		this.server = server;
	}

	@Override
	public void handle() {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		super.handle();
	}

	@Override
	protected void preExpirationCheck(ServerPlayerClaimInfo playerInfo) {
	}

	@Override
	protected boolean checkExpiration(ServerPlayerClaimInfo playerInfo) {
		if(
				Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, playerInfo.getPlayerId()) || 
				Objects.equals(PlayerConfig.EXPIRED_CLAIM_UUID, playerInfo.getPlayerId()) || 
				server.getPlayerList().getPlayer(playerInfo.getPlayerId()) != null)//player is logged in
			return true;
		return false;
	}

	@Override
	protected void expire(ServerPlayerClaimInfo playerInfo) {
		Map<ResourceLocation, List<ChunkPos>> posMap = new HashMap<>();
		playerInfo.getStream().forEach(entry -> {
			ResourceLocation dim = entry.getKey();
			List<ChunkPos> dimPosList = posMap.computeIfAbsent(dim, d -> new ArrayList<>());
			PlayerDimensionClaims dimensionClaims = entry.getValue();
			dimensionClaims.getStream().forEach(posList -> posList.getStream().forEach(dimPosList::add));
		});

		boolean expiredReclaim = ServerConfig.CONFIG.playerClaimsConvertExpiredClaims.get();
		posMap.forEach((dim, dimPosList) -> {
			for(ChunkPos pos : dimPosList) {
				if(expiredReclaim)
					claimsManager.claim(dim, PlayerConfig.EXPIRED_CLAIM_UUID, pos.x, pos.z, false);
				else
					claimsManager.unclaim(dim, pos.x, pos.z);
			}
		});
		manager.tryRemove(playerInfo.getPlayerId());
	}
	
	public static final class Builder extends ObjectExpirationHandler.Builder<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, Builder>{

		private ServerClaimsManager claimsManager;
		private MinecraftServer server;
		
		public Builder setDefault() {
			super.setDefault();
			setClaimsManager(null);
			setServer(null);
			setCheckingMessage("Checking for expired player claims...");
			setExpirationTime(ServerConfig.CONFIG.playerClaimsExpirationTime.get() * 60 * 60 * 1000);
			setLiveCheckInterval(ServerConfig.CONFIG.playerClaimsExpirationCheckInterval.get() * 60000);
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

}
