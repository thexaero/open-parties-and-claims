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

package xaero.pac.common.server.player.config;

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.config.sync.PlayerConfigSynchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public final class PlayerConfigManager
<
	P extends IServerParty<?, ?>,
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
	private final Set<PlayerConfig<P>> configsToSave;
	private final PlayerConfigSynchronizer synchronizer;
	private CM claimsManager;
	private final IPartyManager<P> partyManager;
	private PlayerConfigIO<P, CM> io;

	private PlayerConfigManager(MinecraftServer server, ForceLoadTicketManager forceLoadTicketManager,
			Map<UUID, PlayerConfig<P>> configs, Set<PlayerConfig<P>> configsToSave, PlayerConfigSynchronizer synchronizer, 
			IPartyManager<P> partyManager) {
		super();
		this.server = server;
		this.forceLoadTicketManager = forceLoadTicketManager;
		this.configs = configs;
		this.configsToSave = configsToSave;
		this.synchronizer = synchronizer;
		this.partyManager = partyManager;
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
	
	public PlayerConfig<P> getConfig(UUID id) {
		if(id == null)
			return wildernessConfig;
		if(Objects.equals(id, PlayerConfig.SERVER_CLAIM_UUID))
			return serverClaimConfig;
		if(Objects.equals(id, PlayerConfig.EXPIRED_CLAIM_UUID))
			return expiredClaimConfig;
		return configs.computeIfAbsent(id, 
			i -> PlayerConfig.FinalBuilder.<P>begin().setPlayerId(i).setManager(this).build()
		);
	}
	
	public void onLoad() {
		this.loaded = true;
	}

	@Override
	public Iterable<PlayerConfig<P>> getToSave() {
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
	public void addToSave(PlayerConfig<P> object) {
		configsToSave.add(object);
	}
	
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
	
	public CM getClaimsManager() {
		return claimsManager;
	}
	
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public Stream<PlayerConfig<P>> getAllStream() {
		return configs.values().stream();
	}

	public void onSubConfigRemoved(PlayerSubConfig<P> subConfig) {
		configsToSave.remove(subConfig);
		io.delete(subConfig);
	}

	public void setIO(PlayerConfigIO<P, CM> io) {
		if(this.io != null)
			throw new RuntimeException(new IllegalAccessException());
		this.io = io;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public static final class Builder
	<
		P extends IServerParty<?, ?>,
		CM extends IServerClaimsManager<?, ?, ?>
	> {
		private MinecraftServer server;
		private IPartyManager<P> partyManager;

		private Builder() {
		}

		private Builder<P, CM> setDefault() {
			setServer(null);
			setPartyManager(null);
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

		public PlayerConfigManager<P, CM> build() {
			if (server == null || partyManager == null)
				throw new IllegalStateException();
			PlayerConfigSynchronizer playerConfigSynchronizer = new PlayerConfigSynchronizer(server);
			ForceLoadTicketManager forceLoadTicketManager = ForceLoadTicketManager.Builder.begin().setServer(server).build();
			PlayerConfigManager<P, CM> result = new PlayerConfigManager<>(server, forceLoadTicketManager, new HashMap<>(), new HashSet<>(), playerConfigSynchronizer, partyManager);
			playerConfigSynchronizer.setConfigManager(result);
			return result;
		}

		public static 
		<
			P extends IServerParty<?, ?>,
			CM extends IServerClaimsManager<?, ?, ?>
		> Builder<P, CM> begin() {
			return new Builder<P, CM>().setDefault();
		}

	}

}
