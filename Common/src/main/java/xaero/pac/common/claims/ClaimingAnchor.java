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

package xaero.pac.common.claims;

import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.util.linked.ILinkedChainNode;

import java.util.Objects;

public class ClaimingAnchor implements ILinkedChainNode<ClaimingAnchor> {

	private final ChunkPos pos;
	private ClaimingAnchor next;
	private ClaimingAnchor previous;
	private boolean destroyed;

	public ClaimingAnchor(ChunkPos pos) {
		this.pos = pos;
	}

	public ChunkPos getPos() {
		return pos;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ClaimingAnchor that = (ClaimingAnchor) o;
		return Objects.equals(pos, that.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(pos);
	}

	@Override
	public void setNext(ClaimingAnchor element) {
		this.next = element;
	}

	@Override
	public void setPrevious(ClaimingAnchor element) {
		this.previous = element;
	}

	@Override
	public ClaimingAnchor getNext() {
		return next;
	}

	@Override
	public ClaimingAnchor getPrevious() {
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
