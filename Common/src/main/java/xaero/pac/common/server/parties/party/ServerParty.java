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

package xaero.pac.common.server.parties.party;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.parties.party.Party;
import xaero.pac.common.parties.party.PartyPlayerInfo;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObject;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public final class ServerParty extends Party implements IServerParty<PartyMember, PartyPlayerInfo>, ObjectManagerIOExpirableObject, ILinkedChainNode<ServerParty> {

	private final PartyManager managedBy;
	private boolean dirty;
	private long lastConfirmedActivity;
	private boolean hasBeenActive;
	private ServerParty nextInChain;
	private ServerParty prevInChain;
	private boolean destroyed;

	protected ServerParty(PartyManager managedBy, PartyMember owner, UUID id, List<PartyMember> staffInfo, Map<UUID, PartyMember> memberInfo,
			Map<UUID, PartyPlayerInfo> invitedPlayers, HashSet<UUID> allyParties) {
		super(owner, id, staffInfo, memberInfo, invitedPlayers, allyParties);
		this.managedBy = managedBy;
		confirmActivity(managedBy.getExpirationHandler().getServerInfo());
	}

	@Override
	public PartyMember addMember(@Nonnull UUID memberUUID, @Nullable PartyMemberRank rank, @Nonnull String playerUsername) {//if this party is not managed yet, then managedBy should be null
		if(managedBy != null) {
			if(managedBy.getPartyByMember(memberUUID) != null)
				return null;
		}
		PartyMember m = super.addMember(memberUUID, rank, playerUsername);
		if(m == null)
			return null;
		setDirty(true);
		return m;
	}
	
	public PartyMember addMemberClean(UUID memberUUID, PartyMemberRank rank, String playerUsername) {//if this party is not managed yet, then managedBy should be null
		PartyMember m = super.addMemberClean(memberUUID, rank, playerUsername);
		if(m == null)
			return null;
		if(managedBy != null) {
			managedBy.onMemberAdded(this, m);
			if(managedBy.isLoaded())
				managedBy.getPartySynchronizer().syncToPartyAddMember(this, (PartyMember) m);
		}
		return m;
	}

	@Override
	public PartyMember removeMember(@Nonnull UUID memberUUID) {//if this party is not managed yet, then managedBy should be null
		PartyMember m = super.removeMember(memberUUID);
		if(m == null)
			return null;
		if(managedBy != null) {
			managedBy.onMemberRemoved(this, m);
			if(managedBy.isLoaded())
				managedBy.getPartySynchronizer().syncToPartyRemoveMember(this, m);
		}
		setDirty(true);
		return m;
	}

	@Override
	public void addAllyParty(@Nonnull UUID partyId) {
		super.addAllyParty(partyId);
		setDirty(true);
	}
	
	public void addAllyPartyClean(UUID partyId) {
		super.addAllyPartyClean(partyId);
		managedBy.onAllyAdded(this, partyId);
	}

	@Override
	public void removeAllyParty(@Nonnull UUID partyId) {
		super.removeAllyParty(partyId);
		setDirty(true);
		managedBy.onAllyRemoved(this, partyId, false);
	}

	@Override
	public PartyPlayerInfo invitePlayer(@Nonnull UUID playerUUID, @Nonnull String playerUsername) {
		PartyPlayerInfo result = super.invitePlayer(playerUUID, playerUsername);
		if(result != null)
			setDirty(true);
		return result;
	}
	
	public PartyPlayerInfo invitePlayerClean(UUID playerUUID, String playerUsername) {
		PartyPlayerInfo playerInfo = super.invitePlayerClean(playerUUID, playerUsername);
		if(playerInfo == null)
			return null;
		if(managedBy.isLoaded())
			managedBy.getPartySynchronizer().syncToPartyAddInvite(this, playerInfo);
		return playerInfo;
	}

	@Override
	public PartyPlayerInfo uninvitePlayer(@Nonnull UUID playerUUID) {
		PartyPlayerInfo playerInfo = super.uninvitePlayer(playerUUID);
		if(playerInfo == null)
			return null;
		setDirty(true);
		return playerInfo;
	}

	@Override
	protected PartyPlayerInfo removeInvitedPlayer(UUID playerId) {
		PartyPlayerInfo playerInfo = super.removeInvitedPlayer(playerId);
		if(playerInfo != null && managedBy.isLoaded())
			managedBy.getPartySynchronizer().syncToPartyRemoveInvite(this, playerInfo);
		return playerInfo;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		if(!this.dirty && dirty && managedBy != null)
			managedBy.addToSave(this);
		this.dirty = dirty;
	}

	@Override
	public String getFileName() {
		return getId().toString();
	}

	@Override
	public boolean setRank(@Nonnull PartyMember member, @Nonnull PartyMemberRank rank) {
		if(!super.setRank(member, rank))
			return false;
		setDirty(true);
		if(managedBy.isLoaded())
			managedBy.getPartySynchronizer().syncToPartyUpdateMember(this, member);
		return true;
	}

	@Override
	public boolean updateUsername(PartyMember member, String username) {
		if(Objects.equals(member.getUsername(), username) || memberInfo.get(member.getUUID()) != member && owner != member)
			return false;
		member.setUsername(username);
		setDirty(true);
		if(managedBy.isLoaded())
			if(owner != member)
				managedBy.getPartySynchronizer().syncToPartyUpdateMember(this, member);
			else
				managedBy.getPartySynchronizer().syncToPartyUpdateOwner(this);
		return true;
	}

	@Nonnull
	@Override
	public Stream<ServerPlayer> getOnlineMemberStream() {
		PlayerList playerList = managedBy.getServer().getPlayerList();
		//iterates over the smaller count
		if(playerList.getPlayerCount() > getMemberCount())
			return getMemberInfoStream().map(mi -> playerList.getPlayer(mi.getUUID())).filter(Objects::nonNull);
		else
			return playerList.getPlayers().stream().filter(p -> getMemberInfo(p.getUUID()) != null);
	}

	@Override
	public void confirmActivity(ServerInfo serverInfo) {
		lastConfirmedActivity = serverInfo.getUseTime();
		hasBeenActive = false;
	}
	
	public void setLastConfirmedActivity(long lastActiveTime) {
		this.lastConfirmedActivity = lastActiveTime;
	}
	
	public boolean hasBeenActive() {
		return hasBeenActive;
	}
	
	public void registerActivity() {
		hasBeenActive = true;
	}
	
	@Override
	public long getLastConfirmedActivity() {
		return lastConfirmedActivity;
	}

	@Override
	public void setNext(ServerParty element) {
		this.nextInChain = element;
	}

	@Override
	public void setPrevious(ServerParty element) {
		this.prevInChain = element;
	}

	@Override
	public ServerParty getNext() {
		return nextInChain;
	}

	@Override
	public ServerParty getPrevious() {
		return prevInChain;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void onDestroyed() {
		destroyed = true;
	}

	public static final class Builder extends Party.Builder {
		
		private boolean managed;
		private PartyManager managedBy;
		
		private Builder() {
			managed = true;
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setManaged(false);
			setManagedBy(null);
			return this;
		}
		
		public Builder setManaged(boolean managed) {
			this.managed = managed;
			return this;
		}
		
		public Builder setManagedBy(PartyManager managedBy) {
			this.managedBy = managedBy;
			return this;
		}

		@Override
		public Builder setOwner(PartyMember owner) {
			super.setOwner(owner);
			return this;
		}

		@Override
		public Builder setId(UUID id) {
			super.setId(id);
			return this;
		}

		@Override
		public Builder setMemberInfo(Map<UUID, PartyMember> memberInfo) {
			super.setMemberInfo(memberInfo);
			return this;
		}

		@Override
		public Builder setInvitedPlayers(Map<UUID, PartyPlayerInfo> invitedPlayers) {
			super.setInvitedPlayers(invitedPlayers);
			return this;
		}

		@Override
		public Builder setAllyParties(HashSet<UUID> allyParties) {
			super.setAllyParties(allyParties);
			return this;
		}

		@Override
		public ServerParty build() {
			if(managed && managedBy == null)
				throw new IllegalStateException();
			return (ServerParty) super.build();
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

		@Override
		protected ServerParty buildInternally(List<PartyMember> staffInfo) {
			return new ServerParty(managedBy, owner, id, staffInfo, memberInfo, invitedPlayers, allyParties);
		}
		
	}
	
}
