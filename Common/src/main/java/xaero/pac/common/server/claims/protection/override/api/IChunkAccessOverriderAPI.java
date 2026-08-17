/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.protection.override.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The API for implementation of chunk protection overriders by addons
 */
public interface IChunkAccessOverriderAPI {

	/**
	 * Gets the display name for this chunk protection overrider.
	 *
	 * @return the name String, not null
	 */
	@Nonnull
	String getName();

	/**
	 * Lets you override full chunk access for a specified accessor.
	 * <p>
	 * Argument for accessor being null usually means that the accessor entity doesn't exist anymore or it's an offline
	 * player. Argument for accessorId is never null though.
	 * <p>
	 * This is mainly meant for addons that want to implement a claim access expiration system or a claim raiding system
	 * but can be used for other things as well.
	 * <p>
	 * This only affects whether a player has full access to a claim. Other claim exceptions are not affected by this at all!
	 * <p>
	 * When you don't have a reason to override access, then it is recommended to simply return currentOverride.
	 * <p>
	 * To override access to a chunk, return a new instance of {@link ChunkAccessOverride} with one of the types
	 * from {@link ChunkAccessOverrideType}.
	 * <p>
	 * Overriding access here doesn't cause any special notifications to be displayed to the player.
	 * You must appropriately explain the override to the player yourself, e.g. with your own UI.
	 * Because this method may be called multiple times for a single action, it is not the best place to notify the
	 * player from, but with a cooldown timer or a server tick count check, you can still get it to work nicely.
	 *
	 * @param dim  the dimension ID of the chunk, null when not provided
	 * @param x  the X chunk coordinate, Integer.MIN_VALUE when not provided
	 * @param z  the Z chunk coordinate, Integer.MIN_VALUE when not provided
	 * @param claimConfig  the actual player (sub-)config used for the claim, not null
	 * @param accessor  the entity or player accessing the chunk, can be null
	 * @param accessorId  the UUID of the entity or player accessing the chunk, not null
	 * @param server  the Minecraft server, not null
	 * @param currentOverride  the current candidate to override the access with, not null
	 * @return the chunk access override instance, not null
	 */
	@Nonnull
	ChunkAccessOverride overrideChunkAccess(
			@Nullable
			ResourceLocation dim,
			int x,
			int z,
			@Nonnull
			IPlayerConfigAPI claimConfig,
			@Nullable
			Entity accessor,
			@Nonnull
			UUID accessorId,
			@Nonnull
			MinecraftServer server,
			@Nonnull
			ChunkAccessOverride currentOverride
	);

}
