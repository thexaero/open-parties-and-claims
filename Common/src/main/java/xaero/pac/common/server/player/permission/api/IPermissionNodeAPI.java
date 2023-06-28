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

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

/**
 * A representation of a player permission node.
 * <p>
 * All nodes used by the mod can be accessed in {@link UsedPermissionNodes}
 */
public interface IPermissionNodeAPI {

	/**
	 * Gets the default string representation of this node.
	 *
	 * @return default string representation of this node, not null
	 */
	@Nonnull
	String getDefaultNodeString();

	/**
	 * Gets the user-configured string representation of this node.
	 *
	 * @return the user-configured string representation of this node, not null
	 */
	@Nonnull
	String getNodeString();

	/**
	 * Gets the text component of the name of this node, to be used in UIs.
	 *
	 * @return the text component of the name, not null
	 */
	@Nonnull
	Component getName();

	/**
	 * Gets the text component of the comment/tooltip for this node, to be used in UIs.
	 *
	 * @return the text component of the comment/tooltip, not null
	 */
	@Nonnull
	Component getComment();

	/**
	 * Checks whether this node is for an int permission.
	 *
	 * @return true if this is an int permission node, otherwise false
	 */
	boolean isInt();

}
