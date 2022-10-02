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
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerClaimOwnerPropertiesSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerRegionSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerStateSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerSubClaimPropertiesSync;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.sync.task.PlayerConfigSyncSpreadoutTask;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

public class PlayerLoginHandler {
	
	public void handlePreWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundPacServerLoginResetPacket());

		IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerClaimInfo = serverData.getServerClaimsManager().getPlayerInfo(player.getUUID());
		((ServerPlayerClaimInfo)(Object)playerClaimInfo).setPlayerUsername(player.getGameProfile().getName());

		serverData.getForceLoadManager().updateTicketsFor(serverData.getPlayerConfigs(), player.getUUID(), false);
		
		serverData.getPlayerPartyAssigner().assign(serverData.getPartyManager(), player, serverData.getPartyMemberInfoUpdater());
		
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);

		ClaimsManagerPlayerClaimOwnerPropertiesSync claimsManagerPlayerClaimOwnerPropertiesSync = ClaimsManagerPlayerClaimOwnerPropertiesSync.Builder.begin()
				.setPlayer(player)
				.setSynchronizer((ClaimsManagerSynchronizer) serverData.getServerClaimsManager().getClaimsManagerSynchronizer())
				.build();
		ClaimsManagerPlayerSubClaimPropertiesSync claimsManagerPlayerSubClaimPropertiesSync = ClaimsManagerPlayerSubClaimPropertiesSync.Builder.begin()
				.setPlayer(player)
				.setClaimOwnerPropertiesSync(claimsManagerPlayerClaimOwnerPropertiesSync)
				.setSynchronizer((ClaimsManagerSynchronizer) serverData.getServerClaimsManager().getClaimsManagerSynchronizer())
				.build();
		ClaimsManagerPlayerStateSync claimsManagerPlayerStateSync = ClaimsManagerPlayerStateSync.Builder.begin()
				.setPlayer(player)
				.setSynchronizer((ClaimsManagerSynchronizer) serverData.getServerClaimsManager().getClaimsManagerSynchronizer())
				.setSubClaimPropertiesSync(claimsManagerPlayerSubClaimPropertiesSync)
				.build();

		playerData.onLogin(
				ClaimsManagerPlayerRegionSync.Builder.begin()
						.setClaimsManager(serverData.getServerClaimsManager())
						.setStateSyncHandler(claimsManagerPlayerStateSync)
						.setPlayerId(player.getUUID())
						.build(),
				claimsManagerPlayerStateSync,
				claimsManagerPlayerClaimOwnerPropertiesSync,
				claimsManagerPlayerSubClaimPropertiesSync,
				PlayerClaimActionRequestHandler.Builder.begin()
						.setManager(serverData.getServerClaimsManager())
						.setServerTickHandler(serverData.getServerTickHandler())
						.build(),
				PlayerConfigSyncSpreadoutTask.Builder.begin().build()
		);
		playerData.setOftenSyncedPartyMemberInfo(new PartyMemberDynamicInfoSyncable(player.getUUID(), true));
		
		serverData.getServerClaimsManager().getPlayerInfo(player.getUUID()).registerActivity();
	}
	
	public void handlePostWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		serverData.getPlayerConfigs().getSynchronizer().syncAllToClient(player);
		
		IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
		IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(player.getUUID());
		
		if(playerParty != null)
			serverData.getPartyManager().getPartySynchronizer().syncToClient(player, playerParty);
		
		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();
		claimsManager.getClaimsManagerSynchronizer().syncOnLogin(player);
	}

}
