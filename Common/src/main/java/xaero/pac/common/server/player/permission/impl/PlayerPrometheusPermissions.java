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

package xaero.pac.common.server.player.permission.impl;

import earth.terrarium.prometheus.api.roles.RoleApi;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.mods.prometheus.OPACOptions;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.OptionalInt;

public class PlayerPrometheusPermissions implements IPlayerPermissionSystemAPI {

	@Nonnull
	@Override
	public OptionalInt getIntPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Integer> node) {
		OPACOptions options = RoleApi.API.getOption(player, OPACOptions.SERIALIZER);
		Integer value = options == null ? null : options.getValue(node);
		return value == null ? OptionalInt.empty() : OptionalInt.of(value);
	}

	@Override
	public boolean getPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Boolean> node) {
		OPACOptions options = RoleApi.API.getOption(player, OPACOptions.SERIALIZER);
		Boolean value = options == null ? null : options.getValue(node);
		return value != null && value;
	}

	@Nonnull
	@Override
	public <T> Optional<T> getPermissionTyped(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<T> node) {
		OPACOptions options = RoleApi.API.getOption(player, OPACOptions.SERIALIZER);
		T value = options == null ? null : options.getValue(node);
		return Optional.ofNullable(value);
	}

}
