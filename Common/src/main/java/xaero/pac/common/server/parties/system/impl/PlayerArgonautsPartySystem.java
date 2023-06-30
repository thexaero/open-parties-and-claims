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

import earth.terrarium.argonauts.common.handlers.party.Party;
import earth.terrarium.argonauts.common.handlers.party.PartyHandler;
import earth.terrarium.argonauts.common.handlers.party.members.PartyMember;
import xaero.pac.common.server.parties.system.api.IPlayerPartySystemAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerArgonautsPartySystem implements IPlayerPartySystemAPI<Party> {

	@Nullable
	@Override
	public Party getPartyByOwner(@Nonnull UUID playerId) {
		Party party = PartyHandler.getPlayerParty(playerId);
		if(party == null)
			return null;
		if(!party.members().getLeader().profile().getId().equals(playerId))
			return null;
		return party;
	}

	@Nullable
	@Override
	public Party getPartyByMember(@Nonnull UUID playerId) {
		return PartyHandler.getPlayerParty(playerId);
	}

	@Override
	public boolean isPlayerAllying(@Nonnull UUID playerId, @Nonnull UUID potentialAllyPlayerId) {
		return false;//don't think allies exist here
	}

	@Override
	public boolean isPermittedToPartyClaim(@Nonnull UUID playerId) {
		Party party = getPartyByMember(playerId);
		if(party == null)
			return false;
		PartyMember member = party.members().get(playerId);
		return member != null && member.hasPermission("xaero.pac_party_claim");
	}

}
