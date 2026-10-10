/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config;

import net.minecraft.network.chat.Component;
import xaero.pac.common.server.config.ServerConfig;

public enum PlayerConfigOptionCategory {
	GENERAL(false, false, Component.translatable("gui.xaero_pac_ui_player_config_category_general")),
	GENERAL_PARTY(false, true, Component.translatable("gui.xaero_pac_ui_player_config_category_general_parties")),
	GENERAL_CLAIMS(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_general_claims")),
	BLOCK_PROTECTION(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_block_protection")),
	TRIGGER_BLOCKS(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_trigger_blocks")),
	ENTITY_PROTECTION(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_entity_protection")),
	PICKUP_PROTECTION(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_item_pickup")),
	PROTECTION_FROM_ITEMS(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_item_use")),
	MOVEMENT(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_movement")),
	SPAWN_PROTECTION(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_spawn_protection")),
	MIXED_PROTECTION(true, false, Component.translatable("gui.xaero_pac_ui_player_config_category_mixed_protection"));

	public final boolean requiresClaims;
	public final boolean requiresParties;
	private final Component displayName;

	PlayerConfigOptionCategory(boolean requiresClaims, boolean requiresParties, Component displayName) {
		this.requiresClaims = requiresClaims;
		this.requiresParties = requiresParties;
		this.displayName = displayName;
	}

	public boolean requiredFeaturesAreEnabled(){
		return (!requiresClaims || ServerConfig.CONFIG.claimsEnabled.get()) &&
				(!requiresParties || ServerConfig.CONFIG.partiesEnabled.get());
	}

	public Component getDisplayName() {
		return displayName;
	}

}
