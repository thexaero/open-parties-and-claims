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

package xaero.pac.common.parties.party.api;

import xaero.pac.common.parties.party.ally.api.IPartyAllyAPI;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for a party
 */
public interface IPartyAPI<M extends IPartyMemberAPI, I extends IPartyPlayerInfoAPI, A extends IPartyAllyAPI> {

	/**
	 * Gets the number of members in this party.
	 *
	 * @return the member count
	 */
	public int getMemberCount();

	/**
	 * Gets info about the party member with a specified UUID.
	 *
	 * @param memberUUID  the UUID of a party member, not null
	 * @return the member info, null if doesn't exist
	 */
	@Nullable
	public M getMemberInfo(@Nonnull UUID memberUUID);

	/**
	 * Gets the number of parties allied by this one.
	 *
	 * @return the ally count
	 */
	public int getAllyCount();

	/**
	 * Checks whether the party with a specified UUID is allied by this one.
	 *
	 * @param partyId the UUID of the party, not null
	 * @return true if the party is allied by this one, otherwise false
	 */
	public boolean isAlly(@Nonnull UUID partyId);

	/**
	 * Gets the number of active invitation to this party.
	 *
	 * @return the invite count
	 */
	public int getInviteCount();

	/**
	 * Checks whether the player with a specified UUID has an active invitation to this party.
	 *
	 * @param playerId the UUID of the player, not null
	 * @return true if there is a active invitation, otherwise false
	 */
	public boolean isInvited(@Nonnull  UUID playerId);

	/**
	 * Gets a stream of all member info for this party.
	 *
	 * @return a {@link Stream} of all member info, not null
	 */
	@Nonnull
	public Stream<M> getMemberInfoStream();

	/**
	 * Gets a stream of all member info for the staff members of this party.
	 *
	 * @return a {@link Stream} of all staff member info, not null
	 */
	@Nonnull
	public Stream<M> getStaffInfoStream();

	/**
	 * Gets a stream of all member info for the regular (non-staff) members of this party.
	 *
	 * @return a {@link Stream} of all regular member info, not null
	 */
	@Nonnull
	public Stream<M> getNonStaffInfoStream();

	/**
	 * Gets a stream of all active invitations for this party.
	 *
	 * @return a {@link Stream} of all active invitations, not null
	 */
	@Nonnull
	public Stream<I> getInvitedPlayersStream();

	/**
	 * Gets a stream of UUIDs of all parties allied by this party.
	 *
	 * @return a {@link Stream} of all allies, not null
	 */
	@Nonnull
	public Stream<A> getAllyPartiesStream();

	/**
	 * Gets the member info for the owner of this party.
	 *
	 * @return the party owner
	 */
	@Nonnull
	public M getOwner();

	/**
	 * Gets the UUID of this party.
	 *
	 * @return the UUID of this party, not null
	 */
	@Nonnull
	public UUID getId();

	/**
	 * Gets the default name of this party.
	 *
	 * @return the default name, not null
	 */
	@Nonnull
	public String getDefaultName();

	/**
	 * Sets the rank of a specified party member.
	 * <p>
	 * The member info needs to be from this party.
	 *
	 * @param member  the party member, not null
	 * @param rank  the rank to set the party member to, not null
	 * @return true if the rank is successfully set, otherwise false
	 */
	public boolean setRank(@Nonnull M member, @Nonnull PartyMemberRank rank);

}
