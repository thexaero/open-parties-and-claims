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

package xaero.pac.common.server.player.config.backwards.v1;

import xaero.pac.common.server.player.config.api.v1.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v1.IPlayerConfigManagerAPI;
import xaero.pac.common.server.player.config.api.v1.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v1.PlayerConfigOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

@Deprecated
public class CompatPlayerConfigManager implements IPlayerConfigManagerAPI {

	private final xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI realManager;

	public CompatPlayerConfigManager(xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI realManager) {
		this.realManager = realManager;
	}

	@Override
	public IPlayerConfigAPI getLoadedConfig(@Nullable UUID id) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig = realManager.getLoadedConfig(id);
		return new CompatPlayerConfig(realConfig);
	}

	@Override
	public IPlayerConfigAPI getDefaultConfig() {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig = realManager.getDefaultConfig();
		return new CompatPlayerConfig(realConfig);
	}

	@Override
	public IPlayerConfigAPI getWildernessConfig() {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig = realManager.getWildernessConfig();
		return new CompatPlayerConfig(realConfig);
	}

	@Override
	public IPlayerConfigAPI getServerClaimConfig() {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig = realManager.getServerClaimConfig();
		return new CompatPlayerConfig(realConfig);
	}

	@Override
	public IPlayerConfigAPI getExpiredClaimConfig() {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig = realManager.getExpiredClaimConfig();
		return new CompatPlayerConfig(realConfig);
	}

	@Deprecated
	@Override
	public Stream<IPlayerConfigOptionSpecAPI<?>> getAllOptionsStream() {
		return PlayerConfigOptions.OPTIONS.values().stream();
	}

	@Deprecated
	@Override
	public IPlayerConfigOptionSpecAPI<?> getOptionForId(@Nonnull String id) {
		return PlayerConfigOptions.OPTIONS.get(id);
	}

}
