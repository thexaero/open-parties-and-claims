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

package xaero.pac.common.parties.party;

import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PartyPlayerInfo<PI extends PartyPlayerInfo<PI>> implements IPartyPlayerInfo, ILinkedChainNode<PI> {
	
	private final UUID UUID;
	private String username;//needs to be updated when a member changes their name
	private PI next;
	private PI previous;
	private boolean destroyed;

	public PartyPlayerInfo(UUID playerUUID) {
		super();
		this.UUID = playerUUID;
	}

	@Nonnull
	@Override
	public UUID getUUID() {
		return UUID;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	@Nonnull
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public int hashCode() {
		return UUID.hashCode();
	}

	@Override
	public void setNext(PI element) {
		next = element;
	}

	@Override
	public void setPrevious(PI element) {
		previous = element;
	}

	@Override
	public PI getNext() {
		return next;
	}

	@Override
	public PI getPrevious() {
		return previous;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void onDestroyed() {
		destroyed = true;
	}
}
