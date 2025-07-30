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

package xaero.pac.common.mixin.create;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.core.accessor.ICreateContraption;

@Mixin(value = Contraption.class, priority = 1000001)
public abstract class MixinContraption implements ICreateContraption {

	@Shadow
	public BlockPos anchor;
	private BlockPos xaero_OPAC_placementPos;

	@Inject(method = "movementAllowed", at = @At("HEAD"), cancellable = true)
	public void onMovementAllowed(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
		if(!ServerCore.isCreateModAllowed(level, pos, this))
			cir.setReturnValue(false);
	}

	@ModifyArg(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	public BlockPos onAddBlocksToWorld(BlockPos posToCapture){
		ServerCore.CAPTURED_TARGET_POS = posToCapture;
		return posToCapture;
	}

	@ModifyVariable(method = "addBlocksToWorld", name = "blockState", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	public BlockState onAddBlocksToWorld(BlockState actual, Level level, StructureTransform structureTransform){
		return ServerCore.replaceBlockFetchOnCreateModBreak(actual, level, this);
	}

	@Inject(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	public void preAddSuperGlueToWorld(Level level, StructureTransform structureTransform, CallbackInfo ci){
		ServerCore.preCreateDisassembleSuperGlue(level, this);
	}

	@Inject(method = "addBlocksToWorld", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	public void postAddSuperGlueToWorld(Level level, StructureTransform structureTransform, CallbackInfo ci){
		ServerCore.postCreateDisassembleSuperGlue();
	}

	@Override
	public BlockPos getXaero_OPAC_anchor() {
		return anchor;
	}

	@Override
	public BlockPos getXaero_OPAC_placementPos() {
		return xaero_OPAC_placementPos;
	}

	@Override
	public void setXaero_OPAC_placementPos(BlockPos pos) {
		xaero_OPAC_placementPos = pos;
	}

}
