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

package xaero.pac.common.server.player.config.util;

import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.UUID;

public class ServerPlayerConfigUtils {

	public static IPlayerConfig getTargetConfig(
			UUID ownerId,
			UUID callerId,
			PlayerConfigType type,
			IPlayerConfigManager playerConfigs
	){
		if(type == PlayerConfigType.SERVER)
			return playerConfigs.getServerClaimConfig();
		if(type == PlayerConfigType.EXPIRED)
			return playerConfigs.getExpiredClaimConfig();
		if(type == PlayerConfigType.DEFAULT_PLAYER)
			return playerConfigs.getDefaultConfig();
		if(type == PlayerConfigType.WILDERNESS)
			return playerConfigs.getWildernessConfig();
		if(type == PlayerConfigType.PARTY_CLAIMS)
			return playerConfigs.getPartyOwnerConfig(callerId);
		if(type != PlayerConfigType.PLAYER)
			return null;
		UUID effectiveOwnerId = ownerId == null ? callerId : ownerId;
		return playerConfigs.getLoadedConfig(effectiveOwnerId);
	}

}
