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

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimInfoAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for a claims manager
 */
public interface IClaimsManagerAPI
<
	PCI extends IPlayerClaimInfoAPI<?>,
	WCM extends IDimensionClaimsManagerAPI<?>
> {

	/**
	 * Checks whether a player has claim info.
	 *
	 * @param playerId  UUID of the player, not null
	 * @return true if the player with UUID {@code playerId} has claims info, otherwise false
	 */
	public boolean hasPlayerInfo(@Nonnull UUID playerId);

	/**
	 * Gets or creates the claim info instance for a player UUID.
	 *
	 * @param playerId  UUID of a player, not null
	 * @return the player claim info, not null
	 */
	@Nonnull
	public PCI getPlayerInfo(@Nonnull UUID playerId);

	/**
	 * Gets a stream of all player claim info.
	 *
	 * @return a {@code Stream} of all player claim info
	 */
	@Nonnull
	public Stream<PCI> getPlayerInfoStream();

	/**
	 * Gets the claim state for a specified chunk.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @return the current claim state at the specified location, null if wilderness
	 */
	@Nullable
	public IPlayerChunkClaimAPI get(@Nonnull ResourceLocation dimension, int x, int z);

	/**
	 * Gets the claim state for a specified chunk.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param chunkPos  the coordinates of the chunk, not null
	 * @return the current claim state at the specified location, null if wilderness
	 */
	@Nullable
	public IPlayerChunkClaimAPI get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPos chunkPos);

	/**
	 * Gets the claim state for a specified chunk.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param blockPos  the block coordinates of the chunk, not null
	 * @return the current claim state at the specified location, null if wilderness
	 */
	@Nullable
	public IPlayerChunkClaimAPI get(@Nonnull ResourceLocation dimension, @Nonnull BlockPos blockPos);

	/**
	 * Gets the read-only claims manager for a specified dimension ID.
	 *
	 * @param dimension  the dimension ID, not null
	 * @return the dimension claims manager, null if no claim data exists for the specified dimension
	 */
	@Nullable
	public WCM getDimension(@Nonnull ResourceLocation dimension);

	/**
	 * Gets a stream of all read-only dimension claims managers.
	 *
	 * @return a {@code Stream} of all read-only dimension claims managers
	 */
	@Nonnull
	public Stream<WCM> getDimensionStream();

	/**
	 * Gets the claim change tracker that lets you register claim change listeners.
	 * <p>
	 * The tracker notifies the registered listeners when claim changes occur.
	 *
	 * @return the claim change tracker
	 */
	@Nonnull
	public IClaimsManagerTrackerAPI getTracker();
	
}
