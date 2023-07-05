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

package xaero.pac.common.server.claims.protection;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.function.TriFunction;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.protection.api.IChunkProtectionAPI;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.world.ServerLevelHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChunkProtection
<
	CM extends IServerClaimsManager<?, ?, ?>
> implements IChunkProtectionAPI {

	public static final UUID CREATE_DEPLOYER_UUID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");
	public static final String TAG_PREFIX = "#";
	public static final String BREAK_PREFIX = "break$";
	public static final String HAND_PREFIX = "hand$";
	public static final String ANYTHING_PREFIX = "anything$";
	private final TriFunction<IPlayerConfig, Entity, Entity, IPlayerConfigOptionSpecAPI<Integer>> usedDroppedItemProtectionOptionGetter = this::getUsedDroppedItemProtectionOption;
	private final TriFunction<IPlayerConfig, Entity, Entity, IPlayerConfigOptionSpecAPI<Integer>> usedExperienceOrbProtectionOptionGetter = (c, e, a) -> PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_XP_PICKUP;

	private final Component MAIN_HAND = Component.translatable("gui.xaero_claims_protection_main_hand");
	private final Component OFF_HAND = Component.translatable("gui.xaero_claims_protection_off_hand");
	private final Component CANT_INTERACT_BLOCK = Component.translatable("gui.xaero_claims_protection_interact_block_any").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_BLOCK_MAIN = Component.translatable("gui.xaero_claims_protection_interact_block", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_MAIN = Component.translatable("gui.xaero_claims_protection_interact_block_try_empty", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_DISABLED = Component.translatable("gui.xaero_claims_protection_block_disabled").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_MAIN = Component.translatable("gui.xaero_claims_protection_use_item", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY = Component.translatable("gui.xaero_claims_protection_interact_entity_any").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_MAIN = Component.translatable("gui.xaero_claims_protection_interact_entity", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_MAIN = Component.translatable("gui.xaero_claims_protection_interact_entity_try_empty", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_DISABLED = Component.translatable("gui.xaero_claims_protection_entity_disabled").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_MAIN = Component.translatable("gui.xaero_claims_protection_interact_item_apply", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_THIS_CLOSE_MAIN = Component.translatable("gui.xaero_claims_protection_interact_item_apply_too_close", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ITEM_DISABLED_MAIN = Component.translatable("gui.xaero_claims_protection_item_disabled", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_INTERACT_BLOCK_OFF = Component.translatable("gui.xaero_claims_protection_interact_block", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_OFF = Component.translatable("gui.xaero_claims_protection_interact_block_try_empty", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_OFF = Component.translatable("gui.xaero_claims_protection_use_item", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_OFF = Component.translatable("gui.xaero_claims_protection_interact_item_apply", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_THIS_CLOSE_OFF = Component.translatable("gui.xaero_claims_protection_interact_item_apply_too_close", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ITEM_DISABLED_OFF = Component.translatable("gui.xaero_claims_protection_item_disabled", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_OFF = Component.translatable("gui.xaero_claims_protection_interact_entity", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_OFF = Component.translatable("gui.xaero_claims_protection_interact_entity_try_empty", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_CHORUS = Component.translatable("gui.xaero_claims_protection_chorus").withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_USE_SUPER_GLUE = Component.translatable("gui.xaero_claims_protection_create_cant_use_glue").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_REMOVE_SUPER_GLUE = Component.translatable("gui.xaero_claims_protection_create_cant_remove_glue").withStyle(s -> s.withColor(ChatFormatting.RED));

	private final ChunkProtectionEntityHelper entityHelper;
	private IServerData<CM,?> serverData;
	private final CM claimsManager;
	private final IPlayerPartySystemManager playerPartySystemManager;
	private final ChunkProtectionExceptionSet<EntityType<?>> friendlyEntityList;
	private final ChunkProtectionExceptionSet<EntityType<?>> hostileEntityList;
	private final ChunkProtectionExceptionSet<Block> forcedInteractionExceptionBlocks;
	private final ChunkProtectionExceptionSet<Block> forcedBreakExceptionBlocks;
	private final ChunkProtectionExceptionSet<Block> requiresEmptyHandBlocks;
	private final ChunkProtectionExceptionSet<Block> forcedAllowAnyItemBlocks;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedInteractionExceptionEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedKillExceptionEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> requiresEmptyHandEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedAllowAnyItemEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedEntityClaimBarrierList;
	private final ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGrief;
	private final ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGriefEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGriefDroppedItems;
	private final ChunkProtectionExceptionSet<EntityType<?>> nonBlockGriefingMobs;
	private final ChunkProtectionExceptionSet<EntityType<?>> entityGriefingMobs;
	private final ChunkProtectionExceptionSet<EntityType<?>> droppedItemGriefingMobs;
	private final Set<String> staticFakePlayerUsernames;
	private final Set<UUID> staticFakePlayerIds;
	private final Set<Class<?>> staticFakePlayerClassExceptions;
	private final ChunkProtectionExceptionSet<Item> additionalBannedItems;
	private final ChunkProtectionExceptionSet<Item> itemUseProtectionExceptions;
	private final ChunkProtectionExceptionSet<Item> completelyDisabledItems;
	private final ChunkProtectionExceptionSet<Block> completelyDisabledBlocks;
	private final ChunkProtectionExceptionSet<EntityType<?>> completelyDisabledEntities;
	private final Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups;
	private final Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> blockAccessEntityGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityAccessEntityGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> droppedItemAccessEntityGroups;

	private boolean ignoreChunkEnter = false;
	private final Map<Entity, Set<ChunkPos>> cantPickupItemsInTickCache;
	private final Map<Entity, Set<ChunkPos>> cantPickupXPInTickCache;
	private final Set<UUID> fullPasses;
	private boolean fullPassesPaused;
	
	private ChunkProtection(CM claimsManager, IPlayerPartySystemManager playerPartySystemManager, ChunkProtectionEntityHelper entityHelper,
							ChunkProtectionExceptionSet<EntityType<?>> friendlyEntityList,
							ChunkProtectionExceptionSet<EntityType<?>> hostileEntityList,
							ChunkProtectionExceptionSet<Block> forcedInteractionExceptionBlocks,
							ChunkProtectionExceptionSet<Block> forcedBreakExceptionBlocks,
							ChunkProtectionExceptionSet<Block> requiresEmptyHandBlocks,
							ChunkProtectionExceptionSet<Block> forcedAllowAnyItemBlocks, ChunkProtectionExceptionSet<Block> completelyDisabledBlocks,
							ChunkProtectionExceptionSet<EntityType<?>> forcedInteractionExceptionEntities,
							ChunkProtectionExceptionSet<EntityType<?>> forcedKillExceptionEntities,
							ChunkProtectionExceptionSet<EntityType<?>> requiresEmptyHandEntities, ChunkProtectionExceptionSet<EntityType<?>> forcedAllowAnyItemEntities, ChunkProtectionExceptionSet<EntityType<?>> forcedEntityClaimBarrierList,
							ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGrief,
							ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGriefEntities, ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGriefDroppedItems, ChunkProtectionExceptionSet<EntityType<?>> nonBlockGriefingMobs, ChunkProtectionExceptionSet<EntityType<?>> entityGriefingMobs, ChunkProtectionExceptionSet<EntityType<?>> droppedItemGriefingMobs, Set<String> staticFakePlayerUsernames, Set<UUID> staticFakePlayerIds, Set<Class<?>> staticFakePlayerClassExceptions, ChunkProtectionExceptionSet<Item> additionalBannedItems,
							ChunkProtectionExceptionSet<Item> completelyBannedItems,
							ChunkProtectionExceptionSet<Item> itemUseProtectionExceptions, ChunkProtectionExceptionSet<EntityType<?>> completelyDisabledEntities, Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups, Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> blockAccessEntityGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityAccessEntityGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> droppedItemAccessEntityGroups, Map<Entity, Set<ChunkPos>> cantPickItemsCache, Map<Entity, Set<ChunkPos>> cantPickupXPInTickCache, Set<UUID> fullPasses) {
		this.claimsManager = claimsManager;
		this.playerPartySystemManager = playerPartySystemManager;
		this.entityHelper = entityHelper;
		this.friendlyEntityList = friendlyEntityList;
		this.hostileEntityList = hostileEntityList;
		this.forcedInteractionExceptionBlocks = forcedInteractionExceptionBlocks;
		this.forcedBreakExceptionBlocks = forcedBreakExceptionBlocks;
		this.requiresEmptyHandBlocks = requiresEmptyHandBlocks;
		this.forcedAllowAnyItemBlocks = forcedAllowAnyItemBlocks;
		this.completelyDisabledBlocks = completelyDisabledBlocks;
		this.forcedInteractionExceptionEntities = forcedInteractionExceptionEntities;
		this.forcedKillExceptionEntities = forcedKillExceptionEntities;
		this.requiresEmptyHandEntities = requiresEmptyHandEntities;
		this.forcedAllowAnyItemEntities = forcedAllowAnyItemEntities;
		this.forcedEntityClaimBarrierList = forcedEntityClaimBarrierList;
		this.entitiesAllowedToGrief = entitiesAllowedToGrief;
		this.entitiesAllowedToGriefEntities = entitiesAllowedToGriefEntities;
		this.entitiesAllowedToGriefDroppedItems = entitiesAllowedToGriefDroppedItems;
		this.nonBlockGriefingMobs = nonBlockGriefingMobs;
		this.entityGriefingMobs = entityGriefingMobs;
		this.droppedItemGriefingMobs = droppedItemGriefingMobs;
		this.staticFakePlayerUsernames = staticFakePlayerUsernames;
		this.staticFakePlayerIds = staticFakePlayerIds;
		this.staticFakePlayerClassExceptions = staticFakePlayerClassExceptions;
		this.additionalBannedItems = additionalBannedItems;
		this.completelyDisabledItems = completelyBannedItems;
		this.itemUseProtectionExceptions = itemUseProtectionExceptions;
		this.completelyDisabledEntities = completelyDisabledEntities;
		this.blockExceptionGroups = blockExceptionGroups;
		this.entityExceptionGroups = entityExceptionGroups;
		this.itemExceptionGroups = itemExceptionGroups;
		this.entityBarrierGroups = entityBarrierGroups;
		this.blockAccessEntityGroups = blockAccessEntityGroups;
		this.entityAccessEntityGroups = entityAccessEntityGroups;
		this.droppedItemAccessEntityGroups = droppedItemAccessEntityGroups;
		this.cantPickupItemsInTickCache = cantPickItemsCache;
		this.cantPickupXPInTickCache = cantPickupXPInTickCache;
		this.fullPasses = fullPasses;
	}

	public void setServerData(IServerData<CM, ?> serverData) {
		this.serverData = serverData;
	}

	public void giveFullPass(@Nonnull UUID entityId){
		fullPasses.add(entityId);
	}

	public void removeFullPass(@Nonnull UUID entityId){
		fullPasses.remove(entityId);
	}

	private boolean hasActiveFullPass(Entity entity){
		if(fullPassesPaused)
			return false;
		if(entity instanceof Player){
			if(
				CREATE_DEPLOYER_UUID.equals(entity.getUUID())
				&& !isStaticFakePlayerExceptionClass(entity)
			)
				return true;
		}
		return fullPasses.contains(entity.getUUID());
	}

	private boolean isStaticFakePlayerExceptionClass(Entity entity){
		for (Class<?> fakePlayerExceptionClass : staticFakePlayerClassExceptions)
			if (fakePlayerExceptionClass.isAssignableFrom(entity.getClass()))
				return true;
		return false;
	}

	private InteractionTargetResult entityAccessCheck(IPlayerConfigManager playerConfigs, IPlayerConfig claimConfig, Entity e, Entity from, Entity accessor, UUID accessorId, boolean attack, boolean emptyHand) {
		return entityAccessCheck(playerConfigs, claimConfig, e, from, accessor, accessorId, attack, emptyHand, false);
	}

	private InteractionTargetResult entityAccessCheck(IPlayerConfigManager playerConfigs, IPlayerConfig claimConfig, Entity e, Entity from, Entity accessor, UUID accessorId, boolean attack, boolean emptyHand, boolean checkingInverted) {
		if(e instanceof Player) {
			boolean chunkProtected = claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS);
			InteractionTargetResult result = InteractionTargetResult.ALLOW;
			if (chunkProtected) {
				Entity usedOptionBase = claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_REDIRECT) ? accessor : from;
				if (usedOptionBase == null) {
					if (hasAnEnabledOption(claimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER))
						return InteractionTargetResult.PROTECT;
				} else {
					IPlayerConfigOptionSpecAPI<Boolean> option =
							usedOptionBase instanceof Player ?
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS :
							usedOptionBase instanceof LivingEntity ?
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS :
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER;
					if (claimConfig.getEffective(option))
						return InteractionTargetResult.PROTECT;
				}
				result = InteractionTargetResult.PASS;
			}
			if(accessor instanceof Player && !checkingInverted) {
				//gotta check whether the attacked player can attack back the same way (melee/ranged)
				return entityAccessCheck(playerConfigs, getClaimConfig(playerConfigs, claimsManager.get(accessor.getLevel().dimension().location(), accessor.chunkPosition())), accessor, accessor == from ? e : from, e, null, attack, emptyHand, true);
			}
			return result;
		}
		if(hasChunkAccess(claimConfig, accessor, accessorId))
			return InteractionTargetResult.ALLOW;
		boolean isProtectable = isProtectable(e);
		if(isProtectable){
			if(accessor instanceof Raider raider && raider.canJoinRaid() && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_RAIDS))//based on the accessor on purpose;
				return InteractionTargetResult.PROTECT;
		} else if(attack || emptyHand)
			return InteractionTargetResult.ALLOW;
		IPlayerConfigOptionSpecAPI<Integer> option = getUsedEntityProtectionOption(claimConfig, from, accessor);
		boolean optionProtects = checkProtectionLeveledOption(option, claimConfig, accessor, accessorId);
		if(!optionProtects && (attack || emptyHand || !checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_USE, claimConfig, accessor, accessorId)))
			return InteractionTargetResult.ALLOW;
		EntityType<?> entityType = e.getType();
		if(attack && forcedKillExceptionEntities.contains(entityType))
			return InteractionTargetResult.ALLOW;
		if(!attack && forcedAllowAnyItemEntities.contains(entityType))
			return InteractionTargetResult.ALLOW;
		int exceptionAccessLevel = getExceptionAccessLevel(claimConfig, accessor, accessorId);
		boolean groupsAllowPass = false;
		for (ChunkProtectionExceptionGroup<EntityType<?>> group : entityExceptionGroups.values()) {
			if ((group.getType() == ChunkProtectionExceptionType.BREAK) != attack)
				continue;
			if(!emptyHand && group.getType() == ChunkProtectionExceptionType.EMPTY_HAND_INTERACTION)
				continue;
			if(!isProtectable && group.getType() != ChunkProtectionExceptionType.ANY_ITEM_INTERACTION)//only ALLOW groups matter if the entity isn't protectable
				continue;
			if (exceptionAccessLevel <= claimConfig.getEffective(group.getPlayerConfigOption()) && group.contains(entityType)) {
				if (attack || emptyHand || group.getType() == ChunkProtectionExceptionType.ANY_ITEM_INTERACTION)
					return InteractionTargetResult.ALLOW;
				groupsAllowPass = true;
			}
		}
		if(groupsAllowPass || !optionProtects || !isProtectable)
			return InteractionTargetResult.PASS;
		if (accessor instanceof Player && entityHelper.isTamed(e, (Player) accessor))
			return InteractionTargetResult.PASS;
		if(!attack && forcedInteractionExceptionEntities.contains(entityType) && (emptyHand || !requiresEmptyHandEntities.contains(entityType)))
			return InteractionTargetResult.PASS;
		return InteractionTargetResult.PROTECT;
	}

	private IPlayerConfigOptionSpecAPI<Integer> getUsedEntityProtectionOption(IPlayerConfig claimConfig, Entity entity, Entity accessor){
		Entity usedOptionBase = !(entity instanceof Player) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_REDIRECT) ? accessor : entity;
		if(usedOptionBase == null)
			return getToughestProtectionLevelOption(claimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER);
		return usedOptionBase instanceof Player ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS :
				usedOptionBase instanceof LivingEntity ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS :
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER;
	}

	private IPlayerConfigOptionSpecAPI<Integer> getUsedBlockProtectionOption(IPlayerConfig claimConfig, Entity entity, Entity accessor){
		Entity usedOptionBase = !(entity instanceof Player) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_REDIRECT) ? accessor : entity;
		if(usedOptionBase == null)
			return getToughestProtectionLevelOption(claimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER);
		return usedOptionBase instanceof Player ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS :
				usedOptionBase instanceof LivingEntity ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS :
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER;
	}

	private boolean checkProtectionLeveledOption(IPlayerConfigOptionSpecAPI<Integer> option, IPlayerConfig claimConfig, Entity accessor, UUID accessorId){
		//nobody -> everyone -> not party -> not allies
		int optionValue = claimConfig.getEffective(option);
		if(optionValue <= 0)
			return false;
		if(optionValue == 1)
			return true;
		int exceptionLevel = getExceptionAccessLevel(claimConfig, accessor, accessorId);
		return exceptionLevel >= optionValue;
	}

	private boolean checkExceptionLeveledOption(IPlayerConfigOptionSpecAPI<Integer> option, IPlayerConfig claimConfig, Entity accessor, UUID accessorId){
		//nobody -> party -> allies -> everyone
		int optionValue = claimConfig.getEffective(option);
		if(optionValue >= 3)
			return true;
		if(optionValue == 0)
			return false;
		int exceptionLevel = getExceptionAccessLevel(claimConfig, accessor, accessorId);
		return exceptionLevel <= optionValue;
	}

	@Override
	public boolean checkProtectionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull Entity accessor){
		return checkProtectionLeveledOption(option, (IPlayerConfig) claimConfig, accessor, null);
	}

	@Override
	public boolean checkExceptionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull Entity accessor){
		return checkExceptionLeveledOption(option, (IPlayerConfig) claimConfig, accessor, null);
	}

	@Override
	public boolean checkProtectionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull UUID accessorId){
		return checkProtectionLeveledOption(option, (IPlayerConfig) claimConfig, null, accessorId);
	}

	@Override
	public boolean checkExceptionLeveledOption(@Nonnull IPlayerConfigOptionSpecAPI<Integer> option, @Nonnull IPlayerConfigAPI claimConfig, @Nonnull UUID accessorId){
		return checkExceptionLeveledOption(option, (IPlayerConfig) claimConfig, null, accessorId);
	}
	
	private boolean isIncludedByProtectedEntityLists(Entity e) {
		if(entityHelper.isHostile(e))
			return ServerConfig.CONFIG.hostileChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ALL_BUT && !hostileEntityList.contains(e.getType()) ||
					ServerConfig.CONFIG.hostileChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ONLY && hostileEntityList.contains(e.getType());
		return ServerConfig.CONFIG.friendlyChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ALL_BUT && !friendlyEntityList.contains(e.getType()) ||
				ServerConfig.CONFIG.friendlyChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ONLY && friendlyEntityList.contains(e.getType());
	}
	
	private boolean isProtectable(Entity e) {
		return !(e instanceof Player) && isIncludedByProtectedEntityLists(e);
	}

	private boolean canGrief(Entity e, IPlayerConfig config, Entity accessor, UUID accessorId, boolean blocks, boolean entities, boolean items){
		if(e == null)
			return false;
		IPlayerConfigOptionSpecAPI<Integer> option;
		if(blocks && !isAllowedToGrief(e, accessor, accessorId, config, entitiesAllowedToGrief, blockAccessEntityGroups)) {
			option = getUsedBlockProtectionOption(config, e, accessor);
			if(checkProtectionLeveledOption(option, config, accessor, accessorId))
				return false;
		}
		if(entities && !isAllowedToGrief(e, accessor, accessorId, config, entitiesAllowedToGriefEntities, entityAccessEntityGroups)) {
			option = getUsedEntityProtectionOption(config, e, accessor);
			if(checkProtectionLeveledOption(option, config, accessor, accessorId))
				return false;
		}
		if(items && !isAllowedToGrief(e, accessor, accessorId, config, entitiesAllowedToGriefDroppedItems, droppedItemAccessEntityGroups)) {
			option = getUsedDroppedItemProtectionOption(config, e, accessor);
			if(checkProtectionLeveledOption(option, config, accessor, accessorId))
				return false;
		}
		return true;
	}

	private boolean isAllowedToGrief(Entity e, Entity accessor, UUID accessorId, IPlayerConfig config, ChunkProtectionExceptionSet<EntityType<?>> forcedSet, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> groups){
		if(e == null)
			return false;
		EntityType<?> entityType = e.getType();
		if(forcedSet.contains(entityType))
			return true;
		for(ChunkProtectionExceptionGroup<EntityType<?>> group : groups.values()){
			if(group.contains(entityType) && checkExceptionLeveledOption(group.getPlayerConfigOption(), config, accessor, accessorId))
				return true;
		}
		return false;
	}
	
	public boolean hasChunkAccess(IPlayerConfigAPI claimConfig, Entity accessor, UUID accessorId) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return true;
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return true;
		if(accessor != null) {
			if(accessorId == null)
				accessorId = accessor.getUUID();
			boolean isAServerPlayer = accessor instanceof ServerPlayer;
			if (isAServerPlayer && ServerPlayerDataAPI.from((ServerPlayer) accessor).isClaimsNonallyMode())
				return false;
			if (accessorId.equals(claimConfig.getPlayerId()))
				return true;
			if (isAServerPlayer){
				ServerPlayerDataAPI playerData = ServerPlayerDataAPI.from((ServerPlayer) accessor);
				claimsManager.getPermissionHandler().ensureAdminModeStatusPermission((ServerPlayer) accessor, playerData);
				if (
						playerData.isClaimsAdminMode() ||
						playerData.isClaimsServerMode() &&
								claimsManager.getPermissionHandler().playerHasServerClaimPermission((ServerPlayer) accessor) &&
								claimConfig.getType() == PlayerConfigType.SERVER
				)
					return true;
			} else
				return false;
		} else if(accessorId == null)
			return false;
		if (claimConfig.getPlayerId() == null)
			return false;
		if (claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES))
			return false;
		if(!playerPartySystemManager.isInAParty(claimConfig.getPlayerId()))
			return false;
		return !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) && playerPartySystemManager.areInSameParty(claimConfig.getPlayerId(), accessorId) || !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES) && playerPartySystemManager.isPlayerAllying(claimConfig.getPlayerId(), accessorId);
	}

	@Override
	public boolean hasChunkAccess(@Nonnull IPlayerConfigAPI claimConfig, @Nonnull UUID accessorId) {
		return hasChunkAccess(claimConfig, null, accessorId);
	}

	@Override
	public boolean hasChunkAccess(@Nonnull IPlayerConfigAPI claimConfig, @Nonnull Entity accessor) {
		return hasChunkAccess(claimConfig, accessor, accessor.getUUID());
	}

	private int getExceptionAccessLevel(IPlayerConfig claimConfig, Entity accessor, UUID accessorId){//lower is usually higher access
		if(accessor instanceof ServerPlayer player && ServerPlayerData.from(player).isClaimsNonallyMode())
			return 3;
		if(accessorId == null) {
			if(accessor == null)
				return 3;
			accessorId = accessor.getUUID();
		}
		if(accessorId.equals(claimConfig.getPlayerId()))
			return 0;//owner
		if(!playerPartySystemManager.isInAParty(claimConfig.getPlayerId()))
			return 3;//everyone
		if(playerPartySystemManager.areInSameParty(claimConfig.getPlayerId(), accessorId))
			return 1;//party
		if(playerPartySystemManager.isPlayerAllying(claimConfig.getPlayerId(), accessorId))
			return 2;//allies
		return 3;//everyone
	}

	private BlockPos getFakePlayerPos(Player player){
		BlockPos playerPos = player.blockPosition();
		if(!BlockPos.ZERO.equals(playerPos))
			return playerPos;
		Vec3 position1 = player.getPosition(1);
		if(!Vec3.ZERO.equals(position1))
			return new BlockPos(position1);
		double specificallyX = player.getX();
		double specificallyY = player.getY();
		double specificallyZ = player.getZ();
		if(specificallyX == 0 && specificallyY == 0 && specificallyZ == 0)
			return new BlockPos(player.getPosition(0));
		return new BlockPos(specificallyX, specificallyY, specificallyZ);
	}

	private boolean isAllowedStaticFakePlayerAction(IServerData<CM, ?> serverData, Player player, BlockPos targetPos, BlockPos targetPos2){
		if(player == null || !staticFakePlayerIds.contains(player.getUUID()) && !staticFakePlayerUsernames.contains(player.getGameProfile().getName()))
			return false;
		if(isStaticFakePlayerExceptionClass(player))
			return false;
		BlockPos playerPos = getFakePlayerPos(player);
		//gotta check all sides, player rotation doesn't give us anything in the case of Integrated Tunnels
		//works with either fake player or the target being positioned next to or at the origin block
		BlockPos checkedPos = targetPos;
		BlockPos.MutableBlockPos possibleFakePlayerOrigin = new BlockPos.MutableBlockPos();
		boolean shouldCheckTargetPos2 = targetPos2 != null && ((targetPos.getX() >> 4) != (targetPos2.getX() >> 4) || (targetPos.getZ() >> 4) != (targetPos2.getZ() >> 4));
		while(true) {
			int minX = 0;
			int maxX = 0;
			int minZ = 0;
			int maxZ = 0;
			int localX = checkedPos.getX() & 15;
			int localZ = checkedPos.getZ() & 15;
			if (localX == 0)
				minX = -1;
			else if (localX == 15)
				maxX = 1;
			if (localZ == 0)
				minZ = -1;
			else if (localZ == 15)
				maxZ = 1;
			if (checkedPos == playerPos || minX != maxX || minZ != maxZ) {//on chunk edge or any fake player pos
				for (int i = minX; i <= maxX; i++)
					for (int j = minZ; j <= maxZ; j++) {
						if (i == 0 && j == 0)
							possibleFakePlayerOrigin.set(checkedPos);
						else
							possibleFakePlayerOrigin.set(checkedPos.getX() + i, checkedPos.getY(), checkedPos.getZ() + j);
						if (possibleFakePlayerOrigin.equals(targetPos))
							continue;
						if (checkedPos == playerPos) {//actually the second check
							int diffX = possibleFakePlayerOrigin.getX() - targetPos.getX();
							int diffZ = possibleFakePlayerOrigin.getZ() - targetPos.getZ();
							if (diffX * diffX <= 1 && diffZ * diffZ <= 1)//already checked
								continue;
						}
						if (hitsAnotherClaim(serverData, player.getLevel(), possibleFakePlayerOrigin, targetPos, null, true))
							return false;
						if (shouldCheckTargetPos2 && hitsAnotherClaim(serverData, player.getLevel(), possibleFakePlayerOrigin, targetPos2, null, true))
							return false;
					}
			}
			if(checkedPos.equals(playerPos))//can be true on the targetPos check too!
				break;
			checkedPos = playerPos;
		}
		return true;
	}

	private boolean isAllowedStaticFakePlayerAction(IServerData<CM, ?> serverData, Player player, BlockPos targetPos){
		return isAllowedStaticFakePlayerAction(serverData, player, targetPos, null);
	}

	public boolean onEntityDestroyBlock(IServerData<CM, ?> serverData, Entity entity, ServerLevel world, BlockPos pos, boolean messages) {
		return onBlockInteraction(serverData, entity, InteractionHand.MAIN_HAND, null, world, pos, Direction.UP, true, messages);
	}
	
	public IPlayerConfig getClaimConfig(IPlayerConfigManager playerConfigs, IPlayerChunkClaim claim) {
		IPlayerConfig mainConfig = playerConfigs.getLoadedConfig(claim == null ? null : claim.getPlayerId());
		if(claim == null)
			return mainConfig;
		return mainConfig.getEffectiveSubConfig(claim.getSubConfigIndex());
	}

	@Nonnull
	public IPlayerConfigAPI getClaimConfig(@Nullable IPlayerChunkClaimAPI claim){
		return getClaimConfig(serverData.getPlayerConfigs(), (IPlayerChunkClaim) claim);
	}
	
	private InteractionTargetResult blockAccessCheck(Block block, IPlayerConfig config, Entity entity, Entity accessor, UUID accessorId, boolean emptyHand, boolean breaking) {
		boolean chunkAccess = hasChunkAccess(config, accessor, accessorId);
		if(chunkAccess)
			return InteractionTargetResult.ALLOW;
		else {
			boolean optionProtects = checkProtectionLeveledOption(getUsedBlockProtectionOption(config, entity, accessor), config, accessor, accessorId);
			if(optionProtects)
				optionProtects = entity instanceof Player || !isAllowedToGrief(entity, accessor, accessorId, config, entitiesAllowedToGrief, blockAccessEntityGroups);
			if(!optionProtects && (breaking || emptyHand || !checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_USE, config, accessor, accessorId)))
				return InteractionTargetResult.ALLOW;
			if(breaking && forcedBreakExceptionBlocks.contains(block))
				return InteractionTargetResult.ALLOW;
			if(!breaking && forcedAllowAnyItemBlocks.contains(block))
				return InteractionTargetResult.ALLOW;
			int exceptionAccessLevel = getExceptionAccessLevel(config, accessor, accessorId);
			boolean groupsAllowPass = false;
			for (ChunkProtectionExceptionGroup<Block> group : blockExceptionGroups.values()) {
				if ((group.getType() == ChunkProtectionExceptionType.BREAK) != breaking)
					continue;
				if(!emptyHand && group.getType() == ChunkProtectionExceptionType.EMPTY_HAND_INTERACTION)
					continue;
				if (exceptionAccessLevel <= config.getEffective(group.getPlayerConfigOption()) && group.contains(block)) {
					if(breaking || emptyHand || group.getType() == ChunkProtectionExceptionType.ANY_ITEM_INTERACTION)
						return InteractionTargetResult.ALLOW;
					groupsAllowPass = true;
				}
			}
			if(!optionProtects)
				return InteractionTargetResult.PASS;//after other checks so that this doesn't override potential ALLOW
			if(groupsAllowPass)
				return InteractionTargetResult.PASS;
			if(!breaking && forcedInteractionExceptionBlocks.contains(block) && (emptyHand || !requiresEmptyHandBlocks.contains(block)))
				return InteractionTargetResult.PASS;
			return InteractionTargetResult.PROTECT;
		}
	}
	
	private InteractionTargetResult onBlockAccess(IServerData<CM, ?> serverData, Block block, IPlayerConfig config, Entity entity, Entity accessor, UUID accessorId, InteractionHand hand, boolean emptyHand, boolean leftClick, Component message, boolean messages) {
		InteractionTargetResult result = blockAccessCheck(block, config, entity, accessor, accessorId, emptyHand, leftClick);
		if(result == InteractionTargetResult.PROTECT) {
			if(messages && entity instanceof ServerPlayer player) {
				player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, hand == null ? CANT_INTERACT_BLOCK : hand == InteractionHand.MAIN_HAND ? CANT_INTERACT_BLOCK_MAIN : CANT_INTERACT_BLOCK_OFF));
				if (message != null)
					player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, message));
			}
		}
		return result;
	}

	public boolean onBlockInteraction(IServerData<CM, ?> serverData, Entity entity, InteractionHand hand, ItemStack heldItem, ServerLevel world, BlockPos pos, Direction direction, boolean breaking, boolean messages) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		//entity can be null!
		BlockState blockState = world.getBlockState(pos);
		if(completelyDisabledBlocks.contains(blockState.getBlock())){
			if(messages && entity instanceof ServerPlayer)
				entity.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor((ServerPlayer) entity, BLOCK_DISABLED));
			return true;
		}
		if(entity != null && hasActiveFullPass(entity))//uses custom protection
			return false;
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(world, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		if(heldItem == null)
			heldItem = hand != null && entity instanceof LivingEntity livingEntity ? livingEntity.getItemInHand(hand) : ItemStack.EMPTY;
		boolean emptyHand = heldItem.isEmpty();
		boolean itemMatters = !emptyHand && !breaking;
		Component message = !itemMatters ? null : hand == InteractionHand.MAIN_HAND ? BLOCK_TRY_EMPTY_MAIN : BLOCK_TRY_EMPTY_OFF;
		boolean itemUseAtTargetAllowed = false;
		boolean isPlayer = entity instanceof Player;
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		if(!isPlayer || !isAllowedStaticFakePlayerAction(serverData, (Player)entity, pos)){
			IPlayerConfig config = getClaimConfig(playerConfigs, claim);
			InteractionTargetResult targetResult = onBlockAccess(serverData, blockState.getBlock(), config, entity, accessor, accessorId, hand, emptyHand, breaking, message, messages);
			if(targetResult == InteractionTargetResult.PROTECT)
				return true;
			else if(isPlayer && !emptyHand && targetResult == InteractionTargetResult.ALLOW && !((Player)entity).isSecondaryUseActive())
				itemUseAtTargetAllowed = true;
		}
		if(!itemMatters)
			return false;
		boolean itemUseAtOffsetAllowed = false;
		if(itemUseAtTargetAllowed){
			//testing if the pos in the block hit direction also allows any item interaction with the same block
			if(isOnChunkEdge(pos)) {
				BlockPos offsetPos = pos.offset(direction.getNormal());
				ChunkPos offsetChunkPos = new ChunkPos(offsetPos);
				if(!chunkPos.equals(offsetChunkPos)) {
					IPlayerChunkClaim offsetClaim = claimsManager.get(world.dimension().location(), offsetChunkPos);
					if(offsetClaim != null /*not worried about wilderness*/ && claim != offsetClaim) {
						UUID claimOwnerId = claim == null ? null : claim.getPlayerId();
						UUID offsetClaimOwnerId = offsetClaim.getPlayerId();
						if(Objects.equals(claimOwnerId, offsetClaimOwnerId)) {//should only work between claims of the same owner
							IPlayerConfig config = getClaimConfig(playerConfigs, offsetClaim);
							InteractionTargetResult offsetResult = onBlockAccess(serverData, blockState.getBlock()/*same block on purpose*/, config, entity, accessor, accessorId, hand, emptyHand, breaking, message, messages);
							itemUseAtOffsetAllowed = offsetResult == InteractionTargetResult.ALLOW;
						}
					} else
						itemUseAtOffsetAllowed = true;
				} else
					itemUseAtOffsetAllowed = true;
			} else
				itemUseAtOffsetAllowed = true;
		}
		return onUseItemAt(serverData, entity, world, pos, direction, heldItem, hand, itemUseAtTargetAllowed, itemUseAtOffsetAllowed, messages);
	}

	@Override
	public boolean onBlockInteraction(@Nullable Entity entity, @Nullable InteractionHand hand, @Nullable ItemStack heldItem, @Nonnull ServerLevel world, @Nonnull BlockPos pos, @Nonnull Direction direction, boolean breaking, boolean messages) {
		try {
			fullPassesPaused = true;
			return onBlockInteraction(serverData, entity, hand, heldItem, world, pos, direction, breaking, messages);
		} finally {
			fullPassesPaused = false;
		}
	}

	public boolean onBlockSpecialInteraction(IServerData<CM, ?> serverData, Player player, ServerLevel world, BlockPos pos) {//not left or right click, e.g. scrolling with Create wrench
		return onBlockInteraction(serverData, player, null, null, world, pos, Direction.UP, false, true);
	}

	public boolean onEntityPlaceBlock(IServerData<CM, ?> serverData, Entity entity, ServerLevel world, BlockPos pos, IPlayerConfigOptionSpecAPI<Integer> option) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		//entity can be null!
		if(entity != null && hasActiveFullPass(entity))//uses custom protection
			return false;
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(world, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		if(entity instanceof Player && isAllowedStaticFakePlayerAction(serverData, (Player) entity, pos))
			return false;
		return (option == null || checkProtectionLeveledOption(option, config, accessor, accessorId)) && (entity instanceof Player || !canGrief(entity, config, accessor, accessorId, true, false, false))
				&& blockAccessCheck(Blocks.AIR, config, entity, accessor, accessorId, false, false) == InteractionTargetResult.PROTECT;
	}

	@Override
	public boolean onEntityPlaceBlock(@Nullable Entity entity, @Nonnull ServerLevel world, @Nonnull BlockPos pos){
		try {
			fullPassesPaused = true;
			return onEntityPlaceBlock(serverData, entity, world, pos, null);
		} finally {
			fullPassesPaused = false;
		}
	}

	public boolean onFrostWalk(IServerData<CM, ?> serverData, LivingEntity living, ServerLevel world, BlockPos pos) {
		return onEntityPlaceBlock(serverData, living, world, pos, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_FROST_WALKING);
	}

	private boolean isItemUseRestricted(ItemStack itemStack){
		Item item = itemStack.getItem();
		if(itemUseProtectionExceptions.contains(item))
			return false;
		return item.getFoodProperties() == null &&
				!(item instanceof PotionItem) &&
				!(item instanceof ProjectileWeaponItem) &&
				!(item instanceof TridentItem) &&
				!(item instanceof ShieldItem) &&
				!(item instanceof SwordItem) &&
				!(item instanceof AxeItem) &&
				!(item instanceof HoeItem) &&
				!(item instanceof PickaxeItem) &&
				!(item instanceof BoatItem) &&
				!itemStack.is(ItemTags.BOATS) &&
				!(item instanceof MilkBucketItem) &&
				!(item instanceof ArmorItem)
				||
				additionalBannedItems.contains(item);
	}
	
	public boolean onItemRightClick(IServerData<CM, ?> serverData, InteractionHand hand, ItemStack itemStack, BlockPos pos, Player player, boolean messages) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		boolean shouldProtect = false;
		Item item = itemStack.getItem();
		if(completelyDisabledItems.contains(item)) {
			if(messages && player instanceof ServerPlayer serverPlayer)
				player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(serverPlayer, hand == InteractionHand.MAIN_HAND ? ITEM_DISABLED_MAIN : ITEM_DISABLED_OFF));
			return true;
		}
		if(hasActiveFullPass(player))
			return false;
		if(isItemUseRestricted(itemStack) && !(item instanceof BucketItem) && !(item instanceof SolidBucketItem)) {
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
			ChunkPos chunkPos = new ChunkPos(pos);
			boolean shouldCheckGroups = false;
			for (ChunkProtectionExceptionGroup<Item> group : itemExceptionGroups.values()) {
				if (group.contains(itemStack.getItem())){
					shouldCheckGroups = true;
					break;
				}
			}
			for(int i = -1; i < 2; i++)
				j_loop: for(int j = -1; j < 2; j++) {//checking neighboring chunks too because of items that affect a high range
					ChunkPos offsetChunkPos = new ChunkPos(chunkPos.x + i, chunkPos.z + j);
					IPlayerChunkClaim claim = claimsManager.get(player.getLevel().dimension().location(), offsetChunkPos);
					boolean isCurrentChunk = i == 0 && j == 0;
					if (isCurrentChunk || claim != null){//wilderness neighbors don't have to be protected this much
						IPlayerConfig config = getClaimConfig(playerConfigs, claim);
						if(checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_USE, config, player, null) &&
								(isCurrentChunk || config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE))
								&& !hasChunkAccess(config, player, null) && !isAllowedStaticFakePlayerAction(serverData, player, offsetChunkPos.getMiddleBlockPosition(0))) {
							if(shouldCheckGroups) {
								int exceptionAccessLevel = getExceptionAccessLevel(config, player, null);
								for (ChunkProtectionExceptionGroup<Item> group : itemExceptionGroups.values()) {
									if (exceptionAccessLevel <= config.getEffective(group.getPlayerConfigOption()) && group.contains(itemStack.getItem()))
										continue j_loop;
								}
							}
							shouldProtect = true;
							break;
						}
					}
				}
		}
		if(messages && shouldProtect && player instanceof ServerPlayer)
			player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor((ServerPlayer) player, hand == InteractionHand.MAIN_HAND ? USE_ITEM_MAIN : USE_ITEM_OFF));
		return shouldProtect;
	}

	public boolean onMobGrief(IServerData<CM, ?> serverData, Entity entity){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		boolean blocks = !(entity instanceof Evoker || nonBlockGriefingMobs.contains(entity.getType()));
		boolean entities = entity instanceof Evoker || entityGriefingMobs.contains(entity.getType());
		boolean items = droppedItemGriefingMobs.contains(entity.getType());
		return onMobGrief(serverData, entity, blocks, entities, items);
	}

	private boolean onMobGrief(IServerData<CM, ?> serverData, Entity entity, boolean blocks, boolean entities, boolean items) {
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(ServerLevelHelper.getServerLevel(entity.getLevel()), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		for(int i = -1; i < 2; i++)
			for(int j = -1; j < 2; j++) {
				ChunkPos chunkPos = new ChunkPos(entity.chunkPosition().x + i, entity.chunkPosition().z + j);
				IPlayerChunkClaim claim = claimsManager.get(entity.getLevel().dimension().location(), chunkPos);
				if(i == 0 && j == 0 || claim != null) {//wilderness neighbors don't have to be protected this much
					IPlayerConfig config = getClaimConfig(playerConfigs, claim);
					if (config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_MOB_GRIEFING_OVERRIDE) &&
							!canGrief(entity, config, accessor, accessorId, blocks, entities, items) &&
							!hasChunkAccess(config, accessor, accessorId))
						return true;
				}
			}
		return false;
	}

	public boolean onEntityInteraction(IServerData<CM, ?> serverData, Entity interactingEntityIndirect, Entity interactingEntity, Entity target, ItemStack heldItem, InteractionHand hand, boolean attack, boolean messages) {
		if (!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if (!attack && completelyDisabledEntities.contains(target.getType())) {
			if (messages && hand != InteractionHand.OFF_HAND && interactingEntity instanceof ServerPlayer player)
				player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, ENTITY_DISABLED));
			return true;
		}
		if (interactingEntity != null && hasActiveFullPass(interactingEntity))//uses custom protection
			return false;
		if (interactingEntity instanceof Player && isAllowedStaticFakePlayerAction(serverData, (Player)interactingEntity, target.blockPosition()))
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		Level targetLevel = target.getLevel();
		ServerLevel targetServerLevel = ServerLevelHelper.getServerLevel(targetLevel);
		IPlayerChunkClaim claim = claimsManager.get(target.getLevel().dimension().location(), target.chunkPosition());
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(heldItem == null)
			heldItem = hand != null && interactingEntity instanceof LivingEntity living ? living.getItemInHand(hand) : ItemStack.EMPTY;
		boolean emptyHand = heldItem.isEmpty();
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(interactingEntityIndirect == null ? interactingEntity : interactingEntityIndirect);//in case the indirect entity has an owner too
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(targetServerLevel, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		boolean needsItemCheck = !attack && !emptyHand;
		boolean itemUseAtTargetAllowed = false;
		if(
			target != accessor
			&& (!isAllowedToGrief(interactingEntity, accessor, accessorId, config, entitiesAllowedToGriefEntities, entityAccessEntityGroups))
		) {
			InteractionTargetResult targetResult = entityAccessCheck(playerConfigs, config, target, interactingEntity, accessor, accessorId, attack, emptyHand);
			//checking checkEntityExceptions before shouldProtectEntity so that ALLOW isn't overridden with PASS
			if (targetResult == InteractionTargetResult.PROTECT) {
				if (interactingEntity instanceof ServerPlayer) {
					if (messages) {//avoiding double messages
						interactingEntity.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor((ServerPlayer) interactingEntity, hand == null ? CANT_INTERACT_ENTITY : hand == InteractionHand.MAIN_HAND ? CANT_INTERACT_ENTITY_MAIN : CANT_INTERACT_ENTITY_OFF));
						if (needsItemCheck) {
							Component message = hand == InteractionHand.MAIN_HAND ? ENTITY_TRY_EMPTY_MAIN : ENTITY_TRY_EMPTY_OFF;
							interactingEntity.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor((ServerPlayer) interactingEntity, message));
						}
					}
				}
				//OpenPartiesAndClaims.LOGGER.info("stopped {} interacting with {}", entity, target);
				return true;
			} else if (needsItemCheck && targetResult == InteractionTargetResult.ALLOW && (interactingEntity instanceof Player player && !player.isSecondaryUseActive()))
				itemUseAtTargetAllowed = true;
		}
		if(!needsItemCheck)
			return false;
		return onUseItemAt(serverData, interactingEntity, targetServerLevel, target.blockPosition(), null, heldItem, hand, itemUseAtTargetAllowed, false, messages);
	}

	@Override
	public boolean onEntityInteraction(@Nullable Entity interactingEntityIndirect, @Nullable Entity interactingEntity, @Nonnull Entity target, @Nullable ItemStack heldItem, @Nullable InteractionHand hand, boolean attack, boolean messages) {
		try {
			fullPassesPaused = true;
			return onEntityInteraction(serverData, interactingEntityIndirect, interactingEntity, target, heldItem, hand, attack, messages);
		} finally {
			fullPassesPaused = false;
		}
	}

	public boolean onFishingHookedEntity(IServerData<CM, ?> serverData, FishingHook hook, Entity entity) {
		return onEntityInteraction(serverData, hook.getOwner(), hook, entity, ItemStack.EMPTY, InteractionHand.MAIN_HAND, true, false);
	}

	public boolean onEntityFire(IServerData<CM, ?> serverData, Entity target) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerChunkClaim claim = claimsManager.get(target.getLevel().dimension().location(), target.chunkPosition());
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) &&
				config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_FIRE) &&
				isProtectable(target);
	}

	private boolean blockedByBarrierGroups(IPlayerConfig config, Entity entity, Entity accessor, UUID accessorId){
		int exceptionAccessLevel = getExceptionAccessLevel(config, accessor, accessorId);
		for (ChunkProtectionExceptionGroup<EntityType<?>> group : entityBarrierGroups.values()) {
			int configValue = config.getEffective(group.getPlayerConfigOption());
			if (configValue > 0 && exceptionAccessLevel >= configValue && group.contains(entity.getType()))
				return true;
		}
		return false;
	}

	private boolean shouldPreventEntityChunkEntry(IServerData<CM, ?> serverData, IPlayerConfigManager playerConfigs, IPlayerChunkClaim toClaim, IPlayerChunkClaim fromClaim, IPlayerConfig config, IPlayerConfig fromConfig, Entity entity, SectionPos newSection, SectionPos oldSection){
		if(toClaim == null && newSection != null)
			toClaim = claimsManager.get(entity.getLevel().dimension().location(), newSection.x(), newSection.z());
		if(config == null)
			config = getClaimConfig(playerConfigs, toClaim);
		ServerLevel entityServerLevel = ServerLevelHelper.getServerLevel(entity.getLevel());
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(entityServerLevel, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}

		if(fromClaim == null && oldSection != null)
			fromClaim = claimsManager.get(entity.getLevel().dimension().location(), oldSection.x(), oldSection.z());

		boolean enteringProtectedChunk = toClaim != null && !hasChunkAccess(config, accessor, accessorId);//wilderness is fine
		boolean isBlockedEntity = enteringProtectedChunk && forcedEntityClaimBarrierList.contains(entity.getType());
		boolean madeAnException = false;
		if(enteringProtectedChunk) {
			if (!isBlockedEntity) {
				isBlockedEntity = blockedByBarrierGroups(config, entity, accessor, accessorId);
				if (isBlockedEntity && !hitsAnotherClaim(serverData, fromClaim, toClaim, null, false)) {
					//the "from" claim might be blocking the same entity with a different option, so we don't just check the same one
					fromConfig = getClaimConfig(playerConfigs, fromClaim);
					isBlockedEntity = !blockedByBarrierGroups(fromConfig, entity, accessor, accessorId);
					madeAnException = true;
				}
			} else {
				isBlockedEntity = hitsAnotherClaim(serverData, fromClaim, toClaim, null, false);
				madeAnException = true;
			}
			if (!isBlockedEntity)
				isBlockedEntity = accessor instanceof Raider raider && raider.canJoinRaid() && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_RAIDS);
			if (!isBlockedEntity && entity instanceof ItemEntity itemEntity) {
				UUID throwerId = itemEntity.getThrower();
				if (throwerId != null) {
					if (fromConfig == null)
						fromConfig = getClaimConfig(playerConfigs, fromClaim);
					Entity thrower = getEntityById(entityServerLevel, throwerId);
					isBlockedEntity = fromConfig != config && shouldPreventToss(config, itemEntity, thrower, throwerId, ServerCore.getThrowerAccessor(itemEntity)) != itemEntity;
				}
			}
		}
		if(!isBlockedEntity) {
			UUID lootOwnerId = ServerCore.getLootOwner(entity);
			if (lootOwnerId != null) {
				if (fromConfig == null)
					fromConfig = getClaimConfig(playerConfigs, fromClaim);
				if(fromConfig != config) {
					UUID deadPlayerId = ServerCore.getDeadPlayer(entity);
					if(deadPlayerId != null) {
						Entity deadPlayer = getEntityById(entityServerLevel, deadPlayerId);
						isBlockedEntity = checkExceptionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT, fromConfig, deadPlayer, deadPlayerId);
					} else if(enteringProtectedChunk)
						isBlockedEntity = shouldStopMobLoot(config, getEntityById(entityServerLevel, lootOwnerId), lootOwnerId);
				}
			}
		}
		if(enteringProtectedChunk) {
			if (!isBlockedEntity && madeAnException && accessor != entity) {//(!isBlockedEntity && madeAnException) means that there is a barrier for this entity but it was so in the old chunk too
				//testing if the barrier protection affects the entity's owner
				//this is for cases where a player enters a claim with no player barrier and sends an entity to another claimed chunk
				//of the same owner and barrier protection for the sent entity, but with a player barrier
				//kinda similar to how the main protection option is checked for piston barriers, but a player is the piston here
				isBlockedEntity = blockedByBarrierGroups(config, accessor, accessor, accessorId);
				if (isBlockedEntity) {
					if (fromConfig == null)
						fromConfig = getClaimConfig(playerConfigs, fromClaim);
					isBlockedEntity = !blockedByBarrierGroups(fromConfig, accessor, accessor, accessorId);
				}
			}
		}
		return isBlockedEntity;
	}

	public void onEntityEnterChunk(IServerData<CM, ?> serverData, Entity entity, double goodX, double goodZ, SectionPos newSection, SectionPos oldSection) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		if(ignoreChunkEnter)
			return;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		boolean isBlockedEntity = shouldPreventEntityChunkEntry(serverData, playerConfigs, null, null, null, null, entity, newSection, oldSection);
		if(isBlockedEntity){
			ignoreChunkEnter = true;
			int goodXInt = (int)Math.floor(goodX);
			int goodZInt = (int)Math.floor(goodZ);
			//not using goodX/Z directly because it's not good enough for some things like the Supplementaries slingshot
			double fixedX = goodXInt + 0.5;
			double fixedZ = goodZInt + 0.5;
			entity.removeVehicle();
			entity.moveTo(fixedX, entity.getY(), fixedZ, entity.getYRot(), entity.getXRot());//including the rotation is necessary to prevent errors when teleporting players
			if(entity instanceof ServerPlayer player)
				player.connection.send(new ClientboundPlayerPositionPacket(fixedX, entity.getY(), fixedZ, entity.getYRot(), entity.getXRot(), Collections.emptySet(), -1, true));
			ignoreChunkEnter = false;
		}
	}
	
	public void onExplosionDetonate(IServerData<CM, ?> serverData, ServerLevel world, Explosion explosion, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		DamageSource damageSource = explosion.getDamageSource();
		if(damageSource.getEntity() != null && hasActiveFullPass(damageSource.getEntity()))
			return;
		Iterator<BlockPos> positions = affectedBlocks.iterator();
		while(positions.hasNext()) {
			BlockPos blockPos = positions.next();
			ChunkPos chunkPos = new ChunkPos(blockPos);
			IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
			IPlayerConfig config = getClaimConfig(playerConfigs, claim);
			if(config != null && (!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) || !config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS)))
				continue;
			if(config != null)
				positions.remove();
		}
		Iterator<Entity> entities = affectedEntities.iterator();
		Entity directDamager = damageSource.getDirectEntity();
		Entity damager = damageSource.getEntity();
		while(entities.hasNext()) {
			Entity entity = entities.next();
			IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), entity.chunkPosition());
			IPlayerConfig config = getClaimConfig(playerConfigs, claim);
			if(config != null && !config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
				config = null;
			if(config != null && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS) &&
					(!(damager instanceof Player) && isProtectable(entity) ||
							entityAccessCheck(playerConfigs, config, entity, directDamager, damager, null, true, true) == InteractionTargetResult.PROTECT)
					) {
				entities.remove();
			}
		}
	}
	
	public boolean onChorusFruitTeleport(IServerData<CM, ?> serverData, Vec3 pos, Entity entity) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		ChunkPos chunkPos = new ChunkPos(new BlockPos(pos));
		IPlayerChunkClaim claim = claimsManager.get(entity.getLevel().dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig claimConfig = getClaimConfig(playerConfigs, claim);
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(ServerLevelHelper.getServerLevel(entity.getLevel()), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		if(checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT, claimConfig, accessor, accessorId) && !hasChunkAccess(claimConfig, accessor, accessorId)) {
			if(entity instanceof ServerPlayer)
				entity.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor((ServerPlayer) entity, CANT_CHORUS));
			//OpenPartiesAndClaims.LOGGER.info("stopped {} from teleporting to {}", entity, pos);
			return true;
		}
		return false;
	}

	public void onLightningBolt(IServerData<CM, ?> serverData, LightningBolt bolt) {
		if(!ServerConfig.CONFIG.claimsEnabled.get() || bolt.getCause() == null)
			return;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		for(int i = -1; i < 2; i++)
			for(int j = -1; j < 2; j++) {
				ChunkPos chunkPos = new ChunkPos(bolt.chunkPosition().x + i, bolt.chunkPosition().z + j);
				IPlayerChunkClaim claim = claimsManager.get(bolt.getLevel().dimension().location(), chunkPos);
				if(i == 0 && j == 0 || claim != null) {//wilderness neighbors don't have to be protected this much
					IPlayerConfig config = getClaimConfig(playerConfigs, claim);
					if (checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING, config, bolt.getCause(), null) &&
							!hasChunkAccess(config, bolt.getCause(), null) && !isAllowedStaticFakePlayerAction(serverData, bolt.getCause(), chunkPos.getMiddleBlockPosition(0))) {
						bolt.setVisualOnly(true);
						break;
					}
				}
			}
	}

	public boolean onFireSpread(IServerData<CM, ?> serverData, ServerLevel world, BlockPos pos){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig claimConfig = getClaimConfig(playerConfigs, claim);
		return claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD);
	}

	public boolean onCropTrample(IServerData<CM, ?> serverData, Entity entity, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(entity.level.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig claimConfig = getClaimConfig(playerConfigs, claim);
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(ServerLevelHelper.getServerLevel(entity.getLevel()), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		return claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE)
				&& !hasChunkAccess(claimConfig, accessor, accessorId);
	}

	public boolean onBucketUse(IServerData<CM, ?> serverData, Entity entity, ServerLevel world, HitResult hitResult, ItemStack itemStack) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity != null && hasActiveFullPass(entity))//uses custom protection
			return false;
		//just onUseItemAt would work for buckets in "vanilla" as well, but it's better to use the proper bucket event too
		BlockPos pos;
		Direction direction = null;
		if(hitResult instanceof BlockHitResult blockHitResult) {
			pos = blockHitResult.getBlockPos();
			direction = blockHitResult.getDirection();
		} else
			pos = new BlockPos(hitResult.getLocation());
		return onUseItemAt(serverData, entity, world, pos, direction, itemStack, null, false, false, true);
	}

	public boolean onUseItemAt(IServerData<CM, ?> serverData, Entity entity, ServerLevel world, BlockPos pos, Direction direction, ItemStack itemStack, InteractionHand hand, boolean itemUseAtTargetAllowed, boolean itemUseAtOffsetAllowed, boolean messages) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(completelyDisabledItems.contains(itemStack.getItem())) {
			if(messages && entity instanceof ServerPlayer player)
				player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, hand == InteractionHand.MAIN_HAND ? ITEM_DISABLED_MAIN : ITEM_DISABLED_OFF));
			return true;
		}
		if(entity != null && hasActiveFullPass(entity))//uses custom protection
			return false;
		if(!isItemUseRestricted(itemStack))
			return false;
		if(entity instanceof Player player) {
			if (hand == null)
				hand = player.getItemInHand(InteractionHand.MAIN_HAND) == itemStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			if (additionalBannedItems.contains(itemStack.getItem()) &&
					onItemRightClick(serverData, hand, itemStack, pos, player, false)) {//only configured items on purpose
				if(messages && player instanceof ServerPlayer)
					player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor((ServerPlayer) player, hand == InteractionHand.MAIN_HAND ? CANT_APPLY_ITEM_THIS_CLOSE_MAIN : CANT_APPLY_ITEM_THIS_CLOSE_OFF));
				return true;
			}
		}
		BlockPos pos2 = null;
		if(direction != null)
			pos2 = pos.offset(direction.getNormal());
		if(itemUseAtTargetAllowed && pos2 == null)
			return false;
		if(entity instanceof Player && isAllowedStaticFakePlayerAction(serverData, (Player)entity, pos, pos2))
			return false;
		ChunkPos chunkPos = new ChunkPos(pos);
		ChunkPos chunkPos2;
		if(!itemUseAtTargetAllowed && applyItemAccessCheck(serverData, chunkPos, entity, world, itemStack)
			|| !itemUseAtOffsetAllowed && pos2 != null && !(chunkPos2 = new ChunkPos(pos2)).equals(chunkPos) && applyItemAccessCheck(serverData, chunkPos2, entity, world, itemStack)
				){
			if(messages && entity instanceof ServerPlayer player)
				player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, hand == InteractionHand.MAIN_HAND ? CANT_APPLY_ITEM_MAIN : CANT_APPLY_ITEM_OFF));
			return true;
		}
		return false;
	}

	private boolean applyItemAccessCheck(IServerData<CM, ?> serverData, ChunkPos chunkPos, Entity entity, ServerLevel world, ItemStack itemStack) {
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(world, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		return checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_USE, config, accessor, accessorId) && !hasChunkAccess(config, accessor, accessorId)
				&& !isOptionalItemException(serverData, accessor, accessorId, itemStack, world, chunkPos);
	}

	private boolean isOptionalItemException(IServerData<CM, ?> serverData, Entity accessor, UUID accessorId, ItemStack itemStack, ServerLevel world, ChunkPos chunkPos){
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		int exceptionAccessLevel = getExceptionAccessLevel(config, accessor, accessorId);
		for (ChunkProtectionExceptionGroup<Item> group : itemExceptionGroups.values()) {
			if (exceptionAccessLevel <= config.getEffective(group.getPlayerConfigOption()) && group.contains(itemStack.getItem()))
				return true;
		}
		return false;
	}

	private boolean isOnChunkEdge(BlockPos pos){
		int chunkRelativeX = pos.getX() & 15;
		int chunkRelativeZ = pos.getZ() & 15;
		return isOnChunkEdge(chunkRelativeX, chunkRelativeZ);
	}

	private boolean isOnChunkEdge(int chunkRelativeX, int chunkRelativeZ){
		return chunkRelativeX == 0 || chunkRelativeX == 15 || chunkRelativeZ == 0 || chunkRelativeZ == 15;
	}

	private boolean isProtectionEnabled(IPlayerConfig config, IPlayerConfigOptionSpecAPI<?> option){
		Object value = config.getEffective(option);
		return value instanceof Boolean bool && bool || value instanceof Integer integ && integ > 0;
	}

	private int compareProtectionLevels(IPlayerConfig config1, IPlayerConfig config2, IPlayerConfigOptionSpecAPI<? extends Comparable<?>> option, boolean isExceptionOption){
		Comparable<?> value1 = config1.getEffective(option);
		Comparable<?> value2 = config2.getEffective(option);
		if(value1 instanceof Boolean bool1) {
			int result = bool1.compareTo((Boolean) value2);
			return isExceptionOption ? -result : result;
		}
		Integer int1 = (Integer) value1;
		Integer int2 = (Integer) value2;
		if(int1.equals(int2))
			return 0;
		if(!isExceptionOption) {
			if (int1 > 0 && int2 <= 0)
				return 1;
			if (int2 > 0 && int1 <= 0)
				return -1;
		}
		return int2.compareTo(int1);//purposely reversed because when protection is > 0, lesser value means more protection
	}

	private boolean hitsAnotherClaim(IServerData<CM, ?> serverData, IPlayerChunkClaim fromClaim, IPlayerChunkClaim toClaim,
									 IPlayerConfigOptionSpecAPI<? extends Comparable<?>> optionSpec, boolean withBuildCheck){
		if(toClaim == null || fromClaim == toClaim || fromClaim != null && fromClaim.isSameClaimType(toClaim))
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig toClaimConfig = getClaimConfig(playerConfigs, toClaim);
		if(!toClaimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) || optionSpec != null && !isProtectionEnabled(toClaimConfig, optionSpec))
			return false;
		if(fromClaim != null && fromClaim.getPlayerId().equals(toClaim.getPlayerId())){
			IPlayerConfig fromClaimConfig = getClaimConfig(playerConfigs, fromClaim);
			if(!fromClaimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS)
					|| optionSpec != null && compareProtectionLevels(fromClaimConfig, toClaimConfig, optionSpec, false) < 0)
				return true;
			if(withBuildCheck){
				int toClaimItemUseProt = toClaimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_USE);
				if(toClaimItemUseProt == 0 && toClaimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS) == 0)
					return false;//basically no building protection, so no point in checking other options

				//options that are likely to affect a player's ability to build in a chunk
				if(compareProtectionLevels(fromClaimConfig, toClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS, false) < 0)
					return true;
				if(toClaimItemUseProt > 0) {
					if (compareProtectionLevels(fromClaimConfig, toClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_USE, false) < 0)
						return true;
					if (compareProtectionLevels(fromClaimConfig, toClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE, false) < 0)
						return true;
				}
				if(compareProtectionLevels(fromClaimConfig, toClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS, false) < 0)
					return true;
				if(compareProtectionLevels(fromClaimConfig, toClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER, false) < 0)
					return true;
				if(compareProtectionLevels(fromClaimConfig, toClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER, false) < 0)
					return true;
				for(ChunkProtectionExceptionGroup<Item> itemExceptionGroup : itemExceptionGroups.values()){
					if(compareProtectionLevels(fromClaimConfig, toClaimConfig, itemExceptionGroup.getPlayerConfigOption(), true) < 0)
						return true;
				}
				for(ChunkProtectionExceptionGroup<EntityType<?>> entityBarrierGroup : entityBarrierGroups.values()){
					if(compareProtectionLevels(fromClaimConfig, toClaimConfig, entityBarrierGroup.getPlayerConfigOption(), false) < 0)
						return true;
				}
				for(ChunkProtectionExceptionGroup<Block> blockExceptionGroup : blockExceptionGroups.values()){
					if((blockExceptionGroup.getType() == ChunkProtectionExceptionType.INTERACTION || blockExceptionGroup.getType() == ChunkProtectionExceptionType.ANY_ITEM_INTERACTION) && compareProtectionLevels(fromClaimConfig, toClaimConfig, blockExceptionGroup.getPlayerConfigOption(), true) < 0)
						return true;
				}
			}
			return false;
		}
		return true;
	}

	private boolean hitsAnotherClaim(IServerData<CM, ?> serverData, Level world, BlockPos from, BlockPos to, IPlayerConfigOptionSpecAPI<? extends Comparable<?>> optionSpec, boolean withBuildCheck){
		int fromChunkX = from.getX() >> 4;
		int fromChunkZ = from.getZ() >> 4;
		int toChunkX = to.getX() >> 4;
		int toChunkZ = to.getZ() >> 4;
		if(fromChunkX == toChunkX && fromChunkZ == toChunkZ)
			return false;
		IPlayerChunkClaim toClaim = claimsManager.get(world.dimension().location(), toChunkX, toChunkZ);
		IPlayerChunkClaim fromClaim = claimsManager.get(world.dimension().location(), fromChunkX, fromChunkZ);
		return hitsAnotherClaim(serverData, fromClaim, toClaim, optionSpec, withBuildCheck);
	}

	public boolean onFluidSpread(IServerData<CM, ?> serverData, ServerLevel world, BlockPos from, BlockPos to) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		return isOnChunkEdge(from) && hitsAnotherClaim(serverData, world, from, to, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FLUID_BARRIER, true);
	}

	public boolean onDispenseFrom(IServerData<CM, ?> serverData, ServerLevel serverLevel, BlockPos from) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(!isOnChunkEdge(from))
			return false;
		BlockState blockState = serverLevel.getBlockState(from);
		Direction direction = blockState.getValue(DirectionalBlock.FACING);
		BlockPos to = from.relative(direction);
		return isOnChunkEdge(from) && hitsAnotherClaim(serverData, serverLevel, from, to, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_DISPENSER_BARRIER, true);
	}

	private boolean shouldStopPistonPush(IServerData<CM, ?> serverData, ServerLevel world, BlockPos pushPos, int pistonChunkX, int pistonChunkZ, IPlayerChunkClaim pistonClaim){
		int pushChunkX = pushPos.getX() >> 4;
		int pushChunkZ = pushPos.getZ() >> 4;
		if(pushChunkX == pistonChunkX && pushChunkZ == pistonChunkZ)
			return false;
		IPlayerChunkClaim pushClaim = claimsManager.get(world.dimension().location(), pushChunkX, pushChunkZ);
		return hitsAnotherClaim(serverData, pistonClaim, pushClaim, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER, true);
	}

	public boolean onPistonPush(IServerData<CM, ?> serverData, ServerLevel world, List<BlockPos> toPush, List<BlockPos> toDestroy, BlockPos pistonPos, Direction direction, boolean extending) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim pistonClaim = claimsManager.get(world.dimension().location(), pistonPos);
		int pistonChunkX = pistonPos.getX() >> 4;
		int pistonChunkZ = pistonPos.getZ() >> 4;
		Direction actualDirection = extending ? direction : direction.getOpposite();
		if(toPush.isEmpty() && toDestroy.isEmpty()) {
			BlockPos pushPos = pistonPos.relative(direction);
			if(shouldStopPistonPush(serverData, world, pushPos, pistonChunkX, pistonChunkZ, pistonClaim))
				return true;
			return shouldStopPistonPush(serverData, world, pushPos.relative(actualDirection), pistonChunkX, pistonChunkZ, pistonClaim);
		}
		Iterator<BlockPos> posIterator = Iterators.concat(toPush.iterator(), toDestroy.iterator());
		while(posIterator.hasNext()){
			BlockPos pushPos = posIterator.next();
			if (shouldStopPistonPush(serverData, world, pushPos, pistonChunkX, pistonChunkZ, pistonClaim))
				return true;
			BlockPos pushedToPos = pushPos.relative(actualDirection);
			if (shouldStopPistonPush(serverData, world, pushedToPos, pistonChunkX, pistonChunkZ, pistonClaim))
				return true;
		}
		return false;
	}

	private Object getAccessorInfo(Entity entity){
		Object result;
		if(entity instanceof Projectile){
			result = ((Projectile) entity).getOwner();
		} else if(entity instanceof ItemEntity){
			UUID ownerId = ((ItemEntity) entity).getOwner();
			if(ownerId == null)
				ownerId = ((ItemEntity) entity).getThrower();
			result = ownerId;
		} else if(entity instanceof Vex){
			result = ((Vex) entity).getOwner();
		} else if(entity instanceof EvokerFangs){
			result = ((EvokerFangs) entity).getOwner();
		} else if(entity instanceof Boat){
			result = entity.getControllingPassenger();
		} else
			result = entityHelper.getTamer(entity);
		return result == null ? entity : result;
	}

	public void onEntitiesPushBlock(IServerData<CM, ?> serverData, ServerLevel world, BlockPos pos, Block block, List<? extends Entity> entities) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		Iterator<? extends Entity> iterator = entities.iterator();
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return;
		IPlayerConfigOptionSpecAPI<Integer> blockSpecificOption =
				block instanceof ButtonBlock ?
					PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES :
				block instanceof TargetBlock ?
					PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_TARGETS_FROM_PROJECTILES :
				null;
		if(blockSpecificOption != null && config.getEffective(blockSpecificOption) <= 0)
			return;
		boolean everyoneExceptAccessHavers = blockSpecificOption != null && config.getEffective(blockSpecificOption) == 1;
		Map<UUID, Map<IPlayerConfigOptionSpecAPI<Integer>, Boolean>> cachedAccessorOptionResults = null;
		boolean isWeighted = block instanceof WeightedPressurePlateBlock;
		boolean isTripwire = block instanceof TripWireBlock;
		while(iterator.hasNext()){
			Entity e = iterator.next();
			if(blockSpecificOption == null && !isWeighted && e.isIgnoringBlockTriggers())//already ignored in vanilla
				continue;
			Entity accessor;
			UUID accessorId;
			Object accessorInfo = getAccessorInfo(e);
			if (accessorInfo instanceof UUID) {
				accessorId = (UUID) accessorInfo;
				accessor = getEntityById(world, accessorId);
			} else {
				accessor = (Entity) accessorInfo;
				accessorId = accessor.getUUID();
			}
			IPlayerConfigOptionSpecAPI<Integer> entitySpecificOption = blockSpecificOption;
			if(entitySpecificOption == null) {
				if(isTripwire){
					entitySpecificOption =
							e instanceof Player ?
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_PLAYERS :
							e instanceof LivingEntity ?
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_MOBS :
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_OTHER;
				} else {
					entitySpecificOption =
							e instanceof Player ?
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLATES_FROM_PLAYERS :
							e instanceof LivingEntity ?
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLATES_FROM_MOBS :
								PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLATES_FROM_OTHER;
				}
			}
			Map<IPlayerConfigOptionSpecAPI<Integer>, Boolean> resultsCachedForAccessor;
			if(cachedAccessorOptionResults != null && (resultsCachedForAccessor = cachedAccessorOptionResults.get(accessorId)) != null){
				Boolean cachedResult = resultsCachedForAccessor.get(entitySpecificOption);
				if(cachedResult != null){
					if(cachedResult)
						iterator.remove();
					continue;
				}
			}
			boolean protect = (everyoneExceptAccessHavers || checkProtectionLeveledOption(entitySpecificOption, config, accessor, accessorId)) && !hasChunkAccess(config, accessor, accessorId);
			if(!protect &&
					(blockSpecificOption == PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES ||
					blockSpecificOption == null && !isWeighted)
			)
				break;//for these blocks 1 allowed entity is enough info
			if(iterator.hasNext()){
				if(cachedAccessorOptionResults == null)
					cachedAccessorOptionResults = new HashMap<>();
				resultsCachedForAccessor = cachedAccessorOptionResults.get(accessorId);
				if(resultsCachedForAccessor == null)
					cachedAccessorOptionResults.put(accessorId, resultsCachedForAccessor = new HashMap<>());
				resultsCachedForAccessor.put(entitySpecificOption, protect);
			}
			if(protect)
				iterator.remove();
		}
	}

	public void onEntitiesCollideWithEntity(IServerData<CM, ?> serverData, Entity entity, List<? extends Entity> collidingEntities){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		Level level = entity.getLevel();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return;
		IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), entity.chunkPosition());
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return;
		if(config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS) == 0 &&
				config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS) == 0 &&
				config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER) == 0)
			return;
		Map<UUID, Map<IPlayerConfigOptionSpecAPI<Integer>, Boolean>> cachedAccessorOptionResults = null;
		Iterator<? extends Entity> iterator = collidingEntities.iterator();
		boolean multipleCollisionsMatter = false;
		while(iterator.hasNext()) {
			Entity collidingEntity = iterator.next();
			Entity accessor;
			UUID accessorId;
			Object accessorInfo = getAccessorInfo(collidingEntity);
			if (accessorInfo instanceof UUID) {
				accessorId = (UUID) accessorInfo;
				accessor = getEntityById(serverLevel, accessorId);
			} else {
				accessor = (Entity) accessorInfo;
				accessorId = accessor.getUUID();
			}
			IPlayerConfigOptionSpecAPI<Integer> option = getUsedEntityProtectionOption(config, collidingEntity, accessor);
			Map<IPlayerConfigOptionSpecAPI<Integer>, Boolean> accessorCache;
			if(cachedAccessorOptionResults != null && (accessorCache = cachedAccessorOptionResults.get(accessorId)) != null){
				Boolean cachedResult = accessorCache.get(option);
				if(cachedResult != null) {
					if(cachedResult)
						iterator.remove();
					continue;
				}
			}
			boolean protect = checkProtectionLeveledOption(option, config, accessor, accessorId) && !hasChunkAccess(config, accessor, accessorId);
			if(!protect && !multipleCollisionsMatter)
				break;
			if (iterator.hasNext()){
				if (cachedAccessorOptionResults == null)
					cachedAccessorOptionResults = new HashMap<>();
				accessorCache = cachedAccessorOptionResults.get(accessorId);
				if (accessorCache == null)
					cachedAccessorOptionResults.put(accessorId, accessorCache = new HashMap<>());
				accessorCache.put(option, protect);
			}
			if(protect)
				iterator.remove();
		}
	}

	public void onEntityAffectsEntities(IServerData<CM, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, Entity entity, List<Entity> targets) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		double randomDrop = entity instanceof Boat && ServerConfig.CONFIG.reducedBoatEntityCollisions.get() ? 0.9 : 0;
		if (randomDrop > 0 && Math.random() < randomDrop) {
			//simple optimization to avoid checking claim permissions every tick
			targets.clear();
			return;
		}
		Iterator<Entity> iterator = targets.iterator();
		while(iterator.hasNext()){
			Entity target = iterator.next();
			if(onEntityInteraction(serverData, null, entity, target, null, null, true, false))
				iterator.remove();
		}
	}

	public boolean onEntityPushed(IServerData<CM, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, Entity target, MoverType moverType) {
		if(moverType != MoverType.SHULKER)
			return false;
		return onEntityInteraction(serverData, null, null, target, null, null, true, false);
	}

	public boolean onNetherPortal(IServerData<CM, ?> serverData, Entity entity, ServerLevel world, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return false;
		if(config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_PLAYERS) == 0 &&
				config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_MOBS) == 0 &&
				config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_OTHER) == 0)
			return false;
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(world, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		IPlayerConfigOptionSpecAPI<Integer> option =
				entity instanceof Player ?
						PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_PLAYERS :
				entity instanceof LivingEntity ?
						PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_MOBS :
						PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_OTHER;
		return checkProtectionLeveledOption(option, config, accessor, accessorId) && !hasChunkAccess(config, accessor, accessorId);
	}

	public boolean onRaidSpawn(IServerData<CM, ?> serverData, ServerLevel world, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_RAIDS);
	}

	public boolean onMobSpawn(IServerData<CM, ?> serverData, Entity entity, double x, double y, double z, MobSpawnType spawnReason) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(entity.level.dimension().location(), new ChunkPos(new BlockPos(x, y, z)));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return false;
		IPlayerConfigOptionSpecAPI<Boolean> option;
		boolean hostile = entityHelper.isHostile(entity);
		if(spawnReason == MobSpawnType.SPAWNER){
			if(hostile)
				option = PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_HOSTILE_SPAWNERS;
			else
				option = PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FRIENDLY_SPAWNERS;
		} else {
			if(hostile)
				option = PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_HOSTILE_NATURAL_SPAWN;
			else
				option = PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FRIENDLY_NATURAL_SPAWN;
		}
		return config.getEffective(option);
	}

	public boolean onItemAddedToWorld(IServerData<CM, ?> serverData, ItemEntity itemEntity) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		UUID throwerId = itemEntity.getThrower();
		if(throwerId == null)
			return false;
		IPlayerChunkClaim claim = claimsManager.get(itemEntity.getLevel().dimension().location(), itemEntity.chunkPosition());
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return false;
		Entity thrower = getEntityById(ServerLevelHelper.getServerLevel(itemEntity.getLevel()), throwerId);
		Entity result = shouldPreventToss(config, itemEntity, thrower, throwerId, null);
		if(result != itemEntity){
			if(result instanceof Player player && !player.isCreative()) {//causes weird dupe in creative + isn't really necessary to restore items
				ItemStack itemStack = itemEntity.getItem();
				if (!player.addItem(itemStack) && thrower != player && !thrower.chunkPosition().equals(player.chunkPosition()))
					player.drop(itemStack, true);//try dropping the rest from the accessor
			}
			return true;
		}
		return false;
	}

	private Entity shouldPreventToss(IPlayerConfig config, ItemEntity itemEntity, Entity thrower, UUID throwerId, UUID throwerAccessorId){//returns the accessor if protected, or the item entity if not protected
		if(throwerId == null)
			return itemEntity;
		Entity accessor = null;
		UUID accessorId;
		if(throwerAccessorId == null) {
			if (thrower != null) {
				Object accessorInfo = getAccessorInfo(thrower);
				if (accessorInfo instanceof UUID) {
					accessorId = (UUID) accessorInfo;
					accessor = getEntityById(ServerLevelHelper.getServerLevel(itemEntity.getLevel()), accessorId);
				} else {
					accessor = (Entity) accessorInfo;
					accessorId = accessor.getUUID();
				}
			} else
				accessorId = throwerId;
		} else {
			accessorId = throwerAccessorId;
			accessor = getEntityById(ServerLevelHelper.getServerLevel(itemEntity.level), accessorId);
		}
		Entity usedOptionBase = !config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_REDIRECT) ?
				thrower : accessor;
		IPlayerConfigOptionSpecAPI<Integer> option;
		if(usedOptionBase != null) {
			option = !(usedOptionBase instanceof LivingEntity) ?
					PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_OTHER
					: usedOptionBase instanceof Player ?
					PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_PLAYERS
					: PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_MOBS;
		} else
			option = getToughestProtectionLevelOption(config, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_PLAYERS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_MOBS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_OTHER);
		if(checkProtectionLeveledOption(option, config, accessor, accessorId) && !hasChunkAccess(config, accessor, accessorId))
			return accessor;
		return itemEntity;
	}

	private boolean hasAnEnabledOption(IPlayerConfig config, IPlayerConfigOptionSpecAPI<Boolean> option1, IPlayerConfigOptionSpecAPI<Boolean> option2, IPlayerConfigOptionSpecAPI<Boolean> option3){
		//the used option base is offline; or possibly in another dimension, if it's not a player
		//assume the worst and use the toughest protection
		return config.getEffective(option1) || config.getEffective(option2) || config.getEffective(option3);
	}

	private IPlayerConfigOptionSpecAPI<Integer> getToughestProtectionLevelOption(IPlayerConfig config, IPlayerConfigOptionSpecAPI<Integer> option1, IPlayerConfigOptionSpecAPI<Integer> option2, IPlayerConfigOptionSpecAPI<Integer> option3){
		//the used option base is offline; or possibly in another dimension, if it's not a player
		//assume the worst and use the toughest protection
		int toughestProtectionLevel = config.getEffective(option1);
		IPlayerConfigOptionSpecAPI<Integer> toughestOption = option1;
		int protectionLevel = config.getEffective(option2);
		if(protectionLevel != 0 && (toughestProtectionLevel == 0 || protectionLevel < toughestProtectionLevel)){
			toughestProtectionLevel = protectionLevel;
			toughestOption = option2;
		}
		if(option3 != null) {
			protectionLevel = config.getEffective(option3);
			if (protectionLevel != 0 && (toughestProtectionLevel == 0 || protectionLevel < toughestProtectionLevel))
				return option3;
		}
		return toughestOption;
	}

	public boolean onLivingLootEntity(IServerData<CM, ?> serverData, LivingEntity livingEntity, Entity lootEntity, DamageSource source){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		Entity accessor;
		UUID accessorId;
		boolean noKiller = source.getEntity() == null || source.getEntity() == livingEntity;
		Object accessorInfo = getAccessorInfo(noKiller ? livingEntity : source.getEntity());
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(ServerLevelHelper.getServerLevel(lootEntity.getLevel()), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		if(lootEntity instanceof ItemEntity itemEntity)
			itemEntity.setThrower(livingEntity.getUUID());
		ServerCore.setLootOwner(lootEntity, accessorId);
		if(livingEntity instanceof Player) {
			ServerCore.setDeadPlayer(lootEntity, livingEntity.getUUID());
			return false;
		}
		IPlayerChunkClaim claim = claimsManager.get(lootEntity.getLevel().dimension().location(), lootEntity.chunkPosition());
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return false;
		return shouldStopMobLoot(config, accessor, accessorId) &&
				(!(accessor instanceof Player player) || !isAllowedStaticFakePlayerAction(serverData, player, lootEntity.blockPosition()));
	}

	private boolean shouldStopMobLoot(IPlayerConfig config, Entity accessor, UUID accessorId){
		return !hasChunkAccess(config, accessor, accessorId) && checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_MOB_LOOT, config, accessor, accessorId);
	}

	public boolean onEntityPickup(IServerData<CM, ?> serverData, Entity entity, Entity pickedEntity, UUID pickedEntityThrowerId, UUID pickedEntityOwnerId, Map<Entity, Set<ChunkPos>> cantPickupCache, TriFunction<IPlayerConfig, Entity, Entity, IPlayerConfigOptionSpecAPI<Integer>> protectionOptionGetter) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity.getUUID().equals(pickedEntityThrowerId) || entity.getUUID().equals(pickedEntityOwnerId) ||
				entity.getUUID().equals(ServerCore.getLootOwner(pickedEntity)) || hasActiveFullPass(entity))
			return false;
		ChunkPos chunkPos = pickedEntity.chunkPosition();
		Set<ChunkPos> cantPickupCached = cantPickupCache.get(entity);//avoiding rechecking every tick for a billion pickupable items in the same chunk
		if(cantPickupCached != null && cantPickupCached.contains(chunkPos))
			return true;
		IPlayerChunkClaim claim = claimsManager.get(pickedEntity.getLevel().dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		UUID deadPlayerId = ServerCore.getDeadPlayer(pickedEntity);
		if(deadPlayerId != null){
			Entity deadPlayer = getEntityById(ServerLevelHelper.getServerLevel(pickedEntity.getLevel()), deadPlayerId);
			if(checkExceptionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT, config, deadPlayer, deadPlayerId))
				return true;
		}
		boolean shouldPrevent = false;
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(ServerLevelHelper.getServerLevel(entity.getLevel()), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		if(isAllowedToGrief(entity, accessor, accessorId, config, entitiesAllowedToGriefDroppedItems, droppedItemAccessEntityGroups))
			return false;
		if(config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS)) {
			if (!hasChunkAccess(config, accessor, accessorId)) {
				IPlayerConfigOptionSpecAPI<Integer> usedOption = protectionOptionGetter.apply(config, entity, accessor);
				shouldPrevent = checkProtectionLeveledOption(usedOption, config, accessor, accessorId);
			}
		}
		if(!shouldPrevent && !(entity instanceof Player)){
			ChunkPos entityChunkPos = entity.chunkPosition();
			if(!chunkPos.equals(entityChunkPos)) {
				//Additional check to stop mobs pulling dropped items into a protected claim through a barrier.
				//Foxes pick items up and then throw away some at their own position.
				//Not all item dropping is considered to be tossing, and tamed/owned mobs have the permission to toss
				//items inside their owner's claims, which is why this can be a problem.
				//And, if there is an item barrier, then you probably don't want anything inside the protected chunks to
				//be able to pick the items up either.
				//
				//This does not prevent mobs from leaving the protected chunks, picking items up, going back in and dropping them though.
				//In that case item toss protection is all you have, but it doesn't stop mobs tamed by the claim owner.
				IPlayerChunkClaim entityPosClaim = claimsManager.get(pickedEntity.getLevel().dimension().location(), entityChunkPos);
				IPlayerConfig entityPosConfig = getClaimConfig(playerConfigs, entityPosClaim);
				if(entityPosConfig != config)
					shouldPrevent = shouldPreventEntityChunkEntry(serverData, playerConfigs, entityPosClaim, claim, entityPosConfig, config, pickedEntity, null, null);
			}
		}
		if(shouldPrevent){
			if (cantPickupCached == null) {
				cantPickupCached = new HashSet<>();
				cantPickupCache.put(entity, cantPickupCached);
			}
			cantPickupCached.add(chunkPos);
		}
		return shouldPrevent;
	}

	public boolean onItemPickup(IServerData<CM, ?> serverData, Entity entity, ItemEntity itemEntity) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity.getUUID().equals(ServerCore.getThrowerAccessor(itemEntity)))
			return false;
		return onEntityPickup(serverData, entity, itemEntity, itemEntity.getThrower(), itemEntity.getOwner(), cantPickupItemsInTickCache, usedDroppedItemProtectionOptionGetter);
	}

	@Override
	public boolean onItemPickup(@Nonnull Entity entity, @Nonnull ItemEntity itemEntity) {
		try {
			fullPassesPaused = true;
			return onItemPickup(serverData, entity, itemEntity);
		} finally {
			fullPassesPaused = false;
		}
	}

	private IPlayerConfigOptionSpecAPI<Integer> getUsedDroppedItemProtectionOption(IPlayerConfig config, Entity entity, Entity accessor){
		Entity usedOptionBase = !(entity instanceof Player) && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_REDIRECT) ?
				accessor : entity;
		if(usedOptionBase == null)
			return getToughestProtectionLevelOption(config, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS, null);
		return usedOptionBase instanceof Player ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS :
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS;
	}

	public boolean onEntityMerge(IServerData<CM, ?> serverData, Entity first, UUID firstThrower, UUID firstOwner, Entity second, UUID secondThrower, UUID secondOwner, IPlayerConfigOptionSpecAPI<Integer> playerOption, IPlayerConfigOptionSpecAPI<Integer> mobOption, IPlayerConfigOptionSpecAPI<Boolean> redirectOption){
		//needs to reflect any future changes to item pickup protection
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos firstChunkPos = first.chunkPosition();
		IPlayerChunkClaim firstClaim = claimsManager.get(first.getLevel().dimension().location(), firstChunkPos);
		IPlayerConfig firstConfig = getClaimConfig(playerConfigs, firstClaim);
		boolean differentThrower = !Objects.equals(firstThrower, secondThrower);
		boolean differentOwner =  !Objects.equals(firstOwner, secondOwner);
		boolean differentLootOwner = !Objects.equals(ServerCore.getLootOwner(first), ServerCore.getLootOwner(second));
		boolean firstProtected = firstConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS);
		int firstItemPlayerProtection = !firstProtected ? 0 : firstConfig.getEffective(playerOption);
		int firstItemMobsProtection = !firstProtected || mobOption == null ? 0 : firstConfig.getEffective(mobOption);
		if(differentThrower || differentOwner || differentLootOwner) {
			if(firstItemPlayerProtection > 0 || firstItemMobsProtection > 0)
				return true;
			//if dead player ID exists it will be the same as thrower
			UUID firstDeadPlayerId = ServerCore.getDeadPlayer(first);
			if (firstDeadPlayerId != null) {
				Entity firstDeadPlayer = getEntityById(ServerLevelHelper.getServerLevel(first.getLevel()), firstDeadPlayerId);
				if (checkExceptionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT, firstConfig, firstDeadPlayer, firstDeadPlayerId))
					return true;
			}
		}

		ChunkPos secondChunkPos = second.chunkPosition();
		if(secondChunkPos.equals(firstChunkPos))
			return false;
		IPlayerChunkClaim secondClaim = claimsManager.get(first.getLevel().dimension().location(), secondChunkPos);
		if(firstClaim == secondClaim)
			return false;
		IPlayerConfig secondConfig = getClaimConfig(playerConfigs, secondClaim);
		if(firstConfig == secondConfig)
			return false;
		UUID firstClaimOwner = firstConfig.getPlayerId();
		UUID secondClaimOwner = secondConfig.getPlayerId();
		boolean sameClaimOwner = Objects.equals(firstClaimOwner, secondClaimOwner);
		boolean secondProtected = secondConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS);
		int secondItemPlayerProtection = !secondProtected ? 0 : secondConfig.getEffective(playerOption);
		if(firstItemPlayerProtection != secondItemPlayerProtection || !sameClaimOwner && secondItemPlayerProtection > 1)//party-based protection still matters even if it's equal
			return true;
		int secondItemMobsProtection = !secondProtected || mobOption == null ? 0 : secondConfig.getEffective(mobOption);
		if(firstItemMobsProtection != secondItemMobsProtection || !sameClaimOwner && secondItemMobsProtection > 1)//party-based protection still matters even if it's equal
			return true;
		if(firstItemPlayerProtection != firstItemMobsProtection && redirectOption != null) {//redirect matters
			boolean firstItemProtectionRedirect = firstConfig.getEffective(redirectOption);
			boolean secondItemProtectionRedirect = secondConfig.getEffective(redirectOption);
			if (firstItemProtectionRedirect != secondItemProtectionRedirect)
				return true;
		}
		//death loot is further protected by the entity barrier vvv along with some other stuff
		return shouldPreventEntityChunkEntry(serverData, playerConfigs, firstClaim, secondClaim, firstConfig, secondConfig, second, null, null);
	}

	public boolean onItemStackMerge(IServerData<CM, ?> serverData, ItemEntity first, ItemEntity second) {
		return onEntityMerge(serverData, first, first.getThrower(), first.getOwner(), second, second.getThrower(), second.getOwner(), PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_REDIRECT);
	}

	public boolean onExperiencePickup(IServerData<CM, ?> serverData, ExperienceOrb orb, Player player) {
		return onEntityPickup(serverData, player, orb, null, null, cantPickupXPInTickCache, usedExperienceOrbProtectionOptionGetter);
	}

	public boolean onExperienceMerge(IServerData<CM, ?> serverData, ExperienceOrb from, ExperienceOrb into) {
		return onEntityMerge(serverData, into, null, null, from, null, null, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_XP_PICKUP, null, null);
	}

	public void setThrowerAccessor(ItemEntity itemEntity) {
		//prevents protection circumvention (mostly entity barrier) when changing the owner of the thrower
		UUID throwerId = itemEntity.getThrower();
		Entity thrower = getEntityById(ServerLevelHelper.getServerLevel(itemEntity.level), throwerId);
		UUID accessorId;
		if(thrower != null){
			Object accessorInfo = getAccessorInfo(thrower);
			if (accessorInfo instanceof UUID)
				accessorId = (UUID) accessorInfo;
			else
				accessorId = ((Entity) accessorInfo).getUUID();
		} else
			accessorId = throwerId;
		ServerCore.setThrowerAccessor(itemEntity, accessorId);
	}

	private Entity getEntityById(ServerLevel world, UUID id){
		if(world == null)
			return null;
		Entity result = world.getServer().getPlayerList().getPlayer(id);
		return result != null ? result : world.getEntity(id);
	}

	private boolean onPosAffectedByAnotherPos(IServerData<CM, ?> serverData, IPlayerChunkClaim toClaim, IPlayerChunkClaim fromClaim, boolean affectsBlocks, boolean affectsEntities) {
		if(!hitsAnotherClaim(serverData, fromClaim, toClaim, null, true))
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig posClaimConfig = getClaimConfig(playerConfigs, toClaim);
		if(affectsBlocks && isProtectionEnabled(posClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER))
			return true;
		return affectsEntities && isProtectionEnabled(posClaimConfig, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER);
	}

	private boolean onPosAffectedByAnotherPos(IServerData<CM, ?> serverData, ServerLevel world, IPlayerChunkClaim toClaim, int toChunkX, int toChunkZ, int fromChunkX, int fromChunkZ, boolean affectsBlocks, boolean affectsEntities) {
		if(toChunkX == fromChunkX && toChunkZ == fromChunkZ)
			return false;
		IPlayerChunkClaim anchorClaim = claimsManager.get(world.dimension().location(), fromChunkX, fromChunkZ);
		return onPosAffectedByAnotherPos(serverData, toClaim, anchorClaim, affectsBlocks, affectsEntities);
	}

	public boolean onPosAffectedByAnotherPos(IServerData<CM, ?> serverData, ServerLevel toWorld, int toChunkX, int toChunkZ, ServerLevel fromWorld, int fromChunkX, int fromChunkZ, boolean includeWilderness, boolean affectsBlocks, boolean affectsEntities) {
		if(toChunkX == fromChunkX && toChunkZ == fromChunkZ)
			return false;
		IPlayerChunkClaim toClaim = claimsManager.get(toWorld.dimension().location(), toChunkX, toChunkZ);
		if(!includeWilderness && toClaim == null)
			return false;
		IPlayerChunkClaim fromClaim = claimsManager.get(fromWorld.dimension().location(), fromChunkX, fromChunkZ);
		return onPosAffectedByAnotherPos(serverData, toClaim, fromClaim, affectsBlocks, affectsEntities);
	}

	@Override
	public boolean onPosAffectedByAnotherPos(@Nonnull ServerLevel toWorld, @Nonnull ChunkPos toChunk, @Nonnull ServerLevel fromWorld, @Nonnull ChunkPos fromChunk, boolean includeWilderness, boolean affectsBlocks, boolean affectsEntities) {
		try {
			fullPassesPaused = true;
			return onPosAffectedByAnotherPos(serverData, toWorld, toChunk.x, toChunk.z, fromWorld, fromChunk.x, fromChunk.z, includeWilderness, affectsBlocks, affectsEntities);
		} finally {
			fullPassesPaused = false;
		}
	}

	private boolean onBlockBounds(IServerData<CM, ?> serverData, BlockPos from, BlockPos to, ServerPlayer player) {
		ServerLevel level = player.getLevel();
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		int fromChunkX = from.getX() >> 4;
		int fromChunkZ = from.getZ() >> 4;
		int toChunkX = to.getX() >> 4;
		int toChunkZ = to.getZ() >> 4;
		int minChunkX = Math.min(fromChunkX, toChunkX);
		int minChunkZ = Math.min(fromChunkZ, toChunkZ);
		int maxChunkX = Math.max(fromChunkX, toChunkX);
		int maxChunkZ = Math.max(fromChunkZ, toChunkZ);
		for(int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), chunkX, chunkZ);
				IPlayerConfig config = getClaimConfig(playerConfigs, claim);
				if(!hasChunkAccess(config, player, player.getUUID()))
					return true;
			}
		}
		return false;
	}

	private boolean onBlockBoundsFromAnchor(IServerData<CM, ?> serverData, ServerLevel level, BlockPos from, BlockPos to, BlockPos anchor) {
		IPlayerChunkClaim anchorClaim = claimsManager.get(level.dimension().location(), new ChunkPos(anchor));
		int fromChunkX = from.getX() >> 4;
		int fromChunkZ = from.getZ() >> 4;
		int toChunkX = to.getX() >> 4;
		int toChunkZ = to.getZ() >> 4;
		int minChunkX = Math.min(fromChunkX, toChunkX);
		int minChunkZ = Math.min(fromChunkZ, toChunkZ);
		int maxChunkX = Math.max(fromChunkX, toChunkX);
		int maxChunkZ = Math.max(fromChunkZ, toChunkZ);
		for(int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), chunkX, chunkZ);
				if(onPosAffectedByAnotherPos(serverData, claim, anchorClaim, true, true))
					return true;
			}
		}
		return false;
	}

	public boolean onCreateMod(IServerData<CM, ?> serverData, ServerLevel world, int posChunkX, int posChunkZ, @Nullable BlockPos sourceOrAnchor, boolean checkNeighborBlocks, boolean affectsBlocks, boolean affectsEntities) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim posClaim = claimsManager.get(world.dimension().location(), posChunkX, posChunkZ);
		if(posClaim == null)//wilderness not protected
			return false;
		if(sourceOrAnchor == null)
			return onPosAffectedByAnotherPos(serverData, posClaim, null, affectsBlocks, affectsEntities);

		int anchorChunkRelativeX = sourceOrAnchor.getX() & 15;
		int anchorChunkRelativeZ = sourceOrAnchor.getZ() & 15;
		int anchorChunkX = sourceOrAnchor.getX() >> 4;
		int anchorChunkZ = sourceOrAnchor.getZ() >> 4;
		if(!checkNeighborBlocks || !isOnChunkEdge(sourceOrAnchor))
			return onPosAffectedByAnotherPos(serverData, world, posClaim, posChunkX, posChunkZ, anchorChunkX, anchorChunkZ, affectsBlocks, affectsEntities);

		//checking neighbor blocks as the effective anchor positions because the anchor is often offset by 1 block
		int fromChunkOffX = anchorChunkRelativeX == 0 ? -1 : 0;
		int toChunkOffX = anchorChunkRelativeX == 15 ? 1 : 0;
		int fromChunkOffZ = anchorChunkRelativeZ == 0 ? -1 : 0;
		int toChunkOffZ = anchorChunkRelativeZ == 15 ? 1 : 0;
		for(int offX = fromChunkOffX; offX <= toChunkOffX; offX++)
			for(int offZ = fromChunkOffZ; offZ <= toChunkOffZ; offZ++){
				int effectiveAnchorChunkX = anchorChunkX + offX;
				int effectiveAnchorChunkZ = anchorChunkZ + offZ;
				if(onPosAffectedByAnotherPos(serverData, world, posClaim, posChunkX, posChunkZ, effectiveAnchorChunkX, effectiveAnchorChunkZ, affectsBlocks, affectsEntities))
					return true;
			}
		return false;
	}

	public <E> boolean onCreateModAffectPositionedObjects(IServerData<CM, ?> serverData, ServerLevel world, List<E> objects, Function<E, ChunkPos> positionGetter, BlockPos contraptionAnchor, boolean checkNeighborBlocks, boolean removeInvalid, boolean affectsBlocks, boolean affectsEntities) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		Iterator<E> objectIterator = objects.iterator();
		if(!objectIterator.hasNext())
			return false;
		HashMap<ChunkPos, Boolean> chunkCache = new HashMap<>();
		boolean result = false;
		while(objectIterator.hasNext()){
			E object = objectIterator.next();
			ChunkPos objectChunkPos = positionGetter.apply(object);
			Boolean cachedValue = chunkCache.get(objectChunkPos);
			if(cachedValue != null) {
				if(cachedValue)
					objectIterator.remove();
				continue;
			}
			boolean shouldProtect = onCreateMod(serverData, world, objectChunkPos.x, objectChunkPos.z, contraptionAnchor, checkNeighborBlocks, affectsBlocks, affectsEntities);
			if(shouldProtect) {
				result = true;
				if(!removeInvalid)
					break;
				objectIterator.remove();
			}
			chunkCache.put(objectChunkPos, shouldProtect);
		}
		return result;
	}

	public boolean onCreateGlueSelection(IServerData<CM, ?> serverData, BlockPos from, BlockPos to, ServerPlayer player) {
		if(onBlockBounds(serverData, from, to, player)){
			player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, CANT_USE_SUPER_GLUE));
			return true;
		}
		return false;
	}

	public boolean onCreateGlueRemoval(IServerData<CM, ?> serverData, int entityId, ServerPlayer player) {
		ServerLevel level = player.getLevel();
		Entity superGlueEntity = level.getEntity(entityId);
		return superGlueEntity != null && onCreateGlueEntity(serverData, superGlueEntity, player);
	}

	public boolean onCreateGlueEntity(IServerData<CM, ?> serverData, Entity superGlueEntity, ServerPlayer player) {
		AABB boundingBox = superGlueEntity.getBoundingBox();
		BlockPos minPos = new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		BlockPos maxPos = new BlockPos(boundingBox.maxX - 1, boundingBox.maxY - 1, boundingBox.maxZ - 1);
		if(onBlockBounds(serverData, minPos, maxPos, player)){
			player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, CANT_REMOVE_SUPER_GLUE));
			return true;
		}
		return false;
	}

	public boolean onCreateGlueEntityFromAnchor(IServerData<CM, ?> serverData, Entity superGlueEntity, BlockPos anchor) {
		ServerLevel level = ServerLevelHelper.getServerLevel(superGlueEntity.getLevel());
		AABB boundingBox = superGlueEntity.getBoundingBox();
		BlockPos minPos = new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		BlockPos maxPos = new BlockPos(boundingBox.maxX - 1, boundingBox.maxY - 1, boundingBox.maxZ - 1);
		int fromChunkX = minPos.getX() >> 4;
		int fromChunkZ = minPos.getZ() >> 4;
		int toChunkX = maxPos.getX() >> 4;
		int toChunkZ = maxPos.getZ() >> 4;
		int minChunkX = Math.min(fromChunkX, toChunkX);
		int minChunkZ = Math.min(fromChunkZ, toChunkZ);
		int maxChunkX = Math.max(fromChunkX, toChunkX);
		int maxChunkZ = Math.max(fromChunkZ, toChunkZ);
		for(int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
				if(onCreateMod(serverData, level, chunkX, chunkZ, anchor, true, true, false))
					return true;
		return false;
	}

	public void updateTagExceptions(MinecraftServer server){
		friendlyEntityList.updateTagExceptions(server);
		hostileEntityList.updateTagExceptions(server);
		forcedInteractionExceptionBlocks.updateTagExceptions(server);
		forcedBreakExceptionBlocks.updateTagExceptions(server);
		requiresEmptyHandBlocks.updateTagExceptions(server);
		forcedAllowAnyItemBlocks.updateTagExceptions(server);
		forcedInteractionExceptionEntities.updateTagExceptions(server);
		forcedKillExceptionEntities.updateTagExceptions(server);
		requiresEmptyHandEntities.updateTagExceptions(server);
		forcedAllowAnyItemEntities.updateTagExceptions(server);
		forcedEntityClaimBarrierList.updateTagExceptions(server);
		entitiesAllowedToGrief.updateTagExceptions(server);
		entitiesAllowedToGriefEntities.updateTagExceptions(server);
		entitiesAllowedToGriefDroppedItems.updateTagExceptions(server);
		nonBlockGriefingMobs.updateTagExceptions(server);
		entityGriefingMobs.updateTagExceptions(server);
		droppedItemGriefingMobs.updateTagExceptions(server);
		additionalBannedItems.updateTagExceptions(server);
		itemUseProtectionExceptions.updateTagExceptions(server);
		completelyDisabledItems.updateTagExceptions(server);
		completelyDisabledBlocks.updateTagExceptions(server);
		completelyDisabledEntities.updateTagExceptions(server);
		blockExceptionGroups.values().forEach(g -> g.updateTagExceptions(server));
		entityExceptionGroups.values().forEach(g -> g.updateTagExceptions(server));
		itemExceptionGroups.values().forEach(g -> g.updateTagExceptions(server));
		entityBarrierGroups.values().forEach(g -> g.updateTagExceptions(server));
	}

	public void onServerTick(){
		cantPickupItemsInTickCache.clear();
		cantPickupXPInTickCache.clear();
	}

	public enum InteractionTargetResult {
		PROTECT,
		ALLOW,//allow action without checking item protection
		PASS;//pass to item protection check
	}

	public static final class Builder
	<
		CM extends IServerClaimsManager<?, ?, ?>
	> {

		private MinecraftServer server;
		private CM claimsManager;
		private IPlayerPartySystemManager playerPartySystemManager;
		private Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> blockAccessEntityGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityAccessEntityGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> droppedItemAccessEntityGroups;

		private Builder(){
		}

		public Builder<CM> setDefault(){
			setServer(null);
			setClaimsManager(null);
			setPlayerPartySystemManager(null);
			return this;
		}

		public Builder<CM> setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder<CM> setClaimsManager(CM claimsManager) {
			this.claimsManager = claimsManager;
			return this;
		}

		public Builder<CM> setPlayerPartySystemManager(IPlayerPartySystemManager playerPartySystemManager) {
			this.playerPartySystemManager = playerPartySystemManager;
			return this;
		}

		public Builder<CM> setBlockExceptionGroups(Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups) {
			this.blockExceptionGroups = blockExceptionGroups;
			return this;
		}

		public Builder<CM> setEntityExceptionGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups) {
			this.entityExceptionGroups = entityExceptionGroups;
			return this;
		}

		public Builder<CM> setItemExceptionGroups(Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups) {
			this.itemExceptionGroups = itemExceptionGroups;
			return this;
		}

		public Builder<CM> setEntityBarrierGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups) {
			this.entityBarrierGroups = entityBarrierGroups;
			return this;
		}

		public Builder<CM> setBlockAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> blockAccessEntityGroups) {
			this.blockAccessEntityGroups = blockAccessEntityGroups;
			return this;
		}

		public Builder<CM> setEntityAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityAccessEntityGroups) {
			this.entityAccessEntityGroups = entityAccessEntityGroups;
			return this;
		}

		public Builder<CM> setDroppedItemAccessEntityGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> droppedItemAccessEntityGroups) {
			this.droppedItemAccessEntityGroups = droppedItemAccessEntityGroups;
			return this;
		}

		public ChunkProtection<CM> build(){
			if(server == null || claimsManager == null || playerPartySystemManager == null ||
					blockExceptionGroups == null || entityExceptionGroups == null ||
					itemExceptionGroups == null || entityBarrierGroups == null || blockAccessEntityGroups == null ||
					entityAccessEntityGroups == null || droppedItemAccessEntityGroups == null
			)
				throw new IllegalStateException();
			ChunkProtectionExceptionSet.Builder<EntityType<?>> friendlyEntityList =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> hostileEntityList =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<Block> forcedInteractionExceptionBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.BLOCK);
			ChunkProtectionExceptionSet.Builder<Block> forcedBreakExceptionBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.BLOCK);
			ChunkProtectionExceptionSet.Builder<Block> requiresEmptyHandBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.BLOCK);
			ChunkProtectionExceptionSet.Builder<Block> forcedAllowAnyItemBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.BLOCK);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedInteractionExceptionEntities =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedKillExceptionEntities =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> requiresEmptyHandEntitiesBuilder =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedAllowAnyItemEntitiesBuilder =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedEntityClaimBarrierList =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> entitiesAllowedToGrief =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> entitiesAllowedToGriefEntities =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> entitiesAllowedToGriefDroppedItems =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> nonBlockGriefingMobs =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> entityGriefingMobs =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> droppedItemGriefingMobs =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);
			ChunkProtectionExceptionSet.Builder<Item> additionalBannedItems =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ITEM);
			ChunkProtectionExceptionSet.Builder<Item> itemUseProtectionExceptions =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ITEM);
			ChunkProtectionExceptionSet.Builder<Item> completelyDisabledItems =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ITEM);
			ChunkProtectionExceptionSet.Builder<Block> completelyDisabledBlocks =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.BLOCK);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> completelyDisabledEntities =
					ChunkProtectionExceptionSet.Builder.begin(ExceptionElementType.ENTITY_TYPE);

			WildcardResolver wildcardResolver = new WildcardResolver();
			onExceptionList(server, ServerConfig.CONFIG.friendlyChunkProtectedEntityList,
					friendlyEntityList::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.hostileChunkProtectedEntityList,
					hostileEntityList::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.forcedBlockProtectionExceptionList,
					forcedInteractionExceptionBlocksBuilder::addEither, forcedBreakExceptionBlocksBuilder::addEither,
					o -> {
						forcedInteractionExceptionBlocksBuilder.addEither(o);
						requiresEmptyHandBlocksBuilder.addEither(o);
					},
					forcedAllowAnyItemBlocksBuilder::addEither,
					ExceptionElementType.BLOCK, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.forcedEntityProtectionExceptionList,
					forcedInteractionExceptionEntities::addEither,forcedKillExceptionEntities::addEither,
					o -> {
						forcedInteractionExceptionEntities.addEither(o);
						requiresEmptyHandEntitiesBuilder.addEither(o);
					},
					forcedAllowAnyItemEntitiesBuilder::addEither,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.forcedEntityClaimBarrierList,
					forcedEntityClaimBarrierList::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.entitiesAllowedToGrief,
					entitiesAllowedToGrief::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.entitiesAllowedToGriefEntities,
					entitiesAllowedToGriefEntities::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.entitiesAllowedToGriefDroppedItems,
					entitiesAllowedToGriefDroppedItems::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.nonBlockGriefingMobs,
					nonBlockGriefingMobs::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.entityGriefingMobs,
					entityGriefingMobs::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.droppedItemGriefingMobs,
					droppedItemGriefingMobs::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.additionalBannedItemsList,
					additionalBannedItems::addEither, null, null, null,
					ExceptionElementType.ITEM, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.itemUseProtectionExceptionList,
					itemUseProtectionExceptions::addEither, null, null, null,
					ExceptionElementType.ITEM, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.completelyDisabledItemInteractions,
					completelyDisabledItems::addEither, null, null, null,
					ExceptionElementType.ITEM, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.completelyDisabledBlockInteractions,
					completelyDisabledBlocks::addEither, null, null, null,
					ExceptionElementType.BLOCK, wildcardResolver
			);
			onExceptionList(server, ServerConfig.CONFIG.completelyDisabledEntityInteractions,
					completelyDisabledEntities::addEither, null, null, null,
					ExceptionElementType.ENTITY_TYPE, wildcardResolver
			);
			Set<String> staticFakePlayerUsernames = new HashSet<>();
			Set<UUID> staticFakePlayerIds = new HashSet<>();
			ServerConfig.CONFIG.staticFakePlayers.get().forEach(e -> {
				try {
					staticFakePlayerIds.add(UUID.fromString(e));
				} catch(IllegalArgumentException iae){
					staticFakePlayerUsernames.add(e);
				}
			});
			Set<Class<?>> staticFakePlayerClassExceptions = new HashSet<>();
			ServerConfig.CONFIG.staticFakePlayerClassExceptions.get().forEach(e -> {
				try {
					staticFakePlayerClassExceptions.add(Class.forName(e));
				} catch (ClassNotFoundException ignored) {
				}
			});
			Set<UUID> fullPasses = new HashSet<>();
			return new ChunkProtection<>(claimsManager, playerPartySystemManager, new ChunkProtectionEntityHelper(),
					friendlyEntityList.build(), hostileEntityList.build(),
					forcedInteractionExceptionBlocksBuilder.build(), forcedBreakExceptionBlocksBuilder.build(),
					requiresEmptyHandBlocksBuilder.build(), forcedAllowAnyItemBlocksBuilder.build(), completelyDisabledBlocks.build(), forcedInteractionExceptionEntities.build(),
					forcedKillExceptionEntities.build(), requiresEmptyHandEntitiesBuilder.build(), forcedAllowAnyItemEntitiesBuilder.build(), forcedEntityClaimBarrierList.build(), entitiesAllowedToGrief.build(),
					entitiesAllowedToGriefEntities.build(), entitiesAllowedToGriefDroppedItems.build(), nonBlockGriefingMobs.build(), entityGriefingMobs.build(), droppedItemGriefingMobs.build(), staticFakePlayerUsernames, staticFakePlayerIds, staticFakePlayerClassExceptions, additionalBannedItems.build(), completelyDisabledItems.build(),
					itemUseProtectionExceptions.build(), completelyDisabledEntities.build(), blockExceptionGroups, entityExceptionGroups, itemExceptionGroups, entityBarrierGroups, blockAccessEntityGroups, entityAccessEntityGroups, droppedItemAccessEntityGroups, new HashMap<>(), new HashMap<>(), fullPasses);
		}


		private <T> void onExceptionList(MinecraftServer server, ForgeConfigSpec.ConfigValue<List<? extends String>> list, Consumer<Either<T,TagKey<T>>> interactionException,
										 Consumer<Either<T,TagKey<T>>> breakException,
										 Consumer<Either<T,TagKey<T>>> handException,
										 Consumer<Either<T,TagKey<T>>> anythingException,
										 ExceptionElementType<T> elementType,
										 WildcardResolver wildcardResolver){
			Registry<T> elementRegistry = elementType.getRegistry(server);
			Function<ResourceLocation, T> objectGetter = key -> elementRegistry.getOptional(key).orElse(null);
			Iterable<T> iterable = elementType.getIterable();
			Function<T, ResourceLocation> keyGetter = elementRegistry::getKey;
			Function<ResourceLocation, TagKey<T>> objectTagGetter = rl -> TagKey.create(elementRegistry.key(), rl);
			Iterable<TagKey<T>> tagIterable = elementType.getTagIterable();
			Function<TagKey<T>, ResourceLocation> tagKeyGetter = TagKey::location;
			list.get().forEach(s -> onExceptionListElement(
							s, interactionException, breakException, handException, anythingException,
							objectGetter, iterable, keyGetter, objectTagGetter, tagIterable, tagKeyGetter,
							wildcardResolver
					));
		}

		private <T> void onExceptionListElement(String element, Consumer<Either<T,TagKey<T>>> interactionException,
												Consumer<Either<T,TagKey<T>>> breakException,
												Consumer<Either<T,TagKey<T>>> handException,
												Consumer<Either<T,TagKey<T>>> anythingException,
												Function<ResourceLocation, T> objectGetter,
												Iterable<T> iterable,
												Function<T, ResourceLocation> keyGetter,
												Function<ResourceLocation, TagKey<T>> objectTagGetter,
												Iterable<TagKey<T>> tagIterable,
												Function<TagKey<T>, ResourceLocation> tagKeyGetter,
												WildcardResolver wildcardResolver){
			String id = element;
			Consumer<Either<T,TagKey<T>>> destination = interactionException;
			if(element.startsWith(BREAK_PREFIX) || element.startsWith(HAND_PREFIX) || element.startsWith(ANYTHING_PREFIX)){
				id = element.substring(element.indexOf("$") + 1);
				if(element.startsWith(BREAK_PREFIX))
					destination = breakException;
				else if(element.startsWith(ANYTHING_PREFIX))
					destination = anythingException;
				else
					destination = handException;
			}
			if(destination != null) {
				if (!id.startsWith(TAG_PREFIX)) {
					List<T> objects = wildcardResolver.resolveResourceLocations(objectGetter, iterable, keyGetter, id);
					if(objects != null)
						for(T object : objects)
							destination.accept(Either.left(object));
				} else {
					id = id.substring(TAG_PREFIX.length());
					List<TagKey<T>> objectTags = wildcardResolver.resolveResourceLocations(objectTagGetter, tagIterable, tagKeyGetter, id);
					if(objectTags != null)
						for(TagKey<T> objectTag : objectTags)
							destination.accept(Either.right(objectTag));
				}
			}
		}

		public static
		<
			CM extends IServerClaimsManager<?, ?, ?>
		> Builder<CM> begin(){
			return new Builder<CM>().setDefault();
		}

	}

}
