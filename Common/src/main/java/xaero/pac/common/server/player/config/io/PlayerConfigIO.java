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

package xaero.pac.common.server.player.config.io;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.platform.Services;
import xaero.pac.common.player.config.PlayerConfigConstants;
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
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.config.io.serialization.PlayerConfigDeserializationInfo;
import xaero.pac.common.server.player.config.io.serialization.PlayerConfigSerializationHandler;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.permission.PermissionNode;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;
import xaero.pac.common.server.player.permission.value.type.PermissionValueType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static xaero.pac.common.server.player.config.PlayerConfig.WILDERNESS_PLAYER_ID_STRING;

public final class PlayerConfigIO
<
	P extends IServerParty<?, ?, ?>, 
	CM extends IServerClaimsManager<?, ?, ?>//needed in this class because of some weird compilation error when gradle building (not displayed by the IDE)
> extends ObjectManagerIO<String, PlayerConfigDeserializationInfo, PlayerConfig<P>, PlayerConfigManager<P, CM>> {
	
	private final Path configsPath;
	private final Path configSubConfigPath;
	private final FilePathConfig defaultConfigPathConfig;
	private final FilePathConfig wildernessConfigPathConfig;
	private final FilePathConfig serverClaimConfigPathConfig;
	private final FilePathConfig expiredClaimConfigPathConfig;
	private final Path defaultConfigs;
	
	private PlayerConfigIO(SerializationHandler<String, PlayerConfigDeserializationInfo, PlayerConfig<P>, PlayerConfigManager<P, CM>> serializationHandler, SerializedDataFileIO<String, PlayerConfigDeserializationInfo> serializedDataFileIO, IOThreadWorker ioThreadWorker,
			MinecraftServer server, String fileExtension, PlayerConfigManager<P, CM> manager, FileIOHelper fileIOHelper) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
		configsPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("player-configs");
		configSubConfigPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("player-configs").resolve("sub-configs");

		Path serverconfigPath = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig");
		defaultConfigPathConfig = new FilePathConfig(serverconfigPath.resolve(OpenPartiesAndClaims.MOD_ID + "-default-player-config.toml"), false);
		wildernessConfigPathConfig = new FilePathConfig(serverconfigPath.resolve(OpenPartiesAndClaims.MOD_ID + "-wilderness-config.toml"), false);
		serverClaimConfigPathConfig = new FilePathConfig(serverconfigPath.resolve(OpenPartiesAndClaims.MOD_ID + "-server-claim-config.toml"), false);
		expiredClaimConfigPathConfig = new FilePathConfig(serverconfigPath.resolve(OpenPartiesAndClaims.MOD_ID + "-expired-claim-config.toml"), false);
		defaultConfigs = Services.PLATFORM.getDefaultConfigFolder();
	}

	@Override
	protected Stream<FilePathConfig> getObjectFolderPaths() {
		return Stream.of(new FilePathConfig(configsPath, false), new FilePathConfig(configSubConfigPath, true));
	}
	
	@Override
	public void load() {
		OpenPartiesAndClaims.LOGGER.info("Loading player configs...");
		loadGlobalConfig(PlayerConfigType.DEFAULT_PLAYER, defaultConfigPathConfig, manager::setDefaultConfig);
		loadGlobalConfig(PlayerConfigType.WILDERNESS, wildernessConfigPathConfig, manager::setWildernessConfig);
		loadGlobalConfig(PlayerConfigType.SERVER, serverClaimConfigPathConfig, manager::setServerClaimConfig);
		loadGlobalConfig(PlayerConfigType.EXPIRED, expiredClaimConfigPathConfig, manager::setExpiredClaimConfig);
		saveFile(manager.getDefaultConfig(), defaultConfigPathConfig.getPath());//saves corrected config
		saveFile(manager.getWildernessConfig(), wildernessConfigPathConfig.getPath());//saves corrected config
		saveFile(manager.getServerClaimConfig(), serverClaimConfigPathConfig.getPath());//saves corrected config
		saveFile(manager.getExpiredClaimConfig(), expiredClaimConfigPathConfig.getPath());//saves corrected config
		
		super.load();
		manager.onLoad();
		OpenPartiesAndClaims.LOGGER.info("Loaded player configs!");
	}

	private void loadGlobalConfig(PlayerConfigType type, FilePathConfig filePathConfig, Consumer<PlayerConfig<P>> resultConsumer){
		loadGlobalConfig(type, filePathConfig.getPath(), filePathConfig, resultConsumer);
	}

	private void loadGlobalConfig(PlayerConfigType type, Path path, FilePathConfig filePathConfig, Consumer<PlayerConfig<P>> resultConsumer) {
		if(Files.exists(path)) {
			PlayerConfig<P> config = loadFile(path, filePathConfig, false);
			if(config == null)
				throw new RuntimeException("Server, expired, default and wilderness claim configs must load properly! Check the game logs for errors.");
			tryLoadingCustomGroups(config);
			resultConsumer.accept(config);
		} else {
			if(filePathConfig.getPath() == path) {
				Path fileInDefaultConfigs = defaultConfigs.resolve(path.getFileName());
				if (Files.exists(fileInDefaultConfigs)) {
					loadGlobalConfig(type, fileInDefaultConfigs, filePathConfig, resultConsumer);
					return;
				}
			}
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
			if(filePathConfig == wildernessConfigPathConfig) {
				config.forceSet(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS, false);
				config.forceSet(PlayerConfigOptions.CLAIM_EXCEPTION_RECLAIMABLE, PlayerConfigConstants.EVERYONE_EXCEPTION_ID);
			}
			tryLoadingCustomGroups(config);
			resultConsumer.accept(config);
		}
	}

	@Override
	public boolean save() {
//		if(true)
//			return true;
		OpenPartiesAndClaims.LOGGER.debug("Saving player configs...");
		if(manager.getDefaultConfig().isDirty())
			saveFile(manager.getDefaultConfig(), defaultConfigPathConfig.getPath());
		if(manager.getWildernessConfig().isDirty())
			saveFile(manager.getWildernessConfig(), wildernessConfigPathConfig.getPath());
		if(manager.getServerClaimConfig().isDirty())
			saveFile(manager.getServerClaimConfig(), serverClaimConfigPathConfig.getPath());
		saveGlobalConfigSubConfigs(manager.getServerClaimConfig());//no idea why this is here but will keep it just in case
		if(manager.getExpiredClaimConfig().isDirty())
			saveFile(manager.getExpiredClaimConfig(), expiredClaimConfigPathConfig.getPath());
		return super.save();
		
	}

	@Override
	protected void saveFile(PlayerConfig<P> object, Path filePath) {
		if(!(object instanceof PlayerSubConfig) && object.getPlayerGroups().isSaveNeeded())
			object.getPlayerGroups().getIo().saveToConfig();
		trySavingLastPermissionValues(object);
		super.saveFile(object, filePath);
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
		if(filePathConfig == defaultConfigPathConfig || filePathConfig == wildernessConfigPathConfig)
			return new PlayerConfigDeserializationInfo(null, filePathConfig == defaultConfigPathConfig ? PlayerConfigType.DEFAULT_PLAYER : PlayerConfigType.WILDERNESS, null, -1);
		if(filePathConfig == serverClaimConfigPathConfig)
			return new PlayerConfigDeserializationInfo(PlayerConfig.SERVER_CLAIM_UUID, PlayerConfigType.SERVER, null, -1);
		if(filePathConfig == expiredClaimConfigPathConfig)
			return new PlayerConfigDeserializationInfo(PlayerConfig.EXPIRED_CLAIM_UUID, PlayerConfigType.EXPIRED, null, -1);
		boolean isSub = filePathConfig.getPath() == configSubConfigPath;
		if(!isSub)
			return new PlayerConfigDeserializationInfo(UUID.fromString(fileNameNoExtension), PlayerConfigType.PLAYER, null, -1);
		String playerIdString = file.getParent().getFileName().toString();
		UUID playerId = playerIdString.equals(WILDERNESS_PLAYER_ID_STRING) ? null : UUID.fromString(playerIdString);
		PlayerConfig<?> mainConfig = manager.getConfig(playerId); //should be loaded by now because sub-configs are loaded last
		if(!mainConfig.getType().supportsSubConfigs())
			throw new IllegalArgumentException("A player/claims config that doesn't support sub-configs has sub-configs in the data!");
		String subId;
		int subIndex;
		if(!mainConfig.getType().hasDimensionSubConfigs()) {
			String[] fileNameArgs = fileNameNoExtension.split("\\$");
			subId = fileNameArgs[0];
			String subIndexString = fileNameArgs[1];
			subIndex = Integer.parseInt(subIndexString);
		} else {
			Identifier subConfigDim = fileIOHelper.convertFileNameToDimension(fileNameNoExtension, false);
			if(subConfigDim == null)
				throw new IllegalArgumentException("The " + mainConfig.getType() + " config has a sub-config with an ID that is not properly formatted: " + fileNameNoExtension);
			subId = subConfigDim.toString();
			subIndex = 0;
		}
		if(Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID))
			return new PlayerConfigDeserializationInfo(playerId, PlayerConfigType.SERVER, subId, subIndex);
		return new PlayerConfigDeserializationInfo(playerId, PlayerConfigType.PLAYER, subId, subIndex);
	}

	@Override
	protected Path getFilePath(PlayerConfig<P> object, String fileName) {
		if(object instanceof PlayerSubConfig subConfig) {
			Path folder = configSubConfigPath.resolve(fileName);
			String subIdBasedFileName;
			if(object.getType().hasDimensionSubConfigs())
				subIdBasedFileName = fileIOHelper.convertDimensionToFileName(Identifier.parse(subConfig.getSubId()), false);
			else
				subIdBasedFileName = subConfig.getSubId() + "$" + subConfig.getSubIndex();
			return folder.resolve(subIdBasedFileName + this.fileExtension);
		}
		return configsPath.resolve(fileName + this.fileExtension);
	}

	@Override
	protected void onObjectLoad(PlayerConfig<P> loadedObject) {
		tryLoadingCustomGroups(loadedObject);
		tryLoadingLastPermissionValues(loadedObject);
	}

	private void tryLoadingCustomGroups(PlayerConfig<P> config){
		if(config.getPlayerGroups() != null)
			config.getPlayerGroups().getIo().loadFromConfig();
	}

	private void trySavingLastPermissionValues(PlayerConfig<P> config){
		Map<IPermissionNodeAPI<?>, Object> lastPermissionValues = config.getLastPermissionValues();
		if(lastPermissionValues == null)
			return;
		List<String> data = new ArrayList<>();
		config.getLastPermissionValues().forEach((k, v) ->
				saveLastPermissionValue(data, k, v)
		);
		config.forceSet((PlayerConfigOptionSpec<List<String>>)PlayerConfigOptions.LAST_PERMISSION_VALUES, data);
	}

	@SuppressWarnings("unchecked")
	private <T> void saveLastPermissionValue(List<String> data, IPermissionNodeAPI<T> node, Object value){
		T valueCast = (T) value;
		PermissionValueType<T> valueType = ((PermissionNode<T>) node).getValueType();
		data.add(node.getDefaultNodeString() + "=" + valueType.getStringEncoder().apply(valueCast));
	}

	private void tryLoadingLastPermissionValues(PlayerConfig<P> config){
		if(config.getLastPermissionValues() == null)
			return;
		List<String> savedData = config.getRaw(PlayerConfigOptions.LAST_PERMISSION_VALUES);
		if(savedData == null)
			return;
		savedData.forEach(entry -> {
			int separatorIndex = entry.indexOf("=");
			String nodeId = entry.substring(0, separatorIndex);
			IPermissionNodeAPI<?> node = UsedPermissionNodes.ALL.get(nodeId);
			if(node == null)
				return;
			String valueString = entry.substring(separatorIndex + 1);
			loadLastPermissionValue(config, node, valueString);
		});
	}

	private <T> void loadLastPermissionValue(PlayerConfig<P> config, IPermissionNodeAPI<T> node, String valueString){
		PermissionValueType<T> valueType = ((PermissionNode<T>) node).getValueType();
		T value;
		try {
			value = valueType.getStringDecoder().apply(valueString);
		} catch(Throwable t){
			return;
		}
		config.setLastPermissionValue(node, value);
	}
	
	public static final class Builder
	<
		P extends IServerParty<?, ?, ?>, 
		CM extends IServerClaimsManager<?, ?, ?>
	> extends ObjectManagerIO.Builder<String, PlayerConfigDeserializationInfo, PlayerConfig<P>, PlayerConfigManager<P, CM>, Builder<P, CM>>{

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
			return (PlayerConfigIO<P, CM>) super.build();
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
