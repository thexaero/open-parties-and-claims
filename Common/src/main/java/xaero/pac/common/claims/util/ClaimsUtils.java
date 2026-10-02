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

package xaero.pac.common.claims.util;

import net.minecraft.world.level.ChunkPos;

public class ClaimsUtils {

	public static boolean withinAnchorDistance(Iterable<ChunkPos> anchors, int anchorRange, int x, int z){
		for (ChunkPos anchor : anchors) {
			if(x > anchor.x + anchorRange || x < anchor.x - anchorRange)
				continue;
			if(z > anchor.z + anchorRange || z < anchor.z - anchorRange)
				continue;
			return true;
		}
		return false;
	}

}
