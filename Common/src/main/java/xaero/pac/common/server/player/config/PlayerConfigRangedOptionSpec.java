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

package xaero.pac.common.server.player.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.ClientboundPlayerConfigDynamicOptionsPacket;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.change.IPlayerConfigChangeHandler;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class PlayerConfigRangedOptionSpec<T> extends PlayerConfigOptionSpec<T> {
	
	private final T minValue;
	private final T maxValue;

	private PlayerConfigRangedOptionSpec(
			PlayerConfigOptionValueType<T> type,
			String id,
			String shortenedId,
			List<String> path,
			T defaultValue,
			BiFunction<PlayerConfig<?>, T, T> defaultReplacer,
			String comment,
			String translation,
			String[] translationArgs,
			String commentTranslation,
			String[] commentTranslationArgs,
			PlayerConfigOptionCategory category,
			BiPredicate<PlayerConfig<?>, T> serverSideValidator,
			BiPredicate<PlayerConfigClientStorage, T> clientSideValidator,
			T minValue,
			T maxValue,
			String tooltipPrefix,
			Predicate<PlayerConfigType> configTypeFilter,
			ClientboundPlayerConfigDynamicOptionsPacket.OptionType syncOptionType,
			boolean dynamic,
			boolean overridable,
			boolean forcedPlayerConfigurable,
			boolean directlyConfigurable,
			IPlayerConfigChangeHandler<T> serverChangeHandler,
			boolean syncable,
			Function<PlayerConfig<?>, Stream<String>> commandSuggestionGetter,
			boolean affectsClaimsVisually
	) {
		super(
				type, id, shortenedId, path, defaultValue, defaultReplacer,
				comment, translation, translationArgs, commentTranslation,
				commentTranslationArgs, category,
				serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter,
				syncOptionType, dynamic, overridable, forcedPlayerConfigurable, directlyConfigurable,
				serverChangeHandler, syncable, commandSuggestionGetter, affectsClaimsVisually
		);
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@Override
	public PlayerConfigOptionSpec<T> applyToForgeSpec(ModConfigSpec.Builder builder) {
		ModConfigSpec.Builder b = buildForgeSpec(builder);
		if(valueType.getJType() == Integer.class)
			b.defineInRange(id, (Integer)defaultValue, (Integer)minValue, (Integer)maxValue);
		else 
			b.defineInRange(id, (Double)defaultValue, (Double)minValue, (Double)maxValue);
		return this;
	}

	public T getMinValue() {
		return minValue;
	}

	public T getMaxValue() {
		return maxValue;
	}

	public final static class Builder<T> extends PlayerConfigOptionSpec.Builder<T, Builder<T>> {

		private T minValue;
		private T maxValue;
		
		private Builder(PlayerConfigOptionValueType<T> valueType) {
			super(valueType);
		}
		
		@SuppressWarnings("unchecked")
		private T getAbsoluteMax() {
			if(valueType.getJType() == Integer.class)
				return (T) Integer.valueOf(Integer.MAX_VALUE);
			else 
				return (T) Double.valueOf(Double.MAX_VALUE);
		}
		
		@SuppressWarnings("unchecked")
		private T getAbsoluteMin() {
			if(valueType.getJType() == Integer.class)
				return (T) Integer.valueOf(Integer.MIN_VALUE);
			else
				return (T) Double.valueOf(Double.MIN_VALUE);
		}

		private void setDefaultMinMaxValues(){
			setMinValue(getAbsoluteMin());
			setMaxValue(getAbsoluteMax());
		}
		
		@Override
		public Builder<T> setDefault() {
			setDefaultMinMaxValues();
			return super.setDefault();
		}
		
		public Builder<T> setMinValue(T minValue) {
			this.minValue = minValue;
			return this;
		}
		
		public Builder<T> setMaxValue(T maxValue) {
			this.maxValue = maxValue;
			return this;
		}
		
		public static <T> Builder<T> begin(PlayerConfigOptionValueType<T> valueType){
			if(valueType.getJType() != Integer.class && valueType.getJType() != Double.class)
				throw new IllegalArgumentException();
			return new Builder<T>(valueType).setDefault();
		}

		@Override
		protected Predicate<T> buildValueValidator() {
			Predicate<T> normalValidator = super.buildValueValidator();
			return v -> {
				if(!normalValidator.test(v))
					return false;
				double value;
				double minValueDouble;
				double maxValueDouble;
				if(valueType.getJType() == Integer.class) {
					value = (double) (Integer) v;
					minValueDouble = (double) (Integer) minValue;
					maxValueDouble = (double) (Integer) maxValue;
				} else {
					value = (Double) v;
					minValueDouble = (Double) minValue;
					maxValueDouble = (Double) maxValue;
				}
				return value >= minValueDouble && value <= maxValueDouble;
			};
		}

		@Override
		public PlayerConfigRangedOptionSpec<T> build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(minValue == null || maxValue == null)
				throw new IllegalStateException();
			if(tooltipPrefix == null) {
				boolean absoluteMin = minValue.equals(getAbsoluteMin());
				boolean absoluteMax = maxValue.equals(getAbsoluteMax());
				if(!absoluteMin && !absoluteMax)
					tooltipPrefix = String.format("%s - %s", minValue, maxValue);
				else if(!absoluteMin)
					tooltipPrefix = String.format(">= %s", minValue);
				else if(!absoluteMax)
					tooltipPrefix = String.format("<= %s", maxValue);
			}
			return (PlayerConfigRangedOptionSpec<T>) super.build(dest);
		}

		@Override
		protected PlayerConfigRangedOptionSpec<T> buildInternally(List<String> path, String shortenedId) {
			return new PlayerConfigRangedOptionSpec<T>(
					valueType, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation,
					translationArgs, commentTranslation, commentTranslationArgs, category,
					serverSideValidator, clientSideValidator, minValue, maxValue, tooltipPrefix, configTypeFilter,
					ClientboundPlayerConfigDynamicOptionsPacket.OptionType.RANGED, dynamic, overridable,
					forcedPlayerConfigurable, directlyConfigurable, serverChangeHandler,
					syncable, commandSuggestionGetter, affectsClaimsVisually
			);
		}
		
	}
	
}
