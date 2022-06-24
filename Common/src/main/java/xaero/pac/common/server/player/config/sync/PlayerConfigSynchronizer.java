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

package xaero.pac.common.server.player.config.sync;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.PlayerConfigOptionClientStorage;
import xaero.pac.common.packet.config.PlayerConfigOptionValuePacket;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerConfigSynchronizer implements IPlayerConfigSynchronizer {
	
	private final MinecraftServer server;
	private PlayerConfigManager<?, ?> configManager;
	
	public PlayerConfigSynchronizer(MinecraftServer server) {
		this.server = server;
	}

	public void setConfigManager(PlayerConfigManager<?, ?> configManager) {
		if(this.configManager != null)
			throw new IllegalAccessError();
		this.configManager = configManager;
	}

	private void sendToClient(ServerPlayer player, Object packet) {
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
	}
	
	private <T> PlayerConfigOptionClientStorage<T> getPacketOptionEntry(ServerPlayer player, PlayerConfig<?> config, PlayerConfigOptionSpec<T> option) {
		boolean isOp = player.hasPermissions(2);
		boolean mutable = isOp && config.getType() != PlayerConfigType.PLAYER;
		boolean defaulted = !mutable;
		if(!mutable && config.getType() == PlayerConfigType.PLAYER) {
			mutable = ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get().contains(option.getId());
			defaulted = !mutable;
			if(!mutable) {
				defaulted = !ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(option.getId());
				mutable = !defaulted && isOp;
			}
		}
		PlayerConfigOptionClientStorage<T> entry = new PlayerConfigOptionClientStorage<>(option, config.getFromEffectiveConfig(option));
		entry.setMutable(mutable);
		entry.setDefaulted(defaulted);
		return entry;
	}
	
	private <T> void syncToClient(ServerPlayer player, PlayerConfig<?> config, List<PlayerConfigOptionClientStorage<?>> entries) {
		UUID ownerId = Objects.equals(player.getUUID(), config.getPlayerId()) ? null : config.getPlayerId();
		PlayerConfigOptionValuePacket packet = new PlayerConfigOptionValuePacket(config.getType(), ownerId, entries);
		sendToClient(player, packet);
	}
	
	private <T> void syncToClient(ServerPlayer player, PlayerConfig<?> config, PlayerConfigOptionSpec<T> option) {
		PlayerConfigOptionClientStorage<T> packetOptionEntry = getPacketOptionEntry(player, config, option);
		
		syncToClient(player, config, Lists.newArrayList(packetOptionEntry));
	}
	
	public void syncToClient(ServerPlayer player, PlayerConfig<?> config) {
		List<PlayerConfigOptionClientStorage<?>> entries = new ArrayList<>(PlayerConfig.OPTIONS.size());
		PlayerConfig.OPTIONS.forEach((key, option) -> entries.add(getPacketOptionEntry(player, config, option)));

		syncToClient(player, config, entries);
	}

	@Override
	public void syncToClient(ServerPlayer player) {
		syncToClient(player, configManager.getDefaultConfig());
		syncToClient(player, configManager.getWildernessConfig());
		syncToClient(player, configManager.getServerClaimConfig());
		syncToClient(player, configManager.getExpiredClaimConfig());
		syncToClient(player, configManager.getLoadedConfig(player.getUUID()));
	}
	
	public <T> void syncToClient(PlayerConfig<?> config, PlayerConfigOptionSpec<T> option) {
		PlayerList serverPlayerList = server.getPlayerList();
		if(config.getType() == PlayerConfigType.PLAYER) {
			ServerPlayer ownerPlayer = serverPlayerList.getPlayer(config.getPlayerId());
			if(ownerPlayer != null)
				syncToClient(ownerPlayer, config, option);
		} else {
			List<ServerPlayer> allPlayers = serverPlayerList.getPlayers();
			for(ServerPlayer player : allPlayers)
				syncToClient(player, config, option);
		}
	}

}
