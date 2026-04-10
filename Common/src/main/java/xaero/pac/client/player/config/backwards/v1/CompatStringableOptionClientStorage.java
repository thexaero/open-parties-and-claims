/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.player.config.backwards.v1;

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.client.player.config.api.v1.IPlayerConfigStringableOptionClientStorageAPI;
import xaero.pac.common.server.player.config.api.v1.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.backwards.v1.CompatPlayerConfigOptionSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Function;

@Deprecated
public class CompatStringableOptionClientStorage<T extends Comparable<T>, R> implements IPlayerConfigStringableOptionClientStorageAPI<T> {

	public final xaero.pac.client.player.config.api.v2.IPlayerConfigStringableOptionClientStorageAPI<R> realOption;
	public final CompatPlayerConfigOptionSpec<T, R> compatOption;
	public final BiPredicate<IPlayerConfigClientStorageAPI, String> stringValidator;

	public CompatStringableOptionClientStorage(xaero.pac.client.player.config.api.v2.IPlayerConfigStringableOptionClientStorageAPI<R> realOption, CompatPlayerConfigOptionSpec<T, R> compatOption) {
		this.realOption = realOption;
		this.compatOption = compatOption;
		this.stringValidator = (c, s) -> {
			T parsedValue;
			try {
				parsedValue = compatOption.getCommandInputParser().apply(s);
			} catch(IllegalArgumentException iae) {
				return false;
			}
			return compatOption.getClientSideValidator().test(c, parsedValue);
		};
	}

	@Override
	@Nonnull
	public IPlayerConfigOptionSpecAPI<T> getOption() {
		return compatOption;
	}

	@Override
	@Nonnull
	public String getId() {
		return compatOption.getId();
	}

	@Override
	@Nonnull
	public String getComment() {
		return compatOption.getComment();
	}

	@Override
	@Nonnull
	public String getTranslation() {
		return compatOption.getTranslation();
	}

	@Override
	public Object[] getTranslationArgs() {
		return compatOption.getTranslationArgs();
	}

	@Override
	public String getCommentTranslation() {
		return compatOption.getCommentTranslation();
	}

	@Override
	public Object[] getCommentTranslationArgs() {
		return compatOption.getCommentTranslationArgs();
	}

	@Override
	@Nonnull
	public Class getType() {
		return compatOption.getType();
	}

	@Override
	@Nullable
	public T getValue() {
		R actualValue = realOption.getValue();
		if(actualValue == null)
			return null;
		return compatOption.fromRealConverter.apply(actualValue);
	}

	@Override
	@Nonnull
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getValidator() {
		return compatOption.getClientSideValidator();
	}

	@Nullable
	@Override
	public String getTooltipPrefix() {
		return compatOption.getTooltipPrefix();
	}

	@Override
	public boolean isDefaulted() {
		return realOption.isDefaulted();
	}

	@Override
	public boolean isMutable() {
		return realOption.isMutable();
	}

	@Override
	public Function getCommandInputParser() {
		return compatOption.getCommandInputParser();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Function<Object, Component> getCommandOutputWriterCast() {
		return (Function<Object, Component>)(Object)compatOption.getCommandOutputWriter();
	}

	@Override
	public BiPredicate<IPlayerConfigClientStorageAPI, String> getStringValidator() {
		return stringValidator;
	}

}
