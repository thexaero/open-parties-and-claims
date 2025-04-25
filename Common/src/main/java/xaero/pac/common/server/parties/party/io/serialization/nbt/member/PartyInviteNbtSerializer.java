/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2025, Xaero <xaero1996@gmail.com> and contributors
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
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

public class PartyInviteNbtSerializer {
	
	public CompoundTag serialize(PartyInvite info) {
		CompoundTag nbt = new CompoundTag();
		XaeroNbtUtil.putUUID(nbt, "uuid", info.getUUID());
		nbt.putString("username", info.getUsername());
		return nbt;
	}
	
	public PartyInvite deserialize(CompoundTag nbt) {
		PartyInvite result = new PartyInvite(XaeroNbtUtil.getUUID(nbt, "uuid").orElse(null));
		result.setUsername(nbt.getStringOr("username", ""));
		return result;
	}

}
