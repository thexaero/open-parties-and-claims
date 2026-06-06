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

package xaero.pac.common.server.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ServerLoginHandshakePacket;
import xaero.pac.common.packet.util.PacketConstants;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.sync.PartySynchronizer;
import xaero.pac.common.server.parties.party.sync.player.PlayerFullPartySync;
import xaero.pac.common.server.player.config.sync.task.PlayerConfigSyncSpreadoutTask;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.Objects;
import java.util.UUID;

public class PlayerLoginHandler {
	
	public void handlePreWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
		long time = System.currentTimeMillis();

		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ServerLoginHandshakePacket(PacketConstants.NETWORK_VERSION));

		IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerClaimInfo = serverData.getServerClaimsManager().getPlayerInfo(player.getUUID());
		((ServerPlayerClaimInfo)(Object)playerClaimInfo).setPlayerUsername(player.getGameProfile().getName());

		UUID primaryPartyOwner = serverData.getPlayerPartySystemManager().getPrimaryPartyOwnerByMember(player.getUUID());
		if(primaryPartyOwner != null) {
			playerData.setLastPartyClaimsSync(time, primaryPartyOwner);
			playerData.setLastPartyOnlineUpdate(time, primaryPartyOwner);
			serverData.getPrimaryPartyOnlineCounter().registerOnlinePartyMember(primaryPartyOwner);
		}
		if(
				!ServerConfig.CONFIG.partyOwnedClaims.get() ||
				!Objects.equals(primaryPartyOwner, player.getUUID())
		)
			serverData.getForceLoadManager().updateTicketsFor(player.getUUID(), false);
		
		serverData.getPlayerPartyAssigner().assign(serverData, player, serverData.getPartyMemberInfoUpdater());

		PlayerFullPartySync playerFullPartySync = new PlayerFullPartySync((PartySynchronizer)(Object)serverData.getPartyManager().getPartySynchronizer());

		playerData.onLogin(
				playerFullPartySync,
				PlayerClaimActionRequestHandler.Builder.begin()
						.setManager(serverData.getServerClaimsManager())
						.setServerTickHandler(serverData.getServerTickHandler())
						.build(),
				PlayerConfigSyncSpreadoutTask.Builder.begin().build()
		);
		playerData.setOftenSyncedPartyMemberInfo(new PartyMemberDynamicInfoSyncable(player.getUUID(), true));
		
		serverData.getServerClaimsManager().getPlayerInfo(player.getUUID()).registerActivity(serverData.getServerInfo());
		if(primaryPartyOwner != null && ServerConfig.CONFIG.partyOwnedClaims.get())
			serverData.getServerClaimsManager().getPlayerInfo(primaryPartyOwner).registerActivity(serverData.getServerInfo());
	}
	
	public void handlePostWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);

		serverData.getPlayerConfigPermissionUpdater().update(playerData, player, serverData, false, false);
		serverData.getPlayerConfigManager().getSynchronizer().syncOnLogin(player);
		
		IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
		IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(player.getUUID());
		
		if(playerParty != null)
			serverData.getPartyManager().getPartySynchronizer().syncToClient(player, playerParty);
		
		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();
		claimsManager.getClaimsManagerSynchronizer().syncOnLogin(player);
	}

}
