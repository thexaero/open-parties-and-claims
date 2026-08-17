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
import xaero.pac.common.claims.ClaimLocation;
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
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.player.util.ServerPlayerUtils;
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.Collection;
import java.util.function.Predicate;

public class ClaimsTeleportCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Predicate<CommandSourceStack> requirement = CommandRequirementHelper.onServerThread(context -> {
			if(Commands.LEVEL_GAMEMASTERS.check(context.permissions()))
				return true;
			try {
				ServerPlayer player = context.getPlayerOrException();
				MinecraftServer server = ServerLevelHelper.getServer(player);
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(server);
				return serverData.getServerClaimsManager().getPermissionHandler().playerHasTeleportPermission(player);
			} catch (CommandSyntaxException e) {
				return false;
			}
		});
		Command<CommandSourceStack> action = context -> {
			NameAndId targetProfile = null;
			ServerPlayer casterPlayer = context.getSource().getPlayerOrException();
			try {
				Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "profile");
				if(profiles.size() == 1)
					targetProfile = profiles.iterator().next();
			} catch(IllegalArgumentException iae) {
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(ServerLevelHelper.getServer(casterPlayer));
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			if(!casterPlayer.isCreative() && !casterPlayer.isInvulnerable()) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_teleport_vulnerable"));
				return 0;
			}
			if(targetProfile == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_teleport_invalid_player"));
				return 0;
			}
			final NameAndId profile = targetProfile;
			IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
				claimsManager = serverData.getServerClaimsManager();
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo =
					claimsManager.getPlayerInfo(profile.id());
			ClaimLocation randomClaimPos = playerInfo.getRandomClaimPos(true);
			if(randomClaimPos == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(casterPlayer, "gui.xaero_claims_teleport_no_claims"));
				return 0;
			}
			ServerPlayerUtils.teleport(casterPlayer, randomClaimPos.getDimId(), (randomClaimPos.getChunkX() << 4) + 8, casterPlayer.getY(), (randomClaimPos.getChunkZ() << 4) + 8, casterPlayer.getYRot(), casterPlayer.getXRot());
			Component targetName = Component.literal(profile.name()).withStyle(ChatFormatting.GREEN);
			casterPlayer.sendSystemMessage(Component.translatable("gui.xaero_claims_teleport_success", targetName));
			return 1;
		};
		SuggestionProvider<CommandSourceStack> suggestions = (context, builder) -> {
			PlayerList playerlist = context.getSource().getServer().getPlayerList();
			return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream()
					.map(targetPlayer -> targetPlayer.nameAndId().name()), builder);
		};
		LiteralArgumentBuilder<CommandSourceStack> opTargetCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("teleport")
				.requires(requirement)
				.then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.suggests(suggestions)
				.executes(action)));
		dispatcher.register(opTargetCommand);
	}

}
