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

package xaero.pac.common.server.player.config.sync;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.config.*;
import xaero.pac.common.packet.config.group.*;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.ServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.config.PlayerConfigPermissionUpdateData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerConfigSynchronizer implements IPlayerConfigSynchronizer {
	
	private final MinecraftServer server;
	private PlayerConfigManager<?, ?> configManager;
	private PlayerConfigType forcedConfigType;
	private final ClientboundPlayerConfigConfigurableOptionsPacket configurableOptionsPacket;
	
	public PlayerConfigSynchronizer(
			MinecraftServer server,
			ClientboundPlayerConfigConfigurableOptionsPacket configurableOptionsPacket
	) {
		this.server = server;
		this.configurableOptionsPacket = configurableOptionsPacket;
	}

	public void setConfigManager(PlayerConfigManager<?, ?> configManager) {
		if(this.configManager != null)
			throw new IllegalAccessError();
		this.configManager = configManager;
	}

	private void sendToClient(ServerPlayer player, Object packet) {
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
	}

	private void sendToClient(
			ServerPlayer player,
			IPlayerConfig config,
			Object packetOtherPlayer,
			Object packetNotOtherPlayer,
			Object packetPartyClaims
	){
		PlayerConfigType configType = getEffectiveType(config);
		Object packet =
				configType == PlayerConfigType.PLAYER && !Objects.equals(player.getUUID(), config.getPlayerId()) ?
				packetOtherPlayer : configType == PlayerConfigType.PARTY_CLAIMS ? packetPartyClaims :
									packetNotOtherPlayer;
		sendToClient(player, packet);
	}
	
	private <T> PlayerConfigOptionValuePacket.Entry getPacketOptionEntry(PlayerConfig<?> syncedConfig, PlayerConfigOptionSpec<T> option, boolean afterReset) {
		if(!option.isSyncable())
			return null;
		boolean canDefault = syncedConfig.getType() == PlayerConfigType.PLAYER;
		boolean opMutable = !canDefault;
		boolean playerMutable = false;
		T value = null;
		boolean isSub = syncedConfig instanceof PlayerSubConfig;
		if(!isSub || option.isOverridable()) {
			if(!opMutable){
				opMutable = PlayerConfig.isOptionOPConfigurable(option);
				playerMutable = !opMutable && PlayerConfig.isPlayerConfigurable(option);
			}
			if(playerMutable || opMutable)
				value = syncedConfig.getRaw(option);
		}
		if(afterReset){
			if(!opMutable && !playerMutable)
				return null;
			if(isSub && value == null)
				return null;
			if(!isSub && option.getDefaultValue().equals(value))
				return null;
		}
		return PlayerConfigOptionValuePacket.Entry.of(option, value);
	}
	
	private void syncOptionsToClient(ServerPlayer player, PlayerConfig<?> config, List<PlayerConfigOptionValuePacket.Entry> entries) {
		UUID ownerId = Objects.equals(player.getUUID(), config.getPlayerId()) ? null : config.getPlayerId();
		ClientboundPlayerConfigOptionValuePacket packet = new ClientboundPlayerConfigOptionValuePacket(getEffectiveType(config), config.getSubId(), ownerId, entries);
		sendToClient(player, packet);
	}
	
	public <T> void syncOptionToClient(ServerPlayer player, IPlayerConfig config, PlayerConfigOptionSpec<T> option) {
		PlayerConfigOptionValuePacket.Entry packetOptionEntry = getPacketOptionEntry((PlayerConfig<?>)config, option, false);
		if(packetOptionEntry != null)
			syncOptionsToClient(player, (PlayerConfig<?>)config, Lists.newArrayList(packetOptionEntry));
	}

	public void syncToClient(ServerPlayer player, PlayerConfig<?> config, boolean afterReset) {
		List<PlayerConfigOptionValuePacket.Entry> entries = new ArrayList<>(PlayerConfigOptions.OPTIONS.size());
		configManager.getAllOptionsStream().forEach(option -> {
			PlayerConfigOptionValuePacket.Entry packetOptionEntry = getPacketOptionEntry(config, (PlayerConfigOptionSpec<?>)option, afterReset);
			if(packetOptionEntry != null)
				entries.add(packetOptionEntry);
		});
		if(!(config instanceof PlayerSubConfig)){
			ServerPlayerConfigGroupManager groupManager = config.getPlayerGroups();
			syncGroupLimits(player, groupManager, config);
			for (ICustomPlayerConfigGroup group : groupManager.getAllCustom())
				syncGroupExistence(player, config, true, group.getId());
		}
		syncOptionsToClient(player, config, entries);
		if(getEffectiveType(config) == PlayerConfigType.PLAYER && !player.getUUID().equals(config.getPlayerId())) {
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
		PlayerConfig<?> partyOwnerConfig = configManager.getPartyOwnerConfig(player.getUUID());
		if(partyOwnerConfig != null)
			playerData.getConfigSyncSpreadoutTask().addConfigToSync(partyOwnerConfig, PlayerConfigType.PARTY_CLAIMS);
	}

	@Override
	public void syncOnLogin(ServerPlayer player) {
		sendToClient(player, configurableOptionsPacket);
		List<PlayerConfigOptionSpec<?>> dynamicOptionEntries = new ArrayList<>(configManager.getDynamicOptions().getOptions().size());
		configManager.getDynamicOptions().getOptions().values().forEach(
				option -> dynamicOptionEntries.add((PlayerConfigOptionSpec<?>) option));
		sendToClient(player, new ClientboundPlayerConfigDynamicOptionsPacket(dynamicOptionEntries));
		syncAllToClient(player);
	}

	@Override
	public void sendSyncState(ServerPlayer player, IPlayerConfig config, boolean state){
		PlayerConfigType configType = getEffectiveType(config);
		UUID ownerIdToUse = configType == PlayerConfigType.PLAYER && Objects.equals(player.getUUID(), config.getPlayerId()) ?
				null : config.getPlayerId();
		ClientboundPlayerConfigSyncStatePacket packet = new ClientboundPlayerConfigSyncStatePacket(
				configType, state, ownerIdToUse
		);
		sendToClient(player, packet);
	}

	public void sendGroupSyncState(ServerPlayer player, PlayerConfig<?> config, boolean state){
		PlayerConfigType configType = getEffectiveType(config);
		ClientboundPlayerConfigGroupsSyncStatePacket packet = new ClientboundPlayerConfigGroupsSyncStatePacket(configType,
				configType == PlayerConfigType.PLAYER && !Objects.equals(player.getUUID(), config.getPlayerId()), state);
		sendToClient(player, packet);
	}

	public void forAllRelevantClients(IPlayerConfig config, Consumer<ServerPlayer> action) {
		PlayerList serverPlayerList = server.getPlayerList();
		boolean isPartyClaimOwnershipMode = ServerConfig.CONFIG.partyOwnedClaims.get();
		boolean isPartyClaimsConfig = isPartyClaimOwnershipMode &&
				config.getManager().getPartySystemManager().isPrimaryPartyOwner(config.getPlayerId());
		if(!isPartyClaimsConfig && config.getType() == PlayerConfigType.PLAYER) {//purposely direct type
			ServerPlayer ownerPlayer = serverPlayerList.getPlayer(config.getPlayerId());
			if(ownerPlayer != null)
				action.accept(ownerPlayer);
			return;
		}
		List<ServerPlayer> allPlayers = serverPlayerList.getPlayers();
		for(ServerPlayer player : allPlayers) {
			PlayerConfigType forceConfigTypeBU = forcedConfigType;
			if(config.getType() == PlayerConfigType.PLAYER){//purposely direct type
				//isPartyClaimsConfig is implied here
				UUID playerPartyOwner = configManager.getPartySystemManager().getPrimaryPartyOwnerByMember(player.getUUID());
				if(!config.getPlayerId().equals(playerPartyOwner))
					continue;
				if(player.getUUID().equals(playerPartyOwner))
					action.accept(player);//call action as type PLAYER for the config owner before also calling as PARTY_CLAIMS
				forceConfigType(PlayerConfigType.PARTY_CLAIMS);
			}
			action.accept(player);
			forceConfigType(forceConfigTypeBU);
		}
	}
	
	public <T> void syncOptionToClients(PlayerConfig<?> config, PlayerConfigOptionSpec<T> option) {
		forAllRelevantClients(config, player -> syncOptionToClient(player, config, option));
	}

	@Override
	public void syncGeneralState(ServerPlayer player, IPlayerConfig config){
		String subId = config.getSubId();
		if(subId == null)
			subId = PlayerConfig.MAIN_SUB_ID;
		int subConfigLimit = config.getSubConfigLimit();
		ClientboundPlayerConfigGeneralStatePacket packetOtherPlayer =
				new ClientboundPlayerConfigGeneralStatePacket(PlayerConfigType.PLAYER, true, subId, config.isBeingDeleted(), subConfigLimit);
		ClientboundPlayerConfigGeneralStatePacket packetNotOtherPlayer =
				new ClientboundPlayerConfigGeneralStatePacket(config.getType(), false, subId, config.isBeingDeleted(), subConfigLimit);
		ClientboundPlayerConfigGeneralStatePacket packetPartyClaims =
				new ClientboundPlayerConfigGeneralStatePacket(PlayerConfigType.PARTY_CLAIMS, false, subId, config.isBeingDeleted(), subConfigLimit);
		if(player == null)
			forAllRelevantClients(config, p -> sendToClient(p, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims));
		else
			sendToClient(player, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims);
	}

	public void syncGroupExistence(ServerPlayer player, IPlayerConfig config, boolean add, String groupId){
		PlayerConfigGroupExistencePacket packetOtherPlayer =
				new PlayerConfigGroupExistencePacket(PlayerConfigType.PLAYER, config.getPlayerId(), groupId, add);
		PlayerConfigGroupExistencePacket packetNotOtherPlayer =
				new PlayerConfigGroupExistencePacket(config.getType(), null, groupId, add);
		PlayerConfigGroupExistencePacket packetPartyClaims =
				new PlayerConfigGroupExistencePacket(PlayerConfigType.PARTY_CLAIMS, null, groupId, add);
		if(player == null)
			forAllRelevantClients(config, p -> sendToClient(p, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims));
		else
			sendToClient(player, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims);
	}

	public void syncGroupMemberUpdate(
			ServerPlayer player,
			IPlayerConfig config,
			String groupId,
			PlayerConfigGroupMemberPacket.Action action,
			UUID playerId,
			String playerName
	){
		PlayerConfigGroupMemberPacket packetOtherPlayer =
				new PlayerConfigGroupMemberPacket(
						PlayerConfigType.PLAYER, config.getPlayerId(),
						groupId, action,
						playerId, playerName
				);
		PlayerConfigGroupMemberPacket packetNotOtherPlayer =
				new PlayerConfigGroupMemberPacket(
						config.getType(), null,
						groupId, action,
						playerId, playerName
				);
		PlayerConfigGroupMemberPacket packetPartyClaims =
				new PlayerConfigGroupMemberPacket(
						PlayerConfigType.PARTY_CLAIMS, null,
						groupId, action,
						playerId, playerName
				);
		if(player == null)
			forAllRelevantClients(config, p -> sendToClient(p, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims));
		else
			sendToClient(player, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims);
	}

	public void syncGroupGroupUpdate(
			  ServerPlayer player,
			  IPlayerConfig config,
			  String groupId,
			  PlayerConfigGroupGroupPacket.Action action,
			  String inclusionGroupId
	){
		PlayerConfigGroupGroupPacket packetOtherPlayer =
				new PlayerConfigGroupGroupPacket(
						PlayerConfigType.PLAYER, config.getPlayerId(),
						groupId, action, inclusionGroupId
				);
		PlayerConfigGroupGroupPacket packetNotOtherPlayer =
				new PlayerConfigGroupGroupPacket(
						config.getType(), null,
						groupId, action, inclusionGroupId
				);
		PlayerConfigGroupGroupPacket packetPartyClaims =
				new PlayerConfigGroupGroupPacket(
						PlayerConfigType.PARTY_CLAIMS, null,
						groupId, action, inclusionGroupId
				);
		if(player == null)
			forAllRelevantClients(config, p -> sendToClient(p, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims));
		else
			sendToClient(player, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims);
	}

	public void syncGroupsReset(ServerPlayer player, IPlayerConfig config){
		ClientboundPlayerConfigResetGroupsPacket packetOtherPlayer =
				new ClientboundPlayerConfigResetGroupsPacket(PlayerConfigType.PLAYER, config.getPlayerId());
		ClientboundPlayerConfigResetGroupsPacket packetNotOtherPlayer =
				new ClientboundPlayerConfigResetGroupsPacket(config.getType(), null);
		ClientboundPlayerConfigResetGroupsPacket packetPartyClaims =
				new ClientboundPlayerConfigResetGroupsPacket(PlayerConfigType.PARTY_CLAIMS, null);
		if(player == null)
			forAllRelevantClients(config, p -> sendToClient(p, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims));
		else
			sendToClient(player, config, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims);
	}

	@Override
	public void syncGroupLimits(ServerPlayer player, IServerPlayerConfigGroupManager groupManager, IPlayerConfig config){
		ClientboundPlayerConfigGroupLimitsPacket limitsPacket = new ClientboundPlayerConfigGroupLimitsPacket(
				config.getType(), false, groupManager.getMaxGroups(), groupManager.getGroupSpace()
		);
		ClientboundPlayerConfigGroupLimitsPacket limitsPacketOtherPlayer = new ClientboundPlayerConfigGroupLimitsPacket(
				PlayerConfigType.PLAYER, true, groupManager.getMaxGroups(), groupManager.getGroupSpace()
		);
		ClientboundPlayerConfigGroupLimitsPacket limitsPacketPartyClaims = new ClientboundPlayerConfigGroupLimitsPacket(
				PlayerConfigType.PARTY_CLAIMS, true, groupManager.getMaxGroups(), groupManager.getGroupSpace()
		);
		if(player == null)
			forAllRelevantClients(config, p -> sendToClient(p, config, limitsPacketOtherPlayer, limitsPacket, limitsPacketPartyClaims));
		else
			sendToClient(player, config, limitsPacketOtherPlayer, limitsPacket, limitsPacketPartyClaims);
	}

	private void syncSubExistence(
			ServerPlayer player,
			PlayerSubConfig<?> config,
			boolean create,
			ClientboundPlayerConfigRemoveSubPacket removePacketOtherPlayer,
			ClientboundPlayerConfigRemoveSubPacket removePacketNotOtherPlayer,
			ClientboundPlayerConfigRemoveSubPacket removePacketPartyClaims) {
		if (create) {
			syncToClient(player, config, true);
			PlayerConfig<?> mainConfig = config.getMain();
			confirmSubConfigCreationSync(player, mainConfig);
			return;
		}
		sendToClient(player, config, removePacketOtherPlayer, removePacketNotOtherPlayer, removePacketPartyClaims);
	}

	@Override
	public void syncSubExistence(ServerPlayer player, IPlayerConfig subConfig, boolean create){
		String subId = subConfig.getSubId();
		ClientboundPlayerConfigRemoveSubPacket packetOtherPlayer;
		ClientboundPlayerConfigRemoveSubPacket packetNotOtherPlayer;
		ClientboundPlayerConfigRemoveSubPacket packetPartyClaims;
		if(create){
			packetOtherPlayer = null;
			packetNotOtherPlayer = null;
			packetPartyClaims = null;
		} else {
			packetOtherPlayer = new ClientboundPlayerConfigRemoveSubPacket(PlayerConfigType.PLAYER, true, subId);
			packetNotOtherPlayer = new ClientboundPlayerConfigRemoveSubPacket(subConfig.getType(), false, subId);
			packetPartyClaims = new ClientboundPlayerConfigRemoveSubPacket(PlayerConfigType.PARTY_CLAIMS, false, subId);
		}
		if(player != null)
			syncSubExistence(player, (PlayerSubConfig<?>) subConfig, create, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims);
		else
			forAllRelevantClients(subConfig, p -> syncSubExistence(p, (PlayerSubConfig<?>) subConfig, create, packetOtherPlayer, packetNotOtherPlayer, packetPartyClaims));
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

	@Override
	public void requestPartyClaimsConfigSync(IPlayerConfig partyConfig, ServerPlayer player){
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		IPlayerConfig playerConfig = configManager.getLoadedConfig(player.getUUID());
		if(partyConfig != null) {
			PlayerConfigType forceConfigTypeBU = forcedConfigType;
			forceConfigType(PlayerConfigType.PARTY_CLAIMS);
			sendSyncState(player, partyConfig, true);
			forceConfigType(forceConfigTypeBU);
		}
		configManager.getClaimsManager().getClaimsManagerSynchronizer().syncPartyGeneral(playerConfig, player);
		if(partyConfig == null)
			return;
		playerData.getConfigSyncSpreadoutTask().addConfigToSync(partyConfig, PlayerConfigType.PARTY_CLAIMS);
	}

	@Override
	public void sendPermissions(ServerPlayer player, PlayerConfigType type, PlayerConfigPermissionUpdateData data) {
		ClientboundPlayerConfigPermissionsPacket packet = new ClientboundPlayerConfigPermissionsPacket(
				forcedConfigType != null ? forcedConfigType : type, data.canView(), data.canEdit(),
				data.canIncludePlayersInGroups(), data.canIncludeGroupsInGroups(),
				data.canCreateGroups(), data.canClaimAs()
		);
		sendToClient(player, packet);
	}

	@Override
	public void sendPermissions(ServerPlayer player, PlayerConfigType type) {
		PlayerConfigType effectiveType = forcedConfigType == null ? type : forcedConfigType;
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		PlayerConfigPermissionUpdateData data = playerData.getPlayerConfigPermissionUpdateData(effectiveType);
		sendPermissions(player, type, data);
	}

	@Override
	public void syncAdmin(ServerPlayer player, boolean admin) {
		ClientboundPlayerConfigAdminPacket packet = new ClientboundPlayerConfigAdminPacket(admin);
		sendToClient(player, packet);
	}

	@Override
	public void addConfigToSync(ServerPlayer player, IPlayerConfig config){
		if(player != null){
			sendSyncState(player, config, true);
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
			playerData.getConfigSyncSpreadoutTask().addConfigToSync(config, forcedConfigType);
			return;
		}
		forAllRelevantClients(config, p -> addConfigToSync(p, config));
	}

	private PlayerConfigType getEffectiveType(IPlayerConfig config){
		if(forcedConfigType != null)
			return forcedConfigType;
		return config.getType();
	}

	@Override
	public void forceConfigType(PlayerConfigType forceConfigType) {
		this.forcedConfigType = forceConfigType;
	}

}
