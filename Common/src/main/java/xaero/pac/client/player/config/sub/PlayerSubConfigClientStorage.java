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

package xaero.pac.client.player.config.sub;

import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.client.player.config.PlayerConfigClientStorageManager;
import xaero.pac.client.player.config.PlayerConfigStringableOptionClientStorage;
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

	private final String subID;

	private PlayerSubConfigClientStorage(PlayerConfigClientStorageManager manager, PlayerConfigType type, UUID owner, Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options, String subID, List<String> subConfigIdsUnmodifiable, SortedValueList<String> subConfigIds, Map<String, PlayerSubConfigClientStorage> subConfigs) {
		super(manager, type, owner, options, subConfigIdsUnmodifiable, subConfigIds, subConfigs);
		this.subID = subID;
	}

	@Override
	protected <T extends Comparable<T>> T getDefaultValue(PlayerConfigOptionSpec<T> option) {
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

	public final static class Builder extends PlayerConfigClientStorage.Builder<Builder> {

		private String subID;

		private Builder(MapFactory mapFactory) {
			super(mapFactory);
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setSubID(null);
			return self;
		}

		public Builder setSubID(String subID) {
			this.subID = subID;
			return self;
		}

		@Override
		public PlayerSubConfigClientStorage build() {
			if(subID == null)
				throw new IllegalStateException();
			return (PlayerSubConfigClientStorage) super.build();
		}

		@Override
		protected PlayerConfigClientStorage buildInternally(Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options) {
			return new PlayerSubConfigClientStorage(manager, type, owner, options, subID, null, null, null);
		}

		public static Builder begin(MapFactory mapFactory){
			return new Builder(mapFactory).setDefault();
		}

	}

}
