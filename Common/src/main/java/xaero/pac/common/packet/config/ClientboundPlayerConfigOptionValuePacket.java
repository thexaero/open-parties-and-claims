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

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.util.ClientPlayerConfigUtils;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientboundPlayerConfigOptionValuePacket extends PlayerConfigOptionValuePacket {

	public ClientboundPlayerConfigOptionValuePacket(PlayerConfigType type, String subId, UUID owner, List<Entry> entries) {
		super(type, subId, owner, entries);
	}

	public static class Codec extends PlayerConfigOptionValuePacket.Codec<ClientboundPlayerConfigOptionValuePacket> {

		@Override
		protected int getSizeLimit() {
			return 536870912;
		}

		@Override
		protected ClientboundPlayerConfigOptionValuePacket create(PlayerConfigType type, String subId, UUID owner, List<Entry> list) {
			return new ClientboundPlayerConfigOptionValuePacket(type, subId, owner, list);
		}

	}

	public static class ClientHandler implements Consumer<ClientboundPlayerConfigOptionValuePacket> {

		@Override
		public void accept(ClientboundPlayerConfigOptionValuePacket t) {
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();

			IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>
					storage = ClientPlayerConfigUtils.getTargetConfig(t.owner, t.type, playerConfigStorageManager);

			if(storage == null)
				return;
			if(t.subId != null)
				storage = storage.getOrCreateSubConfig(t.subId);
			final IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> forwardedStorage = storage;
			t.entryStream().forEach(entry -> {
				PlayerConfigOptionSpec<?> option =
						(PlayerConfigOptionSpec<?>) playerConfigStorageManager.getOptionForId(entry.getId());
				if(option == null)
					return;
				if(!option.isSyncable())
					return;
				Object value = null;
				try {
					value = option.getValueType().getSyncDecoder().apply(entry.getValueTag());
				} catch(Throwable e){
				}
				IPlayerConfigStringableOptionClientStorage<?> optionStorage = forwardedStorage.getOptionStorage(option);
				optionStorage.setCastValue(value);
				optionStorage.setMutable(entry.isMutable());
				optionStorage.setDefaulted(entry.isDefaulted());
			});
		}

	}

}
