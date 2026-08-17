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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import xaero.pac.OpenPartiesAndClaims;
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
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
				final ServerPlayer finalPlayer = player;
				MinecraftServer server = context.getSource().getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
				AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
				ServerLevel world;
				try {
					world = DimensionArgument.getDimension(context, "dimension");
				} catch(IllegalArgumentException iae) {
					if(player == null){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, Component.translatable("gui.xaero.claims_claim_command_unknown_dimension")));
						return 0;
					}
					world = player.level();
				}
				int areaLeft;
				int areaTop;
				int areaRight;
				int areaBottom;
				try {
					ColumnPos columnPosFrom = ColumnPosArgument.getColumnPos(context, "from-block-pos");
					int fromChunkX = columnPosFrom.x() >> 4;
					int fromChunkZ = columnPosFrom.z() >> 4;
					ColumnPos columnPosTo = ColumnPosArgument.getColumnPos(context, "to-block-pos");
					int toChunkX = columnPosTo.x() >> 4;
					int toChunkZ = columnPosTo.z() >> 4;
					areaLeft = Math.min(fromChunkX, toChunkX);
					areaTop = Math.min(fromChunkZ, toChunkZ);
					areaRight = Math.max(fromChunkX, toChunkX);
					areaBottom = Math.max(fromChunkZ, toChunkZ);
				} catch(IllegalArgumentException iae) {
					if(player == null){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(player, Component.translatable("gui.xaero.claims_claim_command_unknown_pos")));
						return 0;
					}
					int chunkX = player.chunkPosition().x();
					int chunkZ = player.chunkPosition().z();
					areaLeft = chunkX;
					areaTop = chunkZ;
					areaRight = chunkX;
					areaBottom = chunkZ;
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
					Identifier fromDimension = player == null ? world.dimension().identifier() : player.level().dimension().identifier();
					int middleX = (areaLeft + areaRight) / 2;
					int middleZ = (areaTop + areaBottom) / 2;
					int fromX = player == null ? middleX : player.chunkPosition().x();
					int fromZ = player == null ? middleZ : player.chunkPosition().z();
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
						String subConfigId = usedSubConfig.getSubId() == null ? PlayerConfig.MAIN_SUB_ID : usedSubConfig.getSubId();
						int subConfigIndex = usedSubConfig.getSubIndex();
						if(middleX != areaLeft || middleZ != areaTop){//is more than 1 chunk
							Component defaultClaimName = claimsManager.getDefaultName(claimPlayerId, false, true).copy().withStyle(ChatFormatting.GREEN);
							Component interruptButton = constructInterruptButton(mode, another, context);
							if(interruptButton == null)
								return 0;
							Component subIdComponent = Component.literal(subConfigId).withStyle(ChatFormatting.GREEN);
							context.getSource().sendSuccess(
									adaptiveLocalizer.supplierFor(
											player, "gui.xaero_claims_claim_command_area_start",
											areaLeft, areaTop, areaRight, areaBottom, defaultClaimName, subIdComponent, interruptButton
									),
									true
							);
							Component endMessage = adaptiveLocalizer.getFor(
									player, "gui.xaero_claims_claim_command_area_end",
									areaLeft, areaTop, areaRight, areaBottom, defaultClaimName, subIdComponent
							);
							claimsManager.tryToClaimArea(
									world.dimension().identifier(), claimPlayerId, subConfigIndex,
									fromDimension, fromX, fromZ, areaLeft, areaTop, areaRight, areaBottom,
									shouldReplace, r -> sendResult(context.getSource(), server, finalPlayer, r, endMessage, serverData)
							);
							return 1;
						}
						result = claimsManager.tryToClaimTyped(world.dimension().identifier(), claimPlayerId, subConfigIndex, fromDimension, fromX, fromZ, middleX, middleZ, shouldReplace);

						if(result.getResultType() == ClaimResult.Type.ALREADY_CLAIMED) {
							IPlayerChunkClaimAPI currentClaim = claimsManager.get(world.dimension().identifier(), middleX, middleZ);
							boolean moderatorMode = false;
							if(player != null) {
								claimsManager.getPermissionHandler().ensureModeratorModeStatusPermission(player, playerData);
								moderatorMode = playerData.isClaimsModeratorMode();
							}
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_claim_already_claimed_by", claimsManager.getDefaultName(currentClaim, !moderatorMode)));
							return 0;
						}
						if(!result.getResultType().success) {
							Component message = adaptiveLocalizer.getFor(player, result.getMessage());
							if(result.getResultType().fail)
								context.getSource().sendFailure(message);
							else
								context.getSource().sendSuccess(() -> message, true);
							return 0;
						}
						context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(player, "gui.xaero_claims_claimed_at", middleX, middleZ, world.dimension().identifier().toString()), true);
					} else {
						if(middleX != areaLeft || middleZ != areaTop){//is more than 1 chunk
							Component defaultClaimName = claimsManager.getDefaultName(claimPlayerId, false, true).copy().withStyle(ChatFormatting.GREEN);
							Component interruptButton = constructInterruptButton(mode, another, context);
							if(interruptButton == null)
								return 0;
							context.getSource().sendSuccess(
									adaptiveLocalizer.supplierFor(
											player, "gui.xaero_claims_unclaim_command_area_start",
											areaLeft, areaTop, areaRight, areaBottom, defaultClaimName, interruptButton
									),
									true
							);
							Component endMessage = adaptiveLocalizer.getFor(
									player, "gui.xaero_claims_unclaim_command_area_end",
									areaLeft, areaTop, areaRight, areaBottom, defaultClaimName
							);
							claimsManager.tryToUnclaimArea(
									world.dimension().identifier(), claimPlayerId,
									fromDimension, fromX, fromZ, areaLeft, areaTop, areaRight, areaBottom,
									shouldReplace, r -> sendResult(context.getSource(), server, finalPlayer, r, endMessage, serverData)
							);
							return 1;
						}
						result = claimsManager.tryToUnclaimTyped(world.dimension().identifier(), claimPlayerId, fromDimension, fromX, fromZ, middleX, middleZ, shouldReplace);
						if(!result.getResultType().success) {
							Component message = adaptiveLocalizer.getFor(player, result.getMessage());
							context.getSource().sendFailure(message);
							return 0;
						}
						context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(player, "gui.xaero_claims_unclaimed_at", middleX, middleZ, world.dimension().identifier().toString()), true);
					}
					return 1;
				} finally {
					if(result != null && player != null) {
						Set<Component> customReasons = new HashSet<>();
						if(result.getCustomReason() != null)
							customReasons.add(result.getCustomReason());
						((ClaimsManagerSynchronizer) claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(
								new AreaClaimResult(Sets.newHashSet(result.getResultType()), customReasons, areaLeft, areaTop, areaRight, areaBottom),
								player);
					}
				}
			};
	}

	public static void sendResult(
			CommandSourceStack sourceStack,
			MinecraftServer server,
			ServerPlayer player,
			AreaClaimResult result,
			Component endMessage,
			IServerData<?, ?> serverData
	){
		if(player != null) {
			IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
			ServerPlayer actualOnlinePlayer = server.getPlayerList().getPlayer(player.getUUID());
			((ClaimsManagerSynchronizer)claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(result, actualOnlinePlayer);
			if(player != actualOnlinePlayer)//can't use the command source stack anymore if this is the case
				return;
		}
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		sourceStack.sendSuccess(() -> endMessage, true);
		int resultNumber = 0;
		for (ClaimResult.Type type : result.getResultTypesIterable()) {
			resultNumber++;
			Component resultMessage = Component.literal(resultNumber + ") ").withStyle(ChatFormatting.WHITE);
			resultMessage.getSiblings().add(adaptiveLocalizer.getFor(player, type.message));
			if(type.fail) {
				sourceStack.sendFailure(resultMessage);
				continue;
			}
			sourceStack.sendSuccess(() -> resultMessage, true);
		}
		Iterator<Component> customReasons = result.getCustomReasons().iterator();
		if(customReasons.hasNext())
			sourceStack.sendSuccess(adaptiveLocalizer.supplierFor(player, "gui.xaero_claims_claim_action_forbidden_by_addon_reasons"), true);
		int reasonNumber = 0;
		while(customReasons.hasNext()){
			reasonNumber++;
			Component customReason = customReasons.next().copy().withStyle(ChatFormatting.RED);
			Component reasonMessage = Component.literal(reasonNumber + ") ").withStyle(ChatFormatting.WHITE);
			reasonMessage.getSiblings().add(adaptiveLocalizer.getFor(player, customReason));
			sourceStack.sendSuccess(() -> reasonMessage, true);
		}
	}

	public static String constructClaimInterruptCommand(ClaimingMode mode, boolean another, CommandContext<CommandSourceStack> context){
		String interruptCommand = "/" + ClaimsCommandRegister.COMMAND_PREFIX;
		if(mode != null)
			interruptCommand += " " + mode.getId();
		interruptCommand += " interrupt";
		if(another) {
			String fullCommandInput = context.getInput();
			String[] fullCommandArgs = fullCommandInput.split(" ");
			String typedPlayerName = null;
			boolean nextArgIsPlayer = false;
			for (String arg : fullCommandArgs) {
				if(nextArgIsPlayer) {
					typedPlayerName = arg;
					break;
				}
				if(arg.equals("as"))
					nextArgIsPlayer = true;
			}
			if(typedPlayerName == null){
				OpenPartiesAndClaims.LOGGER.error("Somehow failed to determine the player name in the claim command input for the interrupt button!");
				return null;
			}
			interruptCommand += " for " + typedPlayerName;
		}
		return interruptCommand;
	}

	public static Component constructInterruptButton(ClaimingMode mode, boolean another, CommandContext<CommandSourceStack> context){
		String interruptCommand = constructClaimInterruptCommand(mode, another, context);
		if(interruptCommand == null)
			return null;
		MutableComponent interruptButton = Component.translatable("gui.xaero_claims_claim_command_area_interrupt_button");
		interruptButton.setStyle(interruptButton.getStyle().withColor(ChatFormatting.RED)
				.withHoverEvent(new HoverEvent.ShowText(Component.literal(interruptCommand)))
				.withClickEvent(new ClickEvent.SuggestCommand(interruptCommand)));
		return interruptButton;
	}

	public static Predicate<CommandSourceStack> getServerClaimCommandRequirement(){
		return CommandRequirementHelper.onServerThread(source -> {
			if(Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
				return true;
			try {
				ServerPlayer player = source.getPlayerOrException();
				MinecraftServer server = ServerLevelHelper.getServer(player);
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
			NameAndId inputPlayer = ConfigCommandUtil.getConfigInputPlayer(context, sourcePlayer, tooManyTargetMessage, invalidTargetMessage, serverData.getAdaptiveLocalizer());
			if(inputPlayer == null)
				return null;
			return inputPlayer.id();
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
					}, true
			).getSuggestions(context, builder);
		};
	}

	public static Predicate<CommandSourceStack> getImpersonationRequirement(){
		return CommandRequirementHelper.onServerThread(context -> {
			if(Commands.LEVEL_GAMEMASTERS.check(context.permissions()))
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
