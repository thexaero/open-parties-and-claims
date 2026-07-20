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
import xaero.pac.common.claims.util.ClaimsConstants;
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
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Objects;
import java.util.UUID;

public class ServerPlayerClaimPartyUpdater {

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
			return;//party-related changes are already synced when the default party system is used
		if(System.currentTimeMillis() - playerData.getLastPartyClaimsSyncTime() < 1000)
			return;
		updatePrimaryPartyColor(player, serverData);
		IPlayerConfig partyConfig = serverData.getPlayerConfigManager().getPartyOwnerConfig(player.getUUID());
		UUID lastPartyOwner = playerData.getLastPartyClaimsSyncPartyOwner();
		UUID partyOwner = partyConfig == null ? null : partyConfig.getPlayerId();
		playerData.setLastPartyClaimsSync(System.currentTimeMillis(), partyOwner);
		if(Objects.equals(lastPartyOwner, partyOwner))
			return;
		serverData.getPlayerConfigManager().getSynchronizer().requestPartyClaimsConfigSync(partyConfig, player);
		if(ServerConfig.CONFIG.claimsSynchronization.get() != ServerConfig.ClaimsSyncType.OWNED_ONLY)
			return;
		if((lastPartyOwner == null || partyOwner == null) &&
				(player.getUUID().equals(lastPartyOwner) || player.getUUID().equals(partyOwner)))//don't need a resync in such cases because nothing changes
			return;
		serverData.getServerClaimsManager().getClaimsManagerSynchronizer().fullClaimsSync(player, true);
	}

	public void updatePrimaryPartyColor(
			ServerPlayer player,
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
					>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
			> serverData
	){
		if(!ServerConfig.CONFIG.partyOwnedClaims.get())
			return;
		IPlayerConfig partyConfig = serverData.getPlayerConfigManager().getPartyOwnerConfig(player.getUUID());
		if(partyConfig == null)
			return;
		UUID partyOwner = partyConfig.getPlayerId();
		int currentConfigRawColorValue = partyConfig.getFromEffectiveConfig(PlayerConfigOptions.CLAIMS_COLOR);
		if(currentConfigRawColorValue != PlayerConfigOptions.CLAIMS_COLOR.getDefaultValue())//only the default value is replaced with a party color
			return;
		int cachedDefaultColor = partyConfig.getEffective(PlayerConfigOptions.CLAIMS_COLOR);
		int cachedPrimaryPartyColor = (cachedDefaultColor & ClaimsConstants.COLOR_IS_PARTY_FLAG) != 0 ?
				cachedDefaultColor & 0xFFFFFF : -1;
		int primaryPartyColor = serverData.getPlayerPartySystemManager().getPrimaryPartyColorByOwner(partyOwner);
		if(primaryPartyColor == cachedPrimaryPartyColor)
			return;
		partyConfig.resetAutomaticDefaultValue(PlayerConfigOptions.CLAIMS_COLOR);
	}

}
