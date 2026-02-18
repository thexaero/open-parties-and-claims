/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024-2026, Xaero <xaero1996@gmail.com> and contributors
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
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;

@Mixin(OpenEndedPipe.class)
public class MixinFabricOpenEndedPipe {

	@Shadow
	private Level world;
	@Shadow
	private BlockPos pos;
	@Shadow
	private BlockPos outputPos;

	@Inject(method = "provideFluidToSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), cancellable = true)
	public void onProvideFluidToSpace(FluidStack fluid, TransactionContext ctx, CallbackInfoReturnable<Boolean> cir){
		if(!ServerCore.canCreatePipeAffectBlock(world, pos, outputPos, ctx.nestingDepth() > 0))
			cir.setReturnValue(false);
	}

	@Inject(method = "removeFluidFromSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), cancellable = true)
	public void onRemoveFluidFromSpace(TransactionContext ctx, CallbackInfoReturnable<FluidStack> cir){
		if(!ServerCore.canCreatePipeAffectBlock(world, pos, outputPos, ctx.nestingDepth() > 0))
			cir.setReturnValue(FluidStack.EMPTY);
	}

}
