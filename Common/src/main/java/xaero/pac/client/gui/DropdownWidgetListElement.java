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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.client.gui.widget.dropdown.DropDownWidget;
import xaero.pac.client.gui.widget.dropdown.IDropDownContainer;
import xaero.pac.client.gui.widget.dropdown.IDropDownWidgetCallback;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DropdownWidgetListElement<C, T> extends SimpleValueWidgetListElement<C, T, DropdownWidgetListElement<C, T>> implements IDropDownWidgetCallback {

	private final Consumer<T> valueChangeConsumer;
	private final List<T> options;
	private int currentIndex;
	private final Component title;

	private DropdownWidgetListElement(
			int w,
			int h,
			boolean mutable,
			BiFunction<DropdownWidgetListElement<C, T>, Vec3i,
			AbstractWidget> widgetSupplier,
			Supplier<String> searchName,
			Supplier<C> category,
			Function<T, String> valueNamer,
			List<FormattedCharSequence> tooltip,
			T startValue,
			int startIndex,
			List<T> options,
			Consumer<T> valueChangeConsumer,
			Component title
	) {
		super(startValue, w, h, mutable, widgetSupplier, searchName, category, valueNamer, tooltip);
		this.options = options;
		this.currentIndex = startIndex;
		this.valueChangeConsumer = valueChangeConsumer;
		this.title = title;
	}

	@Override
	public void render(GuiGraphics guiGraphics) {
		super.render(guiGraphics);
		guiGraphics.drawString(Minecraft.getInstance().font, title, x + 2, y + 6, mutable ? -1 : 14737632/*copied from editbox class*/);

	}

	@Override
	public boolean onSelected(DropDownWidget menu, int selected) {
		currentIndex = selected;
		draftValue = options.get(currentIndex);
		valueChangeConsumer.accept(draftValue);
		return true;
	}

	public static final class Builder<C, T> extends SimpleValueWidgetListElement.Builder<C, T, DropdownWidgetListElement<C, T>, Builder<C, T>> {

		private int startIndex;
		private List<T> options;
		private Consumer<T> valueChangeConsumer;
		private Component title;

		@Override
		public Builder<C, T> setDefault() {
			super.setDefault();
			setStartIndex(-1);
			setOptions(null);
			setValueChangeConsumer(v -> {});
			setTitle(null);
			return self;
		}

		@Override
		public Builder<C, T> setStartValue(T startValue) {
			if(startValue != null)
				throw new IllegalArgumentException();
			return super.setStartValue(startValue);
		}

		public Builder<C, T> setStartIndex(int startIndex) {
			this.startIndex = startIndex;
			return self;
		}

		public Builder<C, T> setOptions(List<T> options) {
			this.options = options;
			return self;
		}

		public Builder<C, T> setValueChangeConsumer(Consumer<T> valueChangeConsumer) {
			this.valueChangeConsumer = valueChangeConsumer;
			return self;
		}

		public Builder<C, T> setTitle(Component title) {
			this.title = title;
			return self;
		}

		public DropdownWidgetListElement<C, T> build() {
			if(startIndex == -1 || options == null || valueChangeConsumer == null || title == null)
				throw new IllegalStateException();
			startValue = options.get(startIndex);
			if(searchName == null)
				setSearchName(title::getString);
			return super.build();
		}

		@Override
		protected DropdownWidgetListElement<C, T> buildInternal() {
			String[] stringOptions = new String[options.size()];
			for(int i = 0; i < options.size(); i++)
				stringOptions[i] = Objects.toString(options.get(i));
			int titleWidth = Minecraft.getInstance().font.width(title);
			int margin = 6;
			BiFunction<DropdownWidgetListElement<C, T>, Vec3i, AbstractWidget> widgetSupplier = (el, xy) ->
					DropDownWidget.Builder.begin()
							.setX(xy.getX() + titleWidth + margin)
							.setY(xy.getY() + 4)
							.setW(w - titleWidth - margin - 1)
							.setOptions(stringOptions)
							.setNarrationTitle(title)
							.setCallback(el)
							.setContainer((IDropDownContainer) Minecraft.getInstance().screen)
							.setSelected(el.currentIndex).build();
			return new DropdownWidgetListElement<>(
					w, h, mutable, widgetSupplier, searchName, category, valueNamer, tooltip,
					startValue, startIndex, List.copyOf(options), valueChangeConsumer, title
			);
		}
		
		public static <C, T> Builder<C, T> begin() {
			return new Builder<C, T>().setDefault();
		}
		
	}

}
