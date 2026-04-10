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

package xaero.pac.common.server.player.config.group.api;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for a player group
 */
public interface IPlayerConfigGroupAPI {

	/**
	 * Gets the ID of this group.
	 *
	 * @return the ID of this group, not null
	 */
	@Nonnull
	String getId();

	/**
	 * Checks whether this player group supports a specified player config type.
	 * <p>
	 * For example, the built-in Party and Allies groups only support actual player configs, not global claim ones.
	 *
	 * @param type  the player config type, not null
	 * @return true if this group supports the config type, otherwise false
	 */
	boolean supportsConfigType(@Nonnull PlayerConfigType type);

	/**
	 * Checks whether the player with either a specified object or a UUID is in this group in a specified context.
	 * <p>
	 * The context config is not necessarily where this group originates from.
	 * It's the config of the player whose groups are being checked, but there are groups that
	 * players inherit from outside their own config too, such as built-in groups (party, allies)
	 * and groups from the default player config.
	 * <p>
	 * At least one of the player identifiers (player, playerId) must be non-null.
	 *
	 * @param contextConfig  the context of the group check, not null
	 * @param player  the player object to look up, must not be null if playerId is null
	 * @param playerId  the UUID of the player to look up, must not be null if player is null
	 * @return true if the specified player is in the group, otherwise false
	 */
	boolean isInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId);

	/**
	 * Checks whether the player with a specified UUID is in this group in a specified context.
	 * <p>
	 * The context config is not necessarily where this group originates from.
	 * It's the config of the player whose groups are being checked, but there are groups that
	 * players inherit from outside their own config too, such as built-in groups (party, allies)
	 * and groups from the default player config.
	 *
	 * @param contextConfig  the context of the group check, not null
	 * @param playerId  the UUID of the player to look up, not null
	 * @return true if the specified player is in the group, otherwise false
	 */
	boolean isInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull UUID playerId);

	/**
	 * Checks whether a server player is in this group in a specified context.
	 * <p>
	 * The context config is not necessarily where this group originates from.
	 * It's the config of the player whose groups are being checked, but there are groups that
	 * players inherit from outside their own config too, such as built-in groups (party, allies)
	 * and groups from the default player config.
	 *
	 * @param contextConfig  the context of the group check, not null
	 * @param player  the player object to look up, not null
	 * @return true if the specified player is in the group, otherwise false
	 */
	boolean isInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull ServerPlayer player);

	/**
	 * Checks whether the player with either a specified object or a UUID is directly in this group in a specified context.
	 * <p>
	 * The context config is not necessarily where this group originates from.
	 * It's the config of the player whose groups are being checked, but there are groups that
	 * players inherit from outside their own config too, such as built-in groups (party, allies)
	 * and groups from the default player config.
	 * <p>
	 * At least one of the player identifiers (player, playerId) must be non-null.
	 *
	 * @param contextConfig  the context of the group check, not null
	 * @param player  the player object to look up, must not be null if playerId is null
	 * @param playerId  the UUID of the player to look up, must not be null if player is null
	 * @return true if the specified player is directly in the group, otherwise false
	 */
	boolean isDirectlyInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId);

	/**
	 * Checks whether the player with a specified UUID is directly in this group in a specified context.
	 * <p>
	 * The context config is not necessarily where this group originates from.
	 * It's the config of the player whose groups are being checked, but there are groups that
	 * players inherit from outside their own config too, such as built-in groups (party, allies)
	 * and groups from the default player config.
	 *
	 * @param contextConfig  the context of the group check, not null
	 * @param playerId  the UUID of the player to look up, not null
	 * @return true if the specified player is directly in the group, otherwise false
	 */
	boolean isDirectlyInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull UUID playerId);

	/**
	 * Checks whether a server player is directly in this group in a specified context.
	 * <p>
	 * The context config is not necessarily where this group originates from.
	 * It's the config of the player whose groups are being checked, but there are groups that
	 * players inherit from outside their own config too, such as built-in groups (party, allies)
	 * and groups from the default player config.
	 *
	 * @param contextConfig  the context of the group check, not null
	 * @param player  the player object to look up, not null
	 * @return true if the specified player is directly in the group, otherwise false
	 */
	boolean isDirectlyInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull ServerPlayer player);

}
