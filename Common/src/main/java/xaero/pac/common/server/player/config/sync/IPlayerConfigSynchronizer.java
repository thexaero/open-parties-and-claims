/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config.sync;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.data.config.PlayerConfigPermissionUpdateData;

public interface IPlayerConfigSynchronizer {

	//internal api

	public void syncAllToClient(ServerPlayer player);

	public void syncOnLogin(ServerPlayer player);

	public void sendSyncState(ServerPlayer player, IPlayerConfig config, boolean state);

	public void confirmSubConfigCreationSync(ServerPlayer player, IPlayerConfig mainConfig);

	public void requestPartyClaimsConfigSync(IPlayerConfig partyConfig, ServerPlayer player);

	public void syncGeneralState(ServerPlayer player, IPlayerConfig config);

	public void syncSubExistence(ServerPlayer player, IPlayerConfig subConfig, boolean create);

	public <T> void syncOptionToClient(ServerPlayer player, IPlayerConfig config, PlayerConfigOptionSpec<T> option);

	void syncGroupLimits(ServerPlayer serverPlayer, IServerPlayerConfigGroupManager groupManager, IPlayerConfig config);

	public void forceConfigType(PlayerConfigType forceConfigType);

	public void sendPermissions(ServerPlayer player, PlayerConfigType type, PlayerConfigPermissionUpdateData data);

	void sendPermissions(ServerPlayer player, PlayerConfigType type);

	void syncAdmin(ServerPlayer player, boolean admin);
}
