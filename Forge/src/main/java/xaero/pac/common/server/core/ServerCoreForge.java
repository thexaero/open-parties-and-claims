/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;

public class ServerCoreForge {

	public static boolean isCreateGlueSelectionAllowed(BlockPos from, BlockPos to, CustomPayloadEvent.Context ctx){
		return ServerCore.isCreateGlueSelectionAllowed(from, to, ctx.getSender());
	}

	public static boolean isCreateGlueRemovalAllowed(int entityId, CustomPayloadEvent.Context ctx){
		return ServerCore.isCreateGlueRemovalAllowed(entityId, ctx.getSender());
	}

	public static boolean isCreateTileEntityPacketAllowed(BlockPos pos, CustomPayloadEvent.Context ctx){
		ServerPlayer player = ctx.getSender();
		if (player == null)
			return true;
		return ServerCore.isCreateTileEntityPacketAllowed(pos, player);
	}

	public static boolean isCreateContraptionInteractionPacketAllowed(int contraptionId, InteractionHand interactionHand, CustomPayloadEvent.Context ctx){
		return ServerCore.isCreateContraptionInteractionPacketAllowed(contraptionId, interactionHand, ctx.getSender());
	}

	public static boolean isCreateTrainRelocationPacketAllowed(int contraptionId, BlockPos pos, CustomPayloadEvent.Context ctx){
		return ServerCore.isCreateTrainRelocationPacketAllowed(contraptionId, pos, ctx.getSender());
	}

	public static boolean isCreateTrainControlsPacketAllowed(int contraptionId, CustomPayloadEvent.Context ctx){
		return ServerCore.isCreateTrainControlsPacketAllowed(contraptionId, ctx.getSender());
	}

	public static List<? extends Entity> onPressurePlateEntityCount(List<? extends Entity> entities) {
		if (ServerCore.DETECTING_ENTITY_BLOCK_COLLISION != null)
			ServerCore.onEntitiesPushBlock(entities, ServerCore.DETECTING_ENTITY_BLOCK_COLLISION, ServerCore.DETECTING_ENTITY_BLOCK_COLLISION_POS);
		return entities;
	}

}
