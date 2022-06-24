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

package xaero.pac.client.parties.party.api;

import xaero.pac.common.parties.party.api.IPartyAPI;
import xaero.pac.common.parties.party.api.IPartyPlayerInfoAPI;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for a party on the client side
 */
public interface IClientPartyAPI<M extends IPartyMemberAPI, I extends IPartyPlayerInfoAPI> extends IPartyAPI<M, I> {

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
	public Stream<UUID> getAllyPartiesStream();

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
	
}
