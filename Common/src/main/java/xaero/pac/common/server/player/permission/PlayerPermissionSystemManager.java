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

package xaero.pac.common.server.player.permission;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

import javax.annotation.Nonnull;
import java.util.Map;

public final class PlayerPermissionSystemManager implements IPlayerPermissionSystemManager {

	private final Map<String, IPlayerPermissionSystemAPI> systems;
	private IPlayerPermissionSystemAPI usedSystem;
	private boolean registeringAddons;

	private PlayerPermissionSystemManager(Map<String, IPlayerPermissionSystemAPI> systems){
		this.systems = systems;
	}

	@Override
	public void preRegister(){
		registeringAddons = true;
	}

	@Override
	public void register(@Nonnull String name, @Nonnull IPlayerPermissionSystemAPI system) {
		if(!registeringAddons)
			throw new IllegalStateException("You must register OPAC addons during the OPACAddonRegister event! (OPACAddonRegisterEvent on Forge)");
		if(systems.containsKey(name))
			throw new IllegalArgumentException("This permission system name is already registered!");
		systems.put(name, system);
		OpenPartiesAndClaims.LOGGER.info("Registered permission system for OPAC: {}", name);
	}

	@Override
	public void postRegister(){
		registeringAddons = false;
	}

	@Override
	public void updateUsedSystem(String configuredName) {
		if(configuredName.isEmpty()) {
			usedSystem = null;
			return;
		}
		usedSystem = systems.get(configuredName);
		if(usedSystem == null) {
			OpenPartiesAndClaims.LOGGER.warn("The configured permission system {} isn't registered!", configuredName);
			return;
		}
		OpenPartiesAndClaims.LOGGER.info("Configured OPAC to use the following permission system: {}", configuredName);
	}

	@Override
	public Iterable<String> getRegisteredNames() {
		return systems.keySet();
	}

	@Override
	public IPlayerPermissionSystemAPI getUsedSystem() {
		return usedSystem;
	}

	public static final class Builder {

		private final MapFactory mapFactory;

		private Builder(MapFactory mapFactory){
			this.mapFactory = mapFactory;
		}

		public Builder setDefault() {
			return this;
		}

		public PlayerPermissionSystemManager build(){
			if(mapFactory == null)
				throw new IllegalStateException();
			return new PlayerPermissionSystemManager(mapFactory.get());
		}

		public static Builder begin(MapFactory mapFactory){
			return new Builder(mapFactory).setDefault();
		}

	}

}
