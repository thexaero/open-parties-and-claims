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
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Stream;

public class PlayerConfigClientStorageManager implements IPlayerConfigClientStorageManager<PlayerConfigClientStorage> {
	
	private PlayerConfigClientStorage serverClaimsConfig;
	private PlayerConfigClientStorage expiredClaimsConfig;
	private PlayerConfigClientStorage wildernessConfig;
	private PlayerConfigClientStorage defaultPlayerConfig;
	private PlayerConfigClientStorage myPlayerConfig;
	private PlayerConfigClientStorage otherPlayerConfig;//temporary storage
	private PlayerConfigDynamicOptions dynamicOptions;
	private final Set<IPlayerConfigOptionSpecAPI<?>> overridableOptions;

	private PlayerConfigClientStorageManager(Set<IPlayerConfigOptionSpecAPI<?>> overridableOptions) {
		super();
		this.overridableOptions = overridableOptions;
	}

	private void set(PlayerConfigClientStorage serverClaimsConfig, PlayerConfigClientStorage expiredClaimsConfig,
											 PlayerConfigClientStorage wildernessConfig, PlayerConfigClientStorage defaultPlayerConfig,
											 PlayerConfigClientStorage myPlayerConfig) {
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
		dynamicOptions = null;
		overridableOptions.clear();
		overridableOptions.addAll(PlayerSubConfig.STATIC_OVERRIDABLE_OPTIONS);
	}

	@Override
	public void setOtherPlayerConfig(PlayerConfigClientStorage otherPlayerConfig) {
		this.otherPlayerConfig = otherPlayerConfig;
	}

	@Override
	public PlayerConfigClientStorage getOtherPlayerConfig() {
		return otherPlayerConfig;
	}

	public void setDynamicOptions(PlayerConfigDynamicOptions dynamicOptions) {
		this.dynamicOptions = dynamicOptions;
		overridableOptions.clear();
		overridableOptions.addAll(PlayerSubConfig.STATIC_OVERRIDABLE_OPTIONS);
		overridableOptions.addAll(dynamicOptions.getOptions().values());
	}

	@Nullable
	public PlayerConfigDynamicOptions getDynamicOptions() {
		return dynamicOptions;
	}

	@Override
	public PlayerConfigClientStorage.FinalBuilder beginConfigStorageBuild(MapFactory mapFactory) {
		return PlayerConfigClientStorage.FinalBuilder.begin(mapFactory).setManager(this);
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
				.setManager(this)
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
				.setManager(this)
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
				.setManager(this)
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
				.setManager(this)
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
				.setManager(this)
				.setDefaultPlayerConfigData(getDefaultPlayerConfig())
				.build()
				);
	}

	@Override
	public void openOtherPlayerConfigScreen(@Nullable Screen escape, @Nullable Screen parent, @Nonnull String playerName) {
		if(!playerName.isEmpty())
			Minecraft.getInstance().setScreen(new OtherPlayerConfigWaitScreen(escape, parent, playerName));
	}

	@Nonnull
	@Override
	public Stream<IPlayerConfigOptionSpecAPI<?>> getAllOptionsStream() {
		return Stream.concat(PlayerConfigOptions.OPTIONS.values().stream(), dynamicOptions == null ? Stream.empty() : dynamicOptions.getOptions().values().stream());
	}

	public int getOptionCount(){
		return PlayerConfigOptions.OPTIONS.size() + (dynamicOptions == null ? 0 : dynamicOptions.getOptions().size());
	}

	@Nullable
	@Override
	public IPlayerConfigOptionSpecAPI<?> getOptionForId(@Nonnull String id) {
		IPlayerConfigOptionSpecAPI<?> result = PlayerConfigOptions.OPTIONS.get(id);
		if(result == null && dynamicOptions != null)
			result = dynamicOptions.getOptions().get(id);
		return result;
	}
	
	public Set<IPlayerConfigOptionSpecAPI<?>> getOverridableOptions() {
		return overridableOptions;
	}

	public static final class Builder {

		private Builder() {
		}

		public Builder setDefault() {
			return this;
		}

		public PlayerConfigClientStorageManager build() {
			PlayerConfigClientStorageManager manager = new PlayerConfigClientStorageManager(new HashSet<>(PlayerSubConfig.STATIC_OVERRIDABLE_OPTIONS));
			PlayerConfigClientStorage serverClaimsConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.SERVER).setOwner(PlayerConfig.SERVER_CLAIM_UUID).setManager(manager).build();
			PlayerConfigClientStorage expiredClaimsConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.EXPIRED).setOwner(PlayerConfig.EXPIRED_CLAIM_UUID).setManager(manager).build();
			PlayerConfigClientStorage wildernessConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.WILDERNESS).setOwner(null).setManager(manager).build();
			PlayerConfigClientStorage defaultPlayerConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.DEFAULT_PLAYER).setOwner(null).setManager(manager).build();
			PlayerConfigClientStorage myPlayerConfig = PlayerConfigClientStorage.FinalBuilder.begin(LinkedHashMap::new).setType(PlayerConfigType.PLAYER).setOwner(null).setManager(manager).build();
			manager.set(serverClaimsConfig, expiredClaimsConfig, wildernessConfig, defaultPlayerConfig, myPlayerConfig);
			return manager;
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
