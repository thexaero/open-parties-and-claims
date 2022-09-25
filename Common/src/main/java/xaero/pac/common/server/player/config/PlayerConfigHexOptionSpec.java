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

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PlayerConfigHexOptionSpec extends PlayerConfigOptionSpec<Integer> {

	private PlayerConfigHexOptionSpec(Class<Integer> type, String id, List<String> path, Integer defaultValue, BiFunction<PlayerConfig<?>, Integer, Integer> defaultReplacer, String comment,
									  String translation, Function<String, Integer> commandInputParser, Function<Integer, Component> commandOutputWriter, BiPredicate<PlayerConfig<?>, Integer> serverSideValidator, BiPredicate<PlayerConfigClientStorage, Integer> clientSideValidator, String tooltipPrefix, Predicate<PlayerConfigType> configTypeFilter) {
		super(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter);
	}

	public final static class Builder extends PlayerConfigOptionSpec.Builder<Integer, Builder> {
		
		protected Builder() {
			super(Integer.class);
		}
		
		@Override
		public Builder setDefault() {
			super.setDefault();
			setCommandOutputWriter(o -> Component.literal(Integer.toUnsignedString(o, 16).toUpperCase()));
			return self;
		}
		
		public static <T> Builder begin(){
			return new Builder().setDefault();
		}
		
		@Override
		public PlayerConfigHexOptionSpec build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(tooltipPrefix == null)
				tooltipPrefix = "(hex code)";
			return (PlayerConfigHexOptionSpec) super.build(dest);
		}

		@Override
		protected PlayerConfigHexOptionSpec buildInternally(List<String> path, Function<String, Integer> commandInputParser) {
			commandInputParser = s -> {
				try {
					return Integer.parseUnsignedInt(s, 16);
				} catch(NumberFormatException nfe) {
					throw new IllegalArgumentException(nfe);
				}
			};
			return new PlayerConfigHexOptionSpec(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter);
		}
		
	}
	
}
