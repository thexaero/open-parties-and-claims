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

package xaero.pac.common.server.parties.party.io.serialization.snapshot;

import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.io.serialization.data.SnapshotConverter;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.parties.party.io.serialization.snapshot.member.PartyInviteSnapshotConverter;
import xaero.pac.common.server.parties.party.io.serialization.snapshot.member.PartyMemberSnapshotConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartySnapshotConverter extends SnapshotConverter<PartySnapshot, String, ServerParty, PartyManager>{
	
	private final PartyMemberSnapshotConverter partyMemberSnapshotConverter;
	private final PartyInviteSnapshotConverter partyInviteSnapshotConverter;

	public PartySnapshotConverter(PartyMemberSnapshotConverter partyMemberSnapshotConverter,
			PartyInviteSnapshotConverter partyInviteSnapshotConverter) {
		super();
		this.partyMemberSnapshotConverter = partyMemberSnapshotConverter;
		this.partyInviteSnapshotConverter = partyInviteSnapshotConverter;
	}

	@Override
	public ServerParty convert(String id, PartyManager manager, PartySnapshot data) {
		PartyMember owner = partyMemberSnapshotConverter.convert(data.getOwner(), true);

		Map<UUID, PartyMember> members = new HashMap<>(32);
		Map<UUID, PartyInvite> invites = new HashMap<>(32);
		Map<UUID, PartyAlly> allies = new HashMap<>();
		data.getMembers().forEach(mi -> {
			PartyMember member = partyMemberSnapshotConverter.convert(mi, false);
			members.put(member.getUUID(), member);
		});
		data.getInvitedPlayers().forEach(pi -> {
			PartyInvite invite = partyInviteSnapshotConverter.convert(pi);
			invites.put(invite.getUUID(), invite);
		});
		data.getAllyParties().forEach(a -> {
			UUID ally = UUID.fromString(a);
			allies.put(ally, new PartyAlly(ally));
		});
		ServerParty result = ServerParty.Builder.begin().setManagedBy(manager).setOwner(owner).setId(UUID.fromString(id)).setMemberInfo(members).setInvitedPlayers(invites).setAllyParties(allies).build();
		result.setLastConfirmedActivity(data.getLastConfirmedActivity());
		return result;
	}

	@Override
	public PartySnapshot convert(ServerParty party) {
		PartySnapshot result = new PartySnapshot(partyMemberSnapshotConverter.convert(party.getOwner()));
		result.setLastConfirmedActivity(party.getLastConfirmedActivity());
		party.getInvitedPlayersStream().forEach(p -> result.addInvitedPlayer(partyInviteSnapshotConverter.convert(p)));
		party.getAllyPartiesStream().forEach(a -> result.addAllyParty(a.toString()));
		party.getMemberInfoStream().filter(mi -> mi != party.getOwner()).forEach(mi -> result.addMember(partyMemberSnapshotConverter.convert((PartyMember) mi)));
		return result;
	}

}
