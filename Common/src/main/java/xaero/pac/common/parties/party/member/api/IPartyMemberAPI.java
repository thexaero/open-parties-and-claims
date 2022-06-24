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

package xaero.pac.common.parties.party.member.api;

import xaero.pac.common.parties.party.api.IPartyPlayerInfoAPI;
import xaero.pac.common.parties.party.member.PartyMemberRank;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * API for info about a party member
 */
public interface IPartyMemberAPI extends IPartyPlayerInfoAPI {

	@Nonnull
	@Override
	public UUID getUUID();

	@Nonnull
	@Override
	public String getUsername();

	/**
	 * Gets the rank of this party member.
	 *
	 * @return the rank of this member, not null
	 */
	@Nonnull
	public PartyMemberRank getRank();

	/**
	 * Checks whether this party member is the owner of the party.
	 *
	 * @return true if this player is the owner of the party, otherwise false
	 */
	public boolean isOwner();

}
