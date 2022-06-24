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

import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * API for a stringable player config option value storage on the client side
 */
public interface IPlayerConfigStringableOptionClientStorageAPI<T> extends IPlayerConfigOptionClientStorageAPI<T> {

	@Override
	@Nonnull
	public PlayerConfigOptionSpec<T> getOption();

	@Override
	@Nonnull
	public String getId();

	@Override
	@Nonnull
	public String getComment();

	@Override
	@Nonnull
	public String getTranslation();

	@Override
	@Nonnull
	public Class<T> getType();

	@Override
	@Nullable
	public T getValue();

	@Override
	@Nonnull
	public Predicate<T> getValidator();

	@Nullable
	@Override
	public String getTooltipPrefix();

	@Override
	public boolean isDefaulted();

	@Override
	public boolean isMutable();

	/**
	 * Gets the string input parser for this option.
	 * <p>
	 * It is the same one that is used for parsing command inputs.
	 *
	 * @return the string input parser function, not null
	 */
	@Nonnull
	public Function<String, T> getCommandInputParser();

	/**
	 * Gets the string output writer for this option.
	 * <p>
	 * It is the same one that is used for displaying option values in command outputs.
	 * <p>
	 * It accepts values of any type but will only work with the right one.
	 *
	 * @return the string output writer function, not null
	 */
	@Nonnull
	public Function<Object, String> getCommandOutputWriterCast();

	/**
	 * Gets the string value input validator for this option.
	 *
	 * @return the string value input validator function, not null
	 */
	@Nonnull
	public Predicate<String> getStringValidator();
	
}
