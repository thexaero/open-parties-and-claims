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

package xaero.pac.common.server.config;

import xaero.pac.common.server.info.ServerInfo;

import java.util.ArrayList;
import java.util.List;

public class ServerConfigUpdater {

	public void update(ServerInfo serverInfo){
		if(serverInfo.getLoadedVersion() == 0) {
			List<String> updatedEntitiesAllowedToGrief = new ArrayList<>(ServerConfig.CONFIG.entitiesAllowedToGrief.get());
			updatedEntitiesAllowedToGrief.add("interact$minecraft:potion");
			updatedEntitiesAllowedToGrief.add("interact$minecraft:trident");
			updatedEntitiesAllowedToGrief.add("interact$minecraft:(*_|)arrow");
			updatedEntitiesAllowedToGrief.add("interact$minecraft:ender_pearl");
			updatedEntitiesAllowedToGrief.add("interact$minecraft:egg");
			ServerConfig.CONFIG.entitiesAllowedToGrief.set(updatedEntitiesAllowedToGrief);

			List<String> updatedEntitiesAllowedToGriefEntities = new ArrayList<>(ServerConfig.CONFIG.entitiesAllowedToGriefEntities.get());
			updatedEntitiesAllowedToGriefEntities.add("interact$minecraft:potion");
			updatedEntitiesAllowedToGriefEntities.add("interact$minecraft:trident");
			updatedEntitiesAllowedToGriefEntities.add("interact$minecraft:(*_|)arrow");
			updatedEntitiesAllowedToGriefEntities.add("interact$minecraft:ender_pearl");
			updatedEntitiesAllowedToGriefEntities.add("interact$minecraft:egg");
			ServerConfig.CONFIG.entitiesAllowedToGriefEntities.set(updatedEntitiesAllowedToGriefEntities);
		}
		serverInfo.setDirty(true);
	}

}
