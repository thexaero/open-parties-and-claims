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

import xaero.pac.common.config.IForgeConfigSpecBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PlayerConfigRangedOptionSpec<T> extends PlayerConfigOptionSpec<T> {
	
	private final T minValue;
	private final T maxValue;

	private PlayerConfigRangedOptionSpec(Class<T> type, String id, List<String> path, T defaultValue, BiFunction<PlayerConfig<?>, T, T> defaultReplacer, String comment,
			String translation, Function<String, T> commandInputParser, Function<T, String> commandOutputWriter, Predicate<T> validator, T minValue, T maxValue, String tooltipPrefix) {
		super(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, validator, tooltipPrefix);
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@Override
	public PlayerConfigOptionSpec<T> applyToForgeSpec(IForgeConfigSpecBuilder builder) {
		IForgeConfigSpecBuilder b = buildForgeSpec(builder);
		if(type == Integer.class)
			b.defineInRange(id, (int)defaultValue, (int)minValue, (int)maxValue);
		else 
			b.defineInRange(id, (double)defaultValue, (double)minValue, (double)maxValue);
		return this;
	}

	final static class Builder<T> extends PlayerConfigOptionSpec.Builder<T, Builder<T>> {

		private T minValue;
		private T maxValue;
		
		protected Builder(Class<T> valueType) {
			super(valueType);
		}
		
		@SuppressWarnings("unchecked")
		private T getAbsoluteMax() {
			if(type == Integer.class)
				return (T) Integer.valueOf(Integer.MAX_VALUE);
			else 
				return (T) Double.valueOf(Double.MAX_VALUE);
		}
		
		@SuppressWarnings("unchecked")
		private T getAbsoluteMin() {
			if(type == Integer.class)
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
		
		public static <T> Builder<T> begin(Class<T> valueType){
			if(valueType != Integer.class && valueType != Double.class)
				throw new IllegalArgumentException();
			return new Builder<T>(valueType).setDefault();
		}
		
		@Override
		public PlayerConfigOptionSpec<T> build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(minValue == null || maxValue == null)
				throw new IllegalStateException();
			Predicate<T> normalValidator = getValidator();
			setValidator(v -> {
				if(!normalValidator.test(v))
					return false;
				double value;
				double minValueDouble;
				double maxValueDouble;
				if(type == Integer.class) {
					value = (double) (int) v;
					minValueDouble = (double) (int) minValue;
					maxValueDouble = (double) (int) maxValue;
				} else {
					value = (double) v;
					minValueDouble = (double) minValue;
					maxValueDouble = (double) maxValue;
				}
				return value >= minValueDouble && value <= maxValueDouble;
			});
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
			return super.build(dest);
		}

		@Override
		protected PlayerConfigRangedOptionSpec<T> buildInternally(List<String> path, Function<String, T> commandInputParser) {
			return new PlayerConfigRangedOptionSpec<T>(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, getValidator(), minValue, maxValue, tooltipPrefix);
		}
		
	}
	
}
