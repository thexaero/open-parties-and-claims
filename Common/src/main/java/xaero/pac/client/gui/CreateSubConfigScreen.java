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

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.function.Supplier;

public final class CreateSubConfigScreen extends SimpleCreationScreen<CreateSubConfigScreen.InputValidity> {

	private static final Component TITLE =
			Component.translatable("gui.xaero_pac_ui_player_config_create_sub_config");
	private static final Component CHOOSE_ID =
			Component.translatable("gui.xaero_pac_ui_player_config_choose_sub_config_id");
	private static final Component NORMAL_RULES_1 =
			Component.translatable("gui.xaero_pac_ui_player_config_sub_config_rules1", PlayerConfig.MAX_SUB_ID_LENGTH);
	private static final Component NORMAL_RULES_2 =
			Component.translatable("gui.xaero_pac_ui_player_config_sub_config_rules2");
	private static final Component DIMENSION_RULES_1 =
			Component.translatable("gui.xaero_pac_ui_player_config_sub_config_dimension_rules1");
	private static final Component DIMENSION_RULES_2 =
			Component.translatable("gui.xaero_pac_ui_player_config_sub_config_dimension_rules2");
	private static final Component EDIT_BOX_NAME =
			Component.translatable("gui.xaero_pac_ui_player_config_sub_id_edit_box");

	private final PlayerConfigClientStorage configData;
	private final Supplier<Screen> targetScreenSupplier;

	private CreateSubConfigScreen(
			Screen escape,
			Screen parent,
			Component title,
			Component chooseId,
			Component rules1,
			Component rules2,
			Component editBoxName,
			int maxInputLength,
			PlayerConfigClientStorage configData,
			Supplier<Screen> targetScreenSupplier
	) {
		super(
				escape, parent, title, chooseId,
				rules1, rules2, editBoxName, maxInputLength
		);
		this.configData = configData;
		this.targetScreenSupplier = targetScreenSupplier;
	}

	@Override
	protected InputValidity getInputValidity(String input) {
		if(input.isEmpty())
			return InputValidity.EMPTY;
		if(configData.getType().hasDimensionSubConfigs() && !PlayerConfig.isValidDimensionSubId(input))
			return InputValidity.INVALID;
		if(!configData.getType().hasDimensionSubConfigs() && !PlayerConfig.isValidSubId(input))
			return InputValidity.INVALID;
		if(configData.getSubConfigIds().contains(input))
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
		configData.setSyncInProgress(true);
		configData.setSelectedSubConfig(input);
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer()
				.requestCreateSubConfig(configData, input);
		minecraft.setScreen(targetScreenSupplier.get());
	}

	public enum InputValidity {
		VALID(null),
		EMPTY(null),
		INVALID(Component.translatable("gui.xaero_pac_ui_player_config_sub_id_not_valid")),
		DUPLICATE(Component.translatable("gui.xaero_pac_ui_player_config_sub_id_duplicate"));

		private final Component message;

		InputValidity(Component message) {
			this.message = message;
		}
	}

	public static final class Builder extends SimpleCreationScreen.Builder<InputValidity, Builder> {

		private PlayerConfigClientStorage configData;
		private Supplier<Screen> targetScreenSupplier;

		private Builder(){
			super();
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setConfigData(null);
			setTargetScreenSupplier(null);
			setTitle(TITLE);
			setChooseId(CHOOSE_ID);
			setEditBoxName(EDIT_BOX_NAME);
			return self;
		}

		public Builder setConfigData(PlayerConfigClientStorage configData) {
			this.configData = configData;
			return self;
		}

		public Builder setTargetScreenSupplier(Supplier<Screen> targetScreenSupplier) {
			this.targetScreenSupplier = targetScreenSupplier;
			return self;
		}

		@Override
		public Builder setMaxInputLength(int maxInputLength) {
			if(maxInputLength != 0)
				throw new UnsupportedOperationException();
			return super.setMaxInputLength(maxInputLength);
		}

		@Override
		public Builder setRules1(Component rules1) {
			if(rules1 != null)
				throw new UnsupportedOperationException();
			return super.setRules1(rules1);
		}

		@Override
		public Builder setRules2(Component rules2) {
			if(rules2 != null)
				throw new UnsupportedOperationException();
			return super.setRules2(rules2);
		}

		@Override
		public SimpleCreationScreen<InputValidity> build() {
			if(configData == null || targetScreenSupplier == null)
				throw new IllegalStateException();
			boolean isDimensionType = configData.getType().hasDimensionSubConfigs();
			super.setMaxInputLength(isDimensionType ? 500 : PlayerConfig.MAX_SUB_ID_LENGTH);
			super.setRules1(isDimensionType ? DIMENSION_RULES_1 : NORMAL_RULES_1);
			super.setRules2(isDimensionType ? DIMENSION_RULES_2 : NORMAL_RULES_2);
			return super.build();
		}

		@Override
		protected CreateSubConfigScreen buildInternally(Screen escape) {
			return new CreateSubConfigScreen(
					escape, parent, title, chooseId, rules1, rules2,
					editBoxName, maxInputLength, configData, targetScreenSupplier
			);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
