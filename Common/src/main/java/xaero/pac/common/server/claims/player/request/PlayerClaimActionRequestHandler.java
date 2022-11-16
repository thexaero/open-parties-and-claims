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

package xaero.pac.common.server.claims.player.request;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.request.ClaimActionRequest;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.ServerTickHandler;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.Set;
import java.util.UUID;

public class PlayerClaimActionRequestHandler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private final ServerClaimsManager manager;
	private final ServerTickHandler serverTickHandler;
	private long lastRequestTickCounter;
	
	private PlayerClaimActionRequestHandler(ServerClaimsManager manager, ServerTickHandler serverTickHandler) {
		super();
		this.manager = manager;
		this.serverTickHandler = serverTickHandler;
	}

	public void onReceive(ServerPlayer player, ClaimActionRequest request) {
		if(serverTickHandler.getTickCounter() == lastRequestTickCounter)
			return;
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
		boolean shouldServerClaim = request.isByServer();
		if(playerData.isClaimsServerMode())
			shouldServerClaim = true;
		if(shouldServerClaim && manager.getPermissionHandler().shouldPreventServerClaim(player, playerData, player.getServer())){
			manager.getClaimsManagerSynchronizer().syncToPlayerClaimActionResult(
					new AreaClaimResult(Set.of(ClaimResult.Type.NO_SERVER_PERMISSION), request.getLeft(), request.getTop(), request.getRight(), request.getBottom()),
					player);
			return;
		}
		manager.getPermissionHandler().ensureAdminModeStatusPermission(player, playerData);
		UUID playerId = shouldServerClaim ? PlayerConfig.SERVER_CLAIM_UUID : player.getUUID();
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		IPlayerConfig playerConfig = serverData.getPlayerConfigs().getLoadedConfig(player.getUUID());
		IPlayerConfig usedSubConfig = shouldServerClaim ? playerConfig.getUsedServerSubConfig() : playerConfig.getUsedSubConfig();
		int subConfigIndex = usedSubConfig.getSubIndex();
		int fromX = player.chunkPosition().x;
		int fromZ = player.chunkPosition().z;
		AreaClaimResult result = manager.tryClaimActionOverArea(player.level.dimension().location(), playerId, subConfigIndex,
				fromX, fromZ, request.getLeft(), request.getTop(), request.getRight(), request.getBottom(),
				request.getAction(), playerData.isClaimsAdminMode());
		manager.getClaimsManagerSynchronizer().syncToPlayerClaimActionResult(result, player);
		lastRequestTickCounter = serverTickHandler.getTickCounter();
	}

	public long getLastRequestTickCounter() {
		return lastRequestTickCounter;
	}

	public void setLastRequestTickCounter(long lastRequestTickCounter) {
		this.lastRequestTickCounter = lastRequestTickCounter;
	}

	public static final class Builder {
		private ServerClaimsManager manager;
		private ServerTickHandler serverTickHandler;

		private Builder() {
		}

		private Builder setDefault() {
			setManager(null);
			setServerTickHandler(null);
			return this;
		}

		public Builder setManager(IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> manager) {
			this.manager = (ServerClaimsManager) (Object) manager;
			return this;
		}
		
		public Builder setServerTickHandler(ServerTickHandler serverTickHandler) {
			this.serverTickHandler = serverTickHandler;
			return this;
		}

		public PlayerClaimActionRequestHandler build() {
			if (manager == null || serverTickHandler == null)
				throw new IllegalStateException();
			return new PlayerClaimActionRequestHandler(manager, serverTickHandler);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
