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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


/**
 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
 */
@Deprecated
public interface IPlayerConfigAPI {

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public UUID getPlayerId();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public <T extends Comparable<T>> SetResult tryToSet(@Nonnull IPlayerConfigOptionSpecAPI<T> option, @Nullable T value);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public <T extends Comparable<T>> T getEffective(@Nonnull IPlayerConfigOptionSpecAPI<T> option);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public <T extends Comparable<T>> T getFromEffectiveConfig(@Nonnull IPlayerConfigOptionSpecAPI<T> option);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public <T extends Comparable<T>> T getRaw(@Nonnull IPlayerConfigOptionSpecAPI<T> o);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public <T extends Comparable<T>> SetResult tryToReset(@Nonnull IPlayerConfigOptionSpecAPI<T> option);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public PlayerConfigType getType();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getSubConfig(@Nonnull String id);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getEffectiveSubConfig(@Nonnull String id);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getEffectiveSubConfig(int subIndex);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public boolean subConfigExists(@Nonnull String id);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public boolean subConfigExists(int subIndex);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getUsedSubConfig();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI getUsedServerSubConfig();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public IPlayerConfigAPI createSubConfig(@Nonnull String id);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public String getSubId();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public int getSubIndex();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public int getSubCount();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public List<String> getSubConfigIds();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public Stream<IPlayerConfigAPI> getSubConfigAPIStream();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public <T extends Comparable<T>> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T>option);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option);

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public boolean isBeingDeleted();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public int getSubConfigLimit();

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI}
	 */
	@Deprecated
	public static enum SetResult {
		/** The value is not valid for the option */
		INVALID,

		/** The option is not allowed in this config */
		ILLEGAL_OPTION,

		/** The option value was reset to the default config's value */
		DEFAULTED,

		/** The value was successully set */
		SUCCESS;

		public static SetResult fromReal(xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI.SetResult real){
			return switch(real){
				case ILLEGAL_OPTION -> ILLEGAL_OPTION;
				case DEFAULTED -> DEFAULTED;
				case SUCCESS -> SUCCESS;
				default -> INVALID;
			};
		}
	}
	
}
