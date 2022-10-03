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

package xaero.pac.common.server.parties.party.io.serialization.snapshot.member;

import xaero.pac.common.parties.party.member.PartyInvite;

import java.util.UUID;

public class PartyInviteSnapshotConverter {

	public PartyInvite convert(PartyInviteSnapshot data) {
		PartyInvite result = create(UUID.fromString(data.getUUID()));
		result.setUsername(data.getUsername());
		return result;
	}
	
	public PartyInviteSnapshot convert(PartyInvite partyPlayerInfo) {
		PartyInviteSnapshot result = new PartyInviteSnapshot(partyPlayerInfo.getUUID().toString(), partyPlayerInfo.getUsername());
		return result;
	}
	
	protected PartyInvite create(UUID playerId) {
		return new PartyInvite(playerId);
	}

}
