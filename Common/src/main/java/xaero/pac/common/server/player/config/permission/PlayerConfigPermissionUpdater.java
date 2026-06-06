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

package xaero.pac.common.server.player.config.permission;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.config.PlayerConfigPermissionUpdateData;

public class PlayerConfigPermissionUpdater {

	public void update(
			ServerPlayer player,
			IServerData<?,?> serverData,
			boolean checkTime,
			boolean sendCommandsOnChange
	) {
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		update(playerData, player, serverData, checkTime, sendCommandsOnChange);
	}

	public void update(
			ServerPlayerData playerData,
			ServerPlayer player,
			IServerData<?,?> serverData,
			boolean checkTime,
			boolean sendCommandsOnChange
	) {
		boolean lastSyncedConfigAdmin = playerData.getSyncedConfigAdmin();
		boolean admin = player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
		if(admin != lastSyncedConfigAdmin){
			serverData.getPlayerConfigManager().getSynchronizer().syncAdmin(player, admin);
			playerData.setSyncedConfigAdmin(admin);
		}
		long time = System.currentTimeMillis();
		if (checkTime && time - playerData.getLastPlayerConfigPermissionUpdate() < 100)
			return;
		playerData.setLastPlayerConfigPermissionUpdate(time);
		boolean hadChanges = false;
		for (PlayerConfigType type : PlayerConfigType.values()) {
			if(updateForType(type, playerData, player, serverData))
				hadChanges = true;
		}
		if(hadChanges && sendCommandsOnChange)
			serverData.getServer().getCommands().sendCommands(player);
	}

	private boolean updateForType(
			PlayerConfigType configType,
			ServerPlayerData playerData,
			ServerPlayer player,
			IServerData<?,?> serverData
	){
		PlayerConfigPermissionUpdateData updateData = playerData.getPlayerConfigPermissionUpdateData(configType);
		if(updateData.update(player, serverData)) {
			serverData.getPlayerConfigManager().getSynchronizer().sendPermissions(player, configType, updateData);
			return true;
		}
		return false;
	}

}
