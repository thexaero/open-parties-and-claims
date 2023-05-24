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

public final class DropdownWidgetListElement<T> extends SimpleValueWidgetListElement<T, DropdownWidgetListElement<T>> implements IDropDownWidgetCallback {

	private final Consumer<T> valueChangeConsumer;
	private final List<T> options;
	private int currentIndex;
	private final Component title;

	private DropdownWidgetListElement(int w, int h, boolean mutable, BiFunction<DropdownWidgetListElement<T>, Vec3i, AbstractWidget> widgetSupplier, List<FormattedCharSequence> tooltip, T startValue, int startIndex, List<T> options, Consumer<T> valueChangeConsumer, Component title) {
		super(startValue, w, h, mutable, widgetSupplier, tooltip);
		this.options = options;
		this.currentIndex = startIndex;
		this.valueChangeConsumer = valueChangeConsumer;
		this.title = title;
	}

	@Override
	public void render(GuiGraphics guiGraphics) {
		super.render(guiGraphics);
		guiGraphics.drawString(Minecraft.getInstance().font, title, x, y + 6, mutable ? -1 : 14737632/*copied from editbox class*/);

	}

	@Override
	public boolean onSelected(DropDownWidget menu, int selected) {
		currentIndex = selected;
		draftValue = options.get(currentIndex);
		valueChangeConsumer.accept(draftValue);
		return true;
	}

	public static final class Builder<T> extends SimpleValueWidgetListElement.Builder<T, DropdownWidgetListElement<T>, Builder<T>> {

		private int startIndex;
		private List<T> options;
		private Consumer<T> valueChangeConsumer;
		private Component title;

		@Override
		public Builder<T> setDefault() {
			super.setDefault();
			setStartIndex(-1);
			setOptions(null);
			setValueChangeConsumer(v -> {});
			setTitle(null);
			return self;
		}

		@Override
		public Builder<T> setStartValue(T startValue) {
			if(startValue != null)
				throw new IllegalArgumentException();
			return super.setStartValue(startValue);
		}

		public Builder<T> setStartIndex(int startIndex) {
			this.startIndex = startIndex;
			return self;
		}

		public Builder<T> setOptions(List<T> options) {
			this.options = options;
			return self;
		}

		public Builder<T> setValueChangeConsumer(Consumer<T> valueChangeConsumer) {
			this.valueChangeConsumer = valueChangeConsumer;
			return self;
		}

		public Builder<T> setTitle(Component title) {
			this.title = title;
			return self;
		}

		public DropdownWidgetListElement<T> build() {
			if(startIndex == -1 || options == null || valueChangeConsumer == null || title == null)
				throw new IllegalStateException();
			startValue = options.get(startIndex);
			return super.build();
		}

		@Override
		protected DropdownWidgetListElement<T> buildInternal() {
			String[] stringOptions = new String[options.size()];
			for(int i = 0; i < options.size(); i++)
				stringOptions[i] = Objects.toString(options.get(i));
			int titleWidth = Minecraft.getInstance().font.width(title);
			int margin = 4;
			BiFunction<DropdownWidgetListElement<T>, Vec3i, AbstractWidget> widgetSupplier = (el, xy) ->
					DropDownWidget.Builder.begin()
							.setX(xy.getX() + titleWidth + margin)
							.setY(xy.getY() + 4)
							.setW(w - titleWidth - margin - 1)
							.setOptions(stringOptions)
							.setNarrationTitle(title)
							.setCallback(el)
							.setContainer((IDropDownContainer) Minecraft.getInstance().screen)
							.setSelected(el.currentIndex).build();
			return new DropdownWidgetListElement<>(w, h, mutable, widgetSupplier, tooltip, startValue, startIndex, List.copyOf(options), valueChangeConsumer, title);
		}
		
		public static <T> Builder<T> begin() {
			return new Builder<T>().setDefault();
		}
		
	}

}
