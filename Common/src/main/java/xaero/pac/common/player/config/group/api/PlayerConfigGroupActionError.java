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

package xaero.pac.common.player.config.group.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Enum that defines different errors that may occur during various player group operations.
 */
public enum PlayerConfigGroupActionError {
	/**
	 * Error returned when trying to create a group with an ID that is already used.
	 */
	GROUP_ALREADY_EXISTS(
			null,
			Component.translatable("gui.xaero_pac_command_player_config_player_groups_error_group_already_exists")
	),
	/**
	 * Error returned when trying to use an invalid group ID.
	 */
	INVALID_GROUP_ID(
			null,
			Component.translatable("gui.xaero_pac_command_player_config_player_groups_error_invalid_group_id")
	),
	/**
	 * Error returned when trying to use a group ID that is longer than 16 characters.
	 */
	GROUP_ID_TOO_LONG(
			null,
			Component.translatable("gui.xaero_pac_command_player_config_player_groups_error_group_id_too_long")
	),
	/**
	 * Error returned when trying to remove a group using a group ID that isn't used.
	 */
	GROUP_TO_REMOVE_NOT_FOUND(
			null,
			Component.translatable("gui.xaero_pac_command_player_config_player_groups_error_group_does_not_exist")
	),
	/**
	 * Error returned when trying to edit a group using a group ID that isn't used.
	 */
	GROUP_TO_EDIT_NOT_FOUND(
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_error_group_not_found"),
			Component.translatable("gui.xaero_pac_command_player_config_player_groups_error_edited_group_does_not_exist")
	),
	/**
	 * Error returned when trying to include a player in a group who is already included.
	 */
	MEMBER_ALREADY_INCLUDED(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_member_already_added"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_member_already_added")
	),
	/**
	 * Error returned when trying to affect a group member entry that is not in the group.
	 */
	MEMBER_NOT_FOUND(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_member_not_found"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_member_not_found")
	),
	/**
	 * Error returned when trying to affect a group's group inclusion entry that is not in the group.
	 */
	GROUP_INCLUSION_NOT_FOUND(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_inclusion_not_found"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_inclusion_not_found")
	),
	/**
	 * Error returned when trying to include a group in a group which is already included.
	 */
	GROUP_ALREADY_INCLUDED(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_already_included"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_already_included")
	),
	/**
	 * Error returned when an operation didn't necessarily fail but had no effect.
	 */
	NO_EFFECT(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_no_effect"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_no_effect")
	),
	/**
	 * Error returned when trying to create a new group but the group count limit has been reached.
	 */
	GROUP_COUNT_LIMIT(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_count_limit"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_count_limit")
	),
	/**
	 * Error returned when trying to include something in a group but the group space has been exhausted for the config.
	 */
	OUT_OF_SPACE(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_space_limit"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_group_space_limit")
	),
	/**
	 * Error returned when a non-op player tries to include a player in a group who the server isn't aware of yet.
	 * <p>
	 * This usually means that the player has not yet played on the server.
	 */
	UNKNOWN_PLAYER(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_unknown_player"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_unknown_player")
	),
	/**
	 * Error returned when trying to include a player in a group with a name that isn't valid.
	 */
	INVALID_PLAYER_NAME(
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_invalid_player_name"),
			Component.translatable("gui.xaero_pac_player_config_player_groups_error_invalid_player_name")
	);

	private final Component desyncScreenMessage;
	private final Component commandMessage;

	PlayerConfigGroupActionError(@Nullable Component desyncScreenMessage, @Nonnull Component commandMessage) {
		this.desyncScreenMessage = desyncScreenMessage;
		this.commandMessage = commandMessage;
	}

	/**
	 * Gets whether this error should cause the player group screen to refresh.
	 * <p>
	 * This mostly exists for internal use. I don't think you need this for anything.
	 *
	 * @return true if the player group screen should refresh when this error is received, otherwise false
	 */
	public boolean shouldRefreshGroupScreen() {
		return desyncScreenMessage != null;
	}

	/**
	 * Gets this error's message to display on the player group screen.
	 *
	 * @return the error message {@link Component}, null if nothing should be displayed
	 */
	@Nullable
	public Component getDesyncScreenMessage() {
		return desyncScreenMessage;
	}

	/**
	 * Gets this error's message to display after using a command.
	 *
	 * @return the error message {@link Component}, not null
	 */
	@Nonnull
	public Component getCommandMessage() {
		return commandMessage;
	}

}
