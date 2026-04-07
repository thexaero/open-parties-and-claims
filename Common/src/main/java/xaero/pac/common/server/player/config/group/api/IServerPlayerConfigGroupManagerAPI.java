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

package xaero.pac.common.server.player.config.group.api;

import com.mojang.datafixers.util.Either;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.api.ICustomPlayerConfigGroupDataManagerAPI;
import xaero.pac.common.server.player.config.group.custom.api.ICustomPlayerConfigGroupAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * API for player groups of a player config on the server side
 */
public interface IServerPlayerConfigGroupManagerAPI extends ICustomPlayerConfigGroupDataManagerAPI {

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
	 * Gets the API for the custom group with a specified ID in this group manager.
	 *
	 * @param id  the custom group ID, not null
	 * @return the API for the custom group, null if it doesn't exist
	 */
	@Nullable
	ICustomPlayerConfigGroupAPI getCustom(@Nonnull String id);

	/**
	 * Attempts to add a new custom group to this group manager with a specified ID.
	 * <p>
	 * Gets either the created custom group or an error.
	 *
	 * @param id  the ID for the new custom group, not null
	 * @return an {@link Either} instance with either the created group on the left or an error on the right
	 * {@link PlayerConfigGroupActionError}.
	 */
	@Nonnull
	Either<ICustomPlayerConfigGroupAPI, PlayerConfigGroupActionError> addCustom(@Nonnull String id);

	/**
	 * Attempts to add a new custom group to this group manager with a specified ID.
	 * <p>
	 * Unlike {@link #addCustom(String)}, this method takes more limitations into account, such as the group count limits.
	 * Gets either the created custom group or an error.
	 *
	 * @param id  the ID for the new custom group, not null
	 * @return an {@link Either} instance with either the created group on the left or an error on the right
	 * {@link PlayerConfigGroupActionError}.
	 */
	@Nonnull
	Either<ICustomPlayerConfigGroupAPI, PlayerConfigGroupActionError> addCustomLimited(@Nonnull String id);

	/**
	 * Attempts to remove a custom group from this group manager with a specified ID.
	 * <p>
	 * May return an error.
	 *
	 * @param id  the ID of the custom group to remove, not null
	 * @return an {@link Optional} instance which may contain an error {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Optional<PlayerConfigGroupActionError> removeCustom(@Nonnull String id);

	/**
	 * Gets the group with a specified ID without wrapping groups that originate from the default player config.
	 * <p>
	 * An unwrapped default player config player group is not aware of this group manager or config, so it won't support
	 * any default group extensions that the owner of this player config may have declared.
	 *
	 * @param id  the id of the group, not null
	 * @return the group corresponding to the id, null if it doesn't exist
	 */
	@Nullable
	IPlayerConfigGroupAPI getUnwrapped(@Nonnull String id);

	/**
	 * Gets the group with a specified ID and wraps the group if it originates from the default player config.
	 * <p>
	 * A wrapped default player config player group is aware of this group manager, so it supports
	 * the default group extensions that the owner of this player config may have declared.
	 *
	 * @param id  the id of the group, not null
	 * @return the group corresponding to the id, null if it doesn't exist
	 */
	@Nullable
	IPlayerConfigGroupAPI get(@Nonnull String id);

}
