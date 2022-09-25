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
import net.minecraft.network.chat.TranslatableComponent;
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
	
	private final PlayerConfigClientStorage serverClaimsConfig;
	private final PlayerConfigClientStorage expiredClaimsConfig;
	private final PlayerConfigClientStorage wildernessConfig;
	private final PlayerConfigClientStorage defaultPlayerConfig;
	private final PlayerConfigClientStorage myPlayerConfig;
	private PlayerConfigClientStorage otherPlayerConfig;//temporary storage
	
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

	public void reset(){
		serverClaimsConfig.reset();
		expiredClaimsConfig.reset();
		wildernessConfig.reset();
		defaultPlayerConfig.reset();
		myPlayerConfig.reset();
		otherPlayerConfig = null;
	}

	@Override
	public void setOtherPlayerConfig(PlayerConfigClientStorage otherPlayerConfig) {
		this.otherPlayerConfig = otherPlayerConfig;
	}

	@Override
	public PlayerConfigClientStorage getOtherPlayerConfig() {
		return otherPlayerConfig;
	}

	@Override
	public PlayerConfigClientStorage.FinalBuilder beginConfigStorageBuild(MapFactory mapFactory) {
		return PlayerConfigClientStorage.FinalBuilder.begin(mapFactory);
	}

	@Override
	public void openServerClaimsConfigScreen(@Nullable Screen escape, @Nullable Screen parent) {
		PlayerConfigClientStorage config = getServerClaimsConfig();
		Minecraft.getInstance().setScreen(
				PlayerConfigScreen.Builder
				.begin(ArrayList::new)
				.setParent(parent)
				.setEscape(escape)
				.setMainPlayerConfigData(getMyPlayerConfig())
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
				.setTitle(new TranslatableComponent("gui.xaero_pac_ui_expired_claims_config"))
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
				.setTitle(new TranslatableComponent("gui.xaero_pac_ui_wilderness_config"))
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
				.setTitle(new TranslatableComponent("gui.xaero_pac_ui_default_player_config"))
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

	public static final class Builder {

		private Builder() {
		}

		public Builder setDefault() {
			return this;
		}

		public PlayerConfigClientStorageManager build() {
			PlayerConfigClientStorage serverClaimsConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.SERVER).setOwner(PlayerConfig.SERVER_CLAIM_UUID).build();
			PlayerConfigClientStorage expiredClaimsConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.EXPIRED).setOwner(PlayerConfig.EXPIRED_CLAIM_UUID).build();
			PlayerConfigClientStorage wildernessConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.WILDERNESS).setOwner(null).build();
			PlayerConfigClientStorage defaultPlayerConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.DEFAULT_PLAYER).setOwner(null).build();
			PlayerConfigClientStorage myPlayerConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.PLAYER).setOwner(null).build();
			return new PlayerConfigClientStorageManager(serverClaimsConfig, expiredClaimsConfig, wildernessConfig, defaultPlayerConfig, myPlayerConfig);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
