/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.system.impl;

import net.minecraft.network.chat.Component;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class DefaultPlayerPartySystem implements IPlayerPartySystemAPI<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> {

	private final IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager;

	public DefaultPlayerPartySystem(IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager) {
		this.partyManager = partyManager;
	}

	@Nullable
	@Override
	public IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> getPartyByOwner(@Nonnull UUID playerId) {
		return partyManager.getPartyByOwner(playerId);
	}

	@Nullable
	@Override
	public IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> getPartyByMember(@Nonnull UUID playerId) {
		return partyManager.getPartyByMember(playerId);
	}

	@Override
	public boolean isPlayerAllying(@Nonnull UUID playerId, @Nonnull UUID potentialAllyPlayerId) {
		IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = getPartyByMember(playerId);
		if(playerParty == null)
			return false;
		IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> potentialAllyPlayerParty = getPartyByMember(potentialAllyPlayerId);
		if(potentialAllyPlayerParty == null)
			return false;
		return playerParty.isAlly(potentialAllyPlayerParty.getId());
	}

	@Override
	public boolean isPermittedToPartyClaim(@Nonnull UUID playerId) {
		return isAtLeast(playerId, PartyMemberRank.CLAIMER);
	}

	@Override
	public boolean canEditPartyConfig(@Nonnull UUID playerId) {
		return isAtLeast(playerId, PartyMemberRank.ADMIN);
	}

	@Override
	public boolean canCreatePartyConfigGroups(@Nonnull UUID playerId) {
		return isAtLeast(playerId, PartyMemberRank.ADMIN);
	}

	@Override
	public boolean canIncludeGroupsInPartyConfigGroups(@Nonnull UUID playerId) {
		return isAtLeast(playerId, PartyMemberRank.ADMIN);
	}

	@Override
	public boolean canIncludePlayersInPartyConfigGroups(@Nonnull UUID playerId) {
		return isAtLeast(playerId, PartyMemberRank.MODERATOR);
	}

	private boolean isAtLeast(UUID playerId, PartyMemberRank rank){
		IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> party = getPartyByMember(playerId);
		if(party == null)
			return false;
		IPartyMember member = party.getMemberInfo(playerId);
		return member != null && member.getRank().ordinal() >= rank.ordinal();
	}

	@Nullable
	@Override
	public UUID getOwner(@Nonnull IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> party) {
		return party.getOwner().getUUID();
	}

	@Nullable
	@Override
	public Component getName(@Nonnull IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> party) {
		IPlayerConfig ownerConfig = partyManager.getPlayerConfigs().getLoadedConfig(party.getOwner().getUUID());
		String partyCustomName = ownerConfig.getEffective(PlayerConfigOptions.PARTY_NAME);
		if(partyCustomName.isEmpty())
			return null;
		return Component.literal(partyCustomName);
	}

	@Override
	public int getMemberCount(@Nonnull IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> party) {
		return party.getMemberCount();
	}

}
