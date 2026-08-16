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

package xaero.pac.common.server.claims.protection.override.api;

import javax.annotation.Nonnull;

/**
 * A chunk access override
 */
public class ChunkAccessOverride {

	private final ChunkAccessOverrideType type;

	/**
	 * Constructs a new chunk access override with a specified type.
	 * @param type  the override type, not null
	 */
	public ChunkAccessOverride(@Nonnull ChunkAccessOverrideType type) {
		this.type = type;
	}

	/**
	 * Gets the type of this override.
	 *
	 * @return the override type, not null
	 */
	@Nonnull
	public ChunkAccessOverrideType getType() {
		return type;
	}

}
