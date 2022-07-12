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
import xaero.pac.common.packet.claims.owned.ClientboundPlayerClaimsOwnedClaimPacket;
import xaero.pac.common.packet.claims.owned.ClientboundPlayerClaimsOwnerPacket;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.lazypackets.LazyPacket;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.*;
import java.util.Map.Entry;

public class ClaimsManagerSynchronizer implements IClaimsManagerSynchronizer {
	
	public static final int REGIONS_PER_TICK = 1024;
	public static final int REGIONS_PER_TICK_PER_PLAYER = 16;
	
	protected final MinecraftServer server;
	private ServerClaimsManager claimsManager;
	protected IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?> serverData;
	
	public ClaimsManagerSynchronizer(MinecraftServer server) {
		super();
		this.server = server;
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

	protected void sendToClient(ServerPlayer player, Object packet, boolean instant) {
		if(instant)
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
		else
			serverData.getServerTickHandler().getLazyPacketSender().enqueue(player, (LazyPacket<?, ?>) packet);
	}
	
	private void syncOwnedToClient(ServerPlayer player) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		syncOwnedToClient(player.getUUID(), player);
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.OWNED_ONLY)
			syncOwnedToClient(PlayerConfig.SERVER_CLAIM_UUID, player);
	}
	
	private void syncOwnedToClient(UUID playerId, ServerPlayer player) {
		ServerPlayerClaimInfo playerClaimInfo = claimsManager.getPlayerInfo(playerId);
		sendToClient(player, new ClientboundPlayerClaimsOwnerPacket(playerId), false);
		playerClaimInfo.getStream().map(Entry::getValue).forEach(dimensionClaims -> {
			if(!ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() && !claimsManager.isClaimable(dimensionClaims.getDimension()))
				return;
			sendToClient(player, new ClientboundPlayerClaimsDimensionPacket(dimensionClaims.getDimension()), false);
			dimensionClaims.getStream().forEach(posList ->
				posList.getStream().forEach(
						pos -> sendToClient(player, new ClientboundPlayerClaimsOwnedClaimPacket(pos.x, pos.z, posList.getClaimState().isForceloadable()), false)
						)
			);
		});
	}
	
	private void syncClaimProperties(List<ClientboundClaimPropertiesPacket.PlayerProperties> packetBuilder, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimPropertiesPacket(packetBuilder), false);
	}
	
	private void buildClaimPropertiesPacket(List<ClientboundClaimPropertiesPacket.PlayerProperties> packetBuilder, ServerPlayerClaimInfo pi, ServerPlayer player) {
		UUID playerId = pi.getPlayerId();
		String username = pi.getPlayerUsername();
		String claimsName = pi.getClaimsName();
		int claimsColor = pi.getClaimsColor();
		packetBuilder.add(new ClientboundClaimPropertiesPacket.PlayerProperties(playerId, username, claimsName, claimsColor));
		if(packetBuilder.size() == ClientboundClaimPropertiesPacket.MAX_PROPERTIES) {
			syncClaimProperties(packetBuilder, player);
			packetBuilder.clear();
		}
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

	@Override
	public void syncClaimLimits(IPlayerConfig config, ServerPlayer player) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		int claimsLimit = claimsManager.getPlayerBaseClaimLimit(player) + config.getEffective(PlayerConfig.BONUS_CHUNK_CLAIMS);
		int forceloadLimit = claimsManager.getPlayerBaseForceloadLimit(player) + config.getEffective(PlayerConfig.BONUS_CHUNK_FORCELOADS);
		int maxClaimDistance = ServerConfig.CONFIG.maxClaimDistance.get();
		IServerPlayerClaimInfo<?> playerInfo = claimsManager.getPlayerInfo(player.getUUID());
		sendToClient(player, new ClientboundClaimLimitsPacket(playerInfo.getClaimCount(), playerInfo.getForceloadCount(), claimsLimit, forceloadLimit, maxClaimDistance), false);
	}
	
	private void syncPlayerClaimProperties(ServerPlayer player) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		List<ClientboundClaimPropertiesPacket.PlayerProperties> packetBuilder = new ArrayList<>(ClientboundClaimPropertiesPacket.MAX_PROPERTIES);
		
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.ALL) {
			Iterator<ServerPlayerClaimInfo> iterator = claimsManager.getPlayerInfoStream().iterator();
			while(iterator.hasNext())
				buildClaimPropertiesPacket(packetBuilder, iterator.next(), player);
		} else {
			buildClaimPropertiesPacket(packetBuilder, claimsManager.getPlayerInfo(player.getUUID()), player);
			buildClaimPropertiesPacket(packetBuilder, claimsManager.getPlayerInfo(PlayerConfig.SERVER_CLAIM_UUID), player);
		}
		
		if(!packetBuilder.isEmpty())
			syncClaimProperties(packetBuilder, player);
	}

	@Override
	public void syncToPlayersClaimPropertiesUpdate(IServerPlayerClaimInfo<?> playerInfo) {
		PlayerList players = server.getPlayerList();
		List<ClientboundClaimPropertiesPacket.PlayerProperties> packetBuilder = 
				Lists.newArrayList(new ClientboundClaimPropertiesPacket.PlayerProperties(playerInfo.getPlayerId(), playerInfo.getPlayerUsername(), playerInfo.getClaimsName(), playerInfo.getClaimsColor()));
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.ALL || Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, playerInfo.getPlayerId())) {
			for(ServerPlayer player : players.getPlayers())
				syncClaimProperties(packetBuilder, player);
		} else if(ServerConfig.CONFIG.claimsSynchronization.get() != ServerConfig.ClaimsSyncType.NOT_SYNCED) {
			ServerPlayer selfPlayer = players.getPlayer(playerInfo.getPlayerId());
			if(selfPlayer != null)
				syncClaimProperties(packetBuilder, selfPlayer);
		}
	}
	
	private void syncClaimStates(List<PlayerChunkClaim> packetBuilder, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimStatesPacket(packetBuilder), false);
	}
	
	private void syncClaimStatesToClient(ServerPlayer player) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() != ServerConfig.ClaimsSyncType.ALL)
			return;
		Iterator<PlayerChunkClaim> iterator = claimsManager.getClaimStatesStream().iterator();
		List<PlayerChunkClaim> packetBuilder = new ArrayList<>(ClientboundClaimStatesPacket.MAX_STATES);
		while(iterator.hasNext()) {
			PlayerChunkClaim state = iterator.next();
			packetBuilder.add(state);
			if(packetBuilder.size() == ClientboundClaimStatesPacket.MAX_STATES) {
				syncClaimStates(packetBuilder, player);
				packetBuilder.clear();
			}
		}
		if(!packetBuilder.isEmpty())
			syncClaimStates(packetBuilder, player);
	}
	
	public void syncToPlayersNewClaimState(PlayerChunkClaim claim) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() != ServerConfig.ClaimsSyncType.ALL)
			return;
		//definitely needed when a new claim state is created while a player is still syncing claim regions
		PlayerList players = server.getPlayerList();
		List<PlayerChunkClaim> packetBuilder = Lists.newArrayList(claim);
		for(ServerPlayer player : players.getPlayers())
			syncClaimStates(packetBuilder, player);
	}
	
	public void syncDimensionIdToClient(ResourceLocation dimension, ServerPlayer player) {
		sendToClient(player, new ClientboundPlayerClaimsDimensionPacket(dimension), false);
	}
	
	public void syncRegionClaimsToClient(int x, int z, int[] paletteInts, long[] storageData, int storageBits, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimsRegionPacket(x, z, paletteInts, storageBits, storageData), false);
	}
	
	public void syncToPlayersClaimUpdate(ResourceLocation dimension, int x, int z, PlayerChunkClaim claim, PlayerChunkClaim oldClaim) {
		if(ServerConfig.CONFIG.claimsSynchronization.get() == ServerConfig.ClaimsSyncType.NOT_SYNCED)
			return;
		ServerConfig.ClaimsSyncType syncType = ServerConfig.CONFIG.claimsSynchronization.get();
		PlayerList players = server.getPlayerList();
		UUID newPlayerId = claim == null ? null : claim.getPlayerId();
		ClientboundClaimsClaimUpdatePacket packet = new ClientboundClaimsClaimUpdatePacket(dimension, x, z, newPlayerId, claim != null && claim.isForceloadable());
		if(syncType == ServerConfig.ClaimsSyncType.ALL || Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, newPlayerId)) {//new claim is visible to all
			for(ServerPlayer player : players.getPlayers())
				sendToClient(player, packet, false);
		} else {
			UUID oldPlayerId = oldClaim == null ? null : oldClaim.getPlayerId();
			if(oldPlayerId != null && !Objects.equals(newPlayerId, oldPlayerId)) {
				ClientboundClaimsClaimUpdatePacket removalPacket = new ClientboundClaimsClaimUpdatePacket(dimension, x, z, null, false);
				if(Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, oldPlayerId)) {//old claim is visible to all
					for(ServerPlayer player : players.getPlayers())
						sendToClient(player, removalPacket, false);
				} else {
					ServerPlayer oldPlayer = players.getPlayer(oldPlayerId);
					if(oldPlayer != null)
						sendToClient(oldPlayer, removalPacket, false);
				}
			}
			if(newPlayerId != null) {
				ServerPlayer newPlayer = players.getPlayer(newPlayerId);
				if(newPlayer != null)
					sendToClient(newPlayer, packet, false);
			}
		}
	}
	
	public void syncToPlayerClaimActionResult(AreaClaimResult result, ServerPlayer player) {
		sendToClient(player, new ClientboundClaimResultPacket(result), true);
	}

	@Override
	public void syncOnLogin(ServerPlayer player) {
		IPlayerConfigManager<?> configManager = serverData.getPlayerConfigs();
		IPlayerConfig config = configManager.getLoadedConfig(player.getUUID());
		startSyncing(player);
		syncClaimLimits(config, player);
		syncPlayerClaimProperties(player);
		syncClaimStatesToClient(player);
		syncOwnedToClient(player);
		sendToClient(player, new ClaimRegionsStartPacket(), false);
	}

	@Override
	public void onServerTick() {
		PlayerList players = server.getPlayerList();
		int playerCount = 0;
		for(ServerPlayer player : players.getPlayers()) {
			ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(player);
			if(mainCap.getClaimsManagerPlayerSyncHandler().shouldSchedule() && !serverData.getServerTickHandler().getLazyPacketSender().isClogged(player))
				playerCount++;
		}
		if(playerCount == 0)
			return;
		int regionsPerPlayer = Math.min(REGIONS_PER_TICK_PER_PLAYER, REGIONS_PER_TICK / playerCount);
		for(ServerPlayer player : players.getPlayers()) {
			ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(player);
			if(!serverData.getServerTickHandler().getLazyPacketSender().isClogged(player))
				mainCap.getClaimsManagerPlayerSyncHandler().handle(player, regionsPerPlayer);
		}
	}
	
}
