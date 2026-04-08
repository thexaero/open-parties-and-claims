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
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.group.PlayerConfigGroupExistencePacket;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CreatePlayerGroupScreen extends XPACScreen {

	private static final Component TITLE =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_create_group_title");
	private static final Component CHOOSE_ID =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_choose_id");
	private static final Component ALLOWED_CHARS1 =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_allowed_chars1");
	private static final Component ALLOWED_CHARS_2 =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_allowed_chars2");

	private final PlayerConfigClientStorage configData;
	private final List<String> existingEditorGroups;
	private final Consumer<String> listener;
	private EditBox idTextBox;
	private Button confirmButton;

	private CreatePlayerGroupScreen(
			Screen escape,
			Screen parent,
			PlayerConfigClientStorage configData,
			List<String> existingEditorGroups,
			Consumer<String> listener
	) {
		super(escape, parent, TITLE);
		this.configData = configData;
		this.existingEditorGroups = existingEditorGroups;
		this.listener = listener;
	}

	@Override
	protected void init() {
		super.init();
		String existingInput = idTextBox == null ? null : idTextBox.getValue();
		idTextBox = new EditBox(
				font, width / 2 - 100, height / 7 + 60,
				200, 20,
				Component.translatable("gui.xaero_pac_ui_player_config_player_groups_id_edit_box")
		);
		if(existingInput != null)
			idTextBox.setValue(existingInput);
		idTextBox.setMaxLength(PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH);
		addRenderableWidget(idTextBox);
		idTextBox.setResponder(this::onIdTextBox);
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
		if(listener != null)
			listener.accept(input);
		syncCreation(input);
		goBack();
	}

	private void syncCreation(String input){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(
				new PlayerConfigGroupExistencePacket(configData.getType(), configData.getOwnerForSync(), input, true)
		);
	}

	private boolean confirmButtonIsActive(){
		return getInputValidity() == InputValidity.VALID;
	}

	private InputValidity getInputValidity(){
		String input = idTextBox.getValue();
		if(input.isEmpty())
			return InputValidity.EMPTY;
		if(!CustomPlayerConfigGroupData.isValidId(input))
			return InputValidity.INVALID_CHARS;
		int binarySearchedGroupIndex = Collections.binarySearch(existingEditorGroups, input);
		if(binarySearchedGroupIndex >= 0)
			return InputValidity.DUPLICATE;
		return InputValidity.VALID;
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
		super.render(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.drawCenteredString(font, TITLE, width / 2, 26, -1);
		guiGraphics.drawCenteredString(font, CHOOSE_ID, width / 2, this.height / 7 + 25, -1);
		guiGraphics.drawCenteredString(font, ALLOWED_CHARS1, width / 2, this.height / 7 + 35, 0xFFAAAAAA);
		guiGraphics.drawCenteredString(font, ALLOWED_CHARS_2, width / 2, this.height / 7 + 45, 0xFFAAAAAA);
		InputValidity inputValidity = getInputValidity();
		if(inputValidity.message != null)
			guiGraphics.drawString(font, inputValidity.message, width / 2 + 105, height / 7 + 64, 0xFFFF5555);
	}

	public static final class Builder {

		private Screen parent;
		private PlayerConfigClientStorage configData;
		private List<String> existingEditorGroups;
		private Consumer<String> listener;

		private Builder(){}

		public Builder setDefault(){
			setParent(null);
			setConfigData(null);
			setListener(null);
			setExistingEditorGroups(null);
			return this;
		}

		public Builder setParent(Screen parent) {
			this.parent = parent;
			return this;
		}

		public Builder setConfigData(PlayerConfigClientStorage configData) {
			this.configData = configData;
			return this;
		}

		public Builder setListener(Consumer<String> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setExistingEditorGroups(List<String> existingEditorGroups) {
			this.existingEditorGroups = existingEditorGroups;
			return this;
		}

		public CreatePlayerGroupScreen build(){
			if(configData == null || existingEditorGroups == null)
				throw new IllegalStateException();
			Screen escape = null;
			if(parent instanceof XPACScreen xpacScreen)
				escape = xpacScreen.getEscape();
			return new CreatePlayerGroupScreen(escape, parent, configData, existingEditorGroups, listener);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

	private enum InputValidity {
		VALID(null),
		EMPTY(null),
		INVALID_CHARS(Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_not_valid")),
		DUPLICATE(Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_duplicate"));

		private final Component message;

		InputValidity(Component message) {
			this.message = message;
		}
	}


}
