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

package xaero.pac.common.packet.config.group;

import net.minecraft.nbt.CompoundTag;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.packet.config.ClientboundPlayerConfigAbstractStatePacket;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

public class ClientboundPlayerConfigGroupLimitsPacket extends ClientboundPlayerConfigAbstractStatePacket {

	private final int maxGroups;
	private final int groupSpace;

	public ClientboundPlayerConfigGroupLimitsPacket(PlayerConfigType type, boolean otherPlayer, int maxGroups, int groupSpace){
		super(type, otherPlayer, PlayerConfig.MAIN_SUB_ID);
		this.maxGroups = maxGroups;
		this.groupSpace = groupSpace;
	}

	public static class Codec extends ClientboundPlayerConfigAbstractStatePacket.Codec<ClientboundPlayerConfigGroupLimitsPacket> {

		@Override
		protected ClientboundPlayerConfigGroupLimitsPacket decode(
				CompoundTag nbt,
				PlayerConfigType type,
				boolean otherPlayer,
				String subId
		) {
			int maxGroups = nbt.getIntOr("g", 0);
			int groupSpace = nbt.getIntOr("s", 0);
			return new ClientboundPlayerConfigGroupLimitsPacket(type, otherPlayer, maxGroups, groupSpace);
		}

		@Override
		protected void encode(ClientboundPlayerConfigGroupLimitsPacket packet, CompoundTag nbt) {
			nbt.putInt("g", packet.maxGroups);
			nbt.putInt("s", packet.groupSpace);
		}

		@Override
		protected int getExtraSizeLimit() {
			return 0;
		}

	}

	public static class ClientHandler extends ClientboundPlayerConfigAbstractStatePacket.ClientHandler<ClientboundPlayerConfigGroupLimitsPacket> {

		@Override
		protected void accept(
				ClientboundPlayerConfigGroupLimitsPacket t,
				IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager,
				IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage
		) {
			if(storage == null)
				return;
			storage.getPlayerGroups().setLimits(t.maxGroups, t.groupSpace);
		}

	}

}
