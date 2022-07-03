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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.parties.party.PartyPlayerInfo;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.claims.player.io.serialization.nbt.PlayerClaimInfoNbtSerializer;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.claims.protection.ChunkProtectionEntityHelper;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.info.ServerInfoHolder;
import xaero.pac.common.server.info.io.ServerInfoHolderIO;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerLiveSaver;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SimpleSerializationHandler;
import xaero.pac.common.server.io.serialization.human.HumanReadableSerializedDataFileIO;
import xaero.pac.common.server.io.serialization.human.SimpleHumanReadableStringConverter;
import xaero.pac.common.server.io.serialization.nbt.SimpleNBTSerializedDataFileIO;
import xaero.pac.common.server.lazypackets.LazyPacketSender;
import xaero.pac.common.server.parties.party.*;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.parties.party.io.serialization.nbt.PartyNbtSerializer;
import xaero.pac.common.server.player.PlayerLoginHandler;
import xaero.pac.common.server.player.PlayerLogoutHandler;
import xaero.pac.common.server.player.PlayerTickHandler;
import xaero.pac.common.server.player.PlayerWorldJoinHandler;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;

public class ServerDataInitializer {
	
	public ServerData init(OpenPartiesAndClaims modMain, MinecraftServer server) {
		try {
			IOThreadWorker ioThreadWorker = new IOThreadWorker();
			ioThreadWorker.begin();
			
			FileIOHelper fileIOHelper = new FileIOHelper();
			ServerInfoHolder serverInfoHolder = new ServerInfoHolder();
			ServerInfoHolderIO serverInfoIO = ServerInfoHolderIO.Builder.begin()
					.setServer(server)
					.setFileIOHelper(fileIOHelper)
					.setIoThreadWorker(ioThreadWorker)
					.setManager(serverInfoHolder)
					.build();
			serverInfoIO.load();
			ServerInfo serverInfo;
			if(serverInfoHolder.getServerInfo() == null) {
				serverInfoHolder.setServerInfo(serverInfo = new ServerInfo(0));
				serverInfo.setDirty(true);
				serverInfoIO.save();
			} else
				serverInfo = serverInfoHolder.getServerInfo();
			
			PartyPlayerInfoUpdater partyMemberInfoUpdater = new PartyPlayerInfoUpdater();
			PartyManager partyManager = PartyManager.Builder.begin()
					.setServer(server)
					.build();
			PartyExpirationHandler partyExpirationHandler = PartyExpirationHandler.Builder.begin()
					.setManager(partyManager)
					.setServer(server)
					.setServerInfo(serverInfo)
					.build();
			partyManager.setExpirationHandler(partyExpirationHandler);
			
			SerializationHandler<CompoundTag, String, ServerParty, PartyManager> partySerializationHandler = 
					new SimpleSerializationHandler<>(PartyNbtSerializer.Builder.begin().build());
			
			PartyManagerIO<CompoundTag> partyManagerIO = PartyManagerIO.Builder.<CompoundTag>begin()
					.setFileExtension(".nbt")
					.setSerializationHandler(partySerializationHandler)
					.setSerializedDataFileIO(new SimpleNBTSerializedDataFileIO<>())
					.setIoThreadWorker(ioThreadWorker)
					.setServer(server)
					.setManager(partyManager)
					.setFileIOHelper(fileIOHelper)
					.build();
			partyManager.setIo(partyManagerIO);
			
			PlayerLogInPartyAssigner playerPartyAssigner = new PlayerLogInPartyAssigner();
			ServerTickHandler serverTickHandler = new ServerTickHandler(LazyPacketSender.Builder.begin()
					.setServer(server)
					.setBytesPerTickLimit(104858 /*maximum ~2 MB per second*/)
					.setCapacity(104857600 /*~100 MB*/)
					.setBytesPerConfirmation(26214 * 20 /*~500 KB*/)
					.build());
			PlayerTickHandler playerTickHandler = PlayerTickHandler.Builder.begin().build();
			PlayerLoginHandler playerLoginHandler = new PlayerLoginHandler();
			PlayerLogoutHandler playerLogoutHandler = new PlayerLogoutHandler();
			PlayerWorldJoinHandler playerWorldJoinHandler = new PlayerWorldJoinHandler();
			long autosaveInterval = ServerConfig.CONFIG.autosaveInterval.get() * 60000;
			ObjectManagerLiveSaver partyLiveSaver = new ObjectManagerLiveSaver(partyManagerIO, autosaveInterval, 0);
			
			PlayerConfigManager<ServerParty, ServerClaimsManager> playerConfigs = PlayerConfigManager.Builder.<ServerParty, ServerClaimsManager>begin()
					.setServer(server)
					.setPartyManager(partyManager)
					.build();
			partyManager.setPlayerConfigs(playerConfigs);
			PlayerConfigIO<ServerParty, ServerClaimsManager> playerConfigsIO = PlayerConfigIO.Builder.<ServerParty, ServerClaimsManager>begin()
					.setServer(server)
					.setIoThreadWorker(ioThreadWorker)
					.setFileIOHelper(fileIOHelper)
					.setManager(playerConfigs)
					.setSerializedDataFileIO(new HumanReadableSerializedDataFileIO<>(new SimpleHumanReadableStringConverter<>()))
					.build();
			ObjectManagerLiveSaver playerConfigLiveSaver = new ObjectManagerLiveSaver(playerConfigsIO, autosaveInterval, autosaveInterval / 3);
			
			playerConfigsIO.load();
			if(ServerConfig.CONFIG.partiesEnabled.get()) {
				partyManagerIO.load();
				new PartyManagerFixer().fix(partyManager);
				partyExpirationHandler.handle();
			}
			
			ForceLoadTicketManager forceLoadManager = playerConfigs.getForceLoadTicketManager();
			ClaimsManagerSynchronizer claimsSynchronizer = new ClaimsManagerSynchronizer(server);
			ServerClaimsManager serverClaimsManager = ServerClaimsManager.Builder.begin()
					.setServer(server)
					.setTicketManager(forceLoadManager)
					.setConfigManager(playerConfigs)
					.setClaimsManagerSynchronizer(claimsSynchronizer)
					.build();
			forceLoadManager.setClaimsManager(serverClaimsManager);
			playerConfigs.setClaimsManager(serverClaimsManager);
			
			//PlayerClaimInfoManagerIO<PlayerClaimInfoSnapshot> playerClaimInfoManagerIO = new PlayerClaimInfoManagerIO<>(".json", new SnapshotBasedSerializationHandler<>(new PlayerClaimInfoSnapshotConverter(new PlayerDimensionClaimsSnapshotConverter(new PlayerChunkClaimSnapshotConverter()))), new HumanReadableSerializedDataFileIO<>(new GsonStringConverter<>(new GsonSnapshotSerializer<>(new GsonBuilder().setPrettyPrinting().create(), PlayerClaimInfoSnapshot.class))), ioThreadWorker, server, serverClaimsManager, playerClaimInfoManager);
			PlayerClaimInfoManagerIO<CompoundTag> playerClaimInfoManagerIO = PlayerClaimInfoManagerIO.Builder.<CompoundTag>begin()
					.setServerClaimsManager(serverClaimsManager)
					.setFileExtension(".nbt")
					.setSerializationHandler(new SimpleSerializationHandler<>(PlayerClaimInfoNbtSerializer.Builder.begin().build()))
					.setSerializedDataFileIO(new SimpleNBTSerializedDataFileIO<>())
					.setIoThreadWorker(ioThreadWorker)
					.setFileIOHelper(fileIOHelper)
					.setServer(server)
					.build();
			serverClaimsManager.setIo(playerClaimInfoManagerIO);
			
			ServerPlayerClaimsExpirationHandler claimsExpirationHandler = serverClaimsManager
					.beginExpirationHandlerBuilder()
					.setServer(server)
					.setServerInfo(serverInfo)
					.build();
			serverClaimsManager.setExpirationHandler(claimsExpirationHandler);
			
			ObjectManagerLiveSaver playerClaimInfoLiveSaver = new ObjectManagerLiveSaver(playerClaimInfoManagerIO, autosaveInterval, autosaveInterval / 3 * 2);
			ChunkProtection<ServerClaimsManager, PartyMember, PartyPlayerInfo, ServerParty> chunkProtection = ChunkProtection.Builder
					.<ServerClaimsManager, PartyMember, PartyPlayerInfo, ServerParty>begin()
					.setClaimsManager(serverClaimsManager)
					.setPartyManager(partyManager)
					.build();
			ServerStartingCallback serverLoadCallback = new ServerStartingCallback(playerClaimInfoManagerIO);
			
			ServerData serverData = new ServerData(server, partyManager, partyManagerIO, playerPartyAssigner, partyMemberInfoUpdater, 
					partyExpirationHandler, serverTickHandler, playerTickHandler, playerLoginHandler, playerLogoutHandler, partyLiveSaver, 
					ioThreadWorker, playerConfigs, playerConfigsIO, playerConfigLiveSaver, playerClaimInfoManagerIO, playerClaimInfoLiveSaver,
					serverClaimsManager, chunkProtection, serverLoadCallback, forceLoadManager, playerWorldJoinHandler, serverInfo, serverInfoIO, 
					claimsExpirationHandler);
			partyManager.getPartySynchronizer().setServerData(serverData);
			claimsSynchronizer.setServerData(serverData);
			return serverData;
		} catch(Throwable t) {
			modMain.startupCrashHandler.crash(t);
			return null;
		}
	}

}
