/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config.sync;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.config.*;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerConfigSynchronizer implements IPlayerConfigSynchronizer {
	
	private final MinecraftServer server;
	private PlayerConfigManager<?, ?> configManager;
	
	public PlayerConfigSynchronizer(MinecraftServer server) {
		this.server = server;
	}

	public void setConfigManager(PlayerConfigManager<?, ?> configManager) {
		if(this.configManager != null)
			throw new IllegalAccessError();
		this.configManager = configManager;
	}

	private void sendToClient(ServerPlayer player, Object packet) {
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
	}
	
	private <T extends Comparable<T>> PlayerConfigOptionValuePacket.Entry getPacketOptionEntry(ServerPlayer player, PlayerConfig<?> syncedConfig, IPlayerConfigOptionSpecAPI<T> option, boolean afterReset) {
		boolean isOp = player.hasPermissions(2);
		boolean mutable = isOp && syncedConfig.getType() != PlayerConfigType.PLAYER;
		boolean defaulted = !mutable && syncedConfig.getType() == PlayerConfigType.PLAYER;
		if(defaulted) {
			mutable = PlayerConfig.isPlayerConfigurable(option);
			defaulted = !mutable;
			if(defaulted) {
				defaulted = !PlayerConfig.isOptionOPConfigurable(option);
				mutable = !defaulted && isOp;
			}
		}
		T value = syncedConfig.getRaw(option);
		if(mutable && syncedConfig instanceof PlayerSubConfig && !configManager.getOverridableOptions().contains(option)) {
			mutable = false;
			value = null;
		}
		if(afterReset && defaulted)
			return null;
		return new PlayerConfigOptionValuePacket.Entry(option.getId(), option.getType(), defaulted ? null : value, mutable, defaulted);
	}
	
	private void syncOptionsToClient(ServerPlayer player, PlayerConfig<?> config, List<PlayerConfigOptionValuePacket.Entry> entries) {
		UUID ownerId = Objects.equals(player.getUUID(), config.getPlayerId()) ? null : config.getPlayerId();
		ClientboundPlayerConfigOptionValuePacket packet = new ClientboundPlayerConfigOptionValuePacket(config.getType(), config.getSubId(), ownerId, entries);
		sendToClient(player, packet);
	}
	
	public <T extends Comparable<T>> void syncOptionToClient(ServerPlayer player, IPlayerConfig config, IPlayerConfigOptionSpecAPI<T> option) {
		PlayerConfigOptionValuePacket.Entry packetOptionEntry = getPacketOptionEntry(player, (PlayerConfig<?>)config, option, false);
		if(packetOptionEntry != null)
			syncOptionsToClient(player, (PlayerConfig<?>)config, Lists.newArrayList(packetOptionEntry));
	}

	public void syncToClient(ServerPlayer player, PlayerConfig<?> config, boolean afterReset) {
		List<PlayerConfigOptionValuePacket.Entry> entries = new ArrayList<>(PlayerConfigOptions.OPTIONS.size());
		configManager.getAllOptionsStream().forEach(option -> {
			PlayerConfigOptionValuePacket.Entry packetOptionEntry = getPacketOptionEntry(player, config, (PlayerConfigOptionSpec<?>)option, afterReset);
			if(packetOptionEntry != null)
				entries.add(packetOptionEntry);
		});
		syncOptionsToClient(player, config, entries);
		if(config.getType() == PlayerConfigType.PLAYER && !player.getUUID().equals(config.getPlayerId())) {
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
			playerData.setLastOtherConfigRequest(config.getPlayerId());
		}
		syncGeneralState(player, config);
	}

	@Override
	public void syncAllToClient(ServerPlayer player) {
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		playerData.getConfigSyncSpreadoutTask().addConfigToSync(configManager.getDefaultConfig());
		playerData.getConfigSyncSpreadoutTask().addConfigToSync(configManager.getLoadedConfig(player.getUUID()));
		playerData.getConfigSyncSpreadoutTask().addConfigToSync(configManager.getWildernessConfig());
		playerData.getConfigSyncSpreadoutTask().addConfigToSync(configManager.getServerClaimConfig());
		playerData.getConfigSyncSpreadoutTask().addConfigToSync(configManager.getExpiredClaimConfig());
	}

	@Override
	public void syncOnLogin(ServerPlayer player) {
		List<PlayerConfigOptionSpec<?>> dynamicOptionEntries = new ArrayList<>(configManager.getDynamicOptions().getOptions().size());
		configManager.getDynamicOptions().getOptions().values().forEach(
				option -> dynamicOptionEntries.add((PlayerConfigOptionSpec<?>) option));
		sendToClient(player, new ClientboundPlayerConfigDynamicOptionsPacket(dynamicOptionEntries));
		syncAllToClient(player);
	}

	public void sendSyncState(ServerPlayer player, PlayerConfig<?> config, boolean state){
		ClientboundPlayerConfigSyncStatePacket packet = new ClientboundPlayerConfigSyncStatePacket(config.getType(), !Objects.equals(player.getUUID(), config.getPlayerId()), state);
		sendToClient(player, packet);
	}

	public void forAllRelevantClients(IPlayerConfig config, Consumer<ServerPlayer> action) {
		PlayerList serverPlayerList = server.getPlayerList();
		if(config.getType() == PlayerConfigType.PLAYER) {
			ServerPlayer ownerPlayer = serverPlayerList.getPlayer(config.getPlayerId());
			if(ownerPlayer != null)
				action.accept(ownerPlayer);
		} else {
			List<ServerPlayer> allPlayers = serverPlayerList.getPlayers();
			for(ServerPlayer player : allPlayers)
				action.accept(player);
		}
	}
	
	public <T extends Comparable<T>> void syncOptionToClients(PlayerConfig<?> config, PlayerConfigOptionSpec<T> option) {
		forAllRelevantClients(config, player -> syncOptionToClient(player, config, option));
	}

	private void syncGeneralState(ServerPlayer player, IPlayerConfig config, ClientboundPlayerConfigGeneralStatePacket packetOtherPlayer, ClientboundPlayerConfigGeneralStatePacket packetNotOtherPlayer){
		ClientboundPlayerConfigGeneralStatePacket packet =
				config.getType() == PlayerConfigType.PLAYER && !Objects.equals(player.getUUID(), config.getPlayerId()) ?
					packetOtherPlayer : packetNotOtherPlayer;
		sendToClient(player, packet);
	}

	public void syncGeneralState(ServerPlayer player, IPlayerConfig config){
		String subId = config.getSubId();
		if(subId == null)
			subId = PlayerConfig.MAIN_SUB_ID;
		int subConfigLimit = config.getSubConfigLimit();
		ClientboundPlayerConfigGeneralStatePacket packetOtherPlayer =
				new ClientboundPlayerConfigGeneralStatePacket(config.getType(), true, subId, config.isBeingDeleted(), subConfigLimit);
		ClientboundPlayerConfigGeneralStatePacket packetNotOtherPlayer =
				new ClientboundPlayerConfigGeneralStatePacket(config.getType(), false, subId, config.isBeingDeleted(), subConfigLimit);
		if(player == null)
			forAllRelevantClients(config, p -> syncGeneralState(p, config, packetOtherPlayer, packetNotOtherPlayer));
		else
			syncGeneralState(player, config, packetOtherPlayer, packetNotOtherPlayer);
	}

	private void syncSubExistence(ServerPlayer player, PlayerSubConfig<?> config, boolean create, ClientboundPlayerConfigRemoveSubPacket removePacketOtherPlayer, ClientboundPlayerConfigRemoveSubPacket removePacketNotOtherPlayer) {
		if (create) {
			syncToClient(player, config, true);
			PlayerConfig<?> mainConfig = config.getMainConfig();
			confirmSubConfigCreationSync(player, mainConfig);
		} else {
			ClientboundPlayerConfigRemoveSubPacket packet =
					config.getType() == PlayerConfigType.PLAYER && !Objects.equals(player.getUUID(), config.getPlayerId()) ?
						removePacketOtherPlayer : removePacketNotOtherPlayer;
			sendToClient(player, packet);
		}
	}

	@Override
	public void syncSubExistence(ServerPlayer player, IPlayerConfig subConfig, boolean create){
		String subId = subConfig.getSubId();
		ClientboundPlayerConfigRemoveSubPacket packetOtherPlayer;
		ClientboundPlayerConfigRemoveSubPacket packetNotOtherPlayer;
		if(create){
			packetOtherPlayer = null;
			packetNotOtherPlayer = null;
		} else {
			packetOtherPlayer = new ClientboundPlayerConfigRemoveSubPacket(subConfig.getType(), true, subId);
			packetNotOtherPlayer = new ClientboundPlayerConfigRemoveSubPacket(subConfig.getType(), false, subId);
		}
		if(player != null)
			syncSubExistence(player, (PlayerSubConfig<?>) subConfig, create, packetOtherPlayer, packetNotOtherPlayer);
		else
			forAllRelevantClients(subConfig, p -> syncSubExistence(p, (PlayerSubConfig<?>) subConfig, create, packetOtherPlayer, packetNotOtherPlayer));
		if(create)
			configManager.getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesUpdate(subConfig);
		else
			configManager.getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesRemove(subConfig);
	}

	@Override
	public void confirmSubConfigCreationSync(ServerPlayer player, IPlayerConfig mainConfig){
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		if(!playerData.getConfigSyncSpreadoutTask().stillNeedsSyncing(mainConfig))//otherwise the status will be sent later
			sendSyncState(player, (PlayerConfig<?>) mainConfig, false);
	}

}
