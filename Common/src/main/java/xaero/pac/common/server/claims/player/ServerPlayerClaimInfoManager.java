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

package xaero.pac.common.server.claims.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.ModConfigSpec;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoPostponedSaveReason;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObjectManager;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.io.ObjectManagerIOToSaveTracker;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.util.PermissionUtils;
import xaero.pac.common.util.linked.LinkedChain;

import java.util.*;

//only used by ServerClaimsManager
public final class ServerPlayerClaimInfoManager extends PlayerClaimInfoManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, ServerClaimsManager>
	implements ObjectManagerIOManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager>, ObjectManagerIOExpirableObjectManager<ServerPlayerClaimInfo> {

	private final MinecraftServer server;
	private final IPlayerConfigManager configManager;
	private final ForceLoadTicketManager ticketManager;
	private final Set<ResourceLocation> claimableDimensionsSet;
	private final Set<ServerPlayerClaimInfo> savePostponedFor;
	private final Set<ServerPlayerClaimInfo> unmodifiableSavePostponedFor;
	private ObjectManagerIOToSaveTracker<ServerPlayerClaimInfo> toSave;
	private boolean loaded;
	private PlayerClaimInfoManagerIO<?> io;
	private ServerPlayerClaimsExpirationHandler expirationHandler;

	public ServerPlayerClaimInfoManager(MinecraftServer server, IPlayerConfigManager configManager, ForceLoadTicketManager ticketManager,
	                                    Map<UUID, ServerPlayerClaimInfo> storage, LinkedChain<ServerPlayerClaimInfo> linkedPlayerInfo) {
		super(storage, linkedPlayerInfo);
		this.server = server;
		this.configManager = configManager;
		this.ticketManager = ticketManager;
		this.savePostponedFor = new HashSet<>();
		this.unmodifiableSavePostponedFor = Collections.unmodifiableSet(savePostponedFor);
		claimableDimensionsSet = new HashSet<>();
		for(String s : ServerConfig.CONFIG.claimableDimensionsList.get())
			claimableDimensionsSet.add(new ResourceLocation(s));
	}

	@Override
	public void setIo(ObjectManagerIO<?, ?, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> io) {
		if(this.io != null)
			throw new IllegalStateException();
		this.io = (PlayerClaimInfoManagerIO<?>) io;
		this.toSave = ObjectManagerIOToSaveTracker.Builder.<ServerPlayerClaimInfo>begin().setIo(io).build();
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
	public ObjectManagerIOToSaveTracker<ServerPlayerClaimInfo> getToSave() {
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
	protected ServerPlayerClaimInfo create(
			String username,
			UUID playerId,
			Map<ResourceLocation, PlayerDimensionClaims> claims
	) {
		return new ServerPlayerClaimInfo(getConfig(playerId), username, playerId, claims, this, new ArrayDeque<>(), new ArrayDeque<>());
	}

	@Override
	protected void onAdd(ServerPlayerClaimInfo playerInfo) {
		super.onAdd(playerInfo);
		server.getProfileCache().get(playerInfo.getPlayerId()).map(GameProfile::getName)
				.ifPresent(playerInfo::setPlayerUsername);//helps with claim usernames when the claim owner has never been on the server
		if(!loaded)
			return;
		getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesUpdate(getConfig(playerInfo.getPlayerId()));
	}

	@Override
	protected void onRemove(ServerPlayerClaimInfo playerInfo) {
		super.onRemove(playerInfo);
		io.delete(playerInfo);
		toSave.remove(playerInfo);
	}

	public int getPlayerBaseLimit(
			UUID playerId,
			ServerPlayer player,
			ModConfigSpec.IntValue limitConfig,
			ModConfigSpec.ConfigValue<?> partyBonusConfig,
			ModConfigSpec.ConfigValue<?> partyOwnerBonusConfig,
			IPermissionNodeAPI<Integer> permissionNode
	){
		if(playerId == null)
			playerId = player.getUUID();
		boolean partyOwnedClaims = ServerConfig.CONFIG.partyOwnedClaims.get();
		IPlayerConfig playerConfig = configManager.getLoadedConfig(playerId);
		int result = PermissionUtils.getOverriddenServerConfigInt(
				playerId, server, player, playerConfig, limitConfig, permissionNode, claimsManager.getPermissionHandler().getSystem()
		);
		if(partyOwnedClaims)
			result += getPartyOwnershipBonus(playerId, partyBonusConfig, partyOwnerBonusConfig);
		return result;
	}

	private int getPartyOwnershipBonus(
			UUID playerId,
			ModConfigSpec.ConfigValue<?> partyBonusConfig,
			ModConfigSpec.ConfigValue<?> partyOwnerBonusConfig
	){
		if(partyBonusConfig == null || !configManager.getPartySystemManager().isPrimaryPartyOwner(playerId))
			return 0;
		int memberCount = configManager.getPartySystemManager().getPrimaryMemberCount(playerId);
		if(memberCount > 0)
			memberCount--;//owner doesn't count
		double partyBonusConfigValue = 0;
		if(partyBonusConfig instanceof ModConfigSpec.IntValue partyBonusConfigInt)
			partyBonusConfigValue = partyBonusConfigInt.get();
		else if(partyBonusConfig instanceof ModConfigSpec.DoubleValue partyBonusConfigDouble)
			partyBonusConfigValue = partyBonusConfigDouble.get();
		double result = memberCount * partyBonusConfigValue;
		if(memberCount > 0) {
			double partyOwnerBonusConfigValue = 0;
			if(partyOwnerBonusConfig instanceof ModConfigSpec.IntValue partyOwnerBonusConfigInt)
				partyOwnerBonusConfigValue = partyOwnerBonusConfigInt.get();
			else if(partyOwnerBonusConfig instanceof ModConfigSpec.DoubleValue partyOwnerBonusConfigDouble)
				partyOwnerBonusConfigValue = partyOwnerBonusConfigDouble.get();
			result += partyOwnerBonusConfigValue;
		}
		return (int) result;
	}
	
	public ServerPlayerClaimsExpirationHandler getExpirationHandler() {
		return expirationHandler;
	}
	
	public ServerClaimsManager getClaimsManager() {
		return claimsManager;
	}

	@Override
	public Iterator<ServerPlayerClaimInfo> getExpirationIterator() {
		return iterator();
	}

	@Override
	public boolean usingPartyOwnedClaims() {
		return ServerConfig.CONFIG.partyOwnedClaims.get();
	}

	public void addSavePostponedFor(ServerPlayerClaimInfo playerClaimInfo, PlayerClaimInfoPostponedSaveReason reason){
		savePostponedFor.add(playerClaimInfo);
		playerClaimInfo.getPostponedSaveReasons().add(reason);
	}

	public void removeSavePostponedFor(ServerPlayerClaimInfo playerClaimInfo, PlayerClaimInfoPostponedSaveReason reason){
		playerClaimInfo.getPostponedSaveReasons().remove(reason);
		if(playerClaimInfo.getPostponedSaveReasons().isEmpty())
			savePostponedFor.remove(playerClaimInfo);
	}

	public boolean isSavePostponed(ServerPlayerClaimInfo playerClaimInfo){
		return savePostponedFor.contains(playerClaimInfo);
	}

	public Set<ServerPlayerClaimInfo> getSavePostponedFor() {
		return unmodifiableSavePostponedFor;
	}

	public void preparePostponedSaves(IServerData<?,?> serverData){
		OpenPartiesAndClaims.LOGGER.info("Finishing claim-related tasks that must be completed before saving...");
		while(!savePostponedFor.isEmpty()){
			ServerPlayerClaimInfo first = savePostponedFor.iterator().next();
			while(!first.getPostponedSaveReasons().isEmpty()) {
				PlayerClaimInfoPostponedSaveReason firstReason = first.getPostponedSaveReasons().iterator().next();
				firstReason.getPreparer().accept(serverData);
			}
			//preparers should eventually clear savePostponedFor if they're declared correctly
		}
		OpenPartiesAndClaims.LOGGER.info("Done! All claims can now be saved.");
	}

}
