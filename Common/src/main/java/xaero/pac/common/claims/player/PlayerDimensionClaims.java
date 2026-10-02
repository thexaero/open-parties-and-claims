/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.ClaimingAnchor;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.api.ClaimLocation;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.*;
import java.util.stream.Stream;

public class PlayerDimensionClaims implements IPlayerDimensionClaims<PlayerClaimPosList> {

	protected final UUID playerId;
	protected final Identifier dimension;
	protected final Map<PlayerChunkClaim, PlayerClaimPosList> claimLists;
	protected final Map<ChunkPos, ClaimingAnchor> anchors;
	protected final Map<ChunkPos, ClaimingAnchor> anchorsUnmodifiable;
	protected final LinkedChain<ClaimingAnchor> anchorLinkedChain;
	protected int count;
	protected int forceloadableCount;
	protected final ClaimsManager<?,?,?,?,?> claimsManager;
	
	public PlayerDimensionClaims(UUID playerId, Identifier dimension, Map<PlayerChunkClaim, PlayerClaimPosList> claimLists, Map<ChunkPos, ClaimingAnchor> anchors, ClaimsManager<?,?,?,?,?> claimsManager) {
		this.playerId = playerId;
		this.dimension = dimension;
		this.claimLists = claimLists;
		this.claimsManager = claimsManager;
		this.count = calculateCount();
		this.forceloadableCount = calculateForceloadableCount();
		this.anchors = anchors;
		this.anchorsUnmodifiable = Collections.unmodifiableMap(anchors);
		this.anchorLinkedChain = new LinkedChain<>();
		for (ClaimingAnchor anchor : anchors.values())
			anchorLinkedChain.add(anchor);
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
	
	public Identifier getDimension() {
		return dimension;
	}

	@Nonnull
	@Override
	public Stream<PlayerClaimPosList> getTypedStream() {
		return claimLists.values().stream();
	}

	public ClaimLocation getRandomClaimPos(boolean firstPosIfTooMany) {
		int totalCount = getCount();
		if(totalCount == 0)
			return null;
		int randomClaimIndex = (int) (Math.random() * totalCount);
		int offset = 0;
		for (PlayerClaimPosList claimList : claimLists.values()) {
			int claimListCount = claimList.getCount();
			if(randomClaimIndex >= offset + claimListCount){
				offset += claimListCount;
				continue;
			}
			ChunkPos pos;
			if(firstPosIfTooMany && claimListCount > 10000)
				pos = claimList.getPosSlowly(0);
			else
				pos = claimList.getPosSlowly(randomClaimIndex - offset);
			if(pos == null)
				return null;
			return new ClaimLocation(dimension, pos.x(), pos.z());
		}
		return null;
	}

	public boolean addAnchor(@Nonnull ChunkPos pos){
		if(anchors.containsKey(pos))
			return false;
		ClaimingAnchor anchor = new ClaimingAnchor(pos);
		anchors.put(pos, anchor);
		anchorLinkedChain.add(anchor);
		claimsManager.getTracker().onChunkChange(dimension, pos.x(), pos.z(), claimsManager.get(dimension, pos.x(), pos.z()));
		return true;
	}

	public boolean removeAnchor(@Nonnull ChunkPos pos){
		ClaimingAnchor anchor = anchors.remove(pos);
		if(anchor != null){
			anchorLinkedChain.remove(anchor);
			claimsManager.getTracker().onChunkChange(dimension, pos.x(), pos.z(), claimsManager.get(dimension, pos.x(), pos.z()));
			return true;
		}
		return false;
	}

	@Nonnull
	public Set<ChunkPos> getAnchors() {
		return anchorsUnmodifiable.keySet();
	}

	@Override
	public Iterator<ClaimingAnchor> getAnchorIterator(){
		return anchorLinkedChain.iterator();
	}

}
