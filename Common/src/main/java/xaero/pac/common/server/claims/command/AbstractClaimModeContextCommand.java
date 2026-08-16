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

package xaero.pac.common.server.claims.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public abstract class AbstractClaimModeContextCommand {

	private final String literal;
	private final String tooManyTargetsKey;
	private final String invalidTargetKey;

	protected AbstractClaimModeContextCommand(String literal, String tooManyTargetsKey, String invalidTargetKey) {
		this.literal = literal;
		this.tooManyTargetsKey = tooManyTargetsKey;
		this.invalidTargetKey = invalidTargetKey;
	}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		registerCommands(null, dispatcher);//default mode
		for (IClaimingModeAPI claimingMode : ClaimingModes.ALL_IMMUTABLE.values())
			registerCommands(claimingMode, dispatcher);
	}

	private void registerCommands(
			IClaimingModeAPI modeAPI,
			CommandDispatcher<CommandSourceStack> dispatcher
	){
		ClaimingMode mode = (ClaimingMode) modeAPI;
		registerSelfCommand(mode, dispatcher);
		registerAnotherCommand(mode, dispatcher);
	}

	private void registerSelfCommand(
			ClaimingMode mode,
			CommandDispatcher<CommandSourceStack> dispatcher
	){
		LiteralArgumentBuilder<CommandSourceStack> mainPart;
		LiteralArgumentBuilder<CommandSourceStack> executivePart = getExecutivePart(mode, false);
		if(executivePart != null)
			mainPart = Commands.literal(literal).then(executivePart);
		else
			mainPart = Commands.literal(literal).executes(getExecutor(mode, false));
		registerCommand(mainPart, mode, dispatcher);
	}

	private void registerAnotherCommand(
			ClaimingMode mode,
			CommandDispatcher<CommandSourceStack> dispatcher
	){
		LiteralArgumentBuilder<CommandSourceStack> executivePart = getExecutivePart(mode, true);
		RequiredArgumentBuilder<CommandSourceStack, ?> playerArgument = Commands.argument("player", GameProfileArgument.gameProfile());
		if(executivePart != null)
			playerArgument.then(executivePart);
		else
			playerArgument.executes(getExecutor(mode, true));
		LiteralArgumentBuilder<CommandSourceStack> mainPart = Commands.literal(literal)
			.then(Commands.literal("for")
			.requires(ClaimsClaimCommands.getImpersonationRequirement())
			.then(playerArgument));
		registerCommand(mainPart, mode, dispatcher);
	}

	private void registerCommand(
			LiteralArgumentBuilder<CommandSourceStack> mainPart,
			ClaimingMode mode,
			CommandDispatcher<CommandSourceStack> dispatcher
	){
		LiteralArgumentBuilder<CommandSourceStack> prefixedMainPart =
				mode == null ? mainPart : Commands.literal(mode.getId()).then(mainPart);
		LiteralArgumentBuilder<CommandSourceStack> command =
				Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX)
				.then(prefixedMainPart.requires(mode == null ? s -> true : mode.getCommandVisibilityRequirement()));
		dispatcher.register(command);
	}

	protected final Command<CommandSourceStack> getExecutor(ClaimingMode mode, boolean another){
		return context -> {
			ServerPlayer sourcePlayer = null;
			try {
				sourcePlayer = context.getSource().getPlayerOrException();
			} catch(CommandSyntaxException cse){
			}
			ServerPlayerData sourcePlayerData = sourcePlayer == null ? null : (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
			ClaimingMode effectiveMode = mode == null ?
					(another || sourcePlayer == null ? (ClaimingMode) ClaimingModes.PLAYER : sourcePlayerData.getClaimingMode()) :
					mode;
			MinecraftServer server = context.getSource().getServer();
			IServerData<?, ?> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			UUID contextPlayerId = ClaimsClaimCommands.getClaimInputPlayerId(context, sourcePlayer,
					tooManyTargetsKey, invalidTargetKey, serverData, another, effectiveMode);
			if(contextPlayerId == null)
				return 0;
			if(effectiveMode.getPermissionChecker() != null) {
				ClaimResult.Type failureType = effectiveMode.getPermissionChecker().apply(contextPlayerId, serverData.getServerClaimsManager());
				if(failureType != null) {
					if(sourcePlayer != null && effectiveMode == sourcePlayerData.getRawClaimingMode())
						serverData.getServerClaimsManager().getPermissionHandler().resetClaimingMode(sourcePlayer);
					context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, failureType.message));
					return 0;
				}
			}
			IPlayerConfig claimConfig = ServerPlayerConfigUtils.getTargetConfig(
					contextPlayerId, contextPlayerId,
					effectiveMode.getConfigType(), serverData.getPlayerConfigManager()
			);
			if(claimConfig == null)
				return 0;
			if(claimConfig.getPlayerId() == null)
				return 0;
			UUID sourcePlayerId = sourcePlayer == null ? PlayerConfig.SERVER_CLAIM_UUID : sourcePlayer.getUUID();
			boolean impersonating = !another && !contextPlayerId.equals(sourcePlayerId);
			return execute(
					claimConfig, contextPlayerId,
					sourcePlayerId, sourcePlayer, sourcePlayerData,
					impersonating, another, effectiveMode,
					serverData,
					context);
		};
	}

	protected abstract int execute(
			IPlayerConfig claimConfig,
			UUID contextPlayerId,
			UUID sourcePlayerId,
			ServerPlayer sourcePlayer,
			ServerPlayerData sourcePlayerData,
			boolean impersonating,
			boolean another,
			ClaimingMode effectiveMode,
			IServerData<?, ?> serverData,
			CommandContext<CommandSourceStack> context
	);

	protected LiteralArgumentBuilder<CommandSourceStack> getExecutivePart(ClaimingMode mode, boolean another){
		return null;
	}

}
