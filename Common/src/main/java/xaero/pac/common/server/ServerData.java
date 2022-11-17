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

package xaero.pac.common.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.expiration.task.ObjectExpirationCheckSpreadoutTask;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.info.io.ServerInfoHolderIO;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerLiveSaver;
import xaero.pac.common.server.parties.party.*;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.player.*;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.player.localization.ServerTranslationLoader;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;

import java.util.Map;

public final class ServerData implements IServerData<ServerClaimsManager, ServerParty> {
	
	private final MinecraftServer server;
	private final PartyManager partyManager;
	private final PartyManagerIO<?> partyManagerIO;
	private final PlayerLogInPartyAssigner playerPartyAssigner;
	private final PartyPlayerInfoUpdater partyMemberInfoUpdater;
	private final PartyExpirationHandler partyExpirationHandler;
	private final ServerTickHandler serverTickHandler;
	private final PlayerTickHandler playerTickHandler;
	private final PlayerLoginHandler playerLoginHandler;
	private final PlayerLogoutHandler playerLogoutHandler;
	private final PlayerPermissionChangeHandler playerPermissionChangeHandler;
	private final ObjectManagerLiveSaver partyLiveSaver;
	private final IOThreadWorker ioThreadWorker;
	private final PlayerConfigManager<ServerParty, ServerClaimsManager> playerConfigs;
	private final PlayerConfigIO<ServerParty, ServerClaimsManager> playerConfigsIO;
	private final ObjectManagerLiveSaver playerConfigLiveSaver;
	private final PlayerClaimInfoManagerIO<?> playerClaimInfoManagerIO;
	private final ObjectManagerLiveSaver playerClaimInfoLiveSaver;
	private final ServerClaimsManager serverClaimsManager;
	private final ServerPlayerClaimsExpirationHandler serverPlayerClaimsExpirationHandler;
	private final ChunkProtection<ServerClaimsManager, PartyMember, PartyInvite, ServerParty> chunkProtection;
	private final ServerStartingCallback serverLoadCallback;
	private final ForceLoadTicketManager forceLoadManager;
	private final PlayerWorldJoinHandler playerWorldJoinHandler;
	private final ServerInfo serverInfo;
	private final ServerInfoHolderIO serverInfoIO;
	private final ServerSpreadoutQueuedTaskHandler<ObjectExpirationCheckSpreadoutTask<?>> objectExpirationCheckTaskHandler;
	private AdaptiveLocalizer adaptiveLocalizer;
	private final OpenPACServerAPI api;

	public ServerData(MinecraftServer server, PartyManager partyManager, PartyManagerIO<?> partyManagerIO,
					  PlayerLogInPartyAssigner playerPartyAssigner, PartyPlayerInfoUpdater partyMemberInfoUpdater,
					  PartyExpirationHandler partyExpirationHandler, ServerTickHandler serverTickHandler,
					  PlayerTickHandler playerTickHandler, PlayerLoginHandler playerLoginHandler, PlayerLogoutHandler playerLogoutHandler, PlayerPermissionChangeHandler playerPermissionChangeHandler, ObjectManagerLiveSaver partyLiveSaver, IOThreadWorker ioThreadWorker,
					  PlayerConfigManager<ServerParty, ServerClaimsManager> playerConfigs, PlayerConfigIO<ServerParty, ServerClaimsManager> playerConfigsIO,
					  ObjectManagerLiveSaver playerConfigLiveSaver, PlayerClaimInfoManagerIO<?> playerClaimInfoManagerIO,
					  ObjectManagerLiveSaver playerClaimInfoLiveSaver, ServerClaimsManager serverClaimsManager,
					  ChunkProtection<ServerClaimsManager, PartyMember, PartyInvite, ServerParty> chunkProtection, ServerStartingCallback serverLoadCallback,
					  ForceLoadTicketManager forceLoadManager, PlayerWorldJoinHandler playerWorldJoinHandler, ServerInfo serverInfo,
					  ServerInfoHolderIO serverInfoIO, ServerPlayerClaimsExpirationHandler serverPlayerClaimsExpirationHandler, ServerSpreadoutQueuedTaskHandler<ObjectExpirationCheckSpreadoutTask<?>> objectExpirationCheckTaskHandler) {
		super();
		this.server = server;
		this.partyManager = partyManager;
		this.partyManagerIO = partyManagerIO;
		this.playerPartyAssigner = playerPartyAssigner;
		this.partyMemberInfoUpdater = partyMemberInfoUpdater;
		this.partyExpirationHandler = partyExpirationHandler;
		this.serverTickHandler = serverTickHandler;
		this.playerTickHandler = playerTickHandler;
		this.playerLoginHandler = playerLoginHandler;
		this.playerLogoutHandler = playerLogoutHandler;
		this.playerPermissionChangeHandler = playerPermissionChangeHandler;
		this.partyLiveSaver = partyLiveSaver;
		this.ioThreadWorker = ioThreadWorker;
		this.playerConfigs = playerConfigs;
		this.playerConfigsIO = playerConfigsIO;
		this.playerConfigLiveSaver = playerConfigLiveSaver;
		this.playerClaimInfoManagerIO = playerClaimInfoManagerIO;
		this.playerClaimInfoLiveSaver = playerClaimInfoLiveSaver;
		this.serverClaimsManager = serverClaimsManager;
		this.chunkProtection = chunkProtection;
		this.serverLoadCallback = serverLoadCallback;
		this.forceLoadManager = forceLoadManager;
		this.playerWorldJoinHandler = playerWorldJoinHandler;
		this.serverInfo = serverInfo;
		this.serverInfoIO = serverInfoIO;
		this.serverPlayerClaimsExpirationHandler = serverPlayerClaimsExpirationHandler;
		this.objectExpirationCheckTaskHandler = objectExpirationCheckTaskHandler;
		api = new OpenPACServerAPI(this);
	}

	public void onServerResourcesReload(ResourceManager resourceManager){
		Map<String, String> serverTranslations = new ServerTranslationLoader().loadFromResources(resourceManager);
		adaptiveLocalizer = new AdaptiveLocalizer(serverTranslations);
	}

	public void onStop() {
		@SuppressWarnings("unchecked")
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverDataInterface = (IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>)(Object) this;
		while(!partyManagerIO.save());
		while(!playerConfigsIO.save());
		while(!playerClaimInfoManagerIO.save());
		OpenPartiesAndClaims.LOGGER.info("Stopping IO worker...");
		ioThreadWorker.stop();
		OpenPartiesAndClaims.LOGGER.info("Stopped IO worker!");
	}
	
	@Override
	public PartyManager getPartyManager() {
		return partyManager;
	}

	@Override
	public PlayerLogInPartyAssigner getPlayerPartyAssigner() {
		return playerPartyAssigner;
	}

	@Override
	public PartyPlayerInfoUpdater getPartyMemberInfoUpdater() {
		return partyMemberInfoUpdater;
	}

	@Override
	public PartyManagerIO<?> getPartyManagerIO() {
		return partyManagerIO;
	}

	@Override
	public PartyExpirationHandler getPartyExpirationHandler() {
		return partyExpirationHandler;
	}

	@Override
	public ServerTickHandler getServerTickHandler() {
		return serverTickHandler;
	}
	
	public MinecraftServer getServer() {
		return server;
	}
	
	public ObjectManagerLiveSaver getPartyLiveSaver() {
		return partyLiveSaver;
	}
	
	public ObjectManagerLiveSaver getPlayerConfigLiveSaver() {
		return playerConfigLiveSaver;
	}

	@Override
	public PlayerConfigManager<ServerParty, ServerClaimsManager> getPlayerConfigs() {
		return playerConfigs;
	}
	
	public PlayerConfigIO<ServerParty, ServerClaimsManager> getPlayerConfigsIO() {
		return playerConfigsIO;
	}
	
	public IOThreadWorker getIoThreadWorker() {
		return ioThreadWorker;
	}
	
	public PlayerClaimInfoManagerIO<?> getPlayerClaimInfoManagerIO() {
		return playerClaimInfoManagerIO;
	}
	
	public ObjectManagerLiveSaver getPlayerClaimInfoLiveSaver() {
		return playerClaimInfoLiveSaver;
	}
	
	@Override
	public ServerClaimsManager getServerClaimsManager() {
		return serverClaimsManager;
	}

	@Override
	public ChunkProtection<ServerClaimsManager, PartyMember, PartyInvite, ServerParty> getChunkProtection() {
		return chunkProtection;
	}
	
	public ServerStartingCallback getServerLoadCallback() {
		return serverLoadCallback;
	}

	@Override
	public ForceLoadTicketManager getForceLoadManager() {
		return forceLoadManager;
	}

	@Override
	public PlayerTickHandler getPlayerTickHandler() {
		return playerTickHandler;
	}

	@Override
	public PlayerLoginHandler getPlayerLoginHandler() {
		return playerLoginHandler;
	}
	
	public PlayerLogoutHandler getPlayerLogoutHandler() {
		return playerLogoutHandler;
	}

	@Override
	public PlayerPermissionChangeHandler getPlayerPermissionChangeHandler() {
		return playerPermissionChangeHandler;
	}

	@Override
	public PlayerWorldJoinHandler getPlayerWorldJoinHandler() {
		return playerWorldJoinHandler;
	}

	@Override
	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	@Override
	public ServerInfoHolderIO getServerInfoIO() {
		return serverInfoIO;
	}

	@Override
	public ServerPlayerClaimsExpirationHandler getServerPlayerClaimsExpirationHandler() {
		return serverPlayerClaimsExpirationHandler;
	}

	@Override
	public ServerSpreadoutQueuedTaskHandler<ObjectExpirationCheckSpreadoutTask<?>> getObjectExpirationCheckTaskHandler() {
		return objectExpirationCheckTaskHandler;
	}

	@SuppressWarnings("unchecked")
	public static IServerData<IServerClaimsManager<IPlayerChunkClaim,IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember,IPartyPlayerInfo, IPartyAlly>> from(MinecraftServer server) {
		return (IServerData<IServerClaimsManager<IPlayerChunkClaim,IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember,IPartyPlayerInfo, IPartyAlly>>)
				((IOpenPACMinecraftServer)server).getXaero_OPAC_ServerData();
	}

	@Override
	public AdaptiveLocalizer getAdaptiveLocalizer() {
		return adaptiveLocalizer;
	}

	@Override
	public OpenPACServerAPI getAPI() {
		return api;
	}
	
}
