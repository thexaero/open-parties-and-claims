/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ServerCoreNeoForge {

	public static boolean isCreateGlueSelectionAllowed(BlockPos from, BlockPos to, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateGlueSelectionAllowed(from, to, player);
	}

	public static boolean isCreateGlueRemovalAllowed(int entityId, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateGlueRemovalAllowed(entityId, player);
	}

	public static boolean isCreateTileEntityPacketAllowed(BlockPos pos, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateTileEntityPacketAllowed(pos, player);
	}

	public static boolean isCreateContraptionInteractionPacketAllowed(int contraptionId, InteractionHand interactionHand, BlockPos localPos, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateContraptionInteractionPacketAllowed(contraptionId, interactionHand, localPos, player);
	}

	public static boolean isCreateContraptionControlsPacketAllowed(int contraptionId, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateContraptionControlsPacketAllowed(contraptionId, player);
	}

	public static boolean isCreateTrainRelocationPacketAllowed(int contraptionId, BlockPos pos, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateTrainRelocationPacketAllowed(contraptionId, pos, player);
	}

	public static boolean isCreateTrainControlsPacketAllowed(int contraptionId, ServerPlayer player){
		if(player == null)
			return true;
		return ServerCore.isCreateTrainControlsPacketAllowed(contraptionId, player);
	}

	public static FluidStack onCreatePipeCollectBlock(Level level, BlockPos from, BlockPos to, boolean simulate){
		if(ServerCore.canCreatePipeAffectBlock(level, from, to, simulate))
			return null;
		return FluidStack.EMPTY;
	}

}
