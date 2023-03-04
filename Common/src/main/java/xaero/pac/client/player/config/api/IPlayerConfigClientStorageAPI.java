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

package xaero.pac.client.player.config.api;

import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for a player config storage on the client side
 */
public interface IPlayerConfigClientStorageAPI<OS extends IPlayerConfigStringableOptionClientStorageAPI<?>> {

	/**
	 * Gets the config option value storage for a specified config option in this config.
	 * <p>
	 * All player config option types are statically accessible in {@link PlayerConfigOptions}.
	 *
	 * @param option  the player config option, not null
	 * @return the value storage for the config option, not null
	 * @param <T>  the type of the option value
	 */
	@Nonnull
	public <T extends Comparable<T>> OS getOptionStorage(@Nonnull IPlayerConfigOptionSpecAPI<T> option);

	/**
	 * Gets the type {@link PlayerConfigType} of this config.
	 *
	 * @return the type of this config, not null
	 */
	@Nonnull
	public PlayerConfigType getType();

	/**
	 * Gets the UUID of the owner of this config.
	 *
	 * @return the UUID of the owner, null for the wilderness config
	 */
	@Nullable
	public UUID getOwner();

	/**
	 * Gets a stream of all config option value storages for this config.
	 *
	 * @return the {@link Stream} of all config option value storages, not null
	 */
	@Nonnull
	public Stream<OS> optionStream();

	/**
	 * Gets an unmodifiable list of all string IDs of this config's sub-configs.
	 *
	 * @return an unmodifiable {@code List<String>} of sub-config IDs
	 */
	@Nonnull
	public List<String> getSubConfigIds();

	/**
	 * Gets a sub-config of this config from a specified sub-config ID.
	 * <p>
	 * Gets this config, if the specified sub-config ID is "main".
	 *
	 * @param id  the string ID of the sub-config, not null
	 * @return the sub-config, null if it doesn't exist
	 */
	@Nullable
	public IPlayerConfigClientStorageAPI<OS> getSubConfig(@Nonnull String id);

	/**
	 * Gets a sub-config of this config from a specified sub-config ID.
	 * <p>
	 * Gets this config, if the specified sub-config ID is "main" or isn't used.
	 *
	 * @param id  the string ID of the sub-config, not null
	 * @return the effective sub-config, not null
	 */
	@Nonnull
	public IPlayerConfigClientStorageAPI<OS> getEffectiveSubConfig(@Nonnull String id);

	/**
	 * Checks whether a sub-config with a specified string ID exists.
	 * <p>
	 * Does not consider "main" a sub-config.
	 *
	 * @param id  the string ID of the sub-config, not null
	 * @return true, if the sub-config exists, otherwise false
	 */
	public boolean subConfigExists(@Nonnull String id);

	/**
	 * Gets the number of sub-configs that this config has.
	 *
	 * @return the number of sub-configs
	 */
	public int getSubCount();

	/**
	 * Gets a stream of all sub-configs of this player config.
	 * <p>
	 * This must not be a sub-config in itself.
	 *
	 * @return a stream of all sub-configs, not null
	 */
	@Nonnull
	public Stream<IPlayerConfigClientStorageAPI<OS>> getSubConfigAPIStream();

	/**
	 * Checks whether this player (sub-)config is in the process of being deleted.
	 * <p>
	 * This is typically only ever true for sub-configs.
	 *
	 * @return true if this player (sub-)config is in the process of being deleted, otherwise false
	 */
	public boolean isBeingDeleted();

	/**
	 * Gets the maximum number of sub-configs that this player config is allowed to have.
	 * <p>
	 * Returns 0 if this is a sub-config. Returns the maximum int value if this is a server claims config.
	 *
	 * @return the sub-config limit
	 */
	public int getSubConfigLimit();
	
}
