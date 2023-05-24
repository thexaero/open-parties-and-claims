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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class PickerWidgetListElement<T> extends SimpleValueWidgetListElement<T, PickerWidgetListElement<T>> {

	private int currentIndex;
	private final List<T> options;
	private final T defaultValue;
	private Button prevButton;
	private Button nextButton;
	private Button defaultButton;
	private final BiConsumer<T, Button> valueChangeConsumer;

	private PickerWidgetListElement(int w, int h, boolean mutable, BiFunction<PickerWidgetListElement<T>, Vec3i, AbstractWidget> widgetSupplier, List<FormattedCharSequence> tooltip, T startValue, int startIndex, List<T> options, T defaultValue, BiConsumer<T, Button>  valueChangeConsumer) {
		super(startValue, w, h, mutable, widgetSupplier, tooltip);
		this.options = options;
		this.currentIndex = startIndex;
		this.defaultValue = defaultValue;
		this.valueChangeConsumer = valueChangeConsumer;
	}
	
	@Override
	public AbstractWidget screenInit(int x, int y, WidgetListScreen screen, List<EditBox> tickableBoxes) {
		prevButton = (Button) super.screenInit(x, y, screen, tickableBoxes);
		screen.addRenderableWidget(defaultButton = Button.builder(Component.literal("-"), this::onDefaultButton).bounds(x + w - 20, y, 20, 20).build());
		updateDefaultButton();
		screen.addRenderableWidget(nextButton = Button.builder(Component.literal(">"), this::onNextButton).bounds(x + w - 40, y, 20, 20).build());
		return prevButton;
	}

	private void updateDefaultButton(){
		defaultButton.active = !Objects.equals(draftValue, defaultValue);
	}

	private void updateValue(Button b){
		draftValue = options.get(currentIndex);
		valueChangeConsumer.accept(draftValue, b);
		updateDefaultButton();
	}

	private void onDefaultButton(Button b) {
		currentIndex = options.indexOf(defaultValue);
		if(currentIndex == -1)
			currentIndex = 0;
		updateValue(b);
	}

	private void onPrevButton(Button b) {
		currentIndex--;
		if(currentIndex < 0)
			currentIndex = options.size() - 1;
		updateValue(b);
	}
	
	private void onNextButton(Button b) {
		currentIndex = (currentIndex + 1) % options.size();
		updateValue(b);
	}

	@Override
	public void render(GuiGraphics guiGraphics) {
		super.render(guiGraphics);
		String subName = Objects.toString(draftValue);
		int subNameW = Minecraft.getInstance().font.width(subName);
		guiGraphics.drawString(Minecraft.getInstance().font, subName, x + 90 - subNameW / 2, y + 6, mutable ? -1 : 14737632/*copied from editbox class*/);
	}
	
	public static final class Builder<T> extends SimpleValueWidgetListElement.Builder<T, PickerWidgetListElement<T>, Builder<T>> {

		private int startIndex;
		private List<T> options;
		private T defaultValue;
		private BiConsumer<T, Button> valueChangeConsumer;

		@Override
		public Builder<T> setDefault() {
			super.setDefault();
			setStartIndex(-1);
			setOptions(null);
			setDefaultValue(null);
			setValueChangeConsumer((v,b) -> {});
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

		public Builder<T> setDefaultValue(T defaultValue) {
			this.defaultValue = defaultValue;
			return self;
		}

		public Builder<T> setValueChangeConsumer(BiConsumer<T, Button> valueChangeConsumer) {
			this.valueChangeConsumer = valueChangeConsumer;
			return self;
		}

		public PickerWidgetListElement<T> build() {
			if(startIndex == -1 || options == null || valueChangeConsumer == null)
				throw new IllegalStateException();
			startValue = options.get(startIndex);
			if(defaultValue == null)
				defaultValue = startValue;
			return super.build();
		}

		@Override
		protected PickerWidgetListElement<T> buildInternal() {
			BiFunction<PickerWidgetListElement<T>, Vec3i, AbstractWidget> widgetSupplier = (el, xy) ->
					Button.builder(Component.literal("<"), el::onPrevButton).bounds(xy.getX(), xy.getY(), 20, 20).build();
			return new PickerWidgetListElement<>(w, h, mutable, widgetSupplier, tooltip, startValue, startIndex, options, defaultValue, valueChangeConsumer);
		}
		
		public static <T> Builder<T> begin() {
			return new Builder<T>().setDefault();
		}
		
	}

}
