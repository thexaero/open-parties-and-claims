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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.packet.claims.ClientboundClaimModesPacket;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public class ClaimsSubClaimUseCommand extends AbstractClaimSubClaimCommand {

	@Override
	protected LiteralArgumentBuilder<CommandSourceStack> getExecutivePart(ClaimingMode mode, boolean another){
		return Commands.literal("use")
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(ClaimsClaimCommands.getSubClaimSuggestionProvider(mode, another))
				.executes(getExecutor(mode, another)));
	}

	@Override
	protected int execute(IPlayerConfigOptionSpecAPI<String> option, IPlayerConfig claimConfig, UUID contextPlayerId, UUID sourcePlayerId, ServerPlayer sourcePlayer, ServerPlayerData sourcePlayerData, boolean impersonating, ClaimingMode effectiveMode, IServerData<?, ?> serverData, CommandContext<CommandSourceStack> context) {
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		String inputSubId = StringArgumentType.getString(context, "sub-id");
		IPlayerConfig claimSubConfig = claimConfig == null ? null : claimConfig.getSubConfig(inputSubId);
		if(claimSubConfig == null){
			context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use_not_exist"));
			return 0;
		}
		if(impersonating) {
			sourcePlayerData.getClaimsImpersonationInfo().setSubIndex(effectiveMode, claimSubConfig.getSubIndex());
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(sourcePlayer, ClientboundClaimModesPacket.get(sourcePlayerData));
		} else {
			IPlayerConfig playerConfig = serverData.getPlayerConfigManager().getLoadedConfig(contextPlayerId);
			IPlayerConfigAPI.SetResult setResult = playerConfig.tryToSet(option, inputSubId);
			if (setResult == IPlayerConfigAPI.SetResult.INVALID) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use_invalid_value"));
				return 0;
			}
		}
		context.getSource().sendSuccess(adaptiveLocalizer.supplierFor(sourcePlayer, "gui.xaero_claims_sub_use", inputSubId, effectiveMode.getId()), true);
		return 1;
	}

}
