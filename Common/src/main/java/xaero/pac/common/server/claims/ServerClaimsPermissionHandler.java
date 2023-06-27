/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.server.MinecraftServer;
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

public class ServerClaimsPermissionHandler {

	private IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?> serverData;

	public boolean playerHasServerClaimPermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		IPlayerPermissionSystemAPI permissionSystem = getSystem();
		if(permissionSystem != null && permissionSystem.getPermission(player, ServerConfig.CONFIG.serverClaimPermission.get()))
			return true;
		return false;
	}

	public boolean shouldPreventServerClaim(ServerPlayer player, ServerPlayerDataAPI playerData, MinecraftServer server){
		if(!playerHasServerClaimPermission(player)) {
			if (playerData.isClaimsServerMode()) {
				((ServerPlayerData)playerData).setClaimsServerMode(false);
				OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundModesPacket(playerData.isClaimsAdminMode(), playerData.isClaimsServerMode()));
				server.getCommands().sendCommands(player);
			}
			return true;
		}
		return false;
	}

	public boolean playerHasAdminModePermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		IPlayerPermissionSystemAPI permissionSystem = getSystem();
		if(permissionSystem != null && permissionSystem.getPermission(player, ServerConfig.CONFIG.adminModePermission.get()))
			return true;
		return false;
	}

	public void ensureAdminModeStatusPermission(ServerPlayer player, ServerPlayerDataAPI playerData){
		if(playerData.isClaimsAdminMode() && !playerHasAdminModePermission(player)) {
			((ServerPlayerData)playerData).setClaimsAdminMode(false);
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundModesPacket(playerData.isClaimsAdminMode(), playerData.isClaimsServerMode()));
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
