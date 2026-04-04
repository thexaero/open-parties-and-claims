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

import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.client.gui.widget.dropdown.IDropDownWidgetCallback;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.group.PlayerConfigGroupMemberPacket;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class IncludePlayerScreen extends IncludeElementScreen implements IDropDownWidgetCallback {

	private static final Component TITLE =
			new TranslatableComponent("gui.xaero_pac_ui_player_config_player_groups_include_player_title");
	private static final Component PLAYER_SELECTION_MENU =
			new TranslatableComponent("gui.xaero_pac_ui_player_config_player_groups_include_player_menu");
	private static final Component HINT =
			new TranslatableComponent("gui.xaero_pac_ui_player_config_player_groups_include_player_hint");
	private static final Component ALL_INCLUDED =
			new TranslatableComponent("gui.xaero_pac_ui_player_config_player_groups_include_player_all_included")
					.withStyle(s -> s.withColor(ChatFormatting.YELLOW));
	private static final Component MANUAL_INPUT_BOX =
			new TranslatableComponent("gui.xaero_pac_ui_player_config_player_groups_include_player_manual");
	private static final Component MANUAL_INPUT_HINT =
			new TranslatableComponent("gui.xaero_pac_ui_player_config_player_groups_include_player_manual_hint");

	private IncludePlayerScreen(
			Screen escape,
			Screen parent,
			PlayerConfigClientStorage configData,
			CustomPlayerConfigGroupData groupData,
			Consumer<String> listener,
			String[] options,
			String[] optionNames,
			boolean allowManualInput,
			int manualInputMaxLength
	) {
		super(
				escape, parent, TITLE, configData, groupData,
				listener, options, optionNames, PLAYER_SELECTION_MENU, HINT, ALL_INCLUDED,
				allowManualInput, MANUAL_INPUT_BOX, MANUAL_INPUT_HINT, manualInputMaxLength
		);
	}

	@Override
	protected void includeInData(String input) {
		groupData.includeMember(new CustomPlayerGroupMember(null, input));
	}

	@Override
	protected boolean alreadyIncluded(String input) {
		return groupData.playerNameIsIncluded(input);
	}

	@Override
	protected Object createPacket(String input) {
		return new PlayerConfigGroupMemberPacket(
				configData.getType(), configData.getOwnerForSync(),
				groupData.getId(), PlayerConfigGroupMemberPacket.Action.INCLUDE,
				null, input
		);
	}

	@Override
	protected boolean inputIsValid(String input) {
		return CustomPlayerConfigGroupData.isValidPlayerName(input);
	}

	public static final class Builder extends IncludeElementScreen.Builder<Builder> {

		private Builder(){}

		@Override
		public Builder setDefault(){
			super.setDefault();
			setAllowManualInput(true);
			setManualInputMaxLength(PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_USERNAME_LENGTH);
			return self;
		}

		@Override
		public IncludePlayerScreen build(){
			return (IncludePlayerScreen) super.build();
		}

		@Override
		protected IncludeElementScreen buildInternally(Screen escape) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.getConnection() == null)
				return null;
			if(mc.player == null)
				return null;
			List<String> allOnlinePlayers = Streams.concat(
					Stream.of(""),
					mc.getConnection().getOnlinePlayers().stream()
							.map(PlayerInfo::getProfile)
							.map(GameProfile::getName)
			).toList();
			String excludedName =
					configData.getType() == PlayerConfigType.PLAYER &&
					configData.getOwner() == mc.player.getUUID() ?
							mc.player.getGameProfile().getName() : null;
			List<String> filteredPlayers = allOnlinePlayers.stream()
					.filter(s -> !s.equals(excludedName) && !groupData.playerNameIsIncluded(s))
					.toList();
			String[] playerNames = filteredPlayers.toArray(new String[0]);
			return new IncludePlayerScreen(
					escape, parent, configData,
					groupData, listener, playerNames, playerNames,
					allowManualInput, manualInputMaxLength
			);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}
	}


}
