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

package xaero.pac.common.server.parties.system;

import net.minecraft.network.chat.Component;
import xaero.pac.common.server.parties.system.api.IPlayerPartySystemRegisterAPI;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;

import java.util.UUID;

public interface IPlayerPartySystemManager extends IPlayerPartySystemRegisterAPI {

	//internal api

	void preRegister();
	void postRegister();
	void updatePrimarySystem(String configuredPrimarySystem);
	IPlayerPartySystemAPI<?> getPrimarySystem();
	Iterable<IPlayerPartySystemAPI<?>> getRegisteredSystems();
	boolean isInAParty(UUID playerId);
	boolean isInAPrimaryParty(UUID playerId);
	boolean areInSameParty(UUID playerId, UUID otherPlayerId);
	boolean isPlayerAllying(UUID playerId, UUID potentialAllyPlayerId);
	UUID getPrimaryPartyOwnerByMember(UUID playerId);
	Component getPrimaryPartyNameByOwner(UUID ownerId);
	boolean canEditPartyConfig(UUID playerId);
	boolean canCreatePartyConfigGroups(UUID playerId);
	boolean canIncludeGroupsInPartyConfigGroups(UUID playerId);
	boolean canIncludePlayersInPartyConfigGroups(UUID playerId);
	boolean canPartyClaim(UUID uuid);
	boolean isPrimaryPartyOwner(UUID playerId);
	int getPrimaryMemberCount(UUID ownerId);
	int getPrimaryPartyColorByOwner(UUID playerId);

}
