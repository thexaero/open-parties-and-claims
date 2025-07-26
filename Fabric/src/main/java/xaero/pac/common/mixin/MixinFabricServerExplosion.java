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

package xaero.pac.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.pac.common.server.core.ServerCoreFabric;

import java.util.List;

@Mixin(value = ServerExplosion.class, priority = 1000001)
public class MixinFabricServerExplosion {

	@Shadow
	private ServerLevel level;

	@ModifyVariable(method = "explode", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/ServerExplosion;calculateExplodedPositions()Ljava/util/List;"))
	public List<BlockPos> onCalculateBlocks(List<BlockPos> blockPosList){
		ServerCoreFabric.onExplosionBlockPositions(blockPosList);
		return blockPosList;
	}

	@ModifyVariable(method = "hurtEntities", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
	public List<Entity> onHurtEntities(List<Entity> entityList){
		ServerCoreFabric.onExplosion((ServerExplosion)(Object)this, entityList, level);
		return entityList;
	}

}
