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

package xaero.pac.common.server.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;

public class ServerCoreFabric {

	public static Entity MOB_GRIEFING_GAME_RULE_ENTITY = null;
	public static Block CALCULATING_PRESSURE_PLATE_WEIGHT = null;
	public static BlockPos CALCULATING_PRESSURE_PLATE_WEIGHT_POS = null;
	public static MobSpawnType MOB_SPAWN_TYPE_FOR_NEW_ENTITIES = null;

	public static void tryToSetMobGriefingEntity(Entity entity){
		if(entity != null && entity.level instanceof ServerLevel)
			MOB_GRIEFING_GAME_RULE_ENTITY = entity;
	}

}
