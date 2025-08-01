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

import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.server.core.ServerCore;

@Mixin(value = SchematicannonBlockEntity.class, priority = 1000001)
public class MixinSchematicannonBlockEntity {

	@Shadow(remap = false)
	public SchematicPrinter printer;
	@Shadow(remap = false)
	public String statusMsg;
	@Shadow(remap = false)
	private boolean blockSkipped;

	@Inject(method = "tickPrinter", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/simibubi/create/content/schematics/SchematicPrinter;shouldPlaceCurrent(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/schematics/SchematicPrinter$PlacementPredicate;)Z"), cancellable = true)
	public void onTickPrinter(CallbackInfo ci){
		if(!ServerCore.canCreateCannonPlaceBlock((BlockEntity) (Object)this, printer.getCurrentTarget())) {
			statusMsg = "searching";
			blockSkipped = true;
			ci.cancel();
		}
	}

}
