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

package xaero.pac.common.claims.player;

import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class PlayerChunkClaim implements IPlayerChunkClaim {
	
	private final UUID playerId;
	private final boolean forceloadable;
	private final int syncIndex;
	
	public PlayerChunkClaim(UUID playerId, boolean forceloadable, int syncIndex) {
		super();
		this.playerId = playerId;
		this.forceloadable = forceloadable;
		this.syncIndex = syncIndex;
	}
	
	public boolean isForceloadable() {
		return forceloadable;
	}
	
	public static long getLongCoordinatesFor(int x, int z) {
		return ((long) x << 32) | (z & 0xFFFFFFFFL);
	}
	
	public static int getXFromLongCoordinates(long key) {
		return (int) (key >> 32);
	}
	
	public static int getZFromLongCoordinates(long key) {
		return (int) (key & 0xFFFFFFFF);
	}

	@Nonnull
	public UUID getPlayerId() {
		return playerId;
	}
	
	public int getSyncIndex() {
		return syncIndex;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(playerId, forceloadable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj == null || !(obj instanceof PlayerChunkClaim))
			return false;
		PlayerChunkClaim other = (PlayerChunkClaim) obj;
		return playerId.equals(other.playerId) && forceloadable == ((PlayerChunkClaim) obj).forceloadable;
	}

	@Override
	public boolean isSameClaimType(IPlayerChunkClaimAPI other) {//ignores forceloadable differences
		if(other == this)
			return true;
		if(other == null)
			return false;
		return playerId.equals(other.getPlayerId());
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s, %d]", playerId, forceloadable, syncIndex);
	}

}
