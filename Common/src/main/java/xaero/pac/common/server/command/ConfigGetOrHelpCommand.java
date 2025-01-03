/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.config.ClientboundPlayerConfigHelpPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
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
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

import static xaero.pac.common.server.command.ConfigCommandUtil.*;

public class ConfigGetOrHelpCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		SuggestionProvider<CommandSourceStack> optionSuggestor = getOptionSuggestor();
		SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.PLAYER);
		SuggestionProvider<CommandSourceStack> serverSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.SERVER);

		registerGetCommands(false, optionSuggestor, playerSubConfigSuggestionProvider, serverSubConfigSuggestionProvider, dispatcher);
		registerGetCommands(true, optionSuggestor, playerSubConfigSuggestionProvider, serverSubConfigSuggestionProvider, dispatcher);
	}

	private void registerGetCommands(boolean help, SuggestionProvider<CommandSourceStack> optionSuggestor, SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider, SuggestionProvider<CommandSourceStack> serverSubConfigSuggestionProvider, CommandDispatcher<CommandSourceStack> dispatcher){
		String literalPrefix = help ? "help" : "get";
		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER, help);

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal(literalPrefix)
				.requires(sourceStack -> true)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(regularExecutor))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("sub")
				.then(Commands.literal(literalPrefix)
				.requires(sourceStack -> true)
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(playerSubConfigSuggestionProvider)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(regularExecutor))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(Commands.literal(literalPrefix).then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(regularExecutor))))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("for")
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

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("default")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix).then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(getExecutor(PlayerConfigType.DEFAULT_PLAYER, help))))));
		dispatcher.register(command);

		Command<CommandSourceStack> serverExecutor = getExecutor(PlayerConfigType.SERVER, help);
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(serverExecutor))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("sub")
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(serverSubConfigSuggestionProvider)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(serverExecutor))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("expired-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(getExecutor(PlayerConfigType.EXPIRED, help)))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("wilderness-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.executes(getExecutor(PlayerConfigType.WILDERNESS, help)))));
		dispatcher.register(command);
	}

	static SuggestionProvider<CommandSourceStack> getOptionSuggestor(){
		return (context, builder) -> {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(context.getSource().getServer());
			return SharedSuggestionProvider.suggest(serverData.getPlayerConfigs().getAllOptionsStream().map(IPlayerConfigOptionSpecAPI::getShortenedId), builder);
		};
	}
	
	private static Command<CommandSourceStack> getExecutor(PlayerConfigType type, boolean help){
		return context -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			
			String targetConfigOptionId = StringArgumentType.getString(context, "key");
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			PlayerConfigOptionSpec<?> option = (PlayerConfigOptionSpec<?>) serverData.getPlayerConfigs().getOptionForId(targetConfigOptionId);
			if(option == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get_invalid_key"));
				return 0;
			}
			
			GameProfile inputPlayer = null;
			UUID configPlayerUUID = type == PlayerConfigType.SERVER ? PlayerConfig.SERVER_CLAIM_UUID : null;
			if(type == PlayerConfigType.PLAYER) {
				inputPlayer = getConfigInputPlayer(context, sourcePlayer,
						"gui.xaero_pac_config_option_get_too_many_targets",
						"gui.xaero_pac_config_option_get_invalid_target", adaptiveLocalizer);
				if(inputPlayer == null)
					return 0;
				configPlayerUUID = inputPlayer.getId();
			}
			IPlayerConfig playerConfig =
					type == PlayerConfigType.DEFAULT_PLAYER ?
							serverData.getPlayerConfigs().getDefaultConfig() : 
								type == PlayerConfigType.EXPIRED ?
										serverData.getPlayerConfigs().getExpiredClaimConfig() : 
											serverData.getPlayerConfigs().getLoadedConfig(configPlayerUUID);
			IPlayerConfig effectivePlayerConfig = getEffectiveConfig(context, playerConfig);
			if(effectivePlayerConfig == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get_invalid_sub"));
				return 0;
			}
			if(help){
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
				if(playerData.hasMod())
					OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(sourcePlayer, new ClientboundPlayerConfigHelpPacket(option.getId()));
				else {
					String translatedComment = serverData.getAdaptiveLocalizer().getDefaultTranslation(option.getCommentTranslation());
					if(translatedComment.equals("default"))
						translatedComment = option.getComment();
					sourcePlayer.sendSystemMessage(Component.literal(""));
					sourcePlayer.sendSystemMessage(Component.translatable(translatedComment, (Object[])option.getCommentTranslationArgs()));
				}
				return 1;
			}
			if(!effectivePlayerConfig.isOptionAllowed(option)){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get_not_allowed"));
				return 0;
			}
			Object optionValue = effectivePlayerConfig.getFromEffectiveConfig(option);
			if(effectivePlayerConfig instanceof PlayerSubConfig<?> subConfig && subConfig.isInherited(option))
				optionValue = null;
			Component optionValueName = option.getValueDisplayName(optionValue);
			if(type == PlayerConfigType.PLAYER)
				sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get", inputPlayer.getName(), targetConfigOptionId, optionValueName));
			else
				sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_get", type.getName(), targetConfigOptionId, optionValueName));
			return 1;
		};
	}

}
