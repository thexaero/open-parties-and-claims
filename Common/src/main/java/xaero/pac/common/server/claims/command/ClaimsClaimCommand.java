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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.server.command.AbstractChunkCommand;
import xaero.pac.common.server.config.ServerConfig;

import java.util.function.Predicate;

public class ClaimsClaimCommand extends AbstractChunkCommand {

	private final ClaimingMode mode;

	public ClaimsClaimCommand(boolean add, ClaimingMode mode) {
		super(
				ClaimsCommandRegister.COMMAND_PREFIX, add,
				mode == null || mode.canBeImpersonated(),
				mode == null ? null : mode.getId(),
				"claim", "unclaim",
				add ? "with" : null, "sub-id"
		);
		this.mode = mode;
	}

	@Override
	protected Command<CommandSourceStack> createChunkCommand(boolean shouldApply, boolean another, boolean opForce) {
		return ClaimsClaimCommands.createClaimCommand(shouldApply, mode, another, opForce);
	}

	@Override
	protected ForgeConfigSpec.BooleanValue getFeatureConfigOption() {
		return ServerConfig.CONFIG.claimsEnabled;
	}

	@Override
	protected Predicate<CommandSourceStack> getImpersonationRequirement() {
		return ClaimsClaimCommands.getImpersonationRequirement();
	}

	@Override
	protected SuggestionProvider<CommandSourceStack> getSubArgumentSuggestions(boolean another) {
		return ClaimsClaimCommands.getSubClaimSuggestionProvider(mode, another);
	}

	@Override
	protected Predicate<CommandSourceStack> getRequirement() {
		return mode == null ? s -> true : mode.getCommandVisibilityRequirement();
	}

}
