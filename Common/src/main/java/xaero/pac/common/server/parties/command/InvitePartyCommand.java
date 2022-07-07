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
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.UUID;
import java.util.function.Predicate;

public class InvitePartyCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> mi.getRank().ordinal() >= PartyMemberRank.MODERATOR.ordinal());
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("member")
				.requires(requirement).then(Commands.literal("invite")
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
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(server);
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo> playerParty = partyManager.getPartyByMember(playerId);
							
							ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
							UUID targetPlayerId = targetPlayer.getUUID();
							if(playerParty.getMemberInfo(targetPlayerId) != null) {
								context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_invite_already_your_party", targetPlayer.getGameProfile().getName()));
								return 0;
							} else if(partyManager.getPartyByMember(targetPlayerId) != null) {
								context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_invite_already_a_party", targetPlayer.getGameProfile().getName()));
								return 0;
							} else if(playerParty.getInviteCount() >= ServerConfig.CONFIG.maxPartyInvites.get()) {
								context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_invite_invite_limit"));
								return 0;
							} else if(playerParty.getMemberCount() >= ServerConfig.CONFIG.maxPartyMembers.get()) {
								context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_invite_member_limit"));
								return 0;
							}
							
							playerParty.invitePlayer(targetPlayerId, targetPlayer.getGameProfile().getName());

							IPartyMember casterInfo = playerParty.getMemberInfo(playerId);
							
							Component acceptComponent = new TranslatableComponent("gui.xaero_parties_invite_target_message", casterInfo.getUsername(), playerParty.getDefaultName());
							acceptComponent.getSiblings().add(new TextComponent(" "));
							acceptComponent.getSiblings().add(new TranslatableComponent("gui.xaero_parties_invite_target_message_accept").withStyle(s -> s.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openpac-parties join " + playerParty.getId())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("gui.xaero_parties_invite_target_message_accept_tooltip")))));
							targetPlayer.sendMessage(acceptComponent, playerId);
							Services.PLATFORM.getEntityAccess().getPersistentData(targetPlayer).putUUID("xaero_OPAC_LastInviteId", playerParty.getId());

							new PartyOnCommandUpdater().update(playerId, server, playerParty, serverData.getPlayerConfigs(), mi -> false, new TranslatableComponent("gui.xaero_parties_invite_party_message", new TextComponent(casterInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.GREEN)), new TextComponent(targetPlayer.getGameProfile().getName()).withStyle(s -> s.withColor(ChatFormatting.YELLOW))));
							return 1;
						}))));
		dispatcher.register(command);
	}

}
