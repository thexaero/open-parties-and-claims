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

package xaero.pac.common.server.player.config.group.custom;

import com.mojang.datafixers.util.Either;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.ICustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.ICustomPlayerGroupMember;
import xaero.pac.common.player.config.group.custom.api.ICustomPlayerGroupMemberAPI;
import xaero.pac.common.server.player.config.group.IPlayerConfigParentGroup;
import xaero.pac.common.server.player.config.group.custom.api.ICustomPlayerConfigGroupAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ICustomPlayerConfigGroup extends IPlayerConfigParentGroup, ICustomPlayerConfigGroupData, ICustomPlayerConfigGroupAPI {

	@Override
	Set<ICustomPlayerGroupMember> getDirectMembersInternal();
	@Override
	ICustomPlayerConfigGroup lookupDefaultGroup(String id);

	Either<ICustomPlayerGroupMember, PlayerConfigGroupActionError> includeMemberInternal(@Nullable UUID id, @Nullable String name);
	Either<ICustomPlayerGroupMember, PlayerConfigGroupActionError> includeMemberLimitedInternal(@Nullable UUID id, @Nullable String name);
	Optional<PlayerConfigGroupActionError> excludeMemberInternal(ICustomPlayerGroupMember member);

	@Nonnull
	@Override
	default Set<ICustomPlayerGroupMemberAPI> getDirectMembers() {
		return ICustomPlayerConfigGroupData.super.getDirectMembers();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Either<ICustomPlayerGroupMemberAPI, PlayerConfigGroupActionError> includeMember(@Nullable UUID id, @Nullable String name){
		return (Either<ICustomPlayerGroupMemberAPI, PlayerConfigGroupActionError>)(Object)includeMemberInternal(id, name);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Either<ICustomPlayerGroupMemberAPI, PlayerConfigGroupActionError> includeMemberLimited(@Nullable UUID id, @Nullable String name){
		return (Either<ICustomPlayerGroupMemberAPI, PlayerConfigGroupActionError>)(Object)includeMemberLimitedInternal(id, name);
	}

	@Nonnull
	@Override
	default Optional<PlayerConfigGroupActionError> excludeMember(@Nonnull ICustomPlayerGroupMemberAPI member){
		return excludeMemberInternal((ICustomPlayerGroupMember) member);
	}

	@Nonnull
	@Override
	Optional<PlayerConfigGroupActionError> excludeMember(@Nullable UUID id, @Nullable String name);

	@Nonnull
	@Override
	Optional<PlayerConfigGroupActionError> includeGroup(@Nonnull String groupId);
	@Nonnull
	@Override
	Optional<PlayerConfigGroupActionError> includeGroupLimited(@Nonnull String groupId);
	@Nonnull
	@Override
	Optional<PlayerConfigGroupActionError> excludeGroup(@Nonnull String groupId);

	@Override
	boolean playerNameIsIncluded(@Nullable String name);
	@Override
	boolean groupIdIsIncluded(@Nullable String groupId);

}
