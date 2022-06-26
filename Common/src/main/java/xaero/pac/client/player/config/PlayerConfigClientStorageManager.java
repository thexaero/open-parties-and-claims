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

package xaero.pac.client.player.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xaero.pac.client.gui.OtherPlayerConfigWaitScreen;
import xaero.pac.client.gui.PlayerConfigScreen;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PlayerConfigClientStorageManager implements IPlayerConfigClientStorageManager<PlayerConfigClientStorage> {
	
	private PlayerConfigClientStorage serverClaimsConfig;
	private PlayerConfigClientStorage expiredClaimsConfig;
	private PlayerConfigClientStorage wildernessConfig;
	private PlayerConfigClientStorage defaultPlayerConfig;
	private PlayerConfigClientStorage myPlayerConfig;
	
	private PlayerConfigClientStorageManager(PlayerConfigClientStorage serverClaimsConfig, PlayerConfigClientStorage expiredClaimsConfig,
			PlayerConfigClientStorage wildernessConfig, PlayerConfigClientStorage defaultPlayerConfig,
			PlayerConfigClientStorage myPlayerConfig) {
		super();
		this.serverClaimsConfig = serverClaimsConfig;
		this.expiredClaimsConfig = expiredClaimsConfig;
		this.wildernessConfig = wildernessConfig;
		this.defaultPlayerConfig = defaultPlayerConfig;
		this.myPlayerConfig = myPlayerConfig;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getServerClaimsConfig() {
		return serverClaimsConfig;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getExpiredClaimsConfig() {
		return expiredClaimsConfig;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getWildernessConfig() {
		return wildernessConfig;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getDefaultPlayerConfig() {
		return defaultPlayerConfig;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getMyPlayerConfig() {
		return myPlayerConfig;
	}
	
	public static final class Builder {
		
		private Builder() {
		}
		
		public Builder setDefault() {
			return this;
		}

		public PlayerConfigClientStorageManager build() {
			PlayerConfigClientStorage serverClaimsConfig = PlayerConfigClientStorage.Builder.begin(LinkedHashMap::new).setType(PlayerConfigType.SERVER).setOwner(PlayerConfig.SERVER_CLAIM_UUID).build();
			PlayerConfigClientStorage expiredClaimsConfig = PlayerConfigClientStorage.Builder.begin(LinkedHashMap::new).setType(PlayerConfigType.EXPIRED).setOwner(PlayerConfig.EXPIRED_CLAIM_UUID).build();
			PlayerConfigClientStorage wildernessConfig = PlayerConfigClientStorage.Builder.begin(LinkedHashMap::new).setType(PlayerConfigType.WILDERNESS).setOwner(null).build();
			PlayerConfigClientStorage defaultPlayerConfig = PlayerConfigClientStorage.Builder.begin(LinkedHashMap::new).setType(PlayerConfigType.DEFAULT_PLAYER).setOwner(null).build();
			PlayerConfigClientStorage myPlayerConfig = PlayerConfigClientStorage.Builder.begin(LinkedHashMap::new).setType(PlayerConfigType.PLAYER).setOwner(null).build();
			return new PlayerConfigClientStorageManager(serverClaimsConfig, expiredClaimsConfig, wildernessConfig, defaultPlayerConfig, myPlayerConfig);
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

	@Override
	public PlayerConfigClientStorage.Builder beginConfigStorageBuild(MapFactory mapFactory) {
		return PlayerConfigClientStorage.Builder.begin(mapFactory);
	}

	@Override
	public void openServerClaimsConfigScreen(@Nullable Screen escape, @Nullable Screen parent) {
		PlayerConfigClientStorage config = getServerClaimsConfig();
		Minecraft.getInstance().setScreen(
				PlayerConfigScreen.Builder
				.begin(ArrayList::new)
				.setParent(parent)
				.setEscape(escape)
				.setTitle(Component.translatable("gui.xaero_pac_ui_server_claims_config"))
				.setData(config)
				.build()
				);
	}

	@Override
	public void openExpiredClaimsConfigScreen(@Nullable Screen escape, @Nullable Screen parent) {
		PlayerConfigClientStorage config = getExpiredClaimsConfig();
		Minecraft.getInstance().setScreen(
				PlayerConfigScreen.Builder
				.begin(ArrayList::new)
				.setParent(parent)
				.setEscape(escape)
				.setTitle(Component.translatable("gui.xaero_pac_ui_expired_claims_config"))
				.setData(config)
				.build()
				);
	}

	@Override
	public void openWildernessConfigScreen(@Nullable Screen escape, @Nullable Screen parent) {
		PlayerConfigClientStorage config = getWildernessConfig();
		Minecraft.getInstance().setScreen(
				PlayerConfigScreen.Builder
				.begin(ArrayList::new)
				.setParent(parent)
				.setEscape(escape)
				.setTitle(Component.translatable("gui.xaero_pac_ui_wilderness_config"))
				.setData(config)
				.build()
				);
	}

	@Override
	public void openDefaultPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent) {
		PlayerConfigClientStorage config = getDefaultPlayerConfig();
		Minecraft.getInstance().setScreen(
				PlayerConfigScreen.Builder
				.begin(ArrayList::new)
				.setParent(parent)
				.setEscape(escape)
				.setTitle(Component.translatable("gui.xaero_pac_ui_default_player_config"))
				.setData(config)
				.build()
				);
	}

	@Override
	public void openMyPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent) {
		PlayerConfigClientStorage config = getMyPlayerConfig();
		Minecraft.getInstance().setScreen(
				PlayerConfigScreen.Builder
				.begin(ArrayList::new)
				.setParent(parent)
				.setEscape(escape)
				.setData(config)
				.setDefaultPlayerConfigData(getDefaultPlayerConfig())
				.build()
				);
	}

	@Override
	public void openOtherPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent, @Nonnull String playerName) {
		if(!playerName.isEmpty())
			Minecraft.getInstance().setScreen(new OtherPlayerConfigWaitScreen(escape, parent, playerName));
	}

}
