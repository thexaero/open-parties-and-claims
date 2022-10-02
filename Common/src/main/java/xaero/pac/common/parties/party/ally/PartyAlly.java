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

package xaero.pac.common.parties.party.ally;

import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PartyAlly implements IPartyAlly, ILinkedChainNode<PartyAlly> {

	private final UUID partyId;
	private PartyAlly previous;
	private PartyAlly next;
	private boolean destroyed;

	public PartyAlly(UUID partyId) {
		this.partyId = partyId;
	}

	@Override
	@Nonnull
	public UUID getPartyId() {
		return partyId;
	}

	@Override
	public void setNext(PartyAlly element) {
		this.next = element;
	}

	@Override
	public void setPrevious(PartyAlly element) {
		this.previous = element;
	}

	@Override
	public PartyAlly getNext() {
		return next;
	}

	@Override
	public PartyAlly getPrevious() {
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
