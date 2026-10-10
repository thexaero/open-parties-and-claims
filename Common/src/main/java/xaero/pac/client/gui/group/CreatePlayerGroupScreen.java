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

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.SimpleCreationScreen;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.group.PlayerConfigGroupExistencePacket;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CreatePlayerGroupScreen extends SimpleCreationScreen<CreatePlayerGroupScreen.InputValidity> {

	private static final Component TITLE =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_create_group_title");
	private static final Component CHOOSE_ID =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_choose_id");
	private static final Component ALLOWED_CHARS1 =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_allowed_chars1");
	private static final Component ALLOWED_CHARS2 =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_allowed_chars2");
	private static final Component EDIT_BOX_NAME =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_id_edit_box");

	private final Consumer<String> listener;
	private final PlayerConfigClientStorage configData;
	private final List<String> existingEditorGroups;

	private CreatePlayerGroupScreen(
			Screen escape,
			Screen parent,
			Component title,
			Component chooseId,
			Component rules1,
			Component rules2,
			Component editBoxName,
			int maxInputLength,
			Consumer<String> listener,
			PlayerConfigClientStorage configData,
			List<String> existingEditorGroups
	) {
		super(escape, parent, title, chooseId, rules1, rules2, editBoxName, maxInputLength);
		this.listener = listener;
		this.configData = configData;
		this.existingEditorGroups = existingEditorGroups;
	}

	@Override
	protected InputValidity getInputValidity(String input) {
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
	protected boolean isValid(InputValidity validity) {
		return validity == InputValidity.VALID;
	}

	@Override
	protected Component getValidityMessage(InputValidity validity) {
		return validity.message;
	}

	@Override
	protected void handleConfirmation(String input) {
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

	public enum InputValidity {
		VALID(null),
		EMPTY(null),
		INVALID_CHARS(Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_not_valid")),
		DUPLICATE(Component.translatable("gui.xaero_pac_ui_player_config_player_groups_group_id_duplicate"));

		private final Component message;

		InputValidity(Component message) {
			this.message = message;
		}
	}

	public static final class Builder extends SimpleCreationScreen.Builder<InputValidity, Builder> {

		private PlayerConfigClientStorage configData;
		private List<String> existingEditorGroups;
		private Consumer<String> listener;

		private Builder(){}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setConfigData(null);
			setExistingEditorGroups(null);
			setListener(null);
			setTitle(TITLE);
			setChooseId(CHOOSE_ID);
			setRules1(ALLOWED_CHARS1);
			setRules2(ALLOWED_CHARS2);
			setEditBoxName(EDIT_BOX_NAME);
			setMaxInputLength(PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH);
			return self;
		}

		public Builder setConfigData(PlayerConfigClientStorage configData) {
			this.configData = configData;
			return self;
		}

		public Builder setExistingEditorGroups(List<String> existingEditorGroups) {
			this.existingEditorGroups = existingEditorGroups;
			return self;
		}

		public Builder setListener(Consumer<String> listener) {
			this.listener = listener;
			return self;
		}

		@Override
		protected CreatePlayerGroupScreen buildInternally(Screen escape) {
			if(configData == null || existingEditorGroups == null || listener == null)
				throw new IllegalStateException();
			return new CreatePlayerGroupScreen(
					escape, parent, title, chooseId,
					rules1, rules2, editBoxName, maxInputLength,
					listener, configData,
					existingEditorGroups
			);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
