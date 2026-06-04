/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.player.config.api;

/**
 * The API for the permissions a player has for a config
 */
public interface IPlayerConfigPermissionAPI {

	/**
	 * Checks whether the player can view the config.
	 * <p>
	 * This is not a security feature, it mostly disables the config button to keep the UI clean for normal players.
	 * <p>
	 * The config is still synchronized to every player and can be accessed programmatically.
	 *
	 * @return true if the player should be able to view the config, otherwise false
	 */
	boolean canView();

	/**
	 * Checks whether the player can edit the config.
	 *
	 * @return true if the player should be able to edit the config, otherwise false
	 */
	boolean canEdit();

	/**
	 * Checks whether the player can include/exclude player groups in other player groups of this config.
	 *
	 * @return true if the player should be able to include/exclude player groups in this config's player groups,
	 * otherwise false
	 */
	boolean canIncludeGroupsInGroups();

	/**
	 * Checks whether the player can include/exclude players in the player groups of this config.
	 *
	 * @return true if the player should be able to include/exclude players in this config's player groups,
	 * otherwise false
	 */
	boolean canIncludePlayersInGroups();

	/**
	 * Checks whether the player can create/delete player groups in this config.
	 *
	 * @return true if the player should be able to create/delete player groups in this config, otherwise false
	 */
	boolean canCreateGroups();

	/**
	 * Checks whether the player can claim chunks as this config.
	 *
	 * @return true if the player should be able to claim chunks as this config, otherwise false
	 */
	boolean canClaimAs();

}
