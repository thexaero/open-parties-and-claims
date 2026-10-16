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

package xaero.pac.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class SimpleCreationScreen<V> extends XPACScreen {

	private final Component chooseId;
	private final Component rules1;
	private final Component rules2;

	private final Component editBoxName;
	private final int maxInputLength;
	private EditBox idTextBox;
	private Button confirmButton;

	protected SimpleCreationScreen(
			Screen escape,
			Screen parent,
			Component title,
			Component chooseId,
			Component rules1,
			Component rules2,
			Component editBoxName,
			int maxInputLength
	) {
		super(escape, parent, title);
		this.chooseId = chooseId;
		this.rules1 = rules1;
		this.rules2 = rules2;
		this.editBoxName = editBoxName;
		this.maxInputLength = maxInputLength;
	}

	@Override
	protected void init() {
		super.init();
		String existingInput = idTextBox == null ? null : idTextBox.getValue();
		idTextBox = new EditBox(
				font, width / 2 - 100, height / 7 + 60,
				200, 20,
				editBoxName
		);
		if(existingInput != null)
			idTextBox.setValue(existingInput);
		idTextBox.setMaxLength(maxInputLength);
		addRenderableWidget(idTextBox);
		idTextBox.setResponder(this::onIdTextBox);
		addRenderableWidget(
				confirmButton = Button.builder(Component.translatable("gui.xaero_pac_confirm"), this::onConfirm)
						.bounds(width / 2 - 105, height / 7 + 128, 100, 20).build()
		);
		addRenderableWidget(
				Button.builder(
						Component.translatable("gui.xaero_pac_cancel"), b -> goBack()
				).bounds(width / 2 + 5, this.height / 7 + 128,
						100, 20).build()
		);
		setFocused(idTextBox);
		idTextBox.setFocused(true);
		updateButtons();
	}

	private void updateButtons(){
		confirmButton.active = confirmButtonIsActive();
	}

	private void onIdTextBox(String s) {
		updateButtons();
	}

	private void onConfirm(Button button) {
		if(!confirmButtonIsActive())
			return;
		String input = idTextBox.getValue();
		handleConfirmation(input);
	}

	private boolean confirmButtonIsActive(){
		return isValid(getInputValidity(idTextBox.getValue()));
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public boolean keyPressed(int code, int $$1, int $$2) {
		if(code == GLFW.GLFW_KEY_ENTER && confirmButtonIsActive()) {
			confirmButton.onPress();
			return true;
		}
		return super.keyPressed(code, $$1, $$2);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		renderBackground(guiGraphics, mouseX, mouseY, partial);
		super.render(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.drawCenteredString(font, title, width / 2, height / 7 - 17, -1);
		guiGraphics.drawCenteredString(font, chooseId, width / 2, this.height / 7 + 25, -1);
		guiGraphics.drawCenteredString(font, rules1, width / 2, this.height / 7 + 35, 0xFFAAAAAA);
		guiGraphics.drawCenteredString(font, rules2, width / 2, this.height / 7 + 45, 0xFFAAAAAA);
		V inputValidity = getInputValidity(idTextBox.getValue());
		Component validityMessage = getValidityMessage(inputValidity);
		if(validityMessage != null)
			guiGraphics.drawCenteredString(font, validityMessage, width / 2, height / 7 + 86, 0xFFFF5555);
	}

	protected abstract V getInputValidity(String input);
	protected abstract boolean isValid(V validity);
	protected abstract Component getValidityMessage(V validity);
	protected abstract void handleConfirmation(String input);

	public static abstract class Builder<V, B extends Builder<V, B>> {

		protected final B self;
		protected Screen parent;
		protected Component title;
		protected Component chooseId;
		protected Component rules1;
		protected Component rules2;
		protected Component editBoxName;
		protected int maxInputLength;

		@SuppressWarnings("unchecked")
		protected Builder(){
			self = (B) this;
		}

		public B setDefault(){
			setParent(null);
			setTitle(null);
			setChooseId(null);
			setMaxInputLength(0);
			setRules1(null);
			setRules2(null);
			return self;
		}

		public B setParent(Screen parent) {
			this.parent = parent;
			return self;
		}

		public B setTitle(Component title) {
			this.title = title;
			return self;
		}

		public B setChooseId(Component chooseId) {
			this.chooseId = chooseId;
			return self;
		}

		public B setRules1(Component rules1) {
			this.rules1 = rules1;
			return self;
		}

		public B setRules2(Component rules2) {
			this.rules2 = rules2;
			return self;
		}

		public B setMaxInputLength(int maxInputLength) {
			this.maxInputLength = maxInputLength;
			return self;
		}

		public B setEditBoxName(Component editBoxName) {
			this.editBoxName = editBoxName;
			return self;
		}

		public SimpleCreationScreen<V> build(){
			if(title == null || chooseId == null ||
					rules1 == null || rules2 == null || editBoxName == null)
				throw new IllegalStateException();
			Screen escape = null;
			if(parent instanceof XPACScreen xpacScreen)
				escape = xpacScreen.getEscape();
			return buildInternally(escape);
		}

		protected abstract SimpleCreationScreen<V> buildInternally(Screen escape);

	}

}
