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

package xaero.pac.common.server.parties.party.io.serialization.snapshot;

import xaero.pac.common.parties.party.PartyPlayerInfo;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.io.serialization.data.SnapshotConverter;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.parties.party.io.serialization.snapshot.member.PartyMemberSnapshotConverter;
import xaero.pac.common.server.parties.party.io.serialization.snapshot.member.PartyPlayerInfoSnapshotConverter;

import java.util.UUID;

public class PartySnapshotConverter extends SnapshotConverter<PartySnapshot, String, ServerParty, PartyManager>{
	
	private final PartyMemberSnapshotConverter partyMemberSnapshotConverter;
	private final PartyPlayerInfoSnapshotConverter partyPlayerInfoSnapshotConverter;

	public PartySnapshotConverter(PartyMemberSnapshotConverter partyMemberSnapshotConverter,
			PartyPlayerInfoSnapshotConverter partyPlayerInfoSnapshotConverter) {
		super();
		this.partyMemberSnapshotConverter = partyMemberSnapshotConverter;
		this.partyPlayerInfoSnapshotConverter = partyPlayerInfoSnapshotConverter;
	}

	@Override
	public ServerParty convert(String id, PartyManager manager, PartySnapshot data) {
		ServerParty result = ServerParty.Builder.begin().setManagedBy(manager).setOwner(partyMemberSnapshotConverter.convert(data.getOwner(), true)).setId(UUID.fromString(id)).build();
		result.setLastConfirmedActivity(data.getLastConfirmedActivity());
		data.getInvitedPlayers().forEach(pi -> {
			PartyPlayerInfo ppi = partyPlayerInfoSnapshotConverter.convert(pi);
			result.invitePlayerClean(ppi.getUUID(), ppi.getUsername());
			});
		data.getAllyParties().forEach(a -> result.addAllyPartyClean(UUID.fromString(a)));
		data.getMembers().forEach(mi -> {
			PartyMember memberInfo = partyMemberSnapshotConverter.convert(mi, false);
			result.addMemberClean(memberInfo.getUUID(), memberInfo.getRank(), memberInfo.getUsername());
			});
		return result;
	}

	@Override
	public PartySnapshot convert(ServerParty party) {
		PartySnapshot result = new PartySnapshot(partyMemberSnapshotConverter.convert(party.getOwner()));
		result.setLastConfirmedActivity(party.getLastConfirmedActivity());
		party.getInvitedPlayersStream().forEach(p -> result.addInvitedPlayer(partyPlayerInfoSnapshotConverter.convert((PartyPlayerInfo) p)));
		party.getAllyPartiesStream().forEach(a -> result.addAllyParty(a.toString()));
		party.getMemberInfoStream().filter(mi -> mi != party.getOwner()).forEach(mi -> result.addMember(partyMemberSnapshotConverter.convert((PartyMember) mi)));
		return result;
	}

}
