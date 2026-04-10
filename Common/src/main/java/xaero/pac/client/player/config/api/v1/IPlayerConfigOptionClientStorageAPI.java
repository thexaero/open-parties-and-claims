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

import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.api.v1.IPlayerConfigOptionSpecAPI;

import java.util.function.BiPredicate;

/**
 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
 */
@Deprecated
public interface IPlayerConfigOptionClientStorageAPI<T extends Comparable<T>> {

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public IPlayerConfigOptionSpecAPI<T> getOption();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public String getId();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public String getComment();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public String getTranslation();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public Object[] getTranslationArgs();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public String getCommentTranslation();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public Object[] getCommentTranslationArgs();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public Class<T> getType();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public T getValue();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getValidator();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public String getTooltipPrefix();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public boolean isDefaulted();

	/**
	 * @deprecated use {@link xaero.pac.client.player.config.api.v2.IPlayerConfigOptionClientStorageAPI} instead
	 */
	@Deprecated
	public boolean isMutable();
	
}
