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

import com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;

@Mixin(MinecartContraptionItem.class)
public class MixinMinecartContraptionItem {

	@Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/mounted/MinecartContraptionItem;addContraptionToMinecart(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/core/Direction;)V"))
	public void onUseOnPre(UseOnContext context, CallbackInfoReturnable<Boolean> cir){
		ServerCore.preMinecartContraptionPlaced(context);
	}

	@Inject(method = "useOn", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/simibubi/create/content/contraptions/mounted/MinecartContraptionItem;addContraptionToMinecart(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/core/Direction;)V"))
	public void onUseOnPost(UseOnContext context, CallbackInfoReturnable<Boolean> cir){
		ServerCore.postMinecartContraptionPlaced();
	}

}
