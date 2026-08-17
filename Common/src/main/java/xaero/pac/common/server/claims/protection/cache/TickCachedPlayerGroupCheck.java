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

package xaero.pac.common.server.claims.protection.cache;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TickCachedPlayerGroupCheck {

	private final IPlayerConfigOptionSpecAPI<String> option;
	private final Map<UUID, Map<Identifier, Map<String, Map<ServerPlayer, Boolean>>>> claimPlayerCache;
	private final Map<UUID, Map<Identifier, Map<String, Map<UUID, Boolean>>>> claimIdCache;
	private final Map<Identifier, Map<String, Map<ServerPlayer, Boolean>>> wildernessPlayerCache;
	private final Map<Identifier, Map<String, Map<UUID, Boolean>>> wildernessIdCache;
	private ServerPlayer lastPlayer;
	private UUID lastPlayerId;
	private IPlayerChunkClaim lastClaimState;
	private Identifier lastDimension;
	private boolean lastResult;

	private TickCachedPlayerGroupCheck(
			IPlayerConfigOptionSpecAPI<String> option,
			Map<UUID, Map<Identifier, Map<String, Map<ServerPlayer, Boolean>>>> claimPlayerCache,
			Map<UUID, Map<Identifier, Map<String, Map<UUID, Boolean>>>> claimIdCache,
			Map<Identifier, Map<String, Map<ServerPlayer, Boolean>>> wildernessPlayerCache,
			Map<Identifier, Map<String, Map<UUID, Boolean>>> wildernessIdCache
	) {
		this.option = option;
		this.claimPlayerCache = claimPlayerCache;
		this.claimIdCache = claimIdCache;
		this.wildernessPlayerCache = wildernessPlayerCache;
		this.wildernessIdCache = wildernessIdCache;
	}

	private Map<ServerPlayer, Boolean> getCachePlayerMap(UUID claimOwnerId, Identifier dimension, String subClaim){
		Map<Identifier, Map<String, Map<ServerPlayer, Boolean>>> playerCache = claimOwnerId == null ?
				wildernessPlayerCache : claimPlayerCache.get(claimOwnerId);
		if(playerCache == null)
			claimPlayerCache.put(claimOwnerId, playerCache = new HashMap<>());
		Map<String, Map<ServerPlayer, Boolean>> subClaimMap = playerCache.get(dimension);
		if(subClaimMap == null)
			playerCache.put(dimension, subClaimMap = new HashMap<>());
		Map<ServerPlayer, Boolean> playerMap = subClaimMap.get(subClaim);
		if(playerMap == null)
			subClaimMap.put(subClaim, playerMap = new HashMap<>());
		return playerMap;
	}

	private Map<UUID, Boolean> getCachePlayerIdMap(UUID claimOwnerId, Identifier dimension, String subClaim){
		Map<Identifier, Map<String, Map<UUID, Boolean>>> idCache = claimOwnerId == null ?
				wildernessIdCache : claimIdCache.get(claimOwnerId);
		if(idCache == null)
			claimIdCache.put(claimOwnerId, idCache = new HashMap<>());
		Map<String, Map<UUID, Boolean>> subClaimMap = idCache.get(dimension);
		if(subClaimMap == null)
			idCache.put(dimension, subClaimMap = new HashMap<>());
		Map<UUID, Boolean> playerIdMap = subClaimMap.get(subClaim);
		if(playerIdMap == null)
			subClaimMap.put(subClaim, playerIdMap = new HashMap<>());
		return playerIdMap;
	}

	private Boolean getCache(UUID claimOwnerId, Identifier dimension, String subClaim, ServerPlayer player){
		Map<ServerPlayer, Boolean> playerMap = getCachePlayerMap(claimOwnerId, dimension, subClaim);
		return playerMap.get(player);
	}

	private Boolean getCache(UUID claimOwnerId, Identifier dimension, String subClaim, UUID playerId){
		Map<UUID, Boolean> playerIdMap = getCachePlayerIdMap(claimOwnerId, dimension, subClaim);
		return playerIdMap.get(playerId);
	}

	private Boolean getCache(UUID claimOwnerId, Identifier dimension, String subClaim, ServerPlayer player, UUID playerId){
		if(player != null)
			return getCache(claimOwnerId, dimension, subClaim, player);
		return getCache(claimOwnerId, dimension, subClaim, playerId);
	}

	private void setCache(UUID claimOwnerId, Identifier dimension, String subClaim, ServerPlayer player, Boolean value){
		Map<ServerPlayer, Boolean> playerMap = getCachePlayerMap(claimOwnerId, dimension, subClaim);
		playerMap.put(player, value);
	}

	private void setCache(UUID claimOwnerId, Identifier dimension, String subClaim, UUID playerId, Boolean value){
		Map<UUID, Boolean> playerIdMap = getCachePlayerIdMap(claimOwnerId, dimension, subClaim);
		playerIdMap.put(playerId, value);
	}

	private void setCache(UUID claimOwnerId, Identifier dimension, String subClaim, ServerPlayer player, UUID playerId, Boolean value){
		if(player != null) {
			setCache(claimOwnerId, dimension, subClaim, player, value);
			return;
		}
		setCache(claimOwnerId, dimension, subClaim, playerId, value);
	}

	public boolean checkGroup(
			IPlayerConfigManager playerConfigManager,
			ChunkProtection<ServerClaimsManager> chunkProtection,
			ServerPlayer player,
			UUID playerId,
			IPlayerChunkClaim claimState,
			Identifier dimension
	){
		if(lastPlayer == player &&
				Objects.equals(lastPlayerId, playerId) &&
				Objects.equals(lastClaimState, claimState) &&
				Objects.equals(lastDimension, dimension)
		)
			return lastResult;
		lastPlayer = player;
		lastPlayerId = playerId;
		lastClaimState = claimState;
		lastDimension = dimension;
		IPlayerConfig claimConfig = chunkProtection.getClaimConfig(playerConfigManager, claimState, dimension);
		String subClaim = claimConfig.getSubId();
		if(subClaim == null)
			subClaim = PlayerConfig.MAIN_SUB_ID;
		UUID claimOwner = claimConfig.getPlayerId();
		Boolean cachedResult = getCache(claimOwner, dimension, subClaim, player, playerId);
		if(cachedResult != null) {
			lastResult = cachedResult;
			return cachedResult;
		}
		boolean directResult;
		if(player != null)
			directResult = chunkProtection.checkPlayerGroupExceptionOption(option, claimConfig, player);
		else
			directResult = chunkProtection.checkPlayerGroupExceptionOption(option, claimConfig, playerId);
		setCache(claimOwner, dimension, subClaim, player, playerId, directResult);
		lastResult = directResult;
		return directResult;
	}

	public void onServerTick(){
		claimPlayerCache.clear();
		claimIdCache.clear();
		wildernessPlayerCache.clear();
		wildernessIdCache.clear();
		lastPlayer = null;
		lastPlayerId = null;
		lastClaimState = null;
		lastDimension = null;
	}

	public static final class Builder {

		private IPlayerConfigOptionSpecAPI<String> option;

		private Builder(){}

		public Builder setDefault(){
			setOption(null);
			return this;
		}

		public Builder setOption(IPlayerConfigOptionSpecAPI<String> option){
			this.option = option;
			return this;
		}

		public TickCachedPlayerGroupCheck build(){
			if(option == null)
				throw new IllegalStateException();
			return new TickCachedPlayerGroupCheck(option, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
