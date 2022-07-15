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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.common.misc.ConfigUtil;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.ObjectManagerIOObject;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PlayerConfig
<
	P extends IServerParty<?, ?>
> implements IPlayerConfig, ObjectManagerIOObject {

	public final static UUID SERVER_CLAIM_UUID = new UUID(0, 0);
	public final static UUID EXPIRED_CLAIM_UUID = new UUID(0, 1);
	
	public static final Map<String, PlayerConfigOptionSpec<?>> OPTIONS;
	public static final PlayerConfigOptionSpec<String> PARTY_NAME;
	public static final PlayerConfigOptionSpec<Integer> BONUS_CHUNK_CLAIMS;
	public static final PlayerConfigOptionSpec<Integer> BONUS_CHUNK_FORCELOADS;
	public static final PlayerConfigOptionSpec<String> CLAIMS_NAME;
	public static final PlayerConfigOptionSpec<Integer> CLAIMS_COLOR;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_PARTY;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_MOB_GRIEFING;

	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_ANONYMOUS_ATTACKS;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT;
	public static final PlayerConfigOptionSpec<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING;
	public static final PlayerConfigOptionSpec<Boolean> ALLOW_SOME_BLOCK_INTERACTIONS;
	public static final PlayerConfigOptionSpec<Boolean> ALLOW_SOME_BLOCK_BREAKING;
	public static final PlayerConfigOptionSpec<Boolean> ALLOW_SOME_ENTITY_INTERACTIONS;
	public static final PlayerConfigOptionSpec<Boolean> ALLOW_SOME_ENTITY_KILLING;

	public static final PlayerConfigOptionSpec<Boolean> FORCELOAD;
	public static final PlayerConfigOptionSpec<Boolean> OFFLINE_FORCELOAD;

	public static final PlayerConfigOptionSpec<Boolean> SHARE_LOCATION_WITH_PARTY;
	public static final PlayerConfigOptionSpec<Boolean> SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES;
	public static final PlayerConfigOptionSpec<Boolean> RECEIVE_LOCATIONS_FROM_PARTY;
	public static final PlayerConfigOptionSpec<Boolean> RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES;
	
	public static final ForgeConfigSpec SPEC;

	static {
		Map<String, PlayerConfigOptionSpec<?>> allOptions = new LinkedHashMap<>();
		
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		PARTY_NAME = PlayerConfigStringOptionSpec.Builder.begin()
				.setId("playerConfig.parties.name")
				.setDefaultValue("")
				.setValidator(s -> s.matches("^(\\p{L}|[0-9 _'\"!?,\\-&%*\\(\\):])*$"))
				.setMaxLength(100)
				.setComment("When not empty, used in some places as the name for the parties that you create.")
				.build(allOptions).applyToForgeSpec(builder);
		CLAIMS_NAME = PlayerConfigStringOptionSpec.Builder.begin()
				.setId("playerConfig.claims.name")
				.setDefaultValue("")
				.setValidator(s -> s.matches("^(\\p{L}|[0-9 _'\"!?,\\-&%*\\(\\):])*$"))
				.setMaxLength(100)
				.setComment("When not empty, used as the name for your claimed chunks.")
				.build(allOptions).applyToForgeSpec(builder);
		BONUS_CHUNK_CLAIMS = PlayerConfigOptionSpec.FinalBuilder.begin(Integer.class)
				.setId("playerConfig.claims.bonusChunkClaims")
				.setDefaultValue(0)
				.setComment("The number of additional chunk claims that you can make on top of the normal limit.")
				.build(allOptions).applyToForgeSpec(builder);
		CLAIMS_COLOR = PlayerConfigHexOptionSpec.Builder.begin()
				.setId("playerConfig.claims.color")
				.setDefaultValue(0x000000)
				.setDefaultReplacer((config, value) -> {
					if(config.getPlayerId() == null || Objects.equals(config.getPlayerId(), SERVER_CLAIM_UUID) || Objects.equals(config.getPlayerId(), EXPIRED_CLAIM_UUID))
						return 0xAA0000;
					int playerIdHash = config.getPlayerId().hashCode();
					int red = (playerIdHash >> 16) & 255;
					int green = (playerIdHash >> 8) & 255;
					int blue = playerIdHash & 255;
					int max = Math.max(Math.max(red, green), blue);
					if(max > 0) {
						red = (int) ((float)red / max * 255);
						green = (int) ((float)green / max * 255);
						blue = (int) ((float)blue / max * 255);
					}
					int autoColor = (red << 16) | (green << 8) | blue;
					if(autoColor == 0)
						autoColor = 0xFF000000;
					return autoColor;
				})
				.setComment("Used as the color for your claims. Set to 0 to use the default automatic color.")
				.build(allOptions).applyToForgeSpec(builder);
		BONUS_CHUNK_FORCELOADS = PlayerConfigOptionSpec.FinalBuilder.begin(Integer.class)
				.setId("playerConfig.claims.bonusChunkForceloads")
				.setDefaultValue(0)
				.setComment("The number of additional chunk claim forceloads that you can make on top of the normal limit.")
				.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protectClaimedChunks")
				.setDefaultValue(true)
				.setComment("When enabled, the mod tries to protect your claimed chunks from other players. Workarounds are possible, especially with mods.")
				.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_FROM_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.fromParty")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against players from the same party as you.")
				.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.protection.fromAllyParties")
					.setDefaultValue(true)
					.setComment("When enabled, claimed chunk protection includes protection against players from parties who are allied by the party that you are in.")
					.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.blocksFromExplosions")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes block protection against explosions. Keep in mind that creeper explosions are also affected by the mob griefing option.")
				.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_FROM_MOB_GRIEFING = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.protection.fromMobGriefing")
					.setDefaultValue(true)
					.setComment("When enabled, claimed chunk protection includes protection against mob griefing (e.g. endermen). Chunks directly next to the protected chunks are also partially protected. Should work for vanilla mob behavior, unless another mod breaks it. Modded mob behavior is unlikely to be included. Feel free to set the vanilla game rule for mob griefing to be safe. Keep in mind that creeper explosions are also affected by the explosion-related options. ")
					.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.fromFireSpread")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against fire spread.")
				.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.protection.entitiesFromPlayers")
					.setDefaultValue(true)
					.setComment("When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against players who don't have access to the chunks.")
					.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.protection.entitiesFromMobs")
					.setDefaultValue(true)
					.setComment("When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against mobs.")
					.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_ANONYMOUS_ATTACKS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.protection.entitiesFromAnonymousAttacks")
					.setDefaultValue(true)
					.setComment("When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against non-player entities without a living owner (e.g. dispenser-fired arrows, falling anvils, redstone-activated TNT).")
					.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.protection.entitiesFromExplosions")
					.setDefaultValue(true)
					.setComment("When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against all explosions not directly activated by the chunk owner.")
					.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.chorusFruitTeleport")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes chorus fruit teleportation prevention for players who don't have access to the chunks.")
				.build(allOptions).applyToForgeSpec(builder);
		PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.playerLightning")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes blocks and entities being protected against lightning directly caused by players who don't have access to the chunks (e.g. with the trident). Chunks directly next to the protected chunks are also partially protected.")
				.build(allOptions).applyToForgeSpec(builder);
		ALLOW_SOME_BLOCK_INTERACTIONS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.allowSomeBlockInteractions")
				.setDefaultValue(false)
				.setComment("When enabled, in addition to some forced exceptions across the server, more block interactions with an empty hand are allowed, which are also configured by the server. It is meant for things like levers, doors etc. You can use the non-ally mode to test it out.")
				.build(allOptions).applyToForgeSpec(builder);
		ALLOW_SOME_BLOCK_BREAKING = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.allowSomeBlockBreaking")
				.setDefaultValue(false)
				.setComment("When enabled, in addition to some forced exceptions across the server, more blocks are allowed to be broken, which are also configured by the server. You can use the non-ally mode to test it out.")
				.build(allOptions).applyToForgeSpec(builder);
		ALLOW_SOME_ENTITY_INTERACTIONS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.allowSomeEntityInteractions")
				.setDefaultValue(false)
				.setComment("When enabled, in addition to some forced exceptions across the server, more entity interactions with an empty hand are allowed, which are also configured by the server. It is meant for things like villager trading, minecarts, boats etc. You can use the non-ally mode to test it out.")
				.build(allOptions).applyToForgeSpec(builder);
		ALLOW_SOME_ENTITY_KILLING = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.protection.allowSomeEntityKilling")
				.setDefaultValue(false)
				.setComment("When enabled, in addition to some forced exceptions across the server, more entities are allowed to be attacked and killed, which are also configured by the server. You can use the non-ally mode to test it out.")
				.build(allOptions).applyToForgeSpec(builder);

		FORCELOAD = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.claims.forceload.enabled")
					.setDefaultValue(true)
					.setComment("When enabled, the chunks you have marked for forceloading are forceloaded.\nIf the forceload limit has changed and you have more chunks marked than the new limit, then some of the chunks won't be forceloaded. Unmark any chunks until you are within the limit to ensure that all marked chunks are forceloaded.")
					.build(allOptions).applyToForgeSpec(builder);
		OFFLINE_FORCELOAD = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.claims.forceload.offlineForceload")
				.setDefaultValue(false)
				.setComment("When enabled, the chunks you have marked for forceloading stay loaded even when you are offline (can significantly affect server performance!).\nIf your forceload limit is affected by your FTB Ranks rank/permissions, then you need to login at least once after a server (re)launch for it to take effect while you are offline.")
				.build(allOptions).applyToForgeSpec(builder);
		

		SHARE_LOCATION_WITH_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.parties.shareLocationWithParty")
					.setDefaultValue(true)
					.setComment("When enabled, your location in the game is shared with players from the same party as you, which can be used by other mods, e.g. to display party members on a map.")
					.build(allOptions).applyToForgeSpec(builder);
		
		SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.parties.shareLocationWithMutualAllyParties")
				.setDefaultValue(false)
				.setComment("When enabled, your location in the game is shared with the mutual ally parties of the party that you are in, which can be used by other mods, e.g. to display party members on a map.")
				.build(allOptions).applyToForgeSpec(builder);
		

		RECEIVE_LOCATIONS_FROM_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
					.setId("playerConfig.parties.receiveLocationsFromParty")
					.setDefaultValue(true)
					.setComment("When enabled, the sharable locations of players from the same party as you are shared with your game client, which can be used by other mods, e.g. to display party members on a map.")
					.build(allOptions).applyToForgeSpec(builder);
		
		RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId("playerConfig.parties.receiveLocationsFromMutualAllyParties")
				.setDefaultValue(false)
				.setComment("When enabled, the sharable locations of players from the mutual ally parties of the party that you are in are shared with your game client, which can be used by other mods, e.g. to display allies on a map.")
				.build(allOptions).applyToForgeSpec(builder);
		
		SPEC = builder.build();

		OPTIONS = Collections.unmodifiableMap(allOptions);
	}
	
	private final PlayerConfigManager<P, ?> manager;
	private final PlayerConfigType type;
	private final UUID playerId;
	private Config storage;
	private boolean dirty;
	private final Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues; 
	
	public PlayerConfig(PlayerConfigType type, UUID playerId, PlayerConfigManager<P, ?> manager, Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues) {
		this.type = type;
		this.playerId = playerId;
		this.manager = manager;
		this.automaticDefaultValues = automaticDefaultValues;
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
	
	private <T> void set(PlayerConfigOptionSpec<T> option, T value) {
		getStorage().set(option.getPath(), value);
		setDirty(true);
	}

	private <T> T get(PlayerConfigOptionSpec<T> option) {
		T value = getStorage().get(option.getPath());
		return value;
	}
	
	@Nonnull
	@Override
	public <T> SetResult tryToSet(@Nonnull PlayerConfigOptionSpec<T> option, @Nonnull T value) {
		if(value == null || !option.getValidator().test(value))
			return SetResult.INVALID;
		T beforeEffective = getFromEffectiveConfig(option);
		set(option, value);
		T actualEffective = getFromEffectiveConfig(option);
		if(actualEffective != value) {
			set(option, actualEffective);//to avoid confusion when the option is no longer forced in the future
			return SetResult.DEFAULTED;
		}
		if(playerId != null && !Objects.equals(actualEffective, beforeEffective)) {
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
				boolean castValue = (boolean)actualEffective;
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
				manager.getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersClaimPropertiesUpdate(manager.getClaimsManager().getPlayerInfo(playerId));
		}
		manager.getSynchronizer().syncToClient(this, option);
		return SetResult.SUCCESS;
	}
	
	private ServerPlayer getOnlinePlayer() {
		PlayerList serverPlayers = manager.getServer().getPlayerList();
		return serverPlayers.getPlayer(playerId);
	}

	@Nonnull
	@Override
	public <T> T getFromEffectiveConfig(@Nonnull PlayerConfigOptionSpec<T> option) {
		if(playerId != null && !Objects.equals(playerId, SERVER_CLAIM_UUID) && !Objects.equals(playerId, EXPIRED_CLAIM_UUID) && !ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(option.getId()) && !ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get().contains(option.getId()))//kinda annoying that it iterates over the whole lists but the lists should be small
			return manager.getDefaultConfig().get(option);
		return get(option);
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEffective(@Nonnull PlayerConfigOptionSpec<T> option) {
		T value = getFromEffectiveConfig(option);
		if(option.getDefaultReplacer() != null && value.equals(option.getDefaultValue())) {
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
	
}
