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

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.packet.config.group.PlayerConfigGroupMemberPacket;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.UUID;

public class CustomPlayerConfigGroupProfileUtils {

	protected static void fetchMemberIdAsync(CustomPlayerConfigGroup group, CustomPlayerGroupMember member){
		PlayerConfig<?> storageConfig = group.getStorageConfig();
		MinecraftServer server = storageConfig.getManager().getServer();
		String name = member.getDisplayName();
		server.getProfileCache().getAsync(name, lookedUpProfile -> {
			if(member.getId() != null)//was already handled
				return;
			if(storageConfig.getPlayerGroups().getCustom(group.getId()) != group)//group was removed
				return;
			UUID memberId;
			if(lookedUpProfile.isEmpty())
				memberId = CustomPlayerConfigGroupData.UNKNOWN_ID;
			else
				memberId = lookedUpProfile.get().getId();
			CustomPlayerGroupMember previousIdHolder = group.getData().confirmMemberId(member, memberId);
			if(member.getId() == null)//member was previously removed
				return;
			storageConfig.getPlayerGroups().setResaveNeeded();
			if(!storageConfig.getPlayerGroups().isLoaded())
				return;
			if(previousIdHolder != null) {
				storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
						null, storageConfig, group.getId(),
						PlayerConfigGroupMemberPacket.Action.EXCLUDE,
						memberId, previousIdHolder.getDisplayName()
				);
				storageConfig.getPlayerGroups().decrementUsedSpace();
			}
			//excluding the original name-only entry
			storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
					null, storageConfig, group.getId(),
					PlayerConfigGroupMemberPacket.Action.EXCLUDE,
					null, member.getDisplayName()
			);
			storageConfig.getManager().getSynchronizer().syncGroupMemberUpdate(
					null, storageConfig, group.getId(),
					PlayerConfigGroupMemberPacket.Action.INCLUDE,
					memberId, member.getDisplayName()
			);
		});
	}

}
