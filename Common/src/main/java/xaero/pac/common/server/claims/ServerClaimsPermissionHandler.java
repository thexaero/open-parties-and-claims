/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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
import xaero.pac.common.packet.ClientboundModesPacket;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;

public class ServerClaimsPermissionHandler {

	public boolean playerHasServerClaimPermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		boolean hasFtbRanks = OpenPartiesAndClaims.INSTANCE.getModSupport().FTB_RANKS;
		if(hasFtbRanks &&
				OpenPartiesAndClaims.INSTANCE.getModSupport().getFTBRanksSupport().getPermissionHelper().getPermission(player, ServerConfig.CONFIG.serverClaimFTBPermission.get()))
			return true;
		return false;
	}

	public boolean shouldPreventServerClaim(ServerPlayer player, ServerPlayerData playerData, MinecraftServer server){
		if(!playerHasServerClaimPermission(player)) {
			if (playerData.isClaimsServerMode()) {
				playerData.setClaimsServerMode(false);
				OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundModesPacket(playerData.isClaimsAdminMode(), playerData.isClaimsServerMode()));
				server.getCommands().sendCommands(player);
			}
			return true;
		}
		return false;
	}

}
