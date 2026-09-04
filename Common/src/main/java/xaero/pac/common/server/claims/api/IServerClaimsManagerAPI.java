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

package xaero.pac.common.server.claims.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.claims.api.IClaimsManagerAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerAPI;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.action.listener.api.IClaimActionListenerManagerAPI;
import xaero.pac.common.server.claims.player.api.IServerPlayerClaimInfoAPI;
import xaero.pac.common.server.claims.protection.override.api.IChunkAccessOverriderManagerAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * API for the claims manager on the server side.
 * <p>
 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims.
 */
public interface IServerClaimsManagerAPI
		extends IClaimsManagerAPI {

	@Override
	public boolean hasPlayerInfo(@Nonnull UUID playerId);
	
	@Nonnull
	@Override
	public IServerPlayerClaimInfoAPI getPlayerInfo(@Nonnull UUID playerId);

	/**
	 * Gets a stream of all player claim info.
	 *
	 * @return a {@code Stream} of all player claim info
	 */
	@Nonnull
	public Stream<IServerPlayerClaimInfoAPI> getPlayerInfoStream();
	
	@Nullable
	@Override
	public IPlayerChunkClaimAPI get(@Nonnull ResourceLocation dimension, int x, int z);
	
	@Nullable
	@Override
	public IPlayerChunkClaimAPI get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPos chunkPos);
	
	@Nullable
	@Override
	public IPlayerChunkClaimAPI get(@Nonnull ResourceLocation dimension, @Nonnull BlockPos blockPos);

	@Nullable
	@Override
	public IServerDimensionClaimsManagerAPI getDimension(@Nonnull ResourceLocation dimension);

	/**
	 * Gets a stream of all read-only dimension claims managers.
	 *
	 * @return a {@code Stream} of all read-only dimension claims managers
	 */
	@Nonnull
	public Stream<IServerDimensionClaimsManagerAPI> getDimensionStream();

	@Nonnull
	@Override
	public IClaimsManagerTrackerAPI getTracker();

	@Nonnull
	@Override
	Component getDefaultName(@Nullable IPlayerChunkClaimAPI claimState);

	@Nonnull
	@Override
	Component getDefaultName(@Nullable IPlayerChunkClaimAPI claimState, boolean allowPartyNames);

	@Nonnull
	@Override
	Component getDefaultName(@Nullable UUID claimId, boolean forceloadable);

	@Nonnull
	@Override
	Component getDefaultName(@Nullable UUID claimId, boolean forceloadable, boolean allowPartyNames);

	@Nonnull
	@Override
	Component getFullName(@Nullable IPlayerChunkClaimAPI claimState);

	@Nonnull
	@Override
	Component getFullName(@Nullable IPlayerChunkClaimAPI claimState, boolean allowPartyNames);

	@Nonnull
	@Override
	Component getFullName(@Nullable IPlayerChunkClaimAPI claimState, @Nullable ResourceLocation dimension, boolean allowPartyNames);

	@Override
	int getColor(@Nullable IPlayerChunkClaimAPI claimState, @Nullable ResourceLocation dimension);

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
	 * Use {@link #tryToClaim(ResourceLocation, UUID, int, ResourceLocation, int, int, int, int, boolean)} or
	 * {@link #tryToForceload(ResourceLocation, UUID, ResourceLocation, int, int, int, int, boolean, boolean)}
	 * instead if you want different limitations to be considered,
	 * e.g. maximum claim distance, maximum claim number, the chunk being already claimed etc.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to claim as them.
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
	public IPlayerChunkClaimAPI claim(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int subConfigIndex, int x, int z, boolean forceload);

	/**
	 * Directly removes the current claim state of a chunk.
	 * <p>
	 * It is usually a bad idea to give regular players unfiltered access to this method.
	 * Use {@link #tryToUnclaim(ResourceLocation, UUID, ResourceLocation, int, int, int, int, boolean)} instead
	 * if you want different limitations to be considered,
	 * e.g. maximum claim distance, the chunk being claimed by a different player etc.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 */
	public void unclaim(@Nonnull ResourceLocation dimension, int x, int z);

	/**
	 * @deprecated Use {@link #tryToClaim(ResourceLocation, UUID, int, ResourceLocation, int, int, int, int, boolean)} instead
	 * Tries to claim a chunk by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to claim as them.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (the new one if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the claiming player's UUID, not null
	 * @param subConfigIndex  the sub-config index to be used by the claim
	 * @param fromX  the X coordinate of the claiming player's current chunk position
	 * @param fromZ  the Z coordinate of the claiming player's current chunk position
	 * @param x  the X coordinate of the chunk to claim
	 * @param z  the Z coordinate of the chunk to claim
	 * @param replace  whether to ignore some limitations,
	 *                 mainly the existing claim state at the specified location and the maximum claim distance
	 * @return the result, not null
	 */
	@Deprecated
	@Nonnull
	default ClaimResult<IPlayerChunkClaimAPI> tryToClaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean replace){
		return tryToClaim(dimension, playerId, subConfigIndex, dimension, fromX, fromZ, x, z, replace);
	}

	/**
	 * @deprecated Use {@link #tryToUnclaim(ResourceLocation, UUID, ResourceLocation, int, int, int, int, boolean)} instead
	 * Tries to unclaim a chunk by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to unclaim as them.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (null if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the unclaiming player's UUID, not null
	 * @param fromX  the X coordinate of the unclaiming player's current chunk position
	 * @param fromZ  the Z coordinate of the unclaiming player's current chunk position
	 * @param x  the X coordinate of the chunk to unclaim
	 * @param z  the Z coordinate of the chunk to unclaim
	 * @param replace  whether to ignore some limitations,
	 *                 mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the result, not null
	 */
	@Deprecated
	@Nonnull
	default ClaimResult<IPlayerChunkClaimAPI> tryToUnclaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean replace) {
		return tryToUnclaim(dimension, playerId, dimension, fromX, fromZ, x, z, replace);
	}

	/**
	 * @deprecated Use {@link #tryToForceload(ResourceLocation, UUID, ResourceLocation, int, int, int, int, boolean, boolean)} instead
	 * Tries to (un)mark a chunk for forceloading by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum forceload number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to (un)forceload as them.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (the new one if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the forceloading player's UUID, not null
	 * @param fromX  the X coordinate of the forceloading player's current chunk position
	 * @param fromZ  the Z coordinate of the forceloading player's current chunk position
	 * @param x  the X coordinate of the chunk to (un)mark for forceloading
	 * @param z  the Z coordinate of the chunk to (un)mark for forceloading
	 * @param enable  true to mark for forceloading, false to unmark
	 * @param replace  whether to ignore some limitations,
	 *	               mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the result, not null
	 */
	@Deprecated
	@Nonnull
	default ClaimResult<IPlayerChunkClaimAPI> tryToForceload(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean enable, boolean replace) {
		return tryToForceload(dimension, playerId, dimension, fromX, fromZ, x, z, enable, replace);
	}

	/**
	 * @deprecated Use {@link #tryToClaimArea(ResourceLocation, UUID, int, ResourceLocation, int, int, int, int, int, int, boolean, Consumer)} instead
	 * Tries to claim chunks over a specified area by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to claim as them.
	 * <p>
	 * You get a {@link AreaClaimResult} containing all unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks, not null
	 * @param playerId  the claiming player's UUID, not null
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
	@Deprecated
	@Nonnull
	default AreaClaimResult tryToClaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int left, int top, int right, int bottom, boolean replace){
		return ((ServerClaimsManager)this).backwardsCompatibleClaimActionOverArea(dimension, playerId, subConfigIndex, fromX, fromZ, left, top, right, bottom, ClaimingAction.CLAIM, replace);
	}

	/**
	 * @deprecated Use {@link #tryToUnclaimArea(ResourceLocation, UUID, ResourceLocation, int, int, int, int, int, int, boolean, Consumer)} instead
	 * Tries to unclaim chunks over a specified area by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to unclaim as them.
	 * <p>
	 * You get a {@link AreaClaimResult} containing all unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks, not null
	 * @param playerId  the unclaiming player's UUID, not null
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
	@Deprecated
	@Nonnull
	default AreaClaimResult tryToUnclaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int left, int top, int right, int bottom, boolean replace){
		return ((ServerClaimsManager)this).backwardsCompatibleClaimActionOverArea(dimension, playerId, -1, fromX, fromZ, left, top, right, bottom, ClaimingAction.UNCLAIM, replace);
	}

	/**
	 * @deprecated Use {@link #tryToForceloadArea(ResourceLocation, UUID, ResourceLocation, int, int, int, int, int, int, boolean, boolean, Consumer)} instead
	 * Tries to (un)mark chunks for forceloading over a specified area by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum forceload number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to (un)forceload as them.
	 * <p>
	 * You get a {@link AreaClaimResult} containing all unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks, not null
	 * @param playerId  the forceloading player's UUID, not null
	 * @param fromX  the X coordinate of the forceloading player's current chunk position
	 * @param fromZ  the Z coordinate of the forceloading player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param enable  true to mark for forceloading, false to unmark
	 * @param replace  whether to ignore some limitations,
	 *	               mainly the existing claim owner at the specified location and the maximum claim distance
	 * @return the area result, not null
	 */
	@Deprecated
	@Nonnull
	default AreaClaimResult tryToForceloadArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int left, int top, int right, int bottom, boolean enable, boolean replace){
		return ((ServerClaimsManager)this).backwardsCompatibleClaimActionOverArea(dimension, playerId, -1, fromX, fromZ, left, top, right, bottom, enable ? ClaimingAction.FORCELOAD : ClaimingAction.UNFORCELOAD, replace);
	}

	/**
	 * Tries to claim a chunk by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to claim as them.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (the new one if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the claiming player's UUID, not null
	 * @param subConfigIndex  the sub-config index to be used by the claim
	 * @param fromDimension  the ID of the dimension the claiming player is currently in, not null
	 * @param fromX  the X coordinate of the claiming player's current chunk position
	 * @param fromZ  the Z coordinate of the claiming player's current chunk position
	 * @param x  the X coordinate of the chunk to claim
	 * @param z  the Z coordinate of the chunk to claim
	 * @param force  whether to ignore most limitations
	 * @return the result, not null
	 */
	@Nonnull
	public ClaimResult<IPlayerChunkClaimAPI> tryToClaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, @Nonnull ResourceLocation fromDimension, int fromX, int fromZ, int x, int z, boolean force);

	/**
	 * Tries to unclaim a chunk by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to unclaim as them.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (null if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the unclaiming player's UUID, not null
	 * @param fromDimension  the ID of the dimension the unclaiming player is currently in, not null
	 * @param fromX  the X coordinate of the unclaiming player's current chunk position
	 * @param fromZ  the Z coordinate of the unclaiming player's current chunk position
	 * @param x  the X coordinate of the chunk to unclaim
	 * @param z  the Z coordinate of the chunk to unclaim
	 * @param force  whether to ignore most limitations
	 * @return the result, not null
	 */
	@Nonnull
	public ClaimResult<IPlayerChunkClaimAPI> tryToUnclaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, @Nonnull ResourceLocation fromDimension, int fromX, int fromZ, int x, int z, boolean force);

	/**
	 * Tries to (un)mark a chunk for forceloading by a specified player.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum forceload number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to (un)forceload as them.
	 * <p>
	 * You get a {@link ClaimResult} containing a claim state where relevant (the new one if it's a success)
	 * and a message describing the result.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param playerId  the forceloading player's UUID, not null
	 * @param fromDimension  the ID of the dimension the forceloading player is currently in, not null
	 * @param fromX  the X coordinate of the forceloading player's current chunk position
	 * @param fromZ  the Z coordinate of the forceloading player's current chunk position
	 * @param x  the X coordinate of the chunk to (un)mark for forceloading
	 * @param z  the Z coordinate of the chunk to (un)mark for forceloading
	 * @param enable  true to mark for forceloading, false to unmark
	 * @param force  whether to ignore most limitations
	 * @return the result, not null
	 */
	@Nonnull
	public ClaimResult<IPlayerChunkClaimAPI> tryToForceload(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, @Nonnull ResourceLocation fromDimension, int fromX, int fromZ, int x, int z, boolean enable, boolean force);

	/**
	 * Tries to claim chunks over a specified area as a specified player.
	 * <p>
	 * The effect of calling this method is not immediate, and it may take many server ticks before the result is passed
	 * to a provided listener.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to claim as them.
	 * <p>
	 * When the claiming process is complete, an {@link AreaClaimResult} is passed to the provided listener containing all
	 * unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks to claim, not null
	 * @param playerId  the claiming player's UUID, not null
	 * @param subConfigIndex  the sub-config index to be used by the claims
	 * @param fromDimension  the ID of the dimension the claiming player is currently in, not null
	 * @param fromX  the X coordinate of the claiming player's current chunk position
	 * @param fromZ  the Z coordinate of the claiming player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param force  whether to ignore most limitations
	 * @param listener  the claiming result listener, not null
	 */
	public void tryToClaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, @Nonnull ResourceLocation fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, boolean force, @Nonnull Consumer<AreaClaimResult> listener);

	/**
	 * Tries to unclaim chunks over a specified area by a specified player.
	 * <p>
	 * The effect of calling this method is not immediate, and it may take many server ticks before the result is passed
	 * to a provided listener.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to unclaim as them.
	 * <p>
	 * When the unclaiming process is complete, an {@link AreaClaimResult} is passed to the provided listener containing all
	 * unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks to unclaim, not null
	 * @param playerId  the unclaiming player's UUID, not null
	 * @param fromDimension  the ID of the dimension the unclaiming player is currently in, not null
	 * @param fromX  the X coordinate of the unclaiming player's current chunk position
	 * @param fromZ  the Z coordinate of the unclaiming player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param force  whether to ignore most limitations
	 * @param listener  the claiming result listener, not null
	 */
	public void tryToUnclaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, @Nonnull ResourceLocation fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, boolean force, @Nonnull Consumer<AreaClaimResult> listener);

	/**
	 * Tries to (un)mark chunks for forceloading over a specified area by a specified player.
	 * <p>
	 * The effect of calling this method is not immediate, and it may take many server ticks before the result is passed
	 * to a provided listener.
	 * <p>
	 * Success is not guaranteed. Different limitations are checked, e.g. maximum forceload number, maximum claim distance, existing claims.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to (un)forceload as them.
	 * <p>
	 * When the forceload (un)marking process is complete, an {@link AreaClaimResult} is passed to the provided listener containing all
	 * unique result types, which contain messages describing the results.
	 *
	 * @param dimension  the dimension ID of the chunks to forceload, not null
	 * @param playerId  the forceloading player's UUID, not null
	 * @param fromDimension  the ID of the dimension the forceloading player is currently in, not null
	 * @param fromX  the X coordinate of the forceloading player's current chunk position
	 * @param fromZ  the Z coordinate of the forceloading player's current chunk position
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param enable  true to mark for forceloading, false to unmark
	 * @param force  whether to ignore most limitations
	 * @param listener  the claiming result listener, not null
	 */
	public void tryToForceloadArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, @Nonnull ResourceLocation fromDimension, int fromX, int fromZ, int left, int top, int right, int bottom, boolean enable, boolean force, @Nonnull Consumer<AreaClaimResult> listener);

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

	/**
	 * Gets the full maximum claim number (with the bonus) for a player UUID.
	 * <p>
	 * The returned value is equal to the value returned by {@link #getPlayerBaseClaimLimit(UUID)} with the bonus claims
	 * added to it.
	 *
	 * @param playerId  the player UUID, not null
	 * @return the full maximum claim number
	 */
	public int getPlayerFullClaimLimit(@Nonnull  UUID playerId);

	/**
	 * Gets the full maximum claim number (with the bonus) for a logged in player.
	 * <p>
	 * The returned value is equal to the value returned by {@link #getPlayerBaseClaimLimit(ServerPlayer)} with the bonus claims
	 * added to it.
	 *
	 * @param player  the player, not null
	 * @return the full maximum claim number
	 */
	public int getPlayerFullClaimLimit(@Nonnull  ServerPlayer player);

	/**
	 * Gets the full maximum forceload number (with the bonus) for a player UUID.
	 * <p>
	 * The returned value is equal to the value returned by {@link #getPlayerBaseForceloadLimit(UUID)} with the bonus forceloads
	 * added to it.
	 *
	 * @param playerId  the player UUID, not null
	 * @return the full maximum forceload number
	 */
	public int getPlayerFullForceloadLimit(@Nonnull UUID playerId);

	/**
	 * Gets the full maximum forceload number (with the bonus) for a logged in player.
	 * <p>
	 * The returned value is equal to the value returned by {@link #getPlayerBaseForceloadLimit(ServerPlayer)} with the bonus forceloads
	 * added to it.
	 *
	 * @param player  the player, not null
	 * @return the full maximum forceload number
	 */
	public int getPlayerFullForceloadLimit(@Nonnull ServerPlayer player);

	/**
	 * Gets the API for the claim action listener manager for this claims manager where you can register claim action listeners.
	 *
	 * @return the API for the claim action listener manager, not null
	 */
	@Nonnull
	public IClaimActionListenerManagerAPI getActionListenerManager();

	/**
	 * Gets the API for the chunk access overrider manager for this claims manager where you can register your chunk access
	 * overriders.
	 *
	 * @return the API for the chunk access overrider manager, not null
	 */
	@Nonnull
	public IChunkAccessOverriderManagerAPI getChunkAccessOverriderManager();

}
