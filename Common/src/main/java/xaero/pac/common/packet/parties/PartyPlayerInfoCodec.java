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

package xaero.pac.common.packet.parties;

import net.minecraft.nbt.CompoundTag;
import xaero.pac.common.parties.party.PartyPlayerInfo;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;

import java.util.UUID;

public class PartyPlayerInfoCodec {
	
	public PartyPlayerInfo fromPlayerInfoTag(CompoundTag playerInfoTag) {
		if(playerInfoTag.isEmpty())
			return null;
		try {
			UUID playerUUID = playerInfoTag.getUUID("i");
			String username = playerInfoTag.getString("n");
			if(username.isEmpty() || username.length() > 128)
				return null;
			PartyPlayerInfo result = new PartyPlayerInfo(playerUUID);
			result.setUsername(username);
			return result;
		} catch(Throwable t) {
			return null;
		}
	}
	
	public PartyMember fromMemberTag(CompoundTag memberTag, boolean isOwner) {
		if(memberTag.isEmpty())
			return null;
		try {
			UUID playerUUID = memberTag.getUUID("i");
			String username = memberTag.getString("n");
			if(username.isEmpty() || username.length() > 128) {
				return null;
			}
			String rank = memberTag.getString("r");
			if(rank.isEmpty() || rank.length() > 128) {
				return null;
			}
			PartyMember result = new PartyMember(playerUUID, isOwner);
			result.setUsername(username);
			result.setRank(PartyMemberRank.valueOf(rank));
			return result;
		} catch(Throwable t) {
			return null;
		}
	}
	
	public CompoundTag toPlayerInfoTag(PartyPlayerInfo playerInfo) {
		CompoundTag infoTag = new CompoundTag();
		infoTag.putUUID("i", playerInfo.getUUID());
		infoTag.putString("n", playerInfo.getUsername());
		return infoTag;
	}
	
	public CompoundTag toMemberTag(PartyMember member) {
		CompoundTag memberTag = toPlayerInfoTag(member);
		memberTag.putString("r", member.getRank().toString());
		return memberTag;
	}

}
