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

package xaero.pac.common.packet.config;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.OtherPlayerConfigWaitScreen;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.UUID;

public class ClientboundPlayerConfigSyncStatePacket extends ClientboundPlayerConfigAbstractStatePacket {

	private final boolean state;
	private final UUID ownerId;

	public ClientboundPlayerConfigSyncStatePacket(PlayerConfigType type, boolean state, @Nullable UUID ownerId){
		super(type, type == PlayerConfigType.PLAYER && ownerId != null, PlayerConfig.MAIN_SUB_ID);
		this.state = state;
		this.ownerId = ownerId;
	}

	public static class Codec extends ClientboundPlayerConfigAbstractStatePacket.Codec<ClientboundPlayerConfigSyncStatePacket> {

		@Override
		protected ClientboundPlayerConfigSyncStatePacket decode(CompoundTag nbt, PlayerConfigType type, boolean otherPlayer, String subId) {
			if(!nbt.contains("s", Tag.TAG_BYTE)) {
				OpenPartiesAndClaims.LOGGER.info("Unknown player config sync state!");
				return null;
			}
			boolean state = nbt.getBoolean("s");
			UUID ownerId = null;
			if(nbt.contains("oi"))
				ownerId = nbt.getUUID("oi");
			return new ClientboundPlayerConfigSyncStatePacket(type, state, ownerId);
		}

		@Override
		protected void encode(ClientboundPlayerConfigSyncStatePacket packet, CompoundTag nbt) {
			nbt.putBoolean("s", packet.state);
			if(packet.ownerId != null)
				nbt.putUUID("oi", packet.ownerId);
		}

		@Override
		protected int getExtraSizeLimit() {
			return 0;
		}

	}

	public static class ClientHandler extends ClientboundPlayerConfigAbstractStatePacket.ClientHandler<ClientboundPlayerConfigSyncStatePacket> {

		@Override
		protected void accept(ClientboundPlayerConfigSyncStatePacket t, IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage) {
			if(!t.isOtherPlayer() && !storage.isSyncInProgress() && t.state)//null storage is only possible when otherPlayer
				storage.reset();
			boolean isOtherPlayerWaitScreen = t.isOtherPlayer() &&
					Minecraft.getInstance().screen instanceof OtherPlayerConfigWaitScreen;
			if(isOtherPlayerWaitScreen && t.state){
				IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> prevOtherStorage = storage;
				storage = playerConfigStorageManager
						.beginConfigStorageBuild(LinkedHashMap::new)
						.setType(PlayerConfigType.PLAYER)
						.setOwner(t.ownerId)
						.build();
				if (prevOtherStorage != null && t.ownerId.equals(prevOtherStorage.getOwner()))
					storage.setSelectedSubConfig(prevOtherStorage.getSelectedSubConfig());
				playerConfigStorageManager.setOtherPlayerConfig(storage);
			}
			if(storage == null)
				return;
			storage.setSyncInProgress(t.state);
			if(t.state && storage.getPlayerGroups() != null)
				storage.getPlayerGroups().setSyncInProgress(true);
			if(isOtherPlayerWaitScreen && !t.state){
				OtherPlayerConfigWaitScreen.Listener listener =
						((OtherPlayerConfigWaitScreen) Minecraft.getInstance().screen).getListener();
				if (listener != null)//only true when it's still the wait screen that made the request
					listener.onConfigDataSyncDone(storage);
			}
		}

	}

}
