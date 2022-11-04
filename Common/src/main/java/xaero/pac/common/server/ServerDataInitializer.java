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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.claims.player.io.serialization.nbt.PlayerClaimInfoNbtSerializer;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionType;
import xaero.pac.common.server.claims.protection.ExceptionElementType;
import xaero.pac.common.server.claims.protection.WildcardResolver;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroupLoader;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.task.ObjectExpirationCheckSpreadoutTask;
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
import xaero.pac.common.server.parties.party.*;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.parties.party.io.serialization.nbt.PartyNbtSerializer;
import xaero.pac.common.server.parties.party.task.PartyRemovalSpreadoutTask;
import xaero.pac.common.server.player.*;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;
import xaero.pac.common.server.player.config.sync.task.PlayerConfigSyncSpreadoutTask;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;
import xaero.pac.common.server.task.player.ServerPlayerSpreadoutTaskHandler;

import java.util.LinkedHashMap;
import java.util.Map;

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

			ServerTickHandler serverTickHandler = ServerTickHandler.Builder.begin().setServer(server).build();

			ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler =
					ServerSpreadoutQueuedTaskHandler.Builder
					.<PlayerClaimReplaceSpreadoutTask>begin()
					.setPerTickLimit(256)
					.setPerTickPerTaskLimit(32)
					.build();
			serverTickHandler.registerSpreadoutTaskHandler(claimReplaceTaskHandler);
			ServerSpreadoutQueuedTaskHandler<ObjectExpirationCheckSpreadoutTask<?>> objectExpirationCheckTaskHandler =
					ServerSpreadoutQueuedTaskHandler.Builder
					.<ObjectExpirationCheckSpreadoutTask<?>>begin()
					.setPerTickLimit(512)
					.setPerTickPerTaskLimit(Integer.MAX_VALUE)
					.build();
			serverTickHandler.registerSpreadoutTaskHandler(objectExpirationCheckTaskHandler);
			ServerPlayerSpreadoutTaskHandler<PlayerConfigSyncSpreadoutTask> playerConfigSyncTaskHandler =
					ServerPlayerSpreadoutTaskHandler.FinalBuilder
					.<PlayerConfigSyncSpreadoutTask>begin()
					.setPerTickLimit(128)
					.setPerTickPerTaskLimit(1)
					.setPlayerTaskGetter(ServerPlayerData::getConfigSyncSpreadoutTask)
					.build();
			serverTickHandler.registerSpreadoutTaskHandler(playerConfigSyncTaskHandler);
			ServerSpreadoutQueuedTaskHandler<PartyRemovalSpreadoutTask> partyRemovalTaskHandler =
					ServerSpreadoutQueuedTaskHandler.Builder
					.<PartyRemovalSpreadoutTask>begin()
					.setPerTickLimit(8192)
					.setPerTickPerTaskLimit(512)
					.build();
			serverTickHandler.registerSpreadoutTaskHandler(partyRemovalTaskHandler);

			PartyPlayerInfoUpdater partyMemberInfoUpdater = new PartyPlayerInfoUpdater();
			PartyManager partyManager = PartyManager.Builder.begin()
					.setServer(server)
					.setPartyRemovalTaskHandler(partyRemovalTaskHandler)
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
			PlayerTickHandler playerTickHandler = PlayerTickHandler.Builder.begin().build();
			PlayerLoginHandler playerLoginHandler = new PlayerLoginHandler();
			PlayerLogoutHandler playerLogoutHandler = new PlayerLogoutHandler();
			PlayerPermissionChangeHandler playerPermissionChangeHandler = new PlayerPermissionChangeHandler();
			PlayerWorldJoinHandler playerWorldJoinHandler = new PlayerWorldJoinHandler();
			long autosaveInterval = ServerConfig.CONFIG.autosaveInterval.get() * 60000;
			ObjectManagerLiveSaver partyLiveSaver = new ObjectManagerLiveSaver(partyManagerIO, autosaveInterval, 0);

			WildcardResolver wildcardResolver = new WildcardResolver();
			Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups = new LinkedHashMap<>();
			Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups = new LinkedHashMap<>();
			Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups = new LinkedHashMap<>();
			Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups = new LinkedHashMap<>();
			ChunkProtectionExceptionGroupLoader exceptionGroupLoader = new ChunkProtectionExceptionGroupLoader();
			exceptionGroupLoader.load(ServerConfig.CONFIG.blockProtectionOptionalExceptionGroups, ExceptionElementType.BLOCK, wildcardResolver, blockExceptionGroups, ChunkProtectionExceptionType.INTERACTION, t -> t != ChunkProtectionExceptionType.BARRIER);
			exceptionGroupLoader.load(ServerConfig.CONFIG.entityProtectionOptionalExceptionGroups, ExceptionElementType.ENTITY_TYPE, wildcardResolver, entityExceptionGroups, ChunkProtectionExceptionType.INTERACTION, t -> t != ChunkProtectionExceptionType.BARRIER);
			exceptionGroupLoader.load(ServerConfig.CONFIG.itemUseProtectionOptionalExceptionGroups, ExceptionElementType.ITEM, wildcardResolver, itemExceptionGroups, ChunkProtectionExceptionType.INTERACTION, t -> t == ChunkProtectionExceptionType.INTERACTION);
			exceptionGroupLoader.load(ServerConfig.CONFIG.entityClaimBarrierOptionalGroups, ExceptionElementType.ENTITY_TYPE, wildcardResolver, entityBarrierGroups, ChunkProtectionExceptionType.BARRIER, t -> t == ChunkProtectionExceptionType.BARRIER);

			PlayerConfigManager<ServerParty, ServerClaimsManager> playerConfigs = PlayerConfigManager.Builder.<ServerParty, ServerClaimsManager>begin()
					.setServer(server)
					.setPartyManager(partyManager)
					.setBlockExceptionGroups(blockExceptionGroups)
					.setEntityExceptionGroups(entityExceptionGroups)
					.setItemExceptionGroups(itemExceptionGroups)
					.setEntityBarrierGroups(entityBarrierGroups)
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
			}

			ForceLoadTicketManager forceLoadManager = playerConfigs.getForceLoadTicketManager();
			ClaimsManagerSynchronizer claimsSynchronizer = ClaimsManagerSynchronizer.Builder.begin().setServer(server).build();
			ServerClaimsManager serverClaimsManager = ServerClaimsManager.Builder.begin()
					.setServer(server)
					.setTicketManager(forceLoadManager)
					.setConfigManager(playerConfigs)
					.setClaimsManagerSynchronizer(claimsSynchronizer)
					.setClaimReplaceTaskHandler(claimReplaceTaskHandler)
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
			ChunkProtection<ServerClaimsManager, PartyMember, PartyInvite, ServerParty> chunkProtection = ChunkProtection.Builder
					.<ServerClaimsManager, PartyMember, PartyInvite, ServerParty>begin()
					.setClaimsManager(serverClaimsManager)
					.setPartyManager(partyManager)
					.setBlockExceptionGroups(blockExceptionGroups)
					.setEntityExceptionGroups(entityExceptionGroups)
					.setItemExceptionGroups(itemExceptionGroups)
					.setEntityBarrierGroups(entityBarrierGroups)
					.build();
			chunkProtection.updateTagExceptions();
			ServerStartingCallback serverLoadCallback = new ServerStartingCallback(playerClaimInfoManagerIO);

			ServerData serverData = new ServerData(server, partyManager, partyManagerIO, playerPartyAssigner, partyMemberInfoUpdater, 
					partyExpirationHandler, serverTickHandler, playerTickHandler, playerLoginHandler, playerLogoutHandler, playerPermissionChangeHandler, partyLiveSaver,
					ioThreadWorker, playerConfigs, playerConfigsIO, playerConfigLiveSaver, playerClaimInfoManagerIO, playerClaimInfoLiveSaver,
					serverClaimsManager, chunkProtection, serverLoadCallback, forceLoadManager, playerWorldJoinHandler, serverInfo, serverInfoIO, 
					claimsExpirationHandler, objectExpirationCheckTaskHandler);
			partyManager.getPartySynchronizer().setServerData(serverData);
			claimsSynchronizer.setServerData(serverData);
			return serverData;
		} catch(Throwable t) {
			modMain.startupCrashHandler.crash(t);
			return null;
		}
	}

}
