/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EntityData {

	private UUID lootOwner;
	private UUID deadPlayer;
	private ResourceKey<Level> lastChunkEntryDimension;
	private boolean shouldCheckItemUseTick;

	public UUID getLootOwner() {
		return lootOwner;
	}

	public void setLootOwner(UUID lootOwner) {
		this.lootOwner = lootOwner;
	}

	public UUID getDeadPlayer() {
		return deadPlayer;
	}

	public void setDeadPlayer(UUID deadPlayer) {
		this.deadPlayer = deadPlayer;
	}

	public ResourceKey<Level> getLastChunkEntryDimension() {
		return lastChunkEntryDimension;
	}

	public void setLastChunkEntryDimension(ResourceKey<Level> lastChunkEntryDimension) {
		this.lastChunkEntryDimension = lastChunkEntryDimension;
	}

	public boolean getShouldCheckItemUseTick() {
		return shouldCheckItemUseTick;
	}

	public void setShouldCheckItemUseTick(boolean shouldCheckItemUseTick) {
		this.shouldCheckItemUseTick = shouldCheckItemUseTick;
	}

	public static EntityData from(Entity entity){
		return from((IEntity) entity);
	}

	public static EntityData from(IEntity iEntity){
		EntityData result = iEntity.getXaero_OPAC_data();
		if(result == null)
			iEntity.setXaero_OPAC_data(result = new EntityData());
		return result;
	}

}
