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

package xaero.pac.common.server.player.config.group.custom;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.packet.config.group.PlayerConfigGroupGroupPacket;
import xaero.pac.common.packet.config.group.PlayerConfigGroupMemberPacket;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;
import xaero.pac.common.player.config.group.custom.ICustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.ICustomPlayerGroupMember;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.group.CachedPlayerConfigParentGroup;
import xaero.pac.common.server.player.config.group.IPlayerConfigGroup;
import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CustomPlayerConfigGroup extends CachedPlayerConfigParentGroup implements ICustomPlayerConfigGroupData, ICustomPlayerConfigGroup, ILinkedChainNode<CustomPlayerConfigGroup> {

	private final CustomPlayerConfigGroupData data;
	private final PlayerConfig<?> storageConfig;
	private CustomPlayerConfigGroup next;
	private CustomPlayerConfigGroup previous;
	private boolean destroyed;

	public CustomPlayerConfigGroup(PlayerConfig<?> storageConfig, String id) {
		super();
		this.storageConfig = storageConfig;
		this.data = new CustomPlayerConfigGroupData(id);
	}

	@Nonnull
	@Override
	public String getId() {
		return data.getId();
	}

	@Override
	public boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId) {
		if(player != null)
			return isDirectlyInGroup(contextConfig, player);
		if(playerId == null)
			throw new IllegalArgumentException("Both player object and UUID can't be null here!");
		return isDirectlyInGroup(contextConfig, playerId);
	}

	@Override
	public boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nonnull ServerPlayer player) {
		if(data.updateMemberName(player.getUUID(), player.getGameProfile().name())) {
			if(storageConfig.getPlayerGroups().isLoaded())
				storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
						null, storageConfig, data.getId(),
						PlayerConfigGroupMemberPacket.Action.NAME, player.getUUID(), player.getGameProfile().name()
				);
			storageConfig.getPlayerGroups().setResaveNeeded();
		}
		return isDirectlyInGroup(contextConfig, player.getUUID());
	}

	@Override
	public boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nonnull UUID playerId) {
		return data.playerIdIsIncluded(playerId);
	}

	@Override
	public boolean playerNameIsIncluded(@Nullable String name){
		return data.playerNameIsIncluded(name);
	}

	@Override
	public boolean playerIdIsIncluded(@Nullable UUID playerId) {
		return data.playerIdIsIncluded(playerId);
	}

	@Override
	public boolean groupIdIsIncluded(@Nullable String groupId){
		return data.groupIdIsIncluded(groupId);
	}

	@Override
	public Either<ICustomPlayerGroupMember, PlayerConfigGroupActionError> includeMemberInternal(UUID id, String name, boolean sync) {
		if(data.playerIdIsIncluded(id) || data.playerNameIsIncluded(name))
			return Either.right(PlayerConfigGroupActionError.MEMBER_ALREADY_INCLUDED);
		if(name != null && !CustomPlayerConfigGroupData.isValidPlayerName(name))
			return Either.right(PlayerConfigGroupActionError.INVALID_PLAYER_NAME);
		CustomPlayerGroupMember member = new CustomPlayerGroupMember(id, name);
		data.includeMember(member);
		if(sync && storageConfig.getPlayerGroups().isLoaded())
			storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
					null, storageConfig, data.getId(),
					PlayerConfigGroupMemberPacket.Action.INCLUDE, id, name
			);
		if(id == null)
			CustomPlayerConfigGroupProfileUtils.fetchMemberIdAsync(this, member);
		storageConfig.getPlayerGroups().incrementUsedSpace();
		storageConfig.getPlayerGroups().setSaveNeeded();
		return Either.left(member);
	}

	@Override
	public Either<ICustomPlayerGroupMember, PlayerConfigGroupActionError> includeMemberLimitedInternal(@Nullable UUID id, @Nullable String name, boolean sync) {
		if(storageConfig.getPlayerGroups().getUsedSpace() >= storageConfig.getPlayerGroups().getGroupSpace())
			return Either.right(PlayerConfigGroupActionError.OUT_OF_SPACE);
		return includeMemberInternal(id, name, sync);
	}

	@Override
	public Optional<PlayerConfigGroupActionError> excludeMemberInternal(ICustomPlayerGroupMember member) {
		boolean result = data.excludeMember((CustomPlayerGroupMember) member);
		if(!result)
			return Optional.of(PlayerConfigGroupActionError.MEMBER_NOT_FOUND);
		if(storageConfig.getPlayerGroups().isLoaded())
			storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
					null, storageConfig, data.getId(),
					PlayerConfigGroupMemberPacket.Action.EXCLUDE,
					member.getId(), member.getDisplayName()
			);
		storageConfig.getPlayerGroups().decrementUsedSpace();
		storageConfig.getPlayerGroups().setSaveNeeded();
		return Optional.empty();
	}

	@Nonnull
	@Override
	public CustomPlayerConfigGroupData copyData() {
		return data.copyData();
	}

	@Nonnull
	@Override
	public Optional<PlayerConfigGroupActionError> excludeMember(@Nullable UUID id, @Nullable String name) {
		CustomPlayerGroupMember excludedMember = data.excludeGetMember(id, name);
		if(excludedMember == null)
			return Optional.of(PlayerConfigGroupActionError.MEMBER_NOT_FOUND);
		if(storageConfig.getPlayerGroups().isLoaded())
			storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
					null, storageConfig, data.getId(),
					PlayerConfigGroupMemberPacket.Action.EXCLUDE,
					excludedMember.getId(), excludedMember.getDisplayName()
			);
		storageConfig.getPlayerGroups().decrementUsedSpace();
		storageConfig.getPlayerGroups().setSaveNeeded();
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Optional<PlayerConfigGroupActionError> includeGroup(@Nonnull String groupId) {
		return includeGroupInternal(groupId, true);
	}

	@Nonnull
	@Override
	public Optional<PlayerConfigGroupActionError> includeGroupInternal(@Nonnull String groupId, boolean sync) {
		if(!CustomPlayerConfigGroupData.isValidId(groupId))
			return Optional.of(PlayerConfigGroupActionError.INVALID_GROUP_ID);
		if(data.groupIdIsIncluded(groupId))
			return Optional.of(PlayerConfigGroupActionError.GROUP_ALREADY_INCLUDED);
		data.includeGroup(groupId);
		if(sync && storageConfig.getPlayerGroups().isLoaded())
			storageConfig.getManager().getSynchronizer().syncGroupGroupUpdate(
					null, storageConfig, data.getId(), PlayerConfigGroupGroupPacket.Action.INCLUDE, groupId
			);
		storageConfig.getPlayerGroups().incrementUsedSpace();
		storageConfig.getPlayerGroups().invalidateCacheContaining(this);
		storageConfig.getPlayerGroups().setSaveNeeded();
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Optional<PlayerConfigGroupActionError> includeGroupLimited(@Nonnull String groupId) {
		if(storageConfig.getPlayerGroups().getUsedSpace() >= storageConfig.getPlayerGroups().getGroupSpace())
			return Optional.of(PlayerConfigGroupActionError.OUT_OF_SPACE);
		return includeGroup(groupId);
	}

	@Nonnull
	@Override
	public Optional<PlayerConfigGroupActionError> excludeGroup(@Nonnull String groupId) {
		boolean result = data.excludeGroup(groupId);
		if(!result)
			return Optional.of(PlayerConfigGroupActionError.GROUP_INCLUSION_NOT_FOUND);
		if(storageConfig.getPlayerGroups().isLoaded())
			storageConfig.getManager().getSynchronizer().syncGroupGroupUpdate(
					null, storageConfig, data.getId(), PlayerConfigGroupGroupPacket.Action.EXCLUDE, groupId
			);
		storageConfig.getPlayerGroups().decrementUsedSpace();
		storageConfig.getPlayerGroups().invalidateCacheContaining(this);
		storageConfig.getPlayerGroups().setSaveNeeded();
		return Optional.empty();
	}

	@Override
	protected void beforeGroupLookup() {
		storageConfig.getPlayerGroups().detectDefaultConfigGroupsInvalidation();
	}

	public CustomPlayerConfigGroupData getData() {
		return data;
	}

	public PlayerConfig<?> getStorageConfig() {
		return storageConfig;
	}

	@Nonnull
	@Override
	public Set<String> getDirectGroupIds() {
		return data.getDirectGroupIds();
	}

	@Nonnull
	@Override
	public Set<ICustomPlayerGroupMember> getDirectMembersInternal() {
		return data.getDirectMembersInternal();
	}

	@Override
	public IPlayerConfigGroup lookupGroup(String id) {
		return storageConfig.getPlayerGroups().getUnwrapped(id);
	}

	@Override
	public ICustomPlayerConfigGroup lookupDefaultGroup(String id) {
		return storageConfig.getManager().getDefaultConfig().getPlayerGroups().getCustom(id);
	}

	@Override
	public int getSize() {
		return data.getSize();
	}

	public String debug() {
		StringBuilder debugBuilder = new StringBuilder();
		debugBuilder.append("{");
		for (CustomPlayerGroupMember customPlayerGroupMember : data.getDirectMembersImpl()) {
			debugBuilder.append(customPlayerGroupMember.getDisplayName());
			debugBuilder.append("|");
			debugBuilder.append(customPlayerGroupMember.getId());
			debugBuilder.append(";");
		}
		debugBuilder.append("}");
		return debugBuilder.toString();
	}

	@Override
	public void setNext(CustomPlayerConfigGroup element) {
		this.next = element;
	}

	@Override
	public void setPrevious(CustomPlayerConfigGroup element) {
		this.previous = element;
	}

	@Override
	public CustomPlayerConfigGroup getNext() {
		return this.next;
	}

	@Override
	public CustomPlayerConfigGroup getPrevious() {
		return this.previous;
	}

	@Override
	public boolean isDestroyed() {
		return this.destroyed;
	}

	@Override
	public void onDestroyed() {
		this.destroyed = true;
	}
}
