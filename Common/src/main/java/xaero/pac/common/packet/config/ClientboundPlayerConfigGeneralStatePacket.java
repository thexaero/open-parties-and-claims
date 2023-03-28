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
import net.minecraft.nbt.Tag;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

public class ClientboundPlayerConfigGeneralStatePacket extends ClientboundPlayerConfigAbstractStatePacket {

	private final boolean beingDeleted;
	private final int subConfigLimit;

	public ClientboundPlayerConfigGeneralStatePacket(PlayerConfigType type, boolean otherPlayer, String subId, boolean beingDeleted, int subConfigLimit){
		super(type, otherPlayer, subId);
		this.beingDeleted = beingDeleted;
		this.subConfigLimit = subConfigLimit;
	}

	public static class Codec extends ClientboundPlayerConfigAbstractStatePacket.Codec<ClientboundPlayerConfigGeneralStatePacket> {

		@Override
		protected ClientboundPlayerConfigGeneralStatePacket decode(CompoundTag nbt, PlayerConfigType type, boolean otherPlayer, String subId) {
			if(!nbt.contains("d", Tag.TAG_BYTE)) {
				OpenPartiesAndClaims.LOGGER.info("Unknown player config being deleted state!");
				return null;
			}
			boolean beingDeleted = nbt.getBoolean("d");
			int subConfigLimit = nbt.getInt("sl");
			return new ClientboundPlayerConfigGeneralStatePacket(type, otherPlayer, subId, beingDeleted, subConfigLimit);
		}

		@Override
		protected void encode(ClientboundPlayerConfigGeneralStatePacket packet, CompoundTag nbt) {
			nbt.putBoolean("d", packet.beingDeleted);
			nbt.putInt("sl", packet.subConfigLimit);
		}

		@Override
		protected int getExtraSizeLimit() {
			return 0;
		}

	}

	public static class ClientHandler extends ClientboundPlayerConfigAbstractStatePacket.ClientHandler<ClientboundPlayerConfigGeneralStatePacket> {

		@Override
		protected void accept(ClientboundPlayerConfigGeneralStatePacket t, IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage) {
			storage.setGeneralState(t.beingDeleted, t.subConfigLimit);
		}

	}

}
