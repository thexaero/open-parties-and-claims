/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.player.config.group.custom;

import xaero.pac.common.util.linked.ILinkedChainNode;

public class CustomPlayerGroupIncludedGroup implements ILinkedChainNode<CustomPlayerGroupIncludedGroup> {

	private final String id;
	private CustomPlayerGroupIncludedGroup next;
	private CustomPlayerGroupIncludedGroup previous;
	private boolean destroyed;

	public CustomPlayerGroupIncludedGroup(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "IncludedGroup(" + id + ")";
	}

	@Override
	public void setNext(CustomPlayerGroupIncludedGroup element) {
		this.next = element;
	}

	@Override
	public void setPrevious(CustomPlayerGroupIncludedGroup element) {
		this.previous = element;
	}

	@Override
	public CustomPlayerGroupIncludedGroup getNext() {
		return this.next;
	}

	@Override
	public CustomPlayerGroupIncludedGroup getPrevious() {
		return this.previous;
	}

	@Override
	public boolean isDestroyed() {
		return this.destroyed;
	}

	@Override
	public void onDestroyed() {
		this.destroyed = true;
	}

}
