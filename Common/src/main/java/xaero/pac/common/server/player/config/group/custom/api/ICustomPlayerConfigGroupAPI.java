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

package xaero.pac.common.server.player.config.group.custom.api;

import com.mojang.datafixers.util.Either;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.api.ICustomPlayerConfigGroupDataAPI;
import xaero.pac.common.player.config.group.custom.api.ICustomPlayerGroupMemberAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * API for a custom player config group
 */
public interface ICustomPlayerConfigGroupAPI extends ICustomPlayerConfigGroupDataAPI {

	@Override
	@Nonnull
	ICustomPlayerConfigGroupDataAPI copyData();

	@Override
	int getSize();

	@Override
	@Nonnull
	Set<ICustomPlayerGroupMemberAPI> getDirectMembers();

	@Override
	@Nonnull
	Set<String> getDirectGroupIds();

	@Override
	boolean playerIdIsIncluded(@Nullable UUID playerId);

	@Override
	boolean playerNameIsIncluded(@Nullable String name);

	@Override
	boolean groupIdIsIncluded(@Nullable String groupId);

	/**
	 * Attempts to include a member in this group using specified UUID or player name.
	 * <p>
	 * At least one of the player identified must be non-null.
	 *
	 * @param id  the UUID of the player to include, must not be null if the name is
	 * @param name  the name of the player to include, must not be null if the id is
	 * @return an {@link Either} instance with either the added member entry on the left or an error on the right
	 * {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Either<ICustomPlayerGroupMemberAPI, PlayerConfigGroupActionError> includeMember(@Nullable UUID id, @Nullable String name);

	/**
	 * Attempts to include a member in this group using specified UUID or player name.
	 * <p>
	 * Unlike {@link #includeMember(UUID, String)}, this method takes additional limitations into account, such as the
	 * player group space available for non-op players.
	 * <p>
	 * At least one of the player identified must be non-null.
	 *
	 * @param id  the UUID of the player to include, must not be null if the name is
	 * @param name  the name of the player to include, must not be null if the id is
	 * @return an {@link Either} instance with either the added member entry on the left or an error on the right
	 * {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Either<ICustomPlayerGroupMemberAPI, PlayerConfigGroupActionError> includeMemberLimited(@Nullable UUID id, @Nullable String name);

	/**
	 * Attempts to exclude a specified member entry from this group.
	 *
	 * @param member  the member entry, not null
	 * @return an {@link Optional} error {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Optional<PlayerConfigGroupActionError> excludeMember(@Nonnull ICustomPlayerGroupMemberAPI member);

	/**
	 * Attempts to exclude the member entry with a specified UUID and/or name from this group.
	 *
	 * @param id  the player UUID of the member entry, can be null
	 * @param name  the player name of the member entry, can be null
	 * @return an {@link Optional} error {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Optional<PlayerConfigGroupActionError> excludeMember(@Nullable UUID id, @Nullable String name);

	/**
	 * Attempts to include the group with a specified ID in this group.
	 *
	 * @param groupId  the ID of the group to include, not null
	 * @return an {@link Optional} error {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Optional<PlayerConfigGroupActionError> includeGroup(@Nonnull String groupId);

	/**
	 * Attempts to include the group with a specified ID in this group.
	 * <p>
	 * Unlike {@link #includeGroup(String)}, this method takes additional limitations into account, such as the
	 * player group space available for non-op players.
	 *
	 * @param groupId  the ID of the group to include, not null
	 * @return an {@link Optional} error {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Optional<PlayerConfigGroupActionError> includeGroupLimited(@Nonnull String groupId);

	/**
	 * Attempts to exclude the group with a specified ID from this group.
	 *
	 * @param groupId  the ID of the group to exclude, not null
	 * @return an {@link Optional} error {@link PlayerConfigGroupActionError}
	 */
	@Nonnull
	Optional<PlayerConfigGroupActionError> excludeGroup(@Nonnull String groupId);

}
