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

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.api.IPlayerConfigGroupAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IPlayerConfigGroup extends IPlayerConfigGroupAPI {

	//The context config is not necessarily where this group originates from.
	//It's the config of the player whose groups are being checked, but there are groups that
	//players inherit from outside their own config too, such as built-in groups (party, allies)
	//and groups from the default player config.

	boolean isInGroup(IPlayerConfig contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId);

	default boolean isInGroup(IPlayerConfig contextConfig, @Nonnull UUID playerId){
		return isInGroup(contextConfig, null, playerId);
	}

	default boolean isInGroup(IPlayerConfig contextConfig, @Nonnull ServerPlayer player){
		return isInGroup(contextConfig, player, player.getUUID());
	}

	boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId);

	default boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nonnull UUID playerId){
		return isDirectlyInGroup(contextConfig, null, playerId);
	}

	default boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nonnull ServerPlayer player){
		return isDirectlyInGroup(contextConfig, player, player.getUUID());
	}

	@Override
	@Nonnull
	String getId();

	@Override
	default boolean supportsConfigType(@Nonnull PlayerConfigType type){
		return true;
	}

	@Override
	default boolean isInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId){
		return isInGroup((IPlayerConfig) contextConfig, player, playerId);
	}

	@Override
	default boolean isInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull UUID playerId){
		return isInGroup((IPlayerConfig) contextConfig, playerId);
	}

	@Override
	default boolean isInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull ServerPlayer player){
		return isInGroup((IPlayerConfig) contextConfig, player);
	}

	@Override
	default boolean isDirectlyInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId){
		return isDirectlyInGroup((IPlayerConfig) contextConfig, player, playerId);
	}

	@Override
	default boolean isDirectlyInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull UUID playerId){
		return isDirectlyInGroup((IPlayerConfig) contextConfig, playerId);
	}

	@Override
	default boolean isDirectlyInGroup(@Nonnull IPlayerConfigAPI contextConfig, @Nonnull ServerPlayer player){
		return isDirectlyInGroup((IPlayerConfig) contextConfig, player);
	}

}
