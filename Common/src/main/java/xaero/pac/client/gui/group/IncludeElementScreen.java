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

package xaero.pac.client.gui.group;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.XPACScreen;
import xaero.pac.client.gui.widget.dropdown.DropDownWidget;
import xaero.pac.client.gui.widget.dropdown.IDropDownWidgetCallback;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;

import java.util.function.Consumer;

public abstract class IncludeElementScreen extends XPACScreen implements IDropDownWidgetCallback {

	protected final PlayerConfigClientStorage configData;
	protected final CustomPlayerConfigGroupData groupData;
	private final Consumer<String> listener;
	private final String[] options;
	private final String[] optionNames;
	private final Component selectionMenuNarration;
	private final Component selectionMenuHint;
	private final Component allIncludedErrorMessage;
	private final boolean allowManualInput;
	private final Component manualInputNarration;
	private final Component manualInputHint;
	private final int manualInputMaxLength;
	private EditBox manualInputBox;
	private DropDownWidget selectionMenu;
	private Button confirmButton;
	private boolean needsButtonUpdate;
	private int selectionMenuY;

	protected IncludeElementScreen(
			Screen escape,
			Screen parent,
			Component title,
			PlayerConfigClientStorage configData,
			CustomPlayerConfigGroupData groupData,
			Consumer<String> listener,
			String[] options,
			String[] optionNames,
			Component selectionMenuNarration,
			Component selectionMenuHint,
			Component allIncludedErrorMessage,
			boolean allowManualInput,
			Component manualInputNarration,
			Component manualInputHint,
			int manualInputMaxLength
	) {
		super(escape, parent, title);
		this.configData = configData;
		this.groupData = groupData;
		this.listener = listener;
		this.options = options;
		this.optionNames = optionNames;
		this.selectionMenuNarration = selectionMenuNarration;
		this.selectionMenuHint = selectionMenuHint;
		this.allIncludedErrorMessage = allIncludedErrorMessage;
		this.allowManualInput = allowManualInput;
		this.manualInputNarration = manualInputNarration;
		this.manualInputHint = manualInputHint;
		this.manualInputMaxLength = manualInputMaxLength;
	}

	@Override
	protected void init() {
		super.init();
		Integer existingInput = selectionMenu == null ? null : selectionMenu.getSelected();
		selectionMenuY = height / 7 + 65;
		if(allowManualInput){
			String existingManualInput = manualInputBox == null ? null : manualInputBox.getValue();
			manualInputBox = new EditBox(font, width / 2 - 99, height / 7 + 65, 200, 20, manualInputNarration);
			manualInputBox.setMaxLength(manualInputMaxLength);
			addRenderableWidget(manualInputBox);
			if(existingManualInput != null)
				manualInputBox.setValue(existingManualInput);
			manualInputBox.setResponder(this::onManualInputBox);
			setFocused(manualInputBox);
			manualInputBox.setFocused(true);
			selectionMenuY -= 40;
		}
		if(options.length > 0) {
			selectionMenu = DropDownWidget.Builder.begin()
					.setX(width / 2 - 100)
					.setY(selectionMenuY)
					.setW(200)
					.setContainer(this)
					.setCallback(this)
					.setOptions(optionNames)
					.setNarrationTitle(selectionMenuNarration)
					.setSelected(existingInput == null ? 0 : existingInput)
					.build();
			addRenderableWidget(selectionMenu);
		}
		addRenderableWidget(
				confirmButton = Button.builder(
						Component.translatable("gui.xaero_pac_confirm"), this::onConfirm
				).bounds(width / 2 - 105, height / 7 + 128, 100, 20).build()
		);
		addRenderableWidget(
				Button.builder(
						Component.translatable("gui.xaero_pac_cancel"), b -> goBack()
				).bounds(width / 2 + 5, this.height / 7 + 128, 100, 20).build()
		);
		updateButtons();
	}

	private void updateButtons(){
		needsButtonUpdate = false;
		confirmButton.active = confirmButtonIsActive();
	}

	private void onManualInputBox(String s) {
		updateButtons();
	}

	private void onConfirm(Button button) {
		if(!confirmButtonIsActive())
			return;
		String input = getInput();
		includeInData(input);
		syncInclusion(input);
		if(listener != null)
			listener.accept(input);
		goBack();
	}

	private void syncInclusion(String input){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(createPacket(input));
	}

	private String getInput(){
		if(manualInputBox != null)
			return manualInputBox.getValue();
		if(selectionMenu == null)
			return null;
		return options[selectionMenu.getSelected()];
	}

	private boolean confirmButtonIsActive(){
		String input = getInput();
		if(input == null || input.isEmpty())
			return false;
		return inputIsValid(input) && !alreadyIncluded(input);
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
		if(needsButtonUpdate)
			updateButtons();
		renderBackground(guiGraphics, mouseX, mouseY, partial);
		super.render(guiGraphics, mouseX, mouseY, partial);
	}

	@Override
	protected void renderPreDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		super.renderPreDropdown(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.drawCenteredString(font, title, width / 2, 26, -1);
		if(selectionMenu == null)
			guiGraphics.drawCenteredString(font, allIncludedErrorMessage, width / 2, selectionMenuY, -1);
		else
			guiGraphics.drawCenteredString(font, selectionMenuHint, width / 2, selectionMenuY - 15, -1);
		if(allowManualInput)
			guiGraphics.drawCenteredString(font, manualInputHint, width / 2, height / 7 + 50, -1);
	}

	@Override
	public boolean onSelected(DropDownWidget menu, int selected) {
		if(manualInputBox != null) {
			manualInputBox.setValue(options[selected]);
			setFocused(manualInputBox);
			manualInputBox.setFocused(true);
		}
		needsButtonUpdate = true;
		return true;
	}

	@Override
	public void tick() {
		super.tick();
	}

	protected abstract void includeInData(String input);

	protected abstract boolean alreadyIncluded(String input);

	protected abstract Object createPacket(String input);

	protected abstract boolean inputIsValid(String input);

	public static abstract class Builder<B extends Builder<B>> {

		protected Screen parent;
		protected PlayerConfigClientStorage configData;
		protected CustomPlayerConfigGroupData groupData;
		protected Consumer<String> listener;
		protected boolean allowManualInput;
		protected int manualInputMaxLength;

		protected final B self;

		@SuppressWarnings("unchecked")
		protected Builder(){
			self = (B)this;
		}

		public B setDefault(){
			setParent(null);
			setConfigData(null);
			setListener(null);
			setManualInputMaxLength(0);
			return self;
		}

		public B setParent(Screen parent) {
			this.parent = parent;
			return self;
		}

		public B setConfigData(PlayerConfigClientStorage configData) {
			this.configData = configData;
			return self;
		}

		public B setGroupData(CustomPlayerConfigGroupData groupData) {
			this.groupData = groupData;
			return self;
		}

		public B setListener(Consumer<String> listener) {
			this.listener = listener;
			return self;
		}

		public B setAllowManualInput(boolean allowManualInput) {
			this.allowManualInput = allowManualInput;
			return self;
		}

		public B setManualInputMaxLength(int manualInputMaxLength) {
			this.manualInputMaxLength = manualInputMaxLength;
			return self;
		}

		public IncludeElementScreen build(){
			if(configData == null)
				throw new IllegalStateException();
			if(allowManualInput && manualInputMaxLength <= 0)
				throw new IllegalStateException();
			Screen escape = null;
			if(parent instanceof XPACScreen xpacScreen)
				escape = xpacScreen.getEscape();
			return buildInternally(escape);
		}

		protected abstract IncludeElementScreen buildInternally(Screen escape);

	}


}
