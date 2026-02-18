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

package xaero.pac.common.mods.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.core.accessor.ICreateContraptionEntity;

public class CreateContraptionHelper {

	public static ResourceKey<Block> CONTRAPTION_CONTROLS_BLOCK = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("create", "contraption_controls"));
	public static final String PLACEMENT_POS_TAG = "xaero_OPAC_placementPos";

	public static void handleCreateContraptionAdded(Entity entity, ICreateContraptionEntity contraptionEntity){
		CompoundTag persistentData = Services.PLATFORM.getEntityAccess().getPersistentData(entity);
		if(ServerCore.isPlacingCreateContraption(entity.getServer())){
			CompoundTag placementPosData = new CompoundTag();
			placementPosData.putInt("x", entity.blockPosition().getX());
			placementPosData.putInt("y", entity.blockPosition().getY());
			placementPosData.putInt("z", entity.blockPosition().getZ());
			persistentData.put(PLACEMENT_POS_TAG, placementPosData);
		}
		if(persistentData.contains(PLACEMENT_POS_TAG, 10)) {
			CompoundTag placementPosData = persistentData.getCompound(PLACEMENT_POS_TAG);
			BlockPos persistentPlacementPos = new BlockPos(placementPosData.getInt("x"), placementPosData.getInt("y"), placementPosData.getInt("z"));
			contraptionEntity.getXaero_OPAC_contraption().setXaero_OPAC_placementPos(persistentPlacementPos);
		}
	}

}
