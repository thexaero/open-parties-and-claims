/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.player.config.group.custom.io.serializer;

import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;

import java.util.UUID;

public class CustomPlayerGroupMemberIOSerializer {

	public String serialize(CustomPlayerGroupMember memberData){
		StringBuilder builder = new StringBuilder();
		if(memberData.getDisplayName() != null)
			builder.append(memberData.getDisplayName());
		builder.append("|");
		if(memberData.getId() != null)
			builder.append(memberData.getId());
		return builder.toString();
	}

	public CustomPlayerGroupMember deserialize(String configValue){
		int separatorIndex = configValue.indexOf("|");
		if (separatorIndex < 1 && configValue.length() == separatorIndex + 1)
			throw new IllegalArgumentException(
					"Custom player group member config values must at least contain either name or UUID."
			);
		UUID uuid = null;
		if(separatorIndex == -1)
			separatorIndex = configValue.length();
		else {
			String uuidString = configValue.substring(separatorIndex + 1);
			if(!uuidString.isEmpty())
				try {
					uuid = UUID.fromString(uuidString);
				} catch(Throwable t){
					throw new IllegalArgumentException(
							"Custom player group member config value contains a misformatted UUID: " + uuidString +
									". Details: " + t.getMessage()
					);
				}
		}
		String name = separatorIndex == 0 ? null : configValue.substring(0, separatorIndex);
		return new CustomPlayerGroupMember(uuid, name);
	}

}
