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

package xaero.pac.common.server.parties.party;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.parties.party.Party;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObject;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.util.linked.ILinkedChainNode;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public final class ServerParty extends Party implements IServerParty<PartyMember, PartyInvite, PartyAlly>, ObjectManagerIOExpirableObject, ILinkedChainNode<ServerParty> {

	private final PartyManager managedBy;
	private boolean dirty;
	private long lastConfirmedActivity;
	private boolean hasBeenActive;
	private ServerParty nextInChain;
	private ServerParty prevInChain;
	private boolean destroyed;
	private final Map<String, PartyMember> memberInfoByUsername;
	private final Map<String, PartyInvite> inviteByUsername;
	private final Map<String, PartyAlly> allyByUsername;
	private final Map<UUID, String> usernameByAlly;//because the actual party might be deleted

	protected ServerParty(PartyManager managedBy, PartyMember owner, UUID id, List<PartyMember> staffInfo, Map<UUID, PartyMember> memberInfo,
						  LinkedChain<PartyMember> linkedMemberInfo, Map<UUID, PartyInvite> invitedPlayers, LinkedChain<PartyInvite> linkedInvitedPlayers, Map<UUID, PartyAlly> allyParties, LinkedChain<PartyAlly> linkedAllyParties, Map<String, PartyMember> memberInfoByUsername, Map<String, PartyInvite> inviteByUsername, Map<String, PartyAlly> allyByUsername, Map<UUID, String> usernameByAlly) {
		super(owner, id, staffInfo, memberInfo, linkedMemberInfo, invitedPlayers, linkedInvitedPlayers, allyParties, linkedAllyParties);
		this.managedBy = managedBy;
		this.memberInfoByUsername = memberInfoByUsername;
		this.inviteByUsername = inviteByUsername;
		this.allyByUsername = allyByUsername;
		this.usernameByAlly = usernameByAlly;
		confirmActivity(managedBy.getExpirationHandler().getServerInfo());
	}

	@Override
	public boolean changeOwner(UUID newOwnerId, String newOwnerUsername) {
		PartyMember oldOwner = getOwner();
		boolean result = super.changeOwner(newOwnerId, newOwnerUsername);
		if(result){
			memberInfoByUsername.put(oldOwner.getUsername().toLowerCase(), getMemberInfo(oldOwner.getUUID()));
			memberInfoByUsername.put(this.owner.getUsername().toLowerCase(), this.owner);
			if(managedBy != null) {
				managedBy.onOwnerChange(oldOwner, this.owner);
				if (managedBy.isLoaded()) {
					managedBy.getPartySynchronizer().syncToPartyUpdateOwner(this);
					IPlayerConfig newOwnerConfig = managedBy.getPlayerConfigs().getLoadedConfig(newOwnerId);
					managedBy.getPartySynchronizer().syncToPartyAndAlliersUpdateName(this, newOwnerConfig.getEffective(PlayerConfigOptions.PARTY_NAME));
				}
			}
			setDirty(true);
		}
		return result;
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
		String playerUsernameLowerCase = playerUsername.toLowerCase();
		PartyMember sameNameMember = memberInfoByUsername.get(playerUsernameLowerCase);
		if(sameNameMember != null)
			updateUsername(sameNameMember, sameNameMember.getUUID() + "");//the old member no longer has the name
		memberInfoByUsername.put(playerUsernameLowerCase, m);
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
		memberInfoByUsername.remove(m.getUsername().toLowerCase());
		setDirty(true);
		return m;
	}

	@Nullable
	@Override
	public PartyMember getMemberInfo(@Nonnull String username){
		return memberInfoByUsername.get(username.toLowerCase());
	}

	@Override
	public void addAllyParty(@Nonnull UUID partyId) {
		super.addAllyParty(partyId);
		setDirty(true);
	}
	
	public void addAllyPartyClean(UUID partyId) {
		super.addAllyPartyClean(partyId);
		if(managedBy.isLoaded())
			updateAllyNameMap(partyId, managedBy.getPartyById(partyId).getOwner().getUsername());
		managedBy.onAllyAdded(this, partyId);
	}

	@Override
	public void removeAllyParty(@Nonnull UUID partyId) {
		super.removeAllyParty(partyId);
		if(managedBy.isLoaded())
			updateAllyNameMap(partyId, null);
		setDirty(true);
		managedBy.onAllyRemoved(this, partyId, false);
	}

	public PartyAlly getAlly(String ownerUsername){
		return allyByUsername.get(ownerUsername.toLowerCase());
	}

	@Override
	public PartyInvite invitePlayer(@Nonnull UUID playerUUID, @Nonnull String playerUsername) {
		PartyInvite result = super.invitePlayer(playerUUID, playerUsername);
		if(result != null)
			setDirty(true);
		return result;
	}
	
	public PartyInvite invitePlayerClean(UUID playerUUID, String playerUsername) {
		PartyInvite playerInfo = super.invitePlayerClean(playerUUID, playerUsername);
		if(playerInfo == null)
			return null;
		String playerUsernameLowercase = playerUsername.toLowerCase();
		PartyInvite sameNameInvite = inviteByUsername.get(playerUsernameLowercase);
		if(sameNameInvite != null)
			sameNameInvite.setUsername(sameNameInvite.getUUID() + "");//this player no longer has the name
		inviteByUsername.put(playerUsernameLowercase, playerInfo);
		if(managedBy.isLoaded())
			managedBy.getPartySynchronizer().syncToPartyAddInvite(this, playerInfo);
		return playerInfo;
	}

	@Override
	public PartyInvite uninvitePlayer(@Nonnull UUID playerUUID) {
		PartyInvite playerInfo = super.uninvitePlayer(playerUUID);
		if(playerInfo == null)
			return null;
		setDirty(true);
		return playerInfo;
	}

	@Override
	protected PartyInvite removeInvitedPlayer(UUID playerId) {
		PartyInvite playerInfo = super.removeInvitedPlayer(playerId);
		if(playerInfo != null)
			inviteByUsername.remove(playerInfo.getUsername().toLowerCase());
		if(playerInfo != null && managedBy.isLoaded())
			managedBy.getPartySynchronizer().syncToPartyRemoveInvite(this, playerInfo);
		return playerInfo;
	}

	public PartyInvite getInvite(String username){
		return inviteByUsername.get(username.toLowerCase());
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
		String oldName = member.getUsername();
		if(Objects.equals(oldName, username) || getMemberInfo(member.getUUID()) != member)
			return false;
		member.setUsername(username);
		memberInfoByUsername.remove(oldName.toLowerCase());
		memberInfoByUsername.put(username.toLowerCase(), member);
		setDirty(true);
		if(managedBy.isLoaded()) {
			if (owner != member)
				managedBy.getPartySynchronizer().syncToPartyUpdateMember(this, member);
			else {
				UUID partyId = getId();
				managedBy.getPartiesThatAlly(partyId).forEach(allier -> allier.updateAllyNameMap(partyId, username));
				managedBy.getPartySynchronizer().syncToPartyUpdateOwner(this);
			}
		}
		return true;
	}

	public void updateAllyNameMap(UUID allyId, String username){
		String oldName = usernameByAlly.get(allyId);
		if(oldName != null)
			allyByUsername.remove(oldName);
		if(username == null){
			usernameByAlly.remove(allyId);
			return;
		}
		String usernameLowercase = username.toLowerCase();
		allyByUsername.put(usernameLowercase, getAlly(allyId));
		usernameByAlly.put(allyId, usernameLowercase);
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
		public Builder setInvitedPlayers(Map<UUID, PartyInvite> invitedPlayers) {
			super.setInvitedPlayers(invitedPlayers);
			return this;
		}

		@Override
		public Builder setAllyParties(Map<UUID, PartyAlly> allyParties) {
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
		protected ServerParty buildInternally(List<PartyMember> staffInfo, LinkedChain<PartyMember> linkedMemberInfo, LinkedChain<PartyInvite> linkedInvitedPlayers, LinkedChain<PartyAlly> linkedAllyParties) {
			Map<String, PartyMember> memberInfoByUsername = new HashMap<>();
			Map<String, PartyInvite> inviteByUsername = new HashMap<>();
			Map<String, PartyAlly> allyByUsername = new HashMap<>();
			Map<UUID, String> usernameByAlly = new HashMap<>();
			memberInfoByUsername.put(owner.getUsername().toLowerCase(), owner);
			memberInfo.values().forEach(m -> memberInfoByUsername.put(m.getUsername().toLowerCase(), m));
			invitedPlayers.values().forEach(i -> inviteByUsername.put(i.getUsername().toLowerCase(), i));
			return new ServerParty(managedBy, owner, id, staffInfo, memberInfo, linkedMemberInfo, invitedPlayers, linkedInvitedPlayers, allyParties, linkedAllyParties, memberInfoByUsername, inviteByUsername, allyByUsername, usernameByAlly);
		}
		
	}
	
}
