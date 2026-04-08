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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xaero.pac.client.gui.widget.dropdown.IDropDownWidgetCallback;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.group.PlayerConfigGroupGroupPacket;
import xaero.pac.common.player.config.group.BuiltInPlayerConfigGroupNames;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;

import java.util.List;
import java.util.function.Consumer;

public class IncludeGroupScreen extends IncludeElementScreen implements IDropDownWidgetCallback {

	private static final Component TITLE =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_include_group_title");
	private static final Component GROUP_SELECTION_MENU =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_include_group_menu");
	private static final Component HINT =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_include_group_hint");
	private static final Component ALL_INCLUDED =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_include_group_all_included")
					.withStyle(s -> s.withColor(ChatFormatting.RED));

	private IncludeGroupScreen(
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
				listener, options, optionNames, GROUP_SELECTION_MENU, HINT, ALL_INCLUDED,
				allowManualInput, null, null, manualInputMaxLength
		);
	}

	@Override
	protected void includeInData(String input) {
		groupData.includeGroup(input);
	}

	@Override
	protected boolean alreadyIncluded(String input) {
		return groupData.groupIdIsIncluded(input);
	}

	@Override
	protected Object createPacket(String input) {
		return new PlayerConfigGroupGroupPacket(
				configData.getType(), configData.getOwnerForSync(),
				groupData.getId(), PlayerConfigGroupGroupPacket.Action.INCLUDE,
				input
		);
	}

	@Override
	protected boolean inputIsValid(String input) {
		return CustomPlayerConfigGroupData.isValidId(input);
	}

	public static final class Builder extends IncludeElementScreen.Builder<Builder> {

		private List<String> groupIds;
		private List<String> defaultGroupIds;
		private Builder(){}

		@Override
		public Builder setDefault(){
			super.setDefault();
			setAllowManualInput(false);
			setGroupIds(null);
			setDefaultGroupIds(null);
			return self;
		}

		public Builder setGroupIds(List<String> groupIds) {
			this.groupIds = groupIds;
			return self;
		}

		public Builder setDefaultGroupIds(List<String> defaultGroupIds) {
			this.defaultGroupIds = defaultGroupIds;
			return self;
		}

		@Override
		public IncludeGroupScreen build(){
			if(groupIds == null || defaultGroupIds == null)
				throw new IllegalStateException();
			return (IncludeGroupScreen) super.build();
		}

		@Override
		protected IncludeElementScreen buildInternally(Screen escape) {
			List<String> filteredGroupIds = Streams.concat(groupIds.stream(), defaultGroupIds.stream())
					.filter(s -> !s.equals(groupData.getId()) && !groupData.groupIdIsIncluded(s))
					.toList();
			String[] groupIds = filteredGroupIds.toArray(new String[0]);
			String[] groupNames = new String[groupIds.length];
			for (int i = 0; i < groupIds.length; i++)
				groupNames[i] = BuiltInPlayerConfigGroupNames.apply(groupIds[i]).getString();
			return new IncludeGroupScreen(
					escape, parent, configData,
					groupData, listener, groupIds, groupNames,
					allowManualInput, manualInputMaxLength
			);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}
	}


}
