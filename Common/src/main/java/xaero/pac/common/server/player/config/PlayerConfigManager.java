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

package xaero.pac.common.server.player.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.common.packet.config.ClientboundPlayerConfigConfigurableOptionsPacket;
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.io.ObjectManagerIOToSaveTracker;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.PlayerPartySystemManager;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.config.dynamic.PlayerConfigDynamicOptionsLoader;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.config.sync.PlayerConfigSynchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.OPTIONS;

public final class PlayerConfigManager
<
	P extends IServerParty<?, ?, ?>,
	CM extends IServerClaimsManager<?, ?, ?>
>
implements IPlayerConfigManager, ObjectManagerIOManager<PlayerConfig<P>, PlayerConfigManager<P, CM>> {

	private final MinecraftServer server;
	private boolean loaded;
	private PlayerConfig<P> defaultConfig;
	private PlayerConfig<P> wildernessConfig;
	private PlayerConfig<P> serverClaimConfig;
	private PlayerConfig<P> expiredClaimConfig;
	private final ForceLoadTicketManager forceLoadTicketManager;
	private final Map<UUID, PlayerConfig<P>> configs;
	private ObjectManagerIOToSaveTracker<PlayerConfig<P>> configsToSave;
	private final PlayerConfigSynchronizer synchronizer;
	private CM claimsManager;
	private final IPartyManager<P> partyManager;
	private PlayerConfigIO<P, CM> io;
	private final PlayerConfigDynamicOptions dynamicOptions;
	private final ForgeConfigSpec playerConfigSpec;
	private final PlayerPartySystemManager partySystemManager;

	private PlayerConfigManager(
			MinecraftServer server,
			ForceLoadTicketManager forceLoadTicketManager,
			Map<UUID, PlayerConfig<P>> configs,
			PlayerConfigSynchronizer synchronizer,
			IPartyManager<P> partyManager,
			PlayerConfigDynamicOptions dynamicOptions,
			ForgeConfigSpec playerConfigSpec,
			PlayerPartySystemManager partySystemManager
	) {
		super();
		this.server = server;
		this.forceLoadTicketManager = forceLoadTicketManager;
		this.configs = configs;
		this.synchronizer = synchronizer;
		this.partyManager = partyManager;
		this.dynamicOptions = dynamicOptions;
		this.playerConfigSpec = playerConfigSpec;
		this.partySystemManager = partySystemManager;
	}
	
	public void setClaimsManager(CM claimsManager) {
		if(this.claimsManager != null)
			throw new IllegalStateException();
		this.claimsManager = claimsManager;
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getLoadedConfig(@Nullable UUID id) {
		if(!loaded)
			throw new IllegalStateException();
		return getConfig(id);
	}

	@Nullable
	@Override
	public PlayerConfig<P> getPartyOwnerConfig(@Nonnull UUID memberId) {
		UUID partyOwner = partySystemManager.getPrimaryPartyOwnerByMember(memberId);
		if(partyOwner == null)
			return null;
		return getLoadedConfig(partyOwner);
	}

	public PlayerConfig<P> getConfig(UUID id) {
		if(id == null)
			return wildernessConfig;
		if(Objects.equals(id, PlayerConfig.SERVER_CLAIM_UUID))
			return serverClaimConfig;
		if(Objects.equals(id, PlayerConfig.EXPIRED_CLAIM_UUID))
			return expiredClaimConfig;
		PlayerConfig<P> result = configs.computeIfAbsent(id,
			i -> PlayerConfig.FinalBuilder.<P>begin().setPlayerId(i).setManager(this).build()
		);
		if(loaded && !result.getPlayerGroups().isLoaded())
			result.getPlayerGroups().getIo().loadFromConfig();
		return result;
	}
	
	public void onLoad() {
		this.loaded = true;
	}

	@Override
	public ObjectManagerIOToSaveTracker<PlayerConfig<P>> getToSave() {
		return configsToSave;
	}
	
	public void setDefaultConfig(PlayerConfig<P> defaultConfig) {
		this.defaultConfig = defaultConfig;
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getDefaultConfig() {
		return defaultConfig;
	}
	
	public void setWildernessConfig(PlayerConfig<P> wildernessConfig) {
		this.wildernessConfig = wildernessConfig;
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getWildernessConfig() {
		return wildernessConfig;
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getServerClaimConfig() {
		return serverClaimConfig;
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getExpiredClaimConfig() {
		return expiredClaimConfig;
	}
	
	public void setServerClaimConfig(PlayerConfig<P> serverClaimConfig) {
		this.serverClaimConfig = serverClaimConfig;
	}
	
	public void setExpiredClaimConfig(PlayerConfig<P> expiredClaimConfig) {
		this.expiredClaimConfig = expiredClaimConfig;
	}

	@Override
	public ForceLoadTicketManager getForceLoadTicketManager() {
		return forceLoadTicketManager;
	}
	
	@Override
	public PlayerConfigSynchronizer getSynchronizer() {
		return synchronizer;
	}
	
	public IPartyManager<P> getPartyManager() {
		return partyManager;
	}

	@Override
	public CM getClaimsManager() {
		return claimsManager;
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	public void onSubConfigRemoved(PlayerSubConfig<P> subConfig) {
		configsToSave.remove(subConfig);
		if(loaded) {
			io.delete(subConfig);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setIo(ObjectManagerIO<?, ?, PlayerConfig<P>, PlayerConfigManager<P, CM>> io) {
		if(this.io != null)
			throw new RuntimeException(new IllegalAccessException());
		this.io = (PlayerConfigIO<P, CM>) io;
		this.configsToSave = ObjectManagerIOToSaveTracker.Builder.<PlayerConfig<P>>begin().setIo(io).build();
	}

	public boolean isLoaded() {
		return loaded;
	}

	public ForgeConfigSpec getPlayerConfigSpec() {
		return playerConfigSpec;
	}

	public PlayerConfigDynamicOptions getDynamicOptions() {
		return dynamicOptions;
	}

	@Nonnull
	public Stream<IPlayerConfigOptionSpecAPI<?>> getAllOptionsStream(){
		return Stream.concat(PlayerConfigOptions.OPTIONS.values().stream(), dynamicOptions.getOptions().values().stream());
	}

	@Nullable
	@Override
	public IPlayerConfigOptionSpecAPI<?> getOptionForId(@Nonnull String id) {
		if(!id.startsWith(PlayerConfig.PLAYER_CONFIG_ROOT_DOT))
			id = PlayerConfig.PLAYER_CONFIG_ROOT_DOT + id;
		IPlayerConfigOptionSpecAPI<?> result = PlayerConfigOptions.OPTIONS.get(id);
		if(result == null)
			result = dynamicOptions.getOptions().get(id);
		return result;
	}

	@Override
	public PlayerPartySystemManager getPartySystemManager() {
		return partySystemManager;
	}

	public static final class Builder
	<
		P extends IServerParty<?, ?, ?>,
		CM extends IServerClaimsManager<?, ?, ?>
	> {
		private MinecraftServer server;
		private IPartyManager<P> partyManager;
		private PlayerPartySystemManager partySystemManager;
		private Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> blockAccessEntityGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityAccessEntityGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> playerAccessEntityGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> droppedItemAccessEntityGroups;

		private Builder() {
		}

		private Builder<P, CM> setDefault() {
			setServer(null);
			setPartyManager(null);
			setPartySystemManager(null);
			setBlockExceptionGroups(null);
			setEntityExceptionGroups(null);
			setItemExceptionGroups(null);
			setEntityBarrierGroups(null);
			setBlockAccessEntityGroups(null);
			setEntityAccessEntityGroups(null);
			setPlayerAccessEntityGroups(null);
			setDroppedItemAccessEntityGroups(null);
			return this;
		}

		public Builder<P, CM> setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}
		
		public Builder<P, CM> setPartyManager(IPartyManager<P> partyManager) {
			this.partyManager = partyManager;
			return this;
		}

		public Builder<P, CM> setPartySystemManager(PlayerPartySystemManager partySystemManager) {
			this.partySystemManager = partySystemManager;
			return this;
		}

		public Builder<P, CM> setBlockExceptionGroups(Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups) {
			this.blockExceptionGroups = blockExceptionGroups;
			return this;
		}

		public Builder<P, CM> setEntityExceptionGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups) {
			this.entityExceptionGroups = entityExceptionGroups;
			return this;
		}

		public Builder<P, CM> setItemExceptionGroups(Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups) {
			this.itemExceptionGroups = itemExceptionGroups;
			return this;
		}

		public Builder<P, CM> setEntityBarrierGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups) {
			this.entityBarrierGroups = entityBarrierGroups;
			return this;
		}

		public Builder<P, CM> setBlockAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> blockAccessEntityGroups) {
			this.blockAccessEntityGroups = blockAccessEntityGroups;
			return this;
		}

		public Builder<P, CM> setEntityAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityAccessEntityGroups) {
			this.entityAccessEntityGroups = entityAccessEntityGroups;
			return this;
		}

		public Builder<P, CM> setPlayerAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> playerAccessEntityGroups) {
			this.playerAccessEntityGroups = playerAccessEntityGroups;
			return this;
		}

		public Builder<P, CM> setDroppedItemAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> droppedItemAccessEntityGroups) {
			this.droppedItemAccessEntityGroups = droppedItemAccessEntityGroups;
			return this;
		}

		public PlayerConfigManager<P, CM> build() {
			if (server == null || partyManager == null || blockExceptionGroups == null || entityExceptionGroups == null ||
					itemExceptionGroups == null || entityBarrierGroups == null || blockAccessEntityGroups == null ||
					entityAccessEntityGroups == null || playerAccessEntityGroups == null || droppedItemAccessEntityGroups == null ||
					partySystemManager == null)
				throw new IllegalStateException();
			ClientboundPlayerConfigConfigurableOptionsPacket configurableOptionsPacket = ClientboundPlayerConfigConfigurableOptionsPacket.fromServerConfig();
			PlayerConfigSynchronizer playerConfigSynchronizer = new PlayerConfigSynchronizer(server, configurableOptionsPacket);
			ForceLoadTicketManager forceLoadTicketManager = ForceLoadTicketManager.Builder.begin()
					.setServer(server)
					.setPartySystemManager(partySystemManager)
					.build();

			PlayerConfigDynamicOptions.Builder dynamicOptionsBuilder = PlayerConfigDynamicOptions.Builder.begin();
			new PlayerConfigDynamicOptionsLoader().load(
					dynamicOptionsBuilder,
					blockExceptionGroups, entityExceptionGroups, itemExceptionGroups,
					entityBarrierGroups, blockAccessEntityGroups, entityAccessEntityGroups,
					playerAccessEntityGroups, droppedItemAccessEntityGroups
			);
			PlayerConfigDynamicOptions dynamicOptions = dynamicOptionsBuilder.build();

			ForgeConfigSpec.Builder configSpecBuilder = new ForgeConfigSpec.Builder();
			Consumer<IPlayerConfigOptionSpecAPI<?>> optionConsumer = o -> ((PlayerConfigOptionSpec<?>)o).applyToForgeSpec(configSpecBuilder);
			OPTIONS.values().forEach(optionConsumer);
			dynamicOptions.getOptions().values().forEach(optionConsumer);

			PlayerConfigManager<P, CM> result = new PlayerConfigManager<>(
					server, forceLoadTicketManager, new HashMap<>(),
					playerConfigSynchronizer, partyManager, dynamicOptions,
					configSpecBuilder.build(), partySystemManager
			);
			playerConfigSynchronizer.setConfigManager(result);
			forceLoadTicketManager.setConfigManager(result);
			return result;
		}

		public static 
		<
			P extends IServerParty<?, ?, ?>,
			CM extends IServerClaimsManager<?, ?, ?>
		> Builder<P, CM> begin() {
			return new Builder<P, CM>().setDefault();
		}

	}

}
