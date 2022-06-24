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

package xaero.pac.common.server.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ClientboundPacServerLoginResetPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerSyncHandler;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.sync.IPlayerConfigSynchronizer;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;

public class PlayerLoginHandler {
	
	public void handlePreWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) {
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundPacServerLoginResetPacket());

		((ServerPlayerClaimInfo)(Object)serverData.getServerClaimsManager().getPlayerInfo(player.getUUID())).setPlayerUsername(player.getGameProfile().getName());
		serverData.getForceLoadManager().updateTicketsFor(serverData.getPlayerConfigs(), player.getUUID(), false);
		
		serverData.getPlayerPartyAssigner().assign(serverData.getPartyManager(), player, serverData.getPartyMemberInfoUpdater());
		
		ServerPlayerData playerData = new ServerPlayerData(
				ClaimsManagerPlayerSyncHandler.Builder.begin().setPlayer(player).setClaimsManager(serverData.getServerClaimsManager()).build(),
				PlayerClaimActionRequestHandler.Builder.begin()
					.setPlayer(player)
					.setManager(serverData.getServerClaimsManager())
					.setServerTickHandler(serverData.getServerTickHandler()).build()
				);
		((IOpenPACServerPlayer)player).setXaero_OPAC_PlayerData(playerData);
		playerData.setOftenSyncedPartyMemberInfo(new PartyMemberDynamicInfoSyncable(player.getUUID(), true));
		
		serverData.getServerClaimsManager().getPlayerInfo(player.getUUID()).registerActivity();
	}
	
	public void handlePostWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) {
		IPlayerConfigManager<IServerParty<IPartyMember, IPartyPlayerInfo>> configManager = serverData.getPlayerConfigs();
		IPlayerConfigSynchronizer synchronizer = configManager.getSynchronizer();
		
		synchronizer.syncToClient(player);
		
		IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo>> partyManager = serverData.getPartyManager();
		IServerParty<IPartyMember, IPartyPlayerInfo> playerParty = partyManager.getPartyByMember(player.getUUID());
		
		if(playerParty != null)
			serverData.getPartyManager().getPartySynchronizer().syncToClient(player, playerParty);
		
		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();
		claimsManager.getClaimsManagerSynchronizer().syncOnLogin(player);
	}

}
