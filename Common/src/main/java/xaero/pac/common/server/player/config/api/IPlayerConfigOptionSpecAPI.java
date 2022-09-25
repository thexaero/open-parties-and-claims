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

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A player config option instance used for player config option representation in various API features.
 *
 * @param <T>  the type of values of this option
 */
public interface IPlayerConfigOptionSpecAPI<T extends Comparable<T>> {

	/**
	 * Gets the ID of this option.
	 * <p>
	 * Use {@link #getPath()} if you need it separated into elements.
	 *
	 * @return the string ID, not null
	 */
	@Nonnull
	public String getId();

	/**
	 * Gets the path of this option, which is just the ID from {@link #getId()} but separated into elements.
	 *
	 * @return the path, not null
	 */
	@Nonnull
	public List<String> getPath();

	/**
	 * Gets the type of values that this option can have.
	 *
	 * @return the type of values, not null
	 */
	@Nonnull
	public Class<T> getType();

	/**
	 * Gets the translation key for the name of this option.
	 *
	 * @return the translation key, not null
	 */
	@Nonnull
	public String getTranslation();

	/**
	 * Gets the default en_us comment for this option.
	 *
	 * @return the default comment, not null
	 */
	@Nonnull
	public String getComment();

	/**
	 * Gets the default value that this option is set to in configs.
	 *
	 * @return the default value, not null
	 */
	@Nonnull
	public T getDefaultValue();

	/**
	 * Gets the client-side validator for potential values of this option.
	 *
	 * @return the client-side value validator, not null
	 */
	@Nonnull
	public BiPredicate<IPlayerConfigClientStorageAPI<?>, T> getClientSideValidator();

	/**
	 * Gets the server-side validator for potential values of this option.
	 *
	 * @return the server-side value validator, not null
	 */
	@Nonnull
	public BiPredicate<IPlayerConfigAPI, T> getServerSideValidator();

	/**
	 * Gets the prefix applied to the tooltip of this option on the UI.
	 *
	 * @return the tooltip prefix, not null
	 */
	@Nonnull
	public String getTooltipPrefix();

	/**
	 * Gets the String->value parser of this option, mainly used for commands.
	 *
	 * @return the String->value parser, not null
	 */
	@Nonnull
	public Function<String, T> getCommandInputParser();

	/**
	 * Gets the value->String converter of this option, mainly used for commands.
	 *
	 * @return the value->String converter, not null
	 */
	@Nonnull
	public Function<T, Component> getCommandOutputWriter();

	/**
	 * Gets the config type filter of this option.
	 * <p>
	 * The filter allows this option to only appear and be configurable on some types of player configs
	 * (e.g. just the server claims config).
	 *
	 * @return the config type filter, not null
	 */
	@Nonnull
	Predicate<PlayerConfigType> getConfigTypeFilter();

}
