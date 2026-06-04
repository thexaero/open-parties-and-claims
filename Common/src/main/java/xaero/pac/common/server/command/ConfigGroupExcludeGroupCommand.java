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
import net.minecraft.network.chat.TranslatableComponent;
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
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class ConfigGroupExcludeGroupCommand extends ConfigGroupCommand {

	protected ConfigGroupExcludeGroupCommand() {
		super("exclude-group", "group-id-to-exclude", true, true);
	}

	@Override
	protected Either<Component, PlayerConfigGroupActionError> executeCommand(
			CommandContext<CommandSourceStack> context,
			IPlayerConfig playerConfig,
			String inputGroupId,
			String groupIdToExclude
	) {
		ICustomPlayerConfigGroup customPlayerConfigGroup = playerConfig.getPlayerGroups().getCustom(inputGroupId);
		if(customPlayerConfigGroup == null)
			return Either.right(PlayerConfigGroupActionError.GROUP_TO_EDIT_NOT_FOUND);
		Optional<PlayerConfigGroupActionError> error = customPlayerConfigGroup.excludeGroup(groupIdToExclude);
		return error.<Either<Component, PlayerConfigGroupActionError>>map(Either::right)
				.orElseGet(() ->
						Either.left(new TranslatableComponent("gui.xaero_pac_config_exclude_group", groupIdToExclude, inputGroupId))
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
		return targetGroup.getDirectGroupIds().stream();
	}

	@Override
	protected boolean canAffectPartyConfig(IPlayerPartySystemManager systemManager, UUID playerId) {
		return systemManager.canIncludeGroupsInPartyConfigGroups(playerId);
	}

}
