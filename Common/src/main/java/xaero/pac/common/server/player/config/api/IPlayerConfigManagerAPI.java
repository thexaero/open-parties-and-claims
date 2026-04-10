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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
 */
@Deprecated
public interface IPlayerConfigManagerAPI {

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getLoadedConfig(@Nullable UUID id);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getDefaultConfig();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getWildernessConfig();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getServerClaimConfig();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getExpiredClaimConfig();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public Stream<IPlayerConfigOptionSpecAPI<?>> getAllOptionsStream();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI}
	 */
	@Deprecated
	public IPlayerConfigOptionSpecAPI<?> getOptionForId(@Nonnull String id);

}
