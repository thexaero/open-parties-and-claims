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

import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.server.core.ServerCore;

@Mixin(value = SymmetryWandItem.class, priority = 1000001)
public class MixinNeoForgeSymmetryWandItem {

	@Inject(method = "remove", remap = false, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/simibubi/create/content/equipment/symmetryWand/mirror/SymmetryMirror;process(Ljava/util/Map;)V"))
	private static void onRemove(Level world, ItemStack wand, Player player, BlockPos pos, CallbackInfo ci){
		ServerCore.onCreateModSymmetryProcessed(world, player, false);
	}

}
