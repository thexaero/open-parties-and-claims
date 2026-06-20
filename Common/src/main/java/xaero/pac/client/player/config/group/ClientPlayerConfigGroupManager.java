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

package xaero.pac.client.player.config.group;

import net.minecraft.client.Minecraft;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.group.PlayerGroupsScreen;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.group.PlayerConfigGroupDesyncPacket;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupDataManager;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.*;

public class ClientPlayerConfigGroupManager extends CustomPlayerConfigGroupDataManager<CustomPlayerConfigGroupData> implements IClientPlayerConfigGroupManager {

	private PlayerConfigClientStorage config;
	private boolean syncInProgress;
	private PlayerConfigGroupActionError desyncError;
	private int maxGroups;
	private int groupSpace;

	public ClientPlayerConfigGroupManager(
			PlayerConfigType configType,
			Map<String, CustomPlayerConfigGroupData> customGroups,
			Map<String, CustomPlayerConfigGroupData> customGroupsUnmodifiable,
			Map<String, String> customGroupIdCaseCache
	) {
		super(configType, customGroups, customGroupsUnmodifiable, customGroupIdCaseCache);
	}

	@Override
	protected void onRemoved(CustomPlayerConfigGroupData removedGroup) {
	}

	@Override
	public void setSyncInProgress(boolean syncInProgress) {
		this.syncInProgress = syncInProgress;
	}

	@Override
	public boolean isSyncInProgress() {
		return syncInProgress;
	}

	@Override
	public Optional<PlayerConfigGroupActionError> addCustom(String id){
		return addData(id).right();
	}

	@Override
	public Optional<PlayerConfigGroupActionError> removeCustom(String id) {
		return removeData(id);
	}

	@Override
	protected CustomPlayerConfigGroupData construct(String id) {
		return new CustomPlayerConfigGroupData(id);
	}

	@Override
	protected CustomPlayerConfigGroupDataManager<CustomPlayerConfigGroupData> getDefaultConfigGroups() {
		return config.getManager().getDefaultPlayerConfig().getPlayerGroups();
	}

	public void setConfig(PlayerConfigClientStorage config) {
		if(this.config != null)
			throw new IllegalStateException();
		this.config = config;
	}

	private void beginDesyncFix(){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(
				new PlayerConfigGroupDesyncPacket(config.getType(), config.getOwnerForSync())
		);
	}

	@Override
	public void confirmDesyncFix() {
		desyncError = null;
	}

	public boolean isFixingDesync() {
		return desyncError != null;
	}

	public PlayerConfigGroupActionError getDesyncError() {
		return desyncError;
	}

	public void onDesyncError(PlayerConfigGroupActionError error){
		desyncError = error;
		if(Minecraft.getInstance().gui.screen() instanceof PlayerGroupsScreen groupsScreen)
			groupsScreen.onDesyncError(config);
		beginDesyncFix();
	}

	@Override
	public void reset() {
		super.reset();
		desyncError = null;
		maxGroups = 0;
		groupSpace = 0;
		syncInProgress = true;
	}

	@Override
	public boolean updateCustomMemberName(String groupId, UUID playerId, String playerName) {
		CustomPlayerConfigGroupData groupData = getData(groupId);
		if(groupData == null)
			return false;
		return groupData.updateMemberName(playerId, playerName);
	}

	public boolean includeMemberInCustom(String groupId, UUID playerId, String playerName) {
		CustomPlayerConfigGroupData groupData = getData(groupId);
		if(groupData == null)
			return false;
		if(groupData.playerIdIsIncluded(playerId))
			return false;
		if(groupData.playerNameIsIncluded(playerName))
			return false;
		incrementUsedSpace();
		groupData.includeMember(new CustomPlayerGroupMember(playerId, playerName));
		return true;
	}

	public boolean excludeMemberFromCustom(String groupId, UUID playerId, String playerName) {
		CustomPlayerConfigGroupData groupData = getData(groupId);
		if(groupData == null)
			return false;
		if(groupData.excludeGetMember(playerId, playerName) == null)
			return false;
		decrementUsedSpace();
		return true;
	}

	public boolean includeGroupInCustom(String groupId, String groupIdToInclude) {
		CustomPlayerConfigGroupData groupData = getData(groupId);
		if(groupData == null)
			return false;
		if(groupData.groupIdIsIncluded(groupIdToInclude))
			return false;
		groupData.includeGroup(groupIdToInclude);
		incrementUsedSpace();
		return true;
	}

	public boolean excludeGroupFromCustom(String groupId, String groupIdToExclude) {
		CustomPlayerConfigGroupData groupData = getData(groupId);
		if(groupData == null)
			return false;
		if(!groupData.excludeGroup(groupIdToExclude))
			return false;
		decrementUsedSpace();
		return true;
	}

	@Override
	public void setLimits(int maxGroups, int groupSpace) {
		this.maxGroups = maxGroups;
		this.groupSpace = groupSpace;
	}

	@Override
	public int getMaxGroups() {
		return maxGroups;
	}

	@Override
	public int getGroupSpace() {
		return groupSpace;
	}

	public static final class Builder extends CustomPlayerConfigGroupDataManager.Builder<CustomPlayerConfigGroupData, Builder>{

		private Builder(){}

		public Builder setDefault(){
			return super.setDefault();
		}

		@Override
		public ClientPlayerConfigGroupManager build() {
			return (ClientPlayerConfigGroupManager) super.build();
		}

		@Override
		protected CustomPlayerConfigGroupDataManager<CustomPlayerConfigGroupData> buildInternally() {
			Map<String, CustomPlayerConfigGroupData> customGroups = new HashMap<>();
			Map<String, CustomPlayerConfigGroupData> customGroupsUnmodifiable = Collections.unmodifiableMap(customGroups);
			return new ClientPlayerConfigGroupManager(
					configType, customGroups, customGroupsUnmodifiable, new HashMap<>()
			);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
