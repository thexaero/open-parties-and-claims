/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.awt.*;
import java.util.UUID;
import java.util.function.Predicate;

public class AllyPartyCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> mi.getRank().ordinal() >= PartyMemberRank.MODERATOR.ordinal());
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("ally")
				.requires(requirement).then(Commands.literal("add")
				.then(Commands.argument("player", EntityArgument.player())
						.suggests((context, builder) -> {
							PlayerList playerlist = context.getSource().getServer().getPlayerList();
							return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().map(targetPlayer -> {
								return targetPlayer.getGameProfile().getName();
							}), builder);
						})
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							UUID playerId = player.getUUID();
							MinecraftServer server = context.getSource().getServer();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
							AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);
							
							ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> targetPlayerParty = partyManager.getPartyByMember(targetPlayer.getUUID());
							if(targetPlayerParty == playerParty) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_ally_player_target_in_your_party", targetPlayer.getGameProfile().getName()));
								return 0;
							} else if(targetPlayerParty == null) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_ally_player_target_not_in_party", targetPlayer.getGameProfile().getName()));
								return 0;
							} else if(playerParty.isAlly(targetPlayerParty.getId())) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_ally_player_target_already_ally", targetPlayer.getGameProfile().getName(), targetPlayerParty.getDefaultName()));
								return 0;
							} else if(playerParty.getAllyCount() >= ServerConfig.CONFIG.maxPartyAllies.get()) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_ally_limit"));
								return 0;
							}
							playerParty.addAllyParty(targetPlayerParty.getId());
							
							new PartyOnCommandUpdater().update(playerId, serverData, targetPlayerParty, serverData.getPlayerConfigs(), mi -> false, new TranslatableComponent("gui.xaero_parties_ally_player_target_party_info", new TextComponent(playerParty.getDefaultName()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), targetPlayerParty.getDefaultName()));

							IPartyMember casterInfo = playerParty.getMemberInfo(playerId);
							new PartyOnCommandUpdater().update(playerId, serverData, playerParty, serverData.getPlayerConfigs(), mi -> false, new TranslatableComponent("gui.xaero_parties_ally_caster_party_info", new TextComponent(casterInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), new TextComponent(targetPlayerParty.getDefaultName()).withStyle(s -> s.withColor(ChatFormatting.YELLOW))));
							return 1;
						}))));
		dispatcher.register(command);
	}

}
