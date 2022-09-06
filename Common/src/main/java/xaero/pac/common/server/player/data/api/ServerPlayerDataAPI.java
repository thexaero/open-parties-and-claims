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

package xaero.pac.common.server.player.data.api;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;

import javax.annotation.Nonnull;

/**
 * API for data attached to a server player
 */
public abstract class ServerPlayerDataAPI {

	/**
	 * Checks if the player is using the claims admin mode.
	 *
	 * @return true if the player is in the claims admin mode, otherwise false
	 */
	public abstract boolean isClaimsAdminMode();

	/**
	 * Checks if the player is using the claims non-ally mode.
	 *
	 * @return true if the player is in the claims non-ally mode, otherwise false
	 */
	public abstract boolean isClaimsNonallyMode();

	/**
	 * Checks if the player is using the server claim mode.
	 *
	 * @return true if the player is in the server claim mode, otherwise false
	 */
	public abstract boolean isClaimsServerMode();

	/**
	 * Gets the player data for a specified logged in player.
	 *
	 * @param player  the player, not null
	 * @return the parties and claims player data for the player, not null
	 */
	@Nonnull
	public static ServerPlayerDataAPI from(@Nonnull ServerPlayer player) {
		ServerPlayerDataAPI result = ((IOpenPACServerPlayer)player).getXaero_OPAC_PlayerData();
		if(result == null) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
			((IOpenPACServerPlayer) player).setXaero_OPAC_PlayerData(result = new ServerPlayerData());
		}
		return result;
	}

}
