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

package xaero.pac.common.server.player.config.api.v2;

import xaero.pac.common.server.player.config.PlayerConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for the player config manager on the server side
 */
public interface IPlayerConfigManagerAPI {

	/**
	 * Gets or creates the player config for the player with a specified UUID.
	 * <p>
	 * Gets the wilderness config if the specified UUID is null,
	 * the server claims config if the UUID is {@link PlayerConfig#SERVER_CLAIM_UUID},
	 * the expired claims config if the UUID is {@link PlayerConfig#EXPIRED_CLAIM_UUID}.
	 *
	 * @param id  the UUID of the player, null for wilderness
	 * @return the player config instance, not null
	 */
	@Nonnull
	public IPlayerConfigAPI getLoadedConfig(@Nullable UUID id);

	/**
	 * Gets or creates the player config of the owner of the party that the player with
	 * a specified UUID is a member of.
	 * <p>
	 * The party owner's config is used as the config for the claims owned by the party when the server has party-owned
	 * claims enabled.
	 * <p>
	 * The party system used is the configured primary party system.
	 *
	 * @param memberId  the UUID of the player who is a member of the party, not null
	 * @return the party owner's player config, null if the player is not in a party
	 */
	@Nullable
	public IPlayerConfigAPI getPartyOwnerConfig(@Nonnull UUID memberId);

	/**
	 * Gets the default player config instance.
	 *
	 * @return the default player config instance, not null
	 */
	@Nonnull
	public IPlayerConfigAPI getDefaultConfig();

	/**
	 * Gets the wilderness config instance.
	 *
	 * @return the wilderness config instance, not null
	 */
	@Nonnull
	public IPlayerConfigAPI getWildernessConfig();

	/**
	 * Gets the server claims config instance.
	 *
	 * @return the server claims config instance, not null
	 */
	@Nonnull
	public IPlayerConfigAPI getServerClaimConfig();

	/**
	 * Gets the expired claims config instance.
	 *
	 * @return the expired claims config instance, not null
	 */
	@Nonnull
	public IPlayerConfigAPI getExpiredClaimConfig();

	/**
	 * Gets a stream of all player config option types, including the dynamic ones.
	 *
	 * @return a stream of all player config options, not null
	 */
	@Nonnull
	public Stream<IPlayerConfigOptionSpecAPI<?>> getAllOptionsStream();

	/**
	 * Gets the option type specification with a specified string option id, including dynamic options.
	 * <p>
	 * Returns null if no such option exists.
	 *
	 * @param id  the option id, not null
	 * @return the option type specification instance, null when doesn't exist
	 */
	@Nullable
	public IPlayerConfigOptionSpecAPI<?> getOptionForId(@Nonnull String id);
}
