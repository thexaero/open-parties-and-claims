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

package xaero.pac.client.player.config.api.v1;

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.api.v1.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigStringableOptionClientStorageAPI} instead
 */
@Deprecated
public interface IPlayerConfigStringableOptionClientStorageAPI<T extends Comparable<T>> extends IPlayerConfigOptionClientStorageAPI<T> {

	@Override
	@Nonnull
	public IPlayerConfigOptionSpecAPI<T> getOption();

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
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getValidator();

	@Nullable
	@Override
	public String getTooltipPrefix();

	@Override
	public boolean isDefaulted();

	@Override
	public boolean isMutable();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigStringableOptionClientStorageAPI} instead
	 */
	@Deprecated
	public Function<String, T> getCommandInputParser();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigStringableOptionClientStorageAPI} instead
	 */
	@Deprecated
	public Function<Object, Component> getCommandOutputWriterCast();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigStringableOptionClientStorageAPI} instead
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigClientStorageAPI, String> getStringValidator();
	
}
