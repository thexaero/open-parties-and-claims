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

package xaero.pac.common.claims.player;

import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.ClaimingAnchor;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.claims.player.api.IPlayerDimensionClaimsAPI;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.stream.Stream;

public interface IPlayerDimensionClaims<L extends IPlayerClaimPosList> extends IPlayerDimensionClaimsAPI {
	
	//internal api

	@Nonnull
	public Stream<L> getTypedStream();

	@Override
	@Nonnull
	@SuppressWarnings("unchecked")
	default Stream<IPlayerClaimPosListAPI> getStream(){
		return (Stream<IPlayerClaimPosListAPI>)(Object)getTypedStream();
	}

	/**
	 * Adds a claiming anchor at a specified chunk position if it doesn't already exist there.
	 *
	 * @param pos  the position for the anchor, not null
	 * @return true if the anchor was added, false if the specified position already had an anchor
	 */
	boolean addAnchor(@Nonnull ChunkPos pos);

	/**
	 * Removes the claiming anchor at a specified chunk position if it exists.
	 *
	 * @param pos  the position of the anchor, not null
	 * @return true if the anchor was removed, false if the specified position didn't have one
	 */
	boolean removeAnchor(@Nonnull ChunkPos pos);

	Iterator<ClaimingAnchor> getAnchorIterator();

}
