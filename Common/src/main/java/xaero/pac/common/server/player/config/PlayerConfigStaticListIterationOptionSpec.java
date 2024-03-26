/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

public final class PlayerConfigStaticListIterationOptionSpec<T extends Comparable<T>> extends PlayerConfigListIterationOptionSpec<T> {

	private final List<T> list;

	private PlayerConfigStaticListIterationOptionSpec(Class<T> type, String id, String shortenedId, List<String> path, T defaultValue, BiFunction<PlayerConfig<?>, T, T> defaultReplacer, String comment, String translation,
													  String[] translationArgs, String commentTranslation, String[] commentTranslationArgs, PlayerConfigOptionCategory category, Function<String, T> commandInputParser, Function<T, Component> commandOutputWriter,
													  BiPredicate<PlayerConfig<?>, T> serverSideValidator, BiPredicate<PlayerConfigClientStorage, T> clientSideValidator, String tooltipPrefix, Predicate<PlayerConfigType> configTypeFilter,
													  Function<PlayerConfig<?>, List<T>> serverSideListGetter, Function<PlayerConfigClientStorage, List<T>> clientSideListGetter, List<T> list, ClientboundPlayerConfigDynamicOptionsPacket.OptionType syncOptionType, boolean dynamic) {
		super(type, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation, translationArgs, commentTranslation, commentTranslationArgs, category, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter, serverSideListGetter, clientSideListGetter, syncOptionType, dynamic);
		this.list = list;
	}

	public List<T> getList() {
		return list;
	}

	public static final class Builder<T extends Comparable<T>> extends PlayerConfigListIterationOptionSpec.Builder<T, Builder<T>> {

		private List<T> list;

		private Builder(Class<T> valueType) {
			super(valueType);
		}

		@Override
		public Builder<T> setDefault() {
			super.setDefault();
			setList(null);
			return self;
		}

		public Builder<T> setList(List<T> list) {
			this.list = list;
			return self;
		}

		@Override
		public PlayerConfigStaticListIterationOptionSpec<T> build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(list == null)
				throw new IllegalStateException();
			setServerSideListGetter(c -> list);
			setClientSideListGetter(c -> list);
			return (PlayerConfigStaticListIterationOptionSpec<T>) super.build(dest);
		}

		@Override
		protected PlayerConfigListIterationOptionSpec<T> buildInternally(List<String> path, String shortenedId, Function<String, T> commandInputParser) {
			return new PlayerConfigStaticListIterationOptionSpec<>(type, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation, translationArgs, commentTranslation, commentTranslationArgs, category, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter, serverSideListGetter, clientSideListGetter, list, ClientboundPlayerConfigDynamicOptionsPacket.OptionType.STATIC_LIST, dynamic);
		}

		public static <T extends Comparable<T>> Builder<T> begin(Class<T> valueType){
			return new Builder<>(valueType).setDefault();
		}

	}

}
