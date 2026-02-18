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

package xaero.pac.common.server.core;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.chunk.LevelChunk;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.common.server.world.IServerLevel;
import xaero.pac.common.server.world.ServerLevelHelper;
import xaero.pac.common.util.list.ListUniqueAdder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerCoreFabric {

	public static Entity MOB_GRIEFING_GAME_RULE_ENTITY = null;
	private static EntitySpawnReason MOB_SPAWN_TYPE_FOR_NEW_ENTITIES = null;
	private static int MOB_SPAWN_TYPE_FOR_NEW_ENTITIES_TICK;
	private static final Set<EntitySpawnReason> DISABLED_MOB_SPAWN_TYPES = new HashSet<>();

	public static List<BlockPos> EXPLOSION_BLOCK_POSITIONS;

	private static ListUniqueAdder<LevelChunk> TICKING_CHUNKS_UNIQUE_ADDER = new ListUniqueAdder<>();

	public static void tryToSetMobGriefingEntity(Entity entity){
		if(entity != null && ServerLevelHelper.getServerLevel(entity.level()) != null)
			MOB_GRIEFING_GAME_RULE_ENTITY = entity;
	}

	public static void setMobSpawnTypeForNewEntities(EntitySpawnReason mobSpawnTypeForNewEntities, MinecraftServer server) {
		if(DISABLED_MOB_SPAWN_TYPES.contains(mobSpawnTypeForNewEntities))
			return;
		if(!server.isSameThread())
			return;
		if(mobSpawnTypeForNewEntities != MOB_SPAWN_TYPE_FOR_NEW_ENTITIES || MOB_SPAWN_TYPE_FOR_NEW_ENTITIES_TICK != server.getTickCount()) {
			if(testMobSpawnTypeForNewEntities()) {
				setMobSpawnTypeForNewEntities(mobSpawnTypeForNewEntities, server);
				return;
			}
		}
		MOB_SPAWN_TYPE_FOR_NEW_ENTITIES = mobSpawnTypeForNewEntities;
		MOB_SPAWN_TYPE_FOR_NEW_ENTITIES_TICK = server.getTickCount();
	}

	public static void resetMobSpawnTypeForNewEntities() {
		MOB_SPAWN_TYPE_FOR_NEW_ENTITIES = null;
	}

	private static boolean testMobSpawnTypeForNewEntities(){
		if(MOB_SPAWN_TYPE_FOR_NEW_ENTITIES != null){
			OpenPartiesAndClaims.LOGGER.error("Mob spawn type capture for " + MOB_SPAWN_TYPE_FOR_NEW_ENTITIES + " isn't working properly. Likely a compatibility issue. Turning it off...");
			DISABLED_MOB_SPAWN_TYPES.add(MOB_SPAWN_TYPE_FOR_NEW_ENTITIES);
			resetMobSpawnTypeForNewEntities();
			return true;
		}
		return false;
	}

	public static EntitySpawnReason getMobSpawnTypeForNewEntities(MinecraftServer server) {
		if(server.getTickCount() != MOB_SPAWN_TYPE_FOR_NEW_ENTITIES_TICK)
			testMobSpawnTypeForNewEntities();
		return MOB_SPAWN_TYPE_FOR_NEW_ENTITIES;
	}

	public static void onReleaseUsingItem(LivingEntity livingEntity){
		if(livingEntity.getServer() == null)
			return;
		if(livingEntity.getUseItem().isEmpty())
			return;
		if(OpenPartiesAndClaimsFabric.INSTANCE.getCommonEvents().onItemUseStop(livingEntity, livingEntity.getUseItem()))
			livingEntity.stopUsingItem();
	}

	public static void onUpdatingUsingItem(LivingEntity livingEntity) {
		if(livingEntity.getServer() == null)
			return;
		if(livingEntity.getUseItem().isEmpty())
			return;
		OpenPartiesAndClaimsFabric.INSTANCE.getCommonEvents().onItemUseTick(livingEntity, livingEntity.getUseItem());
	}

	public static void onExplosionBlockPositions(List<BlockPos> list){
		EXPLOSION_BLOCK_POSITIONS = list;
	}

	public static void onExplosion(ServerExplosion serverExplosion, List<Entity> entityList, Level level) {
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onExplosionDetonate(serverExplosion, entityList, level);
		EXPLOSION_BLOCK_POSITIONS = null;
	}

	public static List<LevelChunk> onCollectTickingChunks(ServerChunkCache serverChunkCache, List<LevelChunk> list){
		list = TICKING_CHUNKS_UNIQUE_ADDER.setDestinationList(list);
		LongSet forceloadTickets = ((IServerLevel)serverChunkCache.getLevel()).getXaero_OPAC_forceloadTickets();
		for(long chunkPosLong : forceloadTickets){
			int chunkX = ChunkPos.getX(chunkPosLong);
			int chunkZ = ChunkPos.getZ(chunkPosLong);
			LevelChunk chunk = serverChunkCache.getChunk(chunkX, chunkZ, false);
			if(chunk == null)
				continue;
			list.add(chunk);
		}
		return list;
	}

	public static void reset() {
		MOB_GRIEFING_GAME_RULE_ENTITY = null;
		MOB_SPAWN_TYPE_FOR_NEW_ENTITIES = null;
		DISABLED_MOB_SPAWN_TYPES.clear();
	}
}
