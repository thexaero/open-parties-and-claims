/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config.api;

import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for a player config on the server side
 */
public interface IPlayerConfigAPI {

	/**
	 * Gets the UUID of the player that this config is for.
	 *
	 * @return the UUID of the config owner, null if wilderness
	 */
	@Nullable
	public UUID getPlayerId();

	/**
	 * Tries to set the value of a specified config option to a specified value.
	 * <p>
	 * This won't succeed if the specified option is forced to the default config value (by the server mod config)
	 * or if the specified value isn't valid for the specified option.
	 * In any case, you will receive a {@link SetResult}.
	 * <p>
	 * All player config option types are statically accessible in {@link PlayerConfig}.
	 *
	 * @param option  the type of the option to set, not null
	 * @param value  the value to set the option to, not null
	 * @return the result type of this action
	 * @param <T>  the type of the option value
	 */
	@Nonnull
	public <T> SetResult tryToSet(@Nonnull PlayerConfigOptionSpec<T> option, @Nonnull T value);

	/**
	 * Gets the effective value of a config option.
	 * <p>
	 * This method calculates the automatic option value if such is used, e.g. the claims color,
	 * while {@link #getFromEffectiveConfig} just gets the effective raw value.
	 * <p>
	 * All player config option types are statically accessible in {@link PlayerConfig}.
	 *
	 * @param option  the type of the option, not null
	 * @return the effective value of the option, not null
	 * @param <T>  the type of the option value
	 */
	@Nonnull
	public <T> T getEffective(@Nonnull PlayerConfigOptionSpec<T> option);

	/**
	 * Gets the raw config value from the effective config of a config option, e.g. from the default
	 * player config for options that cannot be set per player.
	 * <p>
	 * This method does not calculate the automatic option value if such is used, e.g. the claims color.
	 * <p>
	 * All player config option types are statically accessible in {@link PlayerConfig}.
	 *
	 * @param option  the type of the option, not null
	 * @return the raw effective value of the option, not null
	 * @param <T>  the type of the option value
	 */
	@Nonnull
	public <T> T getFromEffectiveConfig(@Nonnull PlayerConfigOptionSpec<T> option);

	/**
	 * Gets the type {@link PlayerConfigType} of this config.
	 *
	 * @return the type of this config, not null
	 */
	@Nonnull
	public PlayerConfigType getType();

	/**
	 * All possible result types when trying to set an option value
	 */
	public static enum SetResult {
		/** The value is not valid for the option */
		INVALID,

		/** The option value was reset to the default */
		DEFAULTED,

		/** The value was successully set */
		SUCCESS;
	}
	
}
