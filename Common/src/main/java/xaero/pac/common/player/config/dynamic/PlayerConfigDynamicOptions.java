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

package xaero.pac.common.player.config.dynamic;

import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerConfigDynamicOptions {

	private final Map<String, IPlayerConfigOptionSpecAPI<?>> options;

	private PlayerConfigDynamicOptions(Map<String, IPlayerConfigOptionSpecAPI<?>> options) {
		this.options = options;
	}

	public Map<String, IPlayerConfigOptionSpecAPI<?>> getOptions() {
		return options;
	}

	public static final class Builder {

		private Map<String, IPlayerConfigOptionSpecAPI<?>> options;

		private Builder(){
			options = new LinkedHashMap<>();
		}

		public Builder setDefault() {
			options.clear();
			return this;
		}

		public Builder addOption(PlayerConfigOptionSpec<?> option){
			if(!option.isDynamic())
				throw new IllegalArgumentException("tried to add a static option to dynamic options!");
			options.put(option.getId(), option);
			return this;
		}

		public PlayerConfigDynamicOptions build(){
			return new PlayerConfigDynamicOptions(Collections.unmodifiableMap(options));
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
