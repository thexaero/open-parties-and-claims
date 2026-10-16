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

package xaero.pac.client.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleValueWidgetListElement<C, T, E extends SimpleValueWidgetListElement<C, T, E>> extends WidgetListElement<C, E> {
	
	protected T draftValue;
	protected final Function<T, String> valueNamer;
	
	protected SimpleValueWidgetListElement(
			T startValue,
			int w,
			int h,
			boolean mutable,
			BiFunction<E, Vec3i, AbstractWidget> widgetSupplier,
			Supplier<String> searchName,
			Supplier<C> category,
			Function<T, String> valueNamer,
			List<FormattedCharSequence> tooltip
	) {
		super(w, h, mutable, widgetSupplier, searchName, category, tooltip);
		this.draftValue = startValue;
		this.valueNamer = valueNamer;
	}
	
	public T getDraftValue() {
		return draftValue;
	}
	
	public void setDraftValue(T draftValue) {
		this.draftValue = draftValue;
	}

	@Override
	public String getSearchTextNoTooltip() {
		return super.getSearchTextNoTooltip() + ": " + valueNamer.apply(draftValue);
	}

	public static abstract class Builder<C, T, E extends SimpleValueWidgetListElement<C, T, E>, B extends Builder<C, T, E, B>> extends WidgetListElement.Builder<C, E, B> {
		
		protected T startValue;
		protected Function<T, String> valueNamer;

		@Override
		public B setDefault() {
			super.setDefault();
			setStartValue(null);
			return self;
		}

		public B setStartValue(T startValue) {
			this.startValue = startValue;
			return self;
		}

		public B setValueNamer(Function<T, String> valueNamer) {
			this.valueNamer = valueNamer;
			return self;
		}

		@Override
		public E build() {
			if(valueNamer == null)
				throw new IllegalStateException();
			return super.build();
		}
		
	}
	
	public static final class Final<C, T> extends SimpleValueWidgetListElement<C, T, Final<C, T>>{

		private Final(
				T startValue,
				int w,
				int h,
				boolean mutable,
				BiFunction<Final<C, T>, Vec3i, AbstractWidget> widgetSupplier,
				Supplier<String> searchName,
				Supplier<C> category,
				Function<T, String> valueNamer,
				List<FormattedCharSequence> tooltip
		) {
			super(startValue, w, h, mutable, widgetSupplier, searchName, category, valueNamer, tooltip);
		}
		
	}
	
	public static final class FinalBuilder<C, T> extends Builder<C, T, Final<C, T>, FinalBuilder<C, T>> {
		
		@Override
		protected Final<C, T> buildInternal() {
			return new Final<>(startValue, w, h, mutable, widgetSupplier, searchName, category, valueNamer, tooltip);
		}
		
		public static <C, T> FinalBuilder<C, T> begin() {
			return new FinalBuilder<C, T>().setDefault();
		}
		
	}

}
