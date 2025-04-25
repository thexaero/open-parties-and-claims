/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2025, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.core.ServerCoreFabric;

import java.util.function.BooleanSupplier;

@Mixin(value = ServerLevel.class, priority = 1000001)
public class MixinServerLevel {

	@Inject(method = "isPositionEntityTicking", at = @At("RETURN"), cancellable = true)
	public void onIsPositionEntityTicking(BlockPos pos, CallbackInfoReturnable<Boolean> infoReturnable){
		infoReturnable.setReturnValue(ServerCore.replaceIsPositionEntityTicking(infoReturnable.getReturnValue(), (ServerLevel)(Object)this, pos));
	}

	@Inject(method = "tickCustomSpawners", at = @At("HEAD"))
	public void preTickCustomSpawners(boolean b1, boolean b2, CallbackInfo ci){
		ServerCoreFabric.setMobSpawnTypeForNewEntities(EntitySpawnReason.NATURAL, ((ServerLevel)(Object)this).getServer());
	}

	@Inject(method = "tickCustomSpawners", at = @At("RETURN"))
	public void postTickCustomSpawners(boolean b1, boolean b2, CallbackInfo ci){
		ServerCoreFabric.resetMobSpawnTypeForNewEntities();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void preTick(BooleanSupplier booleanSupplier, CallbackInfo ci){
		ServerCore.preServerLevelTick((ServerLevel)(Object)this);
	}

}
