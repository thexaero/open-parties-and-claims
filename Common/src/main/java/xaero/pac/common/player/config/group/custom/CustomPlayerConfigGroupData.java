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

package xaero.pac.common.player.config.group.custom;

import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.server.player.util.ServerPlayerUtils;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CustomPlayerConfigGroupData implements Comparable<CustomPlayerConfigGroupData>, ICustomPlayerConfigGroupData {

	public static final UUID UNKNOWN_ID = new UUID(0, 0);
	public static final String VALID_ID_CHARS_DISPLAY = "a-z, A-Z, 0-9 and -_";
	private final String id;
	private final Map<String, CustomPlayerGroupIncludedGroup> directGroups;
	private final LinkedChain<CustomPlayerGroupIncludedGroup> directGroupsChain;
	private final Map<String, CustomPlayerGroupIncludedGroup> directGroupsUnmodifiable;
	private final Set<CustomPlayerGroupMember> directMembers;
	private final LinkedChain<CustomPlayerGroupMember> directMembersChain;
	private final Set<CustomPlayerGroupMember> directMembersUnmodifiableImpl;
	private final Set<ICustomPlayerGroupMember> directMembersUnmodifiable;
	private final Map<String, CustomPlayerGroupMember> memberByLowerCaseName;
	private final Map<UUID, CustomPlayerGroupMember> memberById;

	public CustomPlayerConfigGroupData(String id){
		if(!isValidId(id))
			throw new IllegalArgumentException(
					"Player group id must only consist of at most " + PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH +
					" characters from " + VALID_ID_CHARS_DISPLAY +
					" but the following was used: " + id
			);
		this.id = id;
		this.memberByLowerCaseName = new HashMap<>();
		this.memberById = new HashMap<>();
		directMembers = new HashSet<>();
		directGroups = new HashMap<>();
		directMembersUnmodifiableImpl = Collections.unmodifiableSet(directMembers);
		directMembersUnmodifiable = Collections.unmodifiableSet(directMembers);
		directMembersChain = new LinkedChain<>();
		directGroupsUnmodifiable = Collections.unmodifiableMap(directGroups);
		directGroupsChain = new LinkedChain<>();
	}

	public static boolean isValidId(String id){
		return id.matches("[a-zA-Z0-9-_]+") && id.length() <= PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH;
	}

	public static boolean isValidPlayerName(String name){
		if(name.isEmpty())
			return false;
		if(name.length() > PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_USERNAME_LENGTH)
			return false;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(!ServerPlayerUtils.isValidPlayerNameChar(c))
				return false;
		}
		return true;
	}

	public String getId() {
		return id;
	}

	public void includeMember(CustomPlayerGroupMember member){
		String name = member.getDisplayName();
		UUID uuid = member.getId();
		if(uuid == null && name == null)
			throw new IllegalArgumentException("Player group member needs a UUID, a name or both.");
		String nameLowerCase = name == null ? null : name.toLowerCase();
		if(name != null && memberByLowerCaseName.containsKey(nameLowerCase))
			throw new IllegalArgumentException(String.format("A player named %s is already in the group.", name));
		if(uuid != null && !UNKNOWN_ID.equals(uuid) && memberById.containsKey(uuid))
			throw new IllegalArgumentException(String.format("A player with UUID %s is already in the group.", uuid));
		if(name != null && name.length() > PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_USERNAME_LENGTH)
			throw new IllegalArgumentException(String.format("A player name can't be longer than %d.", PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_USERNAME_LENGTH));
		if(name != null && !isValidPlayerName(name))
			throw new IllegalArgumentException(String.format("The player name is not valid for a player group: %s", name));
		member.setGroupData(this);
		directMembers.add(member);
		directMembersChain.add(member);
		if(nameLowerCase != null)
			memberByLowerCaseName.put(nameLowerCase, member);
		if(uuid != null && !UNKNOWN_ID.equals(uuid))
			memberById.put(uuid, member);
	}

	public boolean excludeMember(CustomPlayerGroupMember member) {
		if (!directMembers.remove(member))
			return false;
		directMembersChain.remove(member);
		if (member.getDisplayName() != null)
			memberByLowerCaseName.remove(member.getDisplayName().toLowerCase());
		if (member.getId() != null)
			memberById.remove(member.getId());
		return true;
	}

	public CustomPlayerGroupMember excludeGetMember(UUID playerId, String playerName) {
		if(playerId != null){
			CustomPlayerGroupMember member = memberById.get(playerId);
			if(member != null && excludeMember(member))
				return member;
			return null;//won't be a good idea to remove a player with a different id but same name, so stopping here
		}
		if(playerName == null)
			return null;
		String nameLowerCase = playerName.toLowerCase();
		CustomPlayerGroupMember member = memberByLowerCaseName.get(nameLowerCase);
		if(member == null)
			return null;
		return excludeMember(member) ? member : null;
	}

	public void includeGroup(String id){
		if(directGroups.containsKey(id))
			throw new IllegalArgumentException(String.format("The group %s is already in the group.", id));
		CustomPlayerGroupIncludedGroup includedGroup = new CustomPlayerGroupIncludedGroup(id);
		directGroups.put(id, includedGroup);
		directGroupsChain.add(includedGroup);
	}

	public boolean excludeGroup(String id){
		CustomPlayerGroupIncludedGroup removedGroup = directGroups.remove(id);
		if(removedGroup != null)
			directGroupsChain.remove(removedGroup);
		return removedGroup != null;
	}

	public CustomPlayerGroupMember confirmMemberId(CustomPlayerGroupMember directMember, UUID uuid) {
		if(!directMembers.contains(directMember))
			return null;
		CustomPlayerGroupMember existingIdHolder = memberById.get(uuid);
		if(existingIdHolder == directMember)
			return existingIdHolder;
		if(existingIdHolder != null)
			excludeMember(existingIdHolder);
		directMember.setId(uuid);
		if(!Objects.equals(UNKNOWN_ID, uuid))
			memberById.put(uuid, directMember);
		return existingIdHolder;
	}

	public boolean updateMemberName(UUID playerId, String name){
		if(!memberById.containsKey(playerId))
			return false;
		CustomPlayerGroupMember directMember = memberById.get(playerId);
		String directMemberCurrentName = directMember.getDisplayName();
		if(name.equals(directMemberCurrentName))
			return false;
		String lowercaseName = name.toLowerCase();
		CustomPlayerGroupMember existingNameHolder = memberByLowerCaseName.get(lowercaseName);
		if(existingNameHolder != null)
			existingNameHolder.setDisplayName(null);
		directMember.setDisplayName(name);
		if(existingNameHolder == directMember)//this means that only the cases of the name chars were changed
			return true;
		if(directMemberCurrentName != null)
			memberByLowerCaseName.remove(directMemberCurrentName.toLowerCase());
		memberByLowerCaseName.put(lowercaseName, directMember);
		return true;
	}

	public Set<CustomPlayerGroupMember> getDirectMembersImpl() {
		return directMembersUnmodifiableImpl;
	}

	@Nonnull
	public Set<ICustomPlayerGroupMember> getDirectMembersInternal() {
		return directMembersUnmodifiable;
	}

	@Nonnull
	public Set<String> getDirectGroupIds() {
		return directGroupsUnmodifiable.keySet();
	}

	public boolean playerIdIsIncluded(@Nullable UUID playerId){
		if(playerId == null)
			return false;
		return memberById.containsKey(playerId);
	}

	public boolean playerNameIsIncluded(@Nullable String name){
		if(name == null)
			return false;
		return memberByLowerCaseName.containsKey(name.toLowerCase());
	}

	public boolean groupIdIsIncluded(@Nullable String groupId){
		if(groupId == null)
			return false;
		return directGroups.containsKey(groupId);
	}

	@Override
	public int compareTo(@Nonnull CustomPlayerConfigGroupData o) {
		return id.compareTo(o.id);
	}

	public Iterator<CustomPlayerGroupMember> getDirectMembersChainIterator() {
		return directMembersChain.iterator();
	}

	public Iterator<CustomPlayerGroupIncludedGroup> getDirectGroupsChainIterator() {
		return directGroupsChain.iterator();
	}

	@Nonnull
	@Override
	public CustomPlayerConfigGroupData copyData(){
		CustomPlayerConfigGroupData copy = new CustomPlayerConfigGroupData(id);
		for (CustomPlayerGroupMember directMember : directMembers)
			copy.includeMember(new CustomPlayerGroupMember(directMember.getId(), directMember.getDisplayName()));
		for (CustomPlayerGroupIncludedGroup directGroup : directGroups.values())
			copy.includeGroup(directGroup.getId());
		return copy;
	}

	@Override
	public int getSize() {
		return directMembers.size() + directGroups.size();
	}

}
