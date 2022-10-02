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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
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
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
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
	P extends IServerParty<M, I>
> {

	public static final UUID CREATE_DEPLOYER_UUID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");

	private static final String FORCE_PREFIX = "force$";
	private static final String BREAK_PREFIX = "break$";
	private static final String FORCE_BREAK_PREFIX = "force_break$";
	private static final String HAND_PREFIX = "hand$";
	private static final String FORCE_HAND_PREFIX = "force_hand$";

	private final Component MAIN_HAND = new TranslatableComponent("gui.xaero_claims_protection_main_hand");
	private final Component OFF_HAND = new TranslatableComponent("gui.xaero_claims_protection_off_hand");
	private final Component CANT_INTERACT_BLOCK_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_block", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_block_try_empty", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_MAIN = new TranslatableComponent("gui.xaero_claims_protection_use_item", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_entity", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_entity_try_empty", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_THIS_CLOSE_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply_too_close", MAIN_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_INTERACT_BLOCK_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_block", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_block_try_empty", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_OFF = new TranslatableComponent("gui.xaero_claims_protection_use_item", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_APPLY_ITEM_THIS_CLOSE_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_item_apply_too_close", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_entity", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_entity_try_empty", OFF_HAND).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_CHORUS = new TranslatableComponent("gui.xaero_claims_protection_chorus").withStyle(s -> s.withColor(ChatFormatting.RED));

	private final ChunkProtectionEntityHelper entityHelper;
	private final CM claimsManager;
	private final IPartyManager<P> partyManager;
	private final Set<EntityType<?>> friendlyEntityList;
	private final Set<EntityType<?>> hostileEntityList;
	private final Set<Block> optionalInteractionExceptionBlocks;
	private final Set<Block> optionalBreakExceptionBlocks;
	private final Set<Block> forcedInteractionExceptionBlocks;
	private final Set<Block> forcedBreakExceptionBlocks;
	private final Set<Block> requiresEmptyHandBlocks;
	private final Set<EntityType<?>> optionalEmptyHandExceptionEntities;
	private final Set<EntityType<?>> optionalKillExceptionEntities;
	private final Set<EntityType<?>> forcedEmptyHandExceptionEntities;
	private final Set<EntityType<?>> forcedKillExceptionEntities;
	private final Set<EntityType<?>> optionalEntityClaimBarrierList;
	private final Set<EntityType<?>> forcedEntityClaimBarrierList;
	private final Set<EntityType<?>> entitiesAllowedToGrief;
	private final Set<Item> additionalBannedItems;
	private final Set<Item> itemUseProtectionExceptions;

	private boolean ignoreChunkEnter = false;
	
	private ChunkProtection(CM claimsManager, IPartyManager<P> partyManager, ChunkProtectionEntityHelper entityHelper, Set<EntityType<?>> friendlyEntityList, Set<EntityType<?>> hostileEntityList, Set<Block> optionalInteractionExceptionBlocks, Set<Block> optionalBreakExceptionBlocks, Set<Block> forcedInteractionExceptionBlocks, Set<Block> forcedBreakExceptionBlocks, Set<Block> requiresEmptyHandBlocks, Set<EntityType<?>> optionalEmptyHandExceptionEntities, Set<EntityType<?>> optionalKillExceptionEntities, Set<EntityType<?>> forcedEmptyHandExceptionEntities, Set<EntityType<?>> forcedKillExceptionEntities, Set<EntityType<?>> optionalEntityClaimBarrierList, Set<EntityType<?>> forcedEntityClaimBarrierList, Set<EntityType<?>> entitiesAllowedToGrief, Set<Item> additionalBannedItems, Set<Item> itemUseProtectionExceptions) {
		this.claimsManager = claimsManager;
		this.partyManager = partyManager;
		this.entityHelper = entityHelper;
		this.friendlyEntityList = friendlyEntityList;
		this.hostileEntityList = hostileEntityList;
		this.optionalInteractionExceptionBlocks = optionalInteractionExceptionBlocks;
		this.optionalBreakExceptionBlocks = optionalBreakExceptionBlocks;
		this.forcedInteractionExceptionBlocks = forcedInteractionExceptionBlocks;
		this.forcedBreakExceptionBlocks = forcedBreakExceptionBlocks;
		this.requiresEmptyHandBlocks = requiresEmptyHandBlocks;
		this.optionalEmptyHandExceptionEntities = optionalEmptyHandExceptionEntities;
		this.optionalKillExceptionEntities = optionalKillExceptionEntities;
		this.forcedEmptyHandExceptionEntities = forcedEmptyHandExceptionEntities;
		this.forcedKillExceptionEntities = forcedKillExceptionEntities;
		this.optionalEntityClaimBarrierList = optionalEntityClaimBarrierList;
		this.forcedEntityClaimBarrierList = forcedEntityClaimBarrierList;
		this.entitiesAllowedToGrief = entitiesAllowedToGrief;
		this.additionalBannedItems = additionalBannedItems;
		this.itemUseProtectionExceptions = itemUseProtectionExceptions;
	}
	
	private boolean shouldProtectEntity(IPlayerConfig claimConfig, Entity e, Entity from) {
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return false;
		return isProtectable(e) &&
				(
					from != null && !(from instanceof Player && entityHelper.isTamed(e, (Player)from)) &&
					(
						from instanceof LivingEntity &&
						(
							from instanceof Player && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS) && !hasChunkAccess(claimConfig, from)
						||
							!(from instanceof Player) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS)
						)
					)
				||
					!(from instanceof LivingEntity) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_ANONYMOUS_ATTACKS)
				);
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

	private boolean canGriefBlocks(Entity e, IPlayerConfig config){
		return !config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_MOB_GRIEFING) || entitiesAllowedToGrief.contains(e.getType());
	}
	
	public boolean hasChunkAccess(IPlayerConfig claimConfig, Entity e) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return true;
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
			return true;
		if(e == null)
			return false;
		if(e instanceof Player && ServerPlayerDataAPI.from((ServerPlayer) e).isClaimsNonallyMode())
			return false;
		if(e.getUUID().equals(claimConfig.getPlayerId()) ||
				e instanceof Player && (ServerPlayerDataAPI.from((ServerPlayer) e).isClaimsAdminMode() ||
						ServerPlayerDataAPI.from((ServerPlayer) e).isClaimsServerMode() &&
						claimsManager.getPermissionHandler().playerHasServerClaimPermission((ServerPlayer) e) &&
						claimConfig.getType() == PlayerConfigType.SERVER
				)
		)
			return true;
		if(claimConfig.getPlayerId() == null)
			return false;
		if(!(e instanceof Player) || claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) && claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES))
			return false;
		P claimParty = partyManager.getPartyByMember(claimConfig.getPlayerId());
		if(claimParty == null)
			return false;
		P accessorParty = partyManager.getPartyByMember(e.getUUID());
		return accessorParty == claimParty && !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) || accessorParty != null && !claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES) && claimParty.isAlly(accessorParty.getId());
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

	public boolean onEntityDestroyBlock(IServerData<CM,P> serverData, Level world, Entity entity, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return !canGriefBlocks(entity, config) && blockAccessCheck(pos, config, entity, world, false, true);
	}
	
	public IPlayerConfig getClaimConfig(IPlayerConfigManager playerConfigs, IPlayerChunkClaim claim) {
		IPlayerConfig mainConfig = playerConfigs.getLoadedConfig(claim == null ? null : claim.getPlayerId());
		if(claim == null)
			return mainConfig;
		return mainConfig.getEffectiveSubConfig(claim.getSubConfigIndex());
	}

	private boolean blockAccessCheck(IServerData<CM,P> serverData, BlockPos pos, Entity entity, Level world, boolean emptyHand, boolean leftClick) {
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return blockAccessCheck(pos, config, entity, world, emptyHand, leftClick);
	}
	
	private boolean blockAccessCheck(BlockPos pos, IPlayerConfig config, Entity entity, Level world, boolean emptyHand, boolean leftClick) {
		boolean chunkAccess = hasChunkAccess(config, entity);
		if(chunkAccess)
			return false;
		else {
			Block block = world.getBlockState(pos).getBlock();
			if(leftClick && forcedBreakExceptionBlocks.contains(block) || !leftClick && emptyHand && forcedInteractionExceptionBlocks.contains(block))
				return false;
			if(
				leftClick && config.getEffective(PlayerConfigOptions.ALLOW_SOME_BLOCK_BREAKING) && optionalBreakExceptionBlocks.contains(block)
			||
				!leftClick && emptyHand && config.getEffective(PlayerConfigOptions.ALLOW_SOME_BLOCK_INTERACTIONS) && optionalInteractionExceptionBlocks.contains(block)
			)
				return false;
			return true;
		}
	}
	
	private boolean onBlockAccess(IServerData<CM,P> serverData, BlockPos pos, Player player, Level world, InteractionHand hand, boolean emptyHand, boolean leftClick, boolean direct, Component message) {
		if(blockAccessCheck(serverData, pos, player, world, emptyHand, leftClick)) {
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
		if(player != null && CREATE_DEPLOYER_UUID.equals(player.getUUID()))//uses custom protection
			return false;
		ItemStack itemStack = player.getItemInHand(hand);
		boolean emptyHand = itemStack.getItem() == Items.AIR;
		if(emptyHand || !requiresEmptyHandBlocks.contains(player.getLevel().getBlockState(pos).getBlock())) {
			if (onBlockAccess(serverData, pos, player, player.getLevel(), hand, true, false, true, null))
				return true;
		} else {
			Component message = hand == InteractionHand.MAIN_HAND ? BLOCK_TRY_EMPTY_MAIN : BLOCK_TRY_EMPTY_OFF;
			if(onBlockAccess(serverData, pos, player, player.getLevel(), hand, false, false, true, message))
				return true;
		}
		return !emptyHand && onUseItemAt(serverData, player, pos, blockHit.getDirection(), itemStack, hand);
	}

	public boolean onEntityPlaceBlock(IServerData<CM, P> serverData, Entity entity, Level level, BlockPos pos) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity != null && CREATE_DEPLOYER_UUID.equals(entity.getUUID()))//uses custom protection
			return false;
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), chunkPos);
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return (entity instanceof Player || !canGriefBlocks(entity, config))
				&& blockAccessCheck(pos, config, entity, level, false, false);
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
		if(isItemUseRestricted(itemStack) && !(item instanceof BucketItem) && !(item instanceof SolidBucketItem)) {
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
			ChunkPos chunkPos = new ChunkPos(pos);
			for(int i = -1; i < 2; i++)
				for(int j = -1; j < 2; j++) {//checking neighboring chunks too because of items that affect a high range
					IPlayerChunkClaim claim = claimsManager.get(player.getLevel().dimension().location(), new ChunkPos(chunkPos.x + i, chunkPos.z + j));
					boolean isCurrentChunk = i == 0 && j == 0;
					if (isCurrentChunk || claim != null){//wilderness neighbors don't have to be protected this much
						IPlayerConfig config = getClaimConfig(playerConfigs, claim);
						if((isCurrentChunk || config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE))
								&& !hasChunkAccess(config, player)) {
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
		for(int i = -1; i < 2; i++)
			for(int j = -1; j < 2; j++) {
				ChunkPos chunkPos = new ChunkPos(entity.chunkPosition().x + i, entity.chunkPosition().z + j);
				IPlayerChunkClaim claim = claimsManager.get(entity.getLevel().dimension().location(), chunkPos);
				if(i == 0 && j == 0 || claim != null) {//wilderness neighbors don't have to be protected this much
					IPlayerConfig config = getClaimConfig(playerConfigs, claim);
					if (!canGriefBlocks(entity, config) && !hasChunkAccess(config, entity))
						return true;
				}
			}
		return false;
	}

	private boolean isEntityException(Entity entity, IPlayerConfig config, boolean emptyHand, boolean attack){
		EntityType<?> entityType = entity.getType();
		if(attack && forcedKillExceptionEntities.contains(entityType) || emptyHand && !attack && forcedEmptyHandExceptionEntities.contains(entityType))
			return true;
		if(
			attack && config.getEffective(PlayerConfigOptions.ALLOW_SOME_ENTITY_KILLING) && optionalKillExceptionEntities.contains(entityType)
		||
			!attack && emptyHand && config.getEffective(PlayerConfigOptions.ALLOW_SOME_ENTITY_INTERACTIONS) && optionalEmptyHandExceptionEntities.contains(entityType)
		)
			return true;
		return false;
	}
	
	public boolean onEntityInteract(IServerData<CM,P> serverData, Entity entity, Entity target, InteractionHand hand, boolean direct, boolean attack, boolean posSpecific) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		if(entity != null && CREATE_DEPLOYER_UUID.equals(entity.getUUID()))//uses custom protection
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(new BlockPos(target.getBlockX(), target.getBlockY(), target.getBlockZ()));
		IPlayerChunkClaim claim = claimsManager.get(target.getLevel().dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(shouldProtectEntity(config, target, entity)) {
			if(direct && entity instanceof Player player) {
				ItemStack stack = player.getItemInHand(hand);
				boolean emptyHand = stack.getItem() == Items.AIR;
				if(isEntityException(target, config, emptyHand, attack))
					return false;
				if(!posSpecific) {//avoiding double messages
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
		return false;
	}

	public boolean onEntityFire(IServerData<CM, P> serverData, Entity target) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(new BlockPos(target.getBlockX(), target.getBlockY(), target.getBlockZ()));
		IPlayerChunkClaim claim = claimsManager.get(target.getLevel().dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		return config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS) &&
				config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_FIRE) &&
				isProtectable(target);
	}

	public void onEntityEnterChunk(IServerData<CM, P> serverData, Entity entity, double goodX, double goodZ, SectionPos newSection, SectionPos oldSection) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		if(ignoreChunkEnter)
			return;
		IPlayerChunkClaim toClaim = claimsManager.get(entity.getLevel().dimension().location(), newSection.x(), newSection.z());
		if(toClaim == null)//wilderness is fine
			return;
		IPlayerChunkClaim fromClaim = claimsManager.get(entity.getLevel().dimension().location(), oldSection.x(), oldSection.z());
		boolean isForcedEntity = forcedEntityClaimBarrierList.contains(entity.getType());
		if(isForcedEntity && hitsAnotherClaim(serverData, fromClaim, toClaim, null)
				|| !isForcedEntity && optionalEntityClaimBarrierList.contains(entity.getType()) &&
					hitsAnotherClaim(serverData, fromClaim, toClaim, PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_OPTIONAL_ENTITY_BARRIER)){
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
			IPlayerConfig config = getClaimConfig(playerConfigs, toClaim);
			Entity accessCheckEntity = entity;
			if(entity instanceof Projectile projectile && projectile.getOwner() != null)
				accessCheckEntity = projectile.getOwner();
			if(!hasChunkAccess(config, accessCheckEntity)){
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
		while(entities.hasNext()) {
			Entity entity = entities.next();
			BlockPos blockPos = new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
			ChunkPos chunkPos = new ChunkPos(blockPos);
			IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
			IPlayerConfig config = getClaimConfig(playerConfigs, claim);
			if(config != null && !config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS))
				config = null;
			if(config != null && config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS) &&
					((damageSource.getEntity() == null || !(damageSource.getEntity() instanceof Player)) && isProtectable(entity) || shouldProtectEntity(config, entity, damageSource.getEntity()))
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
		if(claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT) && !hasChunkAccess(claimConfig, entity)) {
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
					if (config.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING) && !hasChunkAccess(config, bolt.getCause())) {
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
		return claimConfig.getEffective(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE)
				&& !hasChunkAccess(claimConfig, entity);
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
		return onUseItemAt(serverData, entity, pos, direction, itemStack, null);
	}

	public boolean onUseItemAt(IServerData<CM, P> serverData, Entity entity, BlockPos pos, Direction direction, ItemStack itemStack, InteractionHand hand) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
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
		if(blockAccessCheck(serverData, pos, entity, entity.getLevel(), false, false) ||
				pos2 != null && blockAccessCheck(serverData, pos2, entity, entity.getLevel(), false, false)){
			if(entity instanceof ServerPlayer player)
				player.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_APPLY_ITEM_MAIN : CANT_APPLY_ITEM_OFF, player.getUUID());
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
			return !hasChunkAccess(getClaimConfig(serverData.getPlayerConfigs(), posClaim), player);
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

	public static final class Builder
	<
		CM extends IServerClaimsManager<?, ?, ?>,
		M extends IPartyMember,
		I extends IPartyPlayerInfo,
		P extends IServerParty<M, I>
	> {

		private CM claimsManager;
		private IPartyManager<P> partyManager;

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

		public ChunkProtection<CM,M,I,P> build(){
			if(claimsManager == null || partyManager == null)
				throw new IllegalStateException();
			Set<EntityType<?>> friendlyEntityList = new HashSet<>();
			Set<EntityType<?>> hostileEntityList = new HashSet<>();
			Set<Block> optionalInteractionExceptionBlocks = new HashSet<>();
			Set<Block> optionalBreakExceptionBlocks = new HashSet<>();
			Set<Block> forcedInteractionExceptionBlocks = new HashSet<>();
			Set<Block> forcedBreakExceptionBlocks = new HashSet<>();
			Set<Block> requiresEmptyHandBlocks = new HashSet<>();
			Set<EntityType<?>> optionalEmptyHandExceptionEntities = new HashSet<>();
			Set<EntityType<?>> optionalKillExceptionEntities = new HashSet<>();
			Set<EntityType<?>> forcedEmptyHandExceptionEntities = new HashSet<>();
			Set<EntityType<?>> forcedKillExceptionEntities = new HashSet<>();
			Set<EntityType<?>> optionalEntityClaimBarrierList = new HashSet<>();
			Set<EntityType<?>> forcedEntityClaimBarrierList = new HashSet<>();
			Set<EntityType<?>> entitiesAllowedToGrief = new HashSet<>();
			Set<Item> additionalBannedItems = new HashSet<>();
			Set<Item> itemUseProtectionExceptions = new HashSet<>();
			ServerConfig.CONFIG.friendlyChunkProtectedEntityList.get().forEach(s -> EntityType.byString(s).ifPresent(friendlyEntityList::add));
			ServerConfig.CONFIG.hostileChunkProtectedEntityList.get().forEach(s -> EntityType.byString(s).ifPresent(hostileEntityList::add));

			Function<ResourceLocation, Block> blockGetter = Services.PLATFORM.getBlockRegistry()::getValue;
			Function<ResourceLocation, EntityType<?>> entityGetter = rl -> EntityType.byString(rl.toString()).orElse(null);
			ServerConfig.CONFIG.blockProtectionExceptionList.get()
					.forEach(s -> onExceptionListElement(
							s,
							optionalInteractionExceptionBlocks::add,
							optionalBreakExceptionBlocks::add,
							o -> {
								optionalInteractionExceptionBlocks.add(o);
								requiresEmptyHandBlocks.add(o);
							},
							forcedInteractionExceptionBlocks::add,
							forcedBreakExceptionBlocks::add,
							o -> {
								forcedInteractionExceptionBlocks.add(o);
								requiresEmptyHandBlocks.add(o);
							},
							blockGetter
					));
			ServerConfig.CONFIG.entityProtectionExceptionList.get()
					.forEach(s -> onExceptionListElement(
							s,
							optionalEmptyHandExceptionEntities::add,
							optionalKillExceptionEntities::add,
							optionalEmptyHandExceptionEntities::add,
							forcedEmptyHandExceptionEntities::add,
							forcedKillExceptionEntities::add,
							forcedEmptyHandExceptionEntities::add,
							entityGetter
					));
			ServerConfig.CONFIG.entityClaimBarrierList.get()
					.forEach(s -> onExceptionListElement(
							s,
							optionalEntityClaimBarrierList::add,
							null,
							null,
							forcedEntityClaimBarrierList::add,
							null,
							null,
							entityGetter
					));
			ServerConfig.CONFIG.entitiesAllowedToGrief.get()
					.forEach(s -> onExceptionListElement(
							s,
							entitiesAllowedToGrief::add,
							entitiesAllowedToGrief::add,
							entitiesAllowedToGrief::add,
							entitiesAllowedToGrief::add,
							entitiesAllowedToGrief::add,
							entitiesAllowedToGrief::add,
							entityGetter
					));
			ServerConfig.CONFIG.additionalBannedItemsList.get().forEach(s -> {
				Item item = Services.PLATFORM.getItemRegistry().getValue(new ResourceLocation(s));
				if(item != null)
					additionalBannedItems.add(item);
			});
			ServerConfig.CONFIG.itemUseProtectionExceptionList.get().forEach(s -> {
				Item item = Services.PLATFORM.getItemRegistry().getValue(new ResourceLocation(s));
				if(item != null)
					itemUseProtectionExceptions.add(item);
			});
			return new ChunkProtection<>(claimsManager, partyManager, new ChunkProtectionEntityHelper(), friendlyEntityList, hostileEntityList, optionalInteractionExceptionBlocks, optionalBreakExceptionBlocks, forcedInteractionExceptionBlocks, forcedBreakExceptionBlocks, requiresEmptyHandBlocks, optionalEmptyHandExceptionEntities, optionalKillExceptionEntities, forcedEmptyHandExceptionEntities, forcedKillExceptionEntities, optionalEntityClaimBarrierList, forcedEntityClaimBarrierList, entitiesAllowedToGrief, additionalBannedItems, itemUseProtectionExceptions);
		}

		private <T> void onExceptionListElement(String element, Consumer<T> optionalInteractionException, Consumer<T> optionalBreakException, Consumer<T> optionalHandException, Consumer<T> forcedInteractionException, Consumer<T> forcedBreakException, Consumer<T> forcedHandException, Function<ResourceLocation, T> objectGetter){
			String id = element;
			Consumer<T> destination = optionalInteractionException;
			if(element.startsWith(BREAK_PREFIX) ||
					element.startsWith(FORCE_PREFIX) ||
					element.startsWith(FORCE_BREAK_PREFIX) ||
					element.startsWith(HAND_PREFIX) ||
					element.startsWith(FORCE_HAND_PREFIX)){
				if(element.startsWith(BREAK_PREFIX))
					destination = optionalBreakException;
				else if(element.startsWith(FORCE_PREFIX))
					destination = forcedInteractionException;
				else if(element.startsWith(HAND_PREFIX))
					destination = optionalHandException;
				else if(element.startsWith(FORCE_HAND_PREFIX))
					destination = forcedHandException;
				else
					destination = forcedBreakException;
				id = element.substring(element.indexOf("$") + 1);
			}
			T object = objectGetter.apply(new ResourceLocation(id));
			if(object != null && destination != null)
				destination.accept(object);
		}

		public static
		<
			CM extends IServerClaimsManager<?, ?, ?>,
			M extends IPartyMember,
			I extends IPartyPlayerInfo,
			P extends IServerParty<M, I>
		> Builder<CM,M,I,P> begin(){
			return new Builder<CM,M,I,P>().setDefault();
		}

	}

}
