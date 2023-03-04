/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.party.api;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.parties.party.ally.api.IPartyAllyAPI;
import xaero.pac.common.parties.party.api.IPartyAPI;
import xaero.pac.common.parties.party.api.IPartyPlayerInfoAPI;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for a party on the server side
 */
public interface IServerPartyAPI<M extends IPartyMemberAPI, I extends IPartyPlayerInfoAPI, A extends IPartyAllyAPI> extends IPartyAPI<M, I, A> {

	@Override
	public int getMemberCount();

	@Override
	@Nullable
	public M getMemberInfo(@Nonnull UUID memberUUID);

	@Override
	public int getAllyCount();

	@Override
	public boolean isAlly(@Nonnull UUID partyId);

	@Override
	public int getInviteCount();

	@Override
	public boolean isInvited(@Nonnull UUID playerId);

	@Nonnull
	@Override
	public Stream<M> getMemberInfoStream();

	@Nonnull
	@Override
	public Stream<M> getStaffInfoStream();

	@Nonnull
	@Override
	public Stream<M> getNonStaffInfoStream();

	@Nonnull
	@Override
	public Stream<I> getInvitedPlayersStream();

	@Nonnull
	@Override
	public Stream<A> getAllyPartiesStream();

	@Nonnull
	@Override
	public M getOwner();

	@Nonnull
	@Override
	public UUID getId();

	@Nonnull
	@Override
	public String getDefaultName();
	
	@Override
	public boolean setRank(@Nonnull M member, @Nonnull PartyMemberRank rank);

	/**
	 * Adds a new party member with specified player UUID, rank and username.
	 *
	 * @param playerUUID  the UUID of a player, not null
	 * @param rank  the rank for the party member, null for the default rank
	 * @param playerUsername  the current username of the player, not null
	 * @return the created party member info, null if the player is already in a party
	 */
	@Nullable
	public M addMember(@Nonnull UUID playerUUID, @Nullable PartyMemberRank rank, @Nonnull String playerUsername);

	/**
	 * Removes the party member with a specified player UUID, unless the player is the owner of this party.
	 *
	 * @param playerUUID  the UUID of a player, not null
	 * @return the removed party member info, null if the specified player is the party owner
	 *         or if the player isn't in the party
	 */
	@Nullable
	public M removeMember(@Nonnull UUID playerUUID);

	/**
	 * Gets info about the party member with a specified username.
	 *
	 * @param username  the username of a party member, not null
	 * @return the member info, null if doesn't exist
	 */
	public M getMemberInfo(@Nonnull String username);

	/**
	 * Adds a new ally party to this party.
	 *
	 * @param partyId  the UUID of the party to ally, not null
	 */
	public void addAllyParty(@Nonnull UUID partyId);

	/**
	 * Removes an ally party from this party.
	 *
	 * @param partyId  the UUID of the party to unally, not null
	 */
	public void removeAllyParty(@Nonnull UUID partyId);

	/**
	 * Invites the player with specified player UUID and username to this party.
	 *
	 * @param playerUUID  the UUID of the player, not null
	 * @param playerUsername  the current username of the player, not null
	 * @return the invitation info, null if the player is already invited to this party
	 */
	@Nullable
	public I invitePlayer(@Nonnull UUID playerUUID, @Nonnull String playerUsername);

	/**
	 * Removes the player invitation for a specified player UUID.
	 *
	 * @param playerUUID  the UUID of the player, not null
	 * @return the removed invitation info, null if the player has not been invited
	 */
	@Nullable
	public I uninvitePlayer(@Nonnull UUID playerUUID);

	/**
	 * Gets a stream of all currently online members of this party.
	 *
	 * @return the stream of online party members, not null
	 */
	@Nonnull
	public Stream<ServerPlayer> getOnlineMemberStream();

}
