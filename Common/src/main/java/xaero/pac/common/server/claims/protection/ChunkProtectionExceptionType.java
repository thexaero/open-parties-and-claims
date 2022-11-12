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

package xaero.pac.common.server.claims.protection;

public enum ChunkProtectionExceptionType {

	INTERACTION("interact$"),
	BARRIER("barrier$"),
	EMPTY_HAND_INTERACTION(ChunkProtection.HAND_PREFIX),
	BREAK(ChunkProtection.BREAK_PREFIX),
	BLOCK_ACCESS("access$"),
	ENTITY_ACCESS("access$"),
	DROPPED_ITEM_ACCESS("access$");

	private final String prefix;

	ChunkProtectionExceptionType(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public boolean is(String prefixedName){
		return prefixedName.startsWith(prefix);
	}

}
