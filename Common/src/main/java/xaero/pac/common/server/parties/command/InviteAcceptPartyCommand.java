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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
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
import java.util.stream.Stream;

public class InviteAcceptPartyCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getNonMemberRequirement(p -> true);
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("join")
				.requires(requirement)
				.then(Commands.argument("id", StringArgumentType.word())
						.suggests((context, builder) -> {
							ServerPlayer casterPlayer = context.getSource().getPlayerOrException();
							CompoundTag data = Services.PLATFORM.getEntityAccess().getPersistentData(casterPlayer);
							Stream<String> lastInviteId = data.hasUUID("xaero_OPAC_LastInviteId") ? Stream.of(data.getUUID("xaero_OPAC_LastInviteId").toString()) : Stream.empty();
							return SharedSuggestionProvider.suggest(lastInviteId, builder);
						})
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							UUID playerId = player.getUUID();
							MinecraftServer server = context.getSource().getServer();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);
							if(playerParty != null) {
								context.getSource().sendFailure(Component.translatable("gui.xaero_parties_join_party_already_in_one"));
								return 0;
							}
							String targetPartyStringId = context.getArgument("id", String.class);
							UUID targetPartyId;
							try {
								targetPartyId = UUID.fromString(targetPartyStringId);
							} catch(IllegalArgumentException iae) {
								context.getSource().sendFailure(Component.translatable("gui.xaero_parties_join_invalid_id"));
								return 0;
							}
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> targetParty = partyManager.getPartyById(targetPartyId);
							if(targetParty == null) {
								context.getSource().sendFailure(Component.translatable("gui.xaero_parties_join_party_not_exist"));
								return 0;
							}
							if(!targetParty.isInvited(playerId)) {
								context.getSource().sendFailure(Component.translatable("gui.xaero_parties_join_party_not_invited"));
								return 0;
							}
							if(targetParty.getMemberCount() >= ServerConfig.CONFIG.maxPartyMembers.get()) {
								context.getSource().sendFailure(Component.translatable("gui.xaero_parties_join_member_limit"));
								return 0;
							}
							IPartyMember addedPartyMember = targetParty.addMember(playerId, null, player.getGameProfile().getName());
							if(addedPartyMember == null)
								return 0;
							player.sendSystemMessage(Component.translatable("gui.xaero_parties_join_success", targetParty.getDefaultName()));
							
							new PartyOnCommandUpdater().update(playerId, server, targetParty, serverData.getPlayerConfigs(), mi -> false, Component.translatable("gui.xaero_parties_join_success_info", Component.literal(addedPartyMember.getUsername()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN))));
							server.getCommands().sendCommands(player);
							return 1;
						})));
		dispatcher.register(command);
	}

}
