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

package xaero.pac.common.server.claims.protection;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChunkProtection
<
	CM extends IServerClaimsManager<?, ?, ?>,
	M extends IPartyMember, 
	I extends IPartyPlayerInfo,
	P extends IServerParty<M, I, ?>
> {

	public static final UUID CREATE_DEPLOYER_UUID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");

	public static final String TAG_PREFIX = "#";
	public static final String BREAK_PREFIX = "break$";
	public static final String HAND_PREFIX = "hand$";

	private final Component MAIN_HAND = new TranslatableComponent("gui.xaero_claims_protection_main_hand");
	private final Component OFF_HAND = new TranslatableComponent("gui.xaero_claims_protection_off_hand");
	private final Component CANT_INTERACT_BLOCK_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_block", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_block_try_empty", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_DISABLED = new TranslatableComponent("gui.xaero_claims_protection_block_disabled").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_MAIN = new TranslatableComponent("gui.xaero_claims_protection_use_item", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_entity", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_entity_try_empty", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_DISABLED = new TranslatableComponent("gui.xaero_claims_protection_entity_disabled").withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_THIS_CLOSE_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply_too_close", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ITEM_DISABLED_MAIN = new TranslatableComponent("gui.xaero_claims_protection_item_disabled", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_INTERACT_BLOCK_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_block", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_block_try_empty", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_OFF = new TranslatableComponent("gui.xaero_claims_protection_use_item", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_THIS_CLOSE_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply_too_close", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ITEM_DISABLED_OFF = new TranslatableComponent("gui.xaero_claims_protection_item_disabled", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_entity", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_entity_try_empty", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_CHORUS = new TranslatableComponent("gui.xaero_claims_protection_chorus").withStyle(s -> s.withColor(ChatFormatting.RED));

	private final ChunkProtectionEntityHelper entityHelper;
	private final CM claimsManager;
	private final IPartyManager<P> partyManager;
	private final ChunkProtectionExceptionSet<EntityType<?>> friendlyEntityList;
	private final ChunkProtectionExceptionSet<EntityType<?>> hostileEntityList;
	private final ChunkProtectionExceptionSet<Block> forcedInteractionExceptionBlocks;
	private final ChunkProtectionExceptionSet<Block> forcedBreakExceptionBlocks;
	private final ChunkProtectionExceptionSet<Block> requiresEmptyHandBlocks;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedInteractionExceptionEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedKillExceptionEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> requiresEmptyHandEntities;
	private final ChunkProtectionExceptionSet<EntityType<?>> forcedEntityClaimBarrierList;
	private final ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGrief;
	private final ChunkProtectionExceptionSet<Item> additionalBannedItems;
	private final ChunkProtectionExceptionSet<Item> itemUseProtectionExceptions;
	private final ChunkProtectionExceptionSet<Item> completelyDisabledItems;
	private final ChunkProtectionExceptionSet<Block> completelyDisabledBlocks;
	private final ChunkProtectionExceptionSet<EntityType<?>> completelyDisabledEntities;
	private final Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups;
	private final Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups;
	private final Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups;

	private boolean ignoreChunkEnter = false;
	
	private ChunkProtection(CM claimsManager, IPartyManager<P> partyManager, ChunkProtectionEntityHelper entityHelper,
							ChunkProtectionExceptionSet<EntityType<?>> friendlyEntityList,
							ChunkProtectionExceptionSet<EntityType<?>> hostileEntityList,
							ChunkProtectionExceptionSet<Block> forcedInteractionExceptionBlocks,
							ChunkProtectionExceptionSet<Block> forcedBreakExceptionBlocks,
							ChunkProtectionExceptionSet<Block> requiresEmptyHandBlocks,
							ChunkProtectionExceptionSet<Block> completelyDisabledBlocks,
							ChunkProtectionExceptionSet<EntityType<?>> forcedInteractionExceptionEntities,
							ChunkProtectionExceptionSet<EntityType<?>> forcedKillExceptionEntities,
							ChunkProtectionExceptionSet<EntityType<?>> requiresEmptyHandEntities, ChunkProtectionExceptionSet<EntityType<?>> forcedEntityClaimBarrierList,
							ChunkProtectionExceptionSet<EntityType<?>> entitiesAllowedToGrief,
							ChunkProtectionExceptionSet<Item> additionalBannedItems,
							ChunkProtectionExceptionSet<Item> completelyBannedItems,
							ChunkProtectionExceptionSet<Item> itemUseProtectionExceptions, ChunkProtectionExceptionSet<EntityType<?>> completelyDisabledEntities, Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups, Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups) {
		this.claimsManager = claimsManager;
		this.partyManager = partyManager;
		this.entityHelper = entityHelper;
		this.friendlyEntityList = friendlyEntityList;
		this.hostileEntityList = hostileEntityList;
		this.forcedInteractionExceptionBlocks = forcedInteractionExceptionBlocks;
		this.forcedBreakExceptionBlocks = forcedBreakExceptionBlocks;
		this.requiresEmptyHandBlocks = requiresEmptyHandBlocks;
		this.completelyDisabledBlocks = completelyDisabledBlocks;
		this.forcedInteractionExceptionEntities = forcedInteractionExceptionEntities;
		this.forcedKillExceptionEntities = forcedKillExceptionEntities;
		this.requiresEmptyHandEntities = requiresEmptyHandEntities;
		this.forcedEntityClaimBarrierList = forcedEntityClaimBarrierList;
		this.entitiesAllowedToGrief = entitiesAllowedToGrief;
		this.additionalBannedItems = additionalBannedItems;
		this.completelyDisabledItems = completelyBannedItems;
		this.itemUseProtectionExceptions = itemUseProtectionExceptions;
		this.completelyDisabledEntities = completelyDisabledEntities;
		this.blockExceptionGroups = blockExceptionGroups;
		this.entityExceptionGroups = entityExceptionGroups;
		this.itemExceptionGroups = itemExceptionGroups;
		this.entityBarrierGroups = entityBarrierGroups;
	}
	
	private boolean shouldProtectEntity(IPlayerConfig claimConfig, Entity e, Entity from, Entity accessor, UUID accessorId) {
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return false;

		if(e instanceof Player){
			Entity usedOptionBase = claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_REDIRECT) ? accessor : from;
			return usedOptionBase instanceof LivingEntity &&
					(
						usedOptionBase instanceof Player && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS)
					||
						!(usedOptionBase instanceof Player) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS)
					)
				||
					!(usedOptionBase instanceof LivingEntity) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER);
		}
		Entity usedOptionBase = claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_REDIRECT) ? accessor : from;
		return isProtectable(e) && !hasChunkAccess(claimConfig, accessor, accessorId) &&
				(
					usedOptionBase instanceof LivingEntity && !(accessor instanceof Player && entityHelper.isTamed(e, (Player)accessor)) &&
					(
						usedOptionBase instanceof Player && checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS, claimConfig, accessor, accessorId)
					||
						!(usedOptionBase instanceof Player) && checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS, claimConfig, accessor, accessorId)
					)
				||
					!(usedOptionBase instanceof LivingEntity) && checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER, claimConfig, accessor, accessorId)
				||
					accessor instanceof Raider raider && raider.canJoinRaid() && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_RAIDS)//based on the accessor on purpose
				);
	}

	private boolean checkProtectionLeveledOption(IPlayerConfigOptionSpecAPI<Integer> option, IPlayerConfig claimConfig, Entity accessor, UUID accessorId){
		int optionValue = claimConfig.getEffective(option);
		if(optionValue <= 0)
			return false;
		if(optionValue == 1)
			return true;
		int exceptionLevel = getExceptionAccessLevel(claimConfig, accessor, accessorId);
		return exceptionLevel >= optionValue;
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

	private boolean canGriefBlocks(Entity e, IPlayerConfig config, Entity accessor, UUID accessorId){
		IPlayerConfigOptionSpecAPI<Integer> option = e instanceof LivingEntity ? PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS :
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER;
		return !checkProtectionLeveledOption(option, config, accessor, accessorId) || e != null && entitiesAllowedToGrief.contains(e.getType());
	}
	
	public boolean hasChunkAccess(IPlayerConfig claimConfig, Entity accessor, UUID accessorId) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return true;
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return true;
		if(accessor != null) {
			if(accessorId == null)
				accessorId = accessor.getUUID();
			boolean isAPlayer = accessor instanceof Player;
			if (isAPlayer && ServerPlayerDataAPI.from((ServerPlayer) accessor).isClaimsNonallyMode())
				return false;
			if (accessorId.equals(claimConfig.getPlayerId()) ||
					isAPlayer && (ServerPlayerDataAPI.from((ServerPlayer) accessor).isClaimsAdminMode() ||
							ServerPlayerDataAPI.from((ServerPlayer) accessor).isClaimsServerMode() &&
									claimsManager.getPermissionHandler().playerHasServerClaimPermission((ServerPlayer) accessor) &&
									claimConfig.getType() == PlayerConfigType.SERVER
					)
			)
				return true;
			if (!isAPlayer)
				return false;
		} else if(accessorId == null)
			return false;
		if (claimConfig.getPlayerId() == null)
			return false;
		if (claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES))
			return false;
		P claimParty = partyManager.getPartyByMember(claimConfig.getPlayerId());
		if(claimParty == null)
			return false;
		P accessorParty = partyManager.getPartyByMember(accessorId);
		return accessorParty == claimParty && !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) || accessorParty != null && !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES) && claimParty.isAlly(accessorParty.getId());
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
		P claimParty = partyManager.getPartyByMember(claimConfig.getPlayerId());
		if(claimParty == null)
			return 3;//everyone
		P accessorParty = partyManager.getPartyByMember(accessorId);
		if(accessorParty == claimParty)
			return 1;//party
		if(accessorParty != null && claimParty.isAlly(accessorParty.getId()))
			return 2;//allies
		return 3;//everyone
	}
	
	public boolean onLeftClickBlockServer(IServerData<CM,P> serverData, BlockPos pos, Player player) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(player != null && CREATE_DEPLOYER_UUID.equals(player.getUUID()))//uses custom protection
			return false;
		return onBlockAccess(serverData, pos, player, player.getLevel(), InteractionHand.MAIN_HAND, false, true, true, null);
	}
	
	public boolean onPlayerDestroyBlock(IServerData<CM,P> serverData, BlockPos pos, Level world, Player player, boolean direct) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(player != null && CREATE_DEPLOYER_UUID.equals(player.getUUID()))//uses custom protection
			return false;
		return onBlockAccess(serverData, pos, player, world, InteractionHand.MAIN_HAND, false, true, direct, null);
	}

	public boolean onEntityDestroyBlock(IServerData<CM,P> serverData, ServerLevel world, Entity entity, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(pos);
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
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		return !canGriefBlocks(entity, config, accessor, accessorId) && blockAccessCheck(pos, config, accessor, accessorId, world, false, true);
	}
	
	public IPlayerConfig getClaimConfig(IPlayerConfigManager playerConfigs, IPlayerChunkClaim claim) {
		IPlayerConfig mainConfig = playerConfigs.getLoadedConfig(claim == null ? null : claim.getPlayerId());
		if(claim == null)
			return mainConfig;
		return mainConfig.getEffectiveSubConfig(claim.getSubConfigIndex());
	}

	private boolean blockAccessCheck(IServerData<CM,P> serverData, BlockPos pos, Entity entity, UUID entityId, Level world, boolean emptyHand, boolean leftClick) {
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return blockAccessCheck(pos, config, entity, entityId, world, emptyHand, leftClick);
	}
	
	private boolean blockAccessCheck(BlockPos pos, IPlayerConfig config, Entity accessor, UUID accessorId, Level world, boolean emptyHand, boolean leftClick) {
		boolean chunkAccess = hasChunkAccess(config, accessor, accessorId);
		if(chunkAccess)
			return false;
		else {
			Block block = world.getBlockState(pos).getBlock();
			if(leftClick && forcedBreakExceptionBlocks.contains(block) || !leftClick && forcedInteractionExceptionBlocks.contains(block) && (emptyHand || !requiresEmptyHandBlocks.contains(block)))
				return false;
			int exceptionAccessLevel = getExceptionAccessLevel(config, accessor, accessorId);
			for (ChunkProtectionExceptionGroup<Block> group : blockExceptionGroups.values()) {
				if ((group.getType() == ChunkProtectionExceptionType.BREAK) != leftClick)
					continue;
				if(!emptyHand && group.getType() == ChunkProtectionExceptionType.EMPTY_HAND_INTERACTION)
					continue;
				if (exceptionAccessLevel <= config.getEffective(group.getPlayerConfigOption()) && group.contains(block))
					return false;
			}
			return true;
		}
	}
	
	private boolean onBlockAccess(IServerData<CM,P> serverData, BlockPos pos, Player player, Level world, InteractionHand hand, boolean emptyHand, boolean leftClick, boolean direct, Component message) {
		if(blockAccessCheck(serverData, pos, player, null, world, emptyHand, leftClick)) {
			if(direct) {
				player.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_INTERACT_BLOCK_MAIN : CANT_INTERACT_BLOCK_OFF, player.getUUID());
				if (message != null)
					player.sendMessage(message, player.getUUID());
			}
			return true;
		}
		return false;
	}

	public boolean onRightClickBlock(IServerData<CM,P> serverData, Player player, InteractionHand hand, BlockPos pos, BlockHitResult blockHit) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		BlockState blockState = player.getLevel().getBlockState(pos);
		if(completelyDisabledBlocks.contains(blockState.getBlock())){
			player.sendMessage(BLOCK_DISABLED, player.getUUID());
			return true;
		}
		if(CREATE_DEPLOYER_UUID.equals(player.getUUID()))//uses custom protection
			return false;
		ItemStack itemStack = player.getItemInHand(hand);
		boolean emptyHand = itemStack.getItem() == Items.AIR;
		Component message = emptyHand ? null : hand == InteractionHand.MAIN_HAND ? BLOCK_TRY_EMPTY_MAIN : BLOCK_TRY_EMPTY_OFF;
		if(onBlockAccess(serverData, pos, player, player.getLevel(), hand, emptyHand, false, true, message))
			return true;
		return !emptyHand && onUseItemAt(serverData, player, pos, blockHit.getDirection(), itemStack, hand, true);
	}

	public boolean onEntityPlaceBlock(IServerData<CM, P> serverData, Entity entity, ServerLevel level, BlockPos pos, IPlayerConfigOptionSpecAPI<Integer> option) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity != null && CREATE_DEPLOYER_UUID.equals(entity.getUUID()))//uses custom protection
			return false;
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById(level, accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		return (option == null || checkProtectionLeveledOption(option, config, accessor, accessorId)) && (entity instanceof Player || !canGriefBlocks(entity, config, accessor, accessorId))
				&& blockAccessCheck(pos, config, accessor, accessorId, level, false, false);
	}

	public boolean onFrostWalk(IServerData<CM, P> serverData, LivingEntity living, ServerLevel level, BlockPos pos) {
		return onEntityPlaceBlock(serverData, living, level, pos, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_FROST_WALKING);
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
	
	public boolean onItemRightClick(IServerData<CM,P> serverData, InteractionHand hand, ItemStack itemStack, BlockPos pos, Player player, boolean message) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		boolean shouldProtect = false;
		Item item = itemStack.getItem();
		if(completelyDisabledItems.contains(item)) {
			player.sendMessage(hand == InteractionHand.MAIN_HAND ? ITEM_DISABLED_MAIN : ITEM_DISABLED_OFF, player.getUUID());
			return true;
		}
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
					IPlayerChunkClaim claim = claimsManager.get(player.getLevel().dimension().location(), new ChunkPos(chunkPos.x + i, chunkPos.z + j));
					boolean isCurrentChunk = i == 0 && j == 0;
					if (isCurrentChunk || claim != null){//wilderness neighbors don't have to be protected this much
						IPlayerConfig config = getClaimConfig(playerConfigs, claim);
						if((isCurrentChunk || config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE))
								&& !hasChunkAccess(config, player, null)) {
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
		if(message && shouldProtect)
			player.sendMessage(hand == InteractionHand.MAIN_HAND ? USE_ITEM_MAIN : USE_ITEM_OFF, player.getUUID());
		return shouldProtect;
	}
	
	public boolean onMobGrief(IServerData<CM,P> serverData, Entity entity) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entitiesAllowedToGrief.contains(entity.getType()))
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById((ServerLevel) entity.getLevel(), accessorId);
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
					if (!canGriefBlocks(entity, config, accessor, accessorId) && !hasChunkAccess(config, accessor, accessorId))
						return true;
				}
			}
		return false;
	}

	private boolean isEntityException(Entity accessor, UUID accessorId, Entity entity, IPlayerConfig config, boolean emptyHand, boolean attack){
		EntityType<?> entityType = entity.getType();
		if(attack && forcedKillExceptionEntities.contains(entityType) || !attack && forcedInteractionExceptionEntities.contains(entityType) && (emptyHand || !requiresEmptyHandEntities.contains(entityType)))
			return true;
		int exceptionAccessLevel = getExceptionAccessLevel(config, accessor, accessorId);
		for (ChunkProtectionExceptionGroup<EntityType<?>> group : entityExceptionGroups.values()) {
			if ((group.getType() == ChunkProtectionExceptionType.BREAK) != attack)
				continue;
			if(!emptyHand && group.getType() == ChunkProtectionExceptionType.EMPTY_HAND_INTERACTION)
				continue;
			if (exceptionAccessLevel <= config.getEffective(group.getPlayerConfigOption()) && group.contains(entityType))
				return true;
		}
		return false;
	}
	
	public boolean onEntityInteract(IServerData<CM,P> serverData, Entity indirectEntity, Entity entity, Entity target, InteractionHand hand, boolean direct, boolean attack, boolean posSpecific) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(!attack && completelyDisabledEntities.contains(target.getType())){
			if(hand == InteractionHand.MAIN_HAND && entity instanceof Player player)
				player.sendMessage(ENTITY_DISABLED, player.getUUID());
			return true;
		}
		if(entity != null && CREATE_DEPLOYER_UUID.equals(entity.getUUID()))//uses custom protection
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerChunkClaim claim = claimsManager.get(target.getLevel().dimension().location(), target.chunkPosition());
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		ItemStack itemStack = entity instanceof LivingEntity living ? living.getItemInHand(hand) : ItemStack.EMPTY;
		boolean emptyHand = itemStack.getItem() == Items.AIR;
		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(indirectEntity == null ? entity : indirectEntity);//in case the indirect entity has an owner too
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById((ServerLevel) target.getLevel(), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		if(
			target != accessor &&
			(
				shouldProtectEntity(config, target, entity, accessor, accessorId)
			||
				target instanceof Player && accessor instanceof Player &&
				shouldProtectEntity(getClaimConfig(playerConfigs, claimsManager.get(accessor.getLevel().dimension().location(), accessor.chunkPosition())), accessor, accessor == entity ? target : entity, target, null)
			) && !isEntityException(accessor, accessorId, target, config, emptyHand, attack)
		) {
			if(direct && entity instanceof Player) {
				if(attack || posSpecific) {//avoiding double messages
					entity.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_INTERACT_ENTITY_MAIN : CANT_INTERACT_ENTITY_OFF, entity.getUUID());
					if (!attack && !emptyHand) {
						Component message = hand == InteractionHand.MAIN_HAND ? ENTITY_TRY_EMPTY_MAIN : ENTITY_TRY_EMPTY_OFF;
						entity.sendMessage(message, entity.getUUID());
					}
				}
			}
			//OpenPartiesAndClaims.LOGGER.info("stopped {} interacting with {}", entity, target);
			return true;
		}
		return !attack && !emptyHand && onUseItemAt(serverData, entity, target.blockPosition(), null, itemStack, hand, posSpecific);
	}

	public boolean onEntityFire(IServerData<CM, P> serverData, Entity target) {
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

	public void onEntityEnterChunk(IServerData<CM, P> serverData, Entity entity, double goodX, double goodZ, SectionPos newSection, SectionPos oldSection) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		if(ignoreChunkEnter)
			return;
		IPlayerChunkClaim toClaim = claimsManager.get(entity.getLevel().dimension().location(), newSection.x(), newSection.z());
		if(toClaim == null)//wilderness is fine
			return;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, toClaim);

		Entity accessor;
		UUID accessorId;
		Object accessorInfo = getAccessorInfo(entity);
		if (accessorInfo instanceof UUID) {
			accessorId = (UUID) accessorInfo;
			accessor = getEntityById((ServerLevel) entity.getLevel(), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		if(hasChunkAccess(config, accessor, accessorId))
			return;

		IPlayerChunkClaim fromClaim = claimsManager.get(entity.getLevel().dimension().location(), oldSection.x(), oldSection.z());
		boolean isBlockedEntity = forcedEntityClaimBarrierList.contains(entity.getType());
		boolean madeAnException = false;
		IPlayerConfig fromConfig = null;
		if(!isBlockedEntity){
			isBlockedEntity = blockedByBarrierGroups(config, entity, accessor, accessorId);
			if(isBlockedEntity && !hitsAnotherClaim(serverData, fromClaim, toClaim, null)){
				//the "from" claim might be blocking the same entity with a different option, so we don't just check the same one
				fromConfig = getClaimConfig(playerConfigs, fromClaim);
				isBlockedEntity = !blockedByBarrierGroups(fromConfig, entity, accessor, accessorId);
				madeAnException = true;
			}
		} else {
			isBlockedEntity = hitsAnotherClaim(serverData, fromClaim, toClaim, null);
			madeAnException = true;
		}
		if(!isBlockedEntity)
			isBlockedEntity = accessor instanceof Raider raider && raider.canJoinRaid() && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_RAIDS);
		if(!isBlockedEntity && entity instanceof ItemEntity itemEntity) {
			UUID throwerId = itemEntity.getThrower();
			if(throwerId != null) {
				Entity thrower = getEntityById((ServerLevel) itemEntity.getLevel(), throwerId);
				isBlockedEntity = shouldPreventToss(config, itemEntity, thrower, throwerId) != itemEntity;
			}
		}
		if(!isBlockedEntity && madeAnException && accessor != entity){
			//testing if the barrier protection affects the entity's owner
			//this is for cases where a player enters a claim with no player barrier and sends an entity to another claimed chunk
			//of the same owner and barrier protection for the sent entity, but with a player barrier
			//kinda similar to how the main protection option is checked for piston barriers, but a player is the piston here
			isBlockedEntity = blockedByBarrierGroups(config, accessor, accessor, accessorId);
			if(isBlockedEntity){
				if(fromConfig == null)
					fromConfig = getClaimConfig(playerConfigs, fromClaim);
				isBlockedEntity = !blockedByBarrierGroups(fromConfig, accessor, accessor, accessorId);
			}
		}
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
	
	public void onExplosionDetonate(IServerData<CM,P> serverData, ServerLevel world, Explosion explosion, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		DamageSource damageSource = explosion.getDamageSource();
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
							shouldProtectEntity(config, entity, directDamager, damager, null))
					) {
				entities.remove();
			}
		}
	}
	
	public boolean onChorusFruitTeleport(IServerData<CM,P> serverData, Vec3 pos, Entity entity) {
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
			accessor = getEntityById((ServerLevel) entity.getLevel(), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor.getUUID();
		}
		if(checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT, claimConfig, accessor, accessorId) && !hasChunkAccess(claimConfig, accessor, accessorId)) {
			if(entity instanceof Player)
				entity.sendMessage(CANT_CHORUS, entity.getUUID());
			//OpenPartiesAndClaims.LOGGER.info("stopped {} from teleporting to {}", entity, pos);
			return true;
		}
		return false;
	}

	public void onLightningBolt(IServerData<CM,P> serverData, LightningBolt bolt) {
		if(!ServerConfig.CONFIG.claimsEnabled.get() || bolt.getCause() == null)
			return;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		for(int i = -1; i < 2; i++)
			for(int j = -1; j < 2; j++) {
				ChunkPos chunkPos = new ChunkPos(bolt.chunkPosition().x + i, bolt.chunkPosition().z + j);
				IPlayerChunkClaim claim = claimsManager.get(bolt.getLevel().dimension().location(), chunkPos);
				if(i == 0 && j == 0 || claim != null) {//wilderness neighbors don't have to be protected this much
					IPlayerConfig config = getClaimConfig(playerConfigs, claim);
					if (checkProtectionLeveledOption(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING, config, bolt.getCause(), null) && !hasChunkAccess(config, bolt.getCause(), null)) {
						bolt.setVisualOnly(true);
						break;
					}
				}
			}
	}

	public boolean onFireSpread(IServerData<CM,P> serverData, ServerLevel level, BlockPos pos){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig claimConfig = getClaimConfig(playerConfigs, claim);
		return claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD);
	}

	public boolean onCropTrample(IServerData<CM,P> serverData, Entity entity, BlockPos pos) {
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
			accessor = getEntityById((ServerLevel) entity.getLevel(), accessorId);
		} else {
			accessor = (Entity) accessorInfo;
			accessorId = accessor == null ? null : accessor.getUUID();
		}
		return claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE)
				&& !hasChunkAccess(claimConfig, accessor, accessorId);
	}

	public boolean onBucketUse(IServerData<CM, P> serverData, Entity entity, HitResult hitResult, ItemStack itemStack) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity != null && CREATE_DEPLOYER_UUID.equals(entity.getUUID()))//uses custom protection
			return false;
		//just onUseItemAt would work for buckets in "vanilla" as well, but it's better to use the proper bucket event too
		BlockPos pos;
		Direction direction = null;
		if(hitResult instanceof BlockHitResult blockHitResult) {
			pos = blockHitResult.getBlockPos();
			direction = blockHitResult.getDirection();
		} else
			pos = new BlockPos(hitResult.getLocation());
		return onUseItemAt(serverData, entity, pos, direction, itemStack, null, true);
	}

	public boolean onUseItemAt(IServerData<CM, P> serverData, Entity entity, BlockPos pos, Direction direction, ItemStack itemStack, InteractionHand hand, boolean message) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(completelyDisabledItems.contains(itemStack.getItem())) {
			if(entity instanceof Player player)
				player.sendMessage(hand == InteractionHand.MAIN_HAND ? ITEM_DISABLED_MAIN : ITEM_DISABLED_OFF, player.getUUID());
			return true;
		}
		if(entity != null && CREATE_DEPLOYER_UUID.equals(entity.getUUID()))//uses custom protection
			return false;
		if(!isItemUseRestricted(itemStack))
			return false;
		if(entity instanceof Player player) {
			if (hand == null)
				hand = player.getItemInHand(InteractionHand.MAIN_HAND) == itemStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			if (additionalBannedItems.contains(itemStack.getItem()) &&
					onItemRightClick(serverData, hand, itemStack, pos, player, false)) {//only configured items on purpose
				player.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_APPLY_ITEM_THIS_CLOSE_MAIN : CANT_APPLY_ITEM_THIS_CLOSE_OFF, player.getUUID());
				return true;
			}
		}
		BlockPos pos2 = null;
		if(direction != null)
			pos2 = pos.offset(direction.getNormal());
		ChunkPos chunkPos;
		ChunkPos chunkPos2;
		if(applyItemAccessCheck(serverData, chunkPos = new ChunkPos(pos), entity, (ServerLevel) entity.getLevel(), itemStack)
			|| pos2 != null && !(chunkPos2 = new ChunkPos(pos2)).equals(chunkPos) && applyItemAccessCheck(serverData, chunkPos2, entity, (ServerLevel) entity.getLevel(), itemStack)
				){
			if(message && entity instanceof ServerPlayer player)
				player.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_APPLY_ITEM_MAIN : CANT_APPLY_ITEM_OFF, player.getUUID());
			return true;
		}
		return false;
	}

	private boolean applyItemAccessCheck(IServerData<CM,P> serverData, ChunkPos chunkPos, Entity entity, ServerLevel world, ItemStack itemStack) {
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
		return !hasChunkAccess(config, accessor, accessorId) && !isOptionalItemException(serverData, accessor, accessorId, itemStack, entity.getLevel(), chunkPos);
	}

	private boolean isOptionalItemException(IServerData<CM, P> serverData, Entity accessor, UUID accessorId, ItemStack itemStack, Level world, ChunkPos chunkPos){
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

	private boolean hitsAnotherClaim(IServerData<CM, P> serverData, IPlayerChunkClaim fromClaim, IPlayerChunkClaim toClaim, IPlayerConfigOptionSpecAPI<Boolean> optionSpec){
		if(toClaim == null || fromClaim == toClaim || fromClaim != null && fromClaim.isSameClaimType(toClaim))
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig toClaimConfig = getClaimConfig(playerConfigs, toClaim);
		if(!toClaimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS)
				|| optionSpec != null && !toClaimConfig.getEffective(optionSpec))
			return false;
		if(fromClaim != null && fromClaim.getPlayerId().equals(toClaim.getPlayerId())){
			IPlayerConfig fromClaimConfig = getClaimConfig(playerConfigs, fromClaim);
			return !fromClaimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS)
					|| optionSpec != null && !fromClaimConfig.getEffective(optionSpec);
		}
		return true;
	}

	private boolean hitsAnotherClaim(IServerData<CM, P> serverData, ServerLevel level, BlockPos from, BlockPos to, IPlayerConfigOptionSpecAPI<Boolean> optionSpec){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(!isOnChunkEdge(from))
			return false;
		int fromChunkX = from.getX() >> 4;
		int fromChunkZ = from.getZ() >> 4;
		int toChunkX = to.getX() >> 4;
		int toChunkZ = to.getZ() >> 4;
		if(fromChunkX == toChunkX && fromChunkZ == toChunkZ)
			return false;
		IPlayerChunkClaim toClaim = claimsManager.get(level.dimension().location(), toChunkX, toChunkZ);
		IPlayerChunkClaim fromClaim = claimsManager.get(level.dimension().location(), fromChunkX, fromChunkZ);
		return hitsAnotherClaim(serverData, fromClaim, toClaim, optionSpec);
	}

	public boolean onFluidSpread(IServerData<CM, P> serverData, ServerLevel level, BlockPos from, BlockPos to) {
		return hitsAnotherClaim(serverData, level, from, to, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FLUID_BARRIER);
	}

	public boolean onDispenseFrom(IServerData<CM, P> serverData, ServerLevel serverLevel, BlockPos from) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(!isOnChunkEdge(from))
			return false;
		BlockState blockState = serverLevel.getBlockState(from);
		Direction direction = blockState.getValue(DirectionalBlock.FACING);
		BlockPos to = from.relative(direction);
		return hitsAnotherClaim(serverData, serverLevel, from, to, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_DISPENSER_BARRIER);
	}

	private boolean shouldStopPistonPush(IServerData<CM, P> serverData, ServerLevel level, BlockPos pushPos, int pistonChunkX, int pistonChunkZ, IPlayerChunkClaim pistonClaim){
		int pushChunkX = pushPos.getX() >> 4;
		int pushChunkZ = pushPos.getZ() >> 4;
		if(pushChunkX == pistonChunkX && pushChunkZ == pistonChunkZ)
			return false;
		IPlayerChunkClaim pushClaim = claimsManager.get(level.dimension().location(), pushChunkX, pushChunkZ);
		return hitsAnotherClaim(serverData, pistonClaim, pushClaim, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER);
	}

	public boolean onPistonPush(IServerData<CM, P> serverData, ServerLevel level, List<BlockPos> toPush, List<BlockPos> toDestroy, BlockPos pistonPos, Direction direction, boolean extending) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim pistonClaim = claimsManager.get(level.dimension().location(), pistonPos);
		int pistonChunkX = pistonPos.getX() >> 4;
		int pistonChunkZ = pistonPos.getZ() >> 4;
		Direction actualDirection = extending ? direction : direction.getOpposite();
		if(toPush.isEmpty() && toDestroy.isEmpty()) {
			BlockPos pushPos = pistonPos.relative(direction);
			if(shouldStopPistonPush(serverData, level, pushPos, pistonChunkX, pistonChunkZ, pistonClaim))
				return true;
			return shouldStopPistonPush(serverData, level, pushPos.relative(actualDirection), pistonChunkX, pistonChunkZ, pistonClaim);
		}
		Iterator<BlockPos> posIterator = Iterators.concat(toPush.iterator(), toDestroy.iterator());
		while(posIterator.hasNext()){
			BlockPos pushPos = posIterator.next();
			if (shouldStopPistonPush(serverData, level, pushPos, pistonChunkX, pistonChunkZ, pistonClaim))
				return true;
			BlockPos pushedToPos = pushPos.relative(actualDirection);
			if (shouldStopPistonPush(serverData, level, pushedToPos, pistonChunkX, pistonChunkZ, pistonClaim))
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
		} else
			result = entityHelper.getTamer(entity);
		return result == null ? entity : result;
	}

	public void onEntitiesPushBlock(IServerData<CM, P> serverData, ServerLevel world, BlockPos pos, Block block, List<? extends Entity> entities) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		Iterator<? extends Entity> iterator = entities.iterator();
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(!config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return;
		IPlayerConfigOptionSpecAPI<Integer> blockSpecificOption = block instanceof ButtonBlock ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES :
				block instanceof TargetBlock ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_TARGETS_FROM_PROJECTILES :
				null;
		if(blockSpecificOption != null && config.getEffective(blockSpecificOption) <= 0)
			return;
		boolean everyoneExceptAccessHavers = blockSpecificOption != null && config.getEffective(blockSpecificOption) == 1;
		Map<UUID, Set<IPlayerConfigOptionSpecAPI<Integer>>> accessorOptionsToIgnore = null;
		while(iterator.hasNext()){
			Entity e = iterator.next();
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
			if(entitySpecificOption == null)
				entitySpecificOption =
						e instanceof Player ?
							PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLATES_FROM_PLAYERS :
						e instanceof LivingEntity ?
							PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLATES_FROM_MOBS :
							PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLATES_FROM_OTHER;
			Set<IPlayerConfigOptionSpecAPI<Integer>> optionsIgnoredForAccessor;
			if(accessorOptionsToIgnore != null && (optionsIgnoredForAccessor = accessorOptionsToIgnore.get(accessorId)) != null && optionsIgnoredForAccessor.contains(entitySpecificOption)){
				iterator.remove();
				continue;
			}
			if((everyoneExceptAccessHavers || checkProtectionLeveledOption(entitySpecificOption, config, accessor, accessorId)) && !hasChunkAccess(config, accessor, accessorId)){
				if(iterator.hasNext()){
					if(accessorOptionsToIgnore == null)
						accessorOptionsToIgnore = new HashMap<>();
					optionsIgnoredForAccessor = accessorOptionsToIgnore.get(accessorId);
					if(optionsIgnoredForAccessor == null)
						accessorOptionsToIgnore.put(accessorId, optionsIgnoredForAccessor = new HashSet<>());
					optionsIgnoredForAccessor.add(entitySpecificOption);
				}
				iterator.remove();
			} else if(
					blockSpecificOption == PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES ||
					blockSpecificOption == null && !(block instanceof WeightedPressurePlateBlock)
			)
				break;//for these blocks 1 allowed entity is enough info
		}
	}

	public boolean onNetherPortal(IServerData<CM, P> serverData, Entity entity, ServerLevel world, BlockPos pos) {
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

	public boolean onRaidSpawn(IServerData<CM, P> serverData, ServerLevel world, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_RAIDS);
	}

	public boolean onItemAddedToWorld(IServerData<CM, P> serverData, ItemEntity itemEntity) {
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
		Entity thrower = getEntityById((ServerLevel) itemEntity.getLevel(), throwerId);
		Entity result = shouldPreventToss(config, itemEntity, thrower, throwerId);
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

	private Entity shouldPreventToss(IPlayerConfig config, ItemEntity itemEntity, Entity thrower, UUID throwerId){//returns the accessor if protected, or the item entity if not protected
		if(throwerId == null)
			return itemEntity;
		Entity accessor = null;
		UUID accessorId;
		if(thrower != null){
			Object accessorInfo = getAccessorInfo(thrower);
			if (accessorInfo instanceof UUID) {
				accessorId = (UUID) accessorInfo;
				accessor = getEntityById((ServerLevel) itemEntity.getLevel(), accessorId);
			} else {
				accessor = (Entity) accessorInfo;
				accessorId = accessor.getUUID();
			}
		} else
			accessorId = throwerId;
		Entity usedOptionBase = accessor == null || config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_DROP_REDIRECT) ?
				thrower : accessor;
		IPlayerConfigOptionSpecAPI<Integer> option = !(usedOptionBase instanceof LivingEntity) ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_DROP_OTHER
				: usedOptionBase instanceof Player ?
				PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_DROP_PLAYERS
				: PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ITEM_DROP_MOBS;
		if(checkProtectionLeveledOption(option, config, accessor, accessorId) && !hasChunkAccess(config, accessor, accessorId))
			return accessor;
		return itemEntity;
	}

	private Entity getEntityById(ServerLevel world, UUID id){
		Entity result = world.getServer().getPlayerList().getPlayer(id);
		return result != null ? result : world.getEntity(id);
	}

	public boolean onCreateMod(IServerData<CM, P> serverData, ServerLevel level, IPlayerChunkClaim posClaim, int posChunkX, int posChunkZ, int anchorChunkX, int anchorChunkZ) {
		if(posChunkX == anchorChunkX && posChunkZ == anchorChunkZ)
			return false;
		IPlayerChunkClaim anchorClaim = claimsManager.get(level.dimension().location(), anchorChunkX, anchorChunkZ);
		return hitsAnotherClaim(serverData, anchorClaim, posClaim, null);
	}

	public boolean onCreateMod(IServerData<CM, P> serverData, ServerLevel level, int posChunkX, int posChunkZ, @Nullable BlockPos sourceOrAnchor, boolean checkNeighborBlocks, Player player) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim posClaim = claimsManager.get(level.dimension().location(), posChunkX, posChunkZ);
		if(posClaim == null)//wilderness not protected
			return false;
		if(player != null)
			return !hasChunkAccess(getClaimConfig(serverData.getPlayerConfigs(), posClaim), player, null);
		if(sourceOrAnchor == null)
			return hitsAnotherClaim(serverData, null, posClaim, null);

		int anchorChunkRelativeX = sourceOrAnchor.getX() & 15;
		int anchorChunkRelativeZ = sourceOrAnchor.getZ() & 15;
		int anchorChunkX = sourceOrAnchor.getX() >> 4;
		int anchorChunkZ = sourceOrAnchor.getZ() >> 4;
		if(!checkNeighborBlocks || !isOnChunkEdge(sourceOrAnchor))
			return onCreateMod(serverData, level, posClaim, posChunkX, posChunkZ, anchorChunkX, anchorChunkZ);

		//checking neighbor blocks as the effective anchor positions because the anchor is often offset by 1 block
		int fromChunkOffX = anchorChunkRelativeX == 0 ? -1 : 0;
		int toChunkOffX = anchorChunkRelativeX == 15 ? 1 : 0;
		int fromChunkOffZ = anchorChunkRelativeZ == 0 ? -1 : 0;
		int toChunkOffZ = anchorChunkRelativeZ == 15 ? 1 : 0;
		for(int offX = fromChunkOffX; offX <= toChunkOffX; offX++)
			for(int offZ = fromChunkOffZ; offZ <= toChunkOffZ; offZ++){
				int effectiveAnchorChunkX = anchorChunkX + offX;
				int effectiveAnchorChunkZ = anchorChunkZ + offZ;
				if(onCreateMod(serverData, level, posClaim, posChunkX, posChunkZ, effectiveAnchorChunkX, effectiveAnchorChunkZ))
					return true;
			}
		return false;
	}

	public <E> boolean onCreateModAffectPositionedObjects(IServerData<CM, P> serverData, Level level, List<E> objects, Function<E, ChunkPos> positionGetter, BlockPos contraptionAnchor, boolean checkNeighborBlocks, boolean removeInvalid) {
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
			boolean shouldProtect = onCreateMod(serverData, (ServerLevel) level, objectChunkPos.x, objectChunkPos.z, contraptionAnchor, checkNeighborBlocks, null);
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

	public void updateTagExceptions(){
		friendlyEntityList.updateTagExceptions();
		hostileEntityList.updateTagExceptions();
		forcedInteractionExceptionBlocks.updateTagExceptions();
		forcedBreakExceptionBlocks.updateTagExceptions();
		requiresEmptyHandBlocks.updateTagExceptions();
		forcedInteractionExceptionEntities.updateTagExceptions();
		forcedKillExceptionEntities.updateTagExceptions();
		forcedEntityClaimBarrierList.updateTagExceptions();
		entitiesAllowedToGrief.updateTagExceptions();
		additionalBannedItems.updateTagExceptions();
		itemUseProtectionExceptions.updateTagExceptions();
		completelyDisabledItems.updateTagExceptions();
		completelyDisabledBlocks.updateTagExceptions();
		completelyDisabledEntities.updateTagExceptions();
		blockExceptionGroups.values().forEach(ChunkProtectionExceptionGroup::updateTagExceptions);
		entityExceptionGroups.values().forEach(ChunkProtectionExceptionGroup::updateTagExceptions);
		itemExceptionGroups.values().forEach(ChunkProtectionExceptionGroup::updateTagExceptions);
		entityBarrierGroups.values().forEach(ChunkProtectionExceptionGroup::updateTagExceptions);
	}
	public static final class Builder
	<
		CM extends IServerClaimsManager<?, ?, ?>,
		M extends IPartyMember,
		I extends IPartyPlayerInfo,
		P extends IServerParty<M, I, ?>
	> {

		private CM claimsManager;
		private IPartyManager<P> partyManager;
		private Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups;
		private Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups;

		private Builder(){
		}

		public Builder<CM,M,I,P> setDefault(){
			setClaimsManager(null);
			setPartyManager(null);
			return this;
		}

		public Builder<CM,M,I,P> setClaimsManager(CM claimsManager) {
			this.claimsManager = claimsManager;
			return this;
		}

		public Builder<CM,M,I,P> setPartyManager(IPartyManager<P> partyManager) {
			this.partyManager = partyManager;
			return this;
		}

		public Builder<CM,M,I,P> setBlockExceptionGroups(Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups) {
			this.blockExceptionGroups = blockExceptionGroups;
			return this;
		}

		public Builder<CM,M,I,P> setEntityExceptionGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups) {
			this.entityExceptionGroups = entityExceptionGroups;
			return this;
		}

		public Builder<CM,M,I,P> setItemExceptionGroups(Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups) {
			this.itemExceptionGroups = itemExceptionGroups;
			return this;
		}

		public Builder<CM,M,I,P> setEntityBarrierGroups(Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups) {
			this.entityBarrierGroups = entityBarrierGroups;
			return this;
		}

		public ChunkProtection<CM,M,I,P> build(){
			if(claimsManager == null || partyManager == null ||
					blockExceptionGroups == null || entityExceptionGroups == null || itemExceptionGroups == null || entityBarrierGroups == null)
				throw new IllegalStateException();
			ChunkProtectionExceptionSet.Builder<EntityType<?>> friendlyEntityList =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> hostileEntityList =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Block> forcedInteractionExceptionBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.<Block>begin().setTagStreamGetter(Services.PLATFORM.getBlockRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Block> forcedBreakExceptionBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.<Block>begin().setTagStreamGetter(Services.PLATFORM.getBlockRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Block> requiresEmptyHandBlocksBuilder =
					ChunkProtectionExceptionSet.Builder.<Block>begin().setTagStreamGetter(Services.PLATFORM.getBlockRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedInteractionExceptionEntities =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedKillExceptionEntities =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> requiresEmptyHandEntitiesBuilder =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> forcedEntityClaimBarrierList =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> entitiesAllowedToGrief =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Item> additionalBannedItems =
					ChunkProtectionExceptionSet.Builder.<Item>begin().setTagStreamGetter(Services.PLATFORM.getItemRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Item> itemUseProtectionExceptions =
					ChunkProtectionExceptionSet.Builder.<Item>begin().setTagStreamGetter(Services.PLATFORM.getItemRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Item> completelyDisabledItems =
					ChunkProtectionExceptionSet.Builder.<Item>begin().setTagStreamGetter(Services.PLATFORM.getItemRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<Block> completelyDisabledBlocks =
					ChunkProtectionExceptionSet.Builder.<Block>begin().setTagStreamGetter(Services.PLATFORM.getBlockRegistry()::getTagStream);
			ChunkProtectionExceptionSet.Builder<EntityType<?>> completelyDisabledEntities =
					ChunkProtectionExceptionSet.Builder.<EntityType<?>>begin().setTagStreamGetter(Services.PLATFORM.getEntityRegistry()::getTagStream);

			Function<ResourceLocation, Block> blockGetter = Services.PLATFORM.getBlockRegistry()::getValue;
			Function<ResourceLocation, TagKey<Block>> blockTagGetter = rl -> TagKey.create(Registry.BLOCK_REGISTRY, rl);
			Function<ResourceLocation, EntityType<?>> entityGetter = Services.PLATFORM.getEntityRegistry()::getValue;
			Function<ResourceLocation, TagKey<EntityType<?>>> entityTagGetter = rl -> TagKey.create(Registry.ENTITY_TYPE_REGISTRY, rl);
			Function<ResourceLocation, Item> itemGetter = Services.PLATFORM.getItemRegistry()::getValue;
			Function<ResourceLocation, TagKey<Item>> itemTagGetter = rl -> TagKey.create(Registry.ITEM_REGISTRY, rl);
			ServerConfig.CONFIG.friendlyChunkProtectedEntityList.get().forEach(s -> onExceptionListElement(
					s,
					friendlyEntityList::addEither, null, null,
					entityGetter, entityTagGetter
			));
			ServerConfig.CONFIG.hostileChunkProtectedEntityList.get().forEach(s -> onExceptionListElement(
					s,
					hostileEntityList::addEither, null, null,
					entityGetter, entityTagGetter
			));
			ServerConfig.CONFIG.forcedBlockProtectionExceptionList.get()
					.forEach(s -> onExceptionListElement(
							s,
							forcedInteractionExceptionBlocksBuilder::addEither, forcedBreakExceptionBlocksBuilder::addEither,
							o -> {
								forcedInteractionExceptionBlocksBuilder.addEither(o);
								requiresEmptyHandBlocksBuilder.addEither(o);
							}, blockGetter, blockTagGetter
					));
			ServerConfig.CONFIG.forcedEntityProtectionExceptionList.get()
					.forEach(s -> onExceptionListElement(
							s,
							forcedInteractionExceptionEntities::addEither,forcedKillExceptionEntities::addEither,
							o -> {
								forcedInteractionExceptionEntities.addEither(o);
								requiresEmptyHandEntitiesBuilder.addEither(o);
							}, entityGetter, entityTagGetter
					));
			ServerConfig.CONFIG.forcedEntityClaimBarrierList.get()
					.forEach(s -> onExceptionListElement(
							s, forcedEntityClaimBarrierList::addEither, null, null,
							entityGetter, entityTagGetter
					));
			ServerConfig.CONFIG.entitiesAllowedToGrief.get()
					.forEach(s -> onExceptionListElement(
							s,
							entitiesAllowedToGrief::addEither, null, null,
							entityGetter, entityTagGetter
					));
			ServerConfig.CONFIG.additionalBannedItemsList.get().forEach(s -> onExceptionListElement(
							s,
							additionalBannedItems::addEither, null, null,
							itemGetter, itemTagGetter
					));
			ServerConfig.CONFIG.itemUseProtectionExceptionList.get().forEach(s -> onExceptionListElement(
							s,
							itemUseProtectionExceptions::addEither, null, null,
							itemGetter, itemTagGetter
					));
			ServerConfig.CONFIG.completelyDisabledItemInteractions.get().forEach(s -> onExceptionListElement(
							s,
							completelyDisabledItems::addEither, null, null,
							itemGetter, itemTagGetter
					));
			ServerConfig.CONFIG.completelyDisabledBlockInteractions.get().forEach(s -> onExceptionListElement(
							s,
							completelyDisabledBlocks::addEither, null, null,
							blockGetter, blockTagGetter
					));
			ServerConfig.CONFIG.completelyDisabledEntityInteractions.get().forEach(s -> onExceptionListElement(
							s,
							completelyDisabledEntities::addEither, null, null,
							entityGetter, entityTagGetter
					));
			return new ChunkProtection<>(claimsManager, partyManager, new ChunkProtectionEntityHelper(),
					friendlyEntityList.build(), hostileEntityList.build(),
					forcedInteractionExceptionBlocksBuilder.build(), forcedBreakExceptionBlocksBuilder.build(),
					requiresEmptyHandBlocksBuilder.build(), completelyDisabledBlocks.build(), forcedInteractionExceptionEntities.build(),
					forcedKillExceptionEntities.build(), requiresEmptyHandEntitiesBuilder.build(), forcedEntityClaimBarrierList.build(), entitiesAllowedToGrief.build(),
					additionalBannedItems.build(), completelyDisabledItems.build(),
					itemUseProtectionExceptions.build(), completelyDisabledEntities.build(), blockExceptionGroups, entityExceptionGroups, itemExceptionGroups, entityBarrierGroups);
		}

		private <T> void onExceptionListElement(String element, Consumer<Either<T,TagKey<T>>> interactionException,
												Consumer<Either<T,TagKey<T>>> breakException,
												Consumer<Either<T,TagKey<T>>> handException,
												Function<ResourceLocation, T> objectGetter,
												Function<ResourceLocation, TagKey<T>> objectTagGetter){
			String id = element;
			Consumer<Either<T,TagKey<T>>> destination = interactionException;
			if(element.startsWith(BREAK_PREFIX) || element.startsWith(HAND_PREFIX)){
				id = element.substring(element.indexOf("$") + 1);
				if(element.startsWith(BREAK_PREFIX))
					destination = breakException;
				else
					destination = handException;
			}
			if(destination != null) {
				if (!id.startsWith(TAG_PREFIX)) {
					T object = objectGetter.apply(new ResourceLocation(id));
					if (object != null)
						destination.accept(Either.left(object));
				} else {
					id = id.substring(TAG_PREFIX.length());
					TagKey<T> objectTag = objectTagGetter.apply(new ResourceLocation(id));
					if (objectTag != null)
						destination.accept(Either.right(objectTag));
				}
			}
		}

		public static
		<
			CM extends IServerClaimsManager<?, ?, ?>,
			M extends IPartyMember,
			I extends IPartyPlayerInfo,
			P extends IServerParty<M, I, ?>
		> Builder<CM,M,I,P> begin(){
			return new Builder<CM,M,I,P>().setDefault();
		}

	}

}
