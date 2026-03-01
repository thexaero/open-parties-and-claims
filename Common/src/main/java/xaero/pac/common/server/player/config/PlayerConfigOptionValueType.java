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

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PlayerConfigOptionValueType<T> {

	private final Class<T> jType;
	private final String id;
	private final Function<T, Tag> syncEncoder;
	private final Function<Tag, T> syncDecoder;
	private final Function<String, T> stringParser;
	private final Function<T, String> stringWriter;
	private final Function<T, Component> componentWriter;

	//The default for the value type, but can be customized per option type like in PlayerConfigRangedOptionSpec
	private final BiConsumer<ForgeConfigSpec.Builder, PlayerConfigOptionSpec<T>> defaultSpecDefiner;

	protected PlayerConfigOptionValueType(
			Class<T> jType,
			String id,
			Function<T, Tag> syncEncoder,
			Function<Tag, T> syncDecoder,
			Function<String, T> stringParser,
			Function<T, String> stringWriter,
			Function<T, Component> componentWriter,
			BiConsumer<ForgeConfigSpec.Builder, PlayerConfigOptionSpec<T>> defaultSpecDefiner
	) {
		this.jType = jType;
		this.id = id;
		this.syncEncoder = syncEncoder;
		this.syncDecoder = syncDecoder;
		this.stringParser = stringParser;
		this.stringWriter = stringWriter;
		this.componentWriter = componentWriter;
		this.defaultSpecDefiner = defaultSpecDefiner;
	}

	public Class<T> getJType() {
		return jType;
	}

	public String getId() {
		return id;
	}

	public Function<T, Tag> getSyncEncoder() {
		return syncEncoder;
	}

	public Function<Tag, T> getSyncDecoder() {
		return syncDecoder;
	}

	public Function<String, T> getStringParser() {
		return stringParser;
	}

	public Function<T, String> getStringWriter() {
		return stringWriter;
	}

	public Function<T, Component> getComponentWriter() {
		return componentWriter;
	}

	public BiConsumer<ForgeConfigSpec.Builder, PlayerConfigOptionSpec<T>> getDefaultSpecDefiner() {
		return defaultSpecDefiner;
	}

	public static abstract class Builder<T, B extends Builder<T, B>> {

		protected final B self;
		protected final Class<T> jType;
		protected String id;
		protected Function<T, Tag> syncEncoder;
		protected Function<Tag, T> syncDecoder;
		protected Function<String, T> stringParser;
		protected Function<T, String> stringWriter;
		protected Function<T, Component> componentWriter;
		protected BiConsumer<ForgeConfigSpec.Builder, PlayerConfigOptionSpec<T>> defaultSpecDefiner;

		@SuppressWarnings("unchecked")
		protected Builder(Class<T> jType){
			self = (B)this;
			this.jType = jType;
		}

		public B setDefault(){
			setId(null);
			setSyncDecoder(null);
			setSyncEncoder(null);
			setStringParser(null);
			setStringWriter(null);
			setComponentWriter(null);
			setDefaultSpecDefiner((specBuilder, o) ->
					specBuilder.define(o.id, o.defaultValue)
			);
			return self;
		}

		public B setId(String id) {
			this.id = id;
			return self;
		}

		public B setSyncDecoder(Function<Tag, T> syncDecoder) {
			this.syncDecoder = syncDecoder;
			return self;
		}

		public B setSyncEncoder(Function<T, Tag> syncEncoder) {
			this.syncEncoder = syncEncoder;
			return self;
		}

		public B setStringParser(Function<String, T> stringParser) {
			this.stringParser = stringParser;
			return self;
		}

		public B setStringWriter(Function<T, String> stringWriter) {
			this.stringWriter = stringWriter;
			return self;
		}

		public B setComponentWriter(Function<T, Component> componentWriter) {
			this.componentWriter = componentWriter;
			return self;
		}

		public B setDefaultSpecDefiner(BiConsumer<ForgeConfigSpec.Builder, PlayerConfigOptionSpec<T>> defaultSpecDefiner) {
			this.defaultSpecDefiner = defaultSpecDefiner;
			return self;
		}

		public PlayerConfigOptionValueType<T> build(Map<String, PlayerConfigOptionValueType<?>> dest){
			if(id == null || jType == null || syncDecoder == null || syncEncoder == null ||
					stringParser == null || stringWriter == null || defaultSpecDefiner == null)
				throw new IllegalStateException();
			if(componentWriter == null) {
				final Function<T, String> finalStringWriter = stringWriter;
				setComponentWriter(v -> new TextComponent(finalStringWriter.apply(v)));
			}
			PlayerConfigOptionValueType<T> result = buildInternally();
			if(dest != null)
				dest.put(id, result);
			return result;
		}

		protected abstract PlayerConfigOptionValueType<T> buildInternally();

	}

	public static final class FinalBuilder<T> extends Builder<T, FinalBuilder<T>> {

		private FinalBuilder(Class<T> jType){
			super(jType);
		}

		@Override
		protected PlayerConfigOptionValueType<T> buildInternally(){
			return new PlayerConfigOptionValueType<>(
					jType, id, syncEncoder, syncDecoder, stringParser,
					stringWriter, componentWriter,
					defaultSpecDefiner
			);
		}

		public static <T> FinalBuilder<T> begin(Class<T> jType){
			return new FinalBuilder<T>(jType).setDefault();
		}

	}

}
