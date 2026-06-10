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

package xaero.pac.common.server.claims.forceload;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.forceload.player.PlayerForceloadTicketManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.system.PlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.party.PrimaryPartyOnlineCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ForceLoadTicketManager {
	
	public static final TicketType OPAC_TICKET = new TicketType(
			0L,
			TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE
	);
	
	private IServerClaimsManager<?, ?, ?> claimsManager;
	private final MinecraftServer server;
	private final Map<UUID, PlayerForceloadTicketManager> claimTickets;
	private final Map<ResourceLocation, DimensionInfo> dimensionInfoMap;
	private final PlayerPartySystemManager partySystemManager;
	private IPlayerConfigManager playerConfigManager;
	private PrimaryPartyOnlineCounter primaryPartyOnlineCounter;
	private boolean loaded;
	
	private ForceLoadTicketManager(
			MinecraftServer server,
			Map<UUID, PlayerForceloadTicketManager> claimTickets,
			Map<ResourceLocation, DimensionInfo> dimensionInfoMap,
			PlayerPartySystemManager partySystemManager
	) {
		super();
		this.server = server;
		this.claimTickets = claimTickets;
		this.dimensionInfoMap = dimensionInfoMap;
		this.partySystemManager = partySystemManager;
	}
	
	public void setClaimsManager(IServerClaimsManager<?, ?, ?> claimsManager) {
		if(this.claimsManager != null)
			throw new IllegalStateException();
		this.claimsManager = claimsManager;
	}

	public void setConfigManager(IPlayerConfigManager playerConfigManager) {
		if(this.playerConfigManager != null)
			throw new IllegalStateException();
		this.playerConfigManager = playerConfigManager;
	}

	public void setPrimaryPartyOnlineCounter(PrimaryPartyOnlineCounter primaryPartyOnlineCounter) {
		if(this.primaryPartyOnlineCounter != null)
			throw new IllegalStateException();
		this.primaryPartyOnlineCounter = primaryPartyOnlineCounter;
	}

	private PlayerForceloadTicketManager getPlayerTickets(UUID id){
		return claimTickets.computeIfAbsent(id, i -> PlayerForceloadTicketManager.Builder.begin().build());
	}

	private DimensionInfo getDimensionInfo(ResourceLocation dimension){
		DimensionInfo dimInfo = dimensionInfoMap.get(dimension);
		if(dimInfo == null)
			dimensionInfoMap.put(dimension, dimInfo = new DimensionInfo());
		return dimInfo;
	}
	
	private boolean enableTicket(ClaimTicket ticket) {
		boolean isServer = Objects.equals(ticket.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID);
		if(!isServer && (!ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() || !ServerConfig.CONFIG.allowExistingForceloadsInUnclaimableDimensions.get()) && !claimsManager.isClaimable(ticket.getDimension()))
			return false;
		ChunkPos pos = new ChunkPos(ticket.getX(), ticket.getZ());

		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, ticket.getDimension());
		ServerLevel world = server.getLevel(levelKey);
		if(world == null)//can happen when a dimension is removed from a server
			return false;
		Services.PLATFORM.getServerChunkCacheAccess().addRegionTicket(world.getChunkSource(), OPAC_TICKET, pos, 2);
		ticket.setEnabled(true);
		countEnabled(ticket.getDimension(), 1);
//		OpenPartiesAndClaims.LOGGER.info("Enabled force load ticket at " + pos);
		return true;
	}

	private void disableTicket(ClaimTicket ticket) {
		ChunkPos pos = new ChunkPos(ticket.getX(), ticket.getZ());
		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, ticket.getDimension());
		ServerLevel world = server.getLevel(levelKey);
		if(world != null)//null can happen when a dimension is removed from a server
			Services.PLATFORM.getServerChunkCacheAccess().removeRegionTicket(world.getChunkSource(), OPAC_TICKET, pos, 2);
		ticket.setEnabled(false);
		countEnabled(ticket.getDimension(), -1);
//		OpenPartiesAndClaims.LOGGER.info("Disabled force load ticket at " + pos);
	}

	private void countEnabled(ResourceLocation dimension, int change){
		DimensionInfo dimInfo = getDimensionInfo(dimension);
		dimInfo.enabledTicketCount += change;
	}
	
	private boolean updateTicket(boolean shouldBeEnabled, ClaimTicket ticket) {
		if(shouldBeEnabled) {
			if(!ticket.isEnabled())
				return enableTicket(ticket);
		} else if(ticket.isEnabled()) {
			disableTicket(ticket);
		}
		return true;
	}
	
	private boolean ticketsShouldBeEnabled(IPlayerConfig ownerConfig, boolean loggedOut) {
		if(!ownerConfig.getEffective(PlayerConfigOptions.FORCELOAD))
			return false;
		if(
				Objects.equals(PlayerConfig.SERVER_CLAIM_UUID, ownerConfig.getPlayerId()) ||
				Objects.equals(PlayerConfig.EXPIRED_CLAIM_UUID, ownerConfig.getPlayerId()) ||
				ownerConfig.getEffective(PlayerConfigOptions.OFFLINE_FORCELOAD)
		)
			return true;
		if(!loaded)
			return false;
		if(isOwnerOfPrimaryParty(ownerConfig.getPlayerId()))
			return primaryPartyOnlineCounter.isPartyOnline(ownerConfig.getPlayerId());
		return !loggedOut && /*is online*/server.getPlayerList().getPlayer(ownerConfig.getPlayerId()) != null;
	}

	public void updateTicketsFor(UUID id, boolean loggedOut) {
		IPlayerConfig ownerConfig = playerConfigManager.getLoadedConfig(id);
		PlayerForceloadTicketManager playerTickets = getPlayerTickets(id);
		boolean isServer = PlayerConfig.SERVER_CLAIM_UUID.equals(id);
		boolean shouldBeEnabled = ticketsShouldBeEnabled(ownerConfig, loggedOut);
		OpenPartiesAndClaims.LOGGER.info("Updating all forceload tickets for " + id);
		int forceloadLimit = claimsManager.getPlayerBaseForceloadLimit(id) + playerConfigManager.getLoadedConfig(id).getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);//for when the bonus forceload count is changed without a restart
		int enableSuccessCount = 0;
		boolean withinLimit = true;
		for(ClaimTicket ticket : playerTickets.values()) {
			if(shouldBeEnabled && !isServer)
				withinLimit = withinLimit && enableSuccessCount < forceloadLimit;
			boolean shouldEnableTicket = shouldBeEnabled && withinLimit;
			if(updateTicket(shouldEnableTicket, ticket) && shouldEnableTicket)
				enableSuccessCount++;
		}
		playerTickets.setFailedToEnableSome(!withinLimit);
	}

	public void addTicket(ResourceLocation dimension, UUID id, int x, int z) {
		ClaimTicket ticket = new ClaimTicket(id, dimension, x, z);
		PlayerForceloadTicketManager playerTickets = getPlayerTickets(id);
		playerTickets.add(ticket);
		IPlayerConfig ownerConfig = playerConfigManager.getLoadedConfig(id);
		boolean shouldBeEnabled = ticketsShouldBeEnabled(ownerConfig, false);
		if(shouldBeEnabled) {
			boolean isServer = PlayerConfig.SERVER_CLAIM_UUID.equals(id);
			int forceloadLimit = isServer ? 0 : claimsManager.getPlayerBaseForceloadLimit(id) + playerConfigManager.getLoadedConfig(id).getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);
			if(isServer || playerTickets.getCount() <= forceloadLimit)
				updateTicket(true, ticket);
		}
	}

	public void removeTicket(ResourceLocation dimension, UUID id, int x, int z) {
		PlayerForceloadTicketManager playerTickets = getPlayerTickets(id);
		ClaimTicket ticket = playerTickets.remove(new ClaimTicket(id, dimension, x, z));//find and remove the equivalent in the map
		if (ticket.isEnabled()){
			updateTicket(false, ticket);
			if (playerTickets.failedToEnableSome())
				updateTicketsFor(id, false);//to enable another one that was disabled by the forceload limit
		}
	}

	public boolean hasEnabledTickets(ServerLevel level){
		DimensionInfo dimensionInfo = dimensionInfoMap.get(level.dimension().location());
		return dimensionInfo != null && dimensionInfo.enabledTicketCount > 0;
	}

	private boolean isOwnerOfPrimaryParty(UUID playerId) {
		if(!ServerConfig.CONFIG.partyOwnedClaims.get())
			return false;
		return partySystemManager.isPrimaryPartyOwner(playerId);
	}

	public void setLoaded(){
		loaded = true;
	}

	public static final class DimensionInfo {

		private int enabledTicketCount;

	}
	
	public static final class Builder {
		private MinecraftServer server;
		private PlayerPartySystemManager partySystemManager;

		private Builder() {
		}

		private Builder setDefault() {
			setPartySystemManager(null);
			setServer(null);
			return this;
		}

		public Builder setPartySystemManager(PlayerPartySystemManager partySystemManager) {
			this.partySystemManager = partySystemManager;
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public ForceLoadTicketManager build() {
			if(partySystemManager == null)
				throw new IllegalStateException();
			if (server == null)
				throw new IllegalStateException();
			return new ForceLoadTicketManager(server, new HashMap<>(), new HashMap<>(), partySystemManager);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}
	
}
