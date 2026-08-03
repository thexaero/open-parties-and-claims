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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.packet.claims.ClientboundClaimModesPacket;
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
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

public class ClaimsSubClaimUseCommand extends ClaimAbstractSubClaimCommand {

	@Override
	protected LiteralArgumentBuilder<CommandSourceStack> getExecutivePart(ClaimingMode mode, boolean another){
		return Commands.literal("use")
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(ClaimsClaimCommands.getSubClaimSuggestionProvider(mode, another))
				.executes(getExecutor(mode, another)));
	}

	private static Command<CommandSourceStack> getExecutor(ClaimingMode mode, boolean another){
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
			IPlayerConfigOptionSpecAPI<String> option = effectiveMode.getSubClaimOption();
			if(option == null)
				throw new IllegalArgumentException();
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			if(!another && sourcePlayer == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(null, "gui.xaero_claims_sub_not_a_player"));
				return 0;
			}
			String inputSubId = StringArgumentType.getString(context, "sub-id");
			UUID configPlayerUUID = ClaimsClaimCommands.getClaimInputPlayerId(context, sourcePlayer,
					"gui.xaero_claims_sub_use_too_many_targets",
					"gui.xaero_claims_sub_use_invalid_target", serverData, another, effectiveMode);
			if(configPlayerUUID == null)
				return 0;
			UUID sourcePlayerId = sourcePlayer == null ? PlayerConfig.SERVER_CLAIM_UUID : sourcePlayer.getUUID();
			boolean impersonating = !another && !configPlayerUUID.equals(sourcePlayerId);
			IPlayerConfig rootConfig = ServerPlayerConfigUtils.getTargetConfig(configPlayerUUID, configPlayerUUID, effectiveMode.getConfigType(), serverData.getPlayerConfigManager());
			IPlayerConfig subConfig = rootConfig == null ? null : rootConfig.getSubConfig(inputSubId);
			if(subConfig == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use_not_exist"));
				return 0;
			}
			if(impersonating) {
				sourcePlayerData.getClaimsImpersonationInfo().setSubIndex(effectiveMode, subConfig.getSubIndex());
				OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(sourcePlayer, ClientboundClaimModesPacket.get(sourcePlayerData));
			} else {
				IPlayerConfig playerConfig = serverData.getPlayerConfigManager().getLoadedConfig(configPlayerUUID);
				IPlayerConfigAPI.SetResult setResult = playerConfig.tryToSet(option, inputSubId);
				if (setResult == IPlayerConfigAPI.SetResult.INVALID) {
					context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use_invalid_value"));
					return 0;
				}
			}
			context.getSource().sendSuccess(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use", inputSubId, effectiveMode.getId()), true);
			return 1;
		};
	}

}
