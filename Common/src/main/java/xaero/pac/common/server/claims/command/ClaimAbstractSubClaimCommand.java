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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;

public abstract class ClaimAbstractSubClaimCommand {

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
		LiteralArgumentBuilder<CommandSourceStack> mainPart = Commands.literal("sub-claim")
				.then(getExecutivePart(mode));
		registerCommand(mainPart, mode, dispatcher);
	}

	private void registerAnotherCommand(
			ClaimingMode mode,
			CommandDispatcher<CommandSourceStack> dispatcher
	){
		LiteralArgumentBuilder<CommandSourceStack> mainPart = Commands.literal("sub-claim")
				.then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(getExecutivePart(mode))));
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

	protected abstract LiteralArgumentBuilder<CommandSourceStack> getExecutivePart(ClaimingMode mode);

}
