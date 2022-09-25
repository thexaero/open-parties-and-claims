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

package xaero.pac.common.server.player.config.io.serialization;

import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.UUID;

public class PlayerConfigDeserializationInfo {
	
	private final UUID id;
	private final PlayerConfigType type;
	private final String subId;

	private final int subIndex;
	
	public PlayerConfigDeserializationInfo(UUID id, PlayerConfigType type, String subId, int subIndex) {
		super();
		this.id = id;
		this.type = type;
		this.subId = subId;
		this.subIndex = subIndex;
	}

	public UUID getId() {
		return id;
	}

	public PlayerConfigType getType() {
		return type;
	}

	public String getSubId() {
		return subId;
	}

	public int getSubIndex() {
		return subIndex;
	}

}
