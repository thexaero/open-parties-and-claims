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

package xaero.pac.common.server.claims.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public class ClaimsForceloadCommands {

	protected static ArgumentBuilder<CommandSourceStack, ?> createForceloadCommand(ArgumentBuilder<CommandSourceStack, ?> builder, boolean enable, ClaimingMode mode, boolean opReplaceCurrent){
		return builder
			.executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				ServerLevel world = player.getLevel();
				int chunkX = player.chunkPosition().x;
				int chunkZ = player.chunkPosition().z;
				try {
					ColumnPos columnPos = ColumnPosArgument.getColumnPos(context, "block-pos");
					chunkX = columnPos.x() >> 4;
					chunkZ = columnPos.z() >> 4;
				} catch(IllegalArgumentException iae) {
				}
				
				MinecraftServer server = context.getSource().getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
				AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
				ClaimingMode finalMode = mode == ClaimingModes.PLAYER ? playerData.getClaimingMode() : mode;
				if(finalMode.getPermissionChecker() != null) {
					ClaimResult.Type failureType = finalMode.getPermissionChecker().apply(player, serverData.getServerClaimsManager());
					if(failureType != null) {
						if(finalMode != ClaimingModes.PLAYER)
							serverData.getServerClaimsManager().getPermissionHandler().resetClaimingMode(player);
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, failureType.message));
						return 0;
					}
				}
				UUID playerId = player.getUUID();
				if(finalMode.getForcedUUIDGetter() != null)
					playerId = finalMode.getForcedUUIDGetter().apply(playerId, serverData.getServerClaimsManager());
				if(playerId == null) {
					//shouldn't actually happen, so no failure is sent,
					// but won't hurt to catch this anyway
					return 0;
				}

				if(serverData.getServerTickHandler().getTickCounter() == playerData.getClaimActionRequestHandler().getLastRequestTickCounter())
					return 0;//going too fast
				playerData.getClaimActionRequestHandler().setLastRequestTickCounter(serverData.getServerTickHandler().getTickCounter());

				IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();

				claimsManager.getPermissionHandler().ensureAdminModeStatusPermission(player, playerData);
				boolean shouldReplace = opReplaceCurrent || playerData.isClaimsAdminMode();

			 	ClaimResult<?> result = claimsManager.tryToForceloadTyped(world.dimension().location(), playerId, player.chunkPosition().x, player.chunkPosition().z, chunkX, chunkZ, enable, shouldReplace);
			 	
			 	try {
				 	if(!result.getResultType().success) {
						if(result.getResultType().fail)
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
						else
							player.sendSystemMessage(adaptiveLocalizer.getFor(player, result.getResultType().message));
				 		return 0;
				 	}
					
				 	if(enable)
				 		player.sendSystemMessage(adaptiveLocalizer.getFor(player, "gui.xaero_claims_forceloaded_at", chunkX, chunkZ));
				 	else
				 		player.sendSystemMessage(adaptiveLocalizer.getFor(player, "gui.xaero_claims_unforceloaded_at", chunkX, chunkZ));
				 	return 1;
			 	} finally {
					((ClaimsManagerSynchronizer)claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(
							new AreaClaimResult(Sets.newHashSet(result.getResultType()), chunkX, chunkZ, chunkX, chunkZ),
							player);
			 	}
			});
	}

}
