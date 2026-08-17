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

package xaero.pac.client.claims.api;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xaero.pac.client.claims.player.api.IClientPlayerClaimInfoAPI;
import xaero.pac.client.claims.tracker.result.api.IClaimsManagerClaimResultTrackerAPI;
import xaero.pac.common.claims.api.IClaimsManagerAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.impersonation.api.IPlayerClaimImpersonationInfoAPI;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for the claims manager on the client side.
 * <p>
 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims.
 */
public interface IClientClaimsManagerAPI
		extends IClaimsManagerAPI {

	@Override
	public boolean hasPlayerInfo(@Nonnull UUID playerId);

	@Nonnull
	@Override
	public IClientPlayerClaimInfoAPI getPlayerInfo(@Nonnull UUID playerId);

	/**
	 * Gets a stream of all player claim info.
	 *
	 * @return a {@code Stream} of all player claim info
	 */
	@Nonnull
	public Stream<IClientPlayerClaimInfoAPI> getPlayerInfoStream();
	
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
	public IClientDimensionClaimsManagerAPI getDimension(@Nonnull ResourceLocation dimension);

	/**
	 * Gets a stream of all read-only dimension claims managers.
	 *
	 * @return a {@code Stream} of all read-only dimension claims managers
	 */
	@Nonnull
	public Stream<IClientDimensionClaimsManagerAPI> getDimensionStream();

	@Nonnull
	@Override
	public IClaimsManagerTrackerAPI getTracker();

	/**
	 * Checks whether the initial server chunk claim sync is in progress.
	 * <p>
	 * Chunk claims start loading in the background when you join the server.
	 *
	 * @return true if the initial chunk claim sync is in progress, otherwise false
	 */
	public boolean isLoading();

	/**
	 * Gets the claim count of the local client player.
	 * <p>
	 * This is not necessarily up-to-date with the actual synced claim data at the time of calling.
	 *
	 * @return the claim count of the player
	 */
	public int getClaimCount();

	/**
	 * Gets the forceload count of the local client player.
	 * <p>
	 * This is not necessarily up-to-date with the actual synced claim data at the time of calling.
	 *
	 * @return the forceload count of the player
	 */
	public int getForceloadCount();

	/**
	 * Gets the local client player's chunk claim limit.
	 *
	 * @return the claim limit
	 */
	public int getClaimLimit();

	/**
	 * Gets the local client player's chunk claim forceload limit.
	 *
	 * @return the forceload limit
	 */
	public int getForceloadLimit();

	/**
	 * Gets the claim count for a specified claiming mode.
	 * <p>
	 * This is not necessarily up-to-date with the actual synced claim data at the time of calling.
	 *
	 * @param mode  the claiming mode, not null
	 * @return the claim count for the mode
	 */
	public int getClaimCount(@Nonnull IClaimingModeAPI mode);

	/**
	 * Gets the forceload count for a specified claiming mode.
	 * <p>
	 * This is not necessarily up-to-date with the actual synced claim data at the time of calling.
	 *
	 * @param mode  the claiming mode, not null
	 * @return the forceload count for the mode
	 */
	public int getForceloadCount(@Nonnull IClaimingModeAPI mode);

	/**
	 * Gets the claim limit for a specified claiming mode.
	 *
	 * @param mode  the claiming mode, not null
	 * @return the claim limit for the mode
	 */
	public int getClaimLimit(@Nonnull IClaimingModeAPI mode);

	/**
	 * Gets the forceload limit for a specified claiming mode.
	 *
	 * @param mode  the claiming mode, not null
	 * @return the forceload limit for the mode
	 */
	public int getForceloadLimit(@Nonnull IClaimingModeAPI mode);

	/**
	 * Gets the maximum distance for claiming/forceloading a chunk allowed by the server.
	 *
	 * @return the maximum claim distance
	 */
	public int getMaxClaimDistance();

	/**
	 * Checks whether the local client player is in moderator or admin mode.
	 *
	 * @return true if the player is effectively in moderator mode, otherwise false
	 */
	public boolean isModeratorMode();

	/**
	 * Checks whether the local client player is in admin mode.
	 *
	 * @return true if the player is in admin mode, otherwise false
	 */
	public boolean isAdminMode();

	/**
	 * @deprecated Use {@link #getClaimingMode()} instead
	 * <p>
	 * Checks whether the local client player is in server claim mode.
	 *
	 * @return true if the player is in server claim mode, otherwise false
	 */
	@Deprecated
	public boolean isServerMode();

	/**
	 * Gets the currently enabled effective claiming mode.
	 *
	 * @return the current effective claiming mode, not null
	 */
	@Nonnull
	public IClaimingModeAPI getClaimingMode();

	/**
	 * Gets the currently enabled claiming mode without automatically determining the effective one if none is set.
	 *
	 * @return the current claiming mode, null if none is set
	 */
	@Nullable
	public IClaimingModeAPI getRawClaimingMode();

	/**
	 * Gets the claim action result tracker that lets you register claim action result listeners.
	 * <p>
	 * The tracker notifies the registered listeners when the client receives a claim result from
	 * the server. This happens after the player requests a claim action, e.g. to (un)claim some chunks.
 	 *
	 * @return the claim action result tracker, not null
	 */
	@Nonnull
	public IClaimsManagerClaimResultTrackerAPI getClaimResultTracker();

	/**
	 * @deprecated switch to {@link IClientClaimsManagerAPI#requestClaim(ResourceLocation, int, int, IClaimingModeAPI)}
	 * <p>
	 * Requests a new chunk claim by the local client player, party or by the server.
	 * <p>
	 * Only OPs can request claims by the server.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 *
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param byServer  whether the claim should be made by the server
	 */
	@Deprecated
	default void requestClaim(int x, int z, boolean byServer){
		requestClaim(x, z, byServer ? ClaimingModes.SERVER : null);
	}

	/**
	 * @deprecated switch to {@link IClientClaimsManagerAPI#requestUnclaim(ResourceLocation, int, int, IClaimingModeAPI)}
	 * <p>
	 * Requests a chunk unclaim by the local client player or by the server.
	 * <p>
	 * Only OPs can request unclaims by the server.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 *
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param byServer  whether the unclaim should be made by the server
	 */
	@Deprecated
	default void requestUnclaim(int x, int z, boolean byServer){
		requestUnclaim(x, z, byServer ? ClaimingModes.SERVER : null);
	}

	/**
	 * @deprecated switch to {@link IClientClaimsManagerAPI#requestForceload(ResourceLocation, int, int, boolean, IClaimingModeAPI)}
	 * <p>
	 * Requests a chunk (un)forceload by the local client player or by the server.
	 * <p>
	 * Only OPs can request (un)forceloads by the server.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 *
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param enable  true to forceload the chunk, false to unforceload
	 * @param byServer  whether the (un)forceload should be made by the server
	 */
	@Deprecated
	default void requestForceload(int x, int z, boolean enable, boolean byServer){
		requestForceload(x, z, enable, byServer ? ClaimingModes.SERVER : null);
	}

	/**
	 * @deprecated switch to {@link IClientClaimsManagerAPI#requestAreaClaim(ResourceLocation, int, int, int, int, IClaimingModeAPI)}
	 * <p>
	 * Requests new chunks claims over a specified area by the local client player or by the server.
	 * <p>
	 * Only OPs can request claims by the server.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 *
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param byServer  whether the claim should be made by the server
	 */
	@Deprecated
	default void requestAreaClaim(int left, int top, int right, int bottom, boolean byServer){
		requestAreaClaim(left, top, right, bottom, byServer ? ClaimingModes.SERVER : null);
	}

	/**
	 * @deprecated switch to {@link IClientClaimsManagerAPI#requestAreaUnclaim(ResourceLocation, int, int, int, int, IClaimingModeAPI)}
	 * <p>
	 * Requests chunk unclaims over a specified area by the local client player or by the server.
	 * <p>
	 * Only OPs can request unclaims by the server.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 *
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param byServer  whether the unclaim should be made by the server
	 */
	@Deprecated
	default void requestAreaUnclaim(int left, int top, int right, int bottom, boolean byServer){
		requestAreaUnclaim(left, top, right, bottom, byServer ? ClaimingModes.SERVER : null);
	}

	/**
	 * @deprecated switch to {@link IClientClaimsManagerAPI#requestAreaForceload(ResourceLocation, int, int, int, int, boolean, IClaimingModeAPI)}
	 * <p>
	 * Requests chunk (un)forceloads over a specified area by the local client player or by the server.
	 * <p>
	 * Only OPs can request (un)forceloads by the server.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 *
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param enable  true to forceload the chunks, false to unforceload
	 * @param byServer  whether the (un)forceload should be made by the server
	 */
	@Deprecated
	default void requestAreaForceload(int left, int top, int right, int bottom, boolean enable, boolean byServer){
		requestAreaForceload(left, top, right, bottom, enable, byServer ? ClaimingModes.SERVER : null);
	}

	/**
	 * @deprecated Use {@link #requestClaim(ResourceLocation, int, int, IClaimingModeAPI)} instead
	 * Requests a new chunk claim by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	@Deprecated
	default void requestClaim(int x, int z, @Nullable IClaimingModeAPI claimingMode){
		ResourceLocation dimension =  Minecraft.getInstance().player == null ? Level.OVERWORLD.location() :
				Minecraft.getInstance().player.level().dimension().location();
		requestClaim(dimension, x, z, claimingMode);
	}

	/**
	 * @deprecated Use {@link #requestUnclaim(ResourceLocation, int, int, IClaimingModeAPI)} instead
	 * Requests a chunk unclaim by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	@Deprecated
	default void requestUnclaim(int x, int z, @Nullable IClaimingModeAPI claimingMode){
		ResourceLocation dimension =  Minecraft.getInstance().player == null ? Level.OVERWORLD.location() :
				Minecraft.getInstance().player.level().dimension().location();
		requestUnclaim(dimension, x, z, claimingMode);
	}

	/**
	 * @deprecated Use {@link #requestForceload(ResourceLocation, int, int, boolean, IClaimingModeAPI)} instead
	 * Requests a chunk (un)forceload by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param enable  true to forceload the chunk, false to unforceload
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	@Deprecated
	default void requestForceload(int x, int z, boolean enable, @Nullable IClaimingModeAPI claimingMode){
		ResourceLocation dimension =  Minecraft.getInstance().player == null ? Level.OVERWORLD.location() :
				Minecraft.getInstance().player.level().dimension().location();
		requestForceload(dimension, x, z, enable, claimingMode);
	}

	/**
	 * @deprecated Use {@link #requestAreaClaim(ResourceLocation, int, int, int, int, IClaimingModeAPI)} instead
	 * Requests new chunks claims over a specified area by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	@Deprecated
	default void requestAreaClaim(int left, int top, int right, int bottom, @Nullable IClaimingModeAPI claimingMode){
		ResourceLocation dimension =  Minecraft.getInstance().player == null ? Level.OVERWORLD.location() :
				Minecraft.getInstance().player.level().dimension().location();
		requestAreaClaim(dimension, left, top, right, bottom, claimingMode);
	}

	/**
	 * @deprecated Use {@link #requestAreaUnclaim(ResourceLocation, int, int, int, int, IClaimingModeAPI)} instead
	 * Requests chunk unclaims over a specified area by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	@Deprecated
	default void requestAreaUnclaim(int left, int top, int right, int bottom, @Nullable IClaimingModeAPI claimingMode){
		ResourceLocation dimension =  Minecraft.getInstance().player == null ? Level.OVERWORLD.location() :
				Minecraft.getInstance().player.level().dimension().location();
		requestAreaUnclaim(dimension, left, top, right, bottom, claimingMode);
	}

	/**
	 * @deprecated Use {@link #requestAreaForceload(ResourceLocation, int, int, int, int, boolean, IClaimingModeAPI)} instead
	 * Requests chunk (un)forceloads over a specified area by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param enable  true to forceload the chunks, false to unforceload
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	@Deprecated
	default void requestAreaForceload(int left, int top, int right, int bottom, boolean enable, @Nullable IClaimingModeAPI claimingMode){
		ResourceLocation dimension =  Minecraft.getInstance().player == null ? Level.OVERWORLD.location() :
				Minecraft.getInstance().player.level().dimension().location();
		requestAreaForceload(dimension, left, top, right, bottom, enable, claimingMode);
	}

	/**
	 * Requests a new chunk claim by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	public void requestClaim(@Nonnull ResourceLocation dimension, int x, int z, @Nullable IClaimingModeAPI claimingMode);

	/**
	 * Requests a chunk unclaim by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	public void requestUnclaim(@Nonnull ResourceLocation dimension, int x, int z, @Nullable IClaimingModeAPI claimingMode);

	/**
	 * Requests a chunk (un)forceload by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param dimension  the dimension ID of the chunk, not null
	 * @param x  the X coordinate of the chunk
	 * @param z  the Z coordinate of the chunk
	 * @param enable  true to forceload the chunk, false to unforceload
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	public void requestForceload(@Nonnull ResourceLocation dimension, int x, int z, boolean enable, @Nullable IClaimingModeAPI claimingMode);

	/**
	 * Requests new chunks claims over a specified area by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param dimension  the dimension ID of the area, not null
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	public void requestAreaClaim(@Nonnull ResourceLocation dimension, int left, int top, int right, int bottom, @Nullable IClaimingModeAPI claimingMode);

	/**
	 * Requests chunk unclaims over a specified area by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param dimension  the dimension ID of the area, not null
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	public void requestAreaUnclaim(@Nonnull ResourceLocation dimension, int left, int top, int right, int bottom, @Nullable IClaimingModeAPI claimingMode);

	/**
	 * Requests chunk (un)forceloads over a specified area by the local client player using a specified claiming mode.
	 * <p>
	 * Register a claim result listener with {@link IClaimsManagerClaimResultTrackerAPI} to receive the result of this request.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param dimension  the dimension ID of the area, not null
	 * @param left  the lowest X coordinate of the area
	 * @param top  the lowest Z coordinate of the area
	 * @param right  the highest X coordinate of the area
	 * @param bottom  the highest Z coordinate of the area
	 * @param enable  true to forceload the chunks, false to unforceload
	 * @param claimingMode  the claiming mode to use, null for current
	 */
	public void requestAreaForceload(@Nonnull ResourceLocation dimension, int left, int top, int right, int bottom, boolean enable, @Nullable IClaimingModeAPI claimingMode);

	/**
	 * Gets a claim state of the same type (see {@link IPlayerChunkClaimAPI#isSameClaimType(IPlayerChunkClaimAPI)}) as
	 * would be placed in the world when the player requests a new claim.
	 * <p>
	 * It's not the actual instance of the claim state that would be placed in the world but it's good enough
	 * for comparisons against actual existing claim states.
	 *
	 * @return a claim state reflection of the same type as a new claim, null if unknown
	 */
	@Nullable
	public IPlayerChunkClaimAPI getPotentialClaimStateReflection();

	/**
	 * Gets the index of the sub-config currently used for new claims.
	 *
	 * @return the current sub-config index
	 */
	public int getCurrentSubConfigIndex();

	/**
	 * @deprecated Use {@link #getCurrentSubConfigIndex(IClaimingModeAPI)} instead
	 * <p>
	 * Gets the index of the sub-config currently used for new server claims.
	 *
	 * @return the current server sub-config index
	 */
	@Deprecated
	public int getCurrentServerSubConfigIndex();

	/**
	 * Gets the index of the sub-config currently used for a specified claiming mode.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param claimingMode  the claiming mode, not null
	 * @return the current sub-config index for the claiming mode
	 */
	public int getCurrentSubConfigIndex(IClaimingModeAPI claimingMode);

	/**
	 * Gets the string ID of the sub-config currently used for new claims.
	 *
	 * @return the current sub-config ID, not null
	 */
	@Nonnull
	public String getCurrentSubConfigId();

	/**
	 * @deprecated Use {@link #getCurrentSubConfigId(IClaimingModeAPI)} instead
	 * <p>
	 * Gets the string ID of the sub-config currently used for new server claims.
	 *
	 * @return the current server sub-config ID, not null
	 */
	@Deprecated
	@Nonnull
	public String getCurrentServerSubConfigId();

	/**
	 * Gets the string ID of the sub-config currently used for a specified claiming mode.
	 * <p>
	 * You can get all claiming modes from {@link ClaimingModes}.
	 *
	 * @param claimingMode  the claiming mode, not null
	 * @return the current sub-config ID for the claiming mode, not null
	 */
	@Nonnull
	public String getCurrentSubConfigId(IClaimingModeAPI claimingMode);

	/**
	 * Gets the UUID of the owner of the primary party that the local player is in.
	 *
	 * @return the UUID of the local player's party owner, null before synced or when not in party
	 */
	@Nullable
	public UUID getCurrentPartyOwner();

	/**
	 * Gets the API for the player claim impersonation info for the local player.
	 *
	 * @return API for getting info about player claim impersonation by the local player, not null
	 */
	@Nonnull
	public IPlayerClaimImpersonationInfoAPI getPlayerImpersonationInfo();
	
}
