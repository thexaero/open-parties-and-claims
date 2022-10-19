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

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.ClientboundPlayerConfigDynamicOptionsPacket;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlayerConfigListIterationOptionSpec<T extends Comparable<T>> extends PlayerConfigOptionSpec<T> {

	private final Function<PlayerConfig<?>, List<T>> serverSideListGetter;
	private final Function<PlayerConfigClientStorage, List<T>> clientSideListGetter;

	protected PlayerConfigListIterationOptionSpec(Class<T> type, String id, String shortenedId, List<String> path, T defaultValue, BiFunction<PlayerConfig<?>, T, T> defaultReplacer, String comment, String translation, String[] translationArgs, String commentTranslation, String[] commentTranslationArgs, Function<String, T> commandInputParser, Function<T, Component> commandOutputWriter, BiPredicate<PlayerConfig<?>, T> serverSideValidator, BiPredicate<PlayerConfigClientStorage, T> clientSideValidator, String tooltipPrefix, Predicate<PlayerConfigType> configTypeFilter, Function<PlayerConfig<?>, List<T>> serverSideListGetter,
												  Function<PlayerConfigClientStorage, List<T>> clientSideListGetter, ClientboundPlayerConfigDynamicOptionsPacket.OptionType syncOptionType) {
		super(type, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation, translationArgs, commentTranslation, commentTranslationArgs, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter, syncOptionType);
		this.serverSideListGetter = serverSideListGetter;
		this.clientSideListGetter = clientSideListGetter;
	}

	public Function<PlayerConfig<?>, List<T>> getServerSideListGetter() {
		return serverSideListGetter;
	}

	public Function<PlayerConfigClientStorage, List<T>> getClientSideListGetter() {
		return clientSideListGetter;
	}

	abstract static class Builder<T extends Comparable<T>, B extends Builder<T,B>> extends PlayerConfigOptionSpec.Builder<T, B> {

		protected Function<PlayerConfig<?>, List<T>> serverSideListGetter;
		protected Function<PlayerConfigClientStorage, List<T>> clientSideListGetter;

		protected Builder(Class<T> type) {
			super(type);
		}

		@Override
		public B setDefault() {
			setServerSideListGetter(null);
			setClientSideListGetter(null);
			return super.setDefault();
		}

		public B setServerSideListGetter(Function<PlayerConfig<?>, List<T>> serverSideListGetter) {
			this.serverSideListGetter = serverSideListGetter;
			return self;
		}

		public B setClientSideListGetter(Function<PlayerConfigClientStorage, List<T>> clientSideListGetter) {
			this.clientSideListGetter = clientSideListGetter;
			return self;
		}

		@Override
		public BiPredicate<PlayerConfig<?>, T> buildServerSideValidator() {
			BiPredicate<PlayerConfig<?>, T> baseValidator = super.buildServerSideValidator();
			return (c, v) -> baseValidator.test(c, v) && serverSideListGetter.apply(c).contains(v);
		}

		@Override
		public BiPredicate<PlayerConfigClientStorage, T> buildClientSideValidator() {
			BiPredicate<PlayerConfigClientStorage, T> baseValidator = super.buildClientSideValidator();
			return (c, v) -> baseValidator.test(c, v) && clientSideListGetter.apply(c).contains(v);
		}

		@Override
		public PlayerConfigListIterationOptionSpec<T> build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(serverSideListGetter == null || clientSideListGetter == null)
				throw new IllegalStateException();
			return (PlayerConfigListIterationOptionSpec<T>) super.build(dest);
		}

		protected abstract PlayerConfigListIterationOptionSpec<T> buildInternally(List<String> path, String shortenedId, Function<String, T> commandInputParser);

	}

	public static final class FinalBuilder<T extends Comparable<T>> extends Builder<T, FinalBuilder<T>> {

		private FinalBuilder(Class<T> type) {
			super(type);
		}

		@Override
		protected PlayerConfigListIterationOptionSpec<T> buildInternally(List<String> path, String shortenedId, Function<String, T> commandInputParser) {
			return new PlayerConfigListIterationOptionSpec<>(type, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation, translationArgs, commentTranslation, commentTranslationArgs, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter, serverSideListGetter, clientSideListGetter, ClientboundPlayerConfigDynamicOptionsPacket.OptionType.UNSYNCABLE);
		}

		public static <T extends Comparable<T>> FinalBuilder<T> begin(Class<T> type){
			return new FinalBuilder<>(type).setDefault();
		}

	}

}
