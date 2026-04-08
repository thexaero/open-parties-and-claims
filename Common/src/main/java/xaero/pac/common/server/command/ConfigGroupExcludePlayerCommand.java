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

package xaero.pac.common.server.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class ConfigGroupExcludePlayerCommand extends ConfigGroupCommand {

	protected ConfigGroupExcludePlayerCommand() {
		super("exclude-player", "name-or-uuid", true, true);
	}

	@Override
	protected Either<Component, PlayerConfigGroupActionError> executeCommand(
			CommandContext<CommandSourceStack> context,
			IPlayerConfig playerConfig,
			String inputGroupId,
			String playerIdentifier
	) {
		ICustomPlayerConfigGroup customPlayerConfigGroup = playerConfig.getPlayerGroups().getCustom(inputGroupId);
		if(customPlayerConfigGroup == null)
			return Either.right(PlayerConfigGroupActionError.GROUP_TO_EDIT_NOT_FOUND);
		Optional<PlayerConfigGroupActionError> error = null;
		if (playerIdentifier.length() == 36) {
			try {
				UUID playerId = UUID.fromString(playerIdentifier);
				error = customPlayerConfigGroup.excludeMember(playerId, null);
			} catch (IllegalArgumentException iae) {
			}
		}
		if(error == null)
			error = customPlayerConfigGroup.excludeMember(null, playerIdentifier);//use as name
		return error.<Either<Component, PlayerConfigGroupActionError>>map(Either::right)
				.orElseGet(() ->
						Either.left(Component.translatable("gui.xaero_pac_config_exclude_player", playerIdentifier, inputGroupId))
				);
	}

	@Override
	protected Stream<String> getSecondaryArgumentSuggestionBaseStream(
			ICustomPlayerConfigGroup targetGroup,
			IServerPlayerConfigGroupManager groupManager,
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
							>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
					> serverData
	) {
		return targetGroup.getDirectMembersInternal().stream().map(member ->
				member.getDisplayName() == null ? member.getId().toString() : member.getDisplayName()
		);
	}

}
