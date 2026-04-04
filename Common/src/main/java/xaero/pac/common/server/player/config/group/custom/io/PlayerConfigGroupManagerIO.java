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

package xaero.pac.common.server.player.config.group.custom.io;

import com.mojang.datafixers.util.Either;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;
import xaero.pac.common.player.config.group.custom.ICustomPlayerGroupMember;
import xaero.pac.common.player.config.group.custom.io.serializer.CustomPlayerGroupDataIOSerializer;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.group.ServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.CustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerConfigGroupManagerIO {

	private final ServerPlayerConfigGroupManager manager;
	private final CustomPlayerGroupDataIOSerializer serializer;

	public PlayerConfigGroupManagerIO(ServerPlayerConfigGroupManager manager) {
		this.manager = manager;
		this.serializer = new CustomPlayerGroupDataIOSerializer();
	}

	public void saveToConfig(){
		PlayerConfig<?> config = manager.getConfig();
		List<String> configValueBuilder = new ArrayList<>();
		for (ICustomPlayerConfigGroup customGroup : manager.getAllCustom())
			configValueBuilder.add(serializer.serialize(((CustomPlayerConfigGroup) customGroup).getData()));
		config.forceSet(
				(PlayerConfigOptionSpec<? super List<String>>)PlayerConfigOptions.CUSTOM_PLAYER_GROUPS,
				configValueBuilder
		);
		manager.confirmSave();
	}

	public void loadFromConfig(){
		PlayerConfig<?> config = manager.getConfig();
		List<String> configValue = config.getRaw(PlayerConfigOptions.CUSTOM_PLAYER_GROUPS);
		if(configValue == null)//if this happens, then we did something wrong
			throw new IllegalStateException();
		manager.removeAll();
		for (String serializedGroupData : configValue) {
			CustomPlayerConfigGroupData groupData;
			try {
				groupData = serializer.deserialize(serializedGroupData);
			} catch(Exception e){
				OpenPartiesAndClaims.LOGGER.error(
						"Failed to deserialize a custom player group for player {}! Details: {}",
						config.getPlayerId(), e.getMessage()
				);
				continue;
			}
			try {
				//addOrGetCustom: if 2 groups have the same id in a config file, then we just combine them
				Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> groupResult = manager.addOrGetCustom(groupData.getId());
				Optional<PlayerConfigGroupActionError> error = groupResult.right();
				if(error.isPresent()){
					OpenPartiesAndClaims.LOGGER.warn(
							"Failed to create custom group for ID {} for player {}! Error: {}",
							groupData.getId(), config.getPlayerId(), error.get().toString()
					);
					continue;
				}
				ICustomPlayerConfigGroup createdGroup = groupResult.orThrow();
				for (String directGroup : groupData.getDirectGroupIds()) {
					error = createdGroup.includeGroup(directGroup);
					if(error.isPresent())
						OpenPartiesAndClaims.LOGGER.warn(
								"Failed to include group {} in a custom player group {} for player {}! Error: {}",
								directGroup, groupData.getId(), config.getPlayerId(), error.get().toString()
						);
				}
				for (CustomPlayerGroupMember directMember : groupData.getDirectMembersImpl()) {
					Either<ICustomPlayerGroupMember, PlayerConfigGroupActionError> result =
							createdGroup.includeMemberInternal(directMember.getId(), directMember.getDisplayName());
					error = result.right();
					if(error.isPresent())
						OpenPartiesAndClaims.LOGGER.warn(
								"Failed to include player {} in a custom player group {} for player {}! Error: {}",
								directMember, groupData.getId(), config.getPlayerId(), error.get().toString()
						);
				}
			} catch(Exception e){
				//removing the partially loaded group
				manager.removeData(groupData.getId());
				OpenPartiesAndClaims.LOGGER.error(
						"Failed to load custom player group {} for player {}! Details: {}",
						groupData.getId(), config.getPlayerId(), e.getMessage()
				);
			}
		}
		manager.confirmLoaded();
	}

}
