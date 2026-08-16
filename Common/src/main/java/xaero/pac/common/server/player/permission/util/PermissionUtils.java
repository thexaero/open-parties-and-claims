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

package xaero.pac.common.server.player.permission.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.ModConfigSpec;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

import java.util.OptionalInt;
import java.util.UUID;

public class PermissionUtils {

	public static int getOverriddenServerConfigInt(
			UUID playerId,
			MinecraftServer server,
			ServerPlayer player,
			IPlayerConfig playerConfig,
			ModConfigSpec.IntValue serverConfigOption,
			IPermissionNodeAPI<Integer> permissionNode,
			IPlayerPermissionSystemAPI permissionSystem
	){
		int defaultLimit = serverConfigOption.get();
		if(permissionSystem == null)
			return defaultLimit;
		if(permissionNode == null || permissionNode.getNodeString().isEmpty())
			return defaultLimit;
		if(player == null)
			player = server.getPlayerList().getPlayer(playerId);
		if(player == null) {
			Integer lastPermissionValue = playerConfig.getLastPermissionValue(permissionNode);
			if(lastPermissionValue == null)
				return defaultLimit;
			return lastPermissionValue;
		}
		OptionalInt optionalValue = permissionSystem.getIntPermission(player, permissionNode);
		Integer permissionValue = optionalValue.isPresent() ? optionalValue.getAsInt() : null;
		playerConfig.setLastPermissionValue(permissionNode, permissionValue);
		if(permissionValue == null)
			return defaultLimit;
		return permissionValue;
	}

}
