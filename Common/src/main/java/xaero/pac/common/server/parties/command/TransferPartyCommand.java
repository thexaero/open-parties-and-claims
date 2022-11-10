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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.UUID;
import java.util.function.Predicate;

public class TransferPartyCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> party.getOwner() == mi);
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(
				Commands.literal("transfer").requires(requirement).then(Commands.argument("new-owner", StringArgumentType.word())
						.suggests(PartyCommands.getPartyMemberSuggestor())
						.executes(context -> {
							context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_transfer_use_confirm"));
							return 0;
						})
						.then(Commands.literal("confirm")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							UUID playerId = player.getUUID();
							MinecraftServer server = context.getSource().getServer();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);

							String targetUsername = StringArgumentType.getString(context, "new-owner");
							IPartyMember targetMember = playerParty.getMemberInfo(targetUsername);
							
							if(targetMember == null) {
								context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_transfer_not_member", targetUsername));
								return 0;
							}
							if(targetMember == playerParty.getOwner()) {
								context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_transfer_already_owner", targetUsername));
								return 0;
							}
							IPartyMember casterInfo = playerParty.getMemberInfo(playerId);
							if(playerParty.changeOwner(targetMember.getUUID(), targetMember.getUsername())) {
								UUID targetPlayerId = targetMember.getUUID();
								ServerPlayer newOwnerPlayer = server.getPlayerList().getPlayer(targetPlayerId);
								if (newOwnerPlayer != null)
									server.getCommands().sendCommands(newOwnerPlayer);
								server.getCommands().sendCommands(player);
								new PartyOnCommandUpdater().update(playerId, server, playerParty, serverData.getPlayerConfigs(), mi -> false, new TranslatableComponent("gui.xaero_parties_transfer_success", new TextComponent(casterInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), new TextComponent(targetMember.getUsername()).withStyle(s -> s.withColor(ChatFormatting.YELLOW))));
								return 1;
							}
							context.getSource().sendFailure(new TranslatableComponent("gui.xaero_parties_transfer_failed"));
							return 0;
						}))));
		dispatcher.register(command);
	}

}
