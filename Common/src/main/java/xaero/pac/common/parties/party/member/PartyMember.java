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

package xaero.pac.common.parties.party.member;

import xaero.pac.common.parties.party.PartyPlayerInfo;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PartyMember extends PartyPlayerInfo implements IPartyMember, Comparable<PartyMember> {
	
	private final static int OWNER_ORDINAL = PartyMemberRank.values().length;
	
	private PartyMemberRank rank;
	private final boolean owner;
	
	public PartyMember(UUID playerUUID, boolean owner) {
		super(playerUUID);
		this.rank = PartyMemberRank.MEMBER;
		this.owner = owner;
	}
	
	public void setRank(PartyMemberRank rank) {
		this.rank = rank;
	}

	@Override
	public boolean isOwner() {
		return owner;
	}

	@Nonnull
	@Override
	public PartyMemberRank getRank() {
		return rank;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s, %s]", getUUID(), getUsername(), getRank());
	}

	@Override
	public int compareTo(PartyMember o) {
		int thisRankNumber = owner ? OWNER_ORDINAL : rank.ordinal();
		int otherRankNumber = o.owner ? OWNER_ORDINAL : o.rank.ordinal();
		if(thisRankNumber != otherRankNumber)
			return thisRankNumber > otherRankNumber ? -1 : 1;
		return getUsername().compareTo(o.getUsername());
	}
	
}
