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

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
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
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.util.ServerPlayerConfigUtils;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static xaero.pac.common.server.command.ConfigCommandUtil.getConfigInputPlayer;

public class ConfigSubListCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		for (PlayerConfigType configType : PlayerConfigType.values()) {
			if(!configType.supportsSubConfigs())
				continue;
			Predicate<CommandSourceStack> requirement = configType.getReadCommandRequirement();
			Command<CommandSourceStack> executor = getExecutor(configType);
			LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
					.then(Commands.literal(configType.getCommandPrefix())
					.requires(requirement)
					.then(getMainCommandPart(executor)));
			dispatcher.register(command);
		}

		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER);
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal(PlayerConfigType.PLAYER.getCommandPrefix())
				.then(Commands.literal("for")
				.requires(sourceStack -> Commands.LEVEL_GAMEMASTERS.check(sourceStack.permissions()))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(getMainCommandPart(regularExecutor)))));
		dispatcher.register(command);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(Command<CommandSourceStack> executor){
		return Commands.literal("sub")
				.then(Commands.literal("list")
				.then(Commands.argument("start-at", IntegerArgumentType.integer(0))
				.executes(executor)));
	}

	private static Command<CommandSourceStack> getExecutor(PlayerConfigType type){
		return context -> {
			ServerPlayer sourcePlayer = null;
			try {
				sourcePlayer = context.getSource().getPlayerOrException();
			} catch(CommandSyntaxException cse){
			}
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();

			NameAndId inputPlayer;
			UUID configPlayerUUID = null;
			if(type == PlayerConfigType.PLAYER) {
				inputPlayer = getConfigInputPlayer(context, sourcePlayer,
						"gui.xaero_pac_config_sub_list_too_many_targets",
						"gui.xaero_pac_config_sub_list_invalid_target", adaptiveLocalizer);
				if(inputPlayer == null)
					return 0;
				configPlayerUUID = inputPlayer.id();
			}

			UUID callerId = sourcePlayer == null ? PlayerConfig.SERVER_CLAIM_UUID : sourcePlayer.getUUID();
			PlayerConfig<?> playerConfig = (PlayerConfig<?>) ServerPlayerConfigUtils.getTargetConfig(
					configPlayerUUID, callerId, type, serverData.getPlayerConfigManager()
			);
			if(playerConfig == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_invalid_config"));
				return 0;
			}
			configPlayerUUID = playerConfig.getPlayerId();

			List<String> subConfigIds =  playerConfig.getSubConfigIds();
			int startAt = IntegerArgumentType.getInteger(context, "start-at");
			if(startAt >= subConfigIds.size()){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_sub_list_bad_start", subConfigIds.size()));
				return 0;
			}
			if(startAt < 0)
				startAt = 0;
			int endAt = Math.min(startAt + 64, subConfigIds.size());
			MutableComponent listMessage = adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_sub_list", startAt + 1, subConfigIds.size());
			for(int i = startAt; i < endAt; i++) {
				if(i != startAt)
					listMessage.getSiblings().add(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_sub_list_separator"));
				listMessage.getSiblings().add(Component.literal(subConfigIds.get(i)));
			}
			if(endAt < subConfigIds.size())
				listMessage.getSiblings().add(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_sub_list_there_is_more"));
			context.getSource().sendSuccess(() -> listMessage, true);
			return 1;
		};
	}



}
