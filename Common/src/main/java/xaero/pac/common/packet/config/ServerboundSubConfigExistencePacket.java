/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfigDeletionStarter;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerboundSubConfigExistencePacket extends PlayerConfigPacket {

	private final String subId;
	private final UUID owner;
	private final PlayerConfigType type;
	private final boolean create;

	public ServerboundSubConfigExistencePacket(String subId, UUID owner, PlayerConfigType type, boolean create) {
		this.subId = subId;
		this.owner = owner;
		this.type = type;
		this.create = create;
	}

	public static class Codec implements BiConsumer<ServerboundSubConfigExistencePacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ServerboundSubConfigExistencePacket> {

		@Override
		public ServerboundSubConfigExistencePacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(4096));
				if(nbt == null)
					return null;
				String subId = nbt.getString("subId");
				if(subId.isEmpty() || subId.length() > 100)
					return null;
				String typeString = nbt.getString("type");
				if(typeString.isEmpty() || typeString.length() > 100)
					return null;
				PlayerConfigType type = null;
				try {
					type = PlayerConfigType.valueOf(typeString);
				} catch(IllegalArgumentException iae) {
				}
				if(type == null) {
					OpenPartiesAndClaims.LOGGER.info("Received unknown player config type!");
					return null;
				}
				UUID owner = nbt.contains("owner") ? nbt.getUUID("owner") : null;
				boolean create = nbt.getBoolean("create");
				return new ServerboundSubConfigExistencePacket(subId, owner, type, create);
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(ServerboundSubConfigExistencePacket t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("subId", t.subId);
			if(t.owner != null)
				nbt.putUUID("owner", t.owner);
			nbt.putString("type", t.type.name());
			nbt.putBoolean("create", t.create);
			u.writeNbt(nbt);
		}
		
	}
	
	public static class ServerHandler implements BiConsumer<ServerboundSubConfigExistencePacket,ServerPlayer> {
		
		@Override
		public void accept(ServerboundSubConfigExistencePacket t, ServerPlayer serverPlayer) {
			if(t.type != PlayerConfigType.PLAYER && t.type != PlayerConfigType.SERVER) {
				OpenPartiesAndClaims.LOGGER.info("Someone is trying to create/delete a sub-config for an invalid config type! Name: " + serverPlayer.getGameProfile().getName());
				return;
			}
			boolean isOP = serverPlayer.hasPermissions(2);
			boolean isServer = t.type == PlayerConfigType.SERVER;
			UUID ownerId = isServer ? null : t.owner == null ? serverPlayer.getUUID() : t.owner;
			if(!isOP) {
				if(isServer) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to create/delete a sub-config without required permissions! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
				if(!Objects.equals(ownerId, serverPlayer.getUUID())) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to create/delete a sub-config for another player! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(serverPlayer.getServer());
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
			IPlayerConfig config = !isServer ?
										playerConfigs.getLoadedConfig(ownerId) :
										playerConfigs.getServerClaimConfig();
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(serverPlayer);
			if(serverData.getServerTickHandler().getTickCounter() == playerData.getLastSubConfigCreationTick())
				return;//going too fast
			playerData.setLastSubConfigCreationTick(serverData.getServerTickHandler().getTickCounter());

			if(t.create) {
				boolean reachedLimit = config.getSubCount() >= config.getSubConfigLimit();
				if (reachedLimit || config.createSubConfig(t.subId) == null || !isServer && !Objects.equals(ownerId, serverPlayer.getUUID())) {
					playerConfigs.getSynchronizer().confirmSubConfigCreationSync(serverPlayer, config);//need to notify the client even when unsuccessful
					if(reachedLimit) {
						MutableComponent limitReachedMessage = Component.translatable("gui.xaero_pac_config_create_sub_id_limit_reached", config.getSubConfigLimit());
						limitReachedMessage.withStyle(s -> s.withColor(ChatFormatting.RED));
						serverPlayer.sendSystemMessage(limitReachedMessage);
					}
				}
			} else {

				IPlayerConfig subConfig = config.getSubConfig(t.subId);
				if(subConfig == null)
					return;
				if(subConfig == config)
					return;
				IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = serverData.getServerClaimsManager().getPlayerInfo(config.getPlayerId());
				if(playerInfo.hasReplacementTasks()){
					serverPlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_config_delete_sub_already_replacing"));
					playerConfigs.getSynchronizer().syncGeneralState(serverPlayer, subConfig);//notify client
					return;
				}
				new PlayerSubConfigDeletionStarter().start(serverPlayer, playerInfo, subConfig, serverData);
			}
		}
		
	}
}
