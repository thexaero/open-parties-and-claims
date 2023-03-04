/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.api.IClaimsManagerAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerAPI;
import xaero.pac.common.server.claims.player.api.IServerPlayerClaimInfoAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for the claims manager on the server side
 */
public interface IServerClaimsManagerAPI
<
	C extends IPlayerChunkClaimAPI,
	PCI extends IServerPlayerClaimInfoAPI<?>,
	WCM extends IServerDimensionClaimsManagerAPI<?>
> extends IClaimsManagerAPI<PCI, WCM>{

	@Override
	public boolean hasPlayerInfo(@Nonnull UUID playerId);
	
	@Nonnull
	@Override
	public PCI getPlayerInfo(@Nonnull UUID playerId);
	
	@Nonnull
	@Override
	public Stream<PCI> getPlayerInfoStream();
	
	@Nullable
	@Override
	public C get(@Nonnull ResourceLocation dimension, int x, int z);
	
	@Nullable
	@Override
	public C get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPos chunkPos);
	
	@Nullable
	@Override
	public C get(@Nonnull ResourceLocation dimension, @Nonnull BlockPos blockPos);

	@Nullable
	@Override
	public WCM getDimension(@Nonnull ResourceLocation dimension);

	@Nonnull
	@Override
	public Stream<WCM> getDimensionStream();

	@Nonnull
	@Override
	public IClaimsManagerTrackerAPI getTracker();

	/**
	 * Checks whether a dimension is claimable.
	 * <p>
	 * Claimable dimensions are determined by the server config.
	 *
	 * @param dimension  the dimension ID
	 * @return whether the dimension is claimable
	 */
	public boolean isClaimable(@Nonnull ResourceLocation dimension);

	/**
	 * Directly replaces the current claim state of a chunk.
	 * <p>
	 * It is usually a bad idea to give regular players unfiltered access to this method.
	 * Use {@link #tryToClaim} or {@link #tryToForceload} instead if you want different limitations to be considered,
	 * e.g. maximum claim distance, maximum claim number, the chunk being already claimed etc.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param id  the claim owner UUID, not null
	 * @param subConfigIndex  the sub-config index to be used by the claim
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param forceload  whether the chunk should be marked for forceloading
	 * @return the new claim state, null if claims are disabled
	 */
	@Nullable
	public C claim(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int subConfigIndex, int x, int z, boolean forceload);

	/**
	 * Directly removes the current claim state of a chunk.
	 * <p>
	 * It is usually a bad idea to give regular players unfiltered access to this method.
	 * Use {@link #tryToUnclaim} instead if you want different limitations to be considered,
	 * e.g. maximum claim distance, the chunk being claimed by a different player etc.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 */
	public void unclaim(@Nonnull ResourceLocation dimension, int x, int z);

	/**
	 * Tries to claim a chunk by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim number, maximum claim distance, existing claims.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (the new one if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the claiming player UUID, not null
	 * @param subConfigIndex  the sub-config index to be used by the claim
	 * @param fromX  the X coordinate of the claiming player's current chunk position
	 * @param fromZ  the Z coordinate of the claiming player's current chunk position
	 * @param x  the X coordinate of the chunk to claim
	 * @param z  the Z coordinate of the chunk to claim
	 * @param replace  whether to ignore some limitations,
	 *                 mainly the existing claim state at the specified location and the maximum claim distance
	 * @return the result, not null
	 */
	@Nonnull
	public ClaimResult<C> tryToClaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean replace);

	/**
	 * Tries to unclaim a chunk by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim distance, existing claims.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (null if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the unclaiming player UUID, not null
	 * @param fromX  the X coordinate of the unclaiming player's current chunk position
	 * @param fromZ  the Z coordinate of the unclaiming player's current chunk position
	 * @param x  the X coordinate of the chunk to unclaim
	 * @param z  the Z coordinate of the chunk to unclaim
	 * @param replace  whether to ignore some limitations,
	 *                 mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the result, not null
	 */
	@Nonnull
	public ClaimResult<C> tryToUnclaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean replace);

	/**
	 * Tries to (un)mark a chunk for forceloading by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum forceload number, maximum claim distance, existing claims.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (the new one if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the claiming player UUID, not null
	 * @param fromX  the X coordinate of the claiming player's current chunk position
	 * @param fromZ  the Z coordinate of the claiming player's current chunk position
	 * @param x  the X coordinate of the chunk to (un)mark for forceloading
	 * @param z  the Z coordinate of the chunk to (un)mark for forceloading
	 * @param enable  true to mark for forceloading, false to unmark
	 * @param replace  whether to ignore some limitations,
	 *	               mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the result, not null
	 */
	@Nonnull
	public ClaimResult<C> tryToForceload(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean enable, boolean replace);

	/**
	 * Tries to claim chunks over a specified area by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim number, maximum claim distance, existing claims.
	 * <p>
	 * You get a {@link AreaClaimResult} containing all unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks, not null
	 * @param playerId  the claiming player UUID, not null
	 * @param subConfigIndex  the sub-config index to be used by the claims
	 * @param fromX  the X coordinate of the claiming player's current chunk position
	 * @param fromZ  the Z coordinate of the claiming player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param replace  whether to ignore some limitations,
	 *	               mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the area result, not null
	 */
	@Nonnull
	public AreaClaimResult tryToClaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int left, int top, int right, int bottom, boolean replace);

	/**
	 * Tries to unclaim chunks over a specified area by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim distance, existing claims.
	 * <p>
	 * You get a {@link AreaClaimResult} containing all unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks, not null
	 * @param playerId  the claiming player UUID, not null
	 * @param fromX  the X coordinate of the unclaiming player's current chunk position
	 * @param fromZ  the Z coordinate of the unclaiming player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param replace  whether to ignore some limitations,
	 *	               mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the area result, not null
	 */
	@Nonnull
	public AreaClaimResult tryToUnclaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int left, int top, int right, int bottom, boolean replace);

	/**
	 * Tries to (un)mark chunks for forceloading over a specified area by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum forceload number, maximum claim distance, existing claims.
	 * <p>
	 * You get a {@link AreaClaimResult} containing all unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks, not null
	 * @param playerId  the claiming player UUID, not null
	 * @param fromX  the X coordinate of the unclaiming player's current chunk position
	 * @param fromZ  the Z coordinate of the unclaiming player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param enable  true to mark for forceloading, false to unmark
	 * @param replace  whether to ignore some limitations,
	 *	               mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the area result, not null
	 */
	@Nonnull
	public AreaClaimResult tryToForceloadArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int left, int top, int right, int bottom, boolean enable, boolean replace);

	/**
	 * Gets the base maximum claim number (without the bonus) for a player UUID.
	 * <p>
	 * By default, the base claim limit is configured in this mod's server config file.
	 * However, if the FTB Ranks mod is installed, a permission node is configured in the claim mod's server config
	 * and the player with UUID {@code playerId} is logged in, then the permission value is used as the base limit.
	 *
	 * @param playerId  the player UUID, not null
	 * @return the base maximum claim number
	 */
	public int getPlayerBaseClaimLimit(@Nonnull  UUID playerId);

	/**
	 * Gets the base maximum claim number (without the bonus) for a logged in player.
	 * <p>
	 * By default, the base claim limit is configured in this mod's server config file.
	 * However, if the FTB Ranks mod is installed and a permission node is configured in the claim mod's server config,
	 * then the permission value is used as the base limit.
	 *
	 * @param player  the player, not null
	 * @return the base maximum claim number
	 */
	public int getPlayerBaseClaimLimit(@Nonnull  ServerPlayer player);

	/**
	 * Gets the base maximum forceload number (without the bonus) for a player UUID.
	 * <p>
	 * By default, the base forceload limit is configured in this mod's server config file.
	 * However, if the FTB Ranks mod is installed, a permission node is configured in the claim mod's server config
	 * and the player with UUID {@code playerId} is logged in, then the permission value is used as the base limit.
	 *
	 * @param playerId  the player UUID, not null
	 * @return the base maximum forceload number
	 */
	public int getPlayerBaseForceloadLimit(@Nonnull UUID playerId);

	/**
	 * Gets the base maximum forceload number (without the bonus) for a logged in player.
	 * <p>
	 * By default, the base forceload limit is configured in this mod's server config file.
	 * However, if the FTB Ranks mod is installed and a permission node is configured in the claim mod's server config,
	 * then the permission value is used as the base limit.
	 *
	 * @param player  the player, not null
	 * @return the base maximum forceload number
	 */
	public int getPlayerBaseForceloadLimit(@Nonnull ServerPlayer player);

}
