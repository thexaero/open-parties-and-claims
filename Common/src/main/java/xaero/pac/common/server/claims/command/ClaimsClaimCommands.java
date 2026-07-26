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
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
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
import xaero.pac.common.server.command.CommandRequirementHelper;
import xaero.pac.common.server.command.ConfigCommandUtil;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

public class ClaimsClaimCommands {

	protected static ArgumentBuilder<CommandSourceStack, ?> createClaimCommand(ArgumentBuilder<CommandSourceStack, ?> builder, boolean shouldClaim, ClaimingMode mode, boolean opReplaceCurrent){
		return builder
			.executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				ServerLevel world = player.getLevel();
				int chunkX = player.chunkPosition().x;
				int chunkZ = player.chunkPosition().z;
				try {
					ColumnPos columnPos = ColumnPosArgument.getColumnPos(context, "block-pos");
					chunkX = columnPos.x >> 4;
					chunkZ = columnPos.z >> 4;
				} catch(IllegalArgumentException iae) {
				}
				
				MinecraftServer server = context.getSource().getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
				AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
				ClaimingMode finalMode = mode == null ? playerData.getClaimingMode() : mode;
				UUID contextPlayerId = player.getUUID();
				serverData.getServerClaimsManager().getPermissionHandler().ensureImpersonationPermission(player, playerData);
				boolean impersonating = finalMode.canBeImpersonated() && playerData.getClaimsImpersonationInfo().getPlayerId() != null;
				if(impersonating)
					contextPlayerId = playerData.getClaimsImpersonationInfo().getPlayerId();
				if(finalMode.getPermissionChecker() != null) {
					ClaimResult.Type failureType = finalMode.getPermissionChecker().apply(contextPlayerId, serverData.getServerClaimsManager());
					if(failureType != null) {
						if(finalMode == playerData.getRawClaimingMode())
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

				if(serverData.getServerTickHandler().getTickCounter() == playerData.getClaimActionRequestHandler().getLastRequestTickCounter())
					return 0;//going too fast
				playerData.getClaimActionRequestHandler().setLastRequestTickCounter(serverData.getServerTickHandler().getTickCounter());

				IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();

				claimsManager.getPermissionHandler().ensureAdminModeStatusPermission(player, playerData);
				boolean shouldReplace = opReplaceCurrent || playerData.isClaimsAdminMode();
				ClaimResult<?> result = null;
				try {
					if(shouldClaim) {
						IPlayerConfig playerConfig = serverData.getPlayerConfigManager().getLoadedConfig(contextPlayerId);
						IPlayerConfig claimConfig = finalMode.getClaimConfigGetter().apply(playerConfig);
						IPlayerConfig usedSubConfig = impersonating ?
								claimConfig.getEffectiveSubConfig(playerData.getClaimsImpersonationInfo().getSubIndex(finalMode)) :
								claimConfig.getEffectiveSubConfig(playerConfig.getEffective(finalMode.getSubClaimOption()));
						int subConfigIndex = usedSubConfig.getSubIndex();
						result = claimsManager.tryToClaimTyped(world.dimension().location(), claimPlayerId, subConfigIndex, player.chunkPosition().x, player.chunkPosition().z, chunkX, chunkZ, shouldReplace);
						
						if(result.getResultType() == ClaimResult.Type.ALREADY_CLAIMED) {
							IPlayerChunkClaimAPI currentClaim = claimsManager.get(world.dimension().location(), player.chunkPosition().x, player.chunkPosition().z);
							claimsManager.getPermissionHandler().ensureModeratorModeStatusPermission(player, playerData);
							boolean moderatorMode = playerData.isClaimsModeratorMode();
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_claim_already_claimed_by", claimsManager.getDefaultName(currentClaim, !moderatorMode)));
							return 0;
						}
					} else {
						result = claimsManager.tryToUnclaimTyped(world.dimension().location(), claimPlayerId, player.chunkPosition().x, player.chunkPosition().z, chunkX, chunkZ, shouldReplace);
						if(!result.getResultType().success) {
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
							return 0;
						}
					}
					if(result.getResultType().success) {
						player.sendMessage(adaptiveLocalizer.getFor(player, shouldClaim ? "gui.xaero_claims_claimed_at" : "gui.xaero_claims_unclaimed_at", chunkX, chunkZ), player.getUUID());
						return 1;
					} else {
						if(result.getResultType().fail)
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
						else
							player.sendMessage(adaptiveLocalizer.getFor(player, result.getResultType().message), player.getUUID());
						return 0;
					}
				} finally {
					if(result != null)
						((ClaimsManagerSynchronizer)claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(
								new AreaClaimResult(Sets.newHashSet(result.getResultType()), chunkX, chunkZ, chunkX, chunkZ),
								player);
				}
			});
	}

	public static Predicate<CommandSourceStack> getServerClaimCommandRequirement(){
		return CommandRequirementHelper.onServerThread(source -> {
			if(source.hasPermission(2))
				return true;
			try {
				ServerPlayer player = source.getPlayerOrException();
				MinecraftServer server = player.getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(server);
				if(serverData.getServerClaimsManager().getPermissionHandler().playerHasServerClaimPermission(player))
					return true;
			} catch (CommandSyntaxException e) {
			}
			return false;
		});
	}

	public static Predicate<CommandSourceStack> getPartyClaimRequirement(){
		return CommandRequirementHelper.onServerThread(sourceStack -> {
			if(!ServerConfig.CONFIG.claimsEnabled.get())
				return false;
			return ServerConfig.CONFIG.partyOwnedClaims.get();
		});
	}

	public static UUID getClaimInputPlayerId(
			CommandContext<CommandSourceStack> context,
			ServerPlayer sourcePlayer,
			String tooManyTargetMessage,
			String invalidTargetMessage,
			IServerData<?, ?> serverData,
			boolean another,
			IClaimingModeAPI claimingModeAPI
	) throws CommandSyntaxException {
		if(another){
			GameProfile inputPlayer = ConfigCommandUtil.getConfigInputPlayer(context, sourcePlayer, tooManyTargetMessage, invalidTargetMessage, serverData.getAdaptiveLocalizer());
			if(inputPlayer == null)
				return null;
			return inputPlayer.getId();
		}
		if(claimingModeAPI.canBeImpersonated()) {
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
			serverData.getServerClaimsManager().getPermissionHandler().ensureImpersonationPermission(sourcePlayer, playerData);
			UUID impersonatedPlayerId = playerData.getClaimsImpersonationInfo().getPlayerId();
			if (impersonatedPlayerId != null)
				return impersonatedPlayerId;
		}
		return sourcePlayer.getUUID();
	}

	public static SuggestionProvider<CommandSourceStack> getSubClaimSuggestionProvider(ClaimingMode mode, boolean another){
		return ConfigCommandUtil.getSubConfigSuggestionProvider(mode.getConfigType(),
				(context, serverData) ->
				{
					try {
						ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
						return getClaimInputPlayerId(
								context, sourcePlayer, null, null,
								serverData, another, mode
						);
					} catch (CommandSyntaxException e) {
						return null;
					}
				}
		);
	}
	
}
