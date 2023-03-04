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

package xaero.pac.common.server.parties.party.io.serialization.nbt.member;

import net.minecraft.nbt.CompoundTag;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;

public class PartyMemberNbtSerializer {

	public CompoundTag serialize(PartyMember info) {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("uuid", info.getUUID());
		nbt.putString("username", info.getUsername());
		nbt.putString("rank", info.getRank().toString());
		return nbt;
	}
	
	public PartyMember deserialize(CompoundTag nbt, boolean isOwner) {
		PartyMember result = new PartyMember(nbt.getUUID("uuid"), isOwner);
		result.setUsername(nbt.getString("username"));
		result.setRank(PartyMemberRank.valueOf(nbt.getString("rank")));
		return result;
	}

}
