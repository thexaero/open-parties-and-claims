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

package xaero.pac.common.server.world;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public class ServerChunkCacheAccessNeoForge implements IServerChunkCacheAccess {

	@Override
	public <T> void addRegionTicket(ServerChunkCache serverChunkCache, TicketType<T> type, ChunkPos pos, int distance, T value, boolean forceTicks) {
		serverChunkCache.addRegionTicket(type, pos, distance, value, forceTicks);
	}

	@Override
	public <T> void removeRegionTicket(ServerChunkCache serverChunkCache, TicketType<T> type, ChunkPos pos, int distance, T value, boolean forceTicks) {
		serverChunkCache.removeRegionTicket(type, pos, distance, value, forceTicks);
	}

}
