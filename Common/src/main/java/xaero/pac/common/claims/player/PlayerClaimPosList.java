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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public final class PlayerClaimPosList implements IPlayerClaimPosList {
	
	private final PlayerChunkClaim claimState;
	private final LongSet positions;
	
	private PlayerClaimPosList(PlayerChunkClaim claimState, LongSet positions) {
		super();
		this.claimState = claimState;
		this.positions = positions;
	}
	
	@Nonnull
	@Override
	public PlayerChunkClaim getClaimState() {
		return claimState;
	}

	@Nonnull
	@Override
	public Stream<ChunkPos> getStream(){
		return positions.longStream().mapToObj(key -> new ChunkPos(PlayerChunkClaim.getXFromLongCoordinates(key), PlayerChunkClaim.getZFromLongCoordinates(key)));
	}

	@Override
	public int getCount() {
		return positions.size();
	}
	
	public boolean remove(int x, int z) {
		long key = PlayerChunkClaim.getLongCoordinatesFor(x, z);
		return positions.remove(key);
	}
	
	public void add(int x, int z) {
		long key = PlayerChunkClaim.getLongCoordinatesFor(x, z);
		positions.add(key);
	}
	
	public static final class Builder {
		
		private PlayerChunkClaim claim;

		private Builder() {
		}

		private Builder setDefault() {
			return this;
		}

		public Builder setClaim(PlayerChunkClaim claim) {
			this.claim = claim;
			return this;
		}

		public PlayerClaimPosList build() {
			if (claim == null)
				throw new IllegalStateException();
			return new PlayerClaimPosList(claim, new LongOpenHashSet());
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
