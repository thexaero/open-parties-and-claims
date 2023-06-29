/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

import me.lucko.fabric.api.permissions.v0.Options;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

public class FabricPermissionsSystem implements IPlayerPermissionSystemAPI {

	private static final Map<Class<?>, Function<String, ?>> PARSERS = Map.of(
			Integer.class, Integer::valueOf,
			Double.class, Double::valueOf,
			Float.class, Float::valueOf,
			Long.class, Long::valueOf,
			Short.class, Short::valueOf,
			Byte.class, Byte::valueOf,
			String.class, Function.identity(),
			Component.class, TextComponent::new
	);

	@Nonnull
	@Override
	public OptionalInt getIntPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Integer> node) {
		Integer parsedInteger = Options.get(player, node.getNodeString(), Integer::parseInt).orElse(null);
		if(parsedInteger == null)
			return OptionalInt.empty();
		return OptionalInt.of(parsedInteger);
	}

	@Override
	public boolean getPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Boolean> node) {
		return Permissions.check(player, node.getNodeString());
	}

	@Nonnull
	@Override
	public <T> Optional<T> getPermissionTyped(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<T> node) {
		@SuppressWarnings("unchecked")
		Function<String, T> parser = (Function<String, T>) PARSERS.get(node.getType());
		if(parser == null)
			return Optional.empty();
		T parsedValue = Options.get(player, node.getNodeString(), parser).orElse(null);
		return Optional.ofNullable(parsedValue);
	}

}
