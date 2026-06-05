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

package xaero.pac.client.player.config.util;

import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.UUID;

public class ClientPlayerConfigUtils {

	public static IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> getTargetConfig(
			boolean isOtherPlayer,
			PlayerConfigType type,
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager
	){
		if(type == PlayerConfigType.SERVER)
			return playerConfigStorageManager.getServerClaimsConfig();
		if(type == PlayerConfigType.EXPIRED)
			return playerConfigStorageManager.getExpiredClaimsConfig();
		if(type == PlayerConfigType.WILDERNESS)
			return playerConfigStorageManager.getWildernessConfig();
		if(type == PlayerConfigType.DEFAULT_PLAYER)
			return playerConfigStorageManager.getDefaultPlayerConfig();
		if(type == PlayerConfigType.PARTY_CLAIMS)
			return playerConfigStorageManager.getPartyClaimsConfig();
		if(type != PlayerConfigType.PLAYER)
			return null;
		if(isOtherPlayer)
			return playerConfigStorageManager.getOtherPlayerConfig();
		return playerConfigStorageManager.getMyPlayerConfig();
	}

	public static IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> getTargetConfig(
			UUID ownerId,
			PlayerConfigType type,
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager
	){
		if(type == PlayerConfigType.SERVER)
			return playerConfigStorageManager.getServerClaimsConfig();
		if(type == PlayerConfigType.EXPIRED)
			return playerConfigStorageManager.getExpiredClaimsConfig();
		if(type == PlayerConfigType.WILDERNESS)
			return playerConfigStorageManager.getWildernessConfig();
		if(type == PlayerConfigType.DEFAULT_PLAYER)
			return playerConfigStorageManager.getDefaultPlayerConfig();
		if(type == PlayerConfigType.PARTY_CLAIMS)
			return playerConfigStorageManager.getPartyClaimsConfig();
		if(type != PlayerConfigType.PLAYER)
			return null;
		if(ownerId == null)
			return playerConfigStorageManager.getMyPlayerConfig();
		IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> otherPlayerConfig =
				playerConfigStorageManager.getOtherPlayerConfig();
		if(ownerId.equals(otherPlayerConfig.getOwner()))
			return otherPlayerConfig;
		return null;
	}

}
