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

package xaero.pac.common.server.parties.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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
import xaero.pac.common.server.command.CommandRequirementHelper;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import java.util.Collection;
import java.util.function.Predicate;

public class ImpersonatePartyCommand {
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Command<CommandSourceStack> action = context -> {
			GameProfile targetProfile = null;
			ServerPlayer casterPlayer = context.getSource().getPlayerOrException();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(context.getSource().getServer());
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			try {
				Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "profile");
				if(profiles.size() == 1)
					targetProfile = profiles.iterator().next();
				else if(profiles.isEmpty()) {
					context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_impersonate_invalid_player"));
					return 0;
				} else {
					context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_impersonate_too_many_targets"));
					return 0;
				}
			} catch(IllegalArgumentException iae) {
			}
			ServerPlayerData casterPlayerData = (ServerPlayerData) ServerPlayerData.from(casterPlayer);
			boolean disabling = targetProfile == null || targetProfile.getId().equals(casterPlayerData.getPartiesImpersonatedPlayerId());
			if(!disabling && !casterPlayer.hasPermissions(Commands.LEVEL_GAMEMASTERS)){
				//this check is important for players who are already impersonating someone but have lost the permission
				IPlayerPermissionSystemAPI usedPermissionSystem = serverData.getPlayerPermissionSystemManager().getUsedSystem();
				if (usedPermissionSystem == null || !usedPermissionSystem.getPermission(casterPlayer, UsedPermissionNodes.PARTIES_IMPERSONATION))
					disabling = true;
			}
			casterPlayerData.setPartiesImpersonatedPlayerProfile(disabling ? null : targetProfile);
			if(disabling)
				context.getSource().sendSuccess(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_impersonate_disabled"), true);
			else {
				Component targetName = new TextComponent(targetProfile.getName()).withStyle(ChatFormatting.GREEN);
				context.getSource().sendSuccess(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_parties_impersonate_enabled", targetName), true);
			}
			serverData.getServer().getCommands().sendCommands(casterPlayer);
			return 1;
		};
		SuggestionProvider<CommandSourceStack> suggestions = (context, builder) -> {
			PlayerList playerlist = context.getSource().getServer().getPlayerList();
			return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream()
					.map(targetPlayer -> targetPlayer.getGameProfile().getName()), builder);
		};
		Predicate<CommandSourceStack> requirement = CommandRequirementHelper.onServerThread(context -> {
			if(context.hasPermission(Commands.LEVEL_GAMEMASTERS) )
				return true;
			try {
				ServerPlayer player = context.getPlayerOrException();
				MinecraftServer server = player.getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(server);
				if(((ServerPlayerData)ServerPlayerDataAPI.from(player)).getPartiesImpersonatedPlayerProfile() != null)//lets you turn it off
					return true;
				IPlayerPermissionSystemAPI usedPermissionSystem = serverData.getPlayerPermissionSystemManager().getUsedSystem();
				if(usedPermissionSystem == null)
					return false;
				return usedPermissionSystem.getPermission(player, UsedPermissionNodes.PARTIES_IMPERSONATION);
			} catch (CommandSyntaxException e) {
				return false;
			}
		});
		LiteralArgumentBuilder<CommandSourceStack> opTargetCommand = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get())
				.then(Commands.literal("impersonate").then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.requires(requirement)
				.suggests(suggestions)
				.executes(action)));
		dispatcher.register(opTargetCommand);
		LiteralArgumentBuilder<CommandSourceStack> disableCommand = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get())
				.then(Commands.literal("impersonate")
				.requires(requirement)
				.executes(action));
		dispatcher.register(disableCommand);
	}

}
