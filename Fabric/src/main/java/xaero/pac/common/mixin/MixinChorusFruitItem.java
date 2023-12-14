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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;

@Mixin(ChorusFruitItem.class)
public class MixinChorusFruitItem {

	@Inject(method = "finishUsingItem", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z"), cancellable = true)
	public void onFinishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> callbackInfoReturnable, ItemStack itemStack2, int i, double d, double e, double f, Vec3 vec3){
		Vec3 target = new Vec3(d, e, f);
		if(((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onChorusFruit(livingEntity, target))
			callbackInfoReturnable.setReturnValue(itemStack2);
	}

}
