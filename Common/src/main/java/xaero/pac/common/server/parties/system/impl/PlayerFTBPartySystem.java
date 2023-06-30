/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.system.impl;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamRank;
import xaero.pac.common.server.parties.system.api.IPlayerPartySystemAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerFTBPartySystem implements IPlayerPartySystemAPI<Team> {

	@Nullable
	@Override
	public Team getPartyByOwner(@Nonnull UUID playerId) {
		Team team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerId).orElse(null);
		if(team == null)
			return null;
		if(team.getOwner().equals(playerId))
			return team;
		return null;
	}

	@Nullable
	@Override
	public Team getPartyByMember(@Nonnull UUID playerId) {
		return FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerId).orElse(null);
	}

	@Override
	public boolean isPlayerAllying(@Nonnull UUID playerId, @Nonnull UUID potentialAllyPlayerId) {
		Team playerTeam = getPartyByMember(playerId);
		if(playerTeam == null)
			return false;
		Team potentialAllyPlayerTeam = getPartyByMember(potentialAllyPlayerId);
		return playerTeam != potentialAllyPlayerTeam && playerTeam.getRankForPlayer(potentialAllyPlayerId) == TeamRank.ALLY;
	}

	@Override
	public boolean isPermittedToPartyClaim(@Nonnull UUID playerId) {
		Team playerTeam = getPartyByMember(playerId);
		return playerTeam != null && playerTeam.getRankForPlayer(playerId).isOfficerOrBetter();
	}

}
