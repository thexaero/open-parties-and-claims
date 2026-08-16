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

package xaero.pac.common.player.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class PlayerConfigConstants {

	public static final Component ON_COMPONENT = Component.translatable("gui.xaero_pac_ui_on");
	public static final Component OFF_COMPONENT = Component.translatable("gui.xaero_pac_ui_off");
	public static final String NO_EXCEPTION_ID = "N";
	public static final String EVERYONE_EXCEPTION_ID = "E";
	public static final String PARTY_EXCEPTION_ID = "P";
	public static final String ALLIES_EXCEPTION_ID = "A";

	public static final Component UNKNOWN_PLAYER =
			Component.translatable("gui.xaero_pac_player_config_player_groups_unknown_player");
	public static final Component OPTION_NOT_DIRECTLY_CONFIGURABLE =
			Component.translatable("gui.xaero_pac_config_option_not_directly_configurable");
	public static final int MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH = 16;
	//I think MC usernames are actually at most 16 chars but this won't cause problems because only ops can add unknown usernames
	public static final int MAX_CUSTOM_PLAYER_GROUP_USERNAME_LENGTH = 32;

}
