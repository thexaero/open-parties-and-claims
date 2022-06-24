/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config;

import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.api.IPlayerConfigManagerAPI;
import xaero.pac.common.server.player.config.sync.IPlayerConfigSynchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IPlayerConfigManager
<
	P extends IServerParty<?, ?>
> extends IPlayerConfigManagerAPI {
	//internal API

	@Nonnull
	@Override
	public IPlayerConfig getLoadedConfig(@Nullable UUID id);
	@Nonnull
	@Override
	public IPlayerConfig getDefaultConfig();
	@Nonnull
	@Override
	public IPlayerConfig getWildernessConfig();
	@Nonnull
	@Override
	public IPlayerConfig getServerClaimConfig();
	@Nonnull
	@Override
	public IPlayerConfig getExpiredClaimConfig();
	public IPlayerConfigSynchronizer getSynchronizer();
	
}
