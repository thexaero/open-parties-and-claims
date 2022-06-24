/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.client.player.config.api;

import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for a player config storage on the client side
 */
public interface IPlayerConfigClientStorageAPI<OS extends IPlayerConfigStringableOptionClientStorageAPI<?>> {

	/**
	 * Gets the config option value storage for a specified config option in this config.
	 * <p>
	 * All player config option types are statically accessible in {@link PlayerConfig}.
	 *
	 * @param option  the player config option, not null
	 * @return the value storage for the config option, not null
	 * @param <T>  the type of the option value
	 */
	@Nonnull
	public <T> OS getOptionStorage(@Nonnull PlayerConfigOptionSpec<T> option);

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
	 * Gets the number of config option value storages.
	 *
	 * @return the number of config option value storages
	 */
	public int size();
	
}
