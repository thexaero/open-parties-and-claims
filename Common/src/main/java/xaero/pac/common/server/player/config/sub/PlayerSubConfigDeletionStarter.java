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

package xaero.pac.common.server.player.config.sub;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.task.PlayerSubClaimDeletionSpreadoutTask;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;

import java.util.UUID;

public class PlayerSubConfigDeletionStarter {

	public void start(ServerPlayer caller, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo,
					  IPlayerConfig subConfig,
					  IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData
	){
		MinecraftServer server = serverData.getServer();
		UUID callerUUID = caller == null ? null : caller.getUUID();
		if(caller != null)
			caller.sendMessage(new TranslatableComponent("gui.xaero_pac_config_delete_sub_started", subConfig.getSubId()), callerUUID);
		subConfig.setBeingDeleted();
		PlayerSubClaimDeletionSpreadoutTask taskNormal = PlayerSubClaimDeletionSpreadoutTask.Builder.begin()
				.setForceloadable(false)
				.setCallerUUID(callerUUID)
				.setSubConfigIndex(subConfig.getSubIndex())
				.setServer(server)
				.setPlayerInfo(playerInfo)
				.build();
		PlayerSubClaimDeletionSpreadoutTask taskForceloadable = PlayerSubClaimDeletionSpreadoutTask.Builder.begin()
				.setForceloadable(true)
				.setLast(true)
				.setCallerUUID(callerUUID)
				.setSubConfigIndex(subConfig.getSubIndex())
				.setServer(server)
				.setPlayerInfo(playerInfo)
				.build();
		playerInfo.addReplacementTask(taskNormal, serverData);
		playerInfo.addReplacementTask(taskForceloadable, serverData);
	}

}
