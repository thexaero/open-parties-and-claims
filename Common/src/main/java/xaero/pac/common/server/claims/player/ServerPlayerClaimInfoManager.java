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

package xaero.pac.common.server.claims.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.util.linked.LinkedChain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

//only used by ServerClaimsManager
public final class ServerPlayerClaimInfoManager extends PlayerClaimInfoManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> 
	implements ObjectManagerIOManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> {

	private final MinecraftServer server;
	private ServerClaimsManager claimsManager;
	private final IPlayerConfigManager<?> configManager;
	private final ForceLoadTicketManager ticketManager;
	private final Set<ServerPlayerClaimInfo> toSave;
	private final Set<ResourceLocation> claimableDimensionsSet;
	private boolean loaded;
	private PlayerClaimInfoManagerIO<?> io;
	private ServerPlayerClaimsExpirationHandler expirationHandler;

	public ServerPlayerClaimInfoManager(MinecraftServer server, IPlayerConfigManager<?> configManager, ForceLoadTicketManager ticketManager,
			Map<UUID, ServerPlayerClaimInfo> storage, LinkedChain<ServerPlayerClaimInfo> linkedPlayerInfo, Set<ServerPlayerClaimInfo> toSave) {
		super(storage, linkedPlayerInfo);
		this.server = server;
		this.configManager = configManager;
		this.ticketManager = ticketManager;
		this.toSave = toSave;
		claimableDimensionsSet = new HashSet<>();
		for(String s : ServerConfig.CONFIG.claimableDimensionsList.get())
			claimableDimensionsSet.add(new ResourceLocation(s));
	}
	
	public void setClaimsManager(ServerClaimsManager claimsManager) {
		if(this.claimsManager != null)
			throw new IllegalStateException();
		this.claimsManager = claimsManager;
	}
	
	public void setIo(PlayerClaimInfoManagerIO<?> io) {
		if(this.io != null)
			throw new IllegalStateException();
		this.io = io;
	}
	
	public void setExpirationHandler(ServerPlayerClaimsExpirationHandler expirationHandler) {
		if(this.expirationHandler != null)
			throw new IllegalStateException();
		this.expirationHandler = expirationHandler;
	}

	public boolean isClaimable(ResourceLocation dimension) {
		boolean contains = claimableDimensionsSet.contains(dimension);
		return ServerConfig.CONFIG.claimableDimensionsListType.get() == ServerConfig.ConfigListType.ONLY && contains || ServerConfig.CONFIG.claimableDimensionsListType.get() == ServerConfig.ConfigListType.ALL_BUT && !contains;
	}

	@Override
	public void addToSave(ServerPlayerClaimInfo object) {
		toSave.add(object);
	}

	@Override
	public Iterable<ServerPlayerClaimInfo> getToSave() {
		return toSave;
	}
	
	public ForceLoadTicketManager getTicketManager() {
		return ticketManager;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public void onLoad() {
		loaded = true;
	}
	
	public IPlayerConfig getConfig(UUID playerId) {
		return configManager.getLoadedConfig(playerId);
	}

	@Override
	protected ServerPlayerClaimInfo create(String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims) {
		return new ServerPlayerClaimInfo(getConfig(playerId), username, playerId, claims, this);
	}

	@Override
	protected void onRemove(ServerPlayerClaimInfo playerInfo) {
		super.onRemove(playerInfo);
		io.delete(playerInfo);
		toSave.remove(playerInfo);
	}

	public int getPlayerBaseLimit(UUID playerId, ServerPlayer player, ForgeConfigSpec.IntValue limitConfig, ForgeConfigSpec.ConfigValue<String> permissionNodeConfig){
		boolean hasFtbRanks = OpenPartiesAndClaims.INSTANCE.getModSupport().FTB_RANKS;
		int defaultLimit = limitConfig.get();
		if(!hasFtbRanks)
			return defaultLimit;
		String ftbRanksPermission = permissionNodeConfig.get();
		if(ftbRanksPermission == null || ftbRanksPermission.isEmpty())
			return defaultLimit;
		if(player == null)
			player = server.getPlayerList().getPlayer(playerId);
		if(player == null)
			return defaultLimit;
		return OpenPartiesAndClaims.INSTANCE.getModSupport().getFTBRanksSupport().getPermissionHelper().getIntPermission(player, ftbRanksPermission).orElse(defaultLimit);
	}

	@Override
	public Stream<ServerPlayerClaimInfo> getAllStream() {
		return getInfoStream();
	}
	
	public ServerPlayerClaimsExpirationHandler getExpirationHandler() {
		return expirationHandler;
	}
	
	public ServerClaimsManager getClaimsManager() {
		return claimsManager;
	}

}
