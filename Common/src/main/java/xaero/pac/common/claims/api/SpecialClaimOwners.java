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

package xaero.pac.common.claims.api;

import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.UUID;

/**
 * Access point for all special claim owners that can exist in the base OPAC mod.
 */
public class SpecialClaimOwners {

	/**
	 * The UUID used for server claims
	 */
	public static final UUID SERVER = PlayerConfig.SERVER_CLAIM_UUID;
	/**
	 * The UUID used for expired claims
	 */
	public static final UUID EXPIRED = PlayerConfig.EXPIRED_CLAIM_UUID;

}
