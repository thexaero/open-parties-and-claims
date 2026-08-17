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

import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.group.IClientPlayerConfigGroupManager;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.util.ServerPlayerUtils;
import xaero.pac.common.server.world.ServerLevelHelper;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PlayerConfigGroupMemberPacket extends PlayerConfigAbstractGroupPacket {

	private final Action action;
	private final UUID playerId;
	private final String playerName;

	public PlayerConfigGroupMemberPacket(
			PlayerConfigType type,
			@Nullable
			UUID ownerId,
			String groupId,
			Action action,
			UUID playerId,
			String playerName
	) {
		super(type, ownerId, groupId);
		this.action = action;
		this.playerId = playerId;
		this.playerName = playerName;
	}

	public static class Codec extends PlayerConfigAbstractGroupPacket.Codec<PlayerConfigGroupMemberPacket> {

		@Override
		protected PlayerConfigGroupMemberPacket readConcreteData(CompoundTag nbt, PlayerConfigType type, UUID ownerId, String groupId) {
			Action add = Action.values()[nbt.getIntOr("a", 0)];
			UUID memberId = XaeroNbtUtil.getUUID(nbt, "i").orElse(null);
			String memberName = nbt.getString("n").orElse(null);
			if(memberName != null && memberName.length() > 100)//suspiciously long
				return null;
			return new PlayerConfigGroupMemberPacket(type, ownerId, groupId, add, memberId, memberName);
		}

		@Override
		protected void writeConcreteData(PlayerConfigGroupMemberPacket t, CompoundTag nbt) {
			nbt.putInt("a", t.action.ordinal());
			if(t.playerId != null)
				XaeroNbtUtil.putUUID(nbt, "i", t.playerId);
			if(t.playerName != null)
				nbt.putString("n", t.playerName);
		}

	}

	public static class ClientHandler extends PlayerConfigAbstractGroupPacket.ClientHandler<PlayerConfigGroupMemberPacket> {

		@Override
		protected void handle(
				PlayerConfigGroupMemberPacket packet,
				IClientPlayerConfigGroupManager groupStorage,
				IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configStorage
		) {
			//these are not guaranteed to be successful if the packet is received before login group sync is fully
			//complete, but it shouldn't cause any actual issues because it will still all look right when the login
			//sync finishes
			if(packet.action == Action.INCLUDE) {
				groupStorage.includeMemberInCustom(packet.groupId, packet.playerId, packet.playerName);
				return;
			}
			if(packet.action == Action.EXCLUDE) {
				groupStorage.excludeMemberFromCustom(packet.groupId, packet.playerId, packet.playerName);
				return;
			}
			if(packet.action == Action.NAME){
				groupStorage.updateCustomMemberName(packet.groupId, packet.playerId, packet.playerName);
			}
		}

	}

	public static class ServerHandler extends PlayerConfigAbstractGroupPacket.ServerHandler<PlayerConfigGroupMemberPacket> {

		@Override
		protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId) {
			return systemManager.canIncludePlayersInPartyConfigGroups(playerId);
		}

		@Override
		public void handle(
				PlayerConfigGroupMemberPacket packet,
				ServerPlayer serverPlayer,
				IServerPlayerConfigGroupManager groupManager,
				IPlayerConfig config) {
			ICustomPlayerConfigGroup group = groupManager.getCustom(packet.groupId);
			if(group == null){
				sendError(serverPlayer, packet, PlayerConfigGroupActionError.GROUP_TO_EDIT_NOT_FOUND);
				return;
			}
			if (packet.action == Action.INCLUDE) {
				boolean isOp = Commands.LEVEL_GAMEMASTERS.check(serverPlayer.permissions());
				if(!isOp && packet.playerId == null &&
						!ServerPlayerUtils.playerNameIsKnown(ServerLevelHelper.getServer(serverPlayer), packet.playerName)){
					//only ops are allowed to add previously unknown players
					sendError(serverPlayer, packet, PlayerConfigGroupActionError.UNKNOWN_PLAYER);
					return;
				}
				Optional<PlayerConfigGroupActionError> error =
						group.includeMemberLimitedInternal(packet.playerId, packet.playerName, true).right();
				if(error.isPresent())
					sendError(serverPlayer, packet, error.get());
				return;
			}
			if (packet.action == Action.EXCLUDE) {
				Optional<PlayerConfigGroupActionError> error =
						group.excludeMember(packet.playerId, packet.playerName);
				if(error.isPresent())
					sendError(serverPlayer, packet, error.get());
				return;
			}
			OpenPartiesAndClaims.LOGGER.warn(
					"Player {} has requested a very unusual change of a group member: {}!",
					serverPlayer.getGameProfile().name(),
					packet.action
			);
		}

	}

	public enum Action {
		INCLUDE,
		EXCLUDE,
		NAME
	}

}
