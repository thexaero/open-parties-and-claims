/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
 */
@Deprecated
public interface IPlayerConfigOptionSpecAPI<T extends Comparable<T>> {

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the ID of this option.
	 * <p>
	 * Use {@link #getPath()} if you need it separated into elements.
	 *
	 * @return the string ID, not null
	 */
	@Deprecated
	public String getId();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the shortened ID of this option, without the "playerConfig." prefix.
	 *
	 * @return the shortened string ID, not null
	 */
	@Deprecated
	public String getShortenedId();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the path of this option, which is just the ID from {@link #getId()} but separated into elements.
	 *
	 * @return the path, not null
	 */
	@Deprecated
	public List<String> getPath();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the type of values that this option can have.
	 *
	 * @return the type of values, not null
	 */
	@Deprecated
	public Class<T> getType();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the translation key for the name of this option.
	 *
	 * @return the translation key, not null
	 */
	@Deprecated
	public String getTranslation();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the translation key arguments for the name of this option.
	 *
	 * @return the translation key arguments, not null
	 */
	@Deprecated
	public String[] getTranslationArgs();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the translation key for the comment of this option.
	 *
	 * @return the comment translation key, not null
	 */
	@Deprecated
	public String getCommentTranslation();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the translation key arguments for the comment of this option.
	 *
	 * @return the comment translation key arguments, not null
	 */
	@Deprecated
	public String[] getCommentTranslationArgs();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the default en_us comment for this option.
	 *
	 * @return the default comment, not null
	 */
	@Deprecated
	public String getComment();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the default value that this option is set to in configs.
	 *
	 * @return the default value, not null
	 */
	@Deprecated
	public T getDefaultValue();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the client-side validator for potential values of this option.
	 *
	 * @return the client-side value validator, not null
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getClientSideValidator();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the server-side validator for potential values of this option.
	 *
	 * @return the server-side value validator, not null
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigAPI, T> getServerSideValidator();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the prefix applied to the tooltip of this option on the UI.
	 *
	 * @return the tooltip prefix, null if no prefix
	 */
	@Deprecated
	public String getTooltipPrefix();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the String->value parser of this option, mainly used for commands.
	 *
	 * @return the String->value parser, not null
	 */
	@Deprecated
	public Function<String, T> getCommandInputParser();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the value->String converter of this option, mainly used for commands.
	 *
	 * @return the value->String converter, not null
	 */
	@Deprecated
	public Function<T, Component> getCommandOutputWriter();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}<p>
	 * Gets the config type filter of this option.
	 * <p>
	 * The filter allows this option to only appear and be configurable on some types of player configs
	 * (e.g. just the server claims config).
	 *
	 * @return the config type filter, not null
	 */
	@Deprecated
	Predicate<PlayerConfigType> getConfigTypeFilter();

}
