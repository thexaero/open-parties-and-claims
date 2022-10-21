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

package xaero.pac.common.server.core;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.Vec3;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ClientboundPacDimensionHandshakePacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.core.accessor.ICreateArmInteractionPoint;
import xaero.pac.common.server.parties.party.IServerParty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServerCore {
	
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

	public static void onServerWorldInfo(Player player){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer((ServerPlayer) player, new ClientboundPacDimensionHandshakePacket(ServerConfig.CONFIG.claimsEnabled.get(), ServerConfig.CONFIG.partiesEnabled.get()));
	}

	public static boolean canAddLivingEntityEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source){
		if(source == null || !(source.level instanceof ServerLevel))
			return true;
		Level world = source.level;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteract(serverData, source, target, InteractionHand.MAIN_HAND, false, true, false);
		return !shouldProtect;
	}

	public static boolean canSpreadFire(LevelReader levelReader, BlockPos pos){
		if(!(levelReader instanceof ServerLevel level))
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onFireSpread(serverData, level, pos);
		return !shouldProtect;
	}

	public static boolean mayUseItemAt(Player player, BlockPos pos, Direction direction, ItemStack itemStack){
		if(player.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onUseItemAt(serverData, player, pos, direction, itemStack, null, true);
		return !shouldProtect;
	}

	public static boolean replaceFluidCanPassThrough(boolean currentValue, BlockGetter blockGetter, BlockPos from, BlockPos to){
		if(!currentValue)
			return false;
		if(!(blockGetter instanceof ServerLevel level))
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onFluidSpread(serverData, level, from, to);
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
		boolean shouldProtect = serverData.getChunkProtection().onPistonPush(serverData, (ServerLevel) level, pistonStructureResolver.getToPush(), pistonStructureResolver.getToDestroy(), pistonPos, direction, extending);
		return !shouldProtect;
	}

	private static boolean isCreateModAllowed(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, Level level, int posChunkX, int posChunkZ, BlockPos sourceOrAnchor, boolean checkNeighborBlocks){
		boolean shouldProtect = serverData.getChunkProtection().onCreateMod(serverData, (ServerLevel) level, posChunkX, posChunkZ, sourceOrAnchor, checkNeighborBlocks, null);
		return !shouldProtect;
	}

	public static boolean isCreateModAllowed(Level level, BlockPos pos, BlockPos sourceOrAnchor){
		if(level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, level, pos.getX() >> 4, pos.getZ() >> 4, sourceOrAnchor, true);
	}

	public static BlockPos CAPTURED_TARGET_POS;
	public static BlockState replaceBlockFetchOnCreateModBreak(BlockState actual, Level level, BlockPos sourceOrAnchor){
		if(!isCreateModAllowed(level, CAPTURED_TARGET_POS, sourceOrAnchor))
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
		while(posIterator.hasNext()){
			BlockPos pos = posIterator.next();
			if(serverData.getChunkProtection().onCreateMod(serverData, (ServerLevel) level, pos.getX() >> 4, pos.getZ() >> 4, null, false, player))
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
		return isCreateModAllowed(serverData, level, pos.getX() >> 4, pos.getZ() >> 4, placer.getBlockPos(), false);
	}

	public static void onCreateCollideEntities(List<Entity> entities, Entity contraption, BlockPos contraptionAnchor){
		Level level = contraption.getLevel();
		if(level == null || level.getServer() == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return;
		serverData.getChunkProtection().onCreateModAffectPositionedObjects(serverData, level, entities, Entity::chunkPosition, contraptionAnchor, true, true);
	}

	public static boolean isCreateMechanicalArmValid(BlockEntity arm, List<ICreateArmInteractionPoint> points){
		Level level = arm.getLevel();
		if(level == null || level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		if(serverData.getChunkProtection().onCreateModAffectPositionedObjects(serverData, level, points, p -> new ChunkPos(p.xaero_OPAC_getPos()), arm.getBlockPos(), false, false)){
			points.clear();
			return false;
		}
		return true;
	}

	public static boolean isCreateTileEntityPacketAllowed(BlockEntity tileEntity, ServerPlayer player){
		Level level = tileEntity.getLevel();
		if(level == null || level.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		BlockPos pos = tileEntity.getBlockPos();
		boolean shouldProtect = serverData.getChunkProtection().onCreateMod(serverData, (ServerLevel) level, pos.getX() >> 4, pos.getZ() >> 4, null, false, player);
		return !shouldProtect;
	}

	public static boolean isCreateDeployerBlockInteractionAllowed(Level level, BlockPos anchor, BlockPos pos){
		return isCreateModAllowed(level, pos, anchor);
	}

	public static boolean isCreateTileDeployerBlockInteractionAllowed(BlockEntity tileEntity){
		Direction direction = tileEntity.getBlockState().getValue(BlockStateProperties.FACING);
		BlockPos pos = tileEntity.getBlockPos().relative(direction, 2);
		return isCreateDeployerBlockInteractionAllowed(tileEntity.getLevel(), tileEntity.getBlockPos(), pos);
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

	public static void onEntitiesPushBlock(List<? extends Entity> entities, Block block){
		if(entities.isEmpty() || !(entities.get(0).getLevel() instanceof ServerLevel level))
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return;
		BlockPos pos = entities.get(0).blockPosition();
		serverData.getChunkProtection().onEntitiesPushBlock(serverData, level, pos, block, entities);
	}

	public static boolean onEntityPushBlock(Block block, Entity entity){
		if(entity == null || !(entity.getLevel() instanceof ServerLevel level))
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return false;
		BlockPos pos = entity.blockPosition();
		List<Entity> helpList = Lists.newArrayList(entity);
		serverData.getChunkProtection().onEntitiesPushBlock(serverData, level, pos, block, helpList);
		return helpList.isEmpty();
	}

	public static BlockPos.MutableBlockPos FROSTWALK_BLOCKPOS = new BlockPos.MutableBlockPos();
	private static LivingEntity FROSTWALK_ENTITY;
	private static Level FROSTWALK_LEVEL;
	public static boolean HANDLING_FROSTWALK = false;

	public static void preFrostWalkHandle(LivingEntity living, Level level){
		HANDLING_FROSTWALK = true;
		FROSTWALK_ENTITY = living;
		FROSTWALK_LEVEL = level;
	}

	public static BlockPos preBlockStateFetchOnFrostwalk(BlockPos pos){
		FROSTWALK_BLOCKPOS.set(pos);
		if(FROSTWALK_ENTITY == null || !(FROSTWALK_LEVEL instanceof ServerLevel))
			return pos;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(FROSTWALK_LEVEL.getServer());
		if(serverData == null)
			return pos;
		if(serverData.getChunkProtection().onFrostWalk(serverData, FROSTWALK_ENTITY, FROSTWALK_LEVEL, FROSTWALK_BLOCKPOS))
			return FROSTWALK_BLOCKPOS.setY(FROSTWALK_LEVEL.getMaxBuildHeight());//won't be water here lol
		return pos;
	}

	public static void postFrostWalkHandle(){
		HANDLING_FROSTWALK = false;
		FROSTWALK_ENTITY = null;
		FROSTWALK_LEVEL = null;
	}

}
