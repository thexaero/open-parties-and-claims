/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.client.player.config;

import com.google.common.collect.Lists;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.config.PlayerConfigOptionValuePacket;

public class PlayerConfigClientSynchronizer {
	
	public <T> void syncToServer(PlayerConfigClientStorage config, IPlayerConfigStringableOptionClientStorage<T> option) {
		PlayerConfigOptionClientStorage<T> packetOptionEntry = new PlayerConfigOptionClientStorage<>(option.getOption(), option.getValue());
		packetOptionEntry.setMutable(option.isMutable());
		packetOptionEntry.setDefaulted(option.isDefaulted());
		
		PlayerConfigOptionValuePacket packet = new PlayerConfigOptionValuePacket(config.getType(), config.getOwner(), Lists.newArrayList(packetOptionEntry));
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(packet);
	}

}
