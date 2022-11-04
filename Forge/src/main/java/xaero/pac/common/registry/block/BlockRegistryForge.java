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

package xaero.pac.common.registry.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public class BlockRegistryForge implements IBlockRegistry {

	@Override
	public Block getValue(ResourceLocation id) {
		return ForgeRegistries.BLOCKS.getValue(id);
	}

	@Override
	public Stream<Block> getTagStream(TagKey<Block> tagKey) {
		return ForgeRegistries.BLOCKS.tags().getTag(tagKey).stream();
	}

	@Override
	public ResourceLocation getKey(Block block) {
		return ForgeRegistries.BLOCKS.getKey(block);
	}

	@Override
	public Iterable<Block> getIterable(){
		return ForgeRegistries.BLOCKS.getValues();
	}

	@Override
	public Iterable<TagKey<Block>> getTagIterable(){
		return ForgeRegistries.BLOCKS.tags().getTagNames().toList();
	}

}
