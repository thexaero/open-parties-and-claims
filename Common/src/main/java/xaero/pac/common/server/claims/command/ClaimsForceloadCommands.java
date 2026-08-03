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
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public class ClaimsForceloadCommands {

	protected static Command<CommandSourceStack> createForceloadCommand(boolean enable, ClaimingMode mode, boolean another, boolean opReplaceCurrent){
		return context -> {
				ServerPlayer player = null;
				try {
					player = context.getSource().getPlayerOrException();
				} catch (CommandSyntaxException cse){
				}
				MinecraftServer server = context.getSource().getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
				AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
				ServerLevel world;
				try {
					world = DimensionArgument.getDimension(context, "dimension");
				} catch(IllegalArgumentException iae) {
					if(player == null){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, new TranslatableComponent("gui.xaero.claims_forceload_command_unknown_dimension")));
						return 0;
					}
					world = player.getLevel();
				}
				int chunkX;
				int chunkZ;
				try {
					ColumnPos columnPos = ColumnPosArgument.getColumnPos(context, "block-pos");
					chunkX = columnPos.x >> 4;
					chunkZ = columnPos.z >> 4;
				} catch(IllegalArgumentException iae) {
					if(player == null){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, new TranslatableComponent("gui.xaero.claims_forceload_command_unknown_pos")));
						return 0;
					}
					chunkX = player.chunkPosition().x;
					chunkZ = player.chunkPosition().z;
				}
				ServerPlayerData playerData = player == null ? null : (ServerPlayerData) ServerPlayerDataAPI.from(player);
				ClaimingMode finalMode = mode == null ?
						(another || player == null ? (ClaimingMode) ClaimingModes.PLAYER : playerData.getClaimingMode()) : mode;
				UUID contextPlayerId = ClaimsClaimCommands.getClaimInputPlayerId(
						context, player,
						"gui.xaero_claims_forceload_command_too_many_targets",
						"gui.xaero_claims_forceload_command_invalid_target",
						serverData, another, finalMode
				);
				if(contextPlayerId == null)
					return 0;
				if(finalMode.getPermissionChecker() != null) {
					ClaimResult.Type failureType = finalMode.getPermissionChecker().apply(contextPlayerId, serverData.getServerClaimsManager());
					if(failureType != null) {
						if(player != null && finalMode == playerData.getRawClaimingMode())
							serverData.getServerClaimsManager().getPermissionHandler().resetClaimingMode(player);
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, failureType.message));
						return 0;
					}
				}
				UUID claimPlayerId = contextPlayerId;
				if(finalMode.getForcedUUIDGetter() != null)
					claimPlayerId = finalMode.getForcedUUIDGetter().apply(claimPlayerId, serverData.getServerClaimsManager());
				if(claimPlayerId == null) {
					//shouldn't actually happen, so no failure is sent,
					// but won't hurt to catch this anyway
					return 0;
				}

				IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();
				boolean shouldReplace = opReplaceCurrent;
				if(player != null) {
					if(serverData.getServerTickHandler().getTickCounter() == playerData.getClaimActionRequestHandler().getLastRequestTickCounter())
						return 0;//going too fast
					playerData.getClaimActionRequestHandler().setLastRequestTickCounter(serverData.getServerTickHandler().getTickCounter());
					claimsManager.getPermissionHandler().ensureAdminModeStatusPermission(player, playerData);
					shouldReplace = shouldReplace || playerData.isClaimsAdminMode();
				}
				UUID sourceUUID = player == null ? PlayerConfig.SERVER_CLAIM_UUID : player.getUUID();
				boolean impersonating = !another && !contextPlayerId.equals(sourceUUID);
				ResourceLocation fromDimension = player == null ? world.dimension().location() : player.level.dimension().location();
				int fromX = player == null ? chunkX : player.chunkPosition().x;
				int fromZ = player == null ? chunkZ : player.chunkPosition().z;
			 	ClaimResult<?> result = claimsManager.tryToForceloadTyped(world.dimension().location(), claimPlayerId, fromDimension, fromX, fromZ, chunkX, chunkZ, enable, shouldReplace);
			 	
			 	try {
				 	if(!result.getResultType().success) {
						if(result.getResultType().fail)
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
						else
							context.getSource().sendSuccess(adaptiveLocalizer.getFor(player, result.getResultType().message), true);
				 		return 0;
				 	}
					
				 	if(enable)
						context.getSource().sendSuccess(adaptiveLocalizer.getFor(player, "gui.xaero_claims_forceloaded_at", chunkX, chunkZ, world.dimension().location()), true);
				 	else
						context.getSource().sendSuccess(adaptiveLocalizer.getFor(player, "gui.xaero_claims_unforceloaded_at", chunkX, chunkZ, world.dimension().location()), true);
				 	return 1;
			 	} finally {
					 if(player != null)
						((ClaimsManagerSynchronizer)claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(
								new AreaClaimResult(Sets.newHashSet(result.getResultType()), chunkX, chunkZ, chunkX, chunkZ),
								player);
			 	}
			};
	}

}
