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

package xaero.pac.common.packet.config;

import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

public class PlayerConfigPacketUtil {

	public static IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> getTargetConfig(boolean isOtherPlayer, PlayerConfigType type, IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager){
		IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage;
		if(isOtherPlayer) {
			storage = playerConfigStorageManager.getOtherPlayerConfig();
		} else {
			storage =
					type == PlayerConfigType.SERVER ? playerConfigStorageManager.getServerClaimsConfig() :
					type == PlayerConfigType.EXPIRED ? playerConfigStorageManager.getExpiredClaimsConfig() :
					type == PlayerConfigType.WILDERNESS ? playerConfigStorageManager.getWildernessConfig() :
					type == PlayerConfigType.DEFAULT_PLAYER ? playerConfigStorageManager.getDefaultPlayerConfig() :
							playerConfigStorageManager.getMyPlayerConfig();
		}
		return storage;
	}

}
