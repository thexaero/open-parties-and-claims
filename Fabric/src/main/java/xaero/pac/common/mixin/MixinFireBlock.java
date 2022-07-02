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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;

import java.util.Random;

@Mixin(FireBlock.class)
public class MixinFireBlock {

	@Inject(method = "checkBurnOut", at = @At("HEAD"), cancellable = true)
	public void onCheckBurnOut(Level level, BlockPos blockPos, int i, RandomSource random, int j, CallbackInfo info){
		if(!ServerCore.canSpreadFire(level, blockPos))
			info.cancel();
	}

	@Inject(method = "getIgniteOdds", at = @At("HEAD"), cancellable = true)
	public void onGetFireOdds(LevelReader levelReader, BlockPos blockPos, CallbackInfoReturnable<Integer> info){
		if(!ServerCore.canSpreadFire(levelReader, blockPos))
			info.setReturnValue(0);
	}

}
