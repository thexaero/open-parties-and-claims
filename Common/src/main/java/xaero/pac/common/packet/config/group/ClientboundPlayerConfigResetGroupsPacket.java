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

package xaero.pac.common.packet.config.group;

import net.minecraft.nbt.CompoundTag;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.group.IClientPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClientboundPlayerConfigResetGroupsPacket extends PlayerConfigAbstractGroupPacket {

	public ClientboundPlayerConfigResetGroupsPacket(
			PlayerConfigType type,
			@Nullable
			UUID ownerId
	) {
		super(type, ownerId, null);
	}

	public static class Codec extends PlayerConfigAbstractGroupPacket.Codec<ClientboundPlayerConfigResetGroupsPacket> {

		@Override
		protected boolean requiresGroupId() {
			return false;
		}

		@Override
		protected ClientboundPlayerConfigResetGroupsPacket readConcreteData(CompoundTag nbt, PlayerConfigType type, UUID ownerId, String groupId) {
			return new ClientboundPlayerConfigResetGroupsPacket(type, ownerId);
		}

		@Override
		protected void writeConcreteData(ClientboundPlayerConfigResetGroupsPacket t, CompoundTag nbt) {
		}

	}

	public static class ClientHandler extends PlayerConfigAbstractGroupPacket.ClientHandler<ClientboundPlayerConfigResetGroupsPacket> {

		@Override
		protected void handle(
				ClientboundPlayerConfigResetGroupsPacket packet,
				IClientPlayerConfigGroupManager groupStorage,
				IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configStorage
		) {
			groupStorage.reset();
		}

	}

}
