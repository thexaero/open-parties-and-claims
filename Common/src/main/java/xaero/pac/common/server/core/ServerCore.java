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

package xaero.pac.common.server.core;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.entity.IEntity;
import xaero.pac.common.entity.IItemEntity;
import xaero.pac.common.packet.ClientboundPacDimensionHandshakePacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.platform.Services;
import xaero.pac.common.reflect.Reflection;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.core.accessor.ICreateArmInteractionPoint;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.world.ServerLevelHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerCore {

	private static final Component TRAIN_CONTROLS_MESSAGE = new TranslatableComponent("gui.xaero_claims_protection_create_train_controls_protected").withStyle(s -> s.withColor(ChatFormatting.RED));

	public static void onServerTickStart(MinecraftServer server) {
		OpenPartiesAndClaims.INSTANCE.startupCrashHandler.check();
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
		if(serverData != null)
			try {
				serverData.getServerTickHandler().onTick(serverData);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
	}

	public static void onServerWorldInfo(ServerPlayer player){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundPacDimensionHandshakePacket(ServerConfig.CONFIG.claimsEnabled.get(), ServerConfig.CONFIG.partiesEnabled.get()));
	}

	public static boolean canAddLivingEntityEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source){
		if(source == null || source.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(source.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, source, source, target, null, null, true, false);
		return !shouldProtect;
	}

	public static boolean canSpreadFire(LevelReader levelReader, BlockPos pos){
		if(!(levelReader instanceof Level level))
			return true;
		if(level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		boolean shouldProtect = serverData.getChunkProtection().onFireSpread(serverData, serverLevel, pos);
		return !shouldProtect;
	}

	public static boolean mayUseItemAt(Player player, BlockPos pos, Direction direction, ItemStack itemStack){
		if(player.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(player.getLevel());
		boolean shouldProtect = serverData.getChunkProtection().onUseItemAt(serverData, player, serverLevel, pos, direction, itemStack, null, false, false, true);
		return !shouldProtect;
	}

	public static boolean replaceFluidCanPassThrough(boolean currentValue, BlockGetter blockGetter, BlockPos from, BlockPos to){
		if(!currentValue)
			return false;
		if(!(blockGetter instanceof Level level))
			return true;
		if(level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		boolean shouldProtect = serverData.getChunkProtection().onFluidSpread(serverData, serverLevel, from, to);
		return !shouldProtect;
	}

	public static DispenseItemBehavior replaceDispenseBehavior(DispenseItemBehavior defaultValue, ServerLevel level, BlockPos blockPos) {
		if(defaultValue == DispenseItemBehavior.NOOP)
			return defaultValue;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return defaultValue;
		boolean shouldProtect = serverData.getChunkProtection().onDispenseFrom(serverData, level, blockPos);
		return shouldProtect ? DispenseItemBehavior.NOOP : defaultValue;
	}

	public static boolean canPistonPush(PistonStructureResolver pistonStructureResolver, Level level, BlockPos pistonPos, Direction direction, boolean extending){
		if(level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onPistonPush(serverData, ServerLevelHelper.getServerLevel(level), pistonStructureResolver.getToPush(), pistonStructureResolver.getToDestroy(), pistonPos, direction, extending);
		return !shouldProtect;
	}

	private static boolean isCreateModAllowed(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, Level level, int posChunkX, int posChunkZ, BlockPos sourceOrAnchor, boolean checkNeighborBlocks, boolean affectsBlocks, boolean affectsEntities){
		boolean shouldProtect = serverData.getChunkProtection().onCreateMod(serverData, ServerLevelHelper.getServerLevel(level), posChunkX, posChunkZ, sourceOrAnchor, checkNeighborBlocks, affectsBlocks, affectsEntities);
		return !shouldProtect;
	}

	private static boolean isCreateModAllowed(Level level, BlockPos pos, BlockPos sourceOrAnchor, boolean affectsBlocks, boolean affectsEntities){
		if(level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, level, pos.getX() >> 4, pos.getZ() >> 4, sourceOrAnchor, true, affectsBlocks, affectsEntities);
	}

	public static boolean isCreateModAllowed(Level level, BlockPos pos, BlockPos sourceOrAnchor){
		//cant rename
		//called when a contraption tries to move a block
		if(level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, level, pos.getX() >> 4, pos.getZ() >> 4, sourceOrAnchor, true, true, false);
	}

	public static BlockPos CAPTURED_TARGET_POS;
	public static BlockState replaceBlockFetchOnCreateModBreak(BlockState actual, Level level, BlockPos sourceOrAnchor){
		if(!isCreateModAllowed(level, CAPTURED_TARGET_POS, sourceOrAnchor, true, false))
			return Blocks.BEDROCK.defaultBlockState();//fake bedrock won't be broken by create
		return actual;
	}

	public static Map<BlockPos, BlockState> CAPTURED_POS_STATE_MAP;
	public static void onCreateModSymmetryProcessed(Level level, Player player){
		if(level.getServer() == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return;
		if(CAPTURED_POS_STATE_MAP == null)
			return;
		Iterator<BlockPos> posIterator = CAPTURED_POS_STATE_MAP.keySet().iterator();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		while(posIterator.hasNext()){
			BlockPos pos = posIterator.next();
			if(serverData.getChunkProtection().onEntityPlaceBlock(serverData, player, serverLevel, pos, null))
				posIterator.remove();
		}
	}

	public static boolean canCreateCannonPlaceBlock(BlockEntity placer, BlockPos pos){
		Level level = placer.getLevel();
		if(level == null || level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, level, pos.getX() >> 4, pos.getZ() >> 4, placer.getBlockPos(), false, true, false);
	}

	public static void onCreateCollideEntities(List<Entity> entities, Entity contraption, BlockPos contraptionAnchor){
		Level level = contraption.getLevel();
		if(level == null || level.getServer() == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		serverData.getChunkProtection().onCreateModAffectPositionedObjects(serverData, serverLevel, entities, Entity::chunkPosition, contraptionAnchor, true, true, false, true);
	}

	public static boolean isCreateMechanicalArmValid(BlockEntity arm, List<ICreateArmInteractionPoint> points){
		Level level = arm.getLevel();
		if(level == null || level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverData.getChunkProtection().onCreateModAffectPositionedObjects(serverData, serverLevel, points, p -> new ChunkPos(p.xaero_OPAC_getPos()), arm.getBlockPos(), false, false, true, false)){
			points.clear();
			return false;
		}
		return true;
	}

	@Deprecated(forRemoval = true)
	public static boolean isCreateTileEntityPacketAllowed(BlockEntity tileEntity, ServerPlayer player){
		//only used on fabric which doesn't have the latest create version yet
		return isCreateTileEntityPacketAllowed(tileEntity.getBlockPos(), player);
	}

	public static boolean isCreateTileEntityPacketAllowed(BlockPos pos, ServerPlayer player){
		if(pos == null)//when "stop tracking" is selected
			return true;
		ServerLevel level = player.getLevel();
		BlockEntity tileEntity = level.getBlockEntity(pos);
		if(tileEntity == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onBlockSpecialInteraction(serverData, player, level, pos);
		return !shouldProtect;
	}

	public static boolean isCreateContraptionInteractionPacketAllowed(int contraptionId, InteractionHand interactionHand, ServerPlayer player){
		if(player.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		Entity contraption = player.getLevel().getEntity(contraptionId);
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, null, player, contraption, null, interactionHand, false, true);
		return !shouldProtect;
	}

	public static boolean isCreateTrainRelocationPacketAllowed(int contraptionId, BlockPos pos, ServerPlayer player) {
		if(player.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		Entity contraption = player.getLevel().getEntity(contraptionId);
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, null, player, contraption, null, null, false, true);
		if(!shouldProtect)
			shouldProtect = serverData.getChunkProtection().onBlockInteraction(serverData, player, null, null, player.getLevel(), pos, Direction.UP, false, true);
		return !shouldProtect;
	}

	public static boolean isCreateTrainControlsPacketAllowed(int contraptionId, ServerPlayer player) {
		if(player.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		Entity contraption = player.getLevel().getEntity(contraptionId);
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, null, player, contraption, null, null, false, false);
		if(shouldProtect)
			player.sendMessage(serverData.getAdaptiveLocalizer().getFor(player, TRAIN_CONTROLS_MESSAGE), player.getUUID());
		return !shouldProtect;
	}

	public static boolean isCreateDeployerBlockInteractionAllowed(Level level, BlockPos anchor, BlockPos pos){
		return isCreateModAllowed(level, pos, anchor, true, true);
	}

	public static boolean isCreateTileDeployerBlockInteractionAllowed(BlockEntity tileEntity){
		Direction direction = tileEntity.getBlockState().getValue(BlockStateProperties.FACING);
		BlockPos pos = tileEntity.getBlockPos().relative(direction, 2);
		return isCreateDeployerBlockInteractionAllowed(tileEntity.getLevel(), tileEntity.getBlockPos(), pos);
	}

	public static boolean isCreateGlueSelectionAllowed(BlockPos from, BlockPos to, ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onCreateGlueSelection(serverData,from, to, player);
		return !shouldProtect;
	}

	public static boolean isCreateGlueRemovalAllowed(int entityId, ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onCreateGlueRemoval(serverData, entityId, player);
		return !shouldProtect;
	}

	public static boolean isProjectileHitAllowed(Projectile entity, EntityHitResult hitResult){
		Entity target = hitResult.getEntity();
		if(target.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, entity.getOwner(), entity, target, null, null, true, false);
		return !shouldProtect;
	}

	private static Level CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL;
	private static BlockPos CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR;

	public static void preCreateDisassembleSuperGlue(Level level, BlockPos anchor){
		CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL = level;
		CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR = anchor;
	}

	public static void postCreateDisassembleSuperGlue(){
		CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL = null;
		CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR = null;
	}

	public static BlockPos getFreshAddedSuperGlueAnchor(Level level){
		if(level != null && level == CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL)
			return CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR;
		return null;
	}

	private static InteractionHand ENTITY_INTERACTION_HAND;

	public static boolean canInteract(ServerGamePacketListenerImpl packetListener, ServerboundInteractPacket packet){
		ENTITY_INTERACTION_HAND = null;
		packet.dispatch(new ServerboundInteractPacket.Handler() {
			@Override
			public void onInteraction(@Nonnull InteractionHand interactionHand) {}
			@Override
			public void onInteraction(@Nonnull InteractionHand interactionHand, @Nonnull Vec3 vec3) {
				ENTITY_INTERACTION_HAND = interactionHand;
			}
			@Override
			public void onAttack() {}
		});
		if(ENTITY_INTERACTION_HAND == null)//not specific interaction
			return true;
		ServerPlayer player = packetListener.player;
		ServerLevel level = player.getLevel();
		final Entity entity = packet.getTarget(level);
		if(entity == null)
			return true;
		return !OpenPartiesAndClaims.INSTANCE.getCommonEvents().onInteractEntitySpecific(player, entity, ENTITY_INTERACTION_HAND);
	}

	public static boolean replaceEntityIsInvulnerable(boolean actual, DamageSource damageSource, Entity entity){
		if(!actual)
			return OpenPartiesAndClaims.INSTANCE.getCommonEvents().onLivingHurt(damageSource, entity);
		return actual;
	}

	public static boolean canDestroyBlock(Level level, BlockPos pos, Entity entity) {
		return entity == null || !OpenPartiesAndClaims.INSTANCE.getCommonEvents().onEntityDestroyBlock(level, pos, entity);
	}

	public static void onEntitiesPushBlock(List<? extends Entity> entities, Block block, BlockPos pos){
		if(entities.isEmpty() || entities.get(0).getServer() == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entities.get(0).getServer());
		if(serverData == null)
			return;
		serverData.getChunkProtection().onEntitiesPushBlock(serverData, ServerLevelHelper.getServerLevel(entities.get(0).getLevel()), pos, block, entities);
	}

	public static boolean onEntityPushBlock(Block block, Entity entity, BlockHitResult blockHitResult){
		if(entity == null || entity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
		if(serverData == null)
			return false;
		List<Entity> helpList = Lists.newArrayList(entity);
		serverData.getChunkProtection().onEntitiesPushBlock(serverData, ServerLevelHelper.getServerLevel(entity.getLevel()), blockHitResult.getBlockPos(), block, helpList);
		return helpList.isEmpty();
	}

	public static final BlockPos.MutableBlockPos FROSTWALK_BLOCKPOS = new BlockPos.MutableBlockPos();
	private static LivingEntity FROSTWALK_ENTITY;
	private static Level FROSTWALK_LEVEL;
	private static boolean FROSTWALK_CAPTURE_USABLE = true;

	public static boolean isHandlingFrostWalk(){
		return FROSTWALK_ENTITY != null;
	}

	public static boolean preFrostWalkHandle(LivingEntity living, Level level){//returns whether to completely disable frostwalk (when capture isn't usable)
		if(level.getServer() == null)
			return false;
		if(ServerConfig.CONFIG.completelyDisableFrostWalking.get())
			return true;
		if (!FROSTWALK_CAPTURE_USABLE)
			return false;
		if (FROSTWALK_ENTITY != null) {
			OpenPartiesAndClaims.LOGGER.error("Frost walk capture isn't working properly. Likely a compatibility issue. Turning off frost walking protection... Please use the option in the main server config file to disable frost walking across the server.");
			FROSTWALK_CAPTURE_USABLE = false;
			postFrostWalkHandle(level);
			return false;
		}
		FROSTWALK_ENTITY = living;
		FROSTWALK_LEVEL = level;
		return false;
	}

	public static BlockPos preBlockStateFetchOnFrostwalk(BlockPos pos){
		if(FROSTWALK_ENTITY == null || FROSTWALK_LEVEL.getServer() == null || !FROSTWALK_LEVEL.getServer().isSameThread())
			return pos;
		BlockState actualState = FROSTWALK_LEVEL.getBlockState(pos);
		if(actualState.getMaterial() != Material.WATER)
			return pos;
		FROSTWALK_BLOCKPOS.set(pos);
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(FROSTWALK_LEVEL.getServer());
		if(serverData == null)
			return pos;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(FROSTWALK_LEVEL);
		if(serverData.getChunkProtection().onFrostWalk(serverData, FROSTWALK_ENTITY, serverLevel, FROSTWALK_BLOCKPOS))
			return FROSTWALK_BLOCKPOS.setY(FROSTWALK_LEVEL.getMaxBuildHeight());//won't be water here lol
		return pos;
	}

	public static void postFrostWalkHandle(Level level){
		if(level.getServer() == null)
			return;
		FROSTWALK_ENTITY = null;
		FROSTWALK_LEVEL = null;
	}

	private static final Field ENTITY_INSIDE_PORTAL_FIELD = Reflection.getFieldReflection(Entity.class, "f_19817_", "field_5963", "Z");

	public static boolean onHandleNetherPortal(Entity entity) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		if(serverData == null)
			return false;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(entity.getLevel());
		if(serverData.getChunkProtection().onNetherPortal(serverData, entity, serverLevel, entity.blockPosition())){
			entity.level.getProfiler().pop();
			Reflection.setReflectFieldValue(entity, ENTITY_INSIDE_PORTAL_FIELD, false);
			return true;
		}
		return false;
	}

	private static boolean FINDING_RAID_SPAWN_POS;
	private static boolean RAID_SPAWN_POS_CAPTURE_USABLE = true;
	private static int RAID_SPAWN_POS_CAPTURE_TICK;
	public static void onFindRandomSpawnPosPre(Raid raid){//returns whether to completely disable (when capture isn't usable)
		if(!RAID_SPAWN_POS_CAPTURE_USABLE)
			return;
		FINDING_RAID_SPAWN_POS = true;
		RAID_SPAWN_POS_CAPTURE_TICK = raid.getLevel().getServer().getTickCount();
	}

	public static void onFindRandomSpawnPosPost(){
		FINDING_RAID_SPAWN_POS = false;
	}

	public static boolean replaceIsPositionEntityTicking(boolean currentReturn, ServerLevel level, BlockPos pos){
		if(!currentReturn)
			return false;
		if(FINDING_RAID_SPAWN_POS) {
			if(level.getServer().getTickCount() != RAID_SPAWN_POS_CAPTURE_TICK){
				OpenPartiesAndClaims.LOGGER.error("Raid spawn capture isn't working properly. Likely a compatibility issue. Turning off raid protection... Please use the disableRaids game rule to disable raids across the server.");
				RAID_SPAWN_POS_CAPTURE_USABLE = false;
				onFindRandomSpawnPosPost();
				return false;
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(level.getServer());
			if (serverData == null)
				return true;
			return !serverData.getChunkProtection().onRaidSpawn(serverData, level, pos);
		}
		return true;
	}

	private static LivingEntity DYING_LIVING;
	private static DamageSource DYING_LIVING_FROM;
	private static boolean DYING_LIVING_USABLE = true;
	private static int DYING_LIVING_TICK;
	//below are the alternatives for DYING_LIVING stuff
	private static LivingEntity DROPPING_LOOT_LIVING;
	private static DamageSource DROPPING_LOOT_LIVING_FROM;
	private static boolean DROPPING_LOOT_LIVING_USABLE = true;
	private static int DROPPING_LOOT_LIVING_TICK;
	public static void onLivingEntityDiePre(LivingEntity living, DamageSource source) {
		if(living.getServer() != null) {
			if(DYING_LIVING == null && DYING_LIVING_USABLE) {//checking that the current capture is null in case a mob kills other mobs on death
				DYING_LIVING_FROM = source;
				DYING_LIVING = living;
				DYING_LIVING_TICK = living.getServer().getTickCount();
			}
		}
	}

	public static void onLivingEntityDiePost(LivingEntity living) {
		if(living.getServer() != null && DYING_LIVING == living) {
			DYING_LIVING_FROM = null;
			DYING_LIVING = null;
		}
	}

	public static void onLivingEntityDropDeathLootPre(LivingEntity living, DamageSource source) {
		if(living.getServer() != null) {
			if(DROPPING_LOOT_LIVING == null && DROPPING_LOOT_LIVING_USABLE) {//checking that the current capture is null in case a mob kills other mobs on death
				DROPPING_LOOT_LIVING_FROM = source;
				DROPPING_LOOT_LIVING = living;
				DROPPING_LOOT_LIVING_TICK = living.getServer().getTickCount();
			}
		}
	}

	public static void onLivingEntityDropDeathLootPost(LivingEntity living) {
		if(living.getServer() != null && DROPPING_LOOT_LIVING == living) {
			DROPPING_LOOT_LIVING_FROM = null;
			DROPPING_LOOT_LIVING = null;
		}
	}

	private static void testLivingLootCapture(int currentTickCount){
		boolean disabledCapture = false;
		if(DYING_LIVING_FROM != null && DYING_LIVING_TICK != currentTickCount) {
			disabledCapture = !DROPPING_LOOT_LIVING_USABLE;
			DYING_LIVING_USABLE = false;
			DYING_LIVING_FROM = null;
			DYING_LIVING = null;
		}
		if(DROPPING_LOOT_LIVING_FROM != null && DROPPING_LOOT_LIVING_TICK != currentTickCount) {
			disabledCapture = !DYING_LIVING_USABLE;
			DROPPING_LOOT_LIVING_USABLE = false;
			DROPPING_LOOT_LIVING_FROM = null;
			DROPPING_LOOT_LIVING = null;
		}
		if(disabledCapture)
			OpenPartiesAndClaims.LOGGER.error("Living entity loot capture isn't working properly. Likely a compatibility issue. Turning off loot-related protection... Please use keepInventory and other stuff, if this is an issue.");
	}

	public static DamageSource getDyingDamageSourceForCurrentEntitySpawns(int currentTickCount){
		testLivingLootCapture(currentTickCount);
		if(DYING_LIVING_FROM != null)
			return DYING_LIVING_FROM;
		return DROPPING_LOOT_LIVING_FROM;
	}

	public static LivingEntity getDyingLivingForCurrentEntitySpawns(int currentTickCount){
		testLivingLootCapture(currentTickCount);
		if(DYING_LIVING != null)
			return DYING_LIVING;
		return DROPPING_LOOT_LIVING;
	}

	private final static String LOOT_OWNER_KEY = "xaero_OPAC_lootOwnerId";
	private final static BiConsumer<IEntity, UUID> LOOT_OWNER_SETTER = IEntity::setXaero_OPAC_lootOwner;
	private final static Function<IEntity, UUID> LOOT_OWNER_GETTER = IEntity::getXaero_OPAC_lootOwner;
	private final static String DEAD_PLAYER_KEY = "xaero_OPAC_deadPlayer";
	private final static BiConsumer<IEntity, UUID> DEAD_PLAYER_SETTER = IEntity::setXaero_OPAC_deadPlayer;
	private final static Function<IEntity, UUID> DEAD_PLAYER_GETTER = IEntity::getXaero_OPAC_deadPlayer;
	private final static String THROWER_ACCESSOR_KEY = "xaero_OPAC_throwerAccessor";
	private final static BiConsumer<IEntity, UUID> THROWER_ACCESSOR_SETTER = (ie, id) -> ((IItemEntity)ie).setXaero_OPAC_throwerAccessor(id);
	private final static Function<IEntity, UUID> THROWER_ACCESSOR_GETTER = ie -> ((IItemEntity)ie).getXaero_OPAC_throwerAccessor();

	public static void setEntityGenericUUID(Entity entity, String key, UUID uuid, BiConsumer<IEntity, UUID> setter){
		setter.accept((IEntity) entity, uuid);
		CompoundTag persistentData = Services.PLATFORM.getEntityAccess().getPersistentData(entity);
		if(uuid == null)
			persistentData.remove(key);
		else
			persistentData.putUUID(key, uuid);
	}

	public static UUID getEntityGenericUUID(Entity entity, String key, Function<IEntity, UUID> getter, BiConsumer<IEntity, UUID> setter){
		UUID result = getter.apply((IEntity) entity);
		if(result == null) {
			CompoundTag persistentData = Services.PLATFORM.getEntityAccess().getPersistentData(entity);
			if(persistentData.contains(key)) {
				result = persistentData.getUUID(key);
				setter.accept((IEntity) entity, result);
			}
		}
		return result;
	}

	public static void setLootOwner(Entity entity, UUID lootOwner){
		setEntityGenericUUID(entity, LOOT_OWNER_KEY, lootOwner, LOOT_OWNER_SETTER);
	}

	public static UUID getLootOwner(Entity entity){
		return getEntityGenericUUID(entity, LOOT_OWNER_KEY, LOOT_OWNER_GETTER, LOOT_OWNER_SETTER);
	}

	public static void setDeadPlayer(Entity entity, UUID deadPlayer){
		setEntityGenericUUID(entity, DEAD_PLAYER_KEY, deadPlayer, DEAD_PLAYER_SETTER);
	}

	public static UUID getDeadPlayer(Entity entity){
		return getEntityGenericUUID(entity, DEAD_PLAYER_KEY, DEAD_PLAYER_GETTER, DEAD_PLAYER_SETTER);
	}

	public static void setThrowerAccessor(ItemEntity entity, UUID throwerAccessor){
		setEntityGenericUUID(entity, THROWER_ACCESSOR_KEY, throwerAccessor, THROWER_ACCESSOR_SETTER);
	}

	public static UUID getThrowerAccessor(ItemEntity entity){
		return getEntityGenericUUID(entity, THROWER_ACCESSOR_KEY, THROWER_ACCESSOR_GETTER, THROWER_ACCESSOR_SETTER);
	}

	public static boolean onEntityItemPickup(Entity entity, ItemEntity itemEntity) {
		return OpenPartiesAndClaims.INSTANCE.getCommonEvents().onItemPickup(entity, itemEntity);
	}

	public static boolean onMobItemPickup(ItemEntity itemEntity, Mob mob) {
		if (OpenPartiesAndClaims.INSTANCE.getCommonEvents().onItemPickup(mob, itemEntity)) {
			mob.level.getProfiler().pop();
			return true;
		}
		return false;
	}

	public static Player onExperiencePickup(Player player, ExperienceOrb orb) {
		if(orb == null || player == null)
			return player;
		if(orb.getServer() == null)
			return player;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(orb.getServer());
		if (serverData == null)
			return player;
		if(serverData.getChunkProtection().onExperiencePickup(serverData, orb, player))
			return null;
		return player;
	}

	private static boolean MOB_GRIEFING_IS_FOR_ITEMS;
	private static boolean MOB_GRIEFING_IS_FOR_ITEMS_USABLE = true;
	private static int MOB_GRIEFING_IS_FOR_ITEMS_TICK;
	public static void forgePreItemMobGriefingCheck(Mob mob){
		if(!MOB_GRIEFING_IS_FOR_ITEMS_USABLE)
			return;
		if(mob.getServer() != null) {
			MOB_GRIEFING_IS_FOR_ITEMS = true;
			MOB_GRIEFING_IS_FOR_ITEMS_TICK = mob.getServer().getTickCount();
		}
	}

	public static void forgePostItemMobGriefingCheck(Mob mob){
		if(mob.getServer() != null) {
			MOB_GRIEFING_IS_FOR_ITEMS = false;
		}
	}

	public static boolean isMobGriefingForItems(int currentTick){
		if(MOB_GRIEFING_IS_FOR_ITEMS && MOB_GRIEFING_IS_FOR_ITEMS_TICK != currentTick){
			OpenPartiesAndClaims.LOGGER.error("Mob griefing rule check for item pickups capture is not working properly. Turning it off... If this is a problem, please manually configure which mobs grief dropped items in the main server config file with options \"nonBlockGriefingMobs\" and \"droppedItemGriefingMobs\".");
			MOB_GRIEFING_IS_FOR_ITEMS_USABLE = false;
			MOB_GRIEFING_IS_FOR_ITEMS = false;
		}
		return MOB_GRIEFING_IS_FOR_ITEMS;
	}

	private static Entity BEHAVIOR_UTILS_THROW_ITEM_LIVING;
	private static boolean BEHAVIOR_UTILS_THROW_ITEM_USABLE = true;
	private static int BEHAVIOR_UTILS_THROW_ITEM_TICK;
	public static void preThrowItem(Entity entity) {
		if(!BEHAVIOR_UTILS_THROW_ITEM_USABLE)
			return;
		if(entity != null && entity.getServer() != null) {
			if(BEHAVIOR_UTILS_THROW_ITEM_LIVING == null) {
				BEHAVIOR_UTILS_THROW_ITEM_LIVING = entity;
				BEHAVIOR_UTILS_THROW_ITEM_TICK = entity.getServer().getTickCount();
			} else if(BEHAVIOR_UTILS_THROW_ITEM_TICK != entity.getServer().getTickCount()){
				OpenPartiesAndClaims.LOGGER.error("Part of the non-player entity item toss capture isn't working properly. Turning it off...");
				BEHAVIOR_UTILS_THROW_ITEM_USABLE = false;
				BEHAVIOR_UTILS_THROW_ITEM_LIVING = null;
			}
		}
	}

	public static void onThrowItem(ItemEntity itemEntity) {
		if(BEHAVIOR_UTILS_THROW_ITEM_LIVING != null && itemEntity.getServer() != null) {
			itemEntity.setThrower(BEHAVIOR_UTILS_THROW_ITEM_LIVING.getUUID());
			BEHAVIOR_UTILS_THROW_ITEM_LIVING = null;
		}
	}

	private static boolean RESOURCES_DROP_OWNER_CAPTURE_USABLE = true;
	private static Entity RESOURCES_DROP_OWNER;
	private static int RESOURCES_DROP_OWNER_TICK;

	public static void preResourcesDrop(Entity entity){
		if(RESOURCES_DROP_OWNER_CAPTURE_USABLE && RESOURCES_DROP_OWNER == null && entity != null && entity.getServer() != null) {
			RESOURCES_DROP_OWNER = entity;
			RESOURCES_DROP_OWNER_TICK = entity.getServer().getTickCount();
		}
	}

	public static void postResourcesDrop(Entity entity){
		if(entity == RESOURCES_DROP_OWNER)
			RESOURCES_DROP_OWNER = null;
	}

	public static Entity getResourcesDropOwner() {
		if(RESOURCES_DROP_OWNER != null && RESOURCES_DROP_OWNER_TICK != RESOURCES_DROP_OWNER.getServer().getTickCount()) {
			OpenPartiesAndClaims.LOGGER.error("Block/entity resource drop owner capture isn't working properly. Likely a compatibility issue. Turning it off...");
			RESOURCES_DROP_OWNER = null;
			RESOURCES_DROP_OWNER_CAPTURE_USABLE = false;
		}
		return RESOURCES_DROP_OWNER;
	}

	public static void onFishingHookAddEntity(Entity entity, FishingHook hook){
		if(entity instanceof ItemEntity itemEntity) {
			preThrowItem(hook.getOwner());
			onThrowItem(itemEntity);
		}
	}

	public static boolean onItemMerge(ItemEntity first, ItemEntity second){
		if(first.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(first.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onItemStackMerge(serverData, first, second);
	}

	public static boolean onExperienceMerge(ExperienceOrb from, ExperienceOrb into){
		if(into.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(into.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onExperienceMerge(serverData, from, into);
	}

	public static boolean onSetFishingHookedEntity(FishingHook hook, Entity entity) {
		if(entity == null)
			return false;
		if(entity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onFishingHookedEntity(serverData, hook, entity);
	}

	public static List<Entity> onEntitiesPushEntity(List<Entity> entities, Entity target){
		if(target == null)
			return entities;
		if(target.getServer() == null)
			return entities;
		if(entities.isEmpty())
			return entities;
		if(!(target instanceof HangingEntity))
			return entities;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(target.getServer());
		if (serverData == null)
			return entities;
		serverData.getChunkProtection().onEntitiesCollideWithEntity(serverData, target, entities);
		return entities;
	}

	public static List<Entity> onEntityAffectsEntities(List<Entity> targets, Entity entity){
		if(entity == null)
			return targets;
		if(entity.getServer() == null)
			return targets;
		if(targets.isEmpty())
			return targets;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		if (serverData == null)
			return targets;
		serverData.getChunkProtection().onEntityAffectsEntities(serverData, entity, targets);
		return targets;
	}

	public static boolean onEntityPushed(Entity target, MoverType moverType) {
		if(target == null)
			return false;
		if(target.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(target.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onEntityPushed(serverData, target, moverType);
	}

	public static void preServerLevelTick(ServerLevel level) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return;
		if(serverData.getForceLoadManager().hasEnabledTickets(level))
			level.resetEmptyTime();//continue ticking entities in dimensions with no players
	}

	public static void reset(){
		CAPTURED_TARGET_POS = null;
		CAPTURED_POS_STATE_MAP = null;
		ENTITY_INTERACTION_HAND = null;
		FROSTWALK_ENTITY = null;
		FROSTWALK_LEVEL = null;
		FINDING_RAID_SPAWN_POS = false;
		DYING_LIVING = null;
		DYING_LIVING_FROM = null;
		DROPPING_LOOT_LIVING = null;
		DROPPING_LOOT_LIVING_FROM = null;
		MOB_GRIEFING_IS_FOR_ITEMS = false;
		BEHAVIOR_UTILS_THROW_ITEM_LIVING = null;
		RESOURCES_DROP_OWNER = null;
	}
}
