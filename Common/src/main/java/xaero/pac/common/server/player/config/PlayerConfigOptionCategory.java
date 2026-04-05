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

import xaero.pac.common.server.config.ServerConfig;

public enum PlayerConfigOptionCategory {
	GENERAL(false, false),
	GENERAL_CLAIMS(true, false),
	BLOCK_PROTECTION(true, false),
	BLOCK_TRIGGERS(true, false),
	ENTITY_PROTECTION(true, false),
	PLAYER_PROTECTION(true, false),
	MOVEMENT(true, false),
	PROTECTION_FROM_ITEMS(true, false),
	PICKUP_PROTECTION(true, false),
	SPAWN_PROTECTION(true, false),
	MIXED_PROTECTION(true, false),
	GENERAL_PARTY(false, true);

	public final boolean requiresClaims;
	public final boolean requiresParties;

	PlayerConfigOptionCategory(boolean requiresClaims, boolean requiresParties) {
		this.requiresClaims = requiresClaims;
		this.requiresParties = requiresParties;
	}

	public boolean requiredFeaturesAreEnabled(){
		return (!requiresClaims || ServerConfig.CONFIG.claimsEnabled.get()) &&
				(!requiresParties || ServerConfig.CONFIG.partiesEnabled.get());
	}

}
