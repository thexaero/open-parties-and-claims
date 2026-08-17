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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
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
import xaero.pac.common.server.claims.ServerClaimsPermissionHandler;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.task.PlayerClaimClearSpreadoutTask;
import xaero.pac.common.server.command.CommandRequirementHelper;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class ClaimsClearCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Predicate<CommandSourceStack> targetRequirement = CommandRequirementHelper.onServerThread(context -> {
			if(Commands.LEVEL_GAMEMASTERS.check(context.permissions()))
				return true;
			try {
				ServerPlayer player = context.getPlayerOrException();
				MinecraftServer server = ServerLevelHelper.getServer(player);
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(server);
				return serverData.getServerClaimsManager().getPermissionHandler().playerHasAdminModePermission(player);
			} catch (CommandSyntaxException e) {
				return false;
			}
		});
		SuggestionProvider<CommandSourceStack> targetSuggestions = (context, builder) -> {
			PlayerList playerlist = context.getSource().getServer().getPlayerList();
			return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream()
					.map(targetPlayer -> targetPlayer.nameAndId().name()), builder);
		};

		LiteralArgumentBuilder<CommandSourceStack> selfNoConfirmCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("clear")
				.executes(getExecutor(false, true)));
		dispatcher.register(selfNoConfirmCommand);
		LiteralArgumentBuilder<CommandSourceStack> selfCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("clear")
				.then(Commands.literal("confirm")
				.executes(getExecutor(true, true))));
		dispatcher.register(selfCommand);

		LiteralArgumentBuilder<CommandSourceStack> targetNoConfirmCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("clear")
				.then(Commands.literal("for")
				.requires(targetRequirement)
				.then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.suggests(targetSuggestions)
				.executes(getExecutor(false, false)))));
		dispatcher.register(targetNoConfirmCommand);
		LiteralArgumentBuilder<CommandSourceStack> targetCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("clear")
				.then(Commands.literal("for")
				.requires(targetRequirement)
				.then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.suggests(targetSuggestions)
				.then(Commands.literal("confirm")
				.executes(getExecutor(true, false))))));
		dispatcher.register(targetCommand);
	}

	private Command<CommandSourceStack> getExecutor(boolean confirmed, boolean self){
		return context -> {
			ServerPlayer casterPlayer = null;
			try {
				casterPlayer = context.getSource().getPlayerOrException();
			} catch(CommandSyntaxException cse){
			}
			ServerPlayerData playerData = casterPlayer == null ? null : (ServerPlayerData) ServerPlayerData.from(casterPlayer);
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(context.getSource().getServer());
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			NameAndId targetProfile = null;
			NameAndId casterPlayerProfile = casterPlayer == null ? PlayerConfig.SERVER_CLAIM_PROFILE : casterPlayer.nameAndId();
			if(self) {
				ServerClaimsPermissionHandler permissionHandler = serverData.getServerClaimsManager().getPermissionHandler();
				if(!confirmed && casterPlayer != null)//don't want to switch off impersonation when using the confirm command
					permissionHandler.ensureImpersonationPermission(casterPlayer, playerData);
				UUID impersonatedId = casterPlayer == null ? null : playerData.getClaimsImpersonationInfo().getPlayerId();
				if(impersonatedId != null) {
					if(confirmed && !permissionHandler.playerHasImpersonationPermission(casterPlayer)){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_no_impersonation_permission"));
						return 0;
					}
					targetProfile = ServerLevelHelper.getServer(casterPlayer).services().nameToIdCache().get(impersonatedId).orElse(null);
					if(targetProfile == null){
						context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_clear_invalid_impersonated_player"));
						return 0;
					}
				} else
					targetProfile = casterPlayerProfile;
			} else try {
				Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "profile");
				if(profiles.size() == 1)
					targetProfile = profiles.iterator().next();
			} catch(IllegalArgumentException iae) {
			}
			if(targetProfile == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_clear_invalid_player"));
				return 0;
			}
			boolean effectivelySelf = targetProfile.equals(casterPlayerProfile);
			if(!effectivelySelf && casterPlayer != null && !playerData.isClaimsAdminMode()) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_clear_not_admin_mode", targetProfile.name()));
				return 0;
			}
			if(!confirmed){
				String primaryPartySystem = serverData.getPlayerPartySystemManager().getPrimarySystemName();
				Component primaryPartyName = !ServerConfig.CONFIG.partyOwnedClaims.get() ? null :
						serverData.getPlayerPartySystemManager().getPrimaryPartyNameByOwner(targetProfile.id());
				if(primaryPartyName == null)
					primaryPartyName = Component.translatable(effectivelySelf ?
							"gui.xaero_claims_clear_needs_confirmation_self_no_party_owned" :
							"gui.xaero_claims_clear_needs_confirmation_other_no_party_owned"
					);
				Component message = Component.translatable(
						effectivelySelf ? "gui.xaero_claims_clear_needs_confirmation_self" :
								"gui.xaero_claims_clear_needs_confirmation_other",
						targetProfile.name(), primaryPartySystem, primaryPartyName
				);
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, message));
				return 0;
			}
			IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
					claimsManager = serverData.getServerClaimsManager();
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo =
					claimsManager.getPlayerInfo(targetProfile.id());
			if(playerInfo.getClaimCount() == 0){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(
						casterPlayer, effectivelySelf ?
								"gui.xaero_claims_clear_no_claims_self" :
								"gui.xaero_claims_clear_no_claims"
				));
				return 0;
			}
			Component targetName = Component.literal(targetProfile.name()).withStyle(ChatFormatting.GREEN);
			context.getSource().sendSuccess(() -> Component.translatable("gui.xaero_claims_clear_start", targetName), true);
			playerInfo.addReplacementTask(
					PlayerClaimClearSpreadoutTask.Builder.begin()
							.setCallerUUID(casterPlayerProfile.id())
							.setServer(serverData.getServer())
							.setTargetPlayerProfile(targetProfile)
							.build(),
					serverData
			);
			return 1;
		};
	}

}
