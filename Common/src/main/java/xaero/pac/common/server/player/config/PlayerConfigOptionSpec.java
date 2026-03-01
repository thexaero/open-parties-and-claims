/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.packet.config.ClientboundPlayerConfigDynamicOptionsPacket;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.change.IPlayerConfigChangeHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlayerConfigOptionSpec<T> implements IPlayerConfigOptionSpecAPI<T> {

	public static final TranslatableComponent INHERITED_TEXT = new TranslatableComponent("gui.xaero_pac_config_option_sub_inherited");

	protected final String id;
	protected final String shortenedId;
	private final List<String> path;
	protected final T defaultValue;
	private final BiFunction<PlayerConfig<?>, T, T> defaultReplacer;
	private final String comment;
	private final String translation;
	private final String[] translationArgs;
	private final String commentTranslation;
	private final String[] commentTranslationArgs;
	private final PlayerConfigOptionCategory category;
	protected final PlayerConfigOptionValueType<T> valueType;
	private final BiPredicate<PlayerConfig<?>, T> serverSideValidator;
	private final BiPredicate<PlayerConfigClientStorage, T> clientSideValidator;

	private final BiPredicate<IPlayerConfigAPI, T> serverSideValidatorAPI;
	private final BiPredicate<IPlayerConfigClientStorageAPI, T> clientSideValidatorAPI;
	private final String tooltipPrefix;
	private final Predicate<PlayerConfigType> configTypeFilter;
	private final ClientboundPlayerConfigDynamicOptionsPacket.OptionType syncOptionType;
	private final boolean dynamic;
	private final boolean overridable;
	private final boolean forcedPlayerConfigurable;
	private final IPlayerConfigChangeHandler<T> serverChangeHandler;
	
	protected PlayerConfigOptionSpec(
			PlayerConfigOptionValueType<T> valueType,
			String id,
			String shortenedId,
			List<String> path,
			T defaultValue,
			BiFunction<PlayerConfig<?>, T, T> defaultReplacer,
			String comment,
			String translation,
			String[] translationArgs,
			String commentTranslation,
			String[] commentTranslationArgs,
			PlayerConfigOptionCategory category,
			BiPredicate<PlayerConfig<?>, T> serverSideValidator,
			BiPredicate<PlayerConfigClientStorage, T> clientSideValidator,
			String tooltipPrefix,
			Predicate<PlayerConfigType> configTypeFilter,
			ClientboundPlayerConfigDynamicOptionsPacket.OptionType syncOptionType,
			boolean dynamic,
			boolean overridable,
			boolean forcedPlayerConfigurable,
			IPlayerConfigChangeHandler<T> serverChangeHandler
	) {
		super();
		this.valueType = valueType;
		this.id = id;
		this.shortenedId = shortenedId;
		this.path = path;
		this.defaultValue = defaultValue;
		this.defaultReplacer = defaultReplacer;
		this.comment = comment;
		this.translation = translation;
		this.translationArgs = translationArgs;
		this.commentTranslation = commentTranslation;
		this.commentTranslationArgs = commentTranslationArgs;
		this.category = category;
		this.serverSideValidator = serverSideValidator;
		this.clientSideValidator = clientSideValidator;
		this.serverSideValidatorAPI = (c,v) -> serverSideValidator.test((PlayerConfig<?>) c, v);
		this.clientSideValidatorAPI = (c,v) -> clientSideValidator.test((PlayerConfigClientStorage) c, v);
		this.tooltipPrefix = tooltipPrefix;
		this.configTypeFilter = configTypeFilter;
		this.syncOptionType = syncOptionType;
		this.dynamic = dynamic;
		this.overridable = overridable;
		this.forcedPlayerConfigurable = forcedPlayerConfigurable;
		this.serverChangeHandler = serverChangeHandler;
	}

	protected ForgeConfigSpec.Builder buildForgeSpec(ForgeConfigSpec.Builder builder) {
		return builder
		.comment(comment)
		.translation(translation);
	}

	public PlayerConfigOptionSpec<T> applyToForgeSpec(ForgeConfigSpec.Builder builder) {
		valueType.getDefaultSpecDefiner().accept(buildForgeSpec(builder), this);
		return this;
	}

	private Component applyValueQuotesIfNeeded(Object value, Component valueString){
		Component result = valueString;
		if(value instanceof String) {
			result = new TextComponent("\"");
			result.getSiblings().add(valueString);
			result.getSiblings().add(new TextComponent("\""));
		}
		return result;
	}

	public Component getValueDisplayName(Object value){
		if(value == null)
			return PlayerConfigOptionSpec.INHERITED_TEXT;
		return applyValueQuotesIfNeeded(value, getComponentWriterCast().apply(value));
	}

	@Nonnull
	@Override
	public String getId() {
		return id;
	}

	@Nonnull
	@Override
	public String getShortenedId() {
		return shortenedId;
	}

	@Nonnull
	@Override
	public List<String> getPath() {
		return path;
	}

	public PlayerConfigOptionValueType<T> getValueType() {
		return valueType;
	}

	@Nonnull
	@Override
	public Class<T> getType() {
		return valueType.getJType();
	}
	
	@Nonnull
	@Override
	public String getTranslation() {
		return translation;
	}

	@Nonnull
	public String[] getTranslationArgs() {
		return translationArgs;
	}

	@Nonnull
	public String getCommentTranslation() {
		return commentTranslation;
	}

	@Nonnull
	public String[] getCommentTranslationArgs() {
		return commentTranslationArgs;
	}

	@Nonnull
	@Override
	public String getComment() {
		return comment;
	}

	@Nonnull
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Nonnull
	@Override
	public BiPredicate<IPlayerConfigAPI, T> getServerSideValidator() {
		return serverSideValidatorAPI;
	}

	@Nonnull
	@Override
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getClientSideValidator() {
		return clientSideValidatorAPI;
	}

	public BiPredicate<PlayerConfig<?>, T> getServerSideValidatorInternal() {
		return serverSideValidator;
	}

	public BiPredicate<PlayerConfigClientStorage, T> getClientSideValidatorInternal() {
		return clientSideValidator;
	}

	@Nullable
	@Override
	public String getTooltipPrefix() {
		return tooltipPrefix;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", id, getType());
	}
	
	@Nonnull
	@Override
	public Function<String, T> getCommandInputParser() {
		return valueType.getStringParser();
	}

	@Deprecated
	@Nonnull
	@Override
	public Function<T, Component> getCommandOutputWriter() {
		return getComponentWriter();
	}

	@Nonnull
	@Override
	public Function<T, Component> getComponentWriter() {
		return valueType.getComponentWriter();
	}

	@SuppressWarnings("unchecked")
	public Function<Object, Component> getComponentWriterCast() {
		return (Function<Object, Component>) valueType.getComponentWriter();
	}

	@SuppressWarnings("unchecked")
	public Function<Object, String> getStringWriterCast() {
		return (Function<Object, String>) valueType.getStringWriter();
	}

	@Override
	@Nonnull
	public Predicate<PlayerConfigType> getConfigTypeFilter() {
		return configTypeFilter;
	}

	public BiFunction<PlayerConfig<?>, T, T> getDefaultReplacer() {
		return defaultReplacer;
	}

	public ClientboundPlayerConfigDynamicOptionsPacket.OptionType getSyncOptionType() {
		return syncOptionType;
	}

	public PlayerConfigOptionCategory getCategory() {
		return category;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public boolean isOverridable() {
		return overridable;
	}

	public boolean isForcedPlayerConfigurable() {
		return forcedPlayerConfigurable;
	}

	public IPlayerConfigChangeHandler<T> getServerChangeHandler() {
		return serverChangeHandler;
	}

	public abstract static class Builder<T, B extends Builder<T, B>> {
		
		protected final B self;
		protected final PlayerConfigOptionValueType<T> valueType;
		protected String id;
		protected T defaultValue;
		protected BiFunction<PlayerConfig<?>, T, T> defaultReplacer;
		protected String comment;
		protected String translation;
		protected String[] translationArgs;
		protected String commentTranslation;
		protected String[] commentTranslationArgs;
		protected PlayerConfigOptionCategory category;
		protected BiPredicate<PlayerConfig<?>, T> serverSideValidator;
		protected BiPredicate<PlayerConfigClientStorage, T> clientSideValidator;
		private Predicate<T> valueValidator;
		protected String tooltipPrefix;
		protected Predicate<PlayerConfigType> configTypeFilter;
		protected boolean dynamic;
		protected boolean overridable;
		protected boolean forcedPlayerConfigurable;
		protected IPlayerConfigChangeHandler<T> serverChangeHandler;
		
		@SuppressWarnings("unchecked")
		protected Builder(PlayerConfigOptionValueType<T> valueType){
			this.self = (B) this;
			this.valueType = valueType;
		}
		
		public B setDefault(){
			setId(null);
			setDefaultValue(null);
			setDefaultReplacer(null);
			setComment(null);
			setTranslation(null);
			setCommentTranslation(null);
			setCategory(null);
			setValueValidator(null);
			setClientSideValidator(null);
			setServerSideValidator(null);
			setTooltipPrefix(null);
			setConfigTypeFilter(t -> true);
			setDynamic(false);
			setOverridable(true);
			setForcedPlayerConfigurable(false);
			setServerChangeHandler(null);
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
		
		public B setTranslation(String translation, String... translationArgs) {
			this.translation = translation;
			this.translationArgs = translationArgs;
			return self;
		}

		public B setCommentTranslation(String translation, String... translationArgs) {
			this.commentTranslation = translation;
			this.commentTranslationArgs = translationArgs;
			return self;
		}

		public B setCategory(PlayerConfigOptionCategory category) {
			this.category = category;
			return self;
		}

		public B setValueValidator(Predicate<T> valueValidator) {
			this.valueValidator = valueValidator;
			return self;
		}

		public B setClientSideValidator(BiPredicate<PlayerConfigClientStorage, T> clientSideValidator) {
			this.clientSideValidator = clientSideValidator;
			return self;
		}

		public B setServerSideValidator(BiPredicate<PlayerConfig<?>, T> serverSideValidator) {
			this.serverSideValidator = serverSideValidator;
			return self;
		}

		public B setTooltipPrefix(String tooltipPrefix) {
			this.tooltipPrefix = tooltipPrefix;
			return self;
		}

		public B setConfigTypeFilter(Predicate<PlayerConfigType> configTypeFilter) {
			this.configTypeFilter = configTypeFilter;
			return self;
		}

		public B setDynamic(boolean dynamic) {
			this.dynamic = dynamic;
			return self;
		}

		public B setOverridable(boolean overridable) {
			this.overridable = overridable;
			return self;
		}

		public B setForcedPlayerConfigurable(boolean forcedPlayerConfigurable) {
			this.forcedPlayerConfigurable = forcedPlayerConfigurable;
			return self;
		}

		public B setServerChangeHandler(IPlayerConfigChangeHandler<T> serverChangeHandler) {
			this.serverChangeHandler = serverChangeHandler;
			return self;
		}

		protected Predicate<T> buildValueValidator() {
			if(valueValidator == null) {
				if(valueType.getJType() == String.class)
					throw new IllegalStateException();
				return v -> true;
			}
			return valueValidator;
		}

		public BiPredicate<PlayerConfig<?>, T> buildServerSideValidator() {
			Predicate<T> finalValueValidator = valueValidator;
			if(serverSideValidator == null)
				return (c, v) -> finalValueValidator.test(v);
			return serverSideValidator;
		}

		public BiPredicate<PlayerConfigClientStorage, T> buildClientSideValidator() {
			Predicate<T> finalValueValidator = valueValidator;
			if(clientSideValidator == null)
				return (c, v) -> finalValueValidator.test(v);
			return clientSideValidator;
		}

		public PlayerConfigOptionSpec<T> build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(id == null || defaultValue == null || comment == null || configTypeFilter == null || category == null)
				throw new IllegalStateException();
			if(translation == null)
				setTranslation("gui.xaero_pac_player_config_" + id);
			if(commentTranslation == null)
				setCommentTranslation("gui.xaero_pac_player_config_tooltip_" + id);
			valueValidator = buildValueValidator();
			serverSideValidator = buildServerSideValidator();
			clientSideValidator = buildClientSideValidator();
			PlayerConfigOptionSpec<T> spec = buildInternally(
					Collections.unmodifiableList(StringUtils.split(id, '.')),
					id.substring(PlayerConfig.PLAYER_CONFIG_ROOT_DOT.length())
			);
			if(dest != null)
				dest.put(spec.getId(), spec);
			return spec;
		}
		
		protected abstract PlayerConfigOptionSpec<T> buildInternally(List<String> path, String shortenedId);
		
	}
	
	public static final class FinalBuilder<T> extends Builder<T, FinalBuilder<T>> {
		
		protected FinalBuilder(PlayerConfigOptionValueType<T> valueType) {
			super(valueType);
		}

		@Override
		protected PlayerConfigOptionSpec<T> buildInternally(List<String> path, String shortenedId){
			return new PlayerConfigOptionSpec<>(
					valueType, id, shortenedId, path, defaultValue,
					defaultReplacer, comment, translation, translationArgs,
					commentTranslation, commentTranslationArgs, category,
					serverSideValidator, clientSideValidator, tooltipPrefix,
					configTypeFilter, ClientboundPlayerConfigDynamicOptionsPacket.OptionType.DEFAULT,
					dynamic, overridable, forcedPlayerConfigurable, serverChangeHandler
			);
		}
		
		public static <T> FinalBuilder<T> begin(PlayerConfigOptionValueType<T> valueType){
			return new FinalBuilder<>(valueType).setDefault();
		}
		
	}
	
}
