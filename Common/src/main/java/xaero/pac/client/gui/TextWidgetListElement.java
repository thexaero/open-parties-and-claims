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

import com.google.common.base.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.client.gui.widget.TextWidgetEditBox;

import java.util.List;
import java.util.Objects;
import java.util.function.*;

public final class TextWidgetListElement<C> extends SimpleValueWidgetListElement<C, String, TextWidgetListElement<C>> {

	private final Component title;
	private EditBox editBox;
	private Button confirmButton;
	private Button cancelButton;
	private String confirmedText;
	private final BiConsumer<TextWidgetListElement<C>, String> valueConfirmResponder;
	private final Predicate<String> validator;
	
	private TextWidgetListElement(
			int w,
			int h,
			boolean mutable,
			Component title,
			BiFunction<TextWidgetListElement<C>, Vec3i, AbstractWidget> widgetSupplier,
			Supplier<String> searchName,
			Supplier<C> category,
			Function<String, String> valueNamer,
			List<FormattedCharSequence> tooltip,
			BiConsumer<TextWidgetListElement<C>, String> valueConfirmResponder,
			Predicate<String> validator, String startValue
	) {
		super(startValue, w, h, mutable, widgetSupplier, searchName, category, valueNamer, tooltip);
		this.confirmedText = startValue;
		this.title = title;
		this.valueConfirmResponder = valueConfirmResponder;
		this.validator = validator;
	}
	
	@Override
	public AbstractWidget screenInit(int x, int y, WidgetListScreen screen, List<EditBox> tickableBoxes) {
		editBox = (EditBox) super.screenInit(x, y, screen, tickableBoxes);
		screen.addRenderableWidget(confirmButton = new Button(x + w - 40, y, 20, 20, new TextComponent("✔"), this::onConfirmButton));
		screen.addRenderableWidget(cancelButton = new Button(x + w - 20, y, 20, 20, new TextComponent("❌"), this::onCancelButton));
		confirmButton.active = !confirmedText.equals(draftValue);
		cancelButton.active = !confirmedText.equals(draftValue);
		return editBox;
	}
	
	public boolean onEnterPressed() {
		if(confirmButton.active) {
			confirmButton.onPress();
			return true;
		}
		return false;
	}
	
	private boolean updateBoxColor() {
		boolean validInput = validator.test(draftValue);
		editBox.setTextColor(validInput ? 14737632/*copied from editbox class*/ : 0xFFFF5555);
		return validInput;
	}
	
	private void onTextTyped(String value) {
		if(!draftValue.equals(value)) {
			draftValue = value;
			confirmButton.active = updateBoxColor();
			cancelButton.active = true;
		}
	}
	
	private void onConfirmButton(Button b) {
		if(validator.test(draftValue)) {
			valueConfirmResponder.accept(this, draftValue);
			confirmedText = draftValue;
			confirmButton.active = false;
			cancelButton.active = false;
		}
	}

	public void clearBox(){
		editBox.setValue("");
	}
	
	private void onCancelButton(Button b) {
		editBox.setValue(draftValue = confirmedText);
		confirmButton.active = false;
		cancelButton.active = false;
		updateBoxColor();
	}
	
	@Override
	public final void render(PoseStack poseStack) {
		super.render(poseStack);
		Minecraft.getInstance().font.drawShadow(poseStack, title, x + 2, y + 6, mutable ? -1 : 14737632/*copied from editbox class*/);
	}
	
	public static final class Builder<C> extends SimpleValueWidgetListElement.Builder<C, String, TextWidgetListElement<C>, Builder<C>> {
		
		private Component title;
		private BiConsumer<TextWidgetListElement<C>, String> responder;
		private Predicate<String> filter;
		private Predicate<String> validator;
		private int boxWidth;
		private int maxLength;
		
		@Override
		public Builder<C> setDefault() {
			super.setDefault();
			setBoxWidth(50);
			setTitle(null);
			setResponder(null);
			setFilter(Objects::nonNull);
			setValidator(Objects::nonNull);
			setMaxLength(32);
			setValueNamer(Functions.identity());
			return this;
		}
		
		public Builder<C> setMaxLength(int maxLength) {
			this.maxLength = maxLength;
			return this;
		}
		
		public Builder<C> setBoxWidth(int boxWidth) {
			this.boxWidth = boxWidth;
			return this;
		}
		
		public Builder<C> setTitle(Component title) {
			this.title = title;
			return this;
		}
		
		public Builder<C> setResponder(BiConsumer<TextWidgetListElement<C>, String> responder) {
			this.responder = responder;
			return this;
		}
		
		public Builder<C> setFilter(Predicate<String> filter) {
			this.filter = filter;
			return this;
		}
		
		public Builder<C> setValidator(Predicate<String> validator) {
			this.validator = validator;
			return this;
		}

		@Override
		public Builder<C> setWidgetSupplier(BiFunction<TextWidgetListElement<C>, Vec3i, AbstractWidget> widgetSupplier) {
			if(widgetSupplier != null)
				throw new UnsupportedOperationException();
			return super.setWidgetSupplier(widgetSupplier);
		}

		public TextWidgetListElement<C> build() {
			if(title == null || filter == null)
				throw new IllegalStateException();
			if(searchName == null)
				setSearchName(title::getString);
			BiFunction<TextWidgetListElement<C>, Vec3i, AbstractWidget> widgetSupplier = (el, xy) -> {
				TextWidgetEditBox box = new TextWidgetEditBox(el, Minecraft.getInstance().font, xy.getX() + w - boxWidth - 42, xy.getY(), boxWidth, h, title);
				box.setMaxLength(maxLength);
				box.setValue(el.getDraftValue());
				box.setResponder(s -> el.onTextTyped(s));
				box.setFilter(filter);
				return box;
			};
			super.setWidgetSupplier(widgetSupplier);
			return super.build();
		}
		
		@Override
		protected TextWidgetListElement<C> buildInternal() {
			return new TextWidgetListElement<>(
					w, h, mutable, title, widgetSupplier, searchName, category, valueNamer,
					tooltip, responder, validator, startValue
			);
		}
		
		public static <C> Builder<C> begin() {
			return new Builder<C>().setDefault();
		}
		
	}

}
