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

import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import earth.terrarium.argonauts.api.party.Party;
import earth.terrarium.argonauts.api.party.PartyApi;
import earth.terrarium.argonauts.common.handlers.base.MemberPermissions;
import earth.terrarium.argonauts.common.handlers.base.members.Member;
import earth.terrarium.argonauts.common.handlers.party.members.PartyMember;
import net.minecraft.network.chat.Component;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerArgonautsPartySystem implements IPlayerPartySystemAPI<Party> {

	@Nullable
	@Override
	public Party getPartyByOwner(@Nonnull UUID playerId) {
		Party party = PartyApi.API.getPlayerParty(playerId);
		if(party == null)
			return null;
		if(!party.members().getLeader().profile().id().equals(playerId))
			return null;
		return party;
	}

	@Nullable
	@Override
	public Party getPartyByMember(@Nonnull UUID playerId) {
		return PartyApi.API.getPlayerParty(playerId);
	}

	@Override
	public boolean isPlayerAllying(@Nonnull UUID playerId, @Nonnull UUID potentialAllyPlayerId) {
		Party party = PartyApi.API.getPlayerParty(playerId);
		if(party == null)
			return false;
		return party.members().isAllied(potentialAllyPlayerId);
	}

	private boolean checkPartyPermission(@Nonnull UUID playerId, String permission){
		Party party = getPartyByMember(playerId);
		if(party == null)
			return false;
		PartyMember member = party.members().get(playerId);
		return member != null && member.hasPermission(permission);
	}

	@Override
	public boolean isPermittedToPartyClaim(@Nonnull UUID playerId) {
		Party guild = getPartyByMember(playerId);
		if(guild == null)
			return false;
		PartyMember member = guild.members().get(playerId);
		if(member == null)
			return false;
		return member.hasPermission(MemberPermissions.MANAGE_MEMBERS) ||
				member.hasPermission(MemberPermissions.MANAGE_PERMISSIONS) ||
				member.hasPermission(MemberPermissions.MANAGE_SETTINGS) ||
				member.hasPermission(MemberPermissions.MANAGE_ROLES);
	}

	@Override
	public boolean canEditPartyConfig(@Nonnull UUID playerId) {
		return checkPartyPermission(playerId, MemberPermissions.MANAGE_SETTINGS);
	}

	@Override
	public boolean canCreatePartyConfigGroups(@Nonnull UUID playerId) {
		return checkPartyPermission(playerId, MemberPermissions.MANAGE_SETTINGS);
	}

	@Override
	public boolean canIncludeGroupsInPartyConfigGroups(@Nonnull UUID playerId) {
		return checkPartyPermission(playerId, MemberPermissions.MANAGE_SETTINGS);
	}

	@Override
	public boolean canIncludePlayersInPartyConfigGroups(@Nonnull UUID playerId) {
		return checkPartyPermission(playerId, MemberPermissions.MANAGE_MEMBERS);
	}

	@Nullable
	@Override
	public UUID getOwner(@Nonnull Party party) {
		Member leader = party.members().getLeader();
		if(leader == null)
			return null;
		GameProfile leaderProfile = leader.profile();
		if(leaderProfile == null)
			return null;
		return leaderProfile.id();
	}

	@Nullable
	@Override
	public Component getName(@Nonnull Party party) {
		return party.displayName();
	}

	@Override
	public int getMemberCount(@Nonnull Party party) {
		//this sucks but there's no other way to only count actual members...
		return (int)Streams.stream(party.members().iterator()).count();
	}

}
