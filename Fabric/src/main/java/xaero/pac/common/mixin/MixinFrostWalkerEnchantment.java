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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.server.core.ServerCore;

@Mixin(FrostWalkerEnchantment.class)
public class MixinFrostWalkerEnchantment {

	@Inject(method = "onEntityMoved", at = @At("HEAD"), cancellable = true)
	private static void onOnEntityMovedPre(LivingEntity living, Level level, BlockPos pos, int a, CallbackInfo ci){
		if(ServerCore.preFrostWalkHandle(living, level))
			ci.cancel();
	}

	@ModifyArg(method = "onEntityMoved", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private static BlockPos onOnEntityMovedPreBlockState(BlockPos pos){
		return ServerCore.preBlockStateFetchOnFrostwalk(pos);
	}

	@Inject(method = "onEntityMoved", at = @At("RETURN"))
	private static void onOnEntityMovedPost(LivingEntity living, Level level, BlockPos pos, int a, CallbackInfo ci){
		ServerCore.postFrostWalkHandle(level);
	}

}
