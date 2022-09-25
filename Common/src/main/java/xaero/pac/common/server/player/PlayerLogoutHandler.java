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
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.parties.party.sync.IPartyMemberDynamicInfoSynchronizer;

public class PlayerLogoutHandler {
	
	public void handle(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) {
		serverData.getForceLoadManager().updateTicketsFor(serverData.getPlayerConfigs(), player.getUUID(), true);
		//PlayerMainCapability playerMainCap = (PlayerMainCapability) player.getCapability(PlayerCapabilityProvider.MAIN_CAP).orElse(null);
		IServerParty<IPartyMember, IPartyPlayerInfo> playerParty = serverData.getPartyManager().getPartyByMember(player.getUUID());
		if(playerParty != null) {
			((ServerParty)(Object)playerParty).registerActivity();
			IPartyMemberDynamicInfoSynchronizer<IServerParty<IPartyMember, IPartyPlayerInfo>> partyOftenSyncedSync = serverData.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync();
			partyOftenSyncedSync.handlePlayerLeave(playerParty, player);
		}
		if(serverData.getServerClaimsManager().hasPlayerInfo(player.getUUID())) {
			serverData.getServerClaimsManager().getPlayerInfo(player.getUUID()).registerActivity();
		}

		serverData.getServerTickHandler().getLazyPacketSender().clearForPlayer(player);
	}

}
