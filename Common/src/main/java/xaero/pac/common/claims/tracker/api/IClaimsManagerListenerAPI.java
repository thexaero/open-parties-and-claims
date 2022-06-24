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

package xaero.pac.common.claims.tracker.api;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The interface to be implemented by all claims manager listeners
 * <p>
 * Register your listeners in {@link IClaimsManagerTrackerAPI}.
 */
public interface IClaimsManagerListenerAPI {

	/**
	 * Called after a whole 512x512 region of claim states is updated.
	 * <p>
	 * Override this method and register the listener in {@link IClaimsManagerTrackerAPI} to handle region claim state
	 * updates however you'd like.
	 * <p>
	 * This method is only called on the client side at the time of writing this.
	 *
	 * @param dimension  the dimension of the region, not null
	 * @param regionX  the X coordinate of the region
	 * @param regionZ  the Z coordinate of the region
	 */
	public void onWholeRegionChange(@Nonnull ResourceLocation dimension, int regionX, int regionZ);

	/**
	 * Called after the claim state of a chunk is updated.
	 * <p>
	 * Override this method and register the listener in {@link IClaimsManagerTrackerAPI} to handle chunk claim state
	 * updates however you'd like.
	 *
	 * @param dimension  the dimension of the chunk, not null
	 * @param chunkX  the X coordinate of the chunk
	 * @param chunkZ  the Z coordinate of the chunk
	 * @param claim  the new claim state, null when the chunk is unclaimed
	 */
	public void onChunkChange(@Nonnull ResourceLocation dimension, int chunkX, int chunkZ, @Nullable IPlayerChunkClaimAPI claim);

}
