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

package xaero.pac.common.server.player.config.api;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.server.player.config.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An access point for all static player config option specifications/representations.
 * <p>
 * Use the client/server sided player config manager API to access the dynamic options generated
 * based on the server config values.
 */
public class PlayerConfigOptions {

	/**
	 * An ID->specification map of all static player config options.
	 */
	public static final Map<String, IPlayerConfigOptionSpecAPI<?>> OPTIONS;
	/**
	 * The current sub-config ID that the player uses for their own new claims.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> USED_SUBCLAIM;
	/**
	 * The current sub-config ID that the player uses for new server claims.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> USED_SERVER_SUBCLAIM;
	/**
	 * The name of the player's party if they own one.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> PARTY_NAME;
	/**
	 * The bonus chunk claims on top of the normal limit.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> BONUS_CHUNK_CLAIMS;
	/**
	 * The bonus chunk forceloads on top of the normal limit.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> BONUS_CHUNK_FORCELOADS;
	/**
	 * The display name of the player's claimed chunks.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIMS_NAME;
	/**
	 * The display color of the player's claimed chunks.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> CLAIMS_COLOR;
	/**
	 * Whether the player's claimed chunks are protected at all.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS;
	/**
	 * Whether the claimed chunk protection includes protection against the party that the player is in.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_PARTY;
	/**
	 * Whether the claimed chunk protection includes protection against allies of the party that the player is in.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES;
	/**
	 * Whether the claimed chunk protection includes protection against players breaking and interacting with blocks.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes protection against mobs breaking/placing blocks.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS;
	/**
	 * Whether the claimed chunk protection includes protection against non-living entities breaking/placing blocks.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER;
	/**
	 * Whether the claimed chunk block protection redirects the used config option to the owner of the entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_BLOCKS_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes protection against fire spread.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD;
	/**
	 * Whether the claimed chunk protection includes protection from frost walking.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_FROM_FROST_WALKING;
	/**
	 * Whether the claimed chunk protection includes protection against explosions.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS;
	/**
	 * Whether the claimed chunk protection includes protection of buttons being pressed by projectiles.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES;
	/**
	 * Whether the claimed chunk protection includes protection of target blocks being pressed by projectiles.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TARGETS_FROM_PROJECTILES;
	/**
	 * Whether the claimed chunk protection includes protection of pressure plates being pressed by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLATES_FROM_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes protection of pressure plates being pressed by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLATES_FROM_MOBS;
	/**
	 * Whether the claimed chunk protection includes protection of pressure plates being pressed by non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLATES_FROM_OTHER;
	/**
	 * Whether the claimed chunk protection includes protection of tripwires being pressed by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes protection of tripwires being pressed by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_MOBS;
	/**
	 * Whether the claimed chunk protection includes protection of tripwires being pressed by non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_OTHER;
	/**
	 * Whether the claimed chunk protection includes entity protection against players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes entity protection against mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS;
	/**
	 * Whether the claimed chunk protection includes entity protection against non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER;
	/**
	 * Whether the claimed chunk entity protection redirects the used config option to the owner of the attacking entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes entity protection against explosions.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS;
	/**
	 * Whether the claimed chunk protection includes entity protection against fire damage.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_FIRE;
	/**
	 * Whether the claimed chunk protection includes player protection against players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes player protection against mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS;
	/**
	 * Whether the claimed chunk protection includes player protection against non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER;
	/**
	 * Whether the claimed chunk player protection redirects the used config option to the owner of the attacking entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PLAYERS_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes protection against chorus fruit teleportation into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT;
	/**
	 * Whether the claimed chunk protection includes protection against players using nether portals.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes protection against mobs using nether portals.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_MOBS;
	/**
	 * Whether the claimed chunk protection includes protection against non-living entities using nether portals.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_OTHER;
	/**
	 * Whether the claimed chunk protection includes protection against player-caused lightnings.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING;
	/**
	 * Whether the claimed chunk protection includes protection against crop trample.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE;
	/**
	 * Whether the claimed chunk protection includes protection against fluids flowing into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FLUID_BARRIER;
	/**
	 * Whether the claimed chunk protection includes protection against directly dispensing into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_DISPENSER_BARRIER;
	/**
	 * Whether the claimed chunk protection includes protection against pistons pushing into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER;
	/**
	 * Whether the claimed chunk protection includes protection against items being dropped by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes protection against items being dropped by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_MOBS;
	/**
	 * Whether the claimed chunk protection includes protection against items being dropped by non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_OTHER;
	/**
	 * Whether the claimed chunk item drop protection redirects the used config option to the owner of the dropping entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes protection from mob loot being dropped unless killed by players with access.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_MOB_LOOT;
	/**
	 * Whether the claimed chunk protection includes protection for items dropped on player death.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT;
	/**
	 * Whether the claimed chunk protection includes protection against items being picked up by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes protection against items being picked up by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS;
	/**
	 * Whether the claimed chunk item pickup protection redirects the used config option to the owner of the entity picking up the item.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes protection against experience orbs being picked up by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_XP_PICKUP;
	/**
	 * Whether the claimed chunk protection includes protection against item use.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> PROTECT_CLAIMED_CHUNKS_ITEM_USE;
	/**
	 * Whether the claimed chunk protection includes protection against at-air (or sometimes other) item use in
	 * neighbor chunks of the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE;
	/**
	 * Whether the claimed chunk protection options for block/entity/items override the value of the vanilla "mob griefing" game rule.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_MOB_GRIEFING_OVERRIDE;
	/**
	 * Whether the claimed chunk protection includes protection against village raids.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_RAIDS;
	/**
	 * Whether the claimed chunk protection includes natural spawn prevention for hostile mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_HOSTILE_NATURAL_SPAWN;
	/**
	 * Whether the claimed chunk protection includes natural spawn prevention for friendly mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FRIENDLY_NATURAL_SPAWN;
	/**
	 * Whether the claimed chunk protection disables spawners for hostile mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_HOSTILE_SPAWNERS;
	/**
	 * Whether the claimed chunk protection disables spawners for friendly mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS_FRIENDLY_SPAWNERS;
	/**
	 * Whether the player's forceloadable claims are forceloaded, at least while the player is online.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> FORCELOAD;
	/**
	 * Whether the player's forceloaded claims stay forceloaded when they go offline.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> OFFLINE_FORCELOAD;

	/**
	 * Whether the player shares their in-game location with their party.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> SHARE_LOCATION_WITH_PARTY;
	/**
	 * Whether the player shares their in-game location with their party's mutual allies.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES;
	/**
	 * Whether the player receives the in-game locations shared by their fellow party members.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> RECEIVE_LOCATIONS_FROM_PARTY;
	/**
	 * Whether the player receives the in-game locations shared by their party's mutual allies.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES;

	static {
		Map<String, PlayerConfigOptionSpec<?>> allOptions = new LinkedHashMap<>();

		USED_SUBCLAIM = PlayerConfigListIterationOptionSpec.FinalBuilder.begin(String.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER)
				.setServerSideListGetter(PlayerConfig::getSubConfigIds)
				.setClientSideListGetter(PlayerConfigClientStorage::getSubConfigIds)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.usedSub")
				.setDefaultValue(PlayerConfig.MAIN_SUB_ID)
				.setValueValidator(PlayerConfig::isValidSubId)
				.setComment("The current sub-config ID used for new chunk claims.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		USED_SERVER_SUBCLAIM = PlayerConfigListIterationOptionSpec.FinalBuilder.begin(String.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER)
				.setServerSideListGetter(pc -> pc.getManager().getServerClaimConfig().getSubConfigIds())
				.setClientSideListGetter(pc ->
					OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().
							getServerClaimsConfig().getSubConfigIds()
				)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.usedServerSub")
				.setDefaultValue(PlayerConfig.MAIN_SUB_ID)
				.setValueValidator(PlayerConfig::isValidSubId)
				.setComment("The current sub-config ID used for new server chunk claims.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);

		CLAIMS_NAME = PlayerConfigStringOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.name")
				.setDefaultValue("")
				.setValueValidator(s -> s.matches("^(\\p{L}|[0-9 _'\"!?,\\-&%*\\(\\):])*$"))
				.setMaxLength(100)
				.setComment("When not empty, used as the name for your claimed chunks.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		CLAIMS_COLOR = PlayerConfigHexOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.color")
				.setDefaultValue(0x000000)
				.setDefaultReplacer((config, value) -> {
					if(config.getPlayerId() == null || Objects.equals(config.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID) || Objects.equals(config.getPlayerId(), PlayerConfig.EXPIRED_CLAIM_UUID))
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
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		PARTY_NAME = PlayerConfigStringOptionSpec.Builder.begin()
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.name")
				.setDefaultValue("")
				.setValueValidator(s -> s.matches("^(\\p{L}|[0-9 _'\"!?,\\-&%*\\(\\):])*$"))
				.setMaxLength(100)
				.setComment("When not empty, used in some places as the name for the parties that you create.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.build(allOptions);
		SHARE_LOCATION_WITH_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.shareLocationWithParty")
				.setDefaultValue(true)
				.setComment("When enabled, your position in the game is shared with players from the same party as you, which can be used by other mods, e.g. to display party members on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.build(allOptions);
		SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.shareLocationWithMutualAllyParties")
				.setDefaultValue(false)
				.setComment("When enabled, your position in the game is shared with the mutual ally parties of the party that you are in, which can be used by other mods, e.g. to display party members on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.build(allOptions);
		RECEIVE_LOCATIONS_FROM_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.receiveLocationsFromParty")
				.setDefaultValue(true)
				.setComment("When enabled, the sharable positions of players from the same party as you are shared with your game client, which can be used by other mods, e.g. to display party members on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.build(allOptions);
		RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.receiveLocationsFromMutualAllyParties")
				.setDefaultValue(false)
				.setComment("When enabled, the sharable positions of players from the mutual ally parties of the party that you are in are shared with your game client, which can be used by other mods, e.g. to display allies on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protectClaimedChunks")
				.setDefaultValue(true)
				.setComment("When enabled, the mod tries to protect your claimed chunks from other players. Workarounds are possible, especially with mods.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		BONUS_CHUNK_CLAIMS = PlayerConfigOptionSpec.FinalBuilder.begin(Integer.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.bonusChunkClaims")
				.setDefaultValue(0)
				.setComment("The number of additional chunk claims that you can make on top of the normal limit.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		BONUS_CHUNK_FORCELOADS = PlayerConfigOptionSpec.FinalBuilder.begin(Integer.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.bonusChunkForceloads")
				.setDefaultValue(0)
				.setComment("The number of additional chunk claim forceloads that you can make on top of the normal limit.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromParty")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection includes protection against players from the same party as you.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromAllyParties")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against players from parties who are allied by the party that you are in.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromPlayers")
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setDefaultValue(1)
				.setComment(
						"When enabled, claimed chunk protection includes basic protection against players breaking or otherwise interacting with blocks if they don't have access to the chunks. Block placing is usually additionally controlled by the item use protection.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromMobs")
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setDefaultValue(1)
				.setComment(
						"When enabled, claimed chunk protection includes protection against mobs, who don't have access to the chunks, breaking/placing blocks (e.g. endermen). Chunks directly next to the protected chunks are also partially protected when protection is based on the mob griefing rule check. Should work for vanilla mob behavior. Modded mob behavior is likely not to be included. Feel free to set the vanilla game rule for mob griefing for extra safety. Keep in mind that creeper explosions are also affected by the explosion-related options.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_OTHER = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromOther")
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setDefaultValue(1)
				.setComment(
						"When enabled, claimed chunk protection includes protection against non-living entities, who don't have access to the chunks, breaking/placing blocks. Should work for vanilla entity behavior, unless another mod breaks it. Modded entity behavior is likely not to be included. Keep in mind that explosions are also affected by the explosion-related options.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Protect Blocks From Mobs/Other\" option for block interactions coming from non-player entities, if the entity (e.g. an arrow) has an owner (e.g. a player), then the block protection option corresponding to the owner is used (e.g. \"Protect Blocks From Players\").\nChunk access is always tested against the owner, whether this is enabled or not.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.blocksFromExplosions")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes block protection against explosions. Keep in mind that creeper explosions are also affected by the block mob protection option.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromFireSpread")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against fire spread.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FROM_FROST_WALKING = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fromFrostWalking")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection against frost walking by players/entities who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_CROP_TRAMPLE = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.cropTrample")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against crop trample (falling on crops destroys them) for players that don't have access to the chunks.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FLUID_BARRIER = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fluidBarrier")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against fluids (e.g. lava) flowing into the protected chunks from outside. This does not protect wilderness.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PISTON_BARRIER = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.pistonBarrier")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against being affected by pistons outside of the protected chunks. This does not protect wilderness.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_BUTTONS_FROM_PROJECTILES = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.buttonsFromProjectiles")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes buttons being protected against projectiles not owned by any player who has access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TARGETS_FROM_PROJECTILES = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.targetsFromProjectiles")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes target blocks being protected against projectiles not owned by any player who has access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLATES_FROM_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.platesFromPlayers")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes pressure plates being protected against players who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLATES_FROM_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.platesFromMobs")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes pressure plates being protected against mobs who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLATES_FROM_OTHER = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.platesFromOther")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes pressure plates being protected against non-living entities who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.tripwireFromPlayers")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes tripwires being protected against players who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.tripwireFromMobs")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes tripwires being protected against mobs who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_TRIPWIRE_FROM_OTHER = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.tripwireFromOther")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes tripwires being protected against non-living entities who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromPlayers")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against players who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromMobs")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against mobs. Chunks directly next to the protected chunks are also partially protected when protection is based on the mob griefing rule check.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_OTHER = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromOther")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against non-living entities (e.g. arrows, falling anvils, activated TNT).\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Protect Entities From Mobs/Other\" option for entity attacks/interactions coming from non-player entities, if the attacking entity (e.g. an arrow) has an owner (e.g. a player), then the entity protection option corresponding to the owner is used (e.g. \"Protect Entities From Players\").\nChunk access is always tested against the owner, whether this is enabled or not.")
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromExplosions")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against all explosions not directly activated by the chunk owner.")
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_FIRE = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.entitiesFromFire")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes friendly (+ server configured) entities in the chunks being protected against fire.")
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_RAIDS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.raids")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection from village raids. It stops raiders from spawning inside the protected chunks, from entering them and from hurting protectable entities, even if entity protection is turned off.")
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_PLAYERS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersFromPlayers")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection includes players being protected from player attacks.")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_MOBS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersFromMobs")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection includes players being protected from mob attacks.")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_FROM_OTHER = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersFromOther")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection includes players being protected against non-living entities.")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYERS_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playersRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Protect Players From Mobs/Other\" option for entity attacks/iteractions coming from non-player entities, if the attacking entity (e.g. an arrow) has an owner (e.g. a player), then the entity protection option corresponding to the owner is used (e.g. \"Protect Players From Players\").")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playerLightning")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes blocks and entities being protected against lightning directly caused by players who don't have access to the chunks (e.g. with the trident). Chunks directly next to the protected chunks are also partially protected.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.chorusFruitTeleport")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes chorus fruit teleportation prevention for entities/players who don't have access to the chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.netherPortalsPlayers")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes nether portal usage prevention for players who don't have access to the chunks. \n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.netherPortalsMobs")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes nether portal usage prevention for mobs who don't have access to the chunks. Even after the protection is turned off, a recently stopped entity is still on a short cooldown. You must let it finish without constantly retrying to push it through the portal, which restarts the cooldown.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NETHER_PORTALS_OTHER = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.netherPortalsOther")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes nether portal usage prevention for non-living entities who don't have access to the chunks. Even after the protection is turned off, a recently stopped entity is still on a short cooldown. You must let it finish without constantly retrying to push it through the portal, which restarts the cooldown.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_USE = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemUse")
				.setDefaultValue(1)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection from right-click held item use. On Fabric, allowed item use means being able to place blocks on blocks that you can interact with, e.g. exception blocks, even if block protection is enabled! Right-click item use can also break blocks, if that is the item's right-click mechanic. Some item use in a chunk might also be prevented by neighbor item use protection in neighbor chunks.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PROTECTION_FROM_ITEMS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_NEIGHBOR_CHUNKS_ITEM_USE = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.neighborChunksItemUse")
				.setDefaultValue(true)
				.setComment("When enabled, the item use protection is extended to some right-click held item use in chunks directly next to the claimed ones. Item use affected by this is usually things that still work while looking at the sky (not block or entity) or item use with custom ray-tracing for blocks/fluids/entities (e.g. placing things on water), but also any item use of \"additional banned items\" configured on the server. Item use protection exceptions (e.g. food, potions etc) still apply.")
				.setCategory(PlayerConfigOptionCategory.PROTECTION_FROM_ITEMS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_DISPENSER_BARRIER = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.dispenserBarrier")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against dispensers that are \"touching\" and facing the protected chunks from outside. This does not protect wilderness.")
				.setCategory(PlayerConfigOptionCategory.PROTECTION_FROM_ITEMS)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossPlayers")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes prevention of item tossing by players that don't have access to the chunks.\nDying can be used to circumvent this, so it is recommended to enable keepInventory or use a gravestone mod.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossMobs")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes prevention of item tossing by some mobs that don't have access to the chunks. Requires the tossing mob to be set as the item's thrower.\nModded mobs are pretty likely to do it themselves or have it done by this mod. Otherwise, the toss won't be prevented.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_OTHER = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossOther")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes prevention of item tossing by non-living entities that don't have access to the chunks. Requires the tossing mob to be set as the item's thrower. Some entities might not that.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_TOSS_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemTossRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Protect Mob/Other Item Toss\" option for item tosses coming from non-player entities, if the tossing entity (e.g. a special arrow) has an owner (e.g. a player), then the item toss protection option corresponding to the owner is used (e.g. \"Protect Player Item Toss\").")
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_MOB_LOOT = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.mobLoot")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection from loot being dropped when mobs die unless they are killed by players who have access to the chunks. Any non-living entity spawned on a mob's death is considered loot.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_PLAYER_DEATH_LOOT = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playerDeathLoot")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection for items and experience that have been dropped on a player death, even if the standard item pickup protection is disabled. The protected items are only accessible to the player that dropped them and the entity/player that killed the player.\n\n"
						+ PlayerConfig.EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_PLAYERS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemPickupPlayers")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection from players picking up items, unless they have access to the chunks or own the items.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_MOBS = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemPickupMobs")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection from mobs picking up items, unless they have access to the chunks or own the items. Might not work for some mobs. Chunks directly next to the protected chunks are also partially protected when protection is based on the mob griefing rule check.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_ITEM_PICKUP_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.itemPickupRedirect")
				.setDefaultValue(false)
				.setComment("When enabled, instead of always simply using the direct \"Protect Items From Mobs\" option for item pickups coming from mobs, if the mob (e.g. an allay) has an owner (e.g. a player), then the item protection option corresponding to the owner is used (e.g. \"Protect Items From Players\").")
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_XP_PICKUP = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.xpPickup")
				.setDefaultValue(0)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setComment(
						"When enabled, claimed chunk protection includes protection from players picking up experience orbs, unless they have access to the chunks or own the orbs.\n\n"
						+ PlayerConfig.PROTECTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_MOB_GRIEFING_OVERRIDE = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.overrideMobGriefingRule")
				.setDefaultValue(true)
				.setComment(
						"Override the value of the vanilla \"mob griefing\" game rule with either block, entity or dropped item protection in the protected chunks and their neighbors.\n" +
						"By default, all \"mob griefing\" game rule checks, except for evokers (sheep conversion spell) and for most item pickups, are overridden with the block protection option. " +
						"By default, the game rule is not overridden for item pickups (e.g. piglins picking up gold) because the basic item protection is already enough for most cases. " +
						"When using the Forge version of the mod, this can be used for modded mobs. The main server config can be used to change which options are checked (even all 3) for specific mobs. Fabric/Quilt does not fire an event for all mob griefing rule checks. " +
						"Fabric/Quilt modded mobs would simply check the game rule directly, which cannot be overridden by this mod."
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_HOSTILE_NATURAL_SPAWN = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.naturalSpawnHostile")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection disables the natural spawning of hostile mobs.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FRIENDLY_NATURAL_SPAWN = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.naturalSpawnFriendly")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection disables the natural spawning of friendly mobs.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_HOSTILE_SPAWNERS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.spawnersHostile")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection disables hostile mob spawners.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS_FRIENDLY_SPAWNERS = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.spawnersFriendly")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection disables friendly mob spawners.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);

		FORCELOAD = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.forceload.enabled")
				.setDefaultValue(true)
				.setComment("When enabled, the chunks you have marked for forceloading are forceloaded.\nIf the forceload limit has changed and you have more chunks marked than the new limit, then some of the chunks won't be forceloaded. Unmark any chunks until you are within the limit to ensure that all marked chunks are forceloaded.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		OFFLINE_FORCELOAD = PlayerConfigOptionSpec.FinalBuilder.begin(Boolean.class)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.forceload.offlineForceload")
				.setDefaultValue(false)
				.setComment("When enabled, the chunks you have marked for forceloading stay loaded even when you are offline (can significantly affect server performance!).\nIf your forceload limit is affected by your FTB Ranks rank/permissions, then you need to login at least once after a server (re)launch for it to take effect while you are offline.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);

		OPTIONS = Collections.unmodifiableMap(allOptions);
	}

}
