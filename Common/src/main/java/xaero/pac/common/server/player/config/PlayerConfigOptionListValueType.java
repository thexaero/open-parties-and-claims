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

package xaero.pac.common.server.player.config;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class PlayerConfigOptionListValueType<T> extends PlayerConfigOptionValueType<List<T>>{

	public static final String ID_PREFIX = "l;";

	@SuppressWarnings("unchecked")
	private PlayerConfigOptionListValueType(
			String id,
			Function<List<T>, Tag> syncEncoder,
			Function<Tag, List<T>> syncDecoder,
			Function<String, List<T>> stringParser,
			Function<List<T>, String> stringWriter,
			Function<List<T>, Component> componentWriter,
			List<String> defaultCommandSuggestions,
			boolean shouldDisplayInQuotes,
			BiConsumer<ForgeConfigSpec.Builder, PlayerConfigOptionSpec<List<T>>> defaultSpecDefiner
	) {
		super(
				(Class<List<T>>)(Object)List.class, id, syncEncoder, syncDecoder,
				stringParser, stringWriter, componentWriter, defaultCommandSuggestions,
				shouldDisplayInQuotes, defaultSpecDefiner
		);
	}

	public static final class Builder<T> extends PlayerConfigOptionValueType.Builder<List<T>, Builder<T>> {

		private PlayerConfigOptionValueType<T> elementType;

		@SuppressWarnings("unchecked")
		private Builder(){
			super((Class<List<T>>)(Object)List.class);
		}

		@Override
		public Builder<T> setDefault() {
			super.setDefault();
			setElementType(null);
			setDefaultSpecDefiner(null);//so we can detect whether a custom one is provided
			return self;
		}

		public Builder<T> setElementType(PlayerConfigOptionValueType<T> elementType) {
			this.elementType = elementType;
			return self;
		}

		@Override
		public Builder<T> setId(String id) {
			if(id != null)
				throw new IllegalArgumentException();
			return super.setId(id);
		}

		@Override
		public Builder<T> setSyncDecoder(Function<Tag, List<T>> syncDecoder) {
			if(syncDecoder != null)
				throw new IllegalArgumentException();
			return super.setSyncDecoder(syncDecoder);
		}

		@Override
		public Builder<T> setSyncEncoder(Function<List<T>, Tag> syncEncoder) {
			if(syncEncoder != null)
				throw new IllegalArgumentException();
			return super.setSyncEncoder(syncEncoder);
		}

		@Override
		public Builder<T> setStringParser(Function<String, List<T>> stringParser) {
			if(stringParser != null)
				throw new IllegalArgumentException();
			return super.setStringParser(stringParser);
		}

		@Override
		public Builder<T> setStringWriter(Function<List<T>, String> stringWriter) {
			if(stringWriter != null)
				throw new IllegalArgumentException();
			return super.setStringWriter(stringWriter);
		}

		@Override
		public Builder<T> setComponentWriter(Function<List<T>, Component> componentWriter) {
			if(componentWriter != null)
				throw new IllegalArgumentException();
			return super.setComponentWriter(componentWriter);
		}

		@Override
		public PlayerConfigOptionValueType<List<T>> build(Map<String, PlayerConfigOptionValueType<?>> dest) {
			if(elementType == null)
				throw new IllegalStateException();
			final PlayerConfigOptionValueType<T> finalElementType = elementType;
			super.setId(ID_PREFIX + elementType.getId());
			super.setSyncDecoder(tag -> {
				if(!(tag instanceof ListTag listTag))
					return null;
				List<T> result = new ArrayList<>();
				for (Tag elementTag : listTag) {
					T decodedElement = finalElementType.getSyncDecoder().apply(elementTag);
					if(decodedElement == null)
						continue;
					result.add(decodedElement);
				}
				return result;
			});
			super.setSyncEncoder(list -> {
				ListTag listTag = new ListTag();
				for (T element : list) {
					Tag encodedElement = finalElementType.getSyncEncoder().apply(element);
					listTag.add(encodedElement);
				}
				return listTag;
			});
			super.setStringWriter(list -> {
				StringBuilder output = new StringBuilder("[");
				boolean first = true;
				for (T element : list) {
					if(!first)
						output.append("; ");
					if(finalElementType.getJType() == String.class)
						output.append("'");
					output.append(finalElementType.getStringWriter().apply(element));
					if(finalElementType.getJType() == String.class)
						output.append("'");
					first = false;
				}
				output.append("]");
				return output.toString();
			});
			super.setComponentWriter(list -> {
				MutableComponent output = Component.literal("[");
				boolean first = true;
				for (T element : list) {
					if(!first)
						output.append("; ");
					if(finalElementType.getJType() == String.class)
						output.append("'");
					output.append(finalElementType.getComponentWriter().apply(element));
					if(finalElementType.getJType() == String.class)
						output.append("'");
					first = false;
				}
				output.append("]");
				return output;
			});
			super.setStringParser(str -> {
				str = str.trim();
				if(!str.startsWith("[") || !str.endsWith("]"))
					throw new IllegalArgumentException();
				String[] elementStrings = str.substring(1, str.length() - 1).split(";");
				List<T> parsedList = new ArrayList<>();
				for (String elementString : elementStrings) {
					elementString = elementString.trim();
					if(elementString.isEmpty())
						continue;
					if(finalElementType.getJType() == String.class){
						if(elementString.length() < 2)
							throw new IllegalArgumentException();
						if(!elementString.startsWith("'") || !elementString.endsWith("'"))
							throw new IllegalArgumentException();
						elementString = elementString.substring(1, elementString.length() - 1);
						if(elementString.contains("'"))
							throw new IllegalArgumentException();
					}
					try {
						T parsedElement = finalElementType.getStringParser().apply(elementString);
						if (parsedElement == null)
							continue;
						parsedList.add(parsedElement);
					} catch(Throwable t){
						throw new IllegalArgumentException(t);
					}
				}
				return parsedList;
			});
			if(defaultSpecDefiner == null){
				setDefaultSpecDefiner((specBuilder, o) ->
					specBuilder.defineListAllowEmpty(
							o.getPath(),
							() -> o.defaultValue,
							el ->
								el != null && finalElementType.getJType().isAssignableFrom(el.getClass())
					)
				);
			}
			return super.build(dest);
		}

		@Override
		protected PlayerConfigOptionListValueType<T> buildInternally(){
			return new PlayerConfigOptionListValueType<>(
					id, syncEncoder, syncDecoder, stringParser,
					stringWriter, componentWriter, defaultCommandSuggestions,
					shouldDisplayInQuotes, defaultSpecDefiner
			);
		}

		public static <T> Builder<T> begin(){
			return new Builder<T>().setDefault();
		}

	}

}
