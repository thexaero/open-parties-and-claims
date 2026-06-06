/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.ServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static xaero.pac.common.server.command.ConfigCommandUtil.getConfigInputPlayer;

public abstract class ConfigGroupCommand {

	private final String mainLiteral;
	private final String secondaryArgumentName;
	private final boolean suggestSecondaryArgument;
	private final boolean suggestExistingGroups;

	protected ConfigGroupCommand(
			String mainLiteral,
			String secondaryArgumentName,
			boolean suggestSecondaryArgument,
			boolean suggestExistingGroups
	) {
		this.mainLiteral = mainLiteral;
		this.secondaryArgumentName = secondaryArgumentName;
		this.suggestSecondaryArgument = suggestSecondaryArgument;
		this.suggestExistingGroups = suggestExistingGroups;
	}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER);

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal("player-config").then(Commands.literal("player-groups")
						.then(getMainCommandPart(regularExecutor, PlayerConfigType.PLAYER))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("for")
						.requires(sourceStack -> sourceStack.hasPermission(2))
						.then(Commands.argument("player", GameProfileArgument.gameProfile()).then(Commands.literal("player-groups")
						.then(getMainCommandPart(regularExecutor, PlayerConfigType.PLAYER))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("default")
				.requires(sourceStack -> sourceStack.hasPermission(2)).then(Commands.literal("player-groups")
						.then(getMainCommandPart(PlayerConfigType.DEFAULT_PLAYER)))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2)).then(Commands.literal("player-groups")
						.then(getMainCommandPart(PlayerConfigType.SERVER))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("expired-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2)).then(Commands.literal("player-groups")
						.then(getMainCommandPart(PlayerConfigType.EXPIRED))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("wilderness-config")
				.requires(sourceStack -> sourceStack.hasPermission(2)).then(Commands.literal("player-groups")
						.then(getMainCommandPart(PlayerConfigType.WILDERNESS))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("party-claims-config")
				.requires(getPartyClaimsRequirement(false)).then(Commands.literal("player-groups")
						.then(getMainCommandPart(PlayerConfigType.PARTY_CLAIMS).requires(getPartyClaimsRequirement(true)))));
		dispatcher.register(command);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(PlayerConfigType type){
		return getMainCommandPart(getExecutor(type), type);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(Command<CommandSourceStack> executor, PlayerConfigType type){
		RequiredArgumentBuilder<CommandSourceStack, ?> groupIdArgument = Commands.argument("group-id", StringArgumentType.word());
		if(suggestExistingGroups) {
			groupIdArgument = groupIdArgument.suggests((context, builder) -> {
				ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
				MinecraftServer server = context.getSource().getServer();
				IServerData<
						IServerClaimsManager<
								IPlayerChunkClaim,
								IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
								IServerDimensionClaimsManager<IServerRegionClaims>
								>,
						IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
						> serverData = ServerData.from(server);
				UUID configPlayerUUID = null;
				if (type == PlayerConfigType.PLAYER) {
					configPlayerUUID = getConfigPlayerUUID(context, sourcePlayer, null);
					if(configPlayerUUID == null)
						return SharedSuggestionProvider.suggest(Stream.empty(), builder);
				}
				IPlayerConfig playerConfig = ServerPlayerConfigUtils.getTargetConfig(
						configPlayerUUID, sourcePlayer.getUUID(),
						type, serverData.getPlayerConfigManager()
				);
				if(playerConfig == null)
					return SharedSuggestionProvider.suggest(Stream.empty(), builder);
				String groupIdSoFar = builder.getRemainingLowerCase();
				Stream<String> suggestionStream = ((ServerPlayerConfigGroupManager)playerConfig.getPlayerGroups()).getIds()
						.stream()
						.filter(s -> s.toLowerCase().startsWith(groupIdSoFar))
						.limit(16)
						.sorted(Comparator.comparing(String::toLowerCase));
				return SharedSuggestionProvider.suggest(suggestionStream, builder);
			});
		}
		ArgumentBuilder<CommandSourceStack, ?> afterMain;
		if(secondaryArgumentName == null)
			afterMain = groupIdArgument.executes(executor);
		else {
			RequiredArgumentBuilder<CommandSourceStack, ?> secondaryArgument =
					Commands.argument(secondaryArgumentName, StringArgumentType.word()).executes(executor);
			if(suggestSecondaryArgument) {
				SuggestionProvider<CommandSourceStack> secondaryArgumentSuggestor = getSecondaryArgumentSuggestor(type);
				if (secondaryArgumentSuggestor != null)
					secondaryArgument.suggests(secondaryArgumentSuggestor);
			}
			afterMain = groupIdArgument.then(secondaryArgument);

		}
		return Commands.literal(mainLiteral).then(afterMain);
	}

	private Command<CommandSourceStack> getExecutor(PlayerConfigType type){
		return context -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			MinecraftServer server = context.getSource().getServer();
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
							>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
					> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();

			String inputGroupId = StringArgumentType.getString(context, "group-id");
			String inputSecondaryArgument = secondaryArgumentName == null ?
					null : StringArgumentType.getString(context, secondaryArgumentName);
			UUID configPlayerUUID = null;
			if(type == PlayerConfigType.PLAYER) {
				configPlayerUUID = getConfigPlayerUUID(context, sourcePlayer, adaptiveLocalizer);
				if(configPlayerUUID == null)
					return 0;
			}
			IPlayerConfig playerConfig = ServerPlayerConfigUtils.getTargetConfig(
					configPlayerUUID, sourcePlayer.getUUID(),
					type, serverData.getPlayerConfigManager()
			);
			Either<Component, PlayerConfigGroupActionError> result = executeCommand(context, playerConfig, inputGroupId, inputSecondaryArgument);
			if(result.right().isPresent()) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, result.right().get().getCommandMessage()));
				return 0;
			}
			sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, result.left().get()));
			return 1;
		};
	}

	protected SuggestionProvider<CommandSourceStack> getSecondaryArgumentSuggestor(PlayerConfigType type) {
		return (context, builder) -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			MinecraftServer server = context.getSource().getServer();
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
							>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
					> serverData = ServerData.from(server);
			UUID configPlayerUUID = null;
			if (type == PlayerConfigType.PLAYER) {
				configPlayerUUID = getConfigPlayerUUID(context, sourcePlayer, null);
				if(configPlayerUUID == null)
					return SharedSuggestionProvider.suggest(Stream.empty(), builder);
			}
			IPlayerConfig playerConfig = ServerPlayerConfigUtils.getTargetConfig(
					configPlayerUUID, sourcePlayer.getUUID(),
					type, serverData.getPlayerConfigManager()
			);
			if(playerConfig == null)
				return SharedSuggestionProvider.suggest(Stream.empty(), builder);
			IServerPlayerConfigGroupManager groupManager = playerConfig.getPlayerGroups();
			String inputGroupId = StringArgumentType.getString(context, "group-id");
			ICustomPlayerConfigGroup targetGroup = groupManager.getCustom(inputGroupId);
			if(targetGroup == null)
				return SharedSuggestionProvider.suggest(Stream.empty(), builder);
			String typedSoFar = builder.getRemainingLowerCase();
			Stream<String> suggestionStream = getSecondaryArgumentSuggestionBaseStream(targetGroup, groupManager, serverData)
					.filter(s -> s.toLowerCase().startsWith(typedSoFar))
					.limit(16)
					.sorted(Comparator.comparing(String::toLowerCase));
			return SharedSuggestionProvider.suggest(suggestionStream, builder);
		};
	}

	protected UUID getConfigPlayerUUID(
			CommandContext<CommandSourceStack> context,
			ServerPlayer sourcePlayer,
			AdaptiveLocalizer adaptiveLocalizer
	) throws CommandSyntaxException {
		NameAndId inputPlayer = getConfigInputPlayer(context, sourcePlayer,
				"gui.xaero_pac_config_group_too_many_targets",
				"gui.xaero_pac_config_group_invalid_target", adaptiveLocalizer);
		if(inputPlayer == null)
			return null;
		return inputPlayer.id();
	}

	protected abstract Either<Component, PlayerConfigGroupActionError> executeCommand(
			CommandContext<CommandSourceStack> context,
			IPlayerConfig playerConfig,
			String inputGroupId,
			String inputSecondaryArgument
	) throws CommandSyntaxException;

	protected Stream<String> getSecondaryArgumentSuggestionBaseStream(
			ICustomPlayerConfigGroup targetGroup,
			IServerPlayerConfigGroupManager groupManager,
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
							>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
					> serverData
	){
		throw new IllegalStateException("This must be overridden if suggestSecondaryArgument is true!");
	}

	protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId){
		return systemManager.canEditPartyConfig(playerId);
	}

	private Predicate<CommandSourceStack> getPartyClaimsRequirement(boolean affect){
		return CommandRequirementHelper.onServerThread(sourceStack -> {
			if(!ServerConfig.CONFIG.claimsEnabled.get())
				return false;
			if(!ServerConfig.CONFIG.partyOwnedClaims.get())
				return false;
			ServerPlayer sourcePlayer;
			try {
				sourcePlayer = sourceStack.getPlayerOrException();
			} catch (CommandSyntaxException e) {
				return false;
			}
			MinecraftServer server = sourceStack.getServer();
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
							>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
					> serverData = ServerData.from(server);
			UUID partyConfigOwner = serverData.getPlayerPartySystemManager().getPrimaryPartyOwnerByMember(sourcePlayer.getUUID());
			if(partyConfigOwner == null)//not in a party
				return false;
			if(!affect)
				return true;
			if(sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
				return true;
			if(sourcePlayer.getUUID().equals(partyConfigOwner))
				return true;
			return canAffectPartyConfig(serverData.getPlayerPartySystemManager(), sourcePlayer.getUUID());
		});
	}

}
