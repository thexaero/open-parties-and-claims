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

package xaero.pac.client.parties.party.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * API for the party storage on the client side
 */
public interface IClientPartyStorageAPI
<
	P extends IClientPartyAPI<?,?>,
		MISS extends IClientPartyMemberDynamicInfoSyncableStorageAPI<?>
> {

	/**
	 * Gets the local client player's party.
	 *
	 * @return the player's party, null if not in one
	 */
	@Nullable
	public P getParty();

	/**
	 * Gets the name of the local client player's party.
	 *
	 * @return the name of the party, null if not in a party
	 */
	@Nullable
	public String getPartyName();

	/**
	 * Gets the party ally info storage.
	 *
	 * @return ally info storage, not null
	 */
	@Nonnull
	public IClientPartyAllyInfoStorageAPI getAllyInfoStorage();

	/**
	 * Gets the party/ally player dynamic info storage.
	 * <p>
	 * The dynamic info includes locations of some party members and allies.
	 *
	 * @return the party/ally player dynamic info storage, not null
	 */
	@Nonnull
	public MISS getPartyMemberDynamicInfoSyncableStorage();

	/**
	 * Checks whether the party data sync is still in progress.
	 * <p>
	 * Party data starts loading in the background when you join the server.
	 *
	 * @return true if party data is loading, otherwise false
	 */
	public boolean isLoading();

	/**
	 * Gets the number of members in the local client player's party, meant for the UI.
	 * <p>
	 * This value is updated before individual member info starts syncing and doesn't necessarily represent the real
	 * number of member info currently loaded. It should mainly be used for displaying the member count on the UI.
	 *
	 * @return the member count for the party meant for the UI
	 */
	public int getUIMemberCount();

	/**
	 * Gets the number of ally parties for the local client player's party, meant for the UI.
	 * <p>
	 * This value is updated before individual ally info starts syncing and doesn't necessarily represent the real
	 * number of ally info currently loaded. It should mainly be used for displaying the ally count on the UI.
	 *
	 * @return the ally count for the party meant for the UI
	 */
	public int getUIAllyCount();

	/**
	 * Gets the number of active invitations for the local client player's party, meant for the UI.
	 * <p>
	 * This value is updated before individual invitation info starts syncing and doesn't necessarily represent the real
	 * number of invites currently loaded. It should mainly be used for displaying the invite count on the UI.
	 *
	 * @return the invite count for the party meant for the UI
	 */
	public int getUIInviteCount();

	/**
	 * Gets the maximum number of members allowed in a party.
	 *
	 * @return the maximum allowed number of members
	 */
	public int getMemberLimit();

	/**
	 * Gets the maximum number of ally parties allowed for a party.
	 *
	 * @return the maximum allowed number of allies
	 */
	public int getAllyLimit();

	/**
	 * Gets the maximum number of active invitations allowed for a party.
	 *
	 * @return the maximum allowed number of invites
	 */
	public int getInviteLimit();

}
