/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.client.gui.screens.Screen;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * API for the player config storage manager on the client side
 */
public interface IPlayerConfigClientStorageManagerAPI<CS extends IPlayerConfigClientStorageAPI<?>> {

	/**
	 * Gets the read-only "player config" storage for the server claims.
	 *
	 * @return the server claims config, not null
	 */
	@Nonnull
	public CS getServerClaimsConfig();

	/**
	 * Gets the read-only "player config" storage for the expired claims.
	 *
	 * @return the expired claims config, not null
	 */
	@Nonnull
	public CS getExpiredClaimsConfig();

	/**
	 * Gets the read-only "player config" storage for the wilderness.
	 *
	 * @return the wilderness config, not null
	 */
	@Nonnull
	public CS getWildernessConfig();

	/**
	 * Gets the read-only storage for the default player config.
	 *
	 * @return the default player config, not null
	 */
	@Nonnull
	public CS getDefaultPlayerConfig();

	/**
	 * Gets the read-only storage for the local client player's config.
	 *
	 * @return the local player's config, not null
	 */
	@Nonnull
	public CS getMyPlayerConfig();

	/**
	 * Opens the config GUI screen for the server claims "player config".
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 */
	public void openServerClaimsConfigScreen(@Nullable Screen escape, @Nullable Screen parent);

	/**
	 * Opens the config GUI screen for the expired claims "player config".
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 */
	public void openExpiredClaimsConfigScreen(@Nullable Screen escape, @Nullable Screen parent);

	/**
	 * Opens the config GUI screen for the wilderness "player config".
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 */
	public void openWildernessConfigScreen(@Nullable Screen escape, @Nullable Screen parent);

	/**
	 * Opens the config GUI screen for the default player config.
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 */
	public void openDefaultPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent);

	/**
	 * Opens the config GUI screen for the local client player's config.
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 */
	public void openMyPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent);

	/**
	 * Opens the config GUI screen for the player with a specified username.
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 * @param playerName  the username of the player, not null
	 */
	public void openOtherPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent, @Nonnull String playerName);

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
