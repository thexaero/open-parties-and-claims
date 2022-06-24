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
import java.util.stream.Stream;

/**
 * API for dimension claims of a player
 */
public interface IPlayerDimensionClaimsAPI<L extends IPlayerClaimPosListAPI> {

	/**
	 * Gets a stream of all claim position lists in this dimension for this player.
	 *
	 * @return the stream of all claim position lists, not null
	 */
	@Nonnull
	public Stream<L> getStream();

}
