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

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.config.ClientboundPlayerConfigHelpPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

import static xaero.pac.common.server.command.ConfigCommandUtil.*;

public class ConfigGetOrHelpCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		SuggestionProvider<CommandSourceStack> optionSuggestor = getOptionSuggestor();
		SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.PLAYER);

		registerGetCommands(
				false, optionSuggestor, playerSubConfigSuggestionProvider,
				dispatcher
		);
		registerGetCommands(
				true, optionSuggestor, playerSubConfigSuggestionProvider,
				dispatcher
		);
	}

	private void registerGetCommands(
			boolean help,
			SuggestionProvider<CommandSourceStack> optionSuggestor,
			SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider,
			CommandDispatcher<CommandSourceStack> dispatcher
	){
		String literalPrefix = help ? "help" : "get";

		for (PlayerConfigType configType : PlayerConfigType.values()) {
			Command<CommandSourceStack> executor = getExecutor(configType, help);
			Predicate<CommandSourceStack> requirement = configType.getReadCommandRequirement();

			LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
					.then(Commands.literal(configType.getCommandPrefix())
					.requires(requirement)
					.then(Commands.literal(literalPrefix)
					.then(Commands.argument("key", StringArgumentType.word())
					.suggests(optionSuggestor)
					.executes(executor))));
			dispatcher.register(command);

			if(!configType.supportsSubConfigs())
				continue;
			SuggestionProvider<CommandSourceStack> subConfigSuggestionProvider = getSubConfigSuggestionProvider(configType);
			//sub version of this ^
			command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
					.then(Commands.literal(configType.getCommandPrefix())
					.requires(requirement)
					.then(Commands.literal("sub")
					.then(Commands.literal(literalPrefix)
					.then(Commands.argument("sub-id", StringArgumentType.word())
					.suggests(subConfigSuggestionProvider)
					.then(Commands.argument("key", StringArgumentType.word())
					.suggests(optionSuggestor)
					.executes(executor))))));
			dispatcher.register(command);
		}

		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER, help);

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal(PlayerConfigType.PLAYER.getCommandPrefix())
				.then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(Commands.literal(literalPrefix).then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(regularExecutor))))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal(PlayerConfigType.PLAYER.getCommandPrefix())
				.then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(Commands.literal("sub")
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(playerSubConfigSuggestionProvider)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(regularExecutor))))))));
		dispatcher.register(command);
	}

	static SuggestionProvider<CommandSourceStack> getOptionSuggestor(){
		return (context, builder) -> {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(context.getSource().getServer());
			return SharedSuggestionProvider.suggest(
					serverData.getPlayerConfigManager().getAllOptionsStream()
							.filter(IPlayerConfigOptionSpecAPI::isDirectlyConfigurable)
							.map(IPlayerConfigOptionSpecAPI::getShortenedId),
					builder
			);
		};
	}
	
	private static Command<CommandSourceStack> getExecutor(PlayerConfigType type, boolean help){
		return context -> {
			ServerPlayer sourcePlayer = null;
			try {
				sourcePlayer = context.getSource().getPlayerOrException();
			} catch(CommandSyntaxException cse){
			}

			String targetConfigOptionId = StringArgumentType.getString(context, "key");
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			PlayerConfigOptionSpec<?> option = (PlayerConfigOptionSpec<?>) serverData.getPlayerConfigManager().getOptionForId(targetConfigOptionId);
			if(option == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get_invalid_key"));
				return 0;
			}
			if(!option.isDirectlyConfigurable()) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, PlayerConfigConstants.OPTION_NOT_DIRECTLY_CONFIGURABLE));
				return 0;
			}

			NameAndId inputPlayer = null;
			UUID configPlayerUUID = null;
			if(type == PlayerConfigType.PLAYER) {
				inputPlayer = getConfigInputPlayer(context, sourcePlayer,
						"gui.xaero_pac_config_option_get_too_many_targets",
						"gui.xaero_pac_config_option_get_invalid_target", adaptiveLocalizer);
				if(inputPlayer == null)
					return 0;
				configPlayerUUID = inputPlayer.id();
			}
			UUID callerId = sourcePlayer == null ? PlayerConfig.SERVER_CLAIM_UUID : sourcePlayer.getUUID();
			IPlayerConfig playerConfig = ServerPlayerConfigUtils.getTargetConfig(
					configPlayerUUID, callerId, type, serverData.getPlayerConfigManager()
			);
			if(playerConfig == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_invalid_config"));
				return 0;
			}
			configPlayerUUID = playerConfig.getPlayerId();
			IPlayerConfig effectivePlayerConfig = getEffectiveConfig(context, playerConfig);
			if(effectivePlayerConfig == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get_invalid_sub"));
				return 0;
			}
			if(help){
				ServerPlayerData playerData = sourcePlayer == null ? null : (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
				if(playerData != null && playerData.hasMod())
					OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(sourcePlayer, new ClientboundPlayerConfigHelpPacket(option.getId()));
				else {
					String translatedComment = serverData.getAdaptiveLocalizer().getDefaultTranslation(option.getCommentTranslation());
					if(translatedComment.equals("default"))
						translatedComment = option.getComment();
					final String finalTranslatedComment = translatedComment;
					context.getSource().sendSuccess(() -> Component.literal(""), false);
					context.getSource().sendSuccess(() -> Component.translatable(finalTranslatedComment, (Object[])option.getCommentTranslationArgs()), false);
				}
				return 1;
			}
			if(!effectivePlayerConfig.isOptionAllowed(option) || !option.getConfigTypeFilter().test(type)){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get_not_allowed"));
				return 0;
			}
			Object optionValue = effectivePlayerConfig.getFromEffectiveConfig(option);
			if(effectivePlayerConfig instanceof PlayerSubConfig<?> subConfig && subConfig.isInherited(option))
				optionValue = null;
			Component optionValueName = option.getValueDisplayName(optionValue);
			if(type == PlayerConfigType.PLAYER)
				context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(sourcePlayer, "gui.xaero_pac_config_option_get", inputPlayer.name(), targetConfigOptionId, optionValueName), false);
			else
				context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(sourcePlayer, "gui.xaero_pac_config_option_get", type.getName(), targetConfigOptionId, optionValueName), false);
			return 1;
		};
	}

}
