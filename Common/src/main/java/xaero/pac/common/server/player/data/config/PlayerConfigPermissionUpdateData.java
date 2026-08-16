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

package xaero.pac.common.server.player.data.config;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.player.config.PlayerConfigPermissions;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Objects;

public class PlayerConfigPermissionUpdateData extends PlayerConfigPermissions {

	private final PlayerConfigType type;
	private final ServerPlayerData playerData;

	public PlayerConfigPermissionUpdateData(PlayerConfigType type, ServerPlayerData playerData) {
		this.type = type;
		this.playerData = playerData;
	}

	public boolean update(
			ServerPlayer player,
			IServerData<?,?> serverData
	) {
		boolean prevView = canView();
		boolean prevEdit = canEdit();
		boolean prevIncludePlayersInGroups = canIncludePlayersInGroups();
		boolean prevIncludeGroupsInGroups = canIncludeGroupsInGroups();
		boolean prevCreateGroups = canCreateGroups();
		boolean prevClaimAs = canClaimAs();

		IPlayerConfigManager configManager = serverData.getPlayerConfigManager();
		IPlayerConfig config = ServerPlayerConfigUtils.getTargetConfig(
				player.getUUID(), player.getUUID(), type, configManager
		);
		boolean otherPlayer = type == PlayerConfigType.PLAYER && !Objects.equals(player.getUUID(), config.getPlayerId());
		boolean isAdmin = player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
		boolean requiresAdmin = otherPlayer || type != PlayerConfigType.PLAYER && type != PlayerConfigType.PARTY_CLAIMS;
		boolean edit = isAdmin || !requiresAdmin;
		boolean view = edit || type == PlayerConfigType.SERVER;
		boolean includePlayersInGroups = edit;
		boolean includeGroupsInGroups = edit;
		boolean createGroups = edit;
		boolean claimAs = false;
		if(type == PlayerConfigType.PLAYER)
			claimAs = !otherPlayer;
		else if(type == PlayerConfigType.SERVER)
			claimAs = isAdmin || serverData.getServerClaimsManager().getPermissionHandler().playerHasServerClaimPermission(player);
		else if(type == PlayerConfigType.PARTY_CLAIMS) {
			IPlayerPartySystemManager partySystems = configManager.getPartySystemManager();
			if(!isAdmin) {
				edit = partySystems.canEditPartyConfig(player.getUUID());
				includePlayersInGroups = partySystems.canIncludePlayersInPartyConfigGroups(player.getUUID());
				includeGroupsInGroups = partySystems.canIncludeGroupsInPartyConfigGroups(player.getUUID());
				createGroups = partySystems.canCreatePartyConfigGroups(player.getUUID());
			}
			claimAs = serverData.getServerClaimsManager().getPermissionHandler().playerHasPartyClaimPermission(player, player.getUUID());
		}
		setEdit(edit);
		setView(view);
		setIncludeGroupsInGroups(includeGroupsInGroups);
		setIncludePlayersInGroups(includePlayersInGroups);
		setCreateGroups(createGroups);
		setClaimAs(claimAs);
		if(view != prevView)
			return true;
		if(edit != prevEdit)
			return true;
		if(includePlayersInGroups != prevIncludePlayersInGroups)
			return true;
		if(includeGroupsInGroups != prevIncludeGroupsInGroups)
			return true;
		if(claimAs != prevClaimAs)
			return true;
		return createGroups != prevCreateGroups;
	}
}
