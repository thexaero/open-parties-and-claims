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

package xaero.pac.common.server.claims.protection.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for the claim-based chunk protection.
 * <p>
 * The methods provided here should enable you to implement basic support for claim protection on your mod's end.
 * <p>
 * Only use this if the Forge/Fabric events and injections this mod uses by default are not already protecting claims.
 * <p>
 * If a claim is overprotected from your mod, you might want to {@link #giveFullPass(UUID)} to an entity to circumvent
 * all protection except when using methods from this API. If you only want temporary protection circumvention,
 * please use {@link #removeFullPass(UUID)} after you're done.
 */
public interface IChunkProtectionAPI {

	/**
	 * Checks whether a specified block interaction should be protected against.
	 * <p>
	 * Whenever possible, use the built-in block interaction events provided by the mod loader (Fabric/Forge)
	 * instead of this method, unless they aren't specific enough, or the entity has a full protection
	 * pass ({@link #giveFullPass(UUID)}).
	 *
	 * @param entity  the entity to interact with the block, can be null
	 * @param hand  the hand that the entity is to interact with, can be null
	 * @param heldItem  the item stack that the entity is to interact with, null to fetch it from the hand
	 * @param world  the world that the entity is to interact with, not null
	 * @param pos  the block position that the entity is to interact with, not null
	 * @param direction  the direction which the entity is to interact from, not null
	 * @param breaking  whether the interaction is to break the block
	 * @param messages  whether to send the player system chat messages on protection
	 * @return true if the block interaction should be protected against, otherwise false
	 */
	boolean onBlockInteraction(@Nullable Entity entity, @Nullable InteractionHand hand, @Nullable ItemStack heldItem, @Nonnull ServerLevel world, @Nonnull BlockPos pos, @Nonnull Direction direction, boolean breaking, boolean messages);

	/**
	 * Checks whether a specified block placement should be protected against.
	 * <p>
	 * Whenever possible, use the built-in block placement event provided by Forge instead of this method,
	 * unless it isn't specific enough, you're on Fabric, or the entity has a full protection pass
	 * ({@link #giveFullPass(UUID)}).
	 *
	 * @param entity  the entity to place the block, can be null
	 * @param world  the world to place the block in, not null
	 * @param pos  the block position to place the block at, not null
	 * @return true if the block placement should be protected against, otherwise false
	 */
	boolean onEntityPlaceBlock(@Nullable Entity entity, @Nonnull ServerLevel world, @Nonnull BlockPos pos);

	/**
	 * Checks whether an entity interaction should be protected against.
	 * <p>
	 * Whenever possible, use the built-in entity interaction event provided by Forge instead of this method,
	 * unless it isn't specific enough, you're on Fabric, or the interacting entity has a full protection pass
	 * ({@link #giveFullPass(UUID)}).
	 *
	 * @param interactingEntityIndirect  the entity that is to interact indirectly, can be null
	 * @param interactingEntity  the directly interacting entity, can be null
	 * @param targetEntity  the entity to interact with, not null
	 * @param heldItem  the item stack to interact with, null to fetch it from the hand
	 * @param hand  the hand to interact with, can be null
	 * @param attack  whether the interaction is to attack the target entity
	 * @param messages  whether to send the player system chat messages on protection
	 * @return true if the entity interaction should be protected against, otherwise false
	 */
	boolean onEntityInteraction(@Nullable Entity interactingEntityIndirect, @Nullable Entity interactingEntity, @Nonnull Entity targetEntity, @Nullable ItemStack heldItem, @Nullable InteractionHand hand, boolean attack, boolean messages);

	/**
	 * Checks whether a specified entity picking up a specified item entity should be protected against.
	 *
	 * @param entity  the entity to pick up the item, not null
	 * @param itemEntity  the item entity to pick up, not null
	 * @return true if the item should be protected from being picked up, otherwise false
	 */
	boolean onItemPickup(@Nonnull Entity entity, @Nonnull ItemEntity itemEntity);

	/**
	 * Checks whether an anonymous action coming from one chunk position to another should be
	 * protected against, based on protection that affects placing blocks and using items in both positions.
	 * <p>
	 * This is meant for mechanics that don't necessarily have a usable owner to plug into the entity/block/item
	 * interaction checks but can be associated with a block position that they operate from, e.g. when a
	 * certain placed block affects other blocks around it. This check will prohibit actions coming from
	 * chunks that have lesser block interaction, item use or some barrier protection.
	 * <p>
	 * The idea is that an action is considered safe, if any player that can find a way to place a block or a stationary entity
	 * in the chunk that the action is coming from, can also do it under the current protection configured for the chunk that
	 * is to be affected. For example, actions coming from already placed blocks/entities would circumvent all protection
	 * within a claim that they are positioned in but a player must first have been able to place the block/entity in the claim,
	 * which ideally means that the player was at one point trusted. Obviously, this leaves a lot of room for user error.
	 *
	 * @param toWorld  the world to be affected by the action, not null
	 * @param toChunk  the chunk position to be affected by the action, not null
	 * @param fromWorld  the world that the action is coming from, not null
	 * @param fromChunk  the chunk position that the action is coming from, not null
	 * @param includeWilderness  whether to even consider protecting the wilderness
	 * @param affectsBlocks  whether the action is able to affect blocks
	 * @param affectsEntities  whether the action is able to affect entities
	 * @return true if the action should be protected against, otherwise false
	 */
	boolean onPosAffectedByAnotherPos(@Nonnull ServerLevel toWorld, @Nonnull ChunkPos toChunk, @Nonnull ServerLevel fromWorld, @Nonnull ChunkPos fromChunk, boolean includeWilderness, boolean affectsBlocks, boolean affectsEntities);

	/**
	 * Checks whether a landing projectile spawning an entity should be protected against.
	 * <p>
	 * Projectiles implementing {@link net.minecraft.world.entity.projectile.AbstractArrow},
	 * {@link net.minecraft.world.entity.projectile.AbstractHurtingProjectile} or
	 * {@link net.minecraft.world.entity.projectile.ThrowableProjectile} are usually already checking
	 * the protection by default, so make sure that using this is even necessary before you do.
	 * <p>
	 * As of writing this, this only ever protects against living entities being spawned.
	 *
	 * @param projectile  the projectile that spawns the entity, not null
	 * @param entity  the entity spawned by the projectile, not null
	 * @return true if the spawn should be prevented, otherwise false
	 */
	boolean onProjectileHitSpawnedEntity(@Nonnull Entity projectile, @Nonnull Entity entity);

	/**
	 * Gives an entity UUID a full pass to circumvent claim protection when affecting entities/blocks/items,
	 * except when the methods of this API are used, e.g. {@link #onEntityPlaceBlock(Entity, ServerLevel, BlockPos)}.
	 *
	 * @param entityId  the entity UUID, not null
	 */
	void giveFullPass(@Nonnull UUID entityId);

	/**
	 * Removes a pass given with {@link #giveFullPass(UUID)} from an entity UUID.
	 *
	 * @param entityId  the entity UUID, not null
	 */
	void removeFullPass(@Nonnull UUID entityId);

	/**
	 * Gets the player/claim config used for a specified claim state.
	 * <p>
	 * You can fetch claim states of chunks from the {@link xaero.pac.common.server.claims.api.IServerClaimsManagerAPI}.
	 *
	 * @param claim  the claim state to get the used config of, null for wilderness
	 * @return the player config used by the claim
	 */
	@Nonnull
	IPlayerConfigAPI getClaimConfig(@Nullable IPlayerChunkClaimAPI claim);

	/**
	 * Directly checks whether a specified entity has full access to a claim with the specified config.
	 * <p>
	 * You most likely don't have to use this method at all. The action-specific protection check methods already do it.
	 * This is meant for things that are not covered by the rest of the API.
	 *
	 * @param claimConfig  the claim config to check access for, not null
	 * @param accessor  the entity to check access for, not null
	 * @return true if accessor has full access to the claim, otherwise false
	 */
	boolean hasChunkAccess(@Nonnull IPlayerConfigAPI claimConfig, @Nonnull Entity accessor);

	/**
	 * Directly checks whether the entity with a specified UUID has full access to a claim with the specified config.
	 * <p>
	 * Please use {@link #hasChunkAccess(IPlayerConfigAPI, Entity)} when you have an actual
	 * entity reference.
	 * <p>
	 * You most likely don't have to use this method at all. The action-specific protection check methods already do it.
	 * This is meant for things that are not covered by the rest of the API.
	 *
	 * @param claimConfig  the claim config to check access for, not null
	 * @param accessorId  the entity UUID to check access for, not null
	 * @return true if accessor has full access to the claim, otherwise false
	 */
	boolean hasChunkAccess(@Nonnull IPlayerConfigAPI claimConfig, @Nonnull UUID accessorId);

	/**
	 * Checks whether a player/claim config option with multiple protection levels protects from a specified entity.
	 * <p>
	 * You most likely don't have to use this method at all. The action-specific protection check methods already
	 * use option values.
	 * This is meant for things that are not covered by the rest of the API.
	 *
	 * @param option  the protection option to check, not null
	 * @param claimConfig  the claim config to check the option value for, not null
	 * @param accessor  the entity to check against the current value of the option, not null
	 * @return true if the option is set to protect from the specified entity, false otherwise
	 */
	boolean checkProtectionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull Entity accessor);

	/**
	 * Checks whether a player/claim config option with multiple protection levels protects from the entity with a
	 * specified UUID.
	 * <p>
	 * Please use {@link #checkProtectionLeveledOption(IPlayerConfigOptionSpecAPI, IPlayerConfigAPI, Entity)}
	 * when you have an actual entity reference.
	 * <p>
	 * You most likely don't have to use this method at all. The action-specific protection check methods already
	 * use option values.
	 * This is meant for things that are not covered by the rest of the API.
	 *
	 * @param option  the protection option to check, not null
	 * @param claimConfig  the claim config to check the option value for, not null
	 * @param accessorId  the UUID of the entity to check against the current value of the option, not null
	 * @return true if the option is set to protect from the specified entity, false otherwise
	 */
	boolean checkProtectionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull UUID accessorId);

	/**
	 * Checks whether a player/claim config option with multiple exception levels includes a specified entity.
	 * <p>
	 * You most likely don't have to use this method at all. The action-specific protection check methods already
	 * use option values.
	 * This is meant for things that are not covered by the rest of the API.
	 *
	 * @param option  the exception option to check, not null
	 * @param claimConfig  the claim config to check the option value for, not null
	 * @param accessor  the entity to check against the current value of the option, not null
	 * @return true if the option is set to include the specified entity, false otherwise
	 */
	boolean checkExceptionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull Entity accessor);

	/**
	 * Checks whether a player/claim config option with multiple exception levels includes the entity with a
	 * specified UUID.
	 * <p>
	 * Please use {@link #checkExceptionLeveledOption(IPlayerConfigOptionSpecAPI, IPlayerConfigAPI, Entity)}
	 * when you have an actual entity reference.
	 * <p>
	 * You most likely don't have to use this method at all. The action-specific protection check methods already
	 * use option values.
	 * This is meant for things that are not covered by the rest of the API.
	 *
	 * @param option  the exception option to check, not null
	 * @param claimConfig  the claim config to check the option value for, not null
	 * @param accessorId  the UUID of the entity to check against the current value of the option, not null
	 * @return true if the option is set to include the specified entity, false otherwise
	 */
	boolean checkExceptionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull UUID accessorId);

}
