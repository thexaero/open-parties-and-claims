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

package xaero.pac.common.registry.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.stream.Stream;

public class ItemRegistryFabric implements IItemRegistry {
	@Override
	public Item getValue(ResourceLocation id) {
		return Registry.ITEM.get(id);
	}

	@Override
	public Stream<Item> getTagStream(TagKey<Item> tagKey) {
		return Registry.ITEM.getTag(tagKey).stream().flatMap(HolderSet.Named::stream).map(Holder::value);
	}

	@Override
	public ResourceLocation getKey(Item item) {
		return Registry.ITEM.getKey(item);
	}

	@Override
	public Iterable<Item> getIterable(){
		return Registry.ITEM.stream().toList();
	}

	@Override
	public Iterable<TagKey<Item>> getTagIterable(){
		return Registry.ITEM.getTags().map(Pair::getFirst).toList();
	}

}
