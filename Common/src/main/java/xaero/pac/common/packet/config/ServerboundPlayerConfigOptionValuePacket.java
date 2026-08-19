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

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ServerboundPlayerConfigOptionValuePacket extends PlayerConfigOptionValuePacket {

	public ServerboundPlayerConfigOptionValuePacket(PlayerConfigType type, String subId, UUID owner, List<Entry> entries) {
		super(type, subId, owner, entries);
	}

	public static class Codec extends PlayerConfigOptionValuePacket.Codec<ServerboundPlayerConfigOptionValuePacket> {

		@Override
		protected int getSizeLimit() {
			return 262144;
		}

		@Override
		protected ServerboundPlayerConfigOptionValuePacket create(PlayerConfigType type, String subId, UUID owner, List<Entry> entries) {
			return new ServerboundPlayerConfigOptionValuePacket(type, subId, owner, entries);
		}

	}

	public static class ServerHandler implements BiConsumer<ServerboundPlayerConfigOptionValuePacket, ServerPlayer> {

		@SuppressWarnings("unchecked")
		private <T> IPlayerConfigAPI.SetResult setConfigUnchecked(
				IPlayerConfig config,
				IPlayerConfigOptionSpecAPI<T> option,
				Object value,
				ServerPlayer serverPlayer
		) {
			if(ServerConfig.CONFIG.claimsEnabled.get()) {
				if(!serverPlayer.hasPermissions(Commands.LEVEL_GAMEMASTERS) &&
						option != PlayerConfigOptions.BONUS_CHUNK_CLAIMS &&
						ServerPlayerConfigUtils.isOverClaimLimit(config)) {
					Component message = Component.translatable("gui.xaero_pac_config_claim_count_over_limit")
							.withStyle(ChatFormatting.RED);
					serverPlayer.sendMessage(message, serverPlayer.getUUID());
					return null;
				}
			}
			return config.tryToSet(option, (T) value);
		}

		@Override
		public void accept(ServerboundPlayerConfigOptionValuePacket t, ServerPlayer serverPlayer) {
			if(t == null)
				return;
			if(t.entries.size() > 1) {
				OpenPartiesAndClaims.LOGGER.info("A player is attempting to modify multiple options in a single packet! Name: " + serverPlayer.getGameProfile().getName());
				return;
			}
			boolean isOP = serverPlayer.hasPermissions(2);
			Entry optionEntry = t.entries.get(0);
			UUID ownerId = t.getType() != PlayerConfigType.PLAYER ? null : t.owner == null ? serverPlayer.getUUID() : t.owner;
			if(!isOP) {
				if(t.getType() != PlayerConfigType.PLAYER && t.getType() != PlayerConfigType.PARTY_CLAIMS) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify a config without required permissions! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
				if(PlayerConfig.isOptionOPConfigurable(optionEntry.getId())) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify a op-only option! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
				if(t.getType() != PlayerConfigType.PARTY_CLAIMS && !Objects.equals(ownerId, serverPlayer.getUUID())) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify another player's config! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(serverPlayer.getServer());
			if(!isOP && t.getType() == PlayerConfigType.PARTY_CLAIMS &&
					!serverData.getPlayerPartySystemManager().canEditPartyConfig(serverPlayer.getUUID())
					){
				OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify party config without required permissions! Name: " + serverPlayer.getGameProfile().getName());
				return;
			}
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigManager();
			PlayerConfigOptionSpec<?> option =
					(PlayerConfigOptionSpec<?>) playerConfigs.getOptionForId(optionEntry.getId());
			if(option == null)
				return;
			if(!option.getConfigTypeFilter().test(t.getType())){
				OpenPartiesAndClaims.LOGGER.info("Player is attempting to modify a config option in a player config of type that doesn't allow the option! Name: " + serverPlayer.getGameProfile().getName());
				return;
			}
			IPlayerConfig config = ServerPlayerConfigUtils.getTargetConfig(ownerId, serverPlayer.getUUID(), t.getType(), playerConfigs);
			if(config == null)
				return;
			if(t.subId != null)
				config = config.getSubConfig(t.subId);
			if(config == null)
				return;
			if(!option.isSyncable())
				return;
			if(!option.isDirectlyConfigurable())
				return;
			Object value = null;
			try {
				value = option.getValueType().getSyncDecoder().apply(optionEntry.getValueTag());
			} catch(Throwable e){
			}
			IPlayerConfigAPI.SetResult result = setConfigUnchecked(config, option, value, serverPlayer);
			if (result == IPlayerConfigAPI.SetResult.SUCCESS)
				return;
			if (config.getType() != PlayerConfigType.PLAYER || serverPlayer.getUUID().equals(config.getPlayerId()))
				playerConfigs.getSynchronizer().syncOptionToClient(serverPlayer, config, option);//restore the correct value
		}
	}

}
