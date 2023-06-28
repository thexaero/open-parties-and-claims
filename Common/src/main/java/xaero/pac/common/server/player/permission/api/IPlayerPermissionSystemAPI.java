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

package xaero.pac.common.server.player.permission.api;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import java.util.OptionalInt;

/**
 * The interface to be overridden by addons that wish to implement additional permission systems to be used
 * by Open Parties and Claims.
 * <p>
 * Player permission system implementations must be registered in {@link IPlayerPermissionSystemRegisterAPI}.
 */
public interface IPlayerPermissionSystemAPI {

	/**
	 * Gets the value of an integer permission for a specified player.
	 *
	 * @param player  the player, not null
	 * @param node  the node of the permission from {@link UsedPermissionNodes}, not null
	 * @return the OptionalInt for the int value of the permission, not null
	 */
	@Nonnull
	OptionalInt getIntPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Integer> node);

	/**
	 * Gets the value of a boolean permission for a specified player.
	 *
	 * @param player  the player, not null
	 * @param node  the node of the permission from {@link UsedPermissionNodes}, not null
	 * @return the boolean value of the permission, false if it doesn't exist
	 */
	boolean getPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Boolean> node);

}
