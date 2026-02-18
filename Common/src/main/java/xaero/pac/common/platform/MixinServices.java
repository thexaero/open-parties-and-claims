/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2025-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.platform;

import xaero.pac.common.platform.services.IPlatformMixinHelper;

import java.util.ServiceLoader;

/**
 * Separate from normal services to avoid loading classes too early that mixins may affect
 */
public class MixinServices {

	public static final IPlatformMixinHelper<?, ?> PLATFORM = load(IPlatformMixinHelper.class);

	public static <T> T load(Class<T> clazz) {

		final T loadedService = ServiceLoader.load(clazz)
				.findFirst()
				.orElseThrow(() -> new NullPointerException("Failed to load mixin service for " + clazz.getName()));
		//DO NOT USE OpenPartiesAndClaims.LOGGER here! OpenPartiesAndClaims will load classes too early.
		return loadedService;
	}
}
