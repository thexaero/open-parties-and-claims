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

package xaero.pac.common.server.player.config.dynamic;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;

import java.util.Map;

public class PlayerConfigDynamicOptionsLoader {

	public void load(PlayerConfigDynamicOptions.Builder builder, Map<String, ChunkProtectionExceptionGroup<Block>> blockExceptionGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityExceptionGroups, Map<String, ChunkProtectionExceptionGroup<Item>> itemExceptionGroups, Map<String, ChunkProtectionExceptionGroup<EntityType<?>>> entityBarrierGroups){
		PlayerConfigExceptionDynamicOptionsLoader exceptionDynamicOptionsLoader = new PlayerConfigExceptionDynamicOptionsLoader();
		entityBarrierGroups.values().forEach(group -> exceptionDynamicOptionsLoader.handleGroup(group, builder, "entity", "entities", e -> EntityType.getKey(e).toString()));
		blockExceptionGroups.values().forEach(group -> exceptionDynamicOptionsLoader.handleGroup(group, builder, "block", "blocks", b -> Registry.BLOCK.getKey(b).toString()));
		entityExceptionGroups.values().forEach(group -> exceptionDynamicOptionsLoader.handleGroup(group, builder, "entity", "entities", e -> EntityType.getKey(e).toString()));
		itemExceptionGroups.values().forEach(group -> exceptionDynamicOptionsLoader.handleGroup(group, builder, "item", "items", i -> Registry.ITEM.getKey(i).toString()));
	}

}
