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

package xaero.pac.client.player.config.api;

import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import java.util.function.BiPredicate;

/**
 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
 */
@Deprecated
public interface IPlayerConfigOptionClientStorageAPI<T extends Comparable<T>> {

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the option spec that this storage holds the value for.
	 *
	 * @return the option spec of this storage, not null
	 */
	@Deprecated
	public IPlayerConfigOptionSpecAPI<T> getOption();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the option string ID.
	 *
	 * @return the option string ID, not null
	 */
	@Deprecated
	public String getId();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the default comment text for the option.
	 *
	 * @return the default comment text, not null
	 */
	@Deprecated
	public String getComment();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the translation key for the name of the option.
	 *
	 * @return translation key for the name of the option, not null
	 */
	@Deprecated
	public String getTranslation();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the translation key arguments for the name of this option.
	 *
	 * @return the translation key arguments, not null
	 */
	@Deprecated
	public Object[] getTranslationArgs();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the translation key for the comment of this option.
	 *
	 * @return the comment translation key, not null
	 */
	@Deprecated
	public String getCommentTranslation();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the translation key arguments for the comment of this option.
	 *
	 * @return the comment translation key arguments, not null
	 */
	@Deprecated
	public Object[] getCommentTranslationArgs();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the type of the option value that this storage holds.
	 *
	 * @return the type of the option value, not null
	 */
	@Deprecated
	public Class<T> getType();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the stored option value.
	 *
	 * @return the stored value
	 */
	@Deprecated
	public T getValue();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the option value validator that checks whether a certain value is valid for the option.
	 *
	 * @return the option value validator, not null
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getValidator();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Gets the text prefix for the option tooltip on the UI screens.
	 *
	 * @return the tooltip prefix, null if there is none
	 */
	@Deprecated
	public String getTooltipPrefix();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Checks whether this option is forced to its default player config value.
	 *
	 * @return true if the option value is defaulted, otherwise false
	 */
	@Deprecated
	public boolean isDefaulted();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead<p>
	 * Checks whether the local client player can edit this option's value.
	 *
	 * @return true if the option value is mutable, otherwise false
	 */
	@Deprecated
	public boolean isMutable();
	
}
