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

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PlayerConfigStringOptionSpec extends PlayerConfigOptionSpec<String> {
	
	private final int maxLength;

	private PlayerConfigStringOptionSpec(Class<String> type, String id, List<String> path, String defaultValue, BiFunction<PlayerConfig<?>, String, String> defaultReplacer, String comment,
			String translation, Function<String, String> commandInputParser, Function<String, String> commandOutputWriter, Predicate<String> validator, int maxLength, String tooltipPrefix) {
		super(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, validator, tooltipPrefix);
		this.maxLength = maxLength;
	}
	
	public int getMaxLength() {
		return maxLength;
	}

	final static class Builder extends PlayerConfigOptionSpec.Builder<String, Builder> {

		private int maxLength;
		
		protected Builder() {
			super(String.class);
		}
		
		@Override
		public Builder setDefault() {
			setMaxLength(32);
			return super.setDefault();
		}
		
		public Builder setMaxLength(int maxLength) {
			this.maxLength = maxLength;
			return this;
		}
		
		public static <T> Builder begin(){
			return new Builder().setDefault();
		}
		
		@Override
		public PlayerConfigStringOptionSpec build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			Predicate<String> normalValidator = getValidator();
			setValidator(v -> {
				if(!normalValidator.test(v))
					return false;
				return v.length() <= maxLength;
			});
			if(tooltipPrefix == null)
				tooltipPrefix = String.format("~%s", maxLength);
			return (PlayerConfigStringOptionSpec) super.build(dest);
		}

		@Override
		protected PlayerConfigStringOptionSpec buildInternally(List<String> path, Function<String, String> commandInputParser) {
			return new PlayerConfigStringOptionSpec(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, getValidator(), maxLength, tooltipPrefix);
		}
		
	}
	
}
