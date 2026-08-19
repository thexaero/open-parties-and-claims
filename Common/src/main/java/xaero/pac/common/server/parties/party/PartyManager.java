/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObjectManager;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.io.ObjectManagerIOToSaveTracker;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.parties.party.sync.PartySynchronizer;
import xaero.pac.common.server.parties.party.task.PartyRemovalSpreadoutTask;
import xaero.pac.common.server.parties.system.impl.DefaultPlayerPartySystem;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.party.ServerPlayerPartyOnlineCounterUpdater;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public final class PartyManager implements IPartyManager<ServerParty>, ObjectManagerIOManager<ServerParty, PartyManager>, ObjectManagerIOExpirableObjectManager<ServerParty> {
	
	private final MinecraftServer server;
	private final PartySynchronizer partySynchronizer;
	private final ServerPlayerPartyOnlineCounterUpdater playerPartyOnlineCounterUpdater;
	private final Map<UUID, ServerParty> partiesByOwner;
	private final Map<UUID, ServerParty> partiesById;
	private final Map<UUID, ServerParty> partiesByMember;
	private final Map<UUID, Set<UUID>> partiesByAlly;
	private final LinkedChain<ServerParty> partyChain;
	private ObjectManagerIOToSaveTracker<ServerParty> toSave;
	private PartyManagerIO<?> io;
	private IPlayerConfigManager playerConfigs;
	private boolean loaded;
	private PartyExpirationHandler expirationHandler;
	private final ServerSpreadoutQueuedTaskHandler<PartyRemovalSpreadoutTask> partyRemovalTaskHandler;
	private DefaultPlayerPartySystem partySystem;
	
	private PartyManager(
			MinecraftServer server,
			PartySynchronizer partySynchronizer,
			ServerPlayerPartyOnlineCounterUpdater playerPartyOnlineCounterUpdater,
			Map<UUID, ServerParty> partiesByOwner,
			Map<UUID, ServerParty> partiesById,
			Map<UUID, ServerParty> partiesByMember,
			Map<UUID, Set<UUID>> partiesByAlly,
			LinkedChain<ServerParty> partyChain,
			ServerSpreadoutQueuedTaskHandler<PartyRemovalSpreadoutTask> partyRemovalTaskHandler
	) {
		super();
		this.server = server;
		this.partySynchronizer = partySynchronizer;
		this.playerPartyOnlineCounterUpdater = playerPartyOnlineCounterUpdater;
		this.partiesByOwner = partiesByOwner;
		this.partiesById = partiesById;
		this.partiesByMember = partiesByMember;
		this.partiesByAlly = partiesByAlly;
		this.partyChain = partyChain;
		this.partyRemovalTaskHandler = partyRemovalTaskHandler;
	}
	
	public void setExpirationHandler(PartyExpirationHandler expirationHandler) {
		this.expirationHandler = expirationHandler;
	}

	public boolean isLoaded() {
		return loaded;
	}
	
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	public MinecraftServer getServer() {
		return server;
	}
	
	public void setPlayerConfigs(IPlayerConfigManager playerConfigs) {
		this.playerConfigs = playerConfigs;
	}
	
	@Override
	public IPlayerConfigManager getPlayerConfigs() {
		return playerConfigs;
	}


	@Override
	public void setIo(ObjectManagerIO<?, ?, ServerParty, PartyManager> io) {
		this.io = (PartyManagerIO<?>) io;
		this.toSave = ObjectManagerIOToSaveTracker.Builder.<ServerParty>begin().setIo(io).build();
	}

	public void setPartySystem(DefaultPlayerPartySystem partySystem) {
		if(this.partySystem != null)
			throw new IllegalStateException();
		this.partySystem = partySystem;
	}

	@Override
	public DefaultPlayerPartySystem getPartySystem() {
		return partySystem;
	}

	@Nullable
	@Override
	public ServerParty getPartyByOwner(@Nonnull UUID owner) {
		return partiesByOwner.get(owner);
	}

	@Nullable
	@Override
	public ServerParty getPartyById(@Nonnull UUID id) {
		return partiesById.get(id);
	}

	@Nullable
	@Override
	public ServerParty getPartyByMember(@Nonnull UUID member) {
		ServerParty result = partiesByMember.get(member);
		if(result != null && result.isDestroyed())//party removal still in progress
			return null;
		return result;
	}
	
	private Set<UUID> getPartiesByAlly(UUID id){//a set of parties that ally the argument party
		return partiesByAlly.computeIfAbsent(id, i -> new HashSet<>());
	}

	public boolean isAlliedByAnyone(UUID id){
		return partiesByAlly.containsKey(id);
	}

	@Override
	public boolean partyExistsForOwner(@Nonnull UUID owner) {
		return getPartyByOwner(owner) != null;
	}

	@Nullable
	@Override
	public ServerParty createPartyForOwner(@Nonnull Player owner) {
		return createPartyForOwner(owner.getGameProfile());
	}

	@Nullable
	@Override
	public ServerParty createPartyForOwner(@Nonnull GameProfile ownerProfile) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return null;
		ServerParty existing = getPartyByOwner(ownerProfile.getId());
		if(existing != null)
			return null;
		UUID createdUUID;
		while(partiesById.containsKey(createdUUID = UUID.randomUUID()));//lol
		PartyMember ownerMember = new PartyMember(ownerProfile.getId(), true);
		ownerMember.setRank(PartyMemberRank.ADMIN);
		ownerMember.setUsername(ownerProfile.getName());
		ServerParty created = ServerParty.Builder.begin().setManagedBy(this).setOwner(ownerMember).setId(createdUUID).build();
		addParty(created);
		return created;
	}

	@Override
	public void removePartyByOwner(@Nonnull UUID owner) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		removeTypedParty(getPartyByOwner(owner));
	}

	@Override
	public void removePartyById(@Nonnull UUID id) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		removeTypedParty(getPartyById(id));
	}

	@Override
	public void removeTypedParty(@Nonnull ServerParty party) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		if(party == null)
			return;
		UUID partyOwnerId = party.getOwner().getUUID();
		if(getPartyByOwner(partyOwnerId) == party)//might not be true when there are inconsistencies in the saved data
			partiesByOwner.remove(partyOwnerId);
		partiesById.remove(party.getId());
		partyChain.remove(party);
		party.getOnlineMemberStream().forEach(p -> onMemberRemoved(party, party.getMemberInfo(p.getUUID())));
		onMemberRemoved(party, party.getOwner());
		io.delete(party);
		toSave.remove(party);
		partyRemovalTaskHandler.addTask(new PartyRemovalSpreadoutTask(this, party), ServerData.from(server));
		if(loaded)
			partySynchronizer.resyncPartyNameForClaims(partyOwnerId);
	}

	public void addParty(ServerParty party) {
		party.setDirty(true);
		UUID partyOwnerId = party.getOwner().getUUID();
		ServerParty currentOwnerParty = getPartyByOwner(partyOwnerId);
		if(currentOwnerParty != null)
			removeTypedParty(currentOwnerParty);//it has a different ID and needs to be removed
		partiesByOwner.put(partyOwnerId, party);
		partiesById.put(party.getId(), party);
		party.getTypedMemberInfoStream().forEach(mi -> onMemberAdded(party, mi));
		party.getTypedAllyPartiesStream().forEach(ally -> onAllyAdded(party, ally.getPartyId()));
		partyChain.add(party);
		if(loaded)
			partySynchronizer.resyncPartyNameForClaims(partyOwnerId);
	}
	
	public void onAllyAdded(ServerParty party, UUID allyId) {
		if(loaded)
			getPartySynchronizer().syncToPartyAllyAdd(party, getPartyById(allyId));
		getPartiesByAlly(allyId).add(party.getId());
	}
	
	public void onAllyRemoved(ServerParty party, UUID allyId, boolean onPartyRemoval) {
		if(loaded)
			getPartySynchronizer().syncToPartyAllyRemove(party, allyId, onPartyRemoval);
		Set<UUID> alliers = getPartiesByAlly(allyId);
		alliers.remove(party.getId());
		if(alliers.isEmpty())
			partiesByAlly.remove(allyId);
	}

	public void onMemberAdded(ServerParty party, PartyMember m) {
		partiesByMember.put(m.getUUID(), party);
		if(loaded) {
			getPartySynchronizer().syncToMemberIncludingPrimarySwitch(m, party);
			ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(m.getUUID());
			if(onlinePlayer != null)
				playerPartyOnlineCounterUpdater.onAddedToDefaultParty(onlinePlayer, this, party);
		}
	}

	public void onMemberRemoved(ServerParty party, PartyMember m) {
		if(partiesByMember.get(m.getUUID()) == party) {//might not be true when there are inconsistencies in the saved data or during party spreadout removal
			partiesByMember.remove(m.getUUID());
			if (loaded) {
				getPartySynchronizer().syncToMemberIncludingPrimarySwitch(m, null);
				ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(m.getUUID());
				if(onlinePlayer != null)
					playerPartyOnlineCounterUpdater.onRemovedFromDefaultParty(onlinePlayer, this, party);
			}
		}
	}

	public void onOwnerChange(PartyMember oldOwner, PartyMember newOwner) {
		ServerParty party = partiesByOwner.remove(oldOwner.getUUID());
		partiesByOwner.put(newOwner.getUUID(), party);
		if (loaded)
			playerPartyOnlineCounterUpdater.onDefaultPartyOwnerChange(this, party, oldOwner.getUUID(), newOwner.getUUID());
	}

	@Override
	public ObjectManagerIOToSaveTracker<ServerParty> getToSave() {
		return toSave;
	}

	@Nonnull
	@Override
	public Stream<ServerParty> getTypedAllStream(){
		return partyChain.stream();
	}

	@Override
	public Iterator<ServerParty> getExpirationIterator(){
		return partyChain.iterator();
	}

	@Nonnull
	@Override
	public Stream<ServerParty> getTypedPartiesThatAlly(@Nonnull UUID allyId) {
		if(isAlliedByAnyone(allyId))
			return getPartiesByAlly(allyId).stream().map(this::getPartyById).filter(Objects::nonNull);
		return Stream.empty();
	}
	
	public void debug() {
		System.out.println("partiesByOwner");
		partiesByOwner.forEach((k, v) -> System.out.println(k + " -> " + v));
		System.out.println("");
		System.out.println("partiesById");
		partiesById.forEach((k, v) -> System.out.println(k + " -> " + v));
		System.out.println("");
		System.out.println("partiesByMember");
		partiesByMember.forEach((k, v) -> System.out.println(k + " -> " + v));
		System.out.println("");
	}

	@Override
	public PartySynchronizer getPartySynchronizer() {
		return partySynchronizer;
	}
	
	public PartyExpirationHandler getExpirationHandler() {
		return expirationHandler;
	}

	public static final class Builder {

		private MinecraftServer server;
		private ServerSpreadoutQueuedTaskHandler<PartyRemovalSpreadoutTask> partyRemovalTaskHandler;
		private ServerPlayerPartyOnlineCounterUpdater playerPartyOnlineCounterUpdater;
		
		public Builder setDefault() {
			setServer(null);
			setPartyRemovalTaskHandler(null);
			setPlayerPartyOnlineCounterUpdater(null);
			return this;
		}
		
		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setPartyRemovalTaskHandler(ServerSpreadoutQueuedTaskHandler<PartyRemovalSpreadoutTask> partyRemovalTaskHandler) {
			this.partyRemovalTaskHandler = partyRemovalTaskHandler;
			return this;
		}

		public Builder setPlayerPartyOnlineCounterUpdater(ServerPlayerPartyOnlineCounterUpdater playerClaimPartyForceloadUpdater) {
			this.playerPartyOnlineCounterUpdater = playerClaimPartyForceloadUpdater;
			return this;
		}

		public PartyManager build() {
			if(server == null || partyRemovalTaskHandler == null || playerPartyOnlineCounterUpdater == null)
				throw new IllegalStateException();

			PartySynchronizer partySynchronizer = PartySynchronizer.Builder.begin()
					.setServer(server)
					.build();
			PartyManager result = new PartyManager(
					server, partySynchronizer, playerPartyOnlineCounterUpdater,
					new HashMap<>(), new HashMap<>(), new HashMap<>(),
					new HashMap<>(), new LinkedChain<>(),
					partyRemovalTaskHandler
			);
			@SuppressWarnings("unchecked")
			IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> internalAPI = (IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>)(Object)result;
			result.setPartySystem(new DefaultPlayerPartySystem(internalAPI));
			partySynchronizer.setPartyManager(result);
			return result;
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
	}

}
