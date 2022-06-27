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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.entity.ILivingEntity;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements ILivingEntity {

	private CompoundTag xaero_OPAC_persistentData;

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	public void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo info) {
		if(xaero_OPAC_persistentData != null && !xaero_OPAC_persistentData.isEmpty())
			tag.put("xaero_OPAC_persistentData", xaero_OPAC_persistentData.copy());
	}

	@Override
	public CompoundTag getPersistentData() {
		if(xaero_OPAC_persistentData == null)
			xaero_OPAC_persistentData = new CompoundTag();
		return xaero_OPAC_persistentData;
	}

}
