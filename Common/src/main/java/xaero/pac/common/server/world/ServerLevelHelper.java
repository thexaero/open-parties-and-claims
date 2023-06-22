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

package xaero.pac.common.server.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import xaero.pac.common.event.CommonEvents;

public class ServerLevelHelper {

	public static ServerLevel getServerLevel(Level level){//simply casting to ServerLevel doesn't work with wrappers that override Level (like in the case of Create deployers using buckets)
		if(level instanceof ServerLevel)
			return (ServerLevel) level;
		MinecraftServer server = getServer(level);
		return server == null ? null : server.getLevel(level.dimension());
	}

	public static MinecraftServer getServer(Level level){
		MinecraftServer result = level.getServer();
		if(result == null && CommonEvents.lastServerStarted != null && CommonEvents.lastServerStarted.isSameThread())
			return CommonEvents.lastServerStarted;
		return result;
	}

}
