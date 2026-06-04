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
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.group.IClientPlayerConfigGroupManager;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PlayerConfigGroupExistencePacket extends PlayerConfigAbstractGroupPacket {

	private final boolean add;

	public PlayerConfigGroupExistencePacket(
			PlayerConfigType type,
			@Nullable
			UUID ownerId,
			String groupId,
			boolean add
	) {
		super(type, ownerId, groupId);
		this.add = add;
	}

	public static class Codec extends PlayerConfigAbstractGroupPacket.Codec<PlayerConfigGroupExistencePacket> {

		@Override
		protected PlayerConfigGroupExistencePacket readConcreteData(CompoundTag nbt, PlayerConfigType type, UUID ownerId, String groupId) {
			boolean add = nbt.getBoolean("a");
			return new PlayerConfigGroupExistencePacket(type, ownerId, groupId, add);
		}

		@Override
		protected void writeConcreteData(PlayerConfigGroupExistencePacket t, CompoundTag nbt) {
			nbt.putBoolean("a", t.add);
		}

	}

	public static class ClientHandler extends PlayerConfigAbstractGroupPacket.ClientHandler<PlayerConfigGroupExistencePacket> {

		@Override
		protected void handle(PlayerConfigGroupExistencePacket packet, IClientPlayerConfigGroupManager groupStorage, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configStorage) {
			if(packet.add) {
				groupStorage.addCustom(packet.groupId);
				return;
			}
			groupStorage.removeCustom(packet.groupId);
		}

	}

	public static class ServerHandler extends PlayerConfigAbstractGroupPacket.ServerHandler<PlayerConfigGroupExistencePacket> {

		@Override
		protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId) {
			return systemManager.canCreatePartyConfigGroups(playerId);
		}

		@Override
		public void handle(PlayerConfigGroupExistencePacket packet, ServerPlayer serverPlayer, IServerPlayerConfigGroupManager groupManager, IPlayerConfig config) {
			if(packet.add) {
				Optional<PlayerConfigGroupActionError> error = groupManager.addCustomLimitedInternal(packet.groupId).right();
				if(error.isPresent())
					sendError(serverPlayer, packet, error.get());
				return;
			} else {
				Optional<PlayerConfigGroupActionError> error = groupManager.removeCustom(packet.groupId);
				if(error.isPresent())
					sendError(serverPlayer, packet, error.get());
			}
		}

	}

}
