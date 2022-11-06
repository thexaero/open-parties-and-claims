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

package xaero.pac.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.core.ServerCoreFabric;

import java.util.Random;

@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner {

	@Inject(method = "isValidPositionForMob", at = @At("RETURN"), cancellable = true)
	private static void onIsValidPositionForMob(ServerLevel serverLevel, Mob mob, double d, CallbackInfoReturnable<Boolean> cir){
		if(cir.getReturnValue())
			cir.setReturnValue(!OpenPartiesAndClaims.INSTANCE.getCommonEvents().onMobSpawn(mob, mob.getX(), mob.getY(), mob.getZ(), MobSpawnType.NATURAL));
	}

	@ModifyVariable(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/NaturalSpawner;getTopNonCollidingPos(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/entity/EntityType;II)Lnet/minecraft/core/BlockPos;"))
	private static BlockPos onSpawnMobsForChunkGenerationPre(BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Holder<Biome> biomeHolder, ChunkPos chunkPos, Random random){
		ServerCoreFabric.setMobSpawnTypeForNewEntities(MobSpawnType.CHUNK_GENERATION, serverLevelAccessor.getServer());
		return blockPos;
	}

	@Inject(method = "spawnMobsForChunkGeneration", at = @At("RETURN"))
	private static void onSpawnMobsForChunkGenerationPost(ServerLevelAccessor levelAccessor, Holder<Biome> biomeHolder, ChunkPos chunkPos, Random random, CallbackInfo ci){
		ServerCoreFabric.resetMobSpawnTypeForNewEntities();
	}

}
