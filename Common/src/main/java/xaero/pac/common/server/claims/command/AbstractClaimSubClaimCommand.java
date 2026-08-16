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

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public abstract class AbstractClaimSubClaimCommand extends AbstractClaimModeContextCommand {

	protected AbstractClaimSubClaimCommand() {
		super("sub-claim", "gui.xaero_claims_sub_use_too_many_targets", "gui.xaero_claims_sub_use_invalid_target");
	}

	@Override
	protected final int execute(
			IPlayerConfig claimConfig,
			UUID contextPlayerId,
			UUID sourcePlayerId,
			ServerPlayer sourcePlayer,
			ServerPlayerData sourcePlayerData,
			boolean impersonating,
			boolean another, ClaimingMode effectiveMode,
			IServerData<?, ?> serverData,
			CommandContext<CommandSourceStack> context
	) {
		IPlayerConfigOptionSpecAPI<String> option = effectiveMode.getSubClaimOption();
		if(option == null)
			throw new IllegalArgumentException();
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		if(!another && sourcePlayer == null){
			context.getSource().sendFailure(adaptiveLocalizer.getFor(null, "gui.xaero_claims_sub_not_a_player"));
			return 0;
		}
		return execute(
				option, claimConfig,
				contextPlayerId, sourcePlayerId, sourcePlayer,
				sourcePlayerData, impersonating, effectiveMode,
				serverData, context
		);
	}

	protected abstract int execute(
			IPlayerConfigOptionSpecAPI<String> option,
			IPlayerConfig claimConfig,
			UUID contextPlayerId,
			UUID sourcePlayerId,
			ServerPlayer sourcePlayer,
			ServerPlayerData sourcePlayerData,
			boolean impersonating,
			ClaimingMode effectiveMode,
			IServerData<?, ?> serverData,
			CommandContext<CommandSourceStack> context
	);

}
