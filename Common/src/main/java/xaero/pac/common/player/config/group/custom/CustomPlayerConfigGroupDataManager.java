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

package xaero.pac.common.player.config.group.custom;

import com.mojang.datafixers.util.Either;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.api.ICustomPlayerConfigGroupDataManagerAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.BuiltInPlayerConfigGroups;
import xaero.pac.common.server.player.config.group.IPlayerConfigGroup;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class CustomPlayerConfigGroupDataManager<G extends ICustomPlayerConfigGroupData> implements ICustomPlayerConfigGroupDataManagerAPI {

	protected final PlayerConfigType configType;
	protected final Map<String, G> customGroups;
	protected final Map<String, G> customGroupsUnmodifiable;
	protected final Map<String, String> customGroupIdCaseCache;
	private List<String> allGroupIdsSortedCache;
	private int defaultConfigInvalidationCount;
	private int usedSpace;

	protected CustomPlayerConfigGroupDataManager(
			PlayerConfigType configType,
			Map<String, G> customGroups,
			Map<String, G> customGroupsUnmodifiable,
			Map<String, String> customGroupIdCaseCache
	) {
		this.configType = configType;
		this.customGroups = customGroups;
		this.customGroupsUnmodifiable = customGroupsUnmodifiable;
		this.customGroupIdCaseCache = customGroupIdCaseCache;
	}

	protected G getData(String id){
		String idLowerCase = id.toLowerCase();
		String cachedId = customGroupIdCaseCache.get(idLowerCase);
		if(cachedId == null)
			return null;
		return customGroups.get(cachedId);
	}

	public CustomPlayerConfigGroupData getDataCopy(String id){
		G actual = getData(id);
		if(actual == null)
			return null;
		return actual.copyData();
	}

	@Nonnull
	public Set<String> getIds() {
		return customGroupsUnmodifiable.keySet();
	}

	public Either<G, PlayerConfigGroupActionError> addData(String id){
		if(id.length() > PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH)
			return Either.right(PlayerConfigGroupActionError.GROUP_ID_TOO_LONG);
		if(!CustomPlayerConfigGroupData.isValidId(id))
			return Either.right(PlayerConfigGroupActionError.INVALID_GROUP_ID);
		String idLowerCase = id.toLowerCase();
		if(customGroupIdCaseCache.containsKey(idLowerCase))
			return Either.right(PlayerConfigGroupActionError.GROUP_ALREADY_EXISTS);
		G result = construct(id);
		customGroups.put(id, result);
		customGroupIdCaseCache.put(idLowerCase, id);
		invalidateCache();
		return Either.left(result);
	}

	public Optional<PlayerConfigGroupActionError> removeData(String id){
		if(id.length() > PlayerConfigConstants.MAX_CUSTOM_PLAYER_GROUP_ID_LENGTH)
			return Optional.of(PlayerConfigGroupActionError.GROUP_ID_TOO_LONG);
		if(!CustomPlayerConfigGroupData.isValidId(id))
			return Optional.of(PlayerConfigGroupActionError.INVALID_GROUP_ID);
		String idLowerCase = id.toLowerCase();
		String idCachedCase = customGroupIdCaseCache.remove(idLowerCase);
		if(idCachedCase == null)
			return Optional.of(PlayerConfigGroupActionError.GROUP_TO_REMOVE_NOT_FOUND);
		G removedGroup = customGroups.remove(idCachedCase);
		onRemoved(removedGroup);
		usedSpace -= removedGroup.getSize();
		invalidateCacheContaining(removedGroup);
		return Optional.empty();
	}

	public void invalidateCache(){
		//for built-in groups, cache is permanent because they can't even indirectly reference custom groups
		//this means that their group lookup can't change, so it's pointless to rebuild it
		allGroupIdsSortedCache = null;
		CustomPlayerConfigGroupDataManager<G> defaultGroups = getDefaultConfigGroups();
		if(this == defaultGroups)
			defaultConfigInvalidationCount++;
	}

	public void invalidateCacheContaining(G group) {
		invalidateCache();
	}

	public boolean detectDefaultConfigGroupsInvalidation(){
		//detects when cache-invalidating changes have occurred in the default config's custom player groups
		//which can be referenced by other player configs' groups, so cache invalidation is required
		//everywhere, not just the default config
		CustomPlayerConfigGroupDataManager<G> defaultConfigGroups = getDefaultConfigGroups();
		if(defaultConfigGroups == this)
			return false;
		int currentCount = defaultConfigGroups.defaultConfigInvalidationCount;
		if(defaultConfigInvalidationCount == currentCount)
			return false;
		invalidateCache();
		defaultConfigInvalidationCount = currentCount;
		return true;
	}

	@Override
	@Nonnull
	public List<String> getAllIdsSorted(){
		detectDefaultConfigGroupsInvalidation();
		if(allGroupIdsSortedCache == null){
			Set<String> cacheBuilder = new HashSet<>();
			CustomPlayerConfigGroupDataManager<G> defaultConfigGroups = getDefaultConfigGroups();
			if(defaultConfigGroups == this) {
				//default config must support all groups, so supportsConfigType isn't checked
				BuiltInPlayerConfigGroups.forEach(g -> cacheBuilder.add(g.getId()));
			} else {
				for (String defaultGroupId : defaultConfigGroups.getAllIdsSorted()) {
					IPlayerConfigGroup builtInGroup = BuiltInPlayerConfigGroups.get(defaultGroupId);
					if(builtInGroup == null || builtInGroup.supportsConfigType(configType))
						cacheBuilder.add(defaultGroupId);
				}
			}
			cacheBuilder.addAll(customGroups.keySet());
			allGroupIdsSortedCache = cacheBuilder.stream().sorted(Comparator.comparing(String::toLowerCase)).toList();
		}
		return allGroupIdsSortedCache;
	}

	@Override
	public boolean dataExists(@Nonnull String id) {
		return getData(id) != null;
	}

	public void reset() {
		invalidateCache();
		if(customGroups != null) {
			customGroups.clear();
			customGroupIdCaseCache.clear();
		}
		usedSpace = 0;
	}

	public void incrementUsedSpace(){
		usedSpace++;
	}

	public void decrementUsedSpace(){
		usedSpace--;
	}

	public int getUsedSpace() {
		return usedSpace;
	}

	public int getCustomGroupCount(){
		return customGroups.size();
	}

	@Override
	public abstract int getMaxGroups();

	@Override
	public abstract int getGroupSpace();

	protected abstract G construct(String id);

	protected abstract CustomPlayerConfigGroupDataManager<G> getDefaultConfigGroups();

	protected abstract void onRemoved(G removedGroup);

	public static abstract class Builder<G extends ICustomPlayerConfigGroupData, B extends Builder<G, B>> {

		protected final B self;
		protected PlayerConfigType configType;

		@SuppressWarnings("unchecked")
		protected Builder(){
			self = (B)this;
		}

		public B setDefault(){
			setConfigType(null);
			return self;
		}

		public B setConfigType(PlayerConfigType configType) {
			this.configType = configType;
			return self;
		}

		public CustomPlayerConfigGroupDataManager<G> build(){
			if(configType == null)
				throw new IllegalStateException();
			return buildInternally();
		}

		protected abstract CustomPlayerConfigGroupDataManager<G> buildInternally();

	}

}
