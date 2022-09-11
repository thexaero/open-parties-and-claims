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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;

@Mixin(targets = "net/minecraft/world/level/entity/TransientEntitySectionManager$Callback")
public class MixinTransientEntitySectionManagerCallback {

	private long OPAC_oldSectionKey;

	@Shadow
	private long currentSectionKey;
	@Shadow
	private EntityAccess entity;

	@Inject(at = @At("HEAD"), method = "onMove")
	public void onOnMoveHead(CallbackInfo ci){
		OPAC_oldSectionKey = currentSectionKey;
	}

	@Inject(at = @At("RETURN"), method = "onMove")
	public void onOnMoveReturn(CallbackInfo ci){
		if (entity instanceof Entity realEntity && currentSectionKey != OPAC_oldSectionKey)
			((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onEntityEnteringSection(realEntity, OPAC_oldSectionKey, currentSectionKey);
	}

}
