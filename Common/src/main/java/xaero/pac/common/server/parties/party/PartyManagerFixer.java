/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.parties.party;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.parties.party.member.PartyMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PartyManagerFixer {
	
	public void fix(PartyManager partyManager) {
		OpenPartiesAndClaims.LOGGER.info("Fixing party inconsistencies...");
		List<ServerParty> partiesToRemove = new ArrayList<>();
		partyManager.getAllStream().forEach(party -> {
			party.getMemberInfoStream().forEach(p -> fixPlayer((ServerParty)party, partyManager, (PartyMember) p, partiesToRemove));
		});
		partiesToRemove.forEach(party -> {
			partyManager.removeParty(party);
		});
	}
	
	private void fixPlayer(ServerParty fixingParty, PartyManager partyManager, PartyMember player, List<ServerParty> partiesToRemove) {
		UUID playerId = player.getUUID();
		ServerParty correctParty = partyManager.getPartyByMember(playerId);
		if(correctParty != fixingParty) {
			if(fixingParty.getOwner().getUUID().equals(playerId))
				partiesToRemove.add(fixingParty);
			else {
				fixingParty.removeMember(player.getUUID());//doesn't mess up client sync because nobody is online yet
			}
		}
	}

}
