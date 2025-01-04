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
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.common.server.core.ServerCore;

@Mixin(Player.class)
public class MixinPlayer {

	@Inject(at = @At("HEAD"), method = "tick")
	public void onTickHead(CallbackInfo info){
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onPlayerTick(true, (Player)(Object)this);
	}

	@Inject(at = @At("TAIL"), method = "tick")
	public void onTickTail(CallbackInfo info){
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onPlayerTick(false, (Player)(Object)this);
	}

	@Inject(at = @At("HEAD"), method = "mayUseItemAt", cancellable = true)
	public void onMayUseItemAt(BlockPos blockPos, Direction direction, ItemStack itemStack, CallbackInfoReturnable<Boolean> info){
		if(!ServerCore.mayUseItemAt((Player)(Object)this, blockPos, direction, itemStack))
			info.setReturnValue(false);
	}

}
