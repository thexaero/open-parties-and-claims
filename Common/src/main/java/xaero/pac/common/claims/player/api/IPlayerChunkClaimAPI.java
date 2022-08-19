/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.claims.player.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for a chunk claim state
 */
public interface IPlayerChunkClaimAPI {

	/**
	 * Checks whether this claim is marked for forceloading.
	 * <p>
	 * It doesn't mean that it's actually currently forceloaded.
	 *
	 * @return true if the claim is marked for forceloading, otherwise false
	 */
	public boolean isForceloadable();

	/**
	 * Gets the UUID of the owner of this claim.
	 *
	 * @return the UUID of this claim's owner, not null
	 */
	@Nonnull
	public UUID getPlayerId();

	/**
	 * Checks if another claim state is of the same type as this, which ignores
	 * whether the claim states are forceloadable.
	 *
	 * @param other  the other claim state, can be null
	 * @return true if the specified claim state is of the same type as this,
	 *         otherwise false
	 */
	public boolean isSameClaimType(@Nullable IPlayerChunkClaimAPI other);
	
}
