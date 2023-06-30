/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.system.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The interface to be overridden by addons that wish to implement additional party systems to be used
 * by Open Parties and Claims (just the claiming feature as of writing this).
 * <p>
 * Player position synchronization is a part of the default party system. Similar functionality should
 * be implemented in other party systems and supported separately by the map mods that display party
 * members. OPAC does not synchronize or provide party member/ally positions from other party systems,
 * even if they're implemented using this interface.
 * <p>
 * Party system implementations must be registered in {@link IPlayerPartySystemRegisterAPI}.
 *
 * @param <P> the type of parties in the implemented system
 */
public interface IPlayerPartySystemAPI<P> {

	/**
	 * Gets the party that the player with a specified UUID owns.
	 *
	 * @param playerId  the UUID of the player, not null
	 * @return the party that the player owns, null if the player doesn't own one
	 */
	@Nullable
	P getPartyByOwner(@Nonnull UUID playerId);

	/**
	 * Gets the party that the player with a specified UUID is a part of.
	 *
	 * @param playerId  the UUID of the player, not null
	 * @return the party that the player is in, null if the player isn't in one
	 */
	@Nullable
	P getPartyByMember(@Nonnull UUID playerId);

	/**
	 * Checks if a party {@code party} considers another party {@code potentialAllyParty}
	 * an ally.
	 *
	 * @param party  the party to check the allies of, not null
	 * @param potentialAllyParty  the party to check the ally status of, not null
	 * @return true, if {@code party} considers {@code potentialAllyParty} an ally, otherwise false
	 */
	boolean isPartyAllying(@Nonnull P party, @Nonnull P potentialAllyParty);

	/**
	 * Checks if a player is permitted to claim as the party that they belong to but don't own.
	 * <p>
	 * This is not used by Open Parties and Claims as of writing this.
	 *
	 * @param playerId  the UUID of the player, not null
	 * @return true, if the player is permitted to claim as the party, otherwise false
	 */
	boolean isPermittedToPartyClaim(@Nonnull UUID playerId);

}
