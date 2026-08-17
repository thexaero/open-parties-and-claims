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

package xaero.pac.common.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.config.sub.PlayerSubConfigDeletionStarter;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

import static xaero.pac.common.server.command.ConfigCommandUtil.getConfigInputPlayer;
import static xaero.pac.common.server.command.ConfigCommandUtil.getSubConfigSuggestionProvider;

public class ConfigSubDeleteCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		for (PlayerConfigType configType : PlayerConfigType.values()) {
			if(!configType.supportsSubConfigs())
				continue;
			Predicate<CommandSourceStack> prefixRequirement = configType.getWriteCommandRequirement();
			Predicate<CommandSourceStack> mainRequirement = s -> true;
			if(configType.readAndWriteReqsDiffer()) {
				prefixRequirement = configType.getReadCommandRequirement();
				mainRequirement = configType.getWriteCommandRequirement();
			}
			Command<CommandSourceStack> executor = getExecutor(configType);
			SuggestionProvider<CommandSourceStack> suggestionProvider = getSubConfigSuggestionProvider(configType);

			LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
					.then(Commands.literal(configType.getCommandPrefix())
					.requires(prefixRequirement)
					.then(getMainCommandPart(suggestionProvider, executor, mainRequirement, !configType.hasDimensionSubConfigs())));
			dispatcher.register(command);
		}

		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER);
		SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.PLAYER);

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal(PlayerConfigType.PLAYER.getCommandPrefix())
				.then(Commands.literal("for")
				.requires(sourceStack -> Commands.LEVEL_GAMEMASTERS.check(sourceStack.permissions()))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(getMainCommandPart(playerSubConfigSuggestionProvider, regularExecutor)))));
		dispatcher.register(command);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(
			SuggestionProvider<CommandSourceStack> subConfigSuggestionProvider,
			Command<CommandSourceStack> executor
	){
		return getMainCommandPart(subConfigSuggestionProvider, executor, s -> true, true);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(
			SuggestionProvider<CommandSourceStack> subConfigSuggestionProvider,
			Command<CommandSourceStack> executor,
			Predicate<CommandSourceStack> requirement,
			boolean wordSubId
	){
		return Commands.literal("sub")
				.then(Commands.literal("delete").requires(requirement)
				.then(Commands.argument("sub-id", wordSubId ? StringArgumentType.word() : StringArgumentType.string())
				.suggests(subConfigSuggestionProvider)
				.executes(executor)));
	}

	private static Command<CommandSourceStack> getExecutor(PlayerConfigType type){
		return context -> {
			ServerPlayer sourcePlayer = null;
			try {
				sourcePlayer = context.getSource().getPlayerOrException();
			} catch(CommandSyntaxException cse){
			}
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();

			String inputSubId = StringArgumentType.getString(context, "sub-id");
			UUID configPlayerUUID = null;
			if(type == PlayerConfigType.PLAYER) {
				NameAndId inputPlayer = getConfigInputPlayer(context, sourcePlayer,
						"gui.xaero_pac_config_delete_sub_too_many_targets",
						"gui.xaero_pac_config_delete_sub_invalid_target", adaptiveLocalizer);
				if(inputPlayer == null)
					return 0;
				configPlayerUUID = inputPlayer.id();
			}

			if(sourcePlayer != null) {
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
				if (serverData.getServerTickHandler().getTickCounter() == playerData.getLastSubConfigCreationTick())
					return 0;//going too fast
				playerData.setLastSubConfigCreationTick(serverData.getServerTickHandler().getTickCounter());
			}

			UUID callerId = sourcePlayer == null ? PlayerConfig.SERVER_CLAIM_UUID : sourcePlayer.getUUID();
			PlayerConfig<?> playerConfig = (PlayerConfig<?>) ServerPlayerConfigUtils.getTargetConfig(
					configPlayerUUID, callerId, type, serverData.getPlayerConfigManager()
			);
			if(playerConfig == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_invalid_config"));
				return 0;
			}
			boolean isOP = Commands.LEVEL_GAMEMASTERS.check(context.getSource().permissions());
			if(ServerConfig.CONFIG.claimsEnabled.get()) {
				if(!isOP && ServerPlayerConfigUtils.isOverClaimLimit(playerConfig)) {
					context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_claim_count_over_limit"));
					return 0;
				}
			}
			configPlayerUUID = playerConfig.getPlayerId();
			PlayerConfig<?> result = playerConfig.getSubConfig(inputSubId);
			if(result == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_not_exist"));
				return 0;
			}
			if(result == playerConfig){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_cant_main"));
				return 0;
			}
			if(configPlayerUUID == null || type.hasDimensionSubConfigs()){//doesn't have individual claims tied to sub-configs
				PlayerSubConfig<?> removedSub = playerConfig.removeSubConfig(inputSubId);
				context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_complete", removedSub.getSubId()), true);
				return 1;
			}
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = serverData.getServerClaimsManager().getPlayerInfo(configPlayerUUID);
			if(playerInfo.isTransferInProgress()){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_transfer_in_progress"));
				return 0;
			}
			if(playerInfo.hasReplacementTasks()){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_already_replacing"));
				return 0;
			}
			context.getSource().sendSuccess(serverData.getAdaptiveLocalizer().supplierFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_started", result.getSubId()), true);
			new PlayerSubConfigDeletionStarter().start(sourcePlayer, playerInfo, result, serverData, false);
			return 1;
		};
	}



}
