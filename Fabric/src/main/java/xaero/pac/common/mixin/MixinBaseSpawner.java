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

package xaero.pac.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.server.core.ServerCoreFabric;

import java.util.Optional;

@Mixin(BaseSpawner.class)
public class MixinBaseSpawner {

	@ModifyVariable(method = "serverTick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/EntityType;by(Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"))
	public Optional<EntityType<?>> onServerTickPre(Optional<EntityType<?>> entityType, ServerLevel serverLevel, BlockPos blockPos){
		ServerCoreFabric.setMobSpawnTypeForNewEntities(MobSpawnType.SPAWNER, serverLevel.getServer());
		return entityType;
	}

	@ModifyArg(method = "serverTick", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseSpawner;delay(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
	public Level onServerTickDelay(Level dontChange){
		ServerCoreFabric.resetMobSpawnTypeForNewEntities();
		return dontChange;
	}

	@Inject(method = "serverTick", at = @At("TAIL"))
	public void onServerTickTail(ServerLevel serverLevel, BlockPos blockPos, CallbackInfo ci){
		ServerCoreFabric.resetMobSpawnTypeForNewEntities();
	}

}
