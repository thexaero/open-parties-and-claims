/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024-2025, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.mixin.create;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;

@Mixin(OpenEndedPipe.class)
public class MixinNeoForgeOpenEndedPipe {

	@Shadow
	private Level world;
	@Shadow
	private BlockPos pos;
	@Shadow
	private BlockPos outputPos;

	@Inject(method = "provideFluidToSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), cancellable = true)
	public void onProvideFluidToSpace(FluidStack fluid, boolean simulate, CallbackInfoReturnable<Boolean> cir){
		if(!ServerCore.canCreatePipeAffectBlock(world, pos, outputPos, simulate))
			cir.setReturnValue(false);
	}

	@Inject(method = "removeFluidFromSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), cancellable = true)
	public void onRemoveFluidFromSpace(boolean simulate, CallbackInfoReturnable<FluidStack> cir){
		if(!ServerCore.canCreatePipeAffectBlock(world, pos, outputPos, simulate))
			cir.setReturnValue(FluidStack.EMPTY);
	}

}
