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

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerDimensionClaims implements IPlayerDimensionClaims<PlayerClaimPosList> {

	private final UUID playerId;
	private final ResourceLocation dimension;
	private final Map<PlayerChunkClaim, PlayerClaimPosList> claimLists;
	private int count;
	private int forceloadableCount;
	
	public PlayerDimensionClaims(UUID playerId, ResourceLocation dimension, Map<PlayerChunkClaim, PlayerClaimPosList> claimLists) {
		this.playerId = playerId;
		this.dimension = dimension;
		this.claimLists = claimLists;
		this.count = calculateCount();
		this.forceloadableCount = calculateForceloadableCount();
	}
	
	private PlayerClaimPosList getOrCreateList(PlayerChunkClaim claim) {
		PlayerClaimPosList result = claimLists.get(claim);
		if(result == null) {
			result = PlayerClaimPosList.Builder.begin().setClaim(claim).build();
			claimLists.put(claim, result);
		}
		return result;
	}

	private void removeList(PlayerClaimPosList list){
		claimLists.remove(list.getClaimState());
	}
	
	public int getCount(PlayerChunkClaim claim) {
		PlayerClaimPosList list = claimLists.get(claim);
		if(list == null)
			return 0;
		return list.getCount();
	}

	public int getCount() {
		return count;
	}

	public int getForceloadableCount() {
		return forceloadableCount;
	}

	private int calculateCount() {
		int total = 0;
		for (Map.Entry<PlayerChunkClaim, PlayerClaimPosList> listEntry : claimLists.entrySet()) {
			PlayerClaimPosList list = listEntry.getValue();
			total += list.getCount();
		}
		return total;
	}
	
	private int calculateForceloadableCount() {
		int total = 0;
		for (Map.Entry<PlayerChunkClaim, PlayerClaimPosList> listEntry : claimLists.entrySet()) {
			PlayerClaimPosList list = listEntry.getValue();
			if(listEntry.getKey().isForceloadable())
				total += list.getCount();
		}
		return total;
	}
	
	public boolean removeClaim(int x, int z, PlayerChunkClaim claim) {
		PlayerClaimPosList list = getOrCreateList(claim);
		boolean result = list.remove(x, z);
		count--;
		if(claim.isForceloadable())
			forceloadableCount--;
		if(list.getCount() <= 0)
			removeList(list);
		return result;
	}
	
	public void addClaim(int x, int z, PlayerChunkClaim claim) {
		PlayerClaimPosList dest = getOrCreateList(claim);
		dest.add(x, z);
		count++;
		if(claim.isForceloadable())
			forceloadableCount++;
	}
	
	public ResourceLocation getDimension() {
		return dimension;
	}

	@Nonnull
	@Override
	public Stream<PlayerClaimPosList> getStream() {
		return claimLists.values().stream();
	}

}
