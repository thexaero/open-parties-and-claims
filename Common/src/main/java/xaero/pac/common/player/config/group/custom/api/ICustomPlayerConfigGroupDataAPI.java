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
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * API for stored data of a custom player config group
 */
public interface ICustomPlayerConfigGroupDataAPI {

	/**
	 * Gets a set containing all group IDs directly included in this group.
	 *
	 * @return the set of directly included group IDs, not null
	 */
	@Nonnull
	Set<String> getDirectGroupIds();

	/**
	 * Creates a copy of the stored data.
	 *
	 * @return the copy, not null
	 */
	@Nonnull
	ICustomPlayerConfigGroupDataAPI copyData();

	/**
	 * Gets the size of this custom player group (included members and groups).
	 *
	 * @return the size of the data
	 */
	int getSize();

	/**
	 * Gets a set of member entries directly included in this group.
	 *
	 * @return the set of direct member entries, not null
	 */
	@Nonnull
	Set<ICustomPlayerGroupMemberAPI> getDirectMembers();

	/**
	 * Checks whether the player with a specified UUID is one of the member entries.
	 *
	 * @param playerId  the ID of the player, not null
	 * @return true if one of the member entries corresponds to the ID, otherwise false
	 */
	boolean playerIdIsIncluded(@Nullable UUID playerId);

	/**
	 * Checks whether a specified player name is one of the member entries.
	 *
	 * @param name  the player name, can be null
	 * @return true if the specified player name is not null and corresponds to a member entry, otherwise false
	 */
	boolean playerNameIsIncluded(@Nullable String name);

	/**
	 * Checks whether a specified group ID is included in this group.
	 *
	 * @param groupId  the group ID, can be null
	 * @return true if the specified group ID is not null and corresponds to a group entry, otherwise false
	 */
	boolean groupIdIsIncluded(@Nullable String groupId);


}
