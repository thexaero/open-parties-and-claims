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

package xaero.pac.common.server.player.permission.api;

import javax.annotation.Nonnull;

/**
 * The API for registering player permission system implementations.
 * <p>
 * Player permission system implementations must be registered during the
 * xaero.pac.common.event.OPACAddonRegister.EVENT on Fabric or OPACAddonRegisterEvent on Forge.
 */
public interface IPlayerPermissionSystemRegisterAPI {

	/**
	 * Registers a player permission system implementation to be available to OPAC
	 * under a specified name.
	 * <p>
	 * The actual permission system used by the mod is configured in the main server config file
	 * with the "permissionSystem" option.
	 *
	 * @param name  the name to register the permission system under, not null
	 * @param system  the permission system implementation, not null
	 */
	void register(@Nonnull String name, @Nonnull IPlayerPermissionSystemAPI system);

}
