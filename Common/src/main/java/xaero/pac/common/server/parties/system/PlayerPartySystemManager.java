/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2026, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.network.chat.Component;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

import java.util.Map;
import java.util.UUID;

public final class PlayerPartySystemManager implements IPlayerPartySystemManager {

	private final Map<String, IPlayerPartySystemAPI<?>> systems;
	private final Map<IPlayerPartySystemAPI<?>, String> systemNames;
	private IPlayerPartySystemAPI<?> primarySystem;
	private IPlayerConfigManager configManager;
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
			OpenPartiesAndClaims.LOGGER.warn("The configured primary party system \"{}\" isn't registered!", configuredPrimarySystem);
			primarySystem = systems.get("default");
		}
		if(
				ServerConfig.CONFIG.partyOwnedClaims.get() &&
				primarySystem instanceof xaero.pac.common.server.parties.system.api.IPlayerPartySystemAPI
		)
			throw new IllegalArgumentException(
					"The configured primary party system " + configuredPrimarySystem +
					" is implemented by an outdated addon mod using deprecated OPAC API which doesn't " +
					"support party-owned claims which you have enabled! Either disable partyOwnedClaims or update the addon, " +
					"if that's an option."
			);
		OpenPartiesAndClaims.LOGGER.info("Configured OPAC to use the following party system as primary: {}", systemNames.get(primarySystem));
	}

	public void setConfigManager(IPlayerConfigManager configManager) {
		if(this.configManager != null)
			throw new IllegalStateException();
		this.configManager = configManager;
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
	public boolean isInAPrimaryParty(UUID playerId) {
		return getPrimaryPartyOwnerByMember(playerId) != null;
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

	@Override
	public UUID getPrimaryPartyOwnerByMember(UUID playerId) {
		return getPrimaryPartyOwnerByMemberHelper(getPrimarySystem(), playerId);
	}

	private <P> UUID getPrimaryPartyOwnerByMemberHelper(IPlayerPartySystemAPI<P> primarySystem, UUID playerId){
		P party = primarySystem.getPartyByMember(playerId);
		if(party == null)
			return null;
		UUID partyOwnerId = primarySystem.getOwner(party);
		if(partyOwnerId == null)
			return null;

		//avoiding creating backdoors to global configs, which could actually happen with FTB Teams where
		//the party owner UUID returned by the default self-parties is weirdly 0, the same as for the server claims config
		if(configManager.getLoadedConfig(partyOwnerId).getType() != PlayerConfigType.PLAYER)
			return null;

		return partyOwnerId;
	}

	@Override
	public Component getPrimaryPartyNameByOwner(UUID ownerId) {
		return getPrimaryPartyNameByOwnerHelper(getPrimarySystem(), ownerId);
	}

	private <P> Component getPrimaryPartyNameByOwnerHelper(IPlayerPartySystemAPI<P> primarySystem, UUID ownerId) {
		P party = primarySystem.getPartyByOwner(ownerId);
		if(party == null)
			return null;
		return primarySystem.getName(party);
	}

	@Override
	public boolean canEditPartyConfig(UUID playerId) {
		if(getPrimaryPartyOwnerByMember(playerId) == null)//called to absolutely ensure the backdoor fix (not necessary here atm)
			return false;
		return getPrimarySystem().canEditPartyConfig(playerId);
	}

	@Override
	public boolean canCreatePartyConfigGroups(UUID playerId) {
		if(getPrimaryPartyOwnerByMember(playerId) == null)//called to absolutely ensure the backdoor fix (not necessary here atm)
			return false;
		return getPrimarySystem().canCreatePartyConfigGroups(playerId);
	}

	@Override
	public boolean canIncludeGroupsInPartyConfigGroups(UUID playerId) {
		if(getPrimaryPartyOwnerByMember(playerId) == null)//called to absolutely ensure the backdoor fix (not necessary here atm)
			return false;
		return getPrimarySystem().canIncludeGroupsInPartyConfigGroups(playerId);
	}

	@Override
	public boolean canIncludePlayersInPartyConfigGroups(UUID playerId) {
		if(getPrimaryPartyOwnerByMember(playerId) == null)//called to absolutely ensure the backdoor fix (not necessary here atm)
			return false;
		return getPrimarySystem().canIncludePlayersInPartyConfigGroups(playerId);
	}

	@Override
	public boolean canPartyClaim(UUID playerId) {
		UUID primaryPartyOwner = getPrimaryPartyOwnerByMember(playerId);
		if(primaryPartyOwner == null)
			return false;
		IPlayerConfig partyOwnerConfig = configManager.getLoadedConfig(primaryPartyOwner);
		if(partyOwnerConfig.getEffective(PlayerConfigOptions.WHOLE_PARTY_CAN_CLAIM))
			return true;
		return getPrimarySystem().isPermittedToPartyClaim(playerId);
	}

	@Override
	public boolean isPrimaryPartyOwner(UUID playerId) {
		return getPrimarySystem().getPartyByOwner(playerId) != null;
	}

	@Override
	public int getPrimaryMemberCount(UUID ownerId) {
		return getPrimaryMemberCountHelper(getPrimarySystem(), ownerId);
	}

	private <P> int getPrimaryMemberCountHelper(IPlayerPartySystemAPI<P> primarySystem, UUID ownerId) {
		P party = primarySystem.getPartyByOwner(ownerId);
		if(party == null)
			return 0;
		return primarySystem.getMemberCount(party);
	}

	@Override
	public int getPrimaryPartyColorByOwner(UUID ownerId) {
		return getPrimaryPartyColorByOwnerHelper(getPrimarySystem(), ownerId);
	}

	private <P> int getPrimaryPartyColorByOwnerHelper(IPlayerPartySystemAPI<P> primarySystem, UUID ownerId) {
		P party = primarySystem.getPartyByOwner(ownerId);
		if(party == null)
			return -1;
		return primarySystem.getColor(party);
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

