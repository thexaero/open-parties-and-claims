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

package xaero.pac.common.server.claims.protection.group;

import com.mojang.datafixers.util.Either;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ChunkProtectionExceptionGroupLoader {

	public <T> void load(ForgeConfigSpec.ConfigValue<List<? extends String>> configOption,
						 Function<ResourceLocation, T> objectGetter, Function<ResourceLocation, TagKey<T>> tagGetter,
						 Function<TagKey<T>, Stream<T>> tagStreamGetter, Map<String, ChunkProtectionExceptionGroup<T>> destination,
						 ChunkProtectionExceptionType defaultType, Predicate<ChunkProtectionExceptionType> typeFilter){
		configOption.get().forEach(stringEntry -> {
			int listStartIndex = stringEntry.indexOf('{');
			int listEndIndex;
			if(listStartIndex < 0 || (listEndIndex = stringEntry.indexOf('}')) < listStartIndex) {
				OpenPartiesAndClaims.LOGGER.error("Exception group must contain { and }, in that order: " + stringEntry);
				return;
			}
			String prefixedName = stringEntry.substring(0, listStartIndex);
			String name = prefixedName;
			ChunkProtectionExceptionType type = defaultType;
			if(prefixedName.contains("$")) {
				type = null;
				for (int i = 0; i < ChunkProtectionExceptionType.values().length; i++) {
					ChunkProtectionExceptionType t = ChunkProtectionExceptionType.values()[i];
					if (typeFilter.test(t) && t.is(prefixedName)) {
						type = t;
						name = prefixedName.substring(type.getPrefix().length());
						break;
					}
				}
			}
			if(type == null) {
				OpenPartiesAndClaims.LOGGER.error("Unknown exception group type prefix: " + prefixedName);
				return;
			}
			if(destination.containsKey(prefixedName)){
				OpenPartiesAndClaims.LOGGER.error("Exception group name must be unique: " + prefixedName);
				return;
			}
			ChunkProtectionExceptionGroup.Builder<T> builder = ChunkProtectionExceptionGroup.Builder.<T>begin()
				.setName(name)
				.setType(type)
				.setTagStreamGetter(tagStreamGetter);

			String groupContent = stringEntry.substring(listStartIndex + 1, listEndIndex).trim();
			String[] groupContentSplit = groupContent.split("\\s*,\\s*");
			for(String element : groupContentSplit){
				boolean tag = element.startsWith(ChunkProtection.TAG_PREFIX);
				String id = element.substring(tag ? ChunkProtection.TAG_PREFIX.length() : 0);
				try {
					ResourceLocation elementResourceLocation = new ResourceLocation(id);
					if(tag) {
						TagKey<T> objectTag = tagGetter.apply(elementResourceLocation);
						if(objectTag != null)
							builder.addException(Either.right(objectTag));
					} else {
						T object = objectGetter.apply(elementResourceLocation);
						if(object != null)
							builder.addException(Either.left(object));
					}
				} catch(ResourceLocationException rle){
					OpenPartiesAndClaims.LOGGER.error("Invalid resource location in an exception group: " + element, rle);
				}
			}
			ChunkProtectionExceptionGroup<T> group = builder.build();
			destination.put(prefixedName, group);
		});
	}

}
