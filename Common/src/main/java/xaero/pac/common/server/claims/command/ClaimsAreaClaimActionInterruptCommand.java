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

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public class ClaimsAreaClaimActionInterruptCommand extends AbstractClaimModeContextCommand {

	protected ClaimsAreaClaimActionInterruptCommand() {
		super("interrupt", "gui.xaero_claims_area_action_interrupt_too_many_targets", "gui.xaero_claims_area_action_interrupt_invalid_target");
	}

	@Override
	protected int execute(
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
	) {
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		UUID targetClaimId = claimConfig.getPlayerId();
		if(targetClaimId == null)//shouldn't actually happen as of writing
			throw new IllegalArgumentException();
		IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
		IServerPlayerClaimInfo<?> playerInfo = claimsManager.getPlayerInfo(targetClaimId);
		playerInfo.stopAllAreaClaimActionTasks(serverData);
		Component defaultClaimName = claimsManager.getDefaultName(targetClaimId, false, true).copy().withStyle(ChatFormatting.GREEN);
		context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(sourcePlayer, "gui.xaero_claims_area_action_interrupt_success", defaultClaimName), true);
		return 1;
	}

}
