/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config.io;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.FilePathConfig;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.io.serialization.PlayerConfigDeserializationInfo;
import xaero.pac.common.server.player.config.io.serialization.PlayerConfigSerializationHandler;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class PlayerConfigIO
<
	P extends IServerParty<?, ?, ?>, 
	CM extends IServerClaimsManager<?, ?, ?>//needed in this class because of some weird compilation error when gradle building (not displayed by the IDE)
> extends ObjectManagerIO<String, PlayerConfigDeserializationInfo, PlayerConfig<P>, PlayerConfigManager<P, CM>, PlayerConfigIO<P, CM>> {
	
	private final Path configsPath;
	private final Path configSubConfigPath;
	private final Path defaultConfigPath;
	private final Path wildernessConfigPath;
	private final Path serverClaimConfigPath;
	private final Path serverClaimConfigOverridesPath;
	private final Path expiredClaimConfigPath;
	private final FilePathConfig globalFilePathConfig;
	
	private PlayerConfigIO(SerializationHandler<String, PlayerConfigDeserializationInfo, PlayerConfig<P>, PlayerConfigManager<P, CM>> serializationHandler, SerializedDataFileIO<String, PlayerConfigDeserializationInfo> serializedDataFileIO, IOThreadWorker ioThreadWorker,
			MinecraftServer server, String fileExtension, PlayerConfigManager<P, CM> manager, FileIOHelper fileIOHelper) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
		configsPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("player-configs");
		configSubConfigPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("player-configs").resolve("sub-configs");

		globalFilePathConfig = new FilePathConfig(server.getWorldPath(LevelResource.ROOT).resolve("serverconfig"), false);
		defaultConfigPath = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(OpenPartiesAndClaims.MOD_ID + "-default-player-config.toml");
		wildernessConfigPath = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(OpenPartiesAndClaims.MOD_ID + "-wilderness-config.toml");
		String serverClaimConfigName = OpenPartiesAndClaims.MOD_ID + "-server-claim-config";
		serverClaimConfigPath = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(serverClaimConfigName + ".toml");
		serverClaimConfigOverridesPath = configSubConfigPath.resolve(serverClaimConfigName);
		expiredClaimConfigPath = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(OpenPartiesAndClaims.MOD_ID + "-expired-claim-config.toml");
	}

	@Override
	protected Stream<FilePathConfig> getObjectFolderPaths() {
		return Stream.of(new FilePathConfig(configsPath, false), new FilePathConfig(configSubConfigPath, true));
	}
	
	@Override
	public void load() {
		OpenPartiesAndClaims.LOGGER.info("Loading player configs...");
		loadGlobalConfig(PlayerConfigType.DEFAULT_PLAYER, defaultConfigPath, manager::setDefaultConfig);
		loadGlobalConfig(PlayerConfigType.WILDERNESS, wildernessConfigPath, manager::setWildernessConfig);
		loadGlobalConfig(PlayerConfigType.SERVER, serverClaimConfigPath, manager::setServerClaimConfig);
		loadGlobalConfig(PlayerConfigType.EXPIRED, expiredClaimConfigPath, manager::setExpiredClaimConfig);
		saveFile(manager.getDefaultConfig(), defaultConfigPath);//saves corrected config
		saveFile(manager.getWildernessConfig(), wildernessConfigPath);//saves corrected config
		saveFile(manager.getServerClaimConfig(), serverClaimConfigPath);//saves corrected config
		saveFile(manager.getExpiredClaimConfig(), expiredClaimConfigPath);//saves corrected config
		
		super.load();
		manager.onLoad();
		OpenPartiesAndClaims.LOGGER.info("Loaded player configs!");
	}
	
	private void loadGlobalConfig(PlayerConfigType type, Path path, Consumer<PlayerConfig<P>> resultConsumer) {
		if(Files.exists(path)) {
			PlayerConfig<P> config = loadFile(path, globalFilePathConfig, false);
			if(config == null)
				throw new RuntimeException("Server, expired, default and wilderness claim configs must load properly! Check the game logs for errors.");
			resultConsumer.accept(config);
		} else {
			PlayerConfig<P> config = PlayerConfig.FinalBuilder.<P>begin()
					.setType(type)
					.setPlayerId(
							type == PlayerConfigType.SERVER ?
								PlayerConfig.SERVER_CLAIM_UUID :
							type == PlayerConfigType.EXPIRED ?
								PlayerConfig.EXPIRED_CLAIM_UUID :
								null
							)
					.setManager(manager)
					.build();
			CommentedConfig storage = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
			manager.getPlayerConfigSpec().correct(storage);
			config.setStorage(storage);
			if(path == wildernessConfigPath)
				config.tryToSet(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS, false);

			resultConsumer.accept(config);
		}
	}

	@Override
	public boolean save() {
//		if(true)
//			return true;
		OpenPartiesAndClaims.LOGGER.debug("Saving player configs...");
		if(manager.getDefaultConfig().isDirty())
			saveFile(manager.getDefaultConfig(), defaultConfigPath);
		if(manager.getWildernessConfig().isDirty())
			saveFile(manager.getWildernessConfig(), wildernessConfigPath);
		if(manager.getServerClaimConfig().isDirty())
			saveFile(manager.getServerClaimConfig(), serverClaimConfigPath);
		saveGlobalConfigSubConfigs(manager.getServerClaimConfig());
		if(manager.getExpiredClaimConfig().isDirty())
			saveFile(manager.getExpiredClaimConfig(), expiredClaimConfigPath);
		return super.save();
		
	}

	private void saveGlobalConfigSubConfigs(PlayerConfig<P> globalConfig){
		globalConfig.getSubConfigStream().filter(PlayerConfig::isDirty)
				.forEach(co -> saveFile(co, getFilePath(co, co.getFileName())));
	}

	@Override
	public void onServerTick() {
		super.onServerTick();
	}

	@Override
	protected PlayerConfigDeserializationInfo getObjectId(String fileNameNoExtension, Path file, FilePathConfig filePathConfig) {
		if(file == defaultConfigPath || file == wildernessConfigPath)
			return new PlayerConfigDeserializationInfo(null, file == defaultConfigPath ? PlayerConfigType.DEFAULT_PLAYER : PlayerConfigType.WILDERNESS, null, -1);
		if(file == serverClaimConfigPath)
			return new PlayerConfigDeserializationInfo(PlayerConfig.SERVER_CLAIM_UUID, PlayerConfigType.SERVER, null, -1);
		if(file == expiredClaimConfigPath)
			return new PlayerConfigDeserializationInfo(PlayerConfig.EXPIRED_CLAIM_UUID, PlayerConfigType.EXPIRED, null, -1);
		boolean isSub = filePathConfig.getPath() == configSubConfigPath;
		if(!isSub)
			return new PlayerConfigDeserializationInfo(UUID.fromString(fileNameNoExtension), PlayerConfigType.PLAYER, null, -1);
		UUID playerId = UUID.fromString(file.getParent().getFileName().toString());
		String[] fileNameArgs = fileNameNoExtension.split("\\$");
		String subId = fileNameArgs[0];
		String subIndexString = fileNameArgs[1];
		int subIndex = Integer.parseInt(subIndexString);
		if(Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID))
			return new PlayerConfigDeserializationInfo(playerId, PlayerConfigType.SERVER, subId, subIndex);
		return new PlayerConfigDeserializationInfo(playerId, PlayerConfigType.PLAYER, subId, subIndex);
	}

	@Override
	protected Path getFilePath(PlayerConfig<P> object, String fileName) {
		if(object instanceof PlayerSubConfig subConfig) {
			Path folder = configSubConfigPath.resolve(fileName);
			return folder.resolve(subConfig.getSubId() + "$" + subConfig.getSubIndex() + this.fileExtension);
		}
		return configsPath.resolve(fileName + this.fileExtension);
	}

	@Override
	protected void onObjectLoad(PlayerConfig<P> loadedObject) {
	}
	
	public static final class Builder
	<
		P extends IServerParty<?, ?, ?>, 
		CM extends IServerClaimsManager<?, ?, ?>
	> extends ObjectManagerIO.Builder<String, PlayerConfigDeserializationInfo, PlayerConfig<P>, PlayerConfigManager<P, CM>, PlayerConfigIO<P, CM>>{

		private Builder() {
		}

		public Builder<P, CM> setDefault() {
			super.setDefault();
			setFileExtension(".toml");
			return this;
		}

		public PlayerConfigIO<P, CM> build() {
			if(serializationHandler == null)
				setSerializationHandler(PlayerConfigSerializationHandler.Builder.<P, CM>begin().build());
			return super.build();
		}

		@Override
		protected PlayerConfigIO<P, CM> buildInternally() {
			PlayerConfigIO<P, CM> result = new PlayerConfigIO<>(serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
			manager.setIO(result);
			return result;
		}

		public static <
			P extends IServerParty<?, ?, ?>, 
			CM extends IServerClaimsManager<?, ?, ?>//needed in this class because of some weird compilation error when gradle building (not displayed by the IDE)
		> Builder<P, CM> begin() {
			return new Builder<P, CM>().setDefault();
		}

	}

}
