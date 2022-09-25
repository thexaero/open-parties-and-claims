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

package xaero.pac.common.server.claims;

import xaero.pac.common.claims.ClaimStateHolder;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.util.linked.ILinkedChainNode;

public final class ServerClaimStateHolder extends ClaimStateHolder implements ILinkedChainNode<ServerClaimStateHolder> {

	private long regionCount;
	private ServerClaimStateHolder previous;
	private ServerClaimStateHolder next;
	private boolean destroyed;

	public ServerClaimStateHolder(PlayerChunkClaim state) {
		super(state);
	}

	public long getRegionCount() {
		return regionCount;
	}

	public void countRegions(int direction){
		regionCount += direction;
	}

	@Override
	public void setNext(ServerClaimStateHolder element) {
		this.next = element;
	}

	@Override
	public void setPrevious(ServerClaimStateHolder element) {
		this.previous = element;
	}

	@Override
	public ServerClaimStateHolder getNext() {
		return next;
	}

	@Override
	public ServerClaimStateHolder getPrevious() {
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
