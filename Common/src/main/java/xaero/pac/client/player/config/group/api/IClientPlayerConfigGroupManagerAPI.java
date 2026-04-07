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

package xaero.pac.client.player.config.group.api;

import xaero.pac.common.player.config.group.custom.api.ICustomPlayerConfigGroupDataManagerAPI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * API for player groups of a player config on the client side
 */
public interface IClientPlayerConfigGroupManagerAPI extends ICustomPlayerConfigGroupDataManagerAPI {

	@Override
	int getMaxGroups();

	@Override
	int getGroupSpace();

	@Nonnull
	@Override
	List<String> getAllIdsSorted();

	@Override
	boolean dataExists(@Nonnull String id);

	/**
	 * Checks whether server-client synchronization of player groups is currently in progress.
	 *
	 * @return true if sync is in progress, otherwise false
	 */
	boolean isSyncInProgress();

	/**
	 * Gets a set of all custom group IDs the client is currently aware of for this config.
	 *
	 * @return the set of custom group IDs, not null
	 */
	@Nonnull
	Set<String> getIds();

}
