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

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;

public class AboutPartyCommand {
	
	private static final int MAX_MEMBER_COUNT = 32;
	private static final int MAX_ALLY_COUNT = 32;
	private static final int MAX_INVITES_COUNT = 16;
	
	private static <T> int createLimitedList(Component listComponent, int maxCount, Iterator<T> iterator, Consumer<T> adder) {
		int count = 0;
		while(iterator.hasNext()) {
			adder.accept(iterator.next());
			count++;
			if(count == maxCount) {
				listComponent.getSiblings().add(Component.literal(" ..."));
				break;
			}
		}
		return count;
	}
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Command<CommandSourceStack> action = context -> {
			GameProfile targetProfile;
			ServerPlayer casterPlayer = context.getSource().getPlayerOrException();
			try {
				Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "profile");
				if(profiles.size() == 1)
					targetProfile = profiles.iterator().next();
				else
					targetProfile = null;
			} catch(IllegalArgumentException iae) {
				try {
					ServerPlayer inputPlayer = EntityArgument.getPlayer(context, "player");
					if(inputPlayer != null)
						targetProfile = inputPlayer.getGameProfile();
					else
						targetProfile = null;
				} catch(IllegalArgumentException iae2) {
					targetProfile = casterPlayer.getGameProfile();
				}
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(context.getSource().getServer());
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			if(targetProfile == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_about_invalid_player"));
				return 0;
			}
			final GameProfile profile = targetProfile;
			UUID casterPlayerId = casterPlayer.getUUID();
			IPartyManager<IServerParty<IPartyMember,IPartyPlayerInfo,IPartyAlly>> partyManager = serverData.getPartyManager();
			IServerParty<IPartyMember,IPartyPlayerInfo,IPartyAlly> playerParty = partyManager.getPartyByMember(profile.getId());
			if(playerParty == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_about_no_party", profile.getName()));
				return 0;
			}
			
			casterPlayer.sendSystemMessage(Component.literal(""));
			casterPlayer.sendSystemMessage(Component.literal("===== Open Parties and Claims").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
			casterPlayer.sendSystemMessage(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_player").withStyle(s -> s.withColor(ChatFormatting.GOLD)));
			casterPlayer.sendSystemMessage(Component.literal(profile.getName()).withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(profile.getId().toString())))));
			casterPlayer.sendSystemMessage(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_current_party").withStyle(s -> s.withColor(ChatFormatting.GOLD)));
			String partyName = playerParty.getDefaultName();
			IPlayerConfig ownerConfig = serverData.getPlayerConfigs().getLoadedConfig(playerParty.getOwner().getUUID());
			String partyCustomName = ownerConfig.getEffective(PlayerConfigOptions.PARTY_NAME);
			String tooltipPrefix = !partyCustomName.isEmpty() ? partyName + "\n" : "";
			if(!partyCustomName.isEmpty())
				partyName = partyCustomName;
			casterPlayer.sendSystemMessage(Component.literal(partyName).withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tooltipPrefix + playerParty.getId().toString())))));
			
			casterPlayer.sendSystemMessage(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_party_members", playerParty.getMemberCount() + "/" + ServerConfig.CONFIG.maxPartyMembers.get()).withStyle(s -> s.withColor(ChatFormatting.GOLD)));
			Component partyMembersComponent = Component.literal("");

			Consumer<IPartyMember> partyMemberConsumer = mi -> {
				if(!partyMembersComponent.getSiblings().isEmpty())
					partyMembersComponent.getSiblings().add(Component.literal(", "));
				partyMembersComponent.getSiblings().add(Component.literal(mi.getUsername()).withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(mi.getUUID().toString())))));
				if(mi.getRank() != PartyMemberRank.MEMBER) {
					Component rankComponent = Component.literal(playerParty.getOwner() == mi ? "OWNER" : mi.getRank().toString()).withStyle(s -> s.withColor(mi.getRank().getColor()));
					partyMembersComponent.getSiblings().add(adaptiveLocalizer.getFor(casterPlayer, "[%s]", rankComponent));
				}
			};
			int staffCount = createLimitedList(partyMembersComponent, MAX_MEMBER_COUNT, playerParty.getStaffInfoStream().iterator(), partyMemberConsumer);
			if(staffCount < MAX_MEMBER_COUNT)
				createLimitedList(partyMembersComponent, MAX_MEMBER_COUNT - staffCount, playerParty.getNonStaffInfoStream().iterator(), partyMemberConsumer);
			casterPlayer.sendSystemMessage(partyMembersComponent);
			
			casterPlayer.sendSystemMessage(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_party_allies", playerParty.getAllyCount() + "/" + ServerConfig.CONFIG.maxPartyAllies.get()).withStyle(s -> s.withColor(ChatFormatting.GOLD)));
			Component partyAlliesComponent = Component.literal("");
			createLimitedList(partyAlliesComponent, MAX_ALLY_COUNT, playerParty.getAllyPartiesStream().iterator(), ally -> {
				IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> allyParty = partyManager.getPartyById(ally.getPartyId());
				if(allyParty != null) {
					if(!partyAlliesComponent.getSiblings().isEmpty())
						partyAlliesComponent.getSiblings().add(Component.literal(", "));
					IPlayerConfig allyOwnerConfig = serverData.getPlayerConfigs().getLoadedConfig(allyParty.getOwner().getUUID());
					String configuredAllyName = allyOwnerConfig.getEffective(PlayerConfigOptions.PARTY_NAME);
					String allyDefaultName = allyParty.getDefaultName();
					String allyTooltipPrefix = !configuredAllyName.isEmpty() ? allyDefaultName + "\n" : "";
					partyAlliesComponent.getSiblings().add(Component.literal(configuredAllyName.isEmpty() ? allyDefaultName : configuredAllyName).withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(allyTooltipPrefix + allyParty.getId().toString())))));
				}
			});
			if(partyAlliesComponent.getSiblings().isEmpty())
				casterPlayer.sendSystemMessage(Component.literal("N/A").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
			else
				casterPlayer.sendSystemMessage(partyAlliesComponent);
			
			casterPlayer.sendSystemMessage(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_party_invited", playerParty.getInviteCount() + "/" + ServerConfig.CONFIG.maxPartyInvites.get()).withStyle(s -> s.withColor(ChatFormatting.GOLD)));
			Component invitedComponent = Component.literal("");

			createLimitedList(invitedComponent, MAX_INVITES_COUNT, playerParty.getInvitedPlayersStream().iterator(), pi -> {
				if(!invitedComponent.getSiblings().isEmpty())
					invitedComponent.getSiblings().add(Component.literal(", "));
				invitedComponent.getSiblings().add(Component.literal(pi.getUsername()).withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(pi.getUUID().toString())))));
			});
			if(invitedComponent.getSiblings().isEmpty())
				casterPlayer.sendSystemMessage(Component.literal("N/A").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
			else
				casterPlayer.sendSystemMessage(invitedComponent);
			
			casterPlayer.sendSystemMessage(Component.literal("=====").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
			
			return 1;
		};
		SuggestionProvider<CommandSourceStack> suggestions = (context, builder) -> {
			PlayerList playerlist = context.getSource().getServer().getPlayerList();
			return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().map(targetPlayer -> {
				return targetPlayer.getGameProfile().getName();
			}), builder);
		};
		LiteralArgumentBuilder<CommandSourceStack> normalCommand = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get())
				.then(Commands.literal("about")
				.executes(action));
		dispatcher.register(normalCommand);
		
		LiteralArgumentBuilder<CommandSourceStack> targetCommand = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get())
				.then(Commands.literal("about").then(Commands.argument("player", EntityArgument.player())
				.requires(c -> !c.hasPermission(2))
				.suggests(suggestions)
				.executes(action)));
		dispatcher.register(targetCommand);
		
		LiteralArgumentBuilder<CommandSourceStack> opTargetCommand = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get())
				.then(Commands.literal("about").then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.requires(c -> c.hasPermission(2))
				.suggests(suggestions)
				.executes(action)));
		dispatcher.register(opTargetCommand);
	}

}
