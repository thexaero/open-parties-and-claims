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

package xaero.pac.common.mixin.create;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.core.accessor.ICreateContraption;

@Mixin(DeployerMovementBehaviour.class)
public class MixinDeployerMovementBehaviour {

	private MovementContext OPAC_lastMovementContext;
	private BlockPos OPAC_lastPos;

	@ModifyVariable(method = "tick", remap = false, at = @At(value = "INVOKE_ASSIGN", target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerMovementBehaviour;getPlayer(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;)Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;"))
	public DeployerFakePlayer onTick(DeployerFakePlayer deployerFakePlayer, MovementContext movementContext){
		Pair<BlockPos, Float> blockBreakingProgress = ((MixinAccessorDeployerFakePlayer)deployerFakePlayer).getBlockBreakingProgress();
		if(blockBreakingProgress != null) {
			OPAC_lastMovementContext = movementContext;
			OPAC_lastPos = blockBreakingProgress.getKey();
		}
		return deployerFakePlayer;
	}

	@Inject(method = "visitNewPosition", remap = false,  at = @At("HEAD"))
	public void onVisitNewPosition(MovementContext movementContext, BlockPos pos, CallbackInfo ci){
		OPAC_lastMovementContext = movementContext;
		OPAC_lastPos = pos;
	}

	@Inject(method = "activate", remap = false, at = @At("HEAD"), cancellable = true)
	public void onActivate(CallbackInfo ci){
		if(OPAC_lastPos == null || !ServerCore.isCreateDeployerBlockInteractionAllowed(OPAC_lastMovementContext.world, (ICreateContraption) OPAC_lastMovementContext.contraption, OPAC_lastPos))
			ci.cancel();
	}

}
