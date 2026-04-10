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

package xaero.pac.common.server.player.config.api.v1;

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

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
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String getId();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String getShortenedId();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public List<String> getPath();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public Class<T> getType();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String getTranslation();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String[] getTranslationArgs();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String getCommentTranslation();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String[] getCommentTranslationArgs();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String getComment();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public T getDefaultValue();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getClientSideValidator();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public BiPredicate<IPlayerConfigAPI, T> getServerSideValidator();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public String getTooltipPrefix();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public Function<String, T> getCommandInputParser();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	public Function<T, Component> getCommandOutputWriter();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI}
	 */
	@Deprecated
	Predicate<PlayerConfigType> getConfigTypeFilter();

}
