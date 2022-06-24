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

package xaero.pac.common.server.parties.party.io.serialization.snapshot;

import xaero.pac.common.server.io.serialization.human.gson.GsonSnapshot;
import xaero.pac.common.server.parties.party.io.serialization.snapshot.member.PartyMemberSnapshot;
import xaero.pac.common.server.parties.party.io.serialization.snapshot.member.PartyPlayerInfoSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PartySnapshot implements GsonSnapshot {
	
	private final PartyMemberSnapshot owner;
	private final List<PartyMemberSnapshot> members;
	private final List<PartyPlayerInfoSnapshot> invitedPlayers;
	private final List<String> allyParties;
	private long lastConfirmedActivity;
	
	public PartySnapshot(PartyMemberSnapshot owner) {
		super();
		this.owner = owner;
		this.members = new ArrayList<>();
		this.invitedPlayers = new ArrayList<>();
		this.allyParties = new ArrayList<>();
	}
	
	public void addMember(PartyMemberSnapshot member) {
		members.add(member);
	}
	
	public void addInvitedPlayer(PartyPlayerInfoSnapshot player) {
		invitedPlayers.add(player);
	}
	
	public void addAllyParty(String ally) {
		allyParties.add(ally);
	}
	
	public Iterable<PartyMemberSnapshot> getMembers() {
		return members;
	}
	
	public Iterable<PartyPlayerInfoSnapshot> getInvitedPlayers(){
		return invitedPlayers;
	}
	
	public Iterable<String> getAllyParties(){
		return allyParties;
	}
	
	public PartyMemberSnapshot getOwner() {
		return owner;
	}
	
	public void setLastConfirmedActivity(long lastConfirmedActivity) {
		this.lastConfirmedActivity = lastConfirmedActivity;
	}
	
	public long getLastConfirmedActivity() {
		return lastConfirmedActivity;
	}

}
