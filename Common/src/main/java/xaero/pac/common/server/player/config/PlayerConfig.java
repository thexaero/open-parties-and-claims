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

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.list.SortedValueList;
import xaero.pac.common.misc.ConfigUtil;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.ObjectManagerIOObject;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.config.change.IPlayerConfigChangeHandler;
import xaero.pac.common.server.player.config.group.ServerPlayerConfigGroupManager;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.util.IdentifierUtils;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerConfig
<
	P extends IServerParty<?, ?, ?>
> implements IPlayerConfig, ObjectManagerIOObject {

	public final static int MAX_SUB_ID_LENGTH = 16;
	public final static String SUB_ID_REGEX_PARAMS = "a-zA-Z\\d\\-_";
	public final static String SUB_ID_REGEX = "[" + SUB_ID_REGEX_PARAMS + "]+";
	public final static String WILDERNESS_PLAYER_ID_STRING = "wilderness";
	public final static UUID SERVER_CLAIM_UUID = new UUID(0, 0);
	public final static GameProfile SERVER_CLAIM_PROFILE = new GameProfile(SERVER_CLAIM_UUID, "[Server]");
	public final static UUID EXPIRED_CLAIM_UUID = new UUID(0, 1);
	public final static String MAIN_SUB_ID = "main";
	public final static String PLAYER_CONFIG_ROOT = "playerConfig";
	public final static String PLAYER_CONFIG_ROOT_DOT = PLAYER_CONFIG_ROOT + ".";

	public static final String BUILTIN_EXCEPTION_LEVELS_TOOLTIP = """
					The built-in player groups are:
					
					(N) Nobody
					(P) Party - players or entities owned by players in the same party as you.
					(A) Allies - players or entities owned by players in parties that are allied by yours.
					(E) Every - all players/entities, even if not owned by anyone.""";
	public static final String BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS = """
					The built-in player groups are:
					
					(N) Nobody
					(P) Party - players in the same party as you.
					(A) Allies - players in parties that are allied by yours.
					(E) Every - all players.""";
	public static final String BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED = """
					The built-in player groups are:
					
					(N) Nobody
					(P) Party - entities owned by players in the same party as you.
					(A) Allies - entities owned by players in parties that are allied by yours.
					(E) Every - all entities, even if not owned by anyone.""";
	public static final String BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PROJECTILE = """
					The built-in player groups are:
					
					(N) Nobody
					(P) Party - projectiles owned by players in the same party as you.
					(A) Allies - projectiles owned by players in parties that are allied by yours.
					(E) Every - all projectiles, even if not owned by anyone.""";

	protected final PlayerConfigManager<P, ?> manager;
	private final PlayerConfigType type;
	private final UUID playerId;
	protected Config storage;
	private boolean dirty;
	private ServerPlayerConfigGroupManager playerGroups;
	private final Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues;
	private final LinkedChain<PlayerSubConfig<P>> linkedSubConfigs;
	private final Map<String, PlayerSubConfig<P>> subByID;
	private final Int2ObjectMap<String> subIndexToID;
	private int lastCreatedSubIndex;
	private final SortedValueList<String> subConfigIds;
	private final List<String> subConfigIdsUnmodifiable;
	private boolean beingDeleted;
	private final Map<IPermissionNodeAPI<?>, Object> lastPermissionValues;
	
	protected PlayerConfig(
			PlayerConfigType type,
			UUID playerId,
			PlayerConfigManager<P, ?> manager,
			Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues,
			LinkedChain<PlayerSubConfig<P>> linkedSubConfigs,
			Map<String, PlayerSubConfig<P>> subByID,
			Int2ObjectMap<String> subIndexToID,
			SortedValueList<String> subConfigIds,
			List<String> subConfigIdsUnmodifiable,
			Map<IPermissionNodeAPI<?>, Object> lastPermissionValues
	) {
		this.type = type;
		this.playerId = playerId;
		this.manager = manager;
		this.automaticDefaultValues = automaticDefaultValues;
		this.linkedSubConfigs = linkedSubConfigs;
		this.subByID = subByID;
		this.subIndexToID = subIndexToID;
		this.subConfigIds = subConfigIds;
		this.subConfigIdsUnmodifiable = subConfigIdsUnmodifiable;
		this.lastPermissionValues = lastPermissionValues;
	}
	
	public Config getStorage() {
		if(storage == null) {
			setStorage(ConfigUtil.deepCopy(manager.getDefaultConfig().getStorage(), LinkedHashMap::new));
			storage.set(
					PlayerConfigOptions.CUSTOM_PLAYER_GROUPS.getPath(),
					PlayerConfigOptions.CUSTOM_PLAYER_GROUPS.getDefaultValue()
			);//removing groups copied from the default config
			setDirty(true);
		}
		return storage;
	}
	
	public void setStorage(Config storage) {
		this.storage = storage;
	}

	public void setPlayerGroups(ServerPlayerConfigGroupManager customPlayerGroups) {
		if(this.playerGroups != null)
			throw new IllegalStateException();
		this.playerGroups = customPlayerGroups;
	}

	@Override
	public ServerPlayerConfigGroupManager getPlayerGroups() {
		return playerGroups;
	}

	public <T> void forceSet(PlayerConfigOptionSpec<T> option, T value) {
		if(value == null)
			getStorage().remove(option.getPath());
		else
			getStorage().set(option.getPath(), value);
		if(manager.isLoaded()) {
			resetAutomaticDefaultValue(option);//so people can manually clear cache by changing the option value
			setDirty(true);
		}
	}

	private <T> T get(PlayerConfigOptionSpec<T> option) {
		return getStorage().get(option.getPath());
	}

	protected <T> boolean isValidSetValue(@Nonnull PlayerConfigOptionSpec<T> option, @Nullable T value){
		return value != null && option.getServerSideValidator().test(this, value);
	}

	protected <T> T getValueForDefaultConfigMatch(IPlayerConfigOptionSpecAPI<T> o, T value){
		return manager.getDefaultConfig().getFromEffectiveConfig(o);//the value from the default config
	}

	@Override
	public boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option){
		return option.getConfigTypeFilter().test(getType());
	}
	
	@Nonnull
	@Override
	public <T> SetResult tryToSet(@Nonnull IPlayerConfigOptionSpecAPI<T> o, @Nullable T value) {
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		if(!option.isDirectlyConfigurable())
			return SetResult.NOT_DIRECTLY_CONFIGURABLE;
		if(!isOptionAllowed(option))
			return SetResult.ILLEGAL_OPTION;
		if(!isValidSetValue(option, value))
			return SetResult.INVALID;
		if(isOptionDefaulted(option)){
			T defaultMatchValue = getValueForDefaultConfigMatch(o, value);
			forceSet(option, defaultMatchValue);//to avoid confusion when the option is no longer forced in the future
			return SetResult.DEFAULTED;
		}
		T beforeEffective = getFromEffectiveConfig(option);
		forceSet(option, value);
		if(playerId != null && !Objects.equals(value, beforeEffective)) {
			IPlayerConfigChangeHandler<T> changeHandler = option.getServerChangeHandler();
			if(changeHandler != null && option.getCategory().requiredFeaturesAreEnabled())
				changeHandler.handle(manager, this, option, beforeEffective, value);
		}
		if(manager.isLoaded())
			manager.getSynchronizer().syncOptionToClients(this, option);
		return SetResult.SUCCESS;
	}
	
	public ServerPlayer getOnlinePlayer() {
		PlayerList serverPlayers = manager.getServer().getPlayerList();
		return serverPlayers.getPlayer(playerId);
	}

	public static boolean isPlayerConfigurable(IPlayerConfigOptionSpecAPI<?> o){
		return ((PlayerConfigOptionSpec<?>)o).isForcedPlayerConfigurable() ||
				ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get().contains(o.getId()) ||
				ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get().contains(o.getShortenedId());
	}

	@Override
	public boolean isOptionDefaulted(IPlayerConfigOptionSpecAPI<?> option){
		return playerId != null && !Objects.equals(playerId, SERVER_CLAIM_UUID) && !Objects.equals(playerId, EXPIRED_CLAIM_UUID) &&
				!isOptionOPConfigurable(option) &&
				!isPlayerConfigurable(option);//kinda annoying that it iterates over the whole lists but the lists should be small
	}

	public static boolean isOptionOPConfigurable(IPlayerConfigOptionSpecAPI<?> option){
		return ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(option.getId()) ||
				ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(option.getShortenedId());
	}

	public static boolean isOptionOPConfigurable(String fullOptionId){
		return ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(fullOptionId) ||
				ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(fullOptionId.substring(PLAYER_CONFIG_ROOT_DOT.length()));
	}

	@Nonnull
	@Override
	public <T> T getFromEffectiveConfig(@Nonnull IPlayerConfigOptionSpecAPI<T> o) {
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		if(isOptionDefaulted(option))
			return manager.getDefaultConfig().getFromEffectiveConfig(option);
		return get(option);
	}

	@Override
	public <T> T getRaw(@Nonnull IPlayerConfigOptionSpecAPI<T> o){
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		return get(option);
	}

	@Nonnull
	@Override
	public <T> SetResult tryToReset(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		return tryToSet(option, getDefaultRawValue(option));
	}

	@Nonnull
	@Override
	public <T> T getEffective(@Nonnull IPlayerConfigOptionSpecAPI<T> o) {
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		T value = getFromEffectiveConfig(option);
		return applyDefaultReplacer(o, value);
	}

	public <T> T applyDefaultReplacer(IPlayerConfigOptionSpecAPI<T> o, T value){
		if(value == null)
			return null;
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		if(option.getDefaultReplacer() != null && value.equals(option.getDefaultValue())) {
			@SuppressWarnings("unchecked")
			T autoValue = (T) automaticDefaultValues.get(option);
			if(autoValue == null)
				automaticDefaultValues.put(option, autoValue = option.getDefaultReplacer().apply(this, value));
			return autoValue;
		}
		return value;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		if(!this.dirty && dirty)
			manager.addToSave(this);
		this.dirty = dirty;
	}

	@Override
	public String getFileName() {
		if(playerId == null) {
			if(type == PlayerConfigType.WILDERNESS)
				return WILDERNESS_PLAYER_ID_STRING;
			return "null";
		}
		return playerId.toString();
	}

	@Nullable
	@Override
	public UUID getPlayerId() {
		return playerId;
	}

	@Nonnull
	@Override
	public PlayerConfigType getType() {
		return type;
	}

	public static boolean isValidDimensionSubId(String id){
		return !id.isEmpty() && id.contains(":") && IdentifierUtils.isValidIdentifier(id);//: check makes sure the id is full
	}

	public static boolean isValidSubId(String id){
		return !id.isEmpty() && id.length() <= MAX_SUB_ID_LENGTH && id.matches(PlayerConfig.SUB_ID_REGEX);
	}

	public boolean checkSubIdValidity(String id){
		if(type.hasDimensionSubConfigs())
			return isValidDimensionSubId(id);
		return isValidSubId(id);
	}

	public static String makeSubIdValid(String id){
		String result = id.replaceAll("[^" + SUB_ID_REGEX_PARAMS + "]", "");
		if(result.isEmpty())
			return "sub";
		if(result.length() > MAX_SUB_ID_LENGTH)
			return result.substring(result.length() - MAX_SUB_ID_LENGTH);
		return result;
	}

	private boolean isFreeSubIndex(int index){
		if(type.hasDimensionSubConfigs())
			return true;
		return index != -1 && !subIndexToID.containsKey(index);
	}

	private int getFreeSubConfigIndex(){
		if(type.hasDimensionSubConfigs())
			return 0;
		int result = lastCreatedSubIndex;
		while(!isFreeSubIndex(++result));
		return result;
	}

	@Nullable
	public PlayerSubConfig<P> createSubConfig(@Nonnull String id){
		return createSubConfig(id, true);
	}

	@Override
	public PlayerSubConfig<P> createSubConfig(@Nonnull String id, boolean initStorage){
		int freeSubIndex = getFreeSubConfigIndex();
		return createSubConfig(id, freeSubIndex, initStorage);
	}

	public PlayerSubConfig<P> createSubConfig(String id, int index, boolean initStorage){
		if(subConfigIds.contains(id) || !isFreeSubIndex(index) || !checkSubIdValidity(id))
			return null;
		if(index > lastCreatedSubIndex || index < 0 && lastCreatedSubIndex >= 0)
			lastCreatedSubIndex = index;
		PlayerSubConfig<P> subConfig = PlayerSubConfig.Builder.<P>begin()
				.setType(type)
				.setPlayerId(playerId)
				.setManager(manager)
				.setMainConfig(this)
				.setSubId(id)
				.setSubIndex(index)
				.build();
		subByID.put(id, subConfig);
		if(!type.hasDimensionSubConfigs())
			subIndexToID.put(index, id);
		linkedSubConfigs.add(subConfig);
		addToSubConfigIds(id);
		if(manager.isLoaded() && initStorage) {
			subConfig.getStorage();//creates the storage here to avoid concur modif exception when saving
			manager.getSynchronizer().syncSubExistence(null, subConfig, true);
		}
		return subConfig;
	}

	private void addToSubConfigIds(String id){
		subConfigIds.add(id);
	}

	private void removeFromSubConfigIds(String id){
		subConfigIds.remove(id);
	}

	@Override
	public PlayerSubConfig<P> removeSubConfig(String id){
		PlayerSubConfig<P> subConfig = subByID.remove(id);
		if(subConfig == null)
			return null;
		subIndexToID.remove(subConfig.getSubIndex());
		removeFromSubConfigIds(id);
		linkedSubConfigs.remove(subConfig);
		manager.onSubConfigRemoved(subConfig);
		if(type != PlayerConfigType.SERVER && getEffective(PlayerConfigOptions.USED_SUBCLAIM).equals(id))
			tryToReset(PlayerConfigOptions.USED_SUBCLAIM);
		if(manager.isLoaded())
			manager.getSynchronizer().syncSubExistence(null, subConfig, false);
		return subConfig;
	}

	@Override
	public PlayerSubConfig<P> removeSubConfig(int index){
		String subId = subIndexToID.get(index);
		return subId != null ? removeSubConfig(subId) : null;
	}

	@Nullable
	@Override
	public PlayerConfig<P> getSubConfig(@Nonnull String id){
		if(PlayerConfig.MAIN_SUB_ID.equals(id))
			return this;
		return subByID.get(id);
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getEffectiveSubConfig(@Nonnull String id) {
		PlayerConfig<P> result = getSubConfig(id);
		return result == null ? this : result;
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getEffectiveSubConfig(int subIndex){
		if(subIndex == -1)
			return this;
		String subId = subIndexToID.get(subIndex);
		if(subId == null)
			return this;
		return getSubConfig(subId);
	}

	@Override
	public boolean subConfigExists(@Nonnull String id) {
		return subByID.containsKey(id);
	}

	@Override
	public boolean subConfigExists(int subIndex) {
		return subIndexToID.containsKey(subIndex);
	}

	@Nonnull
	public PlayerConfig<P> getUsedSubConfig(){
		String usedSubId = getEffective(PlayerConfigOptions.USED_SUBCLAIM);
		return getEffectiveSubConfig(usedSubId);
	}

	@Deprecated
	@Nonnull
	@Override
	public IPlayerConfig getUsedServerSubConfig() {
		return getUsedSubConfig(ClaimingModes.SERVER);
	}

	@Nonnull
	@Override
	public IPlayerConfig getUsedSubConfig(@Nonnull IClaimingModeAPI claimingMode) {
		IPlayerConfigOptionSpecAPI<String> option = ((ClaimingMode) claimingMode).getSubClaimOption();
		return ((ClaimingMode)claimingMode).getClaimConfigGetter().apply(this).getEffectiveSubConfig(getEffective(option));
	}

	@Nullable
	@Override
	public <T> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		return option.getDefaultValue();
	}

	public int getSubCount(){
		return subByID.size();
	}

	public Stream<PlayerSubConfig<P>> getSubConfigStream(){
		return linkedSubConfigs.stream();
	}

	@Override
	public Iterator<IPlayerConfig> getSubConfigIterator(){
		return getSubConfigStream().<IPlayerConfig>map(Function.identity()).iterator();
	}

	@Nonnull
	@Override
	public List<String> getSubConfigIds() {
		return subConfigIdsUnmodifiable;
	}

	@Nonnull
	@Override
	public Stream<IPlayerConfigAPI> getSubConfigAPIStream() {
		return getSubConfigStream().map(Function.identity());
	}

	@Nullable
	@Override
	public String getSubId(){
		return null;
	}

	@Override
	public PlayerConfigManager<P, ?> getManager() {
		return manager;
	}

	@Override
	public int getSubIndex(){
		return -1;
	}

	@Override
	public boolean isBeingDeleted() {
		return beingDeleted;
	}

	@Override
	public int getSubConfigLimit() {
		if(type.isGlobal())
			return Integer.MAX_VALUE;
		return ServerConfig.CONFIG.playerSubConfigLimit.get();
	}

	@Override
	public void setBeingDeleted() {
		this.beingDeleted = true;
		manager.getSynchronizer().syncGeneralState(null, this);
	}

	@Override
	public <T> void resetAutomaticDefaultValue(@Nonnull IPlayerConfigOptionSpecAPI<T> o){
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		T valueBefore = getEffective(o);
		if(automaticDefaultValues.remove(option) == null)
			return;
		T valueAfter = getEffective(o);
		if(Objects.equals(valueAfter, valueBefore))
			return;
		IPlayerConfigChangeHandler<T> changeHandler = option.getServerChangeHandler();
		if(changeHandler != null && option.getCategory().requiredFeaturesAreEnabled())
			changeHandler.handle(manager, this, option, valueBefore, valueAfter);
	}

	@Override
	public PlayerConfig<P> getMain(){
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getLastPermissionValue(IPermissionNodeAPI<T> node) {
		if(lastPermissionValues == null)
			throw new UnsupportedOperationException();
		return (T) lastPermissionValues.get(node);
	}

	@Override
	public <T> void setLastPermissionValue(IPermissionNodeAPI<T> node, T value){
		if(lastPermissionValues == null)
			throw new UnsupportedOperationException();
		Object previousValue;
		if(value != null)
			previousValue = lastPermissionValues.put(node, value);
		else
			previousValue = lastPermissionValues.remove(node);
		if(!Objects.equals(previousValue, value))
			setDirty(true);
	}

	public Map<IPermissionNodeAPI<?>, Object> getLastPermissionValues() {
		return lastPermissionValues;
	}

	public static abstract class Builder
	<
		P extends IServerParty<?, ?, ?>,
		B extends Builder<P, B>
	> {

		protected final B self;
		protected PlayerConfigManager<P, ?> manager;
		protected PlayerConfigType type;
		protected UUID playerId;
		protected Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues;

		@SuppressWarnings("unchecked")
		protected Builder(){
			this.self = (B) this;
		}

		public B setDefault(){
			setManager(null);
			setType(PlayerConfigType.PLAYER);
			setPlayerId(null);
			setAutomaticDefaultValues(null);
			return self;
		}

		public B setManager(PlayerConfigManager<P, ?> manager) {
			this.manager = manager;
			return self;
		}

		public B setType(PlayerConfigType type) {
			this.type = type;
			return self;
		}

		public B setPlayerId(UUID playerId) {
			this.playerId = playerId;
			return self;
		}

		public B setAutomaticDefaultValues(Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues) {
			this.automaticDefaultValues = automaticDefaultValues;
			return self;
		}

		public PlayerConfig<P> build(){
			if(type == PlayerConfigType.PLAYER && playerId == null || manager == null)
				throw new IllegalStateException();
			if(automaticDefaultValues == null)
				automaticDefaultValues = new HashMap<>();
			return buildInternally();
		}

		protected abstract PlayerConfig<P> buildInternally();

	}

	public static final class FinalBuilder
	<
		P extends IServerParty<?, ?, ?>
	> extends Builder<P, FinalBuilder<P>> {

		@Override
		protected PlayerConfig<P> buildInternally() {
			List<String> subConfigIdStorage = Lists.newArrayList(PlayerConfig.MAIN_SUB_ID);
			SortedValueList<String> subConfigIds = SortedValueList.Builder.<String>begin().setContent(subConfigIdStorage).build();
			List<String> subConfigIdsUnmodifiable = Collections.unmodifiableList(subConfigIdStorage);
			PlayerConfig<P> result = new PlayerConfig<>(
					type, playerId, manager, automaticDefaultValues,
					new LinkedChain<>(), new HashMap<>(), new Int2ObjectOpenHashMap<>(),
					subConfigIds, subConfigIdsUnmodifiable, new HashMap<>()
			);
			result.setPlayerGroups(ServerPlayerConfigGroupManager.Builder.begin().setConfig(result).build());
			return result;
		}

		public static <P extends IServerParty<?, ?, ?>> FinalBuilder<P> begin(){
			return new FinalBuilder<P>().setDefault();
		}

	}
	
}
