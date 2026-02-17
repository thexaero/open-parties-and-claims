/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2025, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server;

import net.minecraft.server.MinecraftServer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.impl.DefaultPlayerPartySystem;

public class ServerStartingCallback {
	
	private final PlayerClaimInfoManagerIO<?> playerClaimInfoManagerIO;
	
	public ServerStartingCallback(PlayerClaimInfoManagerIO<?> playerClaimInfoManagerIO) {
		super();
		this.playerClaimInfoManagerIO = playerClaimInfoManagerIO;
	}

	public void onLoad(MinecraftServer server) {
		IServerData<
			IServerClaimsManager<
				IPlayerChunkClaim,
				IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
				IServerDimensionClaimsManager<IServerRegionClaims>
			>,
			IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
		> serverData = ServerData.from(server);
		try {
			serverData.getPlayerPermissionSystemManager().preRegister();
			serverData.getPlayerPartySystemManager().preRegister();
			serverData.getPlayerPartySystemManager().register("default", new DefaultPlayerPartySystem(serverData.getPartyManager()));
			OpenPartiesAndClaims.INSTANCE.getCommonEvents().fireAddonRegisterEvent(serverData);
		} finally {
			serverData.getPlayerPermissionSystemManager().postRegister();
			serverData.getPlayerPartySystemManager().postRegister();
		}
		playerClaimInfoManagerIO.load();
		serverData.getPlayerPermissionSystemManager().updateUsedSystem(ServerConfig.CONFIG.permissionSystem.get());
		serverData.getPlayerPartySystemManager().updatePrimarySystem(ServerConfig.CONFIG.primaryPartySystem.get());
	}
	
}
