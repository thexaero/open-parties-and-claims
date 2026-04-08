/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.player.config.group.custom.api;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * API for a custom player config group data manager
 */
public interface ICustomPlayerConfigGroupDataManagerAPI {

	/**
	 * Gets the maximum number of groups that a non-op player can create in this group manager.
	 *
	 * @return the group count limit for non-op players
	 */
	int getMaxGroups();

	/**
	 * Gets the total group space available in this group manager for non-op players.
	 *
	 * @return the total group space available to non-op players
	 */
	int getGroupSpace();

	/**
	 * Gets a sorted list of all group IDs that can be used as config option exception values in this config.
	 * <p>
	 * This means that the list always includes player groups from the default player config.
	 *
	 * @return the list of all IDs usable as config option exception values
	 */
	@Nonnull
	List<String> getAllIdsSorted();

	/**
	 * Checks whether a specified group ID corresponds to one of the custom groups whose data is stored in this
	 * group manager.
	 *
	 * @param id  the group ID, not null
	 * @return true if this manager stores data for a custom group with the ID, otherwise false
	 */
	boolean dataExists(@Nonnull String id);

}
