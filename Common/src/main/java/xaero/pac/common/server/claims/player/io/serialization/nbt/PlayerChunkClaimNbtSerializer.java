/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.player.io.serialization.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import xaero.pac.common.claims.player.PlayerChunkClaim;

import java.util.UUID;

public class PlayerChunkClaimNbtSerializer {
	
	public PlayerChunkClaim deserialize(UUID playerId, CompoundTag nbt) {
		boolean forceloaded = nbt.getBoolean("forceloaded");
		int subConfigIndex = nbt.contains("subConfigIndex", Tag.TAG_INT) ? nbt.getInt("subConfigIndex") : -1;
		return new PlayerChunkClaim(playerId, subConfigIndex, forceloaded, 0);
	}

	public CompoundTag serialize(PlayerChunkClaim object) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("subConfigIndex", object.getSubConfigIndex());
		nbt.putBoolean("forceloaded", object.isForceloadable());
		return nbt;
	}

}
