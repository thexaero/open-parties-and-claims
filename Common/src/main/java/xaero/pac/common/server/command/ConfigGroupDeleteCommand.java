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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;

import java.util.UUID;

public class ConfigGroupDeleteCommand extends ConfigGroupCommand {

	private final Component MESSAGE = Component.translatable("gui.xaero_pac_config_delete_group_confirmation_request")
			.withStyle(s -> s.withColor(ChatFormatting.YELLOW));

	protected ConfigGroupDeleteCommand() {
		super("delete", null, false, true);
	}

	@Override
	protected Either<Component, PlayerConfigGroupActionError> executeCommand(
			CommandContext<CommandSourceStack> context,
			IPlayerConfig playerConfig,
			String inputGroupId,
			String inputSecondaryArgument
	) {
		return Either.left(MESSAGE);
	}

	@Override
	protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId) {
		return systemManager.canCreatePartyConfigGroups(playerId);
	}

}
