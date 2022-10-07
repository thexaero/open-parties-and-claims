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

package xaero.pac.common.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
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
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ConfigCommandUtil {

	static IPlayerConfig getEffectiveConfig(CommandContext<CommandSourceStack> context, IPlayerConfig playerConfig){
		IPlayerConfig effectivePlayerConfig = playerConfig;
		try {
			String subConfigId = StringArgumentType.getString(context, "sub-id");
			effectivePlayerConfig = playerConfig.getSubConfig(subConfigId);
		} catch(IllegalArgumentException e){
		}
		return effectivePlayerConfig;
	}

	public static GameProfile getConfigInputPlayer(CommandContext<CommandSourceStack> context, ServerPlayer sourcePlayer, String tooManyTargetMessage, String invalidTargetMessage) throws CommandSyntaxException {
		GameProfile inputPlayer;
		try {
			Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "player");
			if(profiles.size() > 1) {
				if(tooManyTargetMessage != null)
					context.getSource().sendFailure(Component.translatable(tooManyTargetMessage));
				return null;
			} else if(profiles.isEmpty()) {
				if(invalidTargetMessage != null)
					context.getSource().sendFailure(Component.translatable(invalidTargetMessage));
				return null;
			}
			inputPlayer = profiles.iterator().next();
		} catch(IllegalArgumentException e) {
			inputPlayer = sourcePlayer.getGameProfile();
		}
		return inputPlayer;
	}

	public static SuggestionProvider<CommandSourceStack> getSubConfigSuggestionProvider(PlayerConfigType type){
		return (context, builder) -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			UUID configOwnerId;
			if(type != PlayerConfigType.SERVER) {
				GameProfile gameProfile = getConfigInputPlayer(context, sourcePlayer, null, null);
				if (gameProfile == null)
					return SharedSuggestionProvider.suggest(Stream.empty(), builder);
				configOwnerId = gameProfile.getId();
			} else
				configOwnerId = PlayerConfig.SERVER_CLAIM_UUID;
			String lowerCaseInput = builder.getRemainingLowerCase();
			MinecraftServer server = sourcePlayer.getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			IPlayerConfig playerConfig = serverData.getPlayerConfigs().getLoadedConfig(configOwnerId);
			List<String> subConfigIds = playerConfig.getSubConfigIds();
			Stream<String> baseStream = subConfigIds.stream();
			if(!lowerCaseInput.isEmpty())
				baseStream = baseStream.filter(s -> s.toLowerCase().startsWith(lowerCaseInput));
			return SharedSuggestionProvider.suggest(baseStream.limit(64), builder);
		};
	}

}
