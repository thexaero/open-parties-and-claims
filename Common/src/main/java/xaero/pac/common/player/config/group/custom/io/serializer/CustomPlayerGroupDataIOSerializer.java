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

import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;

public class CustomPlayerGroupDataIOSerializer {

	private final String FORMAT_ERROR_HINT =
			"A custom player group must be formatted as " +
			"group_id[coma-separated included groups]{coma-separated included players}, for example " +
			"Friends[Best-friends, Siblings]{PKMaster420|5733bf9b-a178-4d15-a68b-114444611e6e, " +
			"N00bSl4y3r73|77594681-0078-4326-a706-589b12643d15, WillAutoFetchUUID123}\n" +
			"Characters allowed in group IDs: " + CustomPlayerConfigGroupData.VALID_ID_CHARS_DISPLAY;

	private final CustomPlayerGroupMemberIOSerializer memberSerializer;

	public CustomPlayerGroupDataIOSerializer() {
		this.memberSerializer = new CustomPlayerGroupMemberIOSerializer();
	}

	public String serialize(CustomPlayerConfigGroupData groupData){
		StringBuilder builder = new StringBuilder(groupData.getId());
		builder.append("[");
		boolean first = true;
		for (String directGroup : groupData.getDirectGroupIds()) {
			if(!first)
				builder.append(", ");
			builder.append(directGroup);
			first = false;
		}
		builder.append("]");
		builder.append("{");
		first = true;
		for (CustomPlayerGroupMember directMember : groupData.getDirectMembersImpl()) {
			if(!first)
				builder.append(", ");
			builder.append(memberSerializer.serialize(directMember));
			first = false;
		}
		builder.append("}");
		return builder.toString();
	}

	public CustomPlayerConfigGroupData deserialize(String configValue){
		int leftSquareBracketIndex = configValue.indexOf("[");
		if(leftSquareBracketIndex == -1)
			throw new IllegalArgumentException("Left square bracket not found. " + FORMAT_ERROR_HINT);
		int rightSquareBracketIndex = configValue.indexOf("]");
		if(rightSquareBracketIndex == -1)
			throw new IllegalArgumentException("Right square bracket not found. " + FORMAT_ERROR_HINT);
		int leftCurlyBracketIndex = configValue.indexOf("{");
		if(leftCurlyBracketIndex == -1)
			throw new IllegalArgumentException("Left curly bracket not found. " + FORMAT_ERROR_HINT);
		int rightCurlyBracketIndex = configValue.indexOf("}");
		if(rightCurlyBracketIndex == -1)
			throw new IllegalArgumentException("Right curly bracket not found. " + FORMAT_ERROR_HINT);
		String groupId = configValue.substring(0, leftSquareBracketIndex).trim();
		if(groupId.isEmpty())
			throw new IllegalArgumentException("Group id not found. " + FORMAT_ERROR_HINT);
		CustomPlayerConfigGroupData result = new CustomPlayerConfigGroupData(groupId);
		String includedGroupsString = configValue.substring(leftSquareBracketIndex + 1, rightSquareBracketIndex).trim();
		if(!includedGroupsString.isEmpty())
			for (String includedGroupUntrimmed : includedGroupsString.split(",")) {
				String includedGroup = includedGroupUntrimmed.trim();
				if(!CustomPlayerConfigGroupData.isValidId(includedGroup))
					throw new IllegalArgumentException(
							"The ID \"" + includedGroup + "\" of a player group to include in " +
							groupId + " is not valid. Allowed characters: " +
							CustomPlayerConfigGroupData.VALID_ID_CHARS_DISPLAY
					);
				result.includeGroup(includedGroup);
			}
		String serializedMembersString = configValue.substring(leftCurlyBracketIndex + 1, rightCurlyBracketIndex).trim();
		if(serializedMembersString.isEmpty())
			return result;
		for (String serializedMemberUntrimmed : serializedMembersString.split(",")) {
			String serializedMember = serializedMemberUntrimmed.trim();
			try {
				CustomPlayerGroupMember member = memberSerializer.deserialize(serializedMember);
				result.includeMember(member);
			} catch(Throwable t){
				throw new IllegalArgumentException(
						"Failed to deserialize group member data. Details: " + t.getMessage()
				);
			}
		}
		return result;
	}

}
