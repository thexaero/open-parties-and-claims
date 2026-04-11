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

package xaero.pac.common.server.player.config.backwards.v1;

import com.electronwill.nightconfig.core.utils.StringUtils;
import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionValueType;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@Deprecated
public class CompatPlayerConfigOptionSpec<T extends Comparable<T>, R> implements IPlayerConfigOptionSpecAPI<T> {

	protected final String[] commentArgs = new String[0];
	protected final String id;
	protected final String shortenedId;
	private final List<String> path;
	private final String translation;
	private final String[] translationArgs;
	protected final PlayerConfigOptionValueType<T> valueType;

	private final BiPredicate<IPlayerConfigAPI, T> serverSideValidatorAPI;
	private final BiPredicate<IPlayerConfigClientStorageAPI, T> clientSideValidatorAPI;
	public final xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption;
	public final BiFunction<T, R, R> toRealConverter;
	public final Function<R, T> fromRealConverter;

	protected CompatPlayerConfigOptionSpec(
			PlayerConfigOptionValueType<T> valueType,
			String id,
			String shortenedId,
			List<String> path,
			String translation,
			String[] translationArgs,
			BiPredicate<PlayerConfig<?>, T> serverSideValidator,
			BiPredicate<PlayerConfigClientStorage, T> clientSideValidator,
			xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption,
			BiFunction<T, R, R> toRealConverter,
			Function<R, T> fromRealConverter
	) {
		super();
		this.valueType = valueType;
		this.id = id;
		this.shortenedId = shortenedId;
		this.path = path;
		this.translation = translation;
		this.translationArgs = translationArgs;
		this.serverSideValidatorAPI = (c,v) -> serverSideValidator.test((PlayerConfig<?>) c, v);
		this.clientSideValidatorAPI = (c,v) -> clientSideValidator.test((PlayerConfigClientStorage) c, v);
		this.realOption = realOption;
		this.toRealConverter = toRealConverter;
		this.fromRealConverter = fromRealConverter;
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
		return "";
	}

	@Nonnull
	public String[] getCommentTranslationArgs() {
		return commentArgs;
	}

	@Nonnull
	@Override
	public String getComment() {
		return "Deprecated Option API";
	}

	@Nonnull
	@Override
	public T getDefaultValue() {
		R actualValue = realOption == null ? null : realOption.getDefaultValue();
		return fromRealConverter.apply(actualValue);
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

	@Nullable
	@Override
	public String getTooltipPrefix() {
		return null;
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
		return valueType.getComponentWriter();
	}

	@Override
	@Nonnull
	public Predicate<PlayerConfigType> getConfigTypeFilter() {
		if(realOption == null)
			return t -> false;
		return realOption.getConfigTypeFilter();
	}

	public final static class Builder<T extends Comparable<T>, R> {
		
		private final Builder<T, R> self;
		private final PlayerConfigOptionValueType<T> valueType;
		private String id;
		private String translation;
		private String[] translationArgs;
		private xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption;
		private BiFunction<T, R, R> toRealConverter;
		private Function<R, T> fromRealConverter;
		
		@SuppressWarnings("unchecked")
		protected Builder(PlayerConfigOptionValueType<T> valueType){
			this.self = this;
			this.valueType = valueType;
		}
		
		public Builder<T, R> setDefault(){
			setId(null);
			setTranslation(null);
			return self;
		}

		public Builder<T, R> setRealOption(xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption) {
			this.realOption = realOption;
			return self;
		}

		public Builder<T, R> setToRealConverter(BiFunction<T, R, R> toRealConverter) {
			this.toRealConverter = toRealConverter;
			return self;
		}

		public Builder<T, R> setFromRealConverter(Function<R, T> fromRealConverter) {
			this.fromRealConverter = fromRealConverter;
			return self;
		}

		public Builder<T, R> setId(String id) {
			this.id = id;
			return self;
		}
		
		public Builder<T, R> setTranslation(String translation, String... translationArgs) {
			this.translation = translation;
			this.translationArgs = translationArgs;
			return self;
		}

		public BiPredicate<PlayerConfig<?>, T> buildServerSideValidator() {
			xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = this.realOption;
			if(realOption == null)
				return (c, v) -> true;
			BiFunction<T, R, R> toRealConverter = this.toRealConverter;
			return (c, v) -> {
				if(v == null)
					return false;
				return realOption.getServerSideValidator().test(c, toRealConverter.apply(v, c.getEffective(realOption)));
			};
		}

		public BiPredicate<PlayerConfigClientStorage, T> buildClientSideValidator() {
			xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = this.realOption;
			if(realOption == null)
				return (c, v) -> true;
			BiFunction<T, R, R> toRealConverter = this.toRealConverter;
			return (c, v) -> {
				if(v == null)
					return false;
				R currentEffectiveReal = c.getOption(realOption).getValue();
				if(currentEffectiveReal == null)
					currentEffectiveReal = c.getMain().getOption(realOption).getValue();
				if(currentEffectiveReal == null)
					currentEffectiveReal = realOption.getDefaultValue();
				return realOption.getClientSideValidator().test(c, toRealConverter.apply(v, currentEffectiveReal));
			};
		}

		public CompatPlayerConfigOptionSpec<T, R> build(Map<String, CompatPlayerConfigOptionSpec<?, ?>> dest) {
			if(id == null || toRealConverter == null || fromRealConverter == null)
				throw new IllegalStateException();
			if(translation == null)
				setTranslation("gui.xaero_pac_config_deprecated_api_option");
			List<String> path = Collections.unmodifiableList(StringUtils.split(id, '.'));
			String shortenedId = id.substring(PlayerConfig.PLAYER_CONFIG_ROOT_DOT.length());
			CompatPlayerConfigOptionSpec<T, R> spec = new CompatPlayerConfigOptionSpec<>(
					valueType,
					id,
					shortenedId,
					path,
					translation,
					translationArgs,
					buildServerSideValidator(),
					buildClientSideValidator(),
					realOption,
					toRealConverter,
					fromRealConverter
			);
			if(dest != null)
				dest.put(spec.getId(), spec);
			return spec;
		}

		public static <T extends Comparable<T>, R> Builder<T, R> begin(PlayerConfigOptionValueType<T> valueType){
			return new Builder<T, R>(valueType).setDefault();
		}
		
	}


	
}
