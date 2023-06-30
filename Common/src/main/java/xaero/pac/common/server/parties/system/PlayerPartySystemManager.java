/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.system;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.parties.system.api.IPlayerPartySystemAPI;

import java.util.Map;
import java.util.UUID;

public final class PlayerPartySystemManager implements IPlayerPartySystemManager {

	private final Map<String, IPlayerPartySystemAPI<?>> systems;
	private final Map<IPlayerPartySystemAPI<?>, String> systemNames;
	private IPlayerPartySystemAPI<?> primarySystem;
	private boolean registeringAddons;

	private PlayerPartySystemManager(Map<String, IPlayerPartySystemAPI<?>> systems, Map<IPlayerPartySystemAPI<?>, String> systemNames) {
		this.systems = systems;
		this.systemNames = systemNames;
	}

	@Override
	public void preRegister() {
		registeringAddons = true;
	}

	@Override
	public void register(String name, IPlayerPartySystemAPI<?> system) {
		if(!registeringAddons)
			throw new IllegalStateException("You must register OPAC addons during the OPACServerAddonRegister event! (OPACServerAddonRegisterEvent on Forge)");
		if(systems.containsKey(name))
			throw new IllegalArgumentException("This party system name is already registered!");
		systems.put(name, system);
		systemNames.put(system, name);
		OpenPartiesAndClaims.LOGGER.info("Registered party system for OPAC: {}", name);
	}

	@Override
	public void postRegister() {
		registeringAddons = false;
	}

	@Override
	public void updatePrimarySystem(String configuredPrimarySystem) {
		primarySystem = systems.get(configuredPrimarySystem);
		if(primarySystem == null){
			OpenPartiesAndClaims.LOGGER.error("The configured primary party system \"{}\" isn't registered!", configuredPrimarySystem);
			primarySystem = systems.get("default");
		}
		OpenPartiesAndClaims.LOGGER.info("Configured OPAC to use the following party system as primary: {}", systemNames.get(primarySystem));
	}

	@Override
	public IPlayerPartySystemAPI<?> getPrimarySystem() {
		return primarySystem;
	}

	@Override
	public Iterable<IPlayerPartySystemAPI<?>> getRegisteredSystems() {
		return systems.values();
	}

	@Override
	public boolean isInAParty(UUID playerId) {
		Iterable<IPlayerPartySystemAPI<?>> allSystems = getRegisteredSystems();
		for(IPlayerPartySystemAPI<?> partySystem : allSystems){
			if(partySystem.getPartyByMember(playerId) != null)
				return true;
		}
		return false;
	}

	@Override
	public boolean areInSameParty(UUID playerId, UUID otherPlayerId) {
		Iterable<IPlayerPartySystemAPI<?>> allSystems = getRegisteredSystems();
		for(IPlayerPartySystemAPI<?> partySystem : allSystems){
			Object playerParty = partySystem.getPartyByMember(playerId);
			if(playerParty != null && playerParty == partySystem.getPartyByMember(otherPlayerId))
				return true;
		}
		return false;
	}

	@Override
	public boolean isPlayerAllying(UUID playerId, UUID potentialAllyPlayerId) {
		Iterable<IPlayerPartySystemAPI<?>> allSystems = getRegisteredSystems();
		for(IPlayerPartySystemAPI<?> partySystem : allSystems){
			if(partySystem.isPlayerAllying(playerId, potentialAllyPlayerId))
				return true;
		}
		return false;
	}

	public static final class Builder {

		private final MapFactory mapFactory;

		private Builder(MapFactory mapFactory){
			this.mapFactory = mapFactory;
		}

		public PlayerPartySystemManager.Builder setDefault() {
			return this;
		}

		public PlayerPartySystemManager build(){
			if(mapFactory == null)
				throw new IllegalStateException();
			return new PlayerPartySystemManager(mapFactory.get(), mapFactory.get());
		}

		public static PlayerPartySystemManager.Builder begin(MapFactory mapFactory){
			return new PlayerPartySystemManager.Builder(mapFactory).setDefault();
		}

	}

}

