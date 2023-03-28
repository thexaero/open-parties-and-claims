/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.nbt.CompoundTag;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

public class ClientboundPlayerConfigRemoveSubPacket extends ClientboundPlayerConfigAbstractStatePacket {

	private final String subIdToRemove;

	public ClientboundPlayerConfigRemoveSubPacket(PlayerConfigType type, boolean otherPlayer, String subIdToRemove){
		super(type, otherPlayer, PlayerConfig.MAIN_SUB_ID);
		this.subIdToRemove = subIdToRemove;
	}

	public static class Codec extends ClientboundPlayerConfigAbstractStatePacket.Codec<ClientboundPlayerConfigRemoveSubPacket> {

		@Override
		protected ClientboundPlayerConfigRemoveSubPacket decode(CompoundTag nbt, PlayerConfigType type, boolean otherPlayer, String subId) {
			String subIdToRemove = nbt.getString("i");
			if(subIdToRemove.isEmpty() || subIdToRemove.length() > 100) {
				OpenPartiesAndClaims.LOGGER.info("Bad sub id!");
				return null;
			}
			return new ClientboundPlayerConfigRemoveSubPacket(type, otherPlayer, subIdToRemove);
		}

		@Override
		protected void encode(ClientboundPlayerConfigRemoveSubPacket packet, CompoundTag nbt) {
			nbt.putString("i", packet.subIdToRemove);
		}

		@Override
		protected int getExtraSizeLimit() {
			return 0;
		}

	}

	public static class ClientHandler extends ClientboundPlayerConfigAbstractStatePacket.ClientHandler<ClientboundPlayerConfigRemoveSubPacket> {

		@Override
		protected void accept(ClientboundPlayerConfigRemoveSubPacket t, IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage) {
			storage.removeSubConfig(t.subIdToRemove);
		}

	}

}
