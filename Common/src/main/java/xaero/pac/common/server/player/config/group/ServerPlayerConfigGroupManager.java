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

package xaero.pac.common.server.player.config.group;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupDataManager;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.config.group.custom.CustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.group.custom.io.PlayerConfigGroupManagerIO;
import xaero.pac.common.server.player.permission.PermissionNode;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;
import xaero.pac.common.server.player.permission.util.PermissionUtils;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import java.util.*;

public class ServerPlayerConfigGroupManager extends CustomPlayerConfigGroupDataManager<CustomPlayerConfigGroup> implements IServerPlayerConfigGroupManager {

	private final PlayerConfig<?> config;
	private LinkedChain<CustomPlayerConfigGroup> customGroupsChain;
	private final Map<String, DefaultPlayerConfigGroupWrapper> defaultConfigGroupWrappers;
	private PlayerConfigGroupManagerIO io;
	private boolean saveNeeded;
	private boolean loaded;

	public ServerPlayerConfigGroupManager(
			PlayerConfig<?> config,
			Map<String, CustomPlayerConfigGroup> customGroups,
			Map<String, CustomPlayerConfigGroup> customGroupsUnmodifiable,
			LinkedChain<CustomPlayerConfigGroup> customGroupsChain,
			Map<String, String> customGroupIdCaseCache,
			Map<String, DefaultPlayerConfigGroupWrapper> defaultConfigGroupWrappers
	) {
		super(config.getType(), customGroups, customGroupsUnmodifiable, customGroupIdCaseCache);
		this.customGroupsChain = customGroupsChain;
		this.config = config;
		this.defaultConfigGroupWrappers = defaultConfigGroupWrappers;
	}

	private void setIo(PlayerConfigGroupManagerIO io) {
		if (this.io != null)
			throw new IllegalStateException();
		this.io = io;
	}

	@SuppressWarnings("unchecked")
	public Iterable<ICustomPlayerConfigGroup> getAllCustom() {
		return (Iterable<ICustomPlayerConfigGroup>) (Object) customGroupsChain;
	}

	public ICustomPlayerConfigGroup getCustom(@Nonnull String id) {
		String idLowerCase = id.toLowerCase();
		String idCaseCache = customGroupIdCaseCache.get(idLowerCase);
		if (idCaseCache == null)
			return null;
		return customGroups.get(idCaseCache);
	}

	public IPlayerConfigGroup getBuiltIn(String id) {
		return BuiltInPlayerConfigGroups.get(id);
	}

	public IPlayerConfigGroup get(@Nonnull String id) {
		return get(id, true);
	}

	public IPlayerConfigGroup getUnwrapped(@Nonnull String id) {
		return get(id, false);
	}

	public IPlayerConfigGroup get(String id, boolean useDefaultGroupWrappers) {
		IPlayerConfigGroup builtIn = getBuiltIn(id);
		if (builtIn != null)
			return builtIn;
		CustomPlayerConfigGroup localCustomGroup = getData(id);
		if (localCustomGroup != null)
			return localCustomGroup;
		IPlayerConfig defaultConfig = config.getManager().getDefaultConfig();
		if (defaultConfig == config)
			return null;
		ICustomPlayerConfigGroup defaultConfigGroup = defaultConfig.getPlayerGroups().getCustom(id);
		if (!useDefaultGroupWrappers)
			return defaultConfigGroup;
		if (defaultConfigGroup == null)
			return null;
		DefaultPlayerConfigGroupWrapper wrapper = defaultConfigGroupWrappers.get(id);
		if (wrapper != null)
			return wrapper;
		defaultConfigGroupWrappers.put(id, wrapper = new DefaultPlayerConfigGroupWrapper(config, defaultConfigGroup));
		return wrapper;
	}

	public Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addOrGetCustom(String id) {
		ICustomPlayerConfigGroup result = getData(id);
		if (result != null)
			return Either.left(result);
		return addData(id).mapBoth(g -> g, e -> e);
	}

	public Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addCustomInternal(String id) {
		return addData(id).mapBoth(g -> g, e -> e);
	}

	@Override
	public Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addCustomLimitedInternal(String id) {
		if (customGroups.size() >= getMaxGroups())
			return Either.right(PlayerConfigGroupActionError.GROUP_COUNT_LIMIT);
		return addCustomInternal(id);
	}

	@Override
	public Either<CustomPlayerConfigGroup, PlayerConfigGroupActionError> addData(String id) {
		return addDataInternal(id, true);
	}

	private Either<CustomPlayerConfigGroup, PlayerConfigGroupActionError> addDataInternal(String id, boolean sync) {
		Either<CustomPlayerConfigGroup, PlayerConfigGroupActionError> result = super.addData(id);
		if (result.left().isPresent()) {
			customGroupsChain.add(result.left().get());
			if (sync && loaded)
				config.getManager().getSynchronizer().syncGroupExistence(null, config, true, id);
			setSaveNeeded();
		}
		return result;
	}

	@Override
	public Either<ICustomPlayerConfigGroup, PlayerConfigGroupActionError> addCustom(String id, boolean sync) {
		return addDataInternal(id, sync).mapBoth(g -> g, e -> e);
	}

	@Nonnull
	@Override
	public Optional<PlayerConfigGroupActionError> removeCustom(@Nonnull String id) {
		return removeData(id);
	}

	@Override
	protected void onRemoved(CustomPlayerConfigGroup removedGroup) {
		customGroupsChain.remove(removedGroup);
		if(loaded)
			config.getManager().getSynchronizer().syncGroupExistence(null, config, false, removedGroup.getId());
		setSaveNeeded();
	}

	public void removeAll(){
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		customGroupsChain.destroy();
		customGroupsChain = new LinkedChain<>();
		if(loaded)
			config.getManager().getSynchronizer().syncGroupsReset(null, config);
		setSaveNeeded();
	}

	public boolean isSaveNeeded() {
		return saveNeeded;
	}

	public void setSaveNeeded() {
		if(!loaded)
			return;
		setResaveNeeded();
	}

	public void setResaveNeeded() {//usable even during loading
		this.saveNeeded = true;
		config.setDirty(true);
	}

	public void confirmSave(){
		this.saveNeeded = false;
	}

	public void confirmLoaded(){
		this.loaded = true;
	}

	@Override
	public void invalidateCache(){
		super.invalidateCache();
		defaultConfigGroupWrappers.values().forEach(CachedPlayerConfigParentGroup::invalidateCache);
		customGroups.values().forEach(CachedPlayerConfigParentGroup::invalidateCache);
	}

	@Override
	public void invalidateCacheContaining(CustomPlayerConfigGroup group) {
		super.invalidateCacheContaining(group);
		//for built-in groups, cache is permanent because they can't even indirectly reference custom groups
		//this means that their group lookup can't change, so it's pointless to rebuild it
		defaultConfigGroupWrappers.values().forEach(g -> g.invalidateCacheContaining(group));
		customGroups.values().forEach(g -> g.invalidateCacheContaining(group));
	}

	@Override
	public boolean detectDefaultConfigGroupsInvalidation(){
		if(super.detectDefaultConfigGroupsInvalidation()) {
			defaultConfigGroupWrappers.clear();
			return true;
		}
		return false;
	}

	@Override
	protected CustomPlayerConfigGroup construct(String id) {
		return new CustomPlayerConfigGroup(config, id);
	}

	@Override
	protected CustomPlayerConfigGroupDataManager<CustomPlayerConfigGroup> getDefaultConfigGroups() {
		if(!config.getManager().isLoaded())
			return null;
		return config.getManager().getDefaultConfig().getPlayerGroups();
	}

	public PlayerConfigGroupManagerIO getIo() {
		return io;
	}

	public PlayerConfig<?> getConfig() {
		return config;
	}

	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public int getMaxGroups() {
		if(configType != PlayerConfigType.PLAYER)
			return Integer.MAX_VALUE;
		return getFullLimit(
				ServerConfig.CONFIG.maxPlayerGroups,
				UsedPermissionNodes.MAX_PLAYER_GROUPS,
				PlayerConfigOptions.BONUS_PLAYER_GROUPS
		);
	}

	@Override
	public int getGroupSpace() {
		if(configType != PlayerConfigType.PLAYER)
			return Integer.MAX_VALUE;
		return getFullLimit(
				ServerConfig.CONFIG.playerGroupSpace,
				UsedPermissionNodes.PLAYER_GROUP_SPACE,
				PlayerConfigOptions.BONUS_PLAYER_GROUP_SPACE
		);
	}

	private int getBaseLimit(ForgeConfigSpec.IntValue serverConfigOption, PermissionNode<Integer> permission){
		MinecraftServer server = config.getManager().getServer();
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(server);
		return PermissionUtils.getOverriddenServerConfigInt(
				config.getPlayerId(), server, null, serverConfigOption,
				permission, serverData.getPlayerPermissionSystemManager().getUsedSystem()
		);
	}

	private int getFullLimit(
			ForgeConfigSpec.IntValue serverConfigOption,
			PermissionNode<Integer> permission,
			IPlayerConfigOptionSpecAPI<Integer> bonusOption
	){
		int baseLimit = getBaseLimit(serverConfigOption, permission);
		int bonusLimit = config.getEffective(bonusOption);
		return baseLimit + bonusLimit;
	}

	public static final class Builder extends CustomPlayerConfigGroupDataManager.Builder<CustomPlayerConfigGroup, Builder> {

		private PlayerConfig<?> config;

		private Builder(){}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setConfig(null);
			return self;
		}

		public Builder setConfig(PlayerConfig<?> config) {
			this.config = config;
			setConfigType(config == null ? null : config.getType());
			return self;
		}

		@Override
		protected ServerPlayerConfigGroupManager buildInternally(){
			Map<String, CustomPlayerConfigGroup> customGroups = new LinkedHashMap<>();
			Map<String, CustomPlayerConfigGroup> customGroupsUnmodifiable = Collections.unmodifiableMap(customGroups);
			Map<String, DefaultPlayerConfigGroupWrapper> defaultConfigGroupWrappers = new HashMap<>();
			ServerPlayerConfigGroupManager result = new ServerPlayerConfigGroupManager(
					config, customGroups, customGroupsUnmodifiable, new LinkedChain<>(),
					new HashMap<>(), defaultConfigGroupWrappers
			);
			PlayerConfigGroupManagerIO io = new PlayerConfigGroupManagerIO(result);
			result.setIo(io);
			return result;
		}

		@Override
		public ServerPlayerConfigGroupManager build() {
			return (ServerPlayerConfigGroupManager) super.build();
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}
}
