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

package xaero.pac.common.server.player.config;

import com.electronwill.nightconfig.core.utils.StringUtils;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlayerConfigOptionSpec<T> {
	
	protected final String id;
	private final List<String> path;
	protected final T defaultValue;
	private final BiFunction<PlayerConfig<?>, T, T> defaultReplacer;
	private final String comment;
	private final String translation;
	protected final Class<T> type;
	private final Function<String, T> commandInputParser;
	private final Function<T, String> commandOutputWriter;
	private final Predicate<T> validator;
	private final String tooltipPrefix;
	
	protected PlayerConfigOptionSpec(Class<T> type, String id, List<String> path, T defaultValue, BiFunction<PlayerConfig<?>, T, T> defaultReplacer, String comment, String translation, Function<String, T> commandInputParser, Function<T, String> commandOutputWriter, Predicate<T> validator, String tooltipPrefix) {
		super();
		this.type = type;
		this.id = id;
		this.path = path;
		this.defaultValue = defaultValue;
		this.defaultReplacer = defaultReplacer;
		this.comment = comment;
		this.translation = translation;
		this.commandInputParser = commandInputParser;
		this.commandOutputWriter = commandOutputWriter;
		this.validator = validator;
		this.tooltipPrefix = tooltipPrefix;
	}

	protected ForgeConfigSpec.Builder buildForgeSpec(ForgeConfigSpec.Builder builder) {
		return builder
		.comment(comment)
		.translation(translation);
	}

	public PlayerConfigOptionSpec<T> applyToForgeSpec(ForgeConfigSpec.Builder builder) {
		buildForgeSpec(builder).define(id, defaultValue);
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public List<String> getPath() {
		return path;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public String getTranslation() {
		return translation;
	}
	
	public String getComment() {
		return comment;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public Predicate<T> getValidator() {
		return validator;
	}
	
	public String getTooltipPrefix() {
		return tooltipPrefix;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", id, type);
	}
	
	public Function<String, T> getCommandInputParser() {
		return commandInputParser;
	}
	
	public Function<T, String> getCommandOutputWriter() {
		return commandOutputWriter;
	}
	
	@SuppressWarnings("unchecked")
	public Function<Object, String> getCommandOutputWriterCast() {
		return (Function<Object, String>) commandOutputWriter;
	}
	
	public BiFunction<PlayerConfig<?>, T, T> getDefaultReplacer() {
		return defaultReplacer;
	}
	
	abstract static class Builder<T, B extends Builder<T, B>> {
		
		protected final B self;
		protected final Class<T> type;
		protected String id;
		protected T defaultValue;
		protected BiFunction<PlayerConfig<?>, T, T> defaultReplacer;
		protected String comment;
		protected String translation;
		private Predicate<T> validator;
		protected String tooltipPrefix;
		protected Function<T, String> commandOutputWriter;
		
		@SuppressWarnings("unchecked")
		protected Builder(Class<T> valueType){
			this.self = (B) this;
			this.type = valueType;
		}
		
		public B setDefault(){
			setId(null);
			setDefaultValue(null);
			setDefaultReplacer(null);
			setComment(null);
			setTranslation(null);
			setValidator(null);
			setTooltipPrefix(null);
			setCommandOutputWriter(o -> o.toString());
			return self;
		}
		
		public B setId(String id) {
			this.id = id;
			return self;
		}
		
		public B setDefaultValue(T defaultValue) {
			this.defaultValue = defaultValue;
			return self;
		}
		
		public B setDefaultReplacer(BiFunction<PlayerConfig<?>, T, T> defaultReplacer) {
			this.defaultReplacer = defaultReplacer;
			return self;
		}
		
		public B setComment(String comment) {
			this.comment = comment;
			return self;
		}
		
		public B setTranslation(String translation) {
			this.translation = translation;
			return self;
		}
		
		public B setValidator(Predicate<T> validator) {
			this.validator = validator;
			return self;
		}
		
		public B setTooltipPrefix(String tooltipPrefix) {
			this.tooltipPrefix = tooltipPrefix;
			return self;
		}
		
		public B setCommandOutputWriter(Function<T, String> commandOutputWriter) {
			this.commandOutputWriter = commandOutputWriter;
			return self;
		}

		@SuppressWarnings("unchecked")
		private Function<String, T> getCommandInputParser() {
			Function<String, T> commandInputParser = null;
			if(type == Boolean.class)
				commandInputParser = s -> (T)(Object)s.equals("true");
			else if(type == Integer.class)
				commandInputParser = s -> (T)(Object)Integer.parseInt(s);
			else if(type == Double.class)
				commandInputParser = s -> (T)(Object)Double.parseDouble(s);
			else if(type == Float.class)
				commandInputParser = s -> (T)(Object)Float.parseFloat(s);
			else if(type == String.class)
				commandInputParser = s -> (T)s;
			return commandInputParser;
		}
		
		protected final Predicate<T> getValidator(){
			if(type == String.class && validator == null)
				throw new IllegalStateException();
			return validator == null ? (v -> true) : validator;
		}
		
		public PlayerConfigOptionSpec<T> build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(id == null || defaultValue == null || comment == null || commandOutputWriter == null)
				throw new IllegalStateException();
			if(translation == null)
				setTranslation("gui.xaero_pac_player_config_" + id);
			Function<String, T> commandInputParser = getCommandInputParser();
			if(commandInputParser == null)
				throw new IllegalStateException();
			PlayerConfigOptionSpec<T> spec = buildInternally(StringUtils.split(id, '.'), commandInputParser);
			dest.put(spec.getId(), spec);
			return spec;
		}
		
		protected abstract PlayerConfigOptionSpec<T> buildInternally(List<String> path, Function<String, T> commandInputParser);
		
	}
	
	public static final class FinalBuilder<T> extends Builder<T, FinalBuilder<T>> {
		
		protected FinalBuilder(Class<T> valueType) {
			super(valueType);
		}

		@Override
		protected PlayerConfigOptionSpec<T> buildInternally(List<String> path, Function<String, T> commandInputParser){
			return new PlayerConfigOptionSpec<>(type, id, path, defaultValue, defaultReplacer, comment, translation, commandInputParser, commandOutputWriter, getValidator(), tooltipPrefix);
		}
		
		public static <T> FinalBuilder<T> begin(Class<T> valueType){
			return new FinalBuilder<T>(valueType).setDefault();
		}
		
	}
	
}
