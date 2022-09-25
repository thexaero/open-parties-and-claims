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

package xaero.pac.client.player.config;

import com.google.common.collect.Lists;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.client.player.config.sub.PlayerSubConfigClientStorage;
import xaero.pac.common.list.SortedValueList;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerConfigClientStorage implements IPlayerConfigClientStorage<PlayerConfigStringableOptionClientStorage<?>> {
	
	private final PlayerConfigType type;
	private final UUID owner;
	private final Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options;
	private final List<String> subConfigIdsUnmodifiable;
	private final SortedValueList<String> subConfigIds;
	private String selectedSubConfig;
	private final Map<String, PlayerSubConfigClientStorage> subConfigs;
	private boolean syncInProgress;
	private boolean beingDeleted;
	private int subConfigLimit;

	protected PlayerConfigClientStorage(PlayerConfigType type, UUID owner, Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options, List<String> subConfigIdsUnmodifiable, SortedValueList<String> subConfigIds, Map<String, PlayerSubConfigClientStorage> subConfigs) {
		super();
		this.type = type;
		this.owner = owner;
		this.options = options;
		this.subConfigIdsUnmodifiable = subConfigIdsUnmodifiable;
		this.subConfigIds = subConfigIds;
		this.subConfigs = subConfigs;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Comparable<T>> PlayerConfigStringableOptionClientStorage<T> getOptionStorage(@Nonnull IPlayerConfigOptionSpecAPI<T> option){
		return (PlayerConfigStringableOptionClientStorage<T>) options.get(option);
	}
	
	@Nonnull
	@Override
	public PlayerConfigType getType() {
		return type;
	}

	@Nullable
	@Override
	public UUID getOwner() {
		return owner;
	}

	@Nonnull
	@Override
	public Stream<PlayerConfigStringableOptionClientStorage<?>> optionStream(){
		return options.values().stream();
	}

	@Override
	public int size() {
		return options.size();
	}

	@Override
	public PlayerSubConfigClientStorage getOrCreateSubConfig(String subId){
		PlayerSubConfigClientStorage result = subConfigs.get(subId);
		if(result == null){
			result = PlayerSubConfigClientStorage.Builder.begin(LinkedHashMap::new)
					.setSubID(subId)
					.setOwner(owner)
					.setType(type).build();
			subConfigs.put(subId, result);
			subConfigIds.add(subId);
		}
		return result;
	}

	@Override
	public void removeSubConfig(String subId){
		PlayerSubConfigClientStorage removed = subConfigs.remove(subId);
		if(removed != null) {
			removed.setBeingDeleted(false);
			subConfigIds.remove(subId);
		}
	}

	@Nonnull
	public List<String> getSubConfigIds() {
		return subConfigIdsUnmodifiable;
	}

	@Nullable
	@Override
	public PlayerSubConfigClientStorage getSubConfig(@Nonnull String id) {
		return subConfigs.get(id);
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getEffectiveSubConfig(@Nonnull String id) {
		if(PlayerConfig.MAIN_SUB_ID.equals(id))
			return this;
		PlayerSubConfigClientStorage sub = getSubConfig(id);
		return sub == null ? this : sub;
	}

	@Override
	public boolean subConfigExists(@Nonnull String id) {
		return subConfigs.containsKey(id);
	}

	@Override
	public int getSubCount() {
		return subConfigs.size();
	}

	@Nonnull
	public Stream<IPlayerConfigClientStorageAPI<PlayerConfigStringableOptionClientStorage<?>>> getSubConfigAPIStream(){
		return getSubConfigStream().map(Function.identity());
	}

	@Override
	public Stream<IPlayerConfigClientStorage<PlayerConfigStringableOptionClientStorage<?>>> getSubConfigStream() {
		return subConfigs.values().stream().map(Function.identity());
	}

	@Override
	public void setSelectedSubConfig(String selectedSubConfig) {
		this.selectedSubConfig = selectedSubConfig;
	}

	@Override
	public String getSelectedSubConfig() {
		return selectedSubConfig;
	}

	public boolean isSubConfigSelected(){
		return selectedSubConfig != null && !selectedSubConfig.equals(PlayerConfig.MAIN_SUB_ID) && subConfigExists(selectedSubConfig);
	}

	@Override
	public void reset() {
		options.forEach((k, os) -> os.setValue(null));
		selectedSubConfig = null;
		if(subConfigs != null) {
			subConfigIds.clear();
			subConfigIds.add(PlayerConfig.MAIN_SUB_ID);
			subConfigs.clear();
			syncInProgress = true;
		}
	}

	public String getSubId(){
		return null;
	}

	@Override
	public boolean isSyncInProgress() {
		return syncInProgress;
	}

	@Override
	public void setSyncInProgress(boolean syncInProgress) {
		this.syncInProgress = syncInProgress;
	}

	@Override
	public void setGeneralState(boolean beingDeleted, int subConfigLimit) {
		this.beingDeleted = beingDeleted;
		this.subConfigLimit = subConfigLimit;
	}

	@Override
	public boolean isBeingDeleted() {
		return beingDeleted;
	}

	public void setBeingDeleted(boolean beingDeleted) {
		this.beingDeleted = beingDeleted;
	}

	@Override
	public int getSubConfigLimit() {
		if(type == PlayerConfigType.SERVER)
			return Integer.MAX_VALUE;
		return subConfigLimit;
	}

	public static abstract class Builder<B extends Builder<B>> implements IBuilder<PlayerConfigClientStorage> {

		protected final B self;
		protected PlayerConfigType type;
		protected UUID owner;
		protected final MapFactory mapFactory;

		@SuppressWarnings("unchecked")
		protected Builder(MapFactory mapFactory) {
			super();
			this.self = (B) this;
			this.mapFactory = mapFactory;
		}
		
		@Override
		public B setDefault() {
			setOwner(null);
			setType(null);
			return self;
		}

		@Override
		public B setType(PlayerConfigType type) {
			this.type = type;
			return self;
		}

		@Override
		public B setOwner(UUID owner) {
			this.owner = owner;
			return self;
		}

		protected abstract <T extends Comparable<T>> T getDefaultValue(PlayerConfigOptionSpec<T> option);

		protected <T extends Comparable<T>> PlayerConfigStringableOptionClientStorage<T> buildOptionStorage(PlayerConfigOptionSpec<T> option){
			PlayerConfigStringableOptionClientStorage.Builder<T> builder = PlayerConfigStringableOptionClientStorage.Builder.begin();
			builder.setOption(option).setValue(getDefaultValue(option));
			return builder.build();
		}

		public PlayerConfigClientStorage build() {
			if(type == null)
				throw new IllegalStateException();
			Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options = mapFactory.get();
			PlayerConfigOptions.OPTIONS.forEach((k, option) -> {
				options.put((PlayerConfigOptionSpec<?>)option, buildOptionStorage((PlayerConfigOptionSpec<?>)option));
			});
			return buildInternally(options);
		}

		protected abstract PlayerConfigClientStorage buildInternally(Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options);
		
	}

	public static final class FinalBuilder extends Builder<FinalBuilder> {

		private FinalBuilder(MapFactory mapFactory) {
			super(mapFactory);
		}

		@Override
		protected <T extends Comparable<T>> T getDefaultValue(PlayerConfigOptionSpec<T> option) {
			return option.getDefaultValue();
		}

		@Override
		protected PlayerConfigClientStorage buildInternally(Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options) {
			List<String> subConfigIdsStorage = Lists.newArrayList(PlayerConfig.MAIN_SUB_ID);
			List<String> subConfigIdsUnmodifiable = Collections.unmodifiableList(subConfigIdsStorage);
			SortedValueList<String> subConfigIds = SortedValueList.Builder.<String>begin()
					.setContent(subConfigIdsStorage)
					.build();
			return new PlayerConfigClientStorage(type, owner, options, subConfigIdsUnmodifiable, subConfigIds, mapFactory.get());
		}

		public static FinalBuilder begin(MapFactory mapFactory) {
			return new FinalBuilder(mapFactory).setDefault();
		}

	}

}
