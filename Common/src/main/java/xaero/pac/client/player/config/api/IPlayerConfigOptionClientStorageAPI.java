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

package xaero.pac.client.player.config.api;

import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/**
 * API for a player config option value storage on the client side
 */
public interface IPlayerConfigOptionClientStorageAPI<T extends Comparable<T>> {

	/**
	 * Gets the option spec that this storage holds the value for.
	 *
	 * @return the option spec of this storage, not null
	 */
	@Nonnull
	public IPlayerConfigOptionSpecAPI<T> getOption();

	/**
	 * Gets the option string ID.
	 *
	 * @return the option string ID, not null
	 */
	@Nonnull
	public String getId();

	/**
	 * Gets the default comment text for the option.
	 *
	 * @return the default comment text, not null
	 */
	@Nonnull
	public String getComment();

	/**
	 * Gets the translation key for the name of the option.
	 *
	 * @return translation key for the name of the option, not null
	 */
	@Nonnull
	public String getTranslation();

	/**
	 * Gets the type of the option value that this storage holds.
	 *
	 * @return the type of the option value, not null
	 */
	@Nonnull
	public Class<T> getType();

	/**
	 * Gets the stored option value.
	 *
	 * @return the stored value
	 */
	@Nullable
	public T getValue();

	/**
	 * Gets the option value validator that checks whether a certain value is valid for the option.
	 *
	 * @return the option value validator, not null
	 */
	@Nonnull
	public BiPredicate<IPlayerConfigClientStorageAPI<?>, T> getValidator();

	/**
	 * Gets the text prefix for the option tooltip on the UI screens.
	 *
	 * @return the tooltip prefix, null if there is none
	 */
	@Nullable
	public String getTooltipPrefix();

	/**
	 * Checks whether this option is forced to its default player config value.
	 *
	 * @return true if the option value is defaulted, otherwise false
	 */
	public boolean isDefaulted();

	/**
	 * Checks whether the local client player can edit this option's value.
	 *
	 * @return true if the option value is mutable, otherwise false
	 */
	public boolean isMutable();
	
}
