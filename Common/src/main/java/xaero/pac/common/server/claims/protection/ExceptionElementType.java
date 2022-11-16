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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import xaero.pac.common.platform.Services;

import java.util.function.Function;
import java.util.stream.Stream;

public class ExceptionElementType<T> {

	public static final ExceptionElementType<Block> BLOCK = new ExceptionElementType<Block>
			(
					Services.PLATFORM.getBlockRegistry()::getValue,
					rl -> TagKey.create(Registry.BLOCK_REGISTRY, rl),
					Services.PLATFORM.getBlockRegistry().getIterable(),
					Services.PLATFORM.getBlockRegistry().getTagIterable(),
					Services.PLATFORM.getBlockRegistry()::getKey,
					Services.PLATFORM.getBlockRegistry()::getTagStream);
	public static final ExceptionElementType<EntityType<?>> ENTITY_TYPE = new ExceptionElementType<EntityType<?>>
			(
					Services.PLATFORM.getEntityRegistry()::getValue,
					rl -> TagKey.create(Registry.ENTITY_TYPE_REGISTRY, rl),
					Services.PLATFORM.getEntityRegistry().getIterable(),
					Services.PLATFORM.getEntityRegistry().getTagIterable(),
					Services.PLATFORM.getEntityRegistry()::getKey,
					Services.PLATFORM.getEntityRegistry()::getTagStream);
	public static final ExceptionElementType<Item> ITEM = new ExceptionElementType<Item>
			(
					Services.PLATFORM.getItemRegistry()::getValue,
					rl -> TagKey.create(Registry.ITEM_REGISTRY, rl),
					Services.PLATFORM.getItemRegistry().getIterable(),
					Services.PLATFORM.getItemRegistry().getTagIterable(),
					Services.PLATFORM.getItemRegistry()::getKey,
					Services.PLATFORM.getItemRegistry()::getTagStream);

	private final Function<ResourceLocation, T> getter;
	private final Function<ResourceLocation, TagKey<T>> tagGetter;
	private final Iterable<T> iterable;
	private final Iterable<TagKey<T>> tagIterable;
	private final Function<T, ResourceLocation> keyGetter;
	private final Function<TagKey<T>, ResourceLocation> tagKeyGetter;
	private final Function<TagKey<T>, Stream<T>> tagStreamGetter;

	private ExceptionElementType(Function<ResourceLocation, T> getter, Function<ResourceLocation, TagKey<T>> tagGetter, Iterable<T> iterable, Iterable<TagKey<T>> tagIterable, Function<T, ResourceLocation> keyGetter, Function<TagKey<T>, Stream<T>> tagStreamGetter) {
		this.getter = getter;
		this.tagGetter = tagGetter;
		this.iterable = iterable;
		this.tagIterable = tagIterable;
		this.keyGetter = keyGetter;
		this.tagStreamGetter = tagStreamGetter;
		this.tagKeyGetter = TagKey::location;
	}

	public Function<ResourceLocation, T> getGetter() {
		return getter;
	}

	public Function<ResourceLocation, TagKey<T>> getTagGetter() {
		return tagGetter;
	}

	public Iterable<T> getIterable() {
		return iterable;
	}

	public Iterable<TagKey<T>> getTagIterable() {
		return tagIterable;
	}

	public Function<T, ResourceLocation> getKeyGetter() {
		return keyGetter;
	}

	public Function<TagKey<T>, ResourceLocation> getTagKeyGetter() {
		return tagKeyGetter;
	}

	public Function<TagKey<T>, Stream<T>> getTagStreamGetter() {
		return tagStreamGetter;
	}

}
