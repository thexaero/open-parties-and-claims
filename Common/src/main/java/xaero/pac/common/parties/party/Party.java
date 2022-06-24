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

import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public abstract class Party implements IParty<PartyMember, PartyPlayerInfo> {

	protected final PartyMember owner;
	private final UUID id;
	private final List<PartyMember> sortedStaffInfo;
	protected final Map<UUID, PartyMember> memberInfo;
	private final Map<UUID, PartyPlayerInfo> invitedPlayers;
	
	private final HashSet<UUID> allyParties;

	protected Party(PartyMember owner, UUID id, List<PartyMember> staffInfo, Map<UUID, PartyMember> memberInfo, Map<UUID, PartyPlayerInfo> invitedPlayers, HashSet<UUID> allyParties) {
		this.owner = owner;
		this.id = id;
		this.sortedStaffInfo = staffInfo;
		this.memberInfo = memberInfo;
		this.invitedPlayers = invitedPlayers;
		this.allyParties = allyParties;
	}

	@Override
	public PartyMember addMember(UUID memberUUID, PartyMemberRank rank, String playerUsername) {
		return addMemberClean(memberUUID, rank, playerUsername);
	}
	
	private boolean addStaff(PartyMember m) {
		int binarySearch = Collections.binarySearch(sortedStaffInfo, m);
		if(binarySearch < 0) {
			sortedStaffInfo.add(-binarySearch - 1, m);
			return true;
		}
		return false;
	}
	
	private boolean removeStaff(PartyMember m) {
		int binarySearch = Collections.binarySearch(sortedStaffInfo, m);
		if(binarySearch >= 0) {
			sortedStaffInfo.remove(binarySearch);
			return true;
		}
		return false;
	}
	
	public PartyMember addMemberClean(UUID memberUUID, PartyMemberRank rank, String playerUsername) {
		if(owner.getUUID().equals(memberUUID))
			return null;
		if(memberInfo.containsKey(memberUUID))
			return null;
		removeInvitedPlayer(memberUUID);
		PartyMember m = new PartyMember(memberUUID, false);
		if(rank == null)
			rank = PartyMemberRank.MEMBER;
		m.setRank(rank);
		m.setUsername(playerUsername);
		if(rank != PartyMemberRank.MEMBER) {
			if(!addStaff(m))
				return null;
		}
		memberInfo.put(memberUUID, m);
		return m;
	}

	@Override
	public PartyMember removeMember(UUID memberUUID) {//if this party is not managed yet, then managedBy should be null
		if(owner.getUUID().equals(memberUUID))
			return null;
		if(!memberInfo.containsKey(memberUUID))
			return null;
		PartyMember m = memberInfo.remove(memberUUID);
		if(m.getRank() != PartyMemberRank.MEMBER)
			removeStaff(m);
		return m;
	}

	@Override
	public PartyMember getMemberInfo(@Nonnull UUID memberUUID) {
		if(owner.getUUID().equals(memberUUID))
			return owner;
		return (PartyMember) memberInfo.get(memberUUID);
	}

	@Override
	public void addAllyParty(UUID partyId) {
		addAllyPartyClean(partyId);
	}
	
	public void addAllyPartyClean(UUID partyId) {
		allyParties.add(partyId);
	}

	@Override
	public void removeAllyParty(UUID partyId) {
		allyParties.remove(partyId);
	}

	@Override
	public boolean isAlly(@Nonnull UUID partyId) {
		return allyParties.contains(partyId);
	}

	@Override
	public PartyPlayerInfo invitePlayer(UUID playerUUID, String playerUsername) {
		return invitePlayerClean(playerUUID, playerUsername);
	}
	
	public PartyPlayerInfo invitePlayerClean(UUID playerUUID, String playerUsername) {
		if(isInvited(playerUUID))
			return null;
		PartyPlayerInfo playerInfo = new PartyPlayerInfo(playerUUID);
		playerInfo.setUsername(playerUsername);
		invitedPlayers.put(playerUUID, playerInfo);
		return playerInfo;
	}

	@Override
	public PartyPlayerInfo uninvitePlayer(UUID playerUUID) {
		if(!isInvited(playerUUID))
			return null;
		return removeInvitedPlayer(playerUUID);
	}

	@Override
	public boolean isInvited(@Nonnull UUID playerId) {
		return invitedPlayers.containsKey(playerId);
	}

	protected PartyPlayerInfo removeInvitedPlayer(UUID playerId) {
		return (PartyPlayerInfo) invitedPlayers.remove(playerId);
	}
	
	public Iterator<UUID> getAllyPartiesIteratorModifiable(){
		return allyParties.iterator();
	}

	@Nonnull
	@Override
	public Stream<PartyMember> getMemberInfoStream(){
		return Stream.concat(Stream.of(owner), memberInfo.values().stream());
	}
	
	@Nonnull
	public Stream<PartyMember> getStaffInfoStream(){
		return sortedStaffInfo.stream();
	}
	
	@Nonnull
	public Stream<PartyMember> getNonStaffInfoStream(){
		return memberInfo.values().stream().filter(mi -> mi.getRank() == PartyMemberRank.MEMBER);
	}

	@Nonnull
	@Override
	public Stream<PartyPlayerInfo> getInvitedPlayersStream() {
		return invitedPlayers.values().stream();
	}

	@Nonnull
	@Override
	public Stream<UUID> getAllyPartiesStream(){
		return allyParties.stream();
	}

	@Nonnull
	@Override
	public PartyMember getOwner() {
		return owner;
	}

	@Nonnull
	@Override
	public UUID getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return String.format("party(owner: %s, id: %s)", owner.getUsername(), id.toString());
	}

	@Nonnull
	@Override
	public String getDefaultName() {
		return String.format("%s's Party", owner.getUsername());
	}

	@Override
	public int getMemberCount() {
		return memberInfo.size() + 1 /*owner*/;
	}

	@Override
	public int getAllyCount() {
		return allyParties.size();
	}

	@Override
	public int getInviteCount() {
		return invitedPlayers.size();
	}

	@Override
	public boolean setRank(@Nonnull PartyMember member, @Nonnull PartyMemberRank rank) {
		if(member == null || memberInfo.get(member.getUUID()) != member)
			return false;
		if(member.getRank() != rank) {
			if(member.getRank() != PartyMemberRank.MEMBER)
				removeStaff(member);
			member.setRank(rank);
			//when changing from 1 staff rank to another (e.g. moderator to admin) should readd to staff list to fix the order
			if(rank != PartyMemberRank.MEMBER)
				addStaff(member);
		}
		return true;
	}
	
	public static abstract class Builder {
		
		protected PartyMember owner;
		protected UUID id;
		protected Map<UUID, PartyMember> memberInfo;
		protected Map<UUID, PartyPlayerInfo> invitedPlayers;
		protected HashSet<UUID> allyParties;
		
		protected Builder() {
		}
		
		public Builder setDefault() {
			setOwner(null);
			setId(null);
			setMemberInfo(null);
			setInvitedPlayers(null);
			setAllyParties(null);
			return this;
		}

		public Builder setOwner(PartyMember owner) {
			if(owner != null && (!owner.isOwner() || owner.getRank() != PartyMemberRank.ADMIN))
				throw new IllegalArgumentException();
			this.owner = owner;
			return this;
		}

		public Builder setId(UUID id) {
			this.id = id;
			return this;
		}

		public Builder setMemberInfo(Map<UUID, PartyMember> memberInfo) {
			this.memberInfo = memberInfo;
			return this;
		}

		public Builder setInvitedPlayers(Map<UUID, PartyPlayerInfo> invitedPlayers) {
			this.invitedPlayers = invitedPlayers;
			return this;
		}

		public Builder setAllyParties(HashSet<UUID> allyParties) {
			this.allyParties = allyParties;
			return this;
		}
		
		public Party build() {
			if(owner == null || id == null)
				throw new IllegalStateException();
			if(memberInfo == null)
				memberInfo = new HashMap<>();
			if(invitedPlayers == null)
				invitedPlayers = new HashMap<>();
			if(allyParties == null)
				allyParties = new HashSet<>();
			owner.setRank(PartyMemberRank.ADMIN);
			Party result = buildInternally(new ArrayList<>());
			memberInfo.forEach((id, m) -> {
				if(m.getRank() != PartyMemberRank.MEMBER)
					result.addStaff(m);
			});
			result.addStaff(owner);
			return result;
		}
		
		protected abstract Party buildInternally(List<PartyMember> staffInfo);
		
	}
	
}
