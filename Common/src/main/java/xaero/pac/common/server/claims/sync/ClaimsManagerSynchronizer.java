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

package xaero.pac.common.server.claims.sync;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.packet.ClientboundLoadingPacket;
import xaero.pac.common.packet.claims.*;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.*;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.lazypacket.LazyPacket;
import xaero.pac.common.server.lazypacket.task.schedule.LazyPacketScheduleTaskHandler;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ClaimsManagerSynchronizer implements IClaimsManagerSynchronizer {
	
	public static final int REGIONS_PER_TICK = 8192;
	public static final int REGIONS_PER_TICK_PER_PLAYER = 128;
	public static final int STATES_PER_TICK = 16384 * 3;//multiplied by 3 because 1 state sync is up to 3 steps
	public static final int STATES_PER_TICK_PER_PLAYER = 1024 * 3;//multiplied by 3 because 1 state sync is up to 3 steps
	public static final int OWNER_PROPERTIES_PER_TICK = 24576;
	public static final int OWNER_PROPERTIES_PER_TICK_PER_PLAYER = 1536;

	public static final int SUBCLAIM_PROPERTIES_PER_TICK = 16384;
	public static final int SUBCLAIM_PROPERTIES_PER_TICK_PER_PLAYER = 1024;
	
	private final MinecraftServer server;
	private ServerClaimsManager claimsManager;
	private IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?> serverData;
	private final List<LazyPacketScheduleTaskHandler> schedulers;

	private ClaimsManagerSynchronizer(MinecraftServer server, List<LazyPacketScheduleTaskHandler> schedulers) {
		super();
		this.server = server;
		this.schedulers = schedulers;
	}
	
	@SuppressWarnings("unchecked")
	public void setServerData(IServerData<?,?> serverData) {
		if(this.serverData != null)
			throw new IllegalAccessError();
		this.serverData = (IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?>) serverData;
	}

	public void setClaimsManager(ServerClaimsManager claimsManager) {
		if(this.claimsManager != null)
			throw new IllegalAccessError();
		this.claimsManager = claimsManager;
	}

	private void sendToClient(ServerPlayer player, Object packet, boolean instant) {
		if(instant)
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
		else
			serverData.getServerTickHandler().getLazyPacketSender().enqueue(player, (LazyPacket<?, ?>) packet);
	}
	
	private void startSyncing(ServerPlayer player) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		sendToClient(player, ClientboundLoadingPacket.START_CLAIMS, false);
	}
	
	public void endSyncing(ServerPlayer player) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		sendToClient(player, ClientboundLoadingPacket.END_CLAIMS, false);
	}

	public void trySyncClaimLimits(IPlayerConfigManager configManager, UUID playerId) {
		ServerPlayer player = server.getPlayerList().getPlayer(playerId);
		if(player != null){
			IPlayerConfig config = configManager.getLoadedConfig(playerId);
			syncClaimLimits(config, player);
		}
	}

	@Override
	public void syncClaimLimits(IPlayerConfig config, ServerPlayer player) {
		int claimsLimit = claimsManager.getPlayerBaseClaimLimit(player) + config.getEffective(PlayerConfigOptions.BONUS_CHUNK_CLAIMS);
		int forceloadLimit = claimsManager.getPlayerBaseForceloadLimit(player) + config.getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);
		int maxClaimDistance = ServerConfig.CONFIG.maxClaimDistance.get();
		boolean alwaysUseLoadingValues = ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED;
		IServerPlayerClaimInfo<?> playerInfo = claimsManager.getPlayerInfo(player.getUUID());
		sendToClient(player, new ClientboundClaimLimitsPacket(playerInfo.getClaimCount(), playerInfo.getForceloadCount(), claimsLimit, forceloadLimit, maxClaimDistance, alwaysUseLoadingValues), false);
	}

	@Override
	public void syncCurrentSubClaim(IPlayerConfig config, ServerPlayer player) {
		int currentSubConfigIndex = config.getUsedSubConfig().getSubIndex();
		int currentServerSubConfigIndex = config.getUsedServerSubConfig().getSubIndex();
		String currentSubConfigId = config.getUsedSubConfig().getSubId();
		if(currentSubConfigId == null)
			currentSubConfigId = PlayerConfig.MAIN_SUB_ID;
		String currentServerSubConfigId = config.getUsedServerSubConfig().getSubId();
		if(currentServerSubConfigId == null)
			currentServerSubConfigId = PlayerConfig.MAIN_SUB_ID;
		sendToClient(player, new ClientboundCurrentSubClaimPacket(currentSubConfigIndex, currentServerSubConfigIndex, currentSubConfigId, currentServerSubConfigId), false);
	}

	public Iterator<ServerPlayerClaimInfo> getClaimPropertiesToSync(ServerPlayer player){
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return List.of(claimsManager.getPlayerInfo(player.getUUID())).iterator();
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.ALL) {
			return claimsManager.getPlayerInfoIterator();
		} else
			return List.of(claimsManager.getPlayerInfo(player.getUUID()), claimsManager.getPlayerInfo(PlayerConfig.SERVER_CLAIM_UUID)).iterator();
	}

	public void syncClaimOwnerProperties(List<ClientboundClaimOwnerPropertiesPacket.PlayerProperties> packetBuilder, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimOwnerPropertiesPacket(packetBuilder), false);
	}

	public void syncSubClaimProperties(List<ClientboundSubClaimPropertiesPacket.SubClaimProperties> packetBuilder, ServerPlayer player) {
		sendToClient(player, new ClientboundSubClaimPropertiesPacket(packetBuilder), false);
	}

	private boolean claimInfoShouldReachEveryone(ServerConfig.ClaimsSyncType syncType, UUID playerId){
		return syncType == ServerConfig.ClaimsSyncType.ALL || Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, playerId);
	}

	@Override
	public void syncToPlayersClaimOwnerPropertiesUpdate(IServerPlayerClaimInfo<?> playerInfo) {
		ServerConfig.ClaimsSyncType syncType = ServerConfig.CONFIG.claimsSynchronization.get();
		if(syncType == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		PlayerList players = server.getPlayerList();
		List<ClientboundClaimOwnerPropertiesPacket.PlayerProperties> packetBuilder =
				Lists.newArrayList(new ClientboundClaimOwnerPropertiesPacket.PlayerProperties(playerInfo.getPlayerId(), playerInfo.getPlayerUsername()));
		if(syncType == ServerConfig.ClaimsSyncType.ALL || Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, playerInfo.getPlayerId())) {
			for(ServerPlayer player : players.getPlayers())
				syncClaimOwnerProperties(packetBuilder, player);
		} else {
			ServerPlayer selfPlayer = players.getPlayer(playerInfo.getPlayerId());
			if(selfPlayer != null)
				syncClaimOwnerProperties(packetBuilder, selfPlayer);
		}
	}

	public ClientboundSubClaimPropertiesPacket.SubClaimProperties getSubClaimPropertiesForSync(IPlayerConfig subConfig, boolean afterReset){
		String claimsName;
		Integer claimsColor;
		if(subConfig instanceof PlayerSubConfig) {
			claimsName = subConfig.getRaw(PlayerConfigOptions.CLAIMS_NAME);
			claimsColor = subConfig.applyDefaultReplacer(PlayerConfigOptions.CLAIMS_COLOR, subConfig.getRaw(PlayerConfigOptions.CLAIMS_COLOR));
		} else {
			claimsName = subConfig.getEffective(PlayerConfigOptions.CLAIMS_NAME);
			claimsColor = subConfig.getEffective(PlayerConfigOptions.CLAIMS_COLOR);
		}
		if(afterReset && claimsName == null && claimsColor == null)
			return null;
		return new ClientboundSubClaimPropertiesPacket.SubClaimProperties(subConfig.getPlayerId(), subConfig.getSubIndex(), claimsName, claimsColor);
	}

	@Override
	public void syncToPlayersSubClaimPropertiesUpdate(IPlayerConfig subConfig) {
		ServerConfig.ClaimsSyncType syncType = ServerConfig.CONFIG.claimsSynchronization.get();
		if(syncType == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		PlayerList players = server.getPlayerList();
		List<ClientboundSubClaimPropertiesPacket.SubClaimProperties> packetBuilder =
				Lists.newArrayList(getSubClaimPropertiesForSync(subConfig, false));
		if(claimInfoShouldReachEveryone(syncType, subConfig.getPlayerId())) {
			for(ServerPlayer player : players.getPlayers())
				syncSubClaimProperties(packetBuilder, player);
		} else {
			ServerPlayer selfPlayer = players.getPlayer(subConfig.getPlayerId());
			if(selfPlayer != null)
				syncSubClaimProperties(packetBuilder, selfPlayer);
		}
	}

	@Override
	public void syncToPlayersSubClaimPropertiesRemove(IPlayerConfig subConfig) {
		ServerConfig.ClaimsSyncType syncType = ServerConfig.CONFIG.claimsSynchronization.get();
		if(syncType == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		PlayerList players = server.getPlayerList();
		ClientboundRemoveSubClaimPacket packet = new ClientboundRemoveSubClaimPacket(subConfig.getPlayerId(), subConfig.getSubIndex());
		if(claimInfoShouldReachEveryone(syncType, subConfig.getPlayerId())) {
			for(ServerPlayer player : players.getPlayers())
				sendToClient(player, packet, false);
		} else {
			ServerPlayer selfPlayer = players.getPlayer(subConfig.getPlayerId());
			if(selfPlayer != null)
				sendToClient(selfPlayer, packet, false);
		}
	}
	
	public void syncClaimStates(List<PlayerChunkClaim> packetBuilder, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimStatesPacket(packetBuilder), false);
	}
	
	public void syncDimensionIdToClient(ResourceLocation dimension, ServerPlayer player) {
		sendToClient(player, new ClientboundPlayerClaimsDimensionPacket(dimension), false);
	}
	
	public void syncRegionClaimsToClient(int x, int z, int[] paletteInts, long[] storageData, int storageBits, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimsRegionPacket(x, z, paletteInts, storageBits, storageData), false);
	}

	private void sendClaimUpdatePacketToPlayer(ServerPlayer player, PlayerChunkClaim claim, ResourceLocation dimension, ClientboundClaimsClaimUpdatePacket packet, ClientboundClaimsClaimUpdatePosPacket posPacket){
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		if(playerData.getLastClaimUpdateState() == claim && playerData.getLastClaimUpdateDimension() == dimension) {
			sendToClient(player, posPacket, false);
			return;
		}
		sendToClient(player, packet, false);
		playerData.setLastClaimUpdate(dimension, claim);
	}
	
	public void syncToPlayersClaimUpdate(ResourceLocation dimension, int x, int z, PlayerChunkClaim claim, PlayerChunkClaim oldClaim) {
		ServerConfig.ClaimsSyncType syncType = ServerConfig.CONFIG.claimsSynchronization.get();
		if(syncType == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		PlayerList players = server.getPlayerList();
		UUID newPlayerId = claim == null ? null : claim.getPlayerId();
		int newSubConfigIndex = claim == null ? -1 : claim.getSubConfigIndex();
		ClientboundClaimsClaimUpdatePacket packet = new ClientboundClaimsClaimUpdatePacket(dimension, x, z, newPlayerId, newSubConfigIndex, claim != null && claim.isForceloadable(), claim != null ? claim.getSyncIndex() : -1);
		ClientboundClaimsClaimUpdatePosPacket posPacket = new ClientboundClaimsClaimUpdatePosPacket(x, z);
		if(claimInfoShouldReachEveryone(syncType, newPlayerId)) {//new claim is visible to all
			for(ServerPlayer player : players.getPlayers())
				sendClaimUpdatePacketToPlayer(player, claim, dimension, packet, posPacket);
		} else {
			UUID oldPlayerId = oldClaim == null ? null : oldClaim.getPlayerId();
			if(oldPlayerId != null && !Objects.equals(newPlayerId, oldPlayerId)) {
				ClientboundClaimsClaimUpdatePacket removalPacket = new ClientboundClaimsClaimUpdatePacket(dimension, x, z, null, -1, false, -1);
				if(Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, oldPlayerId)) {//old claim is visible to all
					for(ServerPlayer player : players.getPlayers())
						sendClaimUpdatePacketToPlayer(player, null, dimension, removalPacket, posPacket);
				} else {
					ServerPlayer oldPlayer = players.getPlayer(oldPlayerId);
					if(oldPlayer != null)
						sendClaimUpdatePacketToPlayer(oldPlayer, null, dimension, removalPacket, posPacket);
				}
			}
			if(newPlayerId != null) {
				ServerPlayer newPlayer = players.getPlayer(newPlayerId);
				if(newPlayer != null)
					sendClaimUpdatePacketToPlayer(newPlayer, claim, dimension, packet, posPacket);
			}
		}
	}

	public void syncToPlayersRemoveClaimState(PlayerChunkClaim state) {
		ServerConfig.ClaimsSyncType syncType = ServerConfig.CONFIG.claimsSynchronization.get();
		if(syncType == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		PlayerList players = server.getPlayerList();
		ClientboundRemoveClaimStatePacket packet = new ClientboundRemoveClaimStatePacket(state.getSyncIndex());
		if(claimInfoShouldReachEveryone(syncType, state.getPlayerId())) {
			for(ServerPlayer player : players.getPlayers())
				sendToClient(player, packet, false);
		} else {
			ServerPlayer player = players.getPlayer(state.getPlayerId());
			if(player != null)
				sendToClient(player, packet, false);
		}
	}
	
	public void syncToPlayerClaimActionResult(AreaClaimResult result, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimResultPacket(result), true);
	}

	@Override
	public void syncOnLogin(ServerPlayer player) {
		IPlayerConfigManager configManager = serverData.getPlayerConfigs();
		IPlayerConfig config = configManager.getLoadedConfig(player.getUUID());
		startSyncing(player);
		syncClaimLimits(config, player);
		syncCurrentSubClaim(config, player);

		sendToClient(player, new ClaimRegionsStartPacket(), false);
	}

	public Iterator<ServerClaimStateHolder> getStateHolderIteratorForSync(){
		return claimsManager.getClaimStateHolderIterator();
	}

	@Override
	public void onLazyPacketsDropped(ServerPlayer player){
		schedulers.forEach(s -> s.onLazyPacketsDropped(player));
	}

	@Override
	public void onServerTick() {
		schedulers.forEach(s -> s.onTick(serverData));
	}

	public static final class Builder {

		private MinecraftServer server;

		private Builder(){}

		public Builder setDefault() {
			setServer(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public ClaimsManagerSynchronizer build(){
			if(server == null)
				throw new IllegalStateException();

			LazyPacketScheduleTaskHandler claimOwnerPropertiesScheduler = LazyPacketScheduleTaskHandler.Builder.begin()
					.setPlayerTaskGetter(ServerPlayerData::getClaimsManagerPlayerClaimOwnerPropertiesSync)
					.setPerTickLimit(OWNER_PROPERTIES_PER_TICK)
					.setPerTickPerTaskLimit(OWNER_PROPERTIES_PER_TICK_PER_PLAYER).build();

			LazyPacketScheduleTaskHandler subClaimPropertiesScheduler = LazyPacketScheduleTaskHandler.Builder.begin()
					.setPlayerTaskGetter(ServerPlayerData::getClaimsManagerPlayerSubClaimPropertiesSync)
					.setPerTickLimit(SUBCLAIM_PROPERTIES_PER_TICK)
					.setPerTickPerTaskLimit(SUBCLAIM_PROPERTIES_PER_TICK_PER_PLAYER).build();

			LazyPacketScheduleTaskHandler claimStateScheduler = LazyPacketScheduleTaskHandler.Builder.begin()
					.setPlayerTaskGetter(ServerPlayerData::getClaimsManagerPlayerStateSync)
					.setPerTickLimit(STATES_PER_TICK)
					.setPerTickPerTaskLimit(STATES_PER_TICK_PER_PLAYER).build();

			//owned-only sync ironically might require more work per region, so cut the frequency a bit
			int regionsPerTick = ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.OWNED_ONLY ? REGIONS_PER_TICK * 2 / 3 : REGIONS_PER_TICK;
			int regionsPerTickPerPlayer = ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.OWNED_ONLY ? REGIONS_PER_TICK_PER_PLAYER * 2 / 3 : REGIONS_PER_TICK_PER_PLAYER;
			LazyPacketScheduleTaskHandler regionScheduler = LazyPacketScheduleTaskHandler.Builder.begin()
					.setPlayerTaskGetter(ServerPlayerData::getClaimsManagerPlayerRegionSync)
					.setPerTickLimit(regionsPerTick)
					.setPerTickPerTaskLimit(regionsPerTickPerPlayer).build();

			List<LazyPacketScheduleTaskHandler> schedulers = List.of(claimOwnerPropertiesScheduler, subClaimPropertiesScheduler, claimStateScheduler, regionScheduler);
			return new ClaimsManagerSynchronizer(server, schedulers);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}
	
}
