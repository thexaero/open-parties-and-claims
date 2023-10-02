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

package xaero.pac.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerRegisterAPI;
import xaero.pac.common.entity.IEntity;
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
import xaero.pac.common.server.parties.system.api.IPlayerPartySystemRegisterAPI;
import xaero.pac.common.server.parties.system.impl.DefaultPlayerPartySystem;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemRegisterAPI;
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public abstract class CommonEvents {

	protected final OpenPartiesAndClaims modMain;
	public static MinecraftServer lastServerStarted;
	private Class<?> createSuperGlueEntityClass;

	protected CommonEvents(OpenPartiesAndClaims modMain) {
		this.modMain = modMain;
		try {
			this.createSuperGlueEntityClass = Class.forName("com.simibubi.create.content.contraptions.glue.SuperGlueEntity");
		} catch (ClassNotFoundException ignored) {
			try {
				this.createSuperGlueEntityClass = Class.forName("com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity");
			} catch (ClassNotFoundException ignored1) {
			}
		}
	}

	public void onServerAboutToStart(MinecraftServer server) throws Throwable {
		lastServerStarted = server;
		OpenPartiesAndClaims.LOGGER.info("Initializing Open Parties and Claims for the server...");
		((IOpenPACMinecraftServer) lastServerStarted).setXaero_OPAC_ServerData(new ServerDataInitializer().init(modMain, lastServerStarted));

		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
		if(serverData != null) {
			modMain.getPacketHandler().onServerAboutToStart();
			try {
				serverData.getPlayerPermissionSystemManager().preRegister();
				serverData.getPlayerPartySystemManager().preRegister();
				serverData.getPlayerPartySystemManager().register("default", new DefaultPlayerPartySystem(serverData.getPartyManager()));
				fireAddonRegisterEvent(serverData);
			} finally {
				serverData.getPlayerPermissionSystemManager().postRegister();
				serverData.getPlayerPartySystemManager().postRegister();
			}
		}
	}

	protected abstract void fireAddonRegisterEvent(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData);

	public void onServerStarting(MinecraftServer server) {
		modMain.startupCrashHandler.check();
		ServerData.from(server).getServerLoadCallback().onLoad(server);
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
		if(player instanceof ServerPlayer serverPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, serverPlayer.serverLevel(), serverPlayer);
			}
		}
	}

	public void onPlayerChangedDimension(Player player) {
		if(player instanceof ServerPlayer serverPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, serverPlayer.serverLevel(), serverPlayer);
			}
		}
	}

	public void onPlayerLogIn(Player player) {
		if(player instanceof ServerPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null)
				ServerPlayerDataAPI.from((ServerPlayer) player);//handles login
		}
	}

	public void onPlayerClone(Player original, Player player) {
		if(original instanceof ServerPlayer) {
			//copy player data on respawn/clone
			((IOpenPACServerPlayer)player).setXaero_OPAC_PlayerData(ServerPlayerDataAPI.from((ServerPlayer)original));
		}
	}

	public void onPlayerLogOut(Player player) {
		if(player instanceof ServerPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerLogoutHandler().handle((ServerPlayer) player, serverData);
			}
		}
	}

	public void onServerTick(MinecraftServer server, boolean isTickStart) throws Throwable {
		if(isTickStart) {
			ServerCore.onServerTickStart(server);//also used by the core mod!
		}
	}

	public void onPlayerTick(boolean isTickStart, boolean isServerSide, Player player) throws Throwable {
		if(isServerSide && isTickStart && player instanceof ServerPlayer) {
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

	public boolean onLeftClickBlock(Level level, BlockPos pos, Player player) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		return serverData.getChunkProtection().onEntityDestroyBlock(serverData, player, serverLevel, pos, true);
	}

	public boolean onDestroyBlock(LevelAccessor levelAccessor, BlockPos pos, Player player) {
		if(!(levelAccessor instanceof Level level))
			return false;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		return serverData.getChunkProtection().onEntityDestroyBlock(serverData, player, serverLevel, pos, true);
	}

	public boolean onEntityDestroyBlock(Level level, BlockPos pos, Entity entity) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		return serverData.getChunkProtection().onEntityDestroyBlock(serverData, entity, serverLevel, pos, false);
	}

	public boolean onRightClickBlock(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitVec) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		if(player.isSpectator())
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		//cancelling both item and block use because players don't expect to use the item when the block would normally be used (and isn't because of protection)
		return serverData.getChunkProtection().onBlockInteraction(serverData, player, hand, null, serverLevel, pos, hitVec.getDirection(), false, true);
	}

	public boolean onItemRightClick(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemStack) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		return serverData.getChunkProtection().onItemRightClick(serverData, hand, itemStack, pos, player, true);
	}

	public boolean onMobGrief(Entity entity) {
		if(entity == null /*anonymous fireballs on Forge*/)
			return false;
		if(entity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onMobGrief(serverData, entity);
	}

	public boolean onLivingHurt(DamageSource source, Entity target) {
		if(target.getServer() == null)
			return false;
		boolean isFire = source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE);
		if(source.getEntity() == null && source.getDirectEntity() == null &&
				(
					!source.is(DamageTypeTags.DAMAGES_HELMET)/*almost certainly something falling from the top*/ && !source.is(DamageTypes.DRAGON_BREATH) &&
					!isFire &&
					!source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION) && !source.getMsgId().startsWith("create.")/*create mod*/
					|| source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)
				))
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
		if(isFire)
			return serverData.getChunkProtection().onEntityFire(serverData, target);
		return serverData.getChunkProtection().onEntityInteraction(serverData, source.getEntity(), source.getDirectEntity(), target, null, InteractionHand.MAIN_HAND, true, true);
	}

	protected boolean onEntityAttack(Player player, Entity target) {
		if(target == null || target.getServer() == null)
			return false;
		boolean result = false;
		if(!player.isSpectator()){
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
			result = serverData.getChunkProtection().onEntityInteraction(serverData, player, player, target, null, InteractionHand.MAIN_HAND, true, true);
		}
		if(result)
			ServerCore.postResourcesDrop(player);//protected attack won't reach this call otherwise
		return result;
	}

	public boolean onEntityInteract(Entity source, Entity target, InteractionHand hand) {
		if(target.getServer() == null)
			return false;
		if(source.isSpectator())
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
		return serverData.getChunkProtection().onEntityInteraction(serverData, source, source, target, null, hand, false, false);
	}

	public boolean onInteractEntitySpecific(Entity source, Entity target, InteractionHand hand) {
		if(target.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(target.getServer());
		return serverData.getChunkProtection().onEntityInteraction(serverData, source, source, target, null, hand, false, true);
	}

	public void onExplosionDetonate(Level level, Explosion explosion, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		serverData.getChunkProtection().onExplosionDetonate(serverData, serverLevel, explosion, affectedEntities, affectedBlocks);
	}

	public boolean onChorusFruit(Entity entity, Vec3 target){
		if(entity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onChorusFruitTeleport(serverData, target, entity);
	}

	public boolean onEntityJoinWorld(Entity entity, Level level, boolean fromDisk) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		if(!serverLevel.getServer().isSameThread())
			return false;
		try {
			Projectile spawnerProjectile = ServerCore.getHitProjectile(serverLevel.getServer().getTickCount());
			if(spawnerProjectile != null) {
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(spawnerProjectile.getServer());
				if(serverData.getChunkProtection().onProjectileHitSpawnedEntity(serverData, spawnerProjectile, entity))
					return true;
			}
			if (!(entity instanceof LivingEntity) && ServerCore.getDyingDamageSourceForCurrentEntitySpawns(serverLevel.getServer().getTickCount()) != null) {
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(serverLevel.getServer());
				if (serverData == null)
					return false;
				if(serverData.getChunkProtection().onLivingLootEntity(serverData, ServerCore.getDyingLivingForCurrentEntitySpawns(serverLevel.getServer().getTickCount()), entity, ServerCore.getDyingDamageSourceForCurrentEntitySpawns(serverLevel.getServer().getTickCount())))
					return true;
			}
			if (entity instanceof LightningBolt bolt) {
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
				serverData.getChunkProtection().onLightningBolt(serverData, bolt);
				return false;
			} else if (entity instanceof Projectile projectile && projectile.getOwner() != null && projectile.getOwner().level() == entity.level()) {
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
						serverData = ServerData.from(serverLevel.getServer());
				if (serverData == null)
					return false;
				return serverData.getChunkProtection().onItemAddedToWorld(serverData, itemEntity);
			} else if(!fromDisk && entity.getClass() == createSuperGlueEntityClass){
				BlockPos contraptionAnchor = ServerCore.getFreshAddedSuperGlueAnchor(serverLevel);
				if(contraptionAnchor != null){
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
							serverData = ServerData.from(entity.getServer());
					ServerCore.postCreateDisassembleSuperGlue();
					return serverData.getChunkProtection().onCreateGlueEntityFromAnchor(serverData, entity, contraptionAnchor);
				}
			}
		} finally {
			if(((IEntity)entity).getXaero_OPAC_lastChunkEntryDimension() == null)
				((IEntity)entity).setXaero_OPAC_lastChunkEntryDimension(entity.level().dimension());
			if (entity instanceof ItemEntity itemEntity) {
				if (ServerCore.getItemEntityThrower(itemEntity) == null && ServerCore.getResourcesDropOwner() != null)//after the protection checks so that this isn't immediately affected by toss protection
					itemEntity.setThrower(ServerCore.getResourcesDropOwner().getUUID());

				if(ServerCore.getItemEntityThrower(itemEntity) != null && ServerCore.getThrowerAccessor(itemEntity) == null){
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
							serverData = ServerData.from(serverLevel.getServer());
					if (serverData != null)
						serverData.getChunkProtection().setThrowerAccessor(itemEntity);
				}
			}
		}
		return false;
	}

	protected void onEntityEnteringSection(Entity entity, SectionPos oldSection, SectionPos newSection, boolean chunkChanged){
		if(entity.getServer() != null && chunkChanged && (entity.xOld != 0 || entity.yOld != 0 || entity.zOld != 0)) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(entity.getServer());
			if(entity.level().dimension().equals(((IEntity)entity).getXaero_OPAC_lastChunkEntryDimension()))
				serverData.getChunkProtection().onEntityEnterChunk(serverData, entity, entity.xOld, entity.zOld, newSection, oldSection);
			((IEntity)entity).setXaero_OPAC_lastChunkEntryDimension(entity.level().dimension());
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

	public boolean onBucketUse(Entity entity, Level level, HitResult hitResult, ItemStack itemStack){
		if(entity.getServer() == null)
			return false;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onBucketUse(serverData, entity, serverLevel, hitResult, itemStack);
	}

	protected boolean onEntityPlaceBlock(LevelAccessor levelAccessor, BlockPos pos, Entity entity) {
		//only supported by Forge atm
		if(!(levelAccessor instanceof Level level))
			return false;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		if(ServerCore.isHandlingFrostWalk())
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return false;
		return serverData.getChunkProtection().onEntityPlaceBlock(serverData, entity, serverLevel, pos, null);
	}

	protected boolean onEntityMultiPlaceBlock(LevelAccessor levelAccessor, Stream<BlockPos> positions, Entity entity) {
		//only supported by Forge atm
		if(!(levelAccessor instanceof Level level))
			return false;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return false;
		if(ServerCore.isHandlingFrostWalk())
			return false;
		Set<ChunkPos> chunkPositions = new HashSet<>();
		Iterator<BlockPos> iterator = positions.iterator();
		while(iterator.hasNext()){
			BlockPos pos = iterator.next();
			if(chunkPositions.add(new ChunkPos(pos))) {
				if (serverData.getChunkProtection().onEntityPlaceBlock(serverData, entity, serverLevel, pos, null))
					return true;
			}
		}
		return false;
	}

	protected void onTagsUpdate(){
		//TODO should properly get the server instance that the tags are updated for
		if(lastServerStarted == null || !lastServerStarted.isRunning() || !lastServerStarted.isSameThread())
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(lastServerStarted);
		if(serverData == null)
			return;
		serverData.getChunkProtection().updateTagExceptions(lastServerStarted);
	}

	public boolean onItemPickup(Entity entity, ItemEntity itemEntity) {
		if(itemEntity.getServer() == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(itemEntity.getServer());
		return serverData.getChunkProtection().onItemPickup(serverData, entity, itemEntity);
	}

	public boolean onMobSpawn(Entity entity, double x, double y, double z, MobSpawnType spawnReason) {
		if(spawnReason == MobSpawnType.CHUNK_GENERATION)
			return false;
		if(entity == null || entity.getServer() == null || !entity.getServer().isSameThread())
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		return serverData.getChunkProtection().onMobSpawn(serverData, entity, x, y, z, spawnReason);
	}

	public void onServerDataReload(ResourceManager resourceManager){
		//TODO should properly get the server instance that data is reloaded for
		if(lastServerStarted != null && lastServerStarted.isSameThread()){
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(lastServerStarted);
			serverData.onServerResourcesReload(resourceManager);
		}
	}

	public void onAddonRegister(MinecraftServer server, IPlayerPermissionSystemRegisterAPI permissionSystemManagerAPI, IPlayerPartySystemRegisterAPI partySystemManagerAPI, IClaimsManagerTrackerRegisterAPI claimsManagerTrackerAPI){
		//built-in "addons"
		if(modMain.getModSupport().LUCK_PERMS)
			permissionSystemManagerAPI.register("luck_perms", modMain.getModSupport().getLuckPerms().getPermissionSystem());
		if(modMain.getModSupport().FTB_RANKS)
			permissionSystemManagerAPI.register("ftb_ranks", modMain.getModSupport().getFTBRanksSupport().getPermissionSystem());
		if(modMain.getModSupport().PROMETHEUS)
			permissionSystemManagerAPI.register("prometheus", modMain.getModSupport().getPrometheusSupport().getPermissionSystem());

		if(modMain.getModSupport().FTB_TEAMS)
			partySystemManagerAPI.register("ftb_teams", modMain.getModSupport().getFTBTeamsSupport().getPartySystem());
		if(modMain.getModSupport().ARGONAUTS) {
			partySystemManagerAPI.register("argonauts", modMain.getModSupport().getArgonautsSupport().getPartySystem());
			partySystemManagerAPI.register("argonauts_guilds", modMain.getModSupport().getArgonautsSupport().createGuildSystem(server));
		}
	}

}
