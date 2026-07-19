/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ClientboundModesPacket;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

public class ServerClaimsPermissionHandler {

	private IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?> serverData;

	public boolean playerHasServerClaimPermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		IPlayerPermissionSystemAPI permissionSystem = getSystem();
		if(permissionSystem == null)
			return false;
		return permissionSystem.getPermission(player, UsedPermissionNodes.SERVER_CLAIMS);
	}

	public void resetClaimingMode(ServerPlayer player){
		ServerPlayerDataAPI playerData = ServerPlayerData.from(player);
		((ServerPlayerData)playerData).setClaimingMode(null);
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, ClientboundModesPacket.get(playerData));
		serverData.getPlayerPermissionChangeHandler().sendCommandsAndUpdatePermissions(player, serverData, false);
	}

	public boolean playerHasPartyClaimPermission(ServerPlayer player){
		if(!ServerConfig.CONFIG.partyOwnedClaims.get())
			return false;
		if(!serverData.getPlayerPartySystemManager().isInAPrimaryParty(player.getUUID()))
			return false;
		if(player.hasPermissions(Commands.LEVEL_GAMEMASTERS))
			return true;
		return serverData.getPlayerPartySystemManager().canPartyClaim(player.getUUID());
	}

	public boolean playerHasAdminModePermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		IPlayerPermissionSystemAPI permissionSystem = getSystem();
		if(permissionSystem == null)
			return false;
		return permissionSystem.getPermission(player, UsedPermissionNodes.ADMIN_MODE);
	}

	public void ensureAdminModeStatusPermission(ServerPlayer player, ServerPlayerDataAPI playerData){
		if(playerData.isClaimsAdminMode() && !playerHasAdminModePermission(player)) {
			((ServerPlayerData)playerData).setClaimsAdminMode(false);
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, ClientboundModesPacket.get(playerData));
		}
	}

	public IPlayerPermissionSystemAPI getSystem() {
		return serverData.getPlayerPermissionSystemManager().getUsedSystem();
	}

	@SuppressWarnings("unchecked")
	public void setServerData(IServerData<?,?> serverData) {
		if(this.serverData != null)
			throw new IllegalAccessError();
		this.serverData = (IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?>) serverData;
	}
}
