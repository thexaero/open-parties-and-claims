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

package xaero.pac.common.claims.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A location which includes the dimension ID and the chunk position coordinates that may be used for a claim
 */
public class ClaimLocation {

	private final Identifier dimId;
	private final ChunkPos pos;

	/**
	 * Constructs a new claim location for a specified dimension ID and chunk position.
	 *
	 * @param dimId  the dimension ID, not null
	 * @param chunkX  the X chunk coordinate
	 * @param chunkZ  the Z chunk coordinate
	 */
	public ClaimLocation(@Nonnull Identifier dimId, int chunkX, int chunkZ) {
		this.dimId = dimId;
		this.pos = new ChunkPos(chunkX, chunkZ);
	}

	/**
	 * Gets the dimension ID.
	 *
	 * @return the dimension ID of the claim, not null
	 */
	@Nonnull
	public Identifier getDimId() {
		return dimId;
	}

	/**
	 * Gets the X chunk coordinate.
	 *
	 * @return the X chunk coordinate of the claim
	 */
	public int getChunkX() {
		return pos.x;
	}

	/**
	 * Gets the Z chunk coordinate.
	 *
	 * @return the Z chunk coordinate of the claim
	 */
	public int getChunkZ() {
		return pos.z;
	}

	/**
	 * Gets the chunk position.
	 *
	 * @return the chunk position of the claim, not null
	 */
	@Nonnull
	public ChunkPos getChunkPos() {
		return pos;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ClaimLocation that = (ClaimLocation) o;
		return Objects.equals(pos, that.pos) && Objects.equals(dimId, that.dimId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dimId, pos);
	}

}
