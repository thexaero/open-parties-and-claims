/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.claims.api;

import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import javax.annotation.Nullable;

/**
 * API for region claims
 */
public interface IRegionClaimsAPI {

	/**
	 * Gets the claim state at a specified chunk location inside the 512x512 region.
	 * <p>
	 * The coordinate values must be within 0 - 31 (inclusive).
	 *
	 * @param x  the X coordinate of the chunk inside the region, 0 - 31 (inclusive)
	 * @param z  the Z coordinate of the chunk inside the region, 0 - 31 (inclusive)
	 * @return the claim state, null if the chunk is not claimed
	 */
	@Nullable
	public IPlayerChunkClaimAPI get(int x, int z);

	/**
	 * Gets the X coordinate of the 512x512 region.
	 *
	 * @return the X coordinate value
	 */
	public int getX();

	/**
	 * Gets the Z coordinate of the 512x512 region.
	 *
	 * @return the Z coordinate value
	 */
	public int getZ();
	
}
