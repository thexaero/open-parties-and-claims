/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2026, Xaero <xaero1996@gmail.com> and contributors
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

import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamRank;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerFTBPartySystem implements IPlayerPartySystemAPI<Team> {

	@Nullable
	@Override
	public Team getPartyByOwner(@Nonnull UUID playerId) {
		Team team = FTBTeamsAPI.getPlayerTeam(playerId);
		if(team == null)
			return null;
		if(team.getOwner().equals(playerId))
			return team;
		return null;
	}

	@Nullable
	@Override
	public Team getPartyByMember(@Nonnull UUID playerId) {
		Team team = FTBTeamsAPI.getPlayerTeam(playerId);

		//NIL_UUID is used for FTB self-teams, which is also used for the Server Claims Config
		//if this is removed, nothing bad will happen, because of other checks, but it's best to catch it anyway
		//this also skips the config type check in PlayerPartySystemManager.getPrimaryPartyOwnerByMemberHelper,
		// so it's in theory good for performance too
		if(team == null || Util.NIL_UUID.equals(team.getOwner()))
			return null;

		return team;
	}

	@Override
	public boolean isPlayerAllying(@Nonnull UUID playerId, @Nonnull UUID potentialAllyPlayerId) {
		Team playerTeam = getPartyByMember(playerId);
		if(playerTeam == null)
			return false;
		Team potentialAllyPlayerTeam = getPartyByMember(potentialAllyPlayerId);
		return playerTeam != potentialAllyPlayerTeam && playerTeam.isAlly(potentialAllyPlayerId);
	}

	@Override
	public boolean isPermittedToPartyClaim(@Nonnull UUID playerId) {
		return isAtLeast(playerId, TeamRank.OFFICER);
	}

	@Override
	public boolean canCreatePartyConfigGroups(@Nonnull UUID playerId) {
		return isAtLeast(playerId, TeamRank.OWNER);
	}

	@Override
	public boolean canIncludePlayersInPartyConfigGroups(@Nonnull UUID playerId) {
		return isAtLeast(playerId, TeamRank.OFFICER);
	}

	@Override
	public boolean canIncludeGroupsInPartyConfigGroups(@Nonnull UUID playerId) {
		return isAtLeast(playerId, TeamRank.OWNER);
	}

	@Override
	public boolean canEditPartyConfig(@Nonnull UUID playerId) {
		return isAtLeast(playerId, TeamRank.OWNER);
	}

	private boolean isAtLeast(UUID playerId, TeamRank rank){
		Team playerTeam = getPartyByMember(playerId);
		return playerTeam != null && playerTeam.getHighestRank(playerId).ordinal() >= rank.ordinal();
	}

	@Nullable
	@Override
	public UUID getOwner(@Nonnull Team party) {
		return party.getOwner();
	}

	@Nullable
	@Override
	public Component getName(@Nonnull Team party) {
		return Component.literal(party.getDisplayName());
	}

	@Override
	public int getMemberCount(@Nonnull Team party) {
		return party.getMembers().size();
	}

}
