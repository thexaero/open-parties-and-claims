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
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerConfigGroupDesyncPacket extends PlayerConfigAbstractGroupPacket {

	public PlayerConfigGroupDesyncPacket(
			PlayerConfigType type,
			@Nullable
			UUID ownerId
	) {
		super(type, ownerId, null);
	}

	public static class Codec extends PlayerConfigAbstractGroupPacket.Codec<PlayerConfigGroupDesyncPacket> {

		@Override
		protected boolean requiresGroupId() {
			return false;
		}

		@Override
		protected PlayerConfigGroupDesyncPacket readConcreteData(CompoundTag nbt, PlayerConfigType type, UUID ownerId, String groupId) {
			return new PlayerConfigGroupDesyncPacket(type, ownerId);
		}

		@Override
		protected void writeConcreteData(PlayerConfigGroupDesyncPacket t, CompoundTag nbt) {
		}

	}

	public static class ClientHandler extends PlayerConfigAbstractGroupPacket.ClientHandler<PlayerConfigGroupDesyncPacket> {

		@Override
		protected void handle(PlayerConfigGroupDesyncPacket packet, IClientPlayerConfigGroupManager groupStorage, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configStorage) {
			groupStorage.confirmDesyncFix();
		}

	}

	public static class ServerHandler extends PlayerConfigAbstractGroupPacket.ServerHandler<PlayerConfigGroupDesyncPacket> {

		@Override
		protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId) {
			return true;
		}

		@Override
		public void handle(PlayerConfigGroupDesyncPacket packet, ServerPlayer serverPlayer, IServerPlayerConfigGroupManager groupManager, IPlayerConfig config) {
			config.getManager().getSynchronizer().syncGroupLimits(serverPlayer, groupManager, config);
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(serverPlayer, new PlayerConfigGroupDesyncPacket(packet.type, packet.ownerId));
		}

	}

}
