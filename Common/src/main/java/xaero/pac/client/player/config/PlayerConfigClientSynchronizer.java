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

package xaero.pac.client.player.config;

import com.google.common.collect.Lists;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.config.PlayerConfigOptionValuePacket;
import xaero.pac.common.packet.config.ServerboundPlayerConfigOptionValuePacket;
import xaero.pac.common.packet.config.ServerboundSubConfigExistencePacket;

public class PlayerConfigClientSynchronizer {
	
	public <T extends Comparable<T>> void syncToServer(PlayerConfigClientStorage config, IPlayerConfigStringableOptionClientStorage<T> option) {
		PlayerConfigOptionValuePacket.Entry packetOptionEntry = new PlayerConfigOptionValuePacket.Entry(option.getId(), option.getType(), option.getValue(), option.isMutable(), option.isDefaulted());

		ServerboundPlayerConfigOptionValuePacket packet = new ServerboundPlayerConfigOptionValuePacket(config.getType(), config.getSubId(), config.getOwner(), Lists.newArrayList(packetOptionEntry));
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(packet);
	}

	public void requestCreateSubConfig(PlayerConfigClientStorage config, String subId){
		ServerboundSubConfigExistencePacket packet = new ServerboundSubConfigExistencePacket(subId, config.getOwner(), config.getType(), true);
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(packet);
	}

	public void requestDeleteSubConfig(PlayerConfigClientStorage config, String subId){
		ServerboundSubConfigExistencePacket packet = new ServerboundSubConfigExistencePacket(subId, config.getOwner(), config.getType(), false);
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(packet);
	}

}
