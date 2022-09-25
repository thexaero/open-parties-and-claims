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

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObjectManager;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.parties.party.sync.PartySynchronizer;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public final class PartyManager implements IPartyManager<ServerParty>, ObjectManagerIOManager<ServerParty, PartyManager>, ObjectManagerIOExpirableObjectManager<ServerParty> {
	
	private final MinecraftServer server;
	private final PartySynchronizer partySynchronizer;
	private final Map<UUID, ServerParty> partiesByOwner;
	private final Map<UUID, ServerParty> partiesById;
	private final Map<UUID, ServerParty> partiesByMember;
	private final Map<UUID, Set<UUID>> partiesByAlly;
	private final LinkedChain<ServerParty> partyChain;
	private final Set<ServerParty> toSave;
	private PartyManagerIO<?> io;
	private IPlayerConfigManager playerConfigs;
	private boolean loaded;
	private PartyExpirationHandler expirationHandler;
	
	private PartyManager(MinecraftServer server, PartySynchronizer partySynchronizer, Map<UUID, ServerParty> partiesByOwner, Map<UUID, ServerParty> partiesById,
						 Map<UUID, ServerParty> partiesByMember, Map<UUID, Set<UUID>> partiesByAlly, LinkedChain<ServerParty> partyChain, Set<ServerParty> toSave) {
		super();
		this.server = server;
		this.partySynchronizer = partySynchronizer;
		this.partiesByOwner = partiesByOwner;
		this.partiesById = partiesById;
		this.partiesByMember = partiesByMember;
		this.partiesByAlly = partiesByAlly;
		this.partyChain = partyChain;
		this.toSave = toSave;
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
	
	public void setIo(PartyManagerIO<?> io) {
		this.io = io;
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
		return partiesByMember.get(member);
	}
	
	private Set<UUID> getPartiesByAlly(UUID id){//a set of parties that ally the argument party
		return partiesByAlly.computeIfAbsent(id, i -> new HashSet<>());
	}

	@Override
	public boolean partyExistsForOwner(@Nonnull UUID owner) {
		return getPartyByOwner(owner) != null;
	}

	@Nullable
	@Override
	public ServerParty createPartyForOwner(@Nonnull Player owner) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return null;
		ServerParty existing = getPartyByOwner(owner.getUUID());
		if(existing != null)
			return null;
		UUID createdUUID;
		while(partiesById.containsKey(createdUUID = UUID.randomUUID()));//lol
		PartyMember ownerMember = new PartyMember(owner.getUUID(), true);
		ownerMember.setRank(PartyMemberRank.ADMIN);
		ownerMember.setUsername(owner.getGameProfile().getName());
		ServerParty created = ServerParty.Builder.begin().setManagedBy(this).setOwner(ownerMember).setId(createdUUID).build();
		addParty(created);
		return created;
	}

	@Override
	public void removePartyByOwner(@Nonnull UUID owner) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		removeParty(getPartyByOwner(owner));
	}

	@Override
	public void removePartyById(@Nonnull UUID id) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		removeParty(getPartyById(id));
	}

	@Override
	public void removeParty(@Nonnull ServerParty party) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		if(party == null)
			return;
		UUID partyOwnerId = party.getOwner().getUUID();
		if(getPartyByOwner(partyOwnerId) == party)//might not be true when there are inconsistencies in the saved data
			partiesByOwner.remove(partyOwnerId);
		partiesById.remove(party.getId());
		party.getMemberInfoStream().forEach(mi -> onMemberRemoved((ServerParty) party, mi));
		io.delete(party);
		toSave.remove(party);
		party.getAllyPartiesStream().forEach(allyId -> onAllyRemoved(party, allyId, true));

		//removing this party from ally lists
		Set<UUID> alliersCopy = new HashSet<>(getPartiesByAlly(party.getId()));
		alliersCopy.forEach(allierId -> {
			ServerParty allier = getPartyById(allierId);
			if(allier != null)
				allier.removeAllyParty(party.getId());
		});
		partyChain.remove(party);
	}

	public void addParty(ServerParty party) {
		party.setDirty(true);
		ServerParty currentOwnerParty = getPartyByOwner(party.getOwner().getUUID());
		if(currentOwnerParty != null)
			removeParty(currentOwnerParty);//it has a different ID and needs to be removed
		partiesByOwner.put(party.getOwner().getUUID(), (ServerParty) party);
		partiesById.put(party.getId(), (ServerParty) party);
		party.getMemberInfoStream().forEach(mi -> onMemberAdded((ServerParty) party, mi));
		party.getAllyPartiesStream().forEach(allyId -> onAllyAdded(party, allyId));
		partyChain.add(party);
	}
	
	public void onAllyAdded(ServerParty party, UUID allyId) {
		if(loaded)
			getPartySynchronizer().syncToPartyAllyAdd(party, getPartyById(allyId));
		getPartiesByAlly(allyId).add(party.getId());
	}
	
	public void onAllyRemoved(ServerParty party, UUID allyId, boolean onPartyRemoval) {
		if(loaded)
			getPartySynchronizer().syncToPartyAllyRemove(party, allyId, onPartyRemoval);
		getPartiesByAlly(allyId).remove(party.getId());
	}

	public void onMemberAdded(ServerParty party, PartyMember m) {
		partiesByMember.put(m.getUUID(), party);
		if(loaded)
			getPartySynchronizer().syncToMember(m, party);
	}

	public void onMemberRemoved(ServerParty party, PartyMember m) {
		if(getPartyByMember(m.getUUID()) == party)//might not be true when there are inconsistencies in the saved data
			partiesByMember.remove(m.getUUID());
		if(loaded)
			getPartySynchronizer().syncToMember(m, null);
	}

	@Override
	public Iterable<ServerParty> getToSave(){
		return toSave;
	}

	@Nonnull
	@Override
	public Stream<ServerParty> getAllStream(){
		return partyChain.stream();
	}

	@Override
	public Iterator<ServerParty> getExpirationIterator(){
		return partyChain.iterator();
	}

	@Nonnull
	@Override
	public Stream<ServerParty> getPartiesThatAlly(@Nonnull UUID allyId) {
		return getPartiesByAlly(allyId).stream().map(partiesById::get).filter(Objects::nonNull);
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
	public void addToSave(ServerParty object) {
		toSave.add(object);
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
		
		public Builder setDefault() {
			setServer(null);
			return this;
		}
		
		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}
		
		public PartyManager build() {
			if(server == null)
				throw new IllegalStateException();

			PartySynchronizer partySynchronizer = PartySynchronizer.Builder.begin()
					.setServer(server)
					.build();
			PartyManager result = new PartyManager(server, partySynchronizer, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new LinkedChain<>(), new HashSet<>());
			partySynchronizer.setPartyManager(result);
			return result;
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
