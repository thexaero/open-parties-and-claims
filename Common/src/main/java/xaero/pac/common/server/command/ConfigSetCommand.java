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

package xaero.pac.common.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.command.ConfigGetCommand.ConfigType;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI.SetResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ConfigSetCommand {
	
	private <T> boolean tryToset(CommandContext<CommandSourceStack> context, IPlayerConfig playerConfig, PlayerConfigOptionSpec<T> option, String valueInput) {
		T value;
		try {
			value = option.getCommandInputParser().apply(valueInput);
		} catch(Throwable t) {
			context.getSource().sendFailure(Component.translatable("gui.xaero_pac_config_option_set_invalid_value_format"));
			return false;
		}
		SetResult result = playerConfig.tryToSet(option, value);
		if(result == SetResult.SUCCESS || result == SetResult.DEFAULTED)
			return true;
		if(result == SetResult.INVALID)
			context.getSource().sendFailure(Component.translatable("gui.xaero_pac_config_option_set_invalid_value"));
		return false;
	}
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		SuggestionProvider<CommandSourceStack> optionSuggestor = (context, builder) -> {
			return SharedSuggestionProvider.suggest(new ArrayList<>(PlayerConfig.OPTIONS.values()).stream().map(r -> r.getId()), builder);
		};
		Command<CommandSourceStack> regularExecutor = getExecutor(ConfigType.PLAYER);
			
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("set")
				.requires(sourceStack -> true)
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.then(Commands.argument("value", StringArgumentType.string())
				.executes(regularExecutor)))));
		dispatcher.register(command);
		
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("for")
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("set")
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.then(Commands.argument("value", StringArgumentType.string())
				.executes(regularExecutor)))))));
		dispatcher.register(command);
		
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("default")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("set")
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.then(Commands.argument("value", StringArgumentType.string())
				.executes(getExecutor(ConfigType.DEFAULT_PLAYER)))))));
		dispatcher.register(command);
		
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("set")
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.then(Commands.argument("value", StringArgumentType.string())
				.executes(getExecutor(ConfigType.SERVER))))));
		dispatcher.register(command);
		
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("expired-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("set")
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.then(Commands.argument("value", StringArgumentType.string())
				.executes(getExecutor(ConfigType.EXPIRED))))));
		dispatcher.register(command);
		
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("wilderness-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("set")
				.then(Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)
				.then(Commands.argument("value", StringArgumentType.string())
				.executes(getExecutor(ConfigType.WILDERNESS))))));
		dispatcher.register(command);
	}
	
	public Command<CommandSourceStack> getExecutor(ConfigType type){
		return context -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			
			String targetConfigOptionId = StringArgumentType.getString(context, "key");
			PlayerConfigOptionSpec<?> option = PlayerConfig.OPTIONS.get(targetConfigOptionId);
			if(option == null) {
				context.getSource().sendFailure(Component.translatable("gui.xaero_pac_config_option_set_invalid_key"));
				return 0;
			}
			GameProfile inputPlayer = null;
			UUID configPlayerUUID = type == ConfigType.SERVER ? PlayerConfig.SERVER_CLAIM_UUID : null;
			if(type == ConfigType.PLAYER) {
				try {
					Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "player");
					if(profiles.size() > 1) {
						context.getSource().sendFailure(Component.translatable("gui.xaero_pac_config_option_set_too_many_targets"));
						return 0;
					} else if(profiles.isEmpty()) {
						context.getSource().sendFailure(Component.translatable("gui.xaero_pac_config_option_set_invalid_target"));
						return 0;
					}
					inputPlayer = profiles.iterator().next();
				} catch(Exception iae) {
					inputPlayer = sourcePlayer.getGameProfile();
				}
				configPlayerUUID = inputPlayer.getId();
			}

			String valueInput = StringArgumentType.getString(context, "value");
			
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>>
				serverData = ServerData.from(server);
			IPlayerConfig playerConfig = 
					type == ConfigType.DEFAULT_PLAYER ? 
						serverData.getPlayerConfigs().getDefaultConfig() : 
							type == ConfigType.EXPIRED ? 
								serverData.getPlayerConfigs().getExpiredClaimConfig() : 
										serverData.getPlayerConfigs().getLoadedConfig(configPlayerUUID);
			
			boolean isOP = context.getSource().hasPermission(2);
			if(!isOP && ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(option.getId())) {
				//such options are not redirected to the default config, so they need a separate check
				context.getSource().sendFailure(Component.translatable("gui.xaero_pac_config_op_option"));
				return 0;
			}
			if(!tryToset(context, playerConfig, option, valueInput))
				return 0;
			Object wantedValue = option.getCommandInputParser().apply(valueInput);
			Object actualValue = playerConfig.getFromEffectiveConfig(option);
			if(type == ConfigType.PLAYER)
				sourcePlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_config_option_set", inputPlayer.getName(), targetConfigOptionId, option.getCommandOutputWriterCast().apply(wantedValue)));
			else
				sourcePlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_config_option_set", type.getName(), targetConfigOptionId, option.getCommandOutputWriterCast().apply(wantedValue)));
			if(wantedValue != actualValue)
				sourcePlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_config_option_set_server_force", option.getCommandOutputWriterCast().apply(actualValue)));
			return 1;
		};
	}

}
