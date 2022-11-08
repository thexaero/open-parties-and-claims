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

package xaero.pac.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IOpenPACMinecraftServer;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.ServerDataInitializer;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.command.ClaimsCommandRegister;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.command.CommonCommandRegister;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.parties.command.PartyCommandRegister;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommonEvents {

	protected final OpenPartiesAndClaims modMain;
	protected MinecraftServer lastServerStarted;
	
	public CommonEvents(OpenPartiesAndClaims modMain) {
		this.modMain = modMain;
	}

	public void onServerAboutToStart(MinecraftServer server) throws Throwable {
		lastServerStarted = server;
		OpenPartiesAndClaims.LOGGER.info("Initializing Open Parties and Claims for the server...");
		((IOpenPACMinecraftServer) lastServerStarted).setXaero_OPAC_ServerData(new ServerDataInitializer().init(modMain, lastServerStarted));
		modMain.getPacketHandler().onServerAboutToStart();
	}

	public void onServerStarting() {
		modMain.startupCrashHandler.check();
		ServerData.from(lastServerStarted).getServerLoadCallback().onLoad();
//		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
//			serverData = ServerData.from(lastServerStarted);
//		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
//				claimsManager = serverData.getServerClaimsManager();
//		ResourceLocation overworld = new ResourceLocation("overworld");
//		UUID myUUID = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
//		OpenPartiesAndClaims.LOGGER.info("my uuid: " + myUUID);
//		for (int i = 0; i < 100; i++)
//			for (int j = 0; j < 100; j++) {
//				for (int a = 0; a < 4; a++)
//					for (int b = 0; b < 4; b++) {
//						UUID uuid = a == 0 && b == 0 ? myUUID : UUID.randomUUID();
//						for (int o = 0; o < 8; o++)
//							for (int p = 0; p < 8; p++)
//								claimsManager.claim(overworld, uuid, i * 32 + a * 8 + o, j * 32 + b * 8 + p, false);
//					}
//			}
	}

	public void onPlayerRespawn(Player player) {
		if(player instanceof ServerPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, (ServerLevel) player.getLevel(), (ServerPlayer) player);
			}
		}
	}

	public void onPlayerChangedDimension(Player player) {
		if(player instanceof ServerPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, (ServerLevel) player.getLevel(), (ServerPlayer) player);
			}
		}
	}

	public void onPlayerLogIn(Player player) {
		if(player.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from((ServerPlayer) player);
				if(playerData.hasReceivedLoginEvent())//can be true if login was already handled inside ServerPlayerDataAPI.from
					return;
				playerData.setReceivedLoginEvent(true);
				serverData.getPlayerLoginHandler().handlePreWorldJoin((ServerPlayer) player, serverData);
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, (ServerLevel) player.getLevel(), (ServerPlayer) player);
				serverData.getPlayerLoginHandler().handlePostWorldJoin((ServerPlayer) player, serverData);
			}
		}
	}

	public void onPlayerClone(Player original, Player player) {
		if(original.getLevel() instanceof ServerLevel) {
			//copy player data on respawn/clone
			((IOpenPACServerPlayer)player).setXaero_OPAC_PlayerData(ServerPlayerDataAPI.from((ServerPlayer)original));
		}
	}

	public void onPlayerLogOut(Player player) {
		if(player.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerLogoutHandler().handle((ServerPlayer) player, serverData);
			}
		}
	}

	public void onServerTick(boolean isTickStart) throws Throwable {
		if(isTickStart) {
			ServerCore.onServerTickStart(lastServerStarted);//also used by the core mod!
		}
	}

	public void onPlayerTick(boolean isTickStart, boolean isServerSide, Player player) throws Throwable {
		if(isServerSide && isTickStart) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null)
				serverData.getPlayerTickHandler().onTick((ServerPlayer) player, serverData);
		}
	}

	public void onServerStopped(MinecraftServer server) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
		if(serverData != null)
			serverData.onStop();
		ServerCore.reset();
	}

	public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		new PartyCommandRegister().register(dispatcher, environment);
		new ClaimsCommandRegister().register(dispatcher, environment);
		new CommonCommandRegister().register(dispatcher, environment);
	}

	public boolean onLeftClickBlock(boolean isServerSide, Level world, BlockPos pos, Player player) {
		if(isServerSide) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onLeftClickBlockServer(serverData, pos, player);
		}
		return false;
	}

	public boolean onDestroyBlock(LevelAccessor world, BlockPos pos, Player player) {
		if(world instanceof ServerLevel serverLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onPlayerDestroyBlock(serverData, pos, serverLevel, player, true);
		}
		return false;
	}

	public boolean onEntityDestroyBlock(Level world, BlockPos pos, Entity entity) {
		if(world instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
			if(entity instanceof Player player)
				return serverData.getChunkProtection().onPlayerDestroyBlock(serverData, pos, world, player, false);
			else
				return serverData.getChunkProtection().onEntityDestroyBlock(serverData, (ServerLevel) world, entity, pos);
		}
		return false;
	}

	public boolean onRightClickBlock(boolean isServerSide, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitVec) {
		if(isServerSide) {
			if(player.isSpectator())
				return false;
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
			//cancelling both item and block use because players don't expect to use the item when the block would normally be used (and isn't because of protection)
			return serverData.getChunkProtection().onRightClickBlock(serverData, player, hand, pos, hitVec);
		}
		return false;
	}

	public boolean onItemRightClick(boolean isServerSide, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack itemStack) {
		if(isServerSide) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onItemRightClick(serverData, hand, itemStack, pos, player, true);
		}
		return false;
	}

	public boolean onMobGrief(Entity entity) {
		if(entity != null /*anonymous fireballs on Forge*/ && entity.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
			return serverData.getChunkProtection().onMobGrief(serverData, entity);
		}
		return false;
	}

	public boolean onLivingHurt(DamageSource source, Entity target) {
		if(target.getLevel() instanceof ServerLevel) {
			if(!(source instanceof EntityDamageSource) &&
					!source.isDamageHelmet()/*almost certainly something falling from the top*/ && source != DamageSource.DRAGON_BREATH &&
					!source.isFire() &&
					!source.isProjectile() && !source.isExplosion() && !source.getMsgId().startsWith("create.")/*create mod*/
					|| source == DamageSource.LAVA || source == DamageSource.HOT_FLOOR)
				return false;
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
			if(source.isFire())
				return serverData.getChunkProtection().onEntityFire(serverData, target);
			return serverData.getChunkProtection().onEntityInteract(serverData, source.getEntity(), source.getDirectEntity(), target, InteractionHand.MAIN_HAND, !source.isExplosion() && source.getEntity() == source.getDirectEntity(), true, false);
		}
		return false;
	}

	protected boolean onEntityAttack(Player player, Entity target) {
		boolean result = false;
		if(target.getLevel() instanceof ServerLevel) {
			if(!player.isSpectator()){
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
				result = serverData.getChunkProtection().onEntityInteract(serverData, player, player, target, InteractionHand.MAIN_HAND, true, true, false);
			}
			if(result)
				ServerCore.postResourcesDrop(player);//protected attack won't reach this call otherwise
		}
		return result;
	}

	public boolean onEntityInteract(Entity source, Entity target, InteractionHand hand) {
		if(target.getLevel() instanceof ServerLevel) {
			if(source.isSpectator())
				return false;
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
			return serverData.getChunkProtection().onEntityInteract(serverData, source, source, target, hand, true, false, false);
		}
		return false;
	}

	public boolean onInteractEntitySpecific(Entity source, Entity target, InteractionHand hand) {
		if(target.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
			return serverData.getChunkProtection().onEntityInteract(serverData, source, source, target, hand, true, false, true);
		}
		return false;
	}

	public void onExplosionDetonate(Level world, Explosion explosion, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
		if(world instanceof ServerLevel){
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(world.getServer());
			serverData.getChunkProtection().onExplosionDetonate(serverData, (ServerLevel) world, explosion, affectedEntities, affectedBlocks);
		}
	}

	public boolean onChorusFruit(Entity entity, Vec3 target){
		if(entity.getLevel() instanceof ServerLevel){
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
			return serverData.getChunkProtection().onChorusFruitTeleport(serverData, target, entity);
		}
		return false;
	}

	public boolean onEntityJoinWorld(Entity entity, Level world, boolean fromDisk) {
		if(world instanceof ServerLevel){
			try {
				if (!(entity instanceof LivingEntity) && ServerCore.getDyingDamageSourceForCurrentEntitySpawns(world.getServer().getTickCount()) != null) {
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
							serverData = ServerData.from(world.getServer());
					if (serverData == null)
						return false;
					return serverData.getChunkProtection().onLivingLootEntity(serverData, ServerCore.getDyingLivingForCurrentEntitySpawns(world.getServer().getTickCount()), entity, ServerCore.getDyingDamageSourceForCurrentEntitySpawns(world.getServer().getTickCount()));
				} else if (entity instanceof LightningBolt bolt) {
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
					serverData.getChunkProtection().onLightningBolt(serverData, bolt);
					return false;
				} else if (entity instanceof Projectile projectile && projectile.getOwner() != null && projectile.getOwner().getLevel() == entity.getLevel()) {
					SectionPos oldSection = SectionPos.of(projectile.getOwner().blockPosition());
					SectionPos newSection = SectionPos.of(entity.blockPosition());
					if (oldSection.x() != newSection.x() || oldSection.z() != newSection.z()) {
						IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
								serverData = ServerData.from(entity.getServer());
						serverData.getChunkProtection().onEntityEnterChunk(serverData, entity, projectile.getOwner().getX(), projectile.getOwner().getZ(), newSection, oldSection);
					}
					return false;
				} else if (!fromDisk && entity instanceof ItemEntity itemEntity) {
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
							serverData = ServerData.from(world.getServer());
					if (serverData == null)
						return false;
					return serverData.getChunkProtection().onItemAddedToWorld(serverData, itemEntity);
				}
			} finally {
				if (entity instanceof ItemEntity itemEntity) {
					if (itemEntity.getThrower() == null && ServerCore.getResourcesDropOwner() != null)//after the protection checks so that this isn't immediately affected by toss protection
						itemEntity.setThrower(ServerCore.getResourcesDropOwner().getUUID());

					if(itemEntity.getThrower() != null && ServerCore.getThrowerAccessor(itemEntity) == null){
						IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
								serverData = ServerData.from(world.getServer());
						if (serverData != null)
							serverData.getChunkProtection().setThrowerAccessor(itemEntity);
					}
				}
			}
		}
		return false;
	}

	protected void onEntityEnteringSection(Entity entity, SectionPos oldSection, SectionPos newSection, boolean chunkChanged){
		if(entity.getServer() != null && chunkChanged && (entity.xOld != 0 || entity.yOld != 0 || entity.zOld != 0)) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(entity.getServer());
			serverData.getChunkProtection().onEntityEnterChunk(serverData, entity, entity.xOld, entity.zOld, newSection, oldSection);
		}
	}

	protected void onPermissionsChanged(ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
		if(serverData != null)
			serverData.getPlayerPermissionChangeHandler().handle(player, serverData);
	}

	protected boolean onCropTrample(Entity entity, BlockPos pos) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onCropTrample(serverData, entity, pos);
	}

	public boolean onBucketUse(Entity entity, HitResult hitResult, ItemStack itemStack){
		if(entity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onBucketUse(serverData, entity, hitResult, itemStack);
	}

	protected boolean onEntityPlaceBlock(LevelAccessor levelAccessor, BlockPos pos, Entity entity) {
		//only supported by Forge atm
		if(levelAccessor instanceof ServerLevel level && level.getServer() != null) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
			if(serverData == null)
				return false;
			if(ServerCore.isHandlingFrostWalk())
				return false;
			return serverData.getChunkProtection().onEntityPlaceBlock(serverData, entity, level, pos, null);
		}
		return false;
	}

	protected boolean onEntityMultiPlaceBlock(LevelAccessor levelAccessor, Stream<BlockPos> positions, Entity entity) {
		//only supported by Forge atm
		if(levelAccessor instanceof ServerLevel level && level.getServer() != null) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(level.getServer());
			if(serverData == null)
				return false;
			if(ServerCore.isHandlingFrostWalk())
				return false;
			Set<ChunkPos> chunkPositions = new HashSet<>();
			Iterator<BlockPos> iterator = positions.iterator();
			while(iterator.hasNext()){
				BlockPos pos = iterator.next();
				if(chunkPositions.add(new ChunkPos(pos))) {
					if (serverData.getChunkProtection().onEntityPlaceBlock(serverData, entity, level, pos, null))
						return true;
				}
			}
			return false;
		}
		return false;
	}

	protected void onTagsUpdate(){
		if(lastServerStarted == null)
			return;
		if(!lastServerStarted.isRunning() || !lastServerStarted.isSameThread())
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(lastServerStarted);
		if(serverData == null)
			return;
		serverData.getChunkProtection().updateTagExceptions();
	}

	public boolean onItemPickup(Entity entity, ItemEntity itemEntity) {
		if(itemEntity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(itemEntity.getServer());
		return serverData.getChunkProtection().onItemPickup(serverData, entity, itemEntity);
	}

	public boolean onMobSpawn(Entity entity, double x, double y, double z, MobSpawnType spawnReason) {
		if(entity == null || entity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onMobSpawn(serverData, entity, x, y, z, spawnReason);
	}

}
