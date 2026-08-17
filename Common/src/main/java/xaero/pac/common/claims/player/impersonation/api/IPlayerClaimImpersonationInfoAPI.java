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

package xaero.pac.common.claims.player.impersonation.api;

import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for a player's info about player impersonation
 */
public interface IPlayerClaimImpersonationInfoAPI {

	/**
	 * Gets the UUID of the player that is currently being impersonated for claims by this player.
	 *
	 * @return the UUID of the impersonated player, null when not impersonating anyone
	 */
	@Nullable
	UUID getPlayerId();

	/**
	 * Gets the player UUID used for claims under a specified claiming mode when the player with a non-null UUID
	 * returned by {@link #getPlayerId()} is being impersonated by this player.
	 *
	 * @param mode the claiming mode, not null
	 * @return the player UUID used for claims under a specified claiming mode, can be null
	 */
	@Nullable
	UUID getClaimPlayerId(@Nonnull IClaimingModeAPI mode);

	/**
	 * Gets the index of the sub-claim used for claims under a specified claiming mode when the player with a
	 * non-null UUID returned by {@link #getPlayerId()} is being impersonated by this player.
	 *
	 * @param mode the claiming mode, not null
	 * @return the sub-claim index, -1 for the main sub-claim
	 */
	int getSubIndex(@Nonnull IClaimingModeAPI mode);

}
