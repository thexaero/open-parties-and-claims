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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntityGetter.class)
public interface MixinEntityGetter {

	@Inject(at = @At("RETURN"), method = "getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;")
	default <T extends Entity> void onGetEntitiesOfClass(Class<T> c, AABB aabb, Predicate<? super T> predicate, CallbackInfoReturnable<List<T>> cir){
		if(ServerCore.DETECTING_ENTITY_BLOCK_COLLISION != null){
			if(!(this instanceof ServerLevel))
				return;
			ServerCore.onEntitiesPushBlock(cir.getReturnValue(), ServerCore.DETECTING_ENTITY_BLOCK_COLLISION, ServerCore.DETECTING_ENTITY_BLOCK_COLLISION_POS);
			if(!(ServerCore.DETECTING_ENTITY_BLOCK_COLLISION instanceof BasePressurePlateBlock))
				ServerCore.DETECTING_ENTITY_BLOCK_COLLISION = null;
		}
	}

}
