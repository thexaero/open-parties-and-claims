/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
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
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.stream.Stream;

public class PartyCommands {

	private static SuggestionProvider<CommandSourceStack> getPartyPlayerSuggestor(boolean members, boolean invites){
		return (context, builder) -> {
			ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(context.getSource().getServer());
			IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
			IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(commandPlayer.getUUID());
			String lowercaseInput = builder.getRemainingLowerCase();
			Stream<IPartyPlayerInfo> stream;
			int maxIterationSize = (members ? playerParty.getMemberCount() : 0) + (invites ? playerParty.getInviteCount() : 0);
			if(maxIterationSize > 1024) {
				IPartyMember exactMember = members ? playerParty.getMemberInfo(lowercaseInput) : null;
				IPartyPlayerInfo exactInvite = invites ? playerParty.getInvite(lowercaseInput) : null;
				return SharedSuggestionProvider.suggest(Stream.concat(Stream.ofNullable(exactMember), Stream.ofNullable(exactInvite)).map(IPartyPlayerInfo::getUsername), builder);
			}
			//probably not a good idea to let players spam something like this somewhat easily, so it's limited at 1024
			stream = Stream.concat(members ? playerParty.getMemberInfoStream() : Stream.empty(), invites ? playerParty.getInvitedPlayersStream() : Stream.empty());
			return SharedSuggestionProvider.suggest(stream
					.map(IPartyPlayerInfo::getUsername)
					.filter(name -> name.toLowerCase().startsWith(lowercaseInput))
					.limit(16), builder);//limited at 16 to reduce synced data for super large parties
		};
	}

	public static SuggestionProvider<CommandSourceStack> getPartyMemberSuggestor(){
		return getPartyPlayerSuggestor(true, false);
	}

	public static SuggestionProvider<CommandSourceStack> getPartyInviteSuggestor(){
		return getPartyPlayerSuggestor(false, true);
	}

	public static SuggestionProvider<CommandSourceStack> getPartyMemberOrInviteSuggestor(){
		return getPartyPlayerSuggestor(true, true);
	}

}
