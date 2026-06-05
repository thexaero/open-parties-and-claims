/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.UUID;

public class ServerPlayerClaimPartyNameUpdater {

	public void onPlayerTick(
			ServerPlayerData playerData,
			ServerPlayer player,
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
					>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
			> serverData
	) {
		if(!ServerConfig.CONFIG.partyOwnedClaims.get())
			return;
		IPlayerPartySystemAPI<?> primaryPartySystem = serverData.getPlayerPartySystemManager().getPrimarySystem();
		if(primaryPartySystem == serverData.getPartyManager().getPartySystem())
			return;//built-in party names are resynced on player config option changes and party creation/removal, so no additional checks are needed

		//always update player's party first, in case the player destroyed their party and joined another between updates
		updateOwnedBy(player.getUUID(), primaryPartySystem, serverData);
		UUID partyOwner = serverData.getPlayerPartySystemManager().getPrimaryPartyOwnerByMember(player.getUUID());
		if(partyOwner == null || player.getUUID().equals(partyOwner))
			return;
		updateOwnedBy(partyOwner, primaryPartySystem, serverData);
	}

	private void updateOwnedBy(
			UUID partyOwner,
			IPlayerPartySystemAPI<?> primaryPartySystem,
			IServerData<
				IServerClaimsManager<
					IPlayerChunkClaim,
					IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
					IServerDimensionClaimsManager<IServerRegionClaims>
				>,
				IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
			> serverData
	){
		IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> claimInfo =
				serverData.getServerClaimsManager().getPlayerInfo(partyOwner);
		if(System.currentTimeMillis() - claimInfo.getPartyNameSyncedTime() < 1000)
			return;
		claimInfo.resyncPartyName(primaryPartySystem);
	}

}
