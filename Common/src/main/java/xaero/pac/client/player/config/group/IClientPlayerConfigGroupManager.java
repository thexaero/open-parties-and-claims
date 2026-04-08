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

package xaero.pac.client.player.config.group;

import xaero.pac.client.player.config.group.api.IClientPlayerConfigGroupManagerAPI;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IClientPlayerConfigGroupManager extends IClientPlayerConfigGroupManagerAPI {

	void setSyncInProgress(boolean syncInProgress);

	Optional<PlayerConfigGroupActionError> addCustom(String id);

	Optional<PlayerConfigGroupActionError> removeCustom(String id);

	boolean includeMemberInCustom(String groupId, UUID playerId, String playerName);

	boolean excludeMemberFromCustom(String groupId, UUID playerId, String playerName);

	boolean updateCustomMemberName(String groupId, UUID playerId, String playerName);

	boolean includeGroupInCustom(String groupId, String groupIdToInclude);

	boolean excludeGroupFromCustom(String groupId, String groupIdToInclude);

	void reset();

	void confirmDesyncFix();

	void onDesyncError(PlayerConfigGroupActionError error);

	void setLimits(int maxGroups, int maxGroupTotal);

	@Override
	boolean isSyncInProgress();

	@Override
	@Nonnull
	Set<String> getIds();

	@Override
	boolean dataExists(@Nonnull String id);

	@Nonnull
	@Override
	List<String> getAllIdsSorted();

	@Override
	int getMaxGroups();

	@Override
	int getGroupSpace();

}
