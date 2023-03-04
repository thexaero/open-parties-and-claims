/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.UUID;
import java.util.function.Predicate;

public class KickPartyCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> mi.getRank().ordinal() >= PartyMemberRank.MODERATOR.ordinal());
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("member")
				.requires(requirement).then(Commands.literal("kick")
				.then(Commands.argument("name", StringArgumentType.word())
						.suggests(PartyCommands.getPartyMemberOrInviteSuggestor())
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							UUID playerId = player.getUUID();
							MinecraftServer server = context.getSource().getServer();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
							AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);
							
							String targetUsername = StringArgumentType.getString(context, "name");
							IPartyPlayerInfo targetPlayerInfo = playerParty.getMemberInfo(targetUsername);
							if(targetPlayerInfo == null)
								targetPlayerInfo = playerParty.getInvite(targetUsername);
							if(targetPlayerInfo == null) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_kick_not_member", targetUsername));
								return 0;
							}
							
							IPartyMember casterInfo = playerParty.getMemberInfo(playerId);
							boolean targetIsMember = targetPlayerInfo instanceof IPartyMember;
							boolean casterIsOwner = playerParty.getOwner() == casterInfo;
							
							if(targetIsMember) {
								IPartyMember targetMember = (IPartyMember) targetPlayerInfo;
								if(targetMember == playerParty.getOwner()) {
									context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_kick_owner"));
									return 0;
								}
								if(!casterIsOwner && targetMember.getRank().ordinal() > casterInfo.getRank().ordinal()) {
									context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_kick_higher_rank"));
									return 0;
								}
							}
							
							playerParty.uninvitePlayer(targetPlayerInfo.getUUID());
							if(targetIsMember) {
								playerParty.removeMember(targetPlayerInfo.getUUID());
								
								UUID targetPlayerId = targetPlayerInfo.getUUID();
								ServerPlayer kickedPlayer = server.getPlayerList().getPlayer(targetPlayerId);
								if(kickedPlayer != null) {
									server.getCommands().sendCommands(kickedPlayer);
									Component acceptComponent = adaptiveLocalizer.getFor(kickedPlayer, "gui.xaero_parties_kick_target_message", playerParty.getDefaultName()).withStyle(s -> s.withColor(ChatFormatting.RED));
									kickedPlayer.sendSystemMessage(acceptComponent);
								}
							}
							
							new PartyOnCommandUpdater().update(playerId, serverData, playerParty, serverData.getPlayerConfigs(), mi -> false, Component.translatable("gui.xaero_parties_kick_party_message", Component.literal(casterInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), Component.literal(targetPlayerInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.RED))));

							return 1;
						}))));
		dispatcher.register(command);
	}

}
