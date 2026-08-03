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
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
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
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

public class ClaimsClaimCommands {

	protected static Command<CommandSourceStack> createClaimCommand(boolean shouldClaim, ClaimingMode mode, boolean another, boolean opReplaceCurrent){
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
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, new TranslatableComponent("gui.xaero.claims_claim_command_unknown_dimension")));
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
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, new TranslatableComponent("gui.xaero.claims_claim_command_unknown_pos")));
						return 0;
					}
					chunkX = player.chunkPosition().x;
					chunkZ = player.chunkPosition().z;
				}
				
				ServerPlayerData playerData = player == null ? null : (ServerPlayerData) ServerPlayerDataAPI.from(player);
				ClaimingMode finalMode = mode == null ?
						(another || player == null ? (ClaimingMode) ClaimingModes.PLAYER : playerData.getClaimingMode()) : mode;
				UUID contextPlayerId = getClaimInputPlayerId(
						context, player,
						"gui.xaero_claims_claim_command_too_many_targets",
						"gui.xaero_claims_claim_command_invalid_target",
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
					if (serverData.getServerTickHandler().getTickCounter() == playerData.getClaimActionRequestHandler().getLastRequestTickCounter())
						return 0;//going too fast
					playerData.getClaimActionRequestHandler().setLastRequestTickCounter(serverData.getServerTickHandler().getTickCounter());
					claimsManager.getPermissionHandler().ensureAdminModeStatusPermission(player, playerData);
					shouldReplace = shouldReplace || playerData.isClaimsAdminMode();
				}
				UUID sourceUUID = player == null ? PlayerConfig.SERVER_CLAIM_UUID : player.getUUID();
				boolean impersonating = !another && !contextPlayerId.equals(sourceUUID);

				ClaimResult<?> result = null;
				try {
					ResourceLocation fromDimension = player == null ? world.dimension().location() : player.level.dimension().location();
					int fromX = player == null ? chunkX : player.chunkPosition().x;
					int fromZ = player == null ? chunkZ : player.chunkPosition().z;
					if(shouldClaim) {
						String specifiedSubId = null;
						try {
							specifiedSubId = StringArgumentType.getString(context, "sub-id");
						} catch(IllegalArgumentException iae){
						}
						IPlayerConfig playerConfig = serverData.getPlayerConfigManager().getLoadedConfig(contextPlayerId);
						IPlayerConfig claimConfig = finalMode.getClaimConfigGetter().apply(playerConfig);
						IPlayerConfig usedSubConfig;
						if(specifiedSubId != null)
							usedSubConfig = claimConfig.getEffectiveSubConfig(specifiedSubId);
						else {
							usedSubConfig = impersonating ?
									claimConfig.getEffectiveSubConfig(playerData.getClaimsImpersonationInfo().getSubIndex(finalMode)) :
									claimConfig.getEffectiveSubConfig(playerConfig.getEffective(finalMode.getSubClaimOption()));
						}
						int subConfigIndex = usedSubConfig.getSubIndex();
						result = claimsManager.tryToClaimTyped(world.dimension().location(), claimPlayerId, subConfigIndex, fromDimension, fromX, fromZ, chunkX, chunkZ, shouldReplace);
						
						if(result.getResultType() == ClaimResult.Type.ALREADY_CLAIMED) {
							IPlayerChunkClaimAPI currentClaim = claimsManager.get(world.dimension().location(), chunkX, chunkZ);
							boolean moderatorMode = false;
							if(player != null) {
								claimsManager.getPermissionHandler().ensureModeratorModeStatusPermission(player, playerData);
								moderatorMode = playerData.isClaimsModeratorMode();
							}
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_claim_already_claimed_by", claimsManager.getDefaultName(currentClaim, !moderatorMode)));
							return 0;
						}
					} else {
						result = claimsManager.tryToUnclaimTyped(world.dimension().location(), claimPlayerId, fromDimension, fromX, fromZ, chunkX, chunkZ, shouldReplace);
						if(!result.getResultType().success) {
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
							return 0;
						}
					}
					if(result.getResultType().success) {
						context.getSource().sendSuccess(adaptiveLocalizer.getFor(player, shouldClaim ? "gui.xaero_claims_claimed_at" : "gui.xaero_claims_unclaimed_at", chunkX, chunkZ, world.dimension().location()), true);
						return 1;
					} else {
						if(result.getResultType().fail)
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
						else
							context.getSource().sendSuccess(adaptiveLocalizer.getFor(player, result.getResultType().message), true);
						return 0;
					}
				} finally {
					if(result != null && player != null)
						((ClaimsManagerSynchronizer)claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(
								new AreaClaimResult(Sets.newHashSet(result.getResultType()), chunkX, chunkZ, chunkX, chunkZ),
								player);
				}
			};
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
		if(sourcePlayer == null)
			return PlayerConfig.SERVER_CLAIM_UUID;
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
		return (context, builder) -> {
			ServerPlayer sourcePlayer = null;
			try {
				sourcePlayer = context.getSource().getPlayerOrException();
			} catch (CommandSyntaxException cse){
			}
			ServerPlayerData sourcePlayerData = sourcePlayer == null ? null : (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
			ClaimingMode effectiveMode = mode == null ?
					(another || sourcePlayer == null ? (ClaimingMode) ClaimingModes.PLAYER : sourcePlayerData.getClaimingMode()) :
					mode;
			final ServerPlayer finalSourcePlayer = sourcePlayer;
			return ConfigCommandUtil.getSubConfigSuggestionProvider(effectiveMode.getConfigType(),
					(context1, serverData) ->
					{
						try {
							return getClaimInputPlayerId(
									context1, finalSourcePlayer, null, null,
									serverData, another, effectiveMode
							);
						} catch (CommandSyntaxException e) {
							return null;
						}
					}
			).getSuggestions(context, builder);
		};
	}

	public static Predicate<CommandSourceStack> getImpersonationRequirement(){
		return CommandRequirementHelper.onServerThread(context -> {
			if(context.hasPermission(Commands.LEVEL_GAMEMASTERS))
				return true;
			ServerPlayer player;
			try {
				player = context.getPlayerOrException();
			} catch(CommandSyntaxException cse){
				return false;
			}
			MinecraftServer server = context.getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(server);
			return serverData.getServerClaimsManager().getPermissionHandler().playerHasImpersonationPermission(player);
		});
	}
	
}
