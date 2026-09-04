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

package xaero.pac.common.claims.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimInfoAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for a claims manager.
 * <p>
 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims.
 */
public interface IClaimsManagerAPI {

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
	public IPlayerClaimInfoAPI getPlayerInfo(@Nonnull UUID playerId);

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
	public IDimensionClaimsManagerAPI getDimension(@Nonnull ResourceLocation dimension);

	/**
	 * Gets the claim change tracker that lets you register claim change listeners.
	 * <p>
	 * The tracker notifies the registered listeners when claim changes occur.
	 *
	 * @return the claim change tracker
	 */
	@Nonnull
	public IClaimsManagerTrackerAPI getTracker();

	/**
	 * Gets the default display name that would be used for a provided claim state.
	 * <p>
	 * Calling this method is the equivalent of calling {@link #getDefaultName(IPlayerChunkClaimAPI, boolean)} with
	 * allowPartyNames as true.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @return the default display name, not null
	 */
	@Nonnull
	public Component getDefaultName(@Nullable IPlayerChunkClaimAPI claimState);

	/**
	 * Gets the default display name that would be used for a provided claim state.
	 * <p>
	 * With this method you can specify whether you want party names applied to party-owned claims or to always
	 * use player names.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @param allowPartyNames  whether to apply party names to party-owned claims
	 * @return the default display name, not null
	 */
	@Nonnull
	public Component getDefaultName(@Nullable IPlayerChunkClaimAPI claimState, boolean allowPartyNames);

	/**
	 * Gets the default display name that would be used for a provided claim UUID.
	 * <p>
	 * Calling this method is the equivalent of calling {@link #getDefaultName(UUID, boolean, boolean)} with
	 * allowPartyNames as true.
	 *
	 * @param claimId  the UUID of the claimer owner, null for wilderness
	 * @param forceloadable  whether to get the default name for a forceloaded claim
	 * @return the default display name, not null
	 */
	@Nonnull
	public Component getDefaultName(@Nullable UUID claimId, boolean forceloadable);

	/**
	 * Gets the default display name that would be used for a provided claim UUID.
	 * <p>
	 * With this method you can specify whether you want party names applied to party-owned claims or to always
	 * use player names.
	 *
	 * @param claimId  the UUID of the claimer owner, null for wilderness
	 * @param forceloadable  whether to get the default name for a forceloaded claim
	 * @param allowPartyNames  whether to apply party names to party-owned claims
	 * @return the default display name, not null
	 */
	@Nonnull
	public Component getDefaultName(@Nullable UUID claimId, boolean forceloadable, boolean allowPartyNames);

	/**
	 * Gets the full display name, with sub-claim names applied, that would be used for a provided claim state.
	 * <p>
	 * Calling this method is the equivalent of calling {@link #getFullName(IPlayerChunkClaimAPI, boolean)} with
	 * allowPartyNames as true.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @return the full display name, not null
	 */
	@Nonnull
	public Component getFullName(@Nullable IPlayerChunkClaimAPI claimState);

	/**
	 * Gets the full display name, with sub-claim names applied, that would be used for a provided claim state.
	 * <p>
	 * With this method you can specify whether you want party names applied to party-owned claims or to always
	 * use player names.
	 * <p>
	 * This method does not apply dimension-specific names to wilderness or expired claims. Use {@link #getFullName(IPlayerChunkClaimAPI, ResourceLocation, boolean)}
	 * instead for that.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @param allowPartyNames  whether to apply party names to party-owned claims
	 * @return the full display name, not null
	 */
	@Nonnull
	public Component getFullName(@Nullable IPlayerChunkClaimAPI claimState, boolean allowPartyNames);

	/**
	 * Gets the full display name, with sub-claim names applied, that would be used for a provided claim state and dimension.
	 * <p>
	 * The dimension argument only matters when claimState is null (wilderness) or an expired claim.
	 * <p>
	 * With this method you can specify whether you want party names applied to party-owned claims or to always
	 * use player names.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @param dimension  the dimension the claim is in, null when doesn't matter
	 * @param allowPartyNames  whether to apply party names to party-owned claims
	 * @return the full display name, not null
	 */
	@Nonnull
	public Component getFullName(@Nullable IPlayerChunkClaimAPI claimState, @Nullable ResourceLocation dimension, boolean allowPartyNames);

	/**
	 * Gets the custom display name that would be used for a provided claim state and dimension.
	 * <p>
	 * The dimension argument only matters when claimState is null (wilderness) or an expired claim.
	 * <p>
	 * The returned value on the client side is null before the custom name is synced from the server.
	 * The returned name may also not correspond to the dimension before the dimension-specific
	 * name is synced.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @param dimension  the dimension the claim is in, null when doesn't matter
	 * @return the custom name of the claims, null if there is none
	 */
	@Nullable
	public String getCustomName(@Nullable IPlayerChunkClaimAPI claimState, @Nullable ResourceLocation dimension);

	/**
	 * Gets the color that would be used for a provided claim state and dimension.
	 * <p>
	 * The dimension argument only matters when claimState is null (wilderness) or an expired claim.
	 * <p>
	 * The returned value on the client side is the default color (0 for actual players) before the color is synced
	 * from the server. The returned color may also not correspond to the dimension before the dimension-specific
	 * color is synced.
	 *
	 * @param claimState  the claim state, can be null for wilderness
	 * @param dimension  the dimension the claim is in, null when doesn't matter
	 * @return the color of the claim state
	 */
	public int getColor(@Nullable IPlayerChunkClaimAPI claimState, @Nullable ResourceLocation dimension);

}
