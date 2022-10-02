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

package xaero.pac.common.parties.party;

import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Predicate;

public class PartySearch {
	
	public 
	<
		M extends IPartyMember, I extends IPartyPlayerInfo, A extends IPartyAlly
	> IPartyPlayerInfo searchForPlayer(IServerParty<M, I, A> party, Predicate<IPartyPlayerInfo> isSearchedFor) {
		Iterator<M> membersIterator = party.getMemberInfoStream().iterator();
		while(membersIterator.hasNext()) {
			IPartyMember member = membersIterator.next();
			if(isSearchedFor.test(member))
				return member;
		}
		
		Iterator<I> invitedIterator = party.getInvitedPlayersStream().iterator();
		while(invitedIterator.hasNext()) {
			IPartyPlayerInfo invited = invitedIterator.next();
			if(isSearchedFor.test(invited))
				return invited;
		}
		return null;
	}
	
	public 
	<
		M extends IPartyMember, I extends IPartyPlayerInfo, A extends IPartyAlly, P extends IServerParty<M, I, A>
	> IServerParty<M, I, A> searchForAlly(IServerParty<M, I, A> party, IPartyManager<P> partyManager, Predicate<IServerParty<M, I, A>> isSearchedFor) {
		Iterator<A> allyIterator = party.getAllyPartiesStream().iterator();
		while(allyIterator.hasNext()) {
			UUID allyId = allyIterator.next().getPartyId();
			IServerParty<M, I, A> ally = partyManager.getPartyById(allyId);
			if(ally != null && isSearchedFor.test(ally))
				return ally;
		}
		return null;
	}

}
