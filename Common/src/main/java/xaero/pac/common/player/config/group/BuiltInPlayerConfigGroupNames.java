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

package xaero.pac.common.player.config.group;

import net.minecraft.network.chat.*;
import xaero.pac.common.player.config.PlayerConfigConstants;

import java.util.HashMap;
import java.util.Map;

public class BuiltInPlayerConfigGroupNames {

	private final static Map<String, Component> NAME_MAP = new HashMap<>();

	private static Component assign(String groupId, MutableComponent groupName){
		groupName.withStyle(s ->
				s.withHoverEvent(
						new HoverEvent.ShowText(Component.literal(groupId))
				)
		);
		NAME_MAP.put(groupId, groupName);
		return groupName;
	}

	public static final Component NO_EXCEPTION_NAME =
			assign(
					PlayerConfigConstants.NO_EXCEPTION_ID,
					Component.translatable("gui.xaero_pac_player_config_playerConfig.playerGroups.nobody")
			);
	public static final Component EVERYONE_EXCEPTION_NAME =
			assign(
					PlayerConfigConstants.EVERYONE_EXCEPTION_ID,
					Component.translatable("gui.xaero_pac_player_config_playerConfig.playerGroups.everyone")
			);
	public static final Component PARTY_EXCEPTION_NAME =
			assign(
					PlayerConfigConstants.PARTY_EXCEPTION_ID,
					Component.translatable("gui.xaero_pac_player_config_playerConfig.playerGroups.party")
			);
	public static final Component ALLIES_EXCEPTION_NAME =
			assign(
					PlayerConfigConstants.ALLIES_EXCEPTION_ID,
					Component.translatable("gui.xaero_pac_player_config_playerConfig.playerGroups.allies")
			);

	public static Component get(String groupId){
		return NAME_MAP.get(groupId);
	}

	public static Component apply(String groupId){
		Component builtInName = get(groupId);
		if(builtInName != null)
			return builtInName;
		return Component.literal(groupId);
	}

}
