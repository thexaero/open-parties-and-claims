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

import xaero.pac.common.claims.player.PlayerChunkClaim;

public class ServerPlayerChunkClaimHolder {
	
	private final PlayerChunkClaim claim;
	private final byte[] counts;
	private byte minX;
	private byte maxX;
	private short count;
	
	public ServerPlayerChunkClaimHolder(PlayerChunkClaim claim, byte[] counts) {
		super();
		if(counts.length != 32)
			throw new IllegalArgumentException();
		this.claim = claim;
		this.counts = counts;
		minX = 32;
		maxX = -1;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getCountForX(int x) {
		return counts[x];
	}
	
	public void increment(int x) {
		count++;
		if(counts[x] == 0) {
			if(x > maxX)
				maxX = (byte)x;
			if(x < minX)
				minX = (byte)x;
		}
		counts[x]++;
	}
	
	public void decrement(int x) {
		count--;
		if(counts[x] == 1) {
			if(x == maxX) {
				maxX = -1;
				if(x > 0)
					for(int i = x - 1; i >= 0; i--)
						if(counts[i] != 0) {
							maxX = (byte)i;
							break;
						}
			}
			if(x == minX) {
				minX = 32;
				if(x < 31)
					for(int i = x + 1; i < 32; i++)
						if(counts[i] != 0) {
							minX = (byte)i;
							break;
						}
			}
		}
		counts[x]--;
	}
	
	public byte getMinX() {
		return minX;
	}
	
	public byte getMaxX() {
		return maxX;
	}
	
	public PlayerChunkClaim getClaim() {
		return claim;
	}

}
