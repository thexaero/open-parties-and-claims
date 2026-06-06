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
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.group.IClientPlayerConfigGroupManager;
import xaero.pac.client.player.config.util.ClientPlayerConfigUtils;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.config.PlayerConfigPacket;
import xaero.pac.common.packet.util.PacketUtils;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerConfigAbstractGroupPacket extends PlayerConfigPacket {

	protected final PlayerConfigType type;
	@Nullable
	protected final UUID ownerId;
	protected final String groupId;

	public PlayerConfigAbstractGroupPacket(
			PlayerConfigType type,
			@Nullable
			UUID ownerId,
			String groupId
	) {
		this.type = type;
		this.ownerId = ownerId;
		this.groupId = groupId;
	}

	public static abstract class Codec<P extends PlayerConfigAbstractGroupPacket> implements BiConsumer<P, FriendlyByteBuf>, Function<FriendlyByteBuf, P> {

		protected long getNbtReadLimit(){
			return 2097152L;
		}

		protected abstract P readConcreteData(
				CompoundTag nbt,
				PlayerConfigType type,
				UUID ownerId,
				String groupId
		);

		protected abstract void writeConcreteData(
				P t,
				CompoundTag nbt
		);

		protected boolean requiresGroupId(){
			return true;
		}

		@Override
		public P apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.create(getNbtReadLimit()));
				if(nbt == null)
					return null;
				String typeString = nbt.getStringOr("t", "");
				if(typeString.length() > 100) {
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Player config type string is too long!");
					return null;
				}
				PlayerConfigType type = null;
				try {
					type = PlayerConfigType.valueOf(typeString);
				} catch(IllegalArgumentException iae) {
				}
				if(type == null) {
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Received unknown player config type!");
					return null;
				}
				String groupId = nbt.getString("i").orElse(null);
				boolean hasGroupId = groupId != null;
				if(!hasGroupId && requiresGroupId()){
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Player config group ID string was not found!");
					return null;
				}
				if(groupId != null && groupId.length() > 100) {
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Player config group ID string is too long!");
					return null;
				}
				UUID ownerId = XaeroNbtUtil.getUUID(nbt, "o").orElse(null);
				CompoundTag concreteNbt = nbt.getCompoundOrEmpty("c");
				return readConcreteData(concreteNbt, type, ownerId, groupId);
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(P t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("t", t.type.toString());
			if(t.ownerId != null)
				XaeroNbtUtil.putUUID(nbt, "o", t.ownerId);
			if(t.groupId != null)
				nbt.putString("i", t.groupId);
			CompoundTag concreteNbt = new CompoundTag();
			writeConcreteData(t, concreteNbt);
			nbt.put("c", concreteNbt);
			u.writeNbt(nbt);
		}

	}

	public static abstract class ClientHandler<P extends PlayerConfigAbstractGroupPacket> implements Consumer<P> {

		protected abstract void handle(
				P packet,
				IClientPlayerConfigGroupManager groupStorage,
				IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configStorage
		);

		@Override
		public void accept(P packet) {
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();
			IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage =
					ClientPlayerConfigUtils.getTargetConfig(packet.ownerId, packet.type, playerConfigStorageManager);
			if(storage == null)
				return;
			handle(packet, storage.getPlayerGroups(), storage);
		}

	}

	public static abstract class ServerHandler<P extends PlayerConfigAbstractGroupPacket> implements BiConsumer<P, ServerPlayer> {

		public abstract void handle(
				P packet,
				ServerPlayer serverPlayer,
				IServerPlayerConfigGroupManager groupManager,
				IPlayerConfig config
		);

		protected void sendError(ServerPlayer serverPlayer, PlayerConfigAbstractGroupPacket packet, PlayerConfigGroupActionError error){
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(
					serverPlayer,
					new ClientboundGroupActionErrorPacket(packet.type, packet.ownerId, error)
			);
		}

		protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId){
			return systemManager.canEditPartyConfig(playerId);
		}

		@Override
		public void accept(P packet, ServerPlayer serverPlayer) {
			boolean isOp = serverPlayer.hasPermissions(Commands.LEVEL_GAMEMASTERS);
			if(!isOp){
				if(packet.type != PlayerConfigType.PLAYER && packet.type != PlayerConfigType.PARTY_CLAIMS){
					OpenPartiesAndClaims.LOGGER.warn(
							"Non-op player {} attempted to affect groups of the {} config!",
							serverPlayer.getGameProfile().getName(),
							packet.type
					);
					return;
				}
				if(packet.type != PlayerConfigType.PARTY_CLAIMS && packet.ownerId != null) {
					OpenPartiesAndClaims.LOGGER.warn(
							"Non-op player {} attempted to affect groups for another player!",
							serverPlayer.getGameProfile().getName()
					);
					return;
				}
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(serverPlayer.getServer());
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigManager();
			IPlayerConfig config = ServerPlayerConfigUtils.getTargetConfig(
					packet.ownerId, serverPlayer.getUUID(), packet.type, playerConfigs
			);
			if(config == null) {
				OpenPartiesAndClaims.LOGGER.warn(
						"Player {} attempted to affect groups of an unknown config!",
						serverPlayer.getGameProfile().getName()
				);
				return;
			}
			if(!isOp && packet.type == PlayerConfigType.PARTY_CLAIMS && !Objects.equals(config.getPlayerId(), serverPlayer.getUUID())){
				if (!canAffectPartyConfig(serverData.getPlayerPartySystemManager(), serverPlayer.getUUID())) {
					OpenPartiesAndClaims.LOGGER.warn(
							"Non-op player {} attempted to add/remove groups to/from party claims config without proper permission!",
							serverPlayer.getGameProfile().getName()
					);
					return;
				}
			}
			IServerPlayerConfigGroupManager groupManager = config.getPlayerGroups();
			try {
				handle(packet, serverPlayer, groupManager, config);
			} catch(Exception e){
				OpenPartiesAndClaims.LOGGER.info(
						"Player {} failed to affect a group with id \"{}\" because of an exception: {}",
						serverPlayer.getGameProfile().getName(),
						packet.groupId,
						e.getMessage()
				);
			}
		}

	}

}
