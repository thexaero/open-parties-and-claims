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
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.group.IClientPlayerConfigGroupManager;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PlayerConfigGroupGroupPacket extends PlayerConfigAbstractGroupPacket {

	private final Action action;
	private final String inclusionId;

	public PlayerConfigGroupGroupPacket(
			PlayerConfigType type,
			@Nullable
			UUID ownerId,
			String groupId,
			Action action,
			String inclusionId
	) {
		super(type, ownerId, groupId);
		this.action = action;
		this.inclusionId = inclusionId;
	}

	public static class Codec extends PlayerConfigAbstractGroupPacket.Codec<PlayerConfigGroupGroupPacket> {

		@Override
		protected PlayerConfigGroupGroupPacket readConcreteData(CompoundTag nbt, PlayerConfigType type, UUID ownerId, String groupId) {
			Action add = Action.values()[nbt.getIntOr("a", 0)];
			String inclusionId = nbt.getStringOr("i", "");
			if(inclusionId.length() > 100)//suspiciously long
				return null;
			return new PlayerConfigGroupGroupPacket(type, ownerId, groupId, add, inclusionId);
		}

		@Override
		protected void writeConcreteData(PlayerConfigGroupGroupPacket t, CompoundTag nbt) {
			nbt.putInt("a", t.action.ordinal());
			nbt.putString("i", t.inclusionId);
		}

	}

	public static class ClientHandler extends PlayerConfigAbstractGroupPacket.ClientHandler<PlayerConfigGroupGroupPacket> {

		@Override
		protected void handle(
				PlayerConfigGroupGroupPacket packet,
				IClientPlayerConfigGroupManager groupStorage,
				IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configStorage
		) {
			if(packet.action == Action.INCLUDE) {
				if(!groupStorage.includeGroupInCustom(packet.groupId, packet.inclusionId)) {
					OpenPartiesAndClaims.LOGGER.warn(
							"Couldn't complete the server's player config group group inclusion request: {}, {}",
							packet.groupId, packet.inclusionId
					);
					OpenPartiesAndClaims.LOGGER.warn("This shouldn't normally be possible!");
				}
				return;
			}
			if(packet.action == Action.EXCLUDE) {
				if(!groupStorage.excludeGroupFromCustom(packet.groupId, packet.inclusionId)) {
					OpenPartiesAndClaims.LOGGER.warn(
							"Couldn't complete the server's player config group group exclusion request: {}, {}",
							packet.groupId, packet.inclusionId
					);
					OpenPartiesAndClaims.LOGGER.warn("This shouldn't normally be possible!");
				}
				return;
			}
		}

	}

	public static class ServerHandler extends PlayerConfigAbstractGroupPacket.ServerHandler<PlayerConfigGroupGroupPacket> {

		@Override
		public void handle(
				PlayerConfigGroupGroupPacket packet,
				ServerPlayer serverPlayer,
				IServerPlayerConfigGroupManager groupManager,
				IPlayerConfig config
		) {
			ICustomPlayerConfigGroup group = groupManager.getCustom(packet.groupId);
			if(group == null){
				sendError(serverPlayer, packet, PlayerConfigGroupActionError.GROUP_TO_EDIT_NOT_FOUND);
				return;
			}
			if (packet.action == Action.INCLUDE) {
				Optional<PlayerConfigGroupActionError> error =
						group.includeGroupLimited(packet.inclusionId);
				if(error.isPresent())
					sendError(serverPlayer, packet, error.get());
				return;
			}
			if (packet.action == Action.EXCLUDE) {
				Optional<PlayerConfigGroupActionError> error =
						group.excludeGroup(packet.inclusionId);
				if(error.isPresent())
					sendError(serverPlayer, packet, error.get());
				return;
			}
			OpenPartiesAndClaims.LOGGER.warn(
					"Player {} has requested a very unusual change of a group inclusion: {}!",
					serverPlayer.getGameProfile().name(),
					packet.action
			);
		}

	}

	public enum Action {
		INCLUDE,
		EXCLUDE
	}

}
