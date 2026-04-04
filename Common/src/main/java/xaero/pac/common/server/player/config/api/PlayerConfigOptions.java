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

import org.apache.commons.compress.utils.Lists;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.server.player.config.*;
import xaero.pac.common.server.player.config.change.PlayerConfigCommonChangeHandlers;

import java.util.*;

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
	 * The option used to store player groups. Can't be used directly, isn't up-to-date with effective player groups.
	 */
	public static final IPlayerConfigOptionSpecAPI<List<String>> CUSTOM_PLAYER_GROUPS;
	/**
	 * The number of bonus player groups that this player can create.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> BONUS_PLAYER_GROUPS;
	/**
	 * The bonus player group total that this player can create. The total is the sum of sizes of all player groups.
	 */
	public static final IPlayerConfigOptionSpecAPI<Integer> BONUS_PLAYER_GROUP_SPACE;
	/**
	 * Whether the player's claimed chunks are protected at all.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> PROTECT_CLAIMED_CHUNKS;
	/**
	 * Which player group has full access to the claimed chunks.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> FULL_ACCESS;
	/**
	 * Whether the claimed chunk protection makes an exception for players breaking and interacting with blocks.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_BLOCKS_BY_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for mobs breaking/placing blocks.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_BLOCKS_BY_MOBS;
	/**
	 * Whether the claimed chunk protection makes an exception for non-living entities breaking/placing blocks.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_BLOCKS_BY_OTHER;
	/**
	 * Whether the claimed chunk block protection redirects the used config option to the owner of the entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_BLOCKS_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes protection against fire spread.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_FIRE_SPREAD;
	/**
	 * Whether the claimed chunk protection makes an exception for frost walking.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_FROST_WALKING;
	/**
	 * Whether the claimed chunk protection includes protection against explosions.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS;
	/**
	 * Whether the claimed chunk protection makes an exception for buttons being pressed by projectiles.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_BUTTONS_BY_PROJECTILES;
	/**
	 * Whether the claimed chunk protection makes an exception for target blocks being pressed by projectiles.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_TARGETS_BY_PROJECTILES;
	/**
	 * Whether the claimed chunk protection makes an exception for pressure plates being pressed by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_PLATES_BY_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for pressure plates being pressed by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_PLATES_BY_MOBS;
	/**
	 * Whether the claimed chunk protection makes an exception for pressure plates being pressed by non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_PLATES_BY_OTHER;
	/**
	 * Whether the claimed chunk protection makes an exception for tripwires being pressed by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_TRIPWIRE_BY_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for tripwires being pressed by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_TRIPWIRE_BY_MOBS;
	/**
	 * Whether the claimed chunk protection makes an exception for tripwires being pressed by non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_TRIPWIRE_BY_OTHER;
	/**
	 * Whether the claimed chunk protection makes an exception for players interacting with entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ENTITIES_BY_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for mobs interacting with entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ENTITIES_BY_MOBS;
	/**
	 * Whether the claimed chunk protection makes an exception for non-living entities interacting with entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ENTITIES_BY_OTHER;
	/**
	 * Whether the claimed chunk entity protection redirects the used config option to the owner of the attacking entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_ENTITIES_REDIRECT;
	/**
	 * Whether the claimed chunk protection includes entity protection against explosions.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_ENTITIES_BY_EXPLOSIONS;
	/**
	 * Whether the claimed chunk protection includes entity protection against fire damage.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_ENTITIES_BY_FIRE;
	/**
	 * Whether the claimed chunk protection includes player protection against players.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_PLAYERS_BY_PLAYERS;
	/**
	 * Whether the claimed chunk protection includes player protection against mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_PLAYERS_BY_MOBS;
	/**
	 * Whether the claimed chunk protection includes player protection against non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_PLAYERS_BY_OTHER;
	/**
	 * Whether the claimed chunk player protection redirects the used config option to the owner of the attacking entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_PLAYERS_REDIRECT;
	/**
	 * Whether the claimed chunk protection makes an exception for chorus fruit teleportation into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_CHORUS_FRUIT;
	/**
	 * Whether the claimed chunk protection makes an exception for players using nether portals.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_NETHER_PORTALS_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for mobs using nether portals.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_NETHER_PORTALS_MOBS;
	/**
	 * Whether the claimed chunk protection makes an exception for non-living entities using nether portals.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_NETHER_PORTALS_OTHER;
	/**
	 * Whether the claimed chunk protection makes an exception for player-caused lightnings.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_PLAYER_LIGHTNING;
	/**
	 * Whether the claimed chunk protection includes protection against crop trample.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_CROP_TRAMPLE;
	/**
	 * Whether the claimed chunk protection includes protection against fluids flowing into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_FLUID_BARRIER;
	/**
	 * Whether the claimed chunk protection includes protection against directly dispensing into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_DISPENSER_BARRIER;
	/**
	 * Whether the claimed chunk protection includes protection against pistons pushing into the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_PISTON_BARRIER;
	/**
	 * Whether the claimed chunk protection makes an exception for items being dropped by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ITEM_TOSS_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for items being dropped by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ITEM_TOSS_MOBS;
	/**
	 * Whether the claimed chunk protection makes an exception for items being dropped by non-living entities.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ITEM_TOSS_OTHER;
	/**
	 * Whether the claimed chunk item drop protection redirects the used config option to the owner of the dropping entity.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_ITEM_TOSS_REDIRECT;
	/**
	 * Whether the claimed chunk protection makes an exception for mob loot being dropped.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_PROTECTION_MOB_LOOT;
	/**
	 * Whether the claimed chunk protection makes an exception for items dropped on player death.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_PROTECTION_PLAYER_DEATH_LOOT;
	/**
	 * Whether the claimed chunk protection makes an exception for items being picked up by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ITEM_PICKUP_PLAYERS;
	/**
	 * Whether the claimed chunk protection makes an exception for items being picked up by mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ITEM_PICKUP_MOBS;
	/**
	 * Whether the claimed chunk item pickup protection redirects the used config option to the owner of the entity picking up the item.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_ITEM_PICKUP_REDIRECT;
	/**
	 * Whether the claimed chunk protection makes an exception for experience orbs being picked up by players.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_XP_PICKUP;
	/**
	 * Whether the claimed chunk protection makes an exception for item use.
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_ITEM_USE;
	/**
	 * Whether the claimed chunk protection includes protection against at-air (or sometimes other) item use in
	 * neighbor chunks of the claim.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_PROTECTION_NEIGHBOR_CHUNKS_ITEM_USE;
	/**
	 * Whether the claimed chunk protection options for block/entity/items override the value of the vanilla "mob griefing" game rule.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_MOB_GRIEFING_OVERRIDE;
	/**
	 * Whether the claimed chunk protection includes protection against village raids.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_RAIDS;
	/**
	 * Whether the claimed chunk protection includes natural spawn prevention for hostile mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_HOSTILE_NATURAL_SPAWN;
	/**
	 * Whether the claimed chunk protection includes natural spawn prevention for friendly mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_FRIENDLY_NATURAL_SPAWN;
	/**
	 * Whether the claimed chunk protection disables spawners for hostile mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_HOSTILE_SPAWNERS;
	/**
	 * Whether the claimed chunk protection disables spawners for friendly mobs.
	 */
	public static final IPlayerConfigOptionSpecAPI<Boolean> CLAIM_EXCEPTION_FRIENDLY_SPAWNERS;
	/**
	 * Whether the claimed chunk protection makes an exception for hostile mobs being spawned by
	 * landing projectiles (e.g. endermites).
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_PROJECTILE_HIT_HOSTILE_SPAWN;
	/**
	 * Whether the claimed chunk protection makes an exception for non-hostile mobs being spawned by
	 * landing projectiles (e.g. chicken).
	 */
	public static final IPlayerConfigOptionSpecAPI<String> CLAIM_EXCEPTION_PROJECTILE_HIT_FRIENDLY_SPAWN;
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

		USED_SUBCLAIM = PlayerConfigListIterationOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.STRING)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER)
				.setServerSideListGetter(PlayerConfig::getSubConfigIds)
				.setClientSideListGetter(PlayerConfigClientStorage::getSubConfigIds)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.usedSub")
				.setDefaultValue(PlayerConfig.MAIN_SUB_ID)
				.setValueValidator(PlayerConfig::isValidSubId)
				.setComment("The current sub-config ID used for new chunk claims.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.setOverridable(false)
				.setForcedPlayerConfigurable(true)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleUsedSubClaim)
				.build(allOptions);
		USED_SERVER_SUBCLAIM = PlayerConfigListIterationOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.STRING)
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
				.setOverridable(false)
				.setForcedPlayerConfigurable(true)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleUsedSubClaim)
				.build(allOptions);

		CLAIMS_NAME = PlayerConfigStringOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.name")
				.setDefaultValue("")
				.setValueValidator(s -> s.matches("^(\\p{L}|[0-9 _'\"!?,\\-&%*\\(\\):])*$"))
				.setMaxLength(100)
				.setComment("When not empty, used as the name for your claimed chunks.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleClaimsProperty)
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
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleClaimsProperty)
				.build(allOptions);
		PARTY_NAME = PlayerConfigStringOptionSpec.Builder.begin()
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.name")
				.setDefaultValue("")
				.setValueValidator(s -> s.matches("^(\\p{L}|[0-9 _'\"!?,\\-&%*\\(\\):])*$"))
				.setMaxLength(100)
				.setComment("When not empty, used in some places as the name for the parties that you create.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handlePartyName)
				.build(allOptions);
		SHARE_LOCATION_WITH_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.shareLocationWithParty")
				.setDefaultValue(true)
				.setComment("When enabled, your position in the game is shared with players from the same party as you, which can be used by other mods, e.g. to display party members on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleShareLocationWithParty)
				.build(allOptions);
		SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.shareLocationWithMutualAllyParties")
				.setDefaultValue(false)
				.setComment("When enabled, your position in the game is shared with the mutual ally parties of the party that you are in, which can be used by other mods, e.g. to display party members on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleShareLocationWithAllies)
				.build(allOptions);
		RECEIVE_LOCATIONS_FROM_PARTY = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.receiveLocationsFromParty")
				.setDefaultValue(true)
				.setComment("When enabled, the sharable positions of players from the same party as you are shared with your game client, which can be used by other mods, e.g. to display party members on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleReceiveLocationsFromParty)
				.build(allOptions);
		RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "parties.receiveLocationsFromMutualAllyParties")
				.setDefaultValue(false)
				.setComment("When enabled, the sharable positions of players from the mutual ally parties of the party that you are in are shared with your game client, which can be used by other mods, e.g. to display allies on a map.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_PARTY)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleReceiveLocationsFromAllies)
				.build(allOptions);
		PROTECT_CLAIMED_CHUNKS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protectClaimedChunks")
				.setDefaultValue(true)
				.setComment("When enabled, the mod tries to protect your claimed chunks from other players. Workarounds are possible, especially with mods.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		CUSTOM_PLAYER_GROUPS = PlayerConfigOptionSpec.FinalBuilder
				.begin(PlayerConfigOptionValueTypes.getListType(PlayerConfigOptionValueTypes.STRING))
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "customPlayerGroups")
				.setDefaultValue(Lists.newArrayList())
				.setComment(".")
				.setCategory(PlayerConfigOptionCategory.GENERAL)
				.setOverridable(false)
				.setForcedPlayerConfigurable(true)
				.setDirectlyConfigurable(false)
				.setSyncable(false)//custom player groups are synced separately on demand
				.build(allOptions);
		BONUS_PLAYER_GROUPS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.INTEGER)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "bonusPlayerGroups")
				.setDefaultValue(0)
				.setComment("The number of additional player groups that you can create on top of the base limit.")
				.setCategory(PlayerConfigOptionCategory.GENERAL)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleAbstractBonusGroupLimit)
				.build(allOptions);
		BONUS_PLAYER_GROUP_SPACE = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.INTEGER)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "bonusPlayerGroupSpace")
				.setDefaultValue(0)
				.setComment("The bonus space (in entries) you have available for your player groups on top of the base space. The space is shared by all your player groups.")
				.setCategory(PlayerConfigOptionCategory.GENERAL)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleAbstractBonusGroupLimit)
				.build(allOptions);
		BONUS_CHUNK_CLAIMS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.INTEGER)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.bonusChunkClaims")
				.setDefaultValue(0)
				.setComment("The number of additional chunk claims that you can make on top of the normal limit.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleAbstractBonusClaims)
				.build(allOptions);
		BONUS_CHUNK_FORCELOADS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.INTEGER)
				.setConfigTypeFilter(t -> t == PlayerConfigType.PLAYER || t == PlayerConfigType.DEFAULT_PLAYER)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.bonusChunkForceloads")
				.setDefaultValue(0)
				.setComment("The number of additional chunk claim forceloads that you can make on top of the normal limit.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleBonusForceloads)
				.build(allOptions);
		FULL_ACCESS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.fullAccess")
				.setDefaultValue(PlayerConfigConstants.PARTY_EXCEPTION_ID)
				.setComment("The chosen group gets full access to the chunks claimed with this config.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.build(allOptions);
		CLAIM_EXCEPTION_BLOCKS_BY_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.blocksByPlayers")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for players in " +
								"the group breaking or otherwise interacting with blocks. Block placing is usually " +
								"additionally controlled by the item use protection.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_BLOCKS_BY_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.blocksByMobs")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for mobs " +
								"breaking/placing blocks (e.g. endermen) if the mob is owned by any player in the group. " +
								"Chunks directly next to the protected chunks are also partially protected when " +
								"protection is based on the mob griefing rule check. Protection should work for vanilla mob " +
								"behavior. Modded mob behavior is likely not to be protected. Feel free to set the " +
								"vanilla game rule for mob griefing for extra safety. Keep in mind that creeper " +
								"explosions are also affected by the explosion-related options.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_BLOCKS_BY_OTHER = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.blocksByOther")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for non-living " +
								"entities breaking/placing blocks if the entity is owned by any player in the group." +
								"Protection should work for vanilla entity behavior, unless another mod breaks it. " +
								"Modded entity behavior is likely not to be protected. Keep in mind that explosions " +
								"use separate explosion-related options.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_BLOCKS_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.blocksRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Allow Blocks By Mobs/Other\" option for block interactions coming from non-player entities, if the entity (e.g. an arrow) has an owner (e.g. a player), then the block exception option corresponding to the owner is used (e.g. \"Allow Blocks By Players\").\nChunk access is always tested against the owner, whether this is enabled or not.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.blocksByExplosions")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection makes an exception for blocks being exploded. Keep in mind that creeper explosions are also affected by the block mob protection.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_FIRE_SPREAD = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.fireSpread")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection allows fire spread.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_FROST_WALKING = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.frostWalking")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for frost walking " +
						"by players in the group or entities owned by any player in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_CROP_TRAMPLE = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.cropTrample")
				.setDefaultValue(false)
				.setComment("When enabled, claimed chunk protection makes an exception for crop trample " +
						"(falling on crops destroys them).")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_FLUID_BARRIER = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.fluidBarrier")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against fluids (e.g. lava) flowing into the protected chunks from outside. This does not protect wilderness.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_PISTON_BARRIER = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.pistonBarrier")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against being affected by pistons outside of the protected chunks. This does not protect wilderness.")
				.setCategory(PlayerConfigOptionCategory.BLOCK_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_BUTTONS_BY_PROJECTILES = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.buttonsByProjectiles")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for buttons being " +
							"pressed by projectiles owned by the players in the group.\n" +
						PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_TARGETS_BY_PROJECTILES = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.targetsByProjectiles")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for target blocks " +
								"(the vanilla block called that) being hit by projectiles owned by the players in " +
								"the group.\n" +
						PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_PLATES_BY_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.platesByPlayers")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for pressure plates " +
								"being pressed by players in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_PLATES_BY_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.platesByMobs")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for pressure plates " +
								"being pressed by mobs owned by players in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_PLATES_BY_OTHER = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.platesByOther")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for pressure plates " +
								"being pressed by non-living entities owned by players in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_TRIPWIRE_BY_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.tripwireByPlayers")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for tripwires being " +
								"triggered by players in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_TRIPWIRE_BY_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.tripwireByMobs")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for tripwires being " +
								"triggered by mobs owned by players in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_TRIPWIRE_BY_OTHER = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.tripwireByOther")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for tripwires being " +
								"triggered by non-living entities owned by players in the group.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.BLOCK_TRIGGERS)
				.build(allOptions);
		CLAIM_EXCEPTION_ENTITIES_BY_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.entitiesByPlayers")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for players " +
								"in the group interacting with friendly (+ server configured) entities.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ENTITIES_BY_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.entitiesByMobs")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for mobs owned " +
								"by any player in the group interacting with friendly (+ server configured) entities. " +
								"Chunks directly next to the protected chunks are also partially protected when " +
								"protection is based on the mob griefing rule check.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ENTITIES_BY_OTHER = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.entitiesByOther")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for non-living " +
								"entities (e.g. arrows, falling anvils, activated TNT) owned by any player in the " +
								"group interacting with friendly (+ server configured) entities.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ENTITIES_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.entitiesRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Allow Entities By Mobs/Other\" option for entity attacks/interactions coming from non-player entities, if the attacking entity (e.g. an arrow) has an owner (e.g. a player), then the entity exception option corresponding to the owner is used (e.g. \"Allow Entities By Players\").\nChunk access is always tested against the owner, whether this is enabled or not.")
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ENTITIES_BY_EXPLOSIONS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.entitiesByExplosions")
				.setDefaultValue(false)
				.setComment(
						"When enabled, claimed chunk protection makes an exception for friendly " +
						"(+ server configured) entities in the chunks being affected by explosions. Explosions " +
						"directly activated by the chunk owner are unprotected by default."
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ENTITIES_BY_FIRE = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.entitiesByFire")
				.setDefaultValue(false)
				.setComment(
						"When enabled, claimed chunk protection makes an exception for friendly " +
								"(+ server configured) entities in the chunks being hurt by fire."
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_RAIDS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.raids")
				.setDefaultValue(false)
				.setComment(
						"When enabled, claimed chunk protection includes makes an exception for village raids. " +
								"The protection stops raiders from spawning inside the protected chunks, from entering them " +
								"and from hurting protectable entities, even if entity protection is turned off."
				)
				.setCategory(PlayerConfigOptionCategory.ENTITY_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PLAYERS_BY_PLAYERS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.playersByPlayers")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection makes an exception for player VS player combat.")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PLAYERS_BY_MOBS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.playersByMobs")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection makes an exception for players being attacked by mobs.")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PLAYERS_BY_OTHER = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.playersByOther")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection makes an exception for players being attacked by non-living entities.")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PLAYERS_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.playersRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Allow Players By Mobs/Other\" option for entity attacks/iteractions coming from non-player entities, if the attacking entity (e.g. an arrow) has an owner (e.g. a player), then the entity protection option corresponding to the owner is used (e.g. \"Allow Players By Players\").")
				.setCategory(PlayerConfigOptionCategory.PLAYER_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PLAYER_LIGHTNING = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.playerLightning")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for lightning " +
								"directly caused by players (e.g. with the trident) affecting blocks and entities. " +
								"Chunks directly next to the protected chunks are also partially protected.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_CHORUS_FRUIT = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.chorusFruitTeleport")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for " +
								"chorus fruit teleportation.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		CLAIM_EXCEPTION_NETHER_PORTALS_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.netherPortalsPlayers")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for nether portal " +
								"usage by players. \n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		CLAIM_EXCEPTION_NETHER_PORTALS_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.netherPortalsMobs")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for nether portal " +
								"usage by mobs. Even after the exception is turned on, a recently stopped entity is " +
								"still on a short cooldown. You must let it finish without constantly retrying to " +
								"push it through the portal, which restarts the cooldown.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		CLAIM_EXCEPTION_NETHER_PORTALS_OTHER = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.netherPortalsOther")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for nether portal " +
								"usage by non-living entities. Even after the exception is turned on, a recently " +
								"stopped entity is still on a short cooldown. You must let it finish without " +
								"constantly retrying to push it through the portal, which restarts the cooldown.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MOVEMENT)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_USE = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemUse")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for right-click " +
								"held item use. On Fabric, allowed item use means being able to place blocks on " +
								"blocks that you can interact with, e.g. exception blocks, even if block protection " +
								"is enabled! Right-click item use can also break blocks, if that is the item's " +
								"right-click mechanic. Some item use in a chunk might also be prevented by neighbor " +
								"item use protection in neighbor chunks.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PROTECTION_FROM_ITEMS)
				.build(allOptions);
		CLAIM_PROTECTION_NEIGHBOR_CHUNKS_ITEM_USE = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.neighborChunksItemUse")
				.setDefaultValue(true)
				.setComment("When enabled, the item use protection is extended to some right-click held item use in chunks directly next to the claimed ones. Item use affected by this is usually things that still work while looking at the sky (not block or entity) or item use with custom ray-tracing for blocks/fluids/entities (e.g. placing things on water), but also any item use of \"additional banned items\" configured on the server. Item use protection exceptions (e.g. food, potions etc) still apply.")
				.setCategory(PlayerConfigOptionCategory.PROTECTION_FROM_ITEMS)
				.build(allOptions);
		CLAIM_DISPENSER_BARRIER = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.dispenserBarrier")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection includes protection against dispensers that are \"touching\" and facing the protected chunks from outside. This does not protect wilderness.")
				.setCategory(PlayerConfigOptionCategory.PROTECTION_FROM_ITEMS)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_TOSS_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemTossPlayers")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for " +
								"item tossing by players in the group.\nDying can be used to circumvent " +
								"the protection, so it is recommended to enable keepInventory or use a gravestone mod.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_TOSS_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemTossMobs")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for " +
								"item tossing by mobs owned by any player in the group. Some modded mob tossing isn't " +
								"protected to begin with. It requires the tossing mob to be set as the item's thrower." +
								"\nModded mobs are pretty likely to do it themselves or have it done by this mod. " +
								"Otherwise, the toss won't be prevented.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_TOSS_OTHER = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemTossOther")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for " +
								"item tossing by non-living entities owned by any player in the group. " +
								"The protection requires the tossing mob to be set as the item's thrower. " +
								"Some modded entities might not be protected from to begin with.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_TOSS_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemTossRedirect")
				.setDefaultValue(true)
				.setComment("When enabled, instead of always simply using the direct \"Allow Mob/Other Item Toss\" option for item tosses coming from non-player entities, if the tossing entity (e.g. a special arrow) has an owner (e.g. a player), then the item toss protection option corresponding to the owner is used (e.g. \"Allow Player Item Toss\").")
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		CLAIM_PROTECTION_MOB_LOOT = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.mobLoot")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for loot being " +
								"dropped when mobs are killed by players in the group. Any non-living entity spawned " +
								"on a mob's death is considered loot.\n"
								+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.MIXED_PROTECTION)
				.build(allOptions);
		CLAIM_PROTECTION_PLAYER_DEATH_LOOT = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.playerDeathLoot")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection includes protection for items and " +
								"experience that have been dropped on a player death, if the player is in the group, " +
								"even if the standard item pickup is allowed. The protected items are only accessible to " +
								"the player that dropped them and the entity/player that killed the player.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_PICKUP_PLAYERS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemPickupPlayers")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for " +
								"players picking up items they don't own.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_PICKUP_MOBS = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemPickupMobs")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for " +
								"mobs picking up items they don't own. " +
								"Protection might not work for some mobs. Chunks directly next to the protected chunks are " +
								"also partially protected when protection is based on the mob griefing rule check.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_OWNED
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_ITEM_PICKUP_REDIRECT = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.itemPickupRedirect")
				.setDefaultValue(false)
				.setComment("When enabled, instead of always simply using the direct \"Allow Items By Mobs\" option for item pickups coming from mobs, if the mob (e.g. an allay) has an owner (e.g. a player), then the item protection option corresponding to the owner is used (e.g. \"Allow Items By Players\").")
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_XP_PICKUP = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.xpPickup")
				.setDefaultValue(PlayerConfigConstants.EVERYONE_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for players picking " +
								"up experience orbs they don't own.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PLAYERS
				)
				.setCategory(PlayerConfigOptionCategory.PICKUP_PROTECTION)
				.build(allOptions);
		CLAIM_MOB_GRIEFING_OVERRIDE = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
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
		CLAIM_EXCEPTION_HOSTILE_NATURAL_SPAWN = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.naturalSpawnHostile")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection allows the natural spawning of hostile mobs.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_FRIENDLY_NATURAL_SPAWN = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.naturalSpawnFriendly")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection allows the natural spawning of friendly mobs.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_HOSTILE_SPAWNERS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.spawnersHostile")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection allows hostile mob spawners to operate.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_FRIENDLY_SPAWNERS = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.spawnersFriendly")
				.setDefaultValue(true)
				.setComment("When enabled, claimed chunk protection allows friendly mob spawners to operate.")
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PROJECTILE_HIT_HOSTILE_SPAWN = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.projectileHitHostileSpawn")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for projectiles " +
								"owned by any player in the group spawning hostile mobs when they land (e.g. endermites). " +
								"Protection might not work with projectiles from mods that don't implement this mod's API.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PROJECTILE
				)
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);
		CLAIM_EXCEPTION_PROJECTILE_HIT_FRIENDLY_SPAWN = PlayerConfigPlayerGroupOptionSpec.Builder.begin()
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptions.projectileHitFriendlySpawn")
				.setDefaultValue(PlayerConfigConstants.NO_EXCEPTION_ID)
				.setComment(
						"When a player group is chosen, claimed chunk protection makes an exception for projectiles " +
								"owned by any player in the group spawning non-hostile mobs when they land " +
								"(e.g. chicken). Protection might not work with projectiles from mods that don't " +
								"implement this mod's API.\n"
						+ PlayerConfig.BUILTIN_EXCEPTION_LEVELS_TOOLTIP_PROJECTILE
				)
				.setCategory(PlayerConfigOptionCategory.SPAWN_PROTECTION)
				.build(allOptions);

		FORCELOAD = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.forceload.enabled")
				.setDefaultValue(true)
				.setComment("When enabled, the chunks you have marked for forceloading are forceloaded.\nIf the forceload limit has changed and you have more chunks marked than the new limit, then some of the chunks won't be forceloaded. Unmark any chunks until you are within the limit to ensure that all marked chunks are forceloaded.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleForceloading)
				.build(allOptions);
		OFFLINE_FORCELOAD = PlayerConfigOptionSpec.FinalBuilder.begin(PlayerConfigOptionValueTypes.BOOLEAN)
				.setId(PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.forceload.offlineForceload")
				.setDefaultValue(false)
				.setComment("When enabled, the chunks you have marked for forceloading stay loaded even when you are offline (can significantly affect server performance!).\nIf your forceload limit is affected by your FTB Ranks rank/permissions, then you need to login at least once after a server (re)launch for it to take effect while you are offline.")
				.setCategory(PlayerConfigOptionCategory.GENERAL_CLAIMS)
				.setOverridable(false)
				.setServerChangeHandler(PlayerConfigCommonChangeHandlers::handleForceloading)
				.build(allOptions);

		OPTIONS = Collections.unmodifiableMap(allOptions);
	}

}
