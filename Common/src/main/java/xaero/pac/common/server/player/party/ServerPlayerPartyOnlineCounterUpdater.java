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

package xaero.pac.common.server.player.party;

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
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Objects;
import java.util.UUID;

public class ServerPlayerPartyOnlineCounterUpdater {

	private PrimaryPartyOnlineCounter primaryPartyOnlineCounter;

	public void setPrimaryPartyOnlineCounter(PrimaryPartyOnlineCounter primaryPartyOnlineCounter) {
		if(this.primaryPartyOnlineCounter != null)
			throw new IllegalStateException();
		this.primaryPartyOnlineCounter = primaryPartyOnlineCounter;
	}

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
		IPlayerPartySystemAPI<?> primaryPartySystem = serverData.getPlayerPartySystemManager().getPrimarySystem();
		if(primaryPartySystem == serverData.getPartyManager().getPartySystem())
			return;//party online counter is already updated when the default party system is used
		if(System.currentTimeMillis() - playerData.getLastPartyOnlineUpdateTime() < 1000)
			return;
		UUID lastPartyOwner = playerData.getLastPartyOnlineUpdateOwner();
		UUID partyOwner = serverData.getPlayerPartySystemManager().getPrimaryPartyOwnerByMember(player.getUUID());
		playerData.setLastPartyOnlineUpdate(System.currentTimeMillis(), partyOwner);
		if(Objects.equals(lastPartyOwner, partyOwner))
			return;
		if(lastPartyOwner != null)
			primaryPartyOnlineCounter.unregisterOnlinePartyMember(lastPartyOwner);
		if(partyOwner != null)
			primaryPartyOnlineCounter.registerOnlinePartyMember(partyOwner);
	}

	public void onAddedToDefaultParty(
			ServerPlayer player,
			IPartyManager<?> partyManager,
			IServerParty<?, ?, ?> party
	) {
		IPlayerPartySystemAPI<?> primaryPartySystem = partyManager.getPlayerConfigs().getPartySystemManager()
				.getPrimarySystem();
		if(primaryPartySystem != partyManager.getPartySystem())
			return;
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		UUID partyOwnerId = party.getOwner().getUUID();
		playerData.setLastPartyOnlineUpdate(System.currentTimeMillis(), partyOwnerId);
		primaryPartyOnlineCounter.registerOnlinePartyMember(partyOwnerId);
	}

	public void onRemovedFromDefaultParty(
			ServerPlayer player,
			IPartyManager<?> partyManager,
			IServerParty<?, ?, ?> party
	) {
		IPlayerPartySystemAPI<?> primaryPartySystem = partyManager.getPlayerConfigs().getPartySystemManager()
				.getPrimarySystem();
		if(primaryPartySystem != partyManager.getPartySystem())
			return;
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		playerData.setLastPartyOnlineUpdate(System.currentTimeMillis(), null);
		primaryPartyOnlineCounter.unregisterOnlinePartyMember(party.getOwner().getUUID());
	}

	public void onDefaultPartyOwnerChange(PartyManager partyManager, IServerParty<?, ?, ?> party, UUID oldOwner, UUID newOwner) {
		IPlayerPartySystemAPI<?> primaryPartySystem = partyManager.getPlayerConfigs().getPartySystemManager()
				.getPrimarySystem();
		if(primaryPartySystem != partyManager.getPartySystem())
			return;
		party.getOnlineMemberStream().map(player -> (ServerPlayerData) ServerPlayerData.from(player))
				.forEach(playerData -> {
					primaryPartyOnlineCounter.unregisterOnlinePartyMember(oldOwner);
					primaryPartyOnlineCounter.registerOnlinePartyMember(newOwner);
					playerData.setLastPartyOnlineUpdate(System.currentTimeMillis(), newOwner);
				});
	}

}
