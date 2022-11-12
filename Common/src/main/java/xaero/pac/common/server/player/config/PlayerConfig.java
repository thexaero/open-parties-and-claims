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

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.list.SortedValueList;
import xaero.pac.common.misc.ConfigUtil;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.ObjectManagerIOObject;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static xaero.pac.common.server.player.config.api.PlayerConfigOptions.*;

public class PlayerConfig
<
	P extends IServerParty<?, ?, ?>
> implements IPlayerConfig, ObjectManagerIOObject {

	public final static int MAX_SUB_ID_LENGTH = 16;
	public final static String SUB_ID_REGEX = "[a-zA-Z\\d\\-_]+";
	public final static UUID SERVER_CLAIM_UUID = new UUID(0, 0);
	public final static UUID EXPIRED_CLAIM_UUID = new UUID(0, 1);
	public final static String MAIN_SUB_ID = "main";
	public final static String PLAYER_CONFIG_ROOT = "playerConfig";
	public final static String PLAYER_CONFIG_ROOT_DOT = PLAYER_CONFIG_ROOT + ".";
	public static final List<Integer> PROTECTION_LEVELS = List.of(0, 1, 2, 3);
	public static final String PROTECTION_LEVELS_TOOLTIP = """
					1) Everyone - protected from all players/entities that don't have chunk access.
					2) Not Party - only players/entities not in the same party as you.
					3) Not Ally - only players/entities not in any party allied by yours.""";

	public static final String PROTECTION_LEVELS_TOOLTIP_PLAYERS = """
					1) Everyone - protected from all players that don't have chunk access.
					2) Not Party - only players not in the same party as you.
					3) Not Ally - only players not in any party allied by yours.""";
	public static final String PROTECTION_LEVELS_TOOLTIP_OWNED = """
					1) Everyone - protected from all entities not owned by a player that has chunk access.
					2) Not Party - all entities, except owned by a player in the same party as you.
					3) Not Ally - all entities, except owned by a player in any party allied by yours.""";

	public static final String EXCEPTION_LEVELS_TOOLTIP = """
					1) Party - players or entities owned by players in the same party as you.
					2) Allies - players or entities owned by players in parties that are allied by yours.
					3) Everyone - all players/entities.""";
	public static final String EXCEPTION_LEVELS_TOOLTIP_PLAYERS = """
					1) Party - players in the same party as you.
					2) Allies - players in parties that are allied by yours.
					3) Everyone - all players.""";

	protected final PlayerConfigManager<P, ?> manager;
	private final PlayerConfigType type;
	private final UUID playerId;
	protected Config storage;
	private boolean dirty;
	private final Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues;
	private final LinkedChain<PlayerSubConfig<P>> linkedSubConfigs;
	private final Map<String, PlayerSubConfig<P>> subByID;
	private final Int2ObjectMap<String> subIndexToID;
	private int lastCreatedSubIndex;
	private final SortedValueList<String> subConfigIds;
	private final List<String> subConfigIdsUnmodifiable;
	private boolean beingDeleted;
	
	protected PlayerConfig(PlayerConfigType type, UUID playerId, PlayerConfigManager<P, ?> manager, Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues, LinkedChain<PlayerSubConfig<P>> linkedSubConfigs, Map<String, PlayerSubConfig<P>> subByID, Int2ObjectMap<String> subIndexToID, SortedValueList<String> subConfigIds, List<String> subConfigIdsUnmodifiable) {
		this.type = type;
		this.playerId = playerId;
		this.manager = manager;
		this.automaticDefaultValues = automaticDefaultValues;
		this.linkedSubConfigs = linkedSubConfigs;
		this.subByID = subByID;
		this.subIndexToID = subIndexToID;
		this.subConfigIds = subConfigIds;
		this.subConfigIdsUnmodifiable = subConfigIdsUnmodifiable;
	}
	
	public Config getStorage() {
		if(storage == null) {
			setStorage(ConfigUtil.deepCopy(manager.getDefaultConfig().getStorage(), LinkedHashMap::new));
			setDirty(true);
		}
		return storage;
	}
	
	public void setStorage(Config storage) {
		this.storage = storage;
	}
	
	private <T extends Comparable<T>> void set(PlayerConfigOptionSpec<T> option, T value) {
		if(value == null)
			getStorage().remove(option.getPath());
		else
			getStorage().set(option.getPath(), value);
		if(manager.isLoaded())
			setDirty(true);
	}

	private <T extends Comparable<T>> T get(PlayerConfigOptionSpec<T> option) {
		return getStorage().get(option.getPath());
	}

	protected <T extends Comparable<T>> boolean isValidSetValue(@Nonnull PlayerConfigOptionSpec<T> option, @Nullable T value){
		return option.getServerSideValidator().test(this, value);
	}

	protected <T extends Comparable<T>> T getValueForDefaultConfigMatch(T actualEffective, T value){
		return actualEffective;//the value from the default config
	}

	@Override
	public  boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option){
		return option.getConfigTypeFilter().test(getType());
	}
	
	@Nonnull
	@Override
	public <T extends Comparable<T>> SetResult tryToSet(@Nonnull IPlayerConfigOptionSpecAPI<T> o, @Nullable T value) {
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		if(!isOptionAllowed(option))
			return SetResult.ILLEGAL_OPTION;
		if(!isValidSetValue(option, value))
			return SetResult.INVALID;
		T beforeEffective = getFromEffectiveConfig(option);
		set(option, value);
		T nowEffective = value;
		if(isOptionDefaulted(option)){
			nowEffective = getValueForDefaultConfigMatch(manager.getDefaultConfig().getFromEffectiveConfig(option), value);
			if (nowEffective != value)
				set(option, nowEffective);//to avoid confusion when the option is no longer forced in the future
			return SetResult.DEFAULTED;
		}
		if(playerId != null && !Objects.equals(nowEffective, beforeEffective)) {
			if(option == BONUS_CHUNK_FORCELOADS || option == BONUS_CHUNK_CLAIMS) {
				ServerPlayer onlinePlayer = getOnlinePlayer();
				if(onlinePlayer != null) {
					IServerClaimsManager<?, ?, ?> claimsManager = manager.getClaimsManager();
					claimsManager.getClaimsManagerSynchronizer().syncClaimLimits(this, onlinePlayer);
				}
			}
			if(option == FORCELOAD || option == OFFLINE_FORCELOAD || option == BONUS_CHUNK_FORCELOADS)
				manager.getForceLoadTicketManager().updateTicketsFor(manager, playerId, false);
			else if(option == PARTY_NAME) {
				P party = manager.getPartyManager().getPartyByOwner(playerId);
				if(party != null)
					manager.getPartyManager().getPartySynchronizer().syncToPartyAndAlliersUpdateName(party, (String)value);
			} else if(option == SHARE_LOCATION_WITH_PARTY || option == SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES || option == RECEIVE_LOCATIONS_FROM_PARTY || option == RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES) {
				boolean castValue = (Boolean)nowEffective;
				P party = manager.getPartyManager().getPartyByMember(playerId);
				if(party != null) {
					ServerPlayer onlinePlayer = getOnlinePlayer();
					if(onlinePlayer != null) {
						if(option == SHARE_LOCATION_WITH_PARTY || option == SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES) {
							ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(onlinePlayer);
							IPartyMemberDynamicInfoSyncable syncedInfo = castValue ? mainCap.getPartyMemberDynamicInfo() : mainCap.getPartyMemberDynamicInfo().getRemover();
							if(option == SHARE_LOCATION_WITH_PARTY)
								manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync().syncToPartyDynamicInfo(party, syncedInfo, party);
							else
								manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync().syncToPartyMutualAlliesDynamicInfo(party, syncedInfo);
						} else {
							if(option == RECEIVE_LOCATIONS_FROM_PARTY)
								manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync().syncToClientAllDynamicInfo(onlinePlayer, party, !castValue);
							else
								manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync().syncToClientMutualAlliesDynamicInfo(onlinePlayer, party, !castValue);
						}
					}
				}
			} else if(option == CLAIMS_NAME || option == CLAIMS_COLOR)
				manager.getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesUpdate(this);
			else if(option == USED_SUBCLAIM || option == USED_SERVER_SUBCLAIM) {
				ServerPlayer onlinePlayer = getOnlinePlayer();
				if(onlinePlayer != null)
					manager.getClaimsManager().getClaimsManagerSynchronizer().syncCurrentSubClaim(this, onlinePlayer);
			}
		}
		manager.getSynchronizer().syncOptionToClients(this, option);
		return SetResult.SUCCESS;
	}
	
	private ServerPlayer getOnlinePlayer() {
		PlayerList serverPlayers = manager.getServer().getPlayerList();
		return serverPlayers.getPlayer(playerId);
	}

	public static boolean isPlayerConfigurable(IPlayerConfigOptionSpecAPI<?> o){
		return o == USED_SUBCLAIM || o == USED_SERVER_SUBCLAIM ||
				ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get().contains(o.getId()) ||
				ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get().contains(o.getShortenedId());
	}

	protected boolean isOptionDefaulted(PlayerConfigOptionSpec<?> option){
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
	public <T extends Comparable<T>> T getFromEffectiveConfig(@Nonnull IPlayerConfigOptionSpecAPI<T> o) {
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		if(isOptionDefaulted(option))
			return manager.getDefaultConfig().getFromEffectiveConfig(option);
		return get(option);
	}

	@Override
	public <T extends Comparable<T>> T getRaw(@Nonnull IPlayerConfigOptionSpecAPI<T> o){
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		return get(option);
	}

	@Nonnull
	@Override
	public <T extends Comparable<T>> SetResult tryToReset(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		return tryToSet(option, getDefaultRawValue(option));
	}

	@Nonnull
	@Override
	public <T extends Comparable<T>> T getEffective(@Nonnull IPlayerConfigOptionSpecAPI<T> o) {
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		T value = getFromEffectiveConfig(option);
		return applyDefaultReplacer(o, value);
	}

	public <T extends Comparable<T>> T applyDefaultReplacer(IPlayerConfigOptionSpecAPI<T> o, T value){
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
		if(playerId != null && !this.dirty && dirty)
			manager.addToSave(this);
		this.dirty = dirty;
	}

	@Override
	public String getFileName() {
		if(playerId == null)
			return "null";
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

	public static boolean isValidSubId(String id){
		return !id.isEmpty() && id.length() <= MAX_SUB_ID_LENGTH && id.matches(PlayerConfig.SUB_ID_REGEX);
	}

	private boolean isFreeSubIndex(int index){
		return index != -1 && !subIndexToID.containsKey(index);
	}

	private int getFreeSubConfigIndex(){
		int result = lastCreatedSubIndex;
		while(!isFreeSubIndex(++result));
		return result;
	}

	@Nullable
	public PlayerSubConfig<P> createSubConfig(@Nonnull String id){
		int freeSubIndex = getFreeSubConfigIndex();
		return createSubConfig(id, freeSubIndex);
	}

	public PlayerSubConfig<P> createSubConfig(String id, int index){
		if(subConfigIds.contains(id) || !isFreeSubIndex(index) || !isValidSubId(id))
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
		subIndexToID.put(index, id);
		linkedSubConfigs.add(subConfig);
		addToSubConfigIds(id);
		if(manager.isLoaded()) {
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
		if(type != PlayerConfigType.SERVER && getEffective(USED_SUBCLAIM).equals(id))
			tryToReset(USED_SUBCLAIM);
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
		String usedSubId = getEffective(USED_SUBCLAIM);
		PlayerConfig<P> result = getSubConfig(usedSubId);
		return result == null ? this : result;
	}

	@Nonnull
	@Override
	public IPlayerConfig getUsedServerSubConfig() {
		return manager.getServerClaimConfig().getEffectiveSubConfig(getEffective(USED_SERVER_SUBCLAIM));
	}

	@Nullable
	@Override
	public <T extends Comparable<T>> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
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
		if(type == PlayerConfigType.SERVER)
			return Integer.MAX_VALUE;
		return ServerConfig.CONFIG.playerSubConfigLimit.get();
	}

	@Override
	public void setBeingDeleted() {
		this.beingDeleted = true;
		manager.getSynchronizer().syncGeneralState(null, this);
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
			return new PlayerConfig<>(type, playerId, manager, automaticDefaultValues, new LinkedChain<>(), new HashMap<>(), new Int2ObjectOpenHashMap<>(), subConfigIds, subConfigIdsUnmodifiable);
		}

		public static <P extends IServerParty<?, ?, ?>> FinalBuilder<P> begin(){
			return new FinalBuilder<P>().setDefault();
		}

	}
	
}
