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

package xaero.pac.common.server.player.config.group;

import com.mojang.datafixers.util.Either;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.player.config.group.api.IServerPlayerConfigGroupManagerAPI;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.group.custom.api.ICustomPlayerConfigGroupAPI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface IServerPlayerConfigGroupManager extends IServerPlayerConfigGroupManagerAPI {

	Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addCustomInternal(String id);

	Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addCustomLimitedInternal(String id);

	@Override
	ICustomPlayerConfigGroup getCustom(@Nonnull String id);

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Either<ICustomPlayerConfigGroupAPI, PlayerConfigGroupActionError> addCustom(@Nonnull String id){
		return (Either<ICustomPlayerConfigGroupAPI, PlayerConfigGroupActionError>)(Object)addCustomInternal(id);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Either<ICustomPlayerConfigGroupAPI, PlayerConfigGroupActionError> addCustomLimited(@Nonnull String id){
		return (Either<ICustomPlayerConfigGroupAPI, PlayerConfigGroupActionError>)(Object)addCustomLimitedInternal(id);
	}

	@Nonnull
	@Override
	Optional<PlayerConfigGroupActionError> removeCustom(@Nonnull String id);

	@Override
	IPlayerConfigGroup getUnwrapped(@Nonnull String id);

	@Override
	IPlayerConfigGroup get(@Nonnull String id);

	@Override
	int getMaxGroups();

	@Override
	int getGroupSpace();

	@Nonnull
	@Override
	List<String> getAllIdsSorted();

}
