/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2025, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.util.nbt;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.UUID;

public class XaeroNbtUtil {

	public static Optional<UUID> getUUID(CompoundTag tag, String id){
		int[] intArray = tag.getIntArray(id).orElse(null);
		if(intArray == null) {
			long[] longArray = tag.getLongArray(id).orElse(null);
			if (longArray == null || longArray.length != 2)
				return Optional.empty();
			return Optional.of(new UUID(longArray[0], longArray[1]));
		}
		if (intArray.length != 4)
			return Optional.empty();
		return Optional.of(UUIDUtil.uuidFromIntArray(intArray));
	}

	public static void putUUID(CompoundTag tag, String id, UUID uuid){
		tag.putIntArray(id, UUIDUtil.uuidToIntArray(uuid));
	}

	public static IntArrayTag createUUIDTag(UUID partyId) {
		return new IntArrayTag(UUIDUtil.uuidToIntArray(partyId));
	}

	public static Optional<UUID> getUUIDFromTag(Tag tag) {
		if(tag instanceof IntArrayTag intArrayTag)
			return Optional.of(UUIDUtil.uuidFromIntArray(intArrayTag.getAsIntArray()));
		if(tag instanceof LongArrayTag longArrayTag) {
			long[] longArray = longArrayTag.getAsLongArray();
			return Optional.of(new UUID(longArray[0], longArray[1]));
		}
		return Optional.empty();
	}

}
