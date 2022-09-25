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

package xaero.pac.common.server.claims.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;

import java.util.Collection;

public class ClaimsAboutCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
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
				/*try {
					ServerPlayer inputPlayer = EntityArgument.getPlayer(context, "player");
					if(inputPlayer != null)
						targetProfile = inputPlayer.getGameProfile();
					else
						targetProfile = null;
				} catch(IllegalArgumentException iae2) {*/
					targetProfile = casterPlayer.getGameProfile();
				//}
			}
			if(targetProfile == null) {
				context.getSource().sendFailure(Component.translatable("gui.xaero_claims_about_invalid_player"));
				return 0;
			}
			final GameProfile profile = targetProfile;
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>>
				serverData = ServerData.from(casterPlayer.getServer());
			IPlayerConfigManager
					configManager = serverData.getPlayerConfigs();
			IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
				claimsManager = serverData.getServerClaimsManager();
			IPlayerConfig 
				playerConfig = configManager.getLoadedConfig(profile.getId());
			IPlayerConfig usedSubConfig = playerConfig.getUsedSubConfig();
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>
				playerInfo = claimsManager.getPlayerInfo(profile.getId());

			int claimLimit = claimsManager.getPlayerBaseClaimLimit(profile.getId()) + playerConfig.getEffective(PlayerConfigOptions.BONUS_CHUNK_CLAIMS);
			int forceloadLimit = claimsManager.getPlayerBaseForceloadLimit(profile.getId()) + playerConfig.getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);
			Component claimCountNumbers = Component.literal(playerInfo.getClaimCount() + " / " + claimLimit).withStyle(s -> s.withColor(0xFFAAAAAA));
			String claimName = usedSubConfig.getEffective(PlayerConfigOptions.CLAIMS_NAME);
			if(claimName.isEmpty())
				claimName = "N/A";
			String subId = usedSubConfig.getSubId();
			if(subId == null)
				subId = PlayerConfig.MAIN_SUB_ID;
			claimName += " (" + subId + ")";
			Component claimNameComponent = Component.literal(claimName).withStyle(s -> s.withColor(0xFFAAAAAA));
			Component forceloadCountNumbers = Component.literal(playerInfo.getForceloadCount() + " / " + forceloadLimit).withStyle(s -> s.withColor(0xFFAAAAAA));
			casterPlayer.sendSystemMessage(Component.literal(""));
			casterPlayer.sendSystemMessage(Component.literal("===== Open Parties and Claims").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
			casterPlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_ui_claim_count", claimCountNumbers));
			casterPlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_ui_forceload_count", forceloadCountNumbers));
			casterPlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_ui_claims_name", claimNameComponent));
			int claimColor = usedSubConfig.getEffective(PlayerConfigOptions.CLAIMS_COLOR);
			Component colorComponent = Component.literal(Integer.toUnsignedString(claimColor, 16).toUpperCase()).withStyle(s -> s.withColor(claimColor));
			casterPlayer.sendSystemMessage(Component.translatable("gui.xaero_pac_ui_claims_color", colorComponent));
			casterPlayer.sendSystemMessage(Component.literal("=====").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
			return 1;
		};
		

		SuggestionProvider<CommandSourceStack> suggestions = (context, builder) -> {
			PlayerList playerlist = context.getSource().getServer().getPlayerList();
			return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().map(targetPlayer -> {
				return targetPlayer.getGameProfile().getName();
			}), builder);
		};
		LiteralArgumentBuilder<CommandSourceStack> normalCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(context -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("about")
				.executes(action));
		dispatcher.register(normalCommand);
		
		/*LiteralArgumentBuilder<CommandSourceStack> targetCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal("about").then(Commands.argument("player", EntityArgument.player())
				.requires(c -> !c.hasPermission(2))
				.suggests(suggestions)
				.executes(action)));
		dispatcher.register(targetCommand);*/
		
		LiteralArgumentBuilder<CommandSourceStack> opTargetCommand = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("about").then(Commands.argument("profile", GameProfileArgument.gameProfile())
				.requires(c -> c.hasPermission(2))
				.suggests(suggestions)
				.executes(action)));
		dispatcher.register(opTargetCommand);
	}

}
