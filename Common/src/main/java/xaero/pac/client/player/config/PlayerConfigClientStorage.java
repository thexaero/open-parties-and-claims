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

import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class PlayerConfigClientStorage implements IPlayerConfigClientStorage<PlayerConfigStringableOptionClientStorage<?>> {
	
	private final PlayerConfigType type;
	private final UUID owner;
	private final Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options;

	private PlayerConfigClientStorage(PlayerConfigType type, UUID owner, Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options) {
		super();
		this.type = type;
		this.owner = owner;
		this.options = options;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> PlayerConfigStringableOptionClientStorage<T> getOptionStorage(@Nonnull PlayerConfigOptionSpec<T> option){
		return (PlayerConfigStringableOptionClientStorage<T>) options.get(option);
	}
	
	@Nonnull
	@Override
	public PlayerConfigType getType() {
		return type;
	}

	@Nullable
	@Override
	public UUID getOwner() {
		return owner;
	}

	@Nonnull
	@Override
	public Stream<PlayerConfigStringableOptionClientStorage<?>> optionStream(){
		return options.values().stream();
	}

	@Override
	public int size() {
		return options.size();
	}
	
	public static final class Builder implements IBuilder<PlayerConfigClientStorage> {

		private PlayerConfigType type;
		private UUID owner;
		private final MapFactory mapFactory;

		private Builder(MapFactory mapFactory) {
			super();
			this.mapFactory = mapFactory;
		}
		
		@Override
		public Builder setDefault() {
			setOwner(null);
			setType(null);
			return this;
		}

		@Override
		public Builder setType(PlayerConfigType type) {
			this.type = type;
			return this;
		}

		@Override
		public Builder setOwner(UUID owner) {
			this.owner = owner;
			return this;
		}
		
		private <T> PlayerConfigStringableOptionClientStorage<T> buildOptionStorage(PlayerConfigOptionSpec<T> option){
			PlayerConfigStringableOptionClientStorage.Builder<T> builder = PlayerConfigStringableOptionClientStorage.Builder.begin();
			builder.setOption(option).setValue(option.getDefaultValue());
			return builder.build();
		}

		@Override
		public PlayerConfigClientStorage build() {
			if(type == null)
				throw new IllegalStateException();
			Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options = mapFactory.get();
			PlayerConfig.OPTIONS.forEach((k, option) -> {
				options.put(option, buildOptionStorage(option));
			});
			return new PlayerConfigClientStorage(type, owner, options);
		}
		
		public static Builder begin(MapFactory mapFactory) {
			return new Builder(mapFactory).setDefault();
		}
		
	}

}
