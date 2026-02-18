/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.parties.party;

import xaero.pac.client.parties.party.api.IClientPartyAPI;
import xaero.pac.common.parties.party.IParty;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.ally.api.IPartyAllyAPI;
import xaero.pac.common.parties.party.api.IPartyPlayerInfoAPI;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public interface IClientParty
<
	M extends IPartyMember, 
	I extends IPartyPlayerInfo,
	A extends IPartyAlly
> extends IClientPartyAPI, IParty<M, I, A> {
	
	//internal api

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IPartyMemberAPI> getMemberInfoStream() {
		return (Stream<IPartyMemberAPI>)(Object)getTypedMemberInfoStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IPartyMemberAPI> getStaffInfoStream() {
		return (Stream<IPartyMemberAPI>)(Object)getTypedStaffInfoStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IPartyMemberAPI> getNonStaffInfoStream() {
		return (Stream<IPartyMemberAPI>)(Object)getTypedNonStaffInfoStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IPartyPlayerInfoAPI> getInvitedPlayersStream() {
		return (Stream<IPartyPlayerInfoAPI>)(Object)getTypedInvitedPlayersStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IPartyAllyAPI> getAllyPartiesStream() {
		return (Stream<IPartyAllyAPI>)(Object)getTypedAllyPartiesStream();
	}

	@Override
	@SuppressWarnings("unchecked")
	default boolean setRank(@Nonnull IPartyMemberAPI member, @Nonnull PartyMemberRank rank) {
		return setRankTyped((M)member, rank);
	}
	
}
