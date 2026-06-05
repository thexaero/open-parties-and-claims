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

package xaero.pac.client.player.config.sub;

import xaero.pac.client.player.config.*;
import xaero.pac.client.player.config.group.ClientPlayerConfigGroupManager;
import xaero.pac.common.list.SortedValueList;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class PlayerSubConfigClientStorage extends PlayerConfigClientStorage {

	private final PlayerConfigClientStorage mainConfig;
	private final String subID;

	private PlayerSubConfigClientStorage(
			PlayerConfigClientStorageManager manager,
			PlayerConfigType type,
			UUID owner,
			Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options,
			String subID,
			List<String> subConfigIdsUnmodifiable,
			SortedValueList<String> subConfigIds,
			Map<String, PlayerSubConfigClientStorage> subConfigs,
			ClientPlayerConfigGroupManager playerGroups,
			PlayerConfigClientStorage mainConfig
	) {
		super(
				manager, type, owner, options, subConfigIdsUnmodifiable,
				subConfigIds, subConfigs, playerGroups, null
		);
		this.subID = subID;
		this.mainConfig = mainConfig;
	}

	@Override
	protected <T> T getDefaultValue(PlayerConfigOptionSpec<T> option) {
		return null;
	}

	@Override
	public String getSubId() {
		return subID;
	}

	@Nonnull
	@Override
	public List<String> getSubConfigIds() {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Nonnull
	@Override
	public Stream<IPlayerConfigClientStorage<PlayerConfigStringableOptionClientStorage<?>>> getSubConfigStream() {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public PlayerSubConfigClientStorage getOrCreateSubConfig(String subId) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public void removeSubConfig(String subId) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public int getSubCount() {
		return 0;
	}

	@Override
	public int getSubConfigLimit() {
		return 0;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getMain() {
		return mainConfig;
	}

	@Nonnull
	@Override
	public PlayerConfigClientPermissions getPermissions() {
		return getMain().getPermissions();
	}

	public final static class Builder extends PlayerConfigClientStorage.Builder<Builder> {

		private String subID;
		private PlayerConfigClientStorage mainConfig;

		private Builder(MapFactory mapFactory) {
			super(mapFactory);
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setSubID(null);
			setMainConfig(null);
			return self;
		}

		public Builder setSubID(String subID) {
			this.subID = subID;
			return self;
		}

		public Builder setMainConfig(PlayerConfigClientStorage mainConfig) {
			this.mainConfig = mainConfig;
			return self;
		}

		@Override
		public PlayerSubConfigClientStorage build() {
			if(subID == null || mainConfig == null)
				throw new IllegalStateException();
			return (PlayerSubConfigClientStorage) super.build();
		}

		@Override
		protected PlayerConfigClientStorage buildInternally(Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options) {
			return new PlayerSubConfigClientStorage(
					manager, type, owner, options, subID, null, null,
					null, null, mainConfig
			);
		}

		public static Builder begin(MapFactory mapFactory){
			return new Builder(mapFactory).setDefault();
		}

	}

}
