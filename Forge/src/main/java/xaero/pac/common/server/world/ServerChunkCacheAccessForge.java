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

package xaero.pac.common.server.world;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.platform.Services;

public class ServerChunkCacheAccessForge implements IServerChunkCacheAccess {

	@Override
	public <T> void addRegionTicket(ServerChunkCache serverChunkCache, TicketType<T> type, ChunkPos pos, int distance, T value, boolean forceTicks) {
		//because of a bug, requires 2 tickets for forced ticks
		//addRegionTicket is vanilla, which is required by default to add a ticket in the tickingTicketsTracker
		//registerTickingTicket is added by forge and adds the ticket to forge forcedTickets, which makes the game ignore all tick requirements except tickingTicketsTracker

		serverChunkCache.addRegionTicket(type, pos, distance, value);
		if(forceTicks)
			serverChunkCache.registerTickingTicket(type, pos, distance, value);
	}

	@Override
	public <T> void removeRegionTicket(ServerChunkCache serverChunkCache, TicketType<T> type, ChunkPos pos, int distance, T value, boolean forceTicks) {
		serverChunkCache.removeRegionTicket(type, pos, distance, value);
		if(forceTicks)
			serverChunkCache.releaseTickingTicket(type, pos, distance, value);
	}

}
