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

package xaero.pac.common.server.claims.player.request;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.request.ClaimActionRequest;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.server.ServerTickHandler;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.UUID;

public class PlayerClaimActionRequestHandler {
	
	private final ServerPlayer player;
	private final ServerClaimsManager manager;
	private final ServerTickHandler serverTickHandler;
	private long lastRequestTickCounter;
	
	private PlayerClaimActionRequestHandler(ServerPlayer player, ServerClaimsManager manager, ServerTickHandler serverTickHandler) {
		super();
		this.player = player;
		this.manager = manager;
		this.serverTickHandler = serverTickHandler;
	}

	public void onReceive(ClaimActionRequest request) {
		if(serverTickHandler.getTickCounter() == lastRequestTickCounter)
			return;
		if(request.isByServer() && !player.hasPermissions(2))
			return;
		UUID playerId = request.isByServer() ? PlayerConfig.SERVER_CLAIM_UUID : player.getUUID();
		int fromX = player.chunkPosition().x;
		int fromZ = player.chunkPosition().z;
		AreaClaimResult result = manager.tryClaimActionOverArea(player.level.dimension().location(), playerId, 
				fromX, fromZ, request.getLeft(), request.getTop(), request.getRight(), request.getBottom(), 
				request.getAction(), false);
		manager.getClaimsManagerSynchronizer().syncToPlayerClaimActionResult(result, player);
		lastRequestTickCounter = serverTickHandler.getTickCounter();
	}
	
	public static final class Builder {
		private ServerPlayer player;
		private ServerClaimsManager manager;
		private ServerTickHandler serverTickHandler;

		private Builder() {
		}

		private Builder setDefault() {
			setPlayer(null);
			setManager(null);
			setServerTickHandler(null);
			return this;
		}
		
		public Builder setPlayer(ServerPlayer player) {
			this.player = player;
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
			if (manager == null || player == null || serverTickHandler == null)
				throw new IllegalStateException();
			return new PlayerClaimActionRequestHandler(player, manager, serverTickHandler);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
