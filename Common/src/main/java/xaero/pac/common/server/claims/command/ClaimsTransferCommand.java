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

import com.electronwill.nightconfig.core.Config;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.ICustomPlayerGroupMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.ServerClaimsPermissionHandler;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.task.PlayerSubClaimTransferSpreadoutTask;
import xaero.pac.common.server.command.CommandRequirementHelper;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigPlayerGroupOptionSpec;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.*;
import java.util.function.Predicate;

public class ClaimsTransferCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Predicate<CommandSourceStack> profileRequirement = CommandRequirementHelper.onServerThread(context -> {
			if(context.hasPermission(2) )
				return true;
			try {
				ServerPlayer player = context.getPlayerOrException();
				MinecraftServer server = player.getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(server);
				return serverData.getServerClaimsManager().getPermissionHandler().playerHasAdminModePermission(player);
			} catch (CommandSyntaxException e) {
				return false;
			}
		});
		SuggestionProvider<CommandSourceStack> suggestions = (context, builder) -> {
			PlayerList playerlist = context.getSource().getServer().getPlayerList();
			return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream()
					.map(targetPlayer -> targetPlayer.getGameProfile().getName()), builder);
		};

		LiteralArgumentBuilder<CommandSourceStack> onlineNoConfirmCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("transfer")
				.then(Commands.argument("player", EntityArgument.player())
				.requires(context -> !profileRequirement.test(context))
				.suggests(suggestions)
				.executes(getExecutor(false, false, false))));
		dispatcher.register(onlineNoConfirmCommand);
		LiteralArgumentBuilder<CommandSourceStack> onlineCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("transfer")
				.then(Commands.argument("player", EntityArgument.player())
				.requires(context -> !profileRequirement.test(context))
				.suggests(suggestions)
				.then(Commands.literal("confirm")
				.executes(getExecutor(false, true, false)))));
		dispatcher.register(onlineCommand);
		LiteralArgumentBuilder<CommandSourceStack> acceptCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("transfer-accept")
				.then(Commands.argument("player-id", StringArgumentType.word())
				.executes(getExecutor(false, true, true))));
		dispatcher.register(acceptCommand);

		LiteralArgumentBuilder<CommandSourceStack> profileNoConfirmCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("transfer")
				.then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.requires(profileRequirement)
				.suggests(suggestions)
				.executes(getExecutor(true, false, false))));
		dispatcher.register(profileNoConfirmCommand);
		LiteralArgumentBuilder<CommandSourceStack> profileCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("transfer")
				.then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.requires(profileRequirement)
				.suggests(suggestions)
				.then(Commands.literal("confirm")
				.executes(getExecutor(true, true, false)))));
		dispatcher.register(profileCommand);
	}

	private Command<CommandSourceStack> getExecutor(boolean profile, boolean confirmed, boolean accept){
		return context -> {
			ServerPlayer callerPlayer = context.getSource().getPlayerOrException();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(callerPlayer.getServer());
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			GameProfile transferTo = null;
			try {
				if(accept){
					transferTo = callerPlayer.getGameProfile();
				} else if(profile) {
					Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "profile");
					if (profiles.size() == 1)
						transferTo = profiles.iterator().next();
				} else {
					ServerPlayer inputPlayer = EntityArgument.getPlayer(context, "player");
					if(inputPlayer != null)
						transferTo = inputPlayer.getGameProfile();
					else {
						context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_online_player_not_found"));
						return 0;
					}
				}
			} catch(IllegalArgumentException iae) {
			}
			if(transferTo == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_invalid_player"));
				return 0;
			}
			ServerClaimsPermissionHandler permissionHandler = serverData.getServerClaimsManager().getPermissionHandler();
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(callerPlayer);
			if(!confirmed)//don't want to switch off impersonation when using the confirm command
				permissionHandler.ensureImpersonationPermission(callerPlayer, playerData);
			UUID originalRequesterId;
			GameProfile transferFrom;
			UUID impersonatedId = null;
			if(!accept) {
				originalRequesterId = callerPlayer.getUUID();
				impersonatedId = playerData.getClaimsImpersonationInfo().getPlayerId();
				transferFrom = callerPlayer.getGameProfile();
				if (impersonatedId != null) {
					if (confirmed && !permissionHandler.playerHasImpersonationPermission(callerPlayer)) {
						context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_no_impersonation_permission"));
						return 0;
					}
					transferFrom = callerPlayer.getServer().getProfileCache().get(impersonatedId).orElse(null);
					if (transferFrom == null) {
						context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_invalid_impersonated_player"));
						return 0;
					}
				}
			} else {
				String acceptedPlayerIdString = StringArgumentType.getString(context, "player-id");
				try {
					originalRequesterId = UUID.fromString(acceptedPlayerIdString);
					ServerPlayer requesterPlayer = serverData.getServer().getPlayerList().getPlayer(originalRequesterId);
					if(requesterPlayer == null){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_accepted_online_player_not_found"));
						return 0;
					}
					ServerPlayerData requesterPlayerData = (ServerPlayerData) ServerPlayerData.from(requesterPlayer);
					long requestTime = requesterPlayerData.getClaimTransferRequestTime();
					long sinceRequest = System.currentTimeMillis() - requestTime;
					if(sinceRequest < 2000){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_accepted_too_quickly"));
						return 0;
					}
					if(!callerPlayer.getUUID().equals(requesterPlayerData.getClaimTransferRequestTargetPlayerId()) || sinceRequest > 60000){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_accepted_expired"));
						return 0;
					}
					transferFrom = requesterPlayerData.getClaimTransferRequestSourcePlayerProfile();
					requesterPlayerData.setClaimTransferRequestTargetPlayerId(null);
				} catch(IllegalArgumentException iae){
					context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_accepted_invalid_player_id"));
					return 0;
				}
			}
			if(transferTo.getId().equals(transferFrom.getId())) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_to_the_same"));
				return 0;
			}
			if(!confirmed){
				Component message;
				message = Component.translatable("gui.xaero_claims_transfer_needs_confirmation", transferFrom.getName(), transferTo.getName());
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, message));
				return 0;
			}
			boolean shouldForceTransfer = !accept && playerData.isClaimsAdminMode() && (
					impersonatedId != null ||//implies permission to impersonate target player as well (it's the same permission)
					permissionHandler.playerHasImpersonationPermission(callerPlayer)
			);
			IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
					claimsManager = serverData.getServerClaimsManager();
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> fromPlayerInfo =
					claimsManager.getPlayerInfo(transferFrom.getId());
			if(!serverData.getServerClaimsManager().hasPlayerInfo(transferTo.getId())) {
				//updating the username for previously unknown players
				IServerPlayerClaimInfo<?> toPlayerInfo =
						serverData.getServerClaimsManager().getPlayerInfo(transferTo.getId());
				((ServerPlayerClaimInfo)toPlayerInfo).setPlayerUsername(transferTo.getName());
			}
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> toPlayerInfo =
					claimsManager.getPlayerInfo(transferTo.getId());
			if(fromPlayerInfo.getClaimCount() == 0){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_no_claims"));
				return 0;
			}
			IPlayerConfig fromConfig = fromPlayerInfo.getConfig();
			IPlayerConfig toConfig = toPlayerInfo.getConfig();
			if(shouldForceTransfer) {
				startTransfer(transferFrom, transferTo, fromConfig, toConfig, originalRequesterId, serverData);
				return 1;
			}
			ServerPlayer targetPlayer = serverData.getServer().getPlayerList().getPlayer(transferTo.getId());
			if(targetPlayer == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_online_player_not_found"));
				return 0;
			}
			String errorSuffix = (accept ? "_accept" : "");
			IServerPlayerConfigGroupManager fromPlayerGroups = fromConfig.getPlayerGroups();
			IServerPlayerConfigGroupManager toPlayerGroups = toConfig.getPlayerGroups();
			int availablePlayerGroups = toPlayerGroups.getMaxGroups() - toPlayerGroups.getCustomGroupCount();
			if(fromPlayerGroups.getCustomGroupCount() > availablePlayerGroups){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_target_group_limit" + errorSuffix, availablePlayerGroups, fromPlayerGroups.getCustomGroupCount()));
				return 0;
			}
			int availableGroupSpace = toPlayerGroups.getGroupSpace() - toPlayerGroups.getUsedSpace();
			if(fromPlayerGroups.getUsedSpace() > availableGroupSpace){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_target_group_space_limit" + errorSuffix, availableGroupSpace, fromPlayerGroups.getUsedSpace()));
				return 0;
			}
			int subLimit = toConfig.getSubConfigLimit();
			int availableSubCount = subLimit - toConfig.getSubCount();
			if(fromConfig.getSubCount() > availableSubCount){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_target_sub_limit" + errorSuffix, availableSubCount, fromConfig.getSubCount()));
				return 0;
			}
			int availableTargetClaims = claimsManager.getPlayerFullClaimLimit(transferTo.getId()) - toPlayerInfo.getClaimCount();
			if(fromPlayerInfo.getClaimCount() > availableTargetClaims){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_target_claim_limit" + errorSuffix, availableTargetClaims, fromPlayerInfo.getClaimCount()));
				return 0;
			}
			if(accept){
				startTransfer(transferFrom, transferTo, fromConfig, toConfig, originalRequesterId, serverData);
				return 1;
			}
			Component callerName = Component.literal(callerPlayer.getGameProfile().getName()).withStyle(ChatFormatting.GREEN);
			Component transferFromName = Component.literal(transferFrom.getName()).withStyle(ChatFormatting.GREEN);
			Component transferToName = Component.literal(transferTo.getName()).withStyle(ChatFormatting.GREEN);
			callerPlayer.sendMessage(adaptiveLocalizer.getFor(callerPlayer, "gui.xaero_claims_transfer_request_sent", transferFromName, transferToName), callerPlayer.getUUID());
			playerData.setClaimTransferRequestSourcePlayerProfile(transferFrom);
			playerData.setClaimTransferRequestTargetPlayerId(transferTo.getId());
			playerData.setClaimTransferRequestTime(System.currentTimeMillis());
			Component acceptComponent = adaptiveLocalizer.getFor(targetPlayer, "gui.xaero_claims_transfer_target_message", callerName, transferFromName, transferToName);
			acceptComponent.getSiblings().add(Component.literal(" "));
			acceptComponent.getSiblings().add(adaptiveLocalizer.getFor(targetPlayer, "gui.xaero_claims_transfer_target_message_accept")
					.withStyle(s -> s.withColor(ChatFormatting.GREEN)
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ClaimsCommandRegister.COMMAND_PREFIX + " transfer-accept " + callerPlayer.getUUID()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, adaptiveLocalizer.getFor(targetPlayer, "gui.xaero_claims_transfer_target_message_accept_tooltip")))));
			targetPlayer.sendMessage(acceptComponent, callerPlayer.getUUID());
			return 1;
		};
	}

	private static void startTransfer(
			GameProfile transferFrom,
			GameProfile transferTo,
			IPlayerConfig fromConfig,
			IPlayerConfig toConfig,
			UUID originalRequesterId,
			IServerData<?, ?> serverData
	){
		IServerClaimsManager<?, ?, ?>
				claimsManager = serverData.getServerClaimsManager();
		IServerPlayerClaimInfo<?> fromPlayerInfo =
				claimsManager.getPlayerInfo(transferFrom.getId());
		IServerPlayerClaimInfo<?> toPlayerInfo =
				claimsManager.getPlayerInfo(transferTo.getId());
		fromPlayerInfo.setTransferInProgress(true);
		toPlayerInfo.setTransferInProgress(true);
		MinecraftServer server = serverData.getServer();
		ServerPlayer onlineRequester = originalRequesterId == null ? null : server.getPlayerList().getPlayer(originalRequesterId);
		notifyPlayerOfStart(onlineRequester, toPlayerInfo.getPlayerId().equals(originalRequesterId), fromPlayerInfo, toPlayerInfo, serverData);
		if(!fromPlayerInfo.getPlayerId().equals(originalRequesterId)){
			ServerPlayer onlineFrom = server.getPlayerList().getPlayer(fromPlayerInfo.getPlayerId());
			notifyPlayerOfStart(onlineFrom, false, fromPlayerInfo, toPlayerInfo, serverData);
		}
		if(!toPlayerInfo.getPlayerId().equals(originalRequesterId)) {
			ServerPlayer onlineTo = server.getPlayerList().getPlayer(toPlayerInfo.getPlayerId());
			notifyPlayerOfStart(onlineTo, true, fromPlayerInfo, toPlayerInfo, serverData);
		}
		//copying all custom player groups
		IServerPlayerConfigGroupManager fromPlayerGroups = fromConfig.getPlayerGroups();
		IServerPlayerConfigGroupManager toPlayerGroups = toConfig.getPlayerGroups();
		Map<String, String> playerGroupIdMap = new HashMap<>();
		for (String groupId : fromPlayerGroups.getIds()) {
			String validGroupId = CustomPlayerConfigGroupData.makeIdValid(groupId);
			String targetGroupId = validGroupId;
			int autoGroupIdCount = 1;
			while(toPlayerGroups.dataExists(targetGroupId)){
				autoGroupIdCount++;
				targetGroupId = validGroupId + autoGroupIdCount;
				int overflow = targetGroupId.length() - PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH;
				if(overflow > 0)
					targetGroupId = targetGroupId.substring(overflow);
			}
			Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addResult = toPlayerGroups.addCustom(targetGroupId, false);
			if(addResult.right().isPresent())
				throw new IllegalStateException("Failed to add a player group during claim transfer. This shouldn't be possible!");
			if(!groupId.equals(targetGroupId))
				playerGroupIdMap.put(groupId, targetGroupId);
			ICustomPlayerConfigGroup sourceGroup = fromPlayerGroups.getCustom(groupId);
			ICustomPlayerConfigGroup addedGroup = addResult.orThrow();
			for (ICustomPlayerGroupMember member : sourceGroup.getDirectMembersInternal())
				addedGroup.includeMemberInternal(member.getId(), member.getDisplayName(), false);//failures are just ignored
			for (String includedGroup : sourceGroup.getDirectGroupIds())
				addedGroup.includeGroupInternal(includedGroup, false);//failures are just ignored
		}
		//moving all sub-configs, removing them from the source, and adding replacement tasks for sub-claims
		for (String subConfigId : List.copyOf(fromConfig.getSubConfigIds())) {
			IPlayerConfig fromSubConfig = fromConfig.getSubConfig(subConfigId);
			if(fromSubConfig == fromConfig)
				continue;
			String validSubConfigId = PlayerConfig.makeSubIdValid(subConfigId);
			String targetSubConfigId = validSubConfigId;
			int autoSubIdCount = 1;
			while(toConfig.subConfigExists(targetSubConfigId)){
				autoSubIdCount++;
				targetSubConfigId = validSubConfigId + autoSubIdCount;
				int overflow = targetSubConfigId.length() - PlayerConfig.MAX_SUB_ID_LENGTH;
				if(overflow > 0)
					targetSubConfigId = targetSubConfigId.substring(overflow);
			}
			IPlayerConfig targetSubConfig = toConfig.createSubConfig(targetSubConfigId, false);
			Config configStorage = ((PlayerConfig<?>)fromSubConfig).getStorage();
			((PlayerConfig<?>)targetSubConfig).setStorage(configStorage);
			remapGroupIdOptions(targetSubConfig, playerGroupIdMap);
			((PlayerConfig<?>)targetSubConfig).setDirty(true);

			claimsManager.getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesUpdate(targetSubConfig);
			fromPlayerInfo.addReplacementTask(
					PlayerSubClaimTransferSpreadoutTask.Builder.begin()
							.setOriginalRequesterUUID(originalRequesterId)
							.setForceloadable(false)
							.setFromPlayerInfo(fromPlayerInfo)
							.setToPlayerInfo(toPlayerInfo)
							.setServer(serverData.getServer())
							.setFromSubConfigId(subConfigId)
							.setToSubConfigId(targetSubConfigId)
							.build(),
					serverData
			);
			fromPlayerInfo.addReplacementTask(
					PlayerSubClaimTransferSpreadoutTask.Builder.begin()
							.setOriginalRequesterUUID(originalRequesterId)
							.setForceloadable(true)
							.setFromPlayerInfo(fromPlayerInfo)
							.setToPlayerInfo(toPlayerInfo)
							.setServer(serverData.getServer())
							.setFromSubConfigId(subConfigId)
							.setToSubConfigId(targetSubConfigId)
							.build(),
					serverData
			);
			fromConfig.removeSubConfig(subConfigId);
		}
		//adding claim transfer task for main config claims
		fromPlayerInfo.addReplacementTask(
				PlayerSubClaimTransferSpreadoutTask.Builder.begin()
						.setOriginalRequesterUUID(originalRequesterId)
						.setForceloadable(false)
						.setFromPlayerInfo(fromPlayerInfo)
						.setToPlayerInfo(toPlayerInfo)
						.setServer(serverData.getServer())
						.setFromSubConfigId(PlayerConfig.MAIN_SUB_ID)
						.setToSubConfigId(PlayerConfig.MAIN_SUB_ID)
						.build(),
				serverData
		);
		fromPlayerInfo.addReplacementTask(
				PlayerSubClaimTransferSpreadoutTask.Builder.begin()
						.setOriginalRequesterUUID(originalRequesterId)
						.setForceloadable(true)
						.setFromPlayerInfo(fromPlayerInfo)
						.setToPlayerInfo(toPlayerInfo)
						.setServer(serverData.getServer())
						.setFromSubConfigId(PlayerConfig.MAIN_SUB_ID)
						.setToSubConfigId(PlayerConfig.MAIN_SUB_ID)
						.setLast(true)
						.build(),
				serverData
		);
		toConfig.getManager().getSynchronizer().addConfigToSync(null, toConfig);
	}

	private static void notifyPlayerOfStart(
			ServerPlayer onlinePlayer,
			boolean isTarget,
			IServerPlayerClaimInfo<?> fromPlayerInfo,
			IServerPlayerClaimInfo<?> toPlayerInfo,
			IServerData<?, ?> serverData
	){
		if(onlinePlayer == null)
			return;
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		Component fromPlayerName = Component.literal(fromPlayerInfo.getPlayerUsername()).withStyle(ChatFormatting.GREEN);
		Component toPlayerName = Component.literal(toPlayerInfo.getPlayerUsername()).withStyle(ChatFormatting.GREEN);
		onlinePlayer.sendMessage(adaptiveLocalizer.getFor(onlinePlayer,
				isTarget ? "gui.xaero_claims_transfer_start_to" : "gui.xaero_claims_transfer_start_from",
				fromPlayerName, toPlayerName
		), onlinePlayer.getUUID());
	}

	private static void remapGroupIdOptions(IPlayerConfig subConfig, Map<String, String> groupIdMap){
		IPlayerConfigManager configManager = subConfig.getManager();
		configManager.getAllOptionsStream()
				.filter(IPlayerConfigOptionSpecAPI::isOverridable)
				.filter(o -> o instanceof PlayerConfigPlayerGroupOptionSpec)
				.forEach(option -> {
					PlayerConfigPlayerGroupOptionSpec groupOption = (PlayerConfigPlayerGroupOptionSpec) option;
					String currentValue = subConfig.getRaw(groupOption);
					if(currentValue == null)
						return;
					String remappedValue = groupIdMap.get(currentValue);
					if(remappedValue == null)
						return;
					((PlayerConfig<?>)subConfig).forceSet(groupOption, remappedValue);
				});
	}

}
