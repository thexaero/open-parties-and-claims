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

package xaero.pac.client.player.config;

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class PlayerConfigStringableOptionClientStorage<T extends Comparable<T>> extends PlayerConfigOptionClientStorage<T> implements IPlayerConfigStringableOptionClientStorage<T> {
	
	private final BiPredicate<IPlayerConfigClientStorageAPI<?>, String> stringValidator;
	
	private PlayerConfigStringableOptionClientStorage(PlayerConfigOptionSpec<T> option, T value, BiPredicate<IPlayerConfigClientStorageAPI<?>, String> stringValidator) {
		super(option, value);
		this.stringValidator = stringValidator;
	}
	
	@Nonnull
	@Override
	public Function<String, T> getCommandInputParser() {
		return option.getCommandInputParser();
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	@Override
	public Function<Object, Component> getCommandOutputWriterCast() {
		return (Function<Object, Component>) (Object) option.getCommandOutputWriter();
	}

	@Nonnull
	@Override
	public BiPredicate<IPlayerConfigClientStorageAPI<?>, String> getStringValidator(){
		return stringValidator;
	}
	
	public static final class Builder<T extends Comparable<T>> extends PlayerConfigOptionClientStorage.Builder<T, Builder<T>> {

		@Override
		protected PlayerConfigOptionClientStorage<T> buildInternally() {
			BiPredicate<IPlayerConfigClientStorageAPI<?>, String> stringValidatorPredicate = (c, s) -> {
				T parsedValue;
				try {
					parsedValue = option.getCommandInputParser().apply(s);
				} catch(IllegalArgumentException iae) {
					return false;
				}
				return option.getClientSideValidator().test(c, parsedValue);
			};
			return new PlayerConfigStringableOptionClientStorage<T>(option, value, stringValidatorPredicate);
		}
		
		@Override
		public PlayerConfigStringableOptionClientStorage<T> build() {
			return (PlayerConfigStringableOptionClientStorage<T>) super.build();
		}
		
		public static <T extends Comparable<T>> Builder<T> begin(){
			return new Builder<T>().setDefault();
		}
		
	}

}
