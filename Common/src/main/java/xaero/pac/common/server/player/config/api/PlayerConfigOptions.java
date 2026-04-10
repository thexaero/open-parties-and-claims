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

package xaero.pac.common.server.player.config.api;

import static xaero.pac.common.player.config.PlayerConfigConstants.*;
import xaero.pac.common.server.player.config.*;
import xaero.pac.common.server.player.config.backwards.v1.CompatPlayerConfigOptionSpec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
 */
@Deprecated
public class PlayerConfigOptions {

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final Map<String, IPlayerConfigOptionSpecAPI<?>> OPTIONS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<String> USED_SUBCLAIM;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<String> USED_SERVER_SUBCLAIM;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<String> PARTY_NAME;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> BONUS_CHUNK_CLAIMS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> BONUS_CHUNK_FORCELOADS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<String> CLAIMS_NAME;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> CLAIMS_COLOR;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_PARTY;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_BLOCKS_REDIRECT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_BLOCKS_FROM_ENCHANTMENTS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TARGETS_FROM_PROJECTILES;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLATES_FROM_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLATES_FROM_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLATES_FROM_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_REDIRECT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_FIRE;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_REDIRECT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FLUID_BARRIER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_DISPENSER_BARRIER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_OTHER;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_REDIRECT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_MOB_LOOT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_REDIRECT;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_XP_PICKUP;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_USE;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_MOB_GRIEFING_OVERRIDE;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_RAIDS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_HOSTILE_NATURAL_SPAWN;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FRIENDLY_NATURAL_SPAWN;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_HOSTILE_SPAWNERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FRIENDLY_SPAWNERS;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PROJECTILE_HIT_HOSTILE_SPAWN;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PROJECTILE_HIT_FRIENDLY_SPAWN;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> FORCELOAD;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> OFFLINE_FORCELOAD;

	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> SHARE_LOCATION_WITH_PARTY;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> RECEIVE_LOCATIONS_FROM_PARTY;
	/**
	 * @deprecated switch to {@link xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions}
	 */
	@Deprecated
	public static final IPlayerConfigOptionSpecAPI<Boolean> RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES;

	static {
		Map<String, CompatPlayerConfigOptionSpec<?, ?>> allOptions = new LinkedHashMap<>();

		BiFunction<Integer, String, String> protectionToGroup = (i, r) ->
				i == 0 ? EVERYONE_EXCEPTION_ID :
				i == 1 ? NO_EXCEPTION_ID :
				i == 2 ? PARTY_EXCEPTION_ID :
				ALLIES_EXCEPTION_ID;
		Function<String, Integer> groupToProtection = r ->
				r.equals(ALLIES_EXCEPTION_ID) ? 3 :
				r.equals(PARTY_EXCEPTION_ID) ? 2 :
				r.equals(EVERYONE_EXCEPTION_ID) ? 0 :
				1;
		BiFunction<Integer, String, String> exceptionToGroup = (i, r) ->
				i == 0 ? NO_EXCEPTION_ID :
				i == 1 ? PARTY_EXCEPTION_ID :
				i == 2 ? ALLIES_EXCEPTION_ID :
				EVERYONE_EXCEPTION_ID;
		Function<String, Integer> groupToException = r ->
				r.equals(EVERYONE_EXCEPTION_ID) ? 3 :
				r.equals(ALLIES_EXCEPTION_ID) ? 2 :
				r.equals(PARTY_EXCEPTION_ID) ? 1 :
				0;
		Function<Boolean, Boolean> flippedBoolean = v -> !v;
		BiFunction<Boolean, Boolean, Boolean> toFlippedBoolean = (v, r) -> !v;
		USED_SUBCLAIM = CompatPlayerConfigOptionSpec.Builder.<String, String>begin(PlayerConfigOptionValueTypes.STRING)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.USED_SUBCLAIM)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.usedSub")
				.build(allOptions);
		USED_SERVER_SUBCLAIM = CompatPlayerConfigOptionSpec.Builder.<String, String>begin(PlayerConfigOptionValueTypes.STRING)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.USED_SERVER_SUBCLAIM)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.usedServerSub")
				.build(allOptions);

		CLAIMS_NAME = CompatPlayerConfigOptionSpec.Builder.<String, String>begin(PlayerConfigOptionValueTypes.STRING)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIMS_NAME)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.name")
				.build(allOptions);
		CLAIMS_COLOR = CompatPlayerConfigOptionSpec.Builder.<Integer, Integer>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIMS_COLOR)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.color")
				.build(allOptions);
		PARTY_NAME = CompatPlayerConfigOptionSpec.Builder.<String, String>begin(PlayerConfigOptionValueTypes.STRING)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.PARTY_NAME)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.name")
				.build(allOptions);
		SHARE_LOCATION_WITH_PARTY = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.shareLocationWithParty")
				.build(allOptions);
		SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.shareLocationWithMutualAllyParties")
				.build(allOptions);
		RECEIVE_LOCATIONS_FROM_PARTY = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.RECEIVE_LOCATIONS_FROM_PARTY)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.receiveLocationsFromParty")
				.build(allOptions);
		RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.receiveLocationsFromMutualAllyParties")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protectClaimedChunks")
				.build(allOptions);
		BONUS_CHUNK_CLAIMS = CompatPlayerConfigOptionSpec.Builder.<Integer, Integer>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.BONUS_CHUNK_CLAIMS)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.bonusChunkClaims")
				.build(allOptions);
		BONUS_CHUNK_FORCELOADS = CompatPlayerConfigOptionSpec.Builder.<Integer, Integer>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.BONUS_CHUNK_FORCELOADS)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.bonusChunkForceloads")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_PARTY = CompatPlayerConfigOptionSpec.Builder.<Boolean, String>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.FULL_ACCESS)
				.setToRealConverter((v, r) -> v ?
						(r.equals(EVERYONE_EXCEPTION_ID) || r.equals(ALLIES_EXCEPTION_ID) || r.equals(PARTY_EXCEPTION_ID) ? NO_EXCEPTION_ID : r)
						: (r.equals(EVERYONE_EXCEPTION_ID) || r.equals(ALLIES_EXCEPTION_ID) || r.equals(PARTY_EXCEPTION_ID) ? r : PARTY_EXCEPTION_ID))
				.setFromRealConverter(v -> !v.equals(PARTY_EXCEPTION_ID) && !v.equals(ALLIES_EXCEPTION_ID) && !v.equals(EVERYONE_EXCEPTION_ID))
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromParty")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES = CompatPlayerConfigOptionSpec.Builder.<Boolean, String>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.FULL_ACCESS)
				.setToRealConverter((v, r) -> v ?
						(r.equals(EVERYONE_EXCEPTION_ID) || r.equals(ALLIES_EXCEPTION_ID) ? NO_EXCEPTION_ID : r)
						: (r.equals(EVERYONE_EXCEPTION_ID) || r.equals(ALLIES_EXCEPTION_ID) ? r : ALLIES_EXCEPTION_ID)
				)
				.setFromRealConverter(v -> !v.equals(ALLIES_EXCEPTION_ID) && !v.equals(EVERYONE_EXCEPTION_ID))
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromAllyParties")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_OTHER)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_REDIRECT = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_REDIRECT)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksRedirect")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromExplosions")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_FIRE_SPREAD)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromFireSpread")
				.build(allOptions);
		PROTECT_CLAIMED_BLOCKS_FROM_ENCHANTMENTS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_ENCHANTMENTS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromEnchantments")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_CROP_TRAMPLE)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.cropTrample")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FLUID_BARRIER = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_FLUID_BARRIER)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fluidBarrier")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_PISTON_BARRIER)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.pistonBarrier")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_BUTTONS_BY_PROJECTILES)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.buttonsFromProjectiles")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TARGETS_FROM_PROJECTILES = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_TARGETS_BY_PROJECTILES)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.targetsFromProjectiles")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLATES_FROM_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLATES_BY_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.platesFromPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLATES_FROM_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLATES_BY_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.platesFromMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLATES_FROM_OTHER = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLATES_BY_OTHER)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.platesFromOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_TRIPWIRE_BY_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.tripwireFromPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_TRIPWIRE_BY_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.tripwireFromMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_OTHER = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_TRIPWIRE_BY_OTHER)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.tripwireFromOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_BY_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_BY_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_BY_OTHER)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_REDIRECT = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_REDIRECT)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesRedirect")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_BY_EXPLOSIONS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromExplosions")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_FIRE = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_BY_FIRE)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromFire")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_RAIDS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_RAIDS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.raids")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLAYERS_BY_PLAYERS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersFromPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLAYERS_BY_MOBS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersFromMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLAYERS_BY_OTHER)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersFromOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_REDIRECT = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLAYERS_REDIRECT)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersRedirect")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PLAYER_LIGHTNING)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playerLightning")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_CHORUS_FRUIT)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.chorusFruitTeleport")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_NETHER_PORTALS_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.netherPortalsPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_NETHER_PORTALS_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.netherPortalsMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_OTHER = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_NETHER_PORTALS_OTHER)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.netherPortalsOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_USE = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_USE)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemUse")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_PROTECTION_NEIGHBOR_CHUNKS_ITEM_USE)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.neighborChunksItemUse")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_DISPENSER_BARRIER = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_DISPENSER_BARRIER)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.dispenserBarrier")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_TOSS_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_TOSS_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_OTHER = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_TOSS_OTHER)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossOther")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_REDIRECT = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_TOSS_REDIRECT)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossRedirect")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_MOB_LOOT = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_MOB_LOOT)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.mobLoot")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_PROTECTION_PLAYER_DEATH_LOOT)
				.setToRealConverter(exceptionToGroup)
				.setFromRealConverter(groupToException)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playerDeathLoot")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_PICKUP_PLAYERS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemPickupPlayers")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_PICKUP_MOBS)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemPickupMobs")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_REDIRECT = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_ITEM_PICKUP_REDIRECT)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemPickupRedirect")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_XP_PICKUP = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_XP_PICKUP)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.xpPickup")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_MOB_GRIEFING_OVERRIDE = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_MOB_GRIEFING_OVERRIDE)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.overrideMobGriefingRule")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_HOSTILE_NATURAL_SPAWN = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_HOSTILE_NATURAL_SPAWN)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.naturalSpawnHostile")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FRIENDLY_NATURAL_SPAWN = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_FRIENDLY_NATURAL_SPAWN)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.naturalSpawnFriendly")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_HOSTILE_SPAWNERS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_HOSTILE_SPAWNERS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.spawnersHostile")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FRIENDLY_SPAWNERS = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_FRIENDLY_SPAWNERS)
				.setToRealConverter(toFlippedBoolean)
				.setFromRealConverter(flippedBoolean)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.spawnersFriendly")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PROJECTILE_HIT_HOSTILE_SPAWN = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PROJECTILE_HIT_HOSTILE_SPAWN)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.projectileHitHostileSpawn")
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PROJECTILE_HIT_FRIENDLY_SPAWN = CompatPlayerConfigOptionSpec.Builder.<Integer, String>begin(PlayerConfigOptionValueTypes.INTEGER)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.CLAIM_EXCEPTION_PROJECTILE_HIT_FRIENDLY_SPAWN)
				.setToRealConverter(protectionToGroup)
				.setFromRealConverter(groupToProtection)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.projectileHitFriendlySpawn")
				.build(allOptions);

		FORCELOAD = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.FORCELOAD)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.forceload.enabled")
				.build(allOptions);
		OFFLINE_FORCELOAD = CompatPlayerConfigOptionSpec.Builder.<Boolean, Boolean>begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setRealOption(xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions.OFFLINE_FORCELOAD)
				.setToRealConverter((v, r) -> v)
				.setFromRealConverter(v -> v)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.forceload.offlineForceload")
				.build(allOptions);

		OPTIONS = Collections.unmodifiableMap(allOptions);
	}

}
