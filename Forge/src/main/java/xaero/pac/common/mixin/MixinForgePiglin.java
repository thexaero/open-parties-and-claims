/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2025-2026, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.pac.common.server.core.ServerCore;

@Mixin(Piglin.class)
public class MixinForgePiglin {

	@ModifyVariable(method = "wantsToPickUp", at = @At("HEAD"), index = 1)
	public ItemStack onWantsToPickupPre(ItemStack itemStack){
		ServerCore.forgePreItemMobGriefingCheck((Mob)(Object)this);
		return itemStack;
	}

	@ModifyArg(method = "wantsToPickUp", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;wantsToPickup(Lnet/minecraft/world/entity/monster/piglin/Piglin;Lnet/minecraft/world/item/ItemStack;)Z"))
	public Piglin onWantsToPickupPost(Piglin piglin){
		ServerCore.forgePostItemMobGriefingCheck(piglin);
		return piglin;
	}

}
