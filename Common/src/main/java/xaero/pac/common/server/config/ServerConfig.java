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

package xaero.pac.common.server.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {

	public final ForgeConfigSpec.ConfigValue<String> defaultLanguage;
	public final ForgeConfigSpec.BooleanValue partiesEnabled;
	public final ForgeConfigSpec.BooleanValue claimsEnabled;
	public final ForgeConfigSpec.IntValue autosaveInterval;
	public final ForgeConfigSpec.IntValue playerSubConfigLimit;
	public final ForgeConfigSpec.IntValue partyExpirationTime;
	public final ForgeConfigSpec.IntValue partyExpirationCheckInterval;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> opConfigurablePlayerConfigOptions;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> playerConfigurablePlayerConfigOptions;
	public final ForgeConfigSpec.EnumValue<ConfigListType> friendlyChunkProtectedEntityListType;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> friendlyChunkProtectedEntityList;
	public final ForgeConfigSpec.EnumValue<ConfigListType> hostileChunkProtectedEntityListType;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> hostileChunkProtectedEntityList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockProtectionExceptionList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entityProtectionExceptionList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entityClaimBarrierList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> forcedBlockProtectionExceptionList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockProtectionOptionalExceptionGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> forcedEntityProtectionExceptionList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entityProtectionOptionalExceptionGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> forcedEntityClaimBarrierList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entityClaimBarrierOptionalGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entitiesAllowedToGrief;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entitiesAllowedToGriefEntities;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entitiesAllowedToGriefDroppedItems;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> nonBlockGriefingMobs;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entityGriefingMobs;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> droppedItemGriefingMobs;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockAccessEntityGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> entityAccessEntityGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> droppedItemAccessEntityGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> staticFakePlayers;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> staticFakePlayerClassExceptions;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> additionalBannedItemsList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> itemUseProtectionExceptionList;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> itemUseProtectionOptionalExceptionGroups;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> completelyDisabledItemInteractions;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> completelyDisabledBlockInteractions;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> completelyDisabledEntityInteractions;
	public final ForgeConfigSpec.BooleanValue completelyDisableFrostWalking;
	public final ForgeConfigSpec.IntValue maxClaimDistance;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> claimableDimensionsList;
	public final ForgeConfigSpec.EnumValue<ConfigListType> claimableDimensionsListType;
	public final ForgeConfigSpec.BooleanValue allowExistingClaimsInUnclaimableDimensions;
	public final ForgeConfigSpec.BooleanValue allowExistingForceloadsInUnclaimableDimensions;
	public final ForgeConfigSpec.IntValue maxPlayerClaims;
	public final ForgeConfigSpec.IntValue maxPlayerClaimForceloads;
	public final ForgeConfigSpec.IntValue maxPartyMembers;
	public final ForgeConfigSpec.IntValue maxPartyAllies;
	public final ForgeConfigSpec.IntValue maxPartyInvites;
	public final ForgeConfigSpec.IntValue playerClaimsExpirationTime;
	public final ForgeConfigSpec.IntValue playerClaimsExpirationCheckInterval;
	public final ForgeConfigSpec.BooleanValue playerClaimsConvertExpiredClaims;
	public final ForgeConfigSpec.EnumValue<ClaimsSyncType> claimsSynchronization;
	public final ForgeConfigSpec.ConfigValue<String> maxPlayerClaimsPermission;
	public final ForgeConfigSpec.ConfigValue<String> maxPlayerClaimForceloadsPermission;
	public final ForgeConfigSpec.ConfigValue<String> serverClaimPermission;
	public final ForgeConfigSpec.ConfigValue<String> adminModePermission;
	public final ForgeConfigSpec.ConfigValue<String> permissionSystem;
	public final ForgeConfigSpec.ConfigValue<String> primaryPartySystem;

	private ServerConfig(ForgeConfigSpec.Builder builder) {
		builder.push("serverConfig");

		defaultLanguage = builder
			.comment("The default language used for server-side localization for players that don't have the mod installed.")
			.translation("gui.xaero_pac_config_default_language")
			.worldRestart()
			.define("defaultLanguage", "en_us");

		autosaveInterval = builder
			.comment("How often to auto-save modified data, e.g. parties, claims, player configs (in minutes).")
			.translation("gui.xaero_pac_config_autosave_interval")
			.worldRestart()
			.defineInRange("autosaveInterval", 10, 1, Integer.MAX_VALUE);

		playerSubConfigLimit = builder
			.comment("How many sub-configs (sub-claims) can each player create.")
			.translation("gui.xaero_pac_config_player_subconfig_limit")
			.worldRestart()
			.defineInRange("playerSubConfigLimit", 64, 0, 1024);

		permissionSystem = builder
			.comment("The permission system to use for everything that requires permission checks (e.g. permission_api, ftb_ranks, luck_perms, prometheus). Non-built-in permission systems can be registered through the API with an addon.")
			.translation("gui.xaero_pac_config_permission_system")
			.worldRestart()
			.define("permissionSystem", "prometheus");

		primaryPartySystem = builder
			.comment("The player party system to prefer and use for anything that can't support multiple systems (e.g. default, ftb_teams, argonauts, argonauts_guilds). Non-built-in party systems can be registered through the API with an addon.")
			.translation("gui.xaero_pac_config_primary_party_system")
			.worldRestart()
			.define("primaryPartySystem", "argonauts_guilds");

		builder.push("parties");

		partiesEnabled = builder
			.comment("Whether the parties part of this mod is enabled.")
			.translation("gui.xaero_pac_config_parties_enabled")
			.worldRestart()
		   	.define("enabled", true);
		
		maxPartyMembers = builder
			.comment("The maximum number of members in a party. Existing members are not removed if the limit is reduced.")
			.translation("gui.xaero_pac_config_party_max_members")
			.worldRestart()
			.defineInRange("maxPartyMembers", 64, 1, Integer.MAX_VALUE);
		
		maxPartyAllies = builder
			.comment("The maximum number of allies for a party. Existing allies are not removed if the limit is reduced.")
			.translation("gui.xaero_pac_config_party_max_allies")
			.worldRestart()
			.defineInRange("maxPartyAllies", 64, 0, Integer.MAX_VALUE);
		
		maxPartyInvites = builder
			.comment("The maximum number of invites to a party. Existing invites are not removed if the limit is reduced.")
			.translation("gui.xaero_pac_config_party_max_invites")
			.worldRestart()
			.defineInRange("maxPartyInvites", 16, 1, Integer.MAX_VALUE);
		
		partyExpirationTime = builder
			.comment("For how long a party (members) can stay completely inactive on the server until it is deleted (in hours). This improves performance for servers running for years.")
			.translation("gui.xaero_pac_config_party_expiration_time")
			.worldRestart()
			.defineInRange("partyExpirationTime", 168, 1, Integer.MAX_VALUE);
		
		partyExpirationCheckInterval = builder
			.comment("How often to check for expired parties in order to remove them (in minutes). The interval is effectively rounded up to a multiple of 10 minutes.")
			.translation("gui.xaero_pac_config_party_expiration_check_interval")
			.worldRestart()
			.defineInRange("partyExpirationCheckInterval", 6 * 60, 10, Integer.MAX_VALUE);
		
		builder.pop();
		
		builder.push("claims");

		claimsEnabled = builder
			.comment("Whether the claims part of this mod is enabled.")
			.translation("gui.xaero_pac_config_claims_enabled")
			.worldRestart()
		   	.define("enabled", true);
		
		playerClaimsExpirationTime = builder
			.comment("For how long a player can stay completely inactive on the server until their claims are expired (in hours). This improves performance for servers running for years.")
			.translation("gui.xaero_pac_config_claims_expiration_time")
			.worldRestart()
			.defineInRange("playerClaimsExpirationTime", 8760, 1, Integer.MAX_VALUE);
		
		playerClaimsExpirationCheckInterval = builder
			.comment("How often to check for expired player chunk claims in order to remove them (in minutes). The interval is effectively rounded up to a multiple of 10 minutes.")
			.translation("gui.xaero_pac_config_claims_expiration_check_interval")
			.worldRestart()
			.defineInRange("playerClaimsExpirationCheckInterval", 6 * 60, 10, Integer.MAX_VALUE);

		playerClaimsConvertExpiredClaims = builder
			.comment("Whether to convert expired player chunk claims to \"expired claims\" instead of completely freeing them. This shouldn't be too bad for performance because it still reduces the number of unique claims.")
			.translation("gui.xaero_pac_config_keep_expired_claims")
			.worldRestart()
		   	.define("playerClaimsConvertExpiredClaims", true);
		
		maxPlayerClaims = builder
			.comment("""
					The maximum number of chunks that a player can claim. Additional claims can be configured in the player config.
					This value can be overridden with a player permission.""")
			.translation("gui.xaero_pac_config_max_player_claims")
			.worldRestart()
			.defineInRange("maxPlayerClaims", 500, 0, Integer.MAX_VALUE);
		
		maxPlayerClaimForceloads = builder
			.comment("""
					The maximum number of claimed chunks that a player can forceload. Additional forceloads can be configured in the player config.
					This value can be overridden with a player permission.""")
			.translation("gui.xaero_pac_config_max_player_forceloads")
			.worldRestart()
			.defineInRange("maxPlayerClaimForceloads", 10, 0, Integer.MAX_VALUE);

		maxPlayerClaimsPermission = builder
			.comment("The permission that should override the default \"maxPlayerClaims\" value. Set it to an empty string to never check permissions. The used permission system can be configured with \"permissionSystem\".")
			.translation("gui.xaero_pac_config_max_claims_permission")
			.worldRestart()
			.define("maxPlayerClaimsPermission", UsedPermissionNodes.MAX_PLAYER_CLAIMS.getDefaultNodeString());

		maxPlayerClaimForceloadsPermission = builder
			.comment("""
					The permission that should override the default "maxPlayerClaimForceloads" value. Set it to an empty string to never check permissions.
					The permission override only takes effect after the player logs in at least once after a server (re)launch, so it is recommended to keep all permission-based forceload limits equal to or greater than "maxPlayerClaimForceloads".
					The used permission system can be configured with "permissionSystem".""")
			.translation("gui.xaero_pac_config_max_claims_permission")
			.worldRestart()
			.define("maxPlayerClaimForceloadsPermission", UsedPermissionNodes.MAX_PLAYER_FORCELOADS.getDefaultNodeString());

		serverClaimPermission = builder
			.comment("The permission that gives non-OP players the ability to make server claims and enable server claim mode. The used permission system can be configured with \"permissionSystem\".")
			.translation("gui.xaero_pac_config_server_claim_permission")
			.worldRestart()
			.define("serverClaimPermission", UsedPermissionNodes.SERVER_CLAIMS.getDefaultNodeString());

		adminModePermission = builder
			.comment("The permission that gives non-OP players the ability to enable claim admin mode. The used permission system can be configured with \"permissionSystem\".")
			.translation("gui.xaero_pac_config_admin_mode_permission")
			.worldRestart()
			.define("adminModePermission", UsedPermissionNodes.ADMIN_MODE.getDefaultNodeString());

		maxClaimDistance = builder
			.comment("The maximum distance on the X or Z axis (forming a square) that a chunk can be claimed at by a player.")
			.translation("gui.xaero_pac_config_max_claim_distance")
			.worldRestart()
		   	.defineInRange("maxClaimDistance", 5, 0, Integer.MAX_VALUE);

		claimableDimensionsListType = builder
			.comment("The type of the list defined in \"claimableDimensionsList\". ONLY - include only the listed dimensions. ALL_BUT - include all but the listed dimensions.")
			.translation("gui.xaero_pac_config_claimable_dimensions_list_type")
			.worldRestart()
		   	.defineEnum("claimableDimensionsListType", ConfigListType.ALL_BUT);
		
		claimableDimensionsList = builder
			.comment("""
					Dimensions to include/exclude from being claimable, depending on the list type in "claimableDimensionsListType".
					For example ["minecraft:overworld", "minecraft:the_nether"].
					By default the list is empty and of type ALL_BUT, meaning that all dimensions are claimable.""")
			.translation("gui.xaero_pac_config_claimable_dimensions_list")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("claimableDimensionsList"), ArrayList::new, s -> s instanceof String);

		allowExistingClaimsInUnclaimableDimensions = builder
			.comment("Whether to allow existing player claims to stay active in unclaimable dimensions which were previously claimable.")
			.translation("gui.xaero_pac_config_allow_existing_claims_in_unclaimable_dims")
			.worldRestart()
		   	.define("allowExistingClaimsInUnclaimableDimensions", true);

		allowExistingForceloadsInUnclaimableDimensions = builder
			.comment("Whether to allow existing player forceloads to stay active in unclaimable dimensions which were previously claimable. Only relevant if existing claims are allowed.")
			.translation("gui.xaero_pac_config_allow_existing_forceloads_in_unclaimable_dims")
			.worldRestart()
		   	.define("allowExistingForceloadsInUnclaimableDimensions", false);

		claimsSynchronization = builder
			.comment("""
					Whether to synchronize world chunk claims to the game clients. Enables client-side mods to access the claims data, e.g. to display it on a map.
					ALL - all claims are synced. OWNED_ONLY - only the claims that the client player owns and server claims are synced. NOT_SYNCED - claims are not synced.""")
			.translation("gui.xaero_pac_config_claims_synchronization")
			.worldRestart()
		   	.defineEnum("claimsSynchronization", ClaimsSyncType.ALL);

		builder.push("protection");

		friendlyChunkProtectedEntityListType = builder
			.comment("The type of the list defined in \"friendlyChunkProtectedEntityList\". ONLY - include only the listed entities. ALL_BUT - include all but the listed entities.")
			.translation("gui.xaero_pac_config_friendly_protected_entities_list_type")
			.worldRestart()
		   	.defineEnum("friendlyChunkProtectedEntityListType", ConfigListType.ALL_BUT);
		
		friendlyChunkProtectedEntityList = builder
			.comment("""
					Friendly entities to fully include/exclude in chunk protection, depending on the list type in "friendlyChunkProtectedEntityListType". Supports entity type tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["*:villager", "minecraft:m(ule|ooshroom)", "#minecraft:axolotl_hunt_targets"].
					By default the list is empty with the type set to ALL_BUT, which means that all friendly entities are included.""")
			.translation("gui.xaero_pac_config_friendly_protected_entities")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("friendlyChunkProtectedEntityList"), () -> Lists.newArrayList("minecraft:boat"), s -> s instanceof String);

		hostileChunkProtectedEntityListType = builder
			.comment("The type of the list defined in \"hostileChunkProtectedEntityList\". ONLY - include only the listed entities. ALL_BUT - include all but the listed entities.")
			.translation("gui.xaero_pac_config_hostile_protected_entities_list_type")
			.worldRestart()
		   	.defineEnum("hostileChunkProtectedEntityListType", ConfigListType.ONLY);
		
		hostileChunkProtectedEntityList = builder
			.comment("""
					Hostile entities to fully include/exclude in chunk protection, depending on the list type in "hostileChunkProtectedEntityListType". Supports entity type tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(|wither_)skeleton", "minecraft:zombie(_villager|)", "#minecraft:raiders"]""")
			.translation("gui.xaero_pac_config_hostile_protected_entities")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("hostileChunkProtectedEntityList"), ArrayList::new, s -> s instanceof String);

		blockProtectionExceptionList = builder
			.comment("""
				No longer a working option. Please transfer anything you still have here to "forcedBlockProtectionExceptionList" or "blockProtectionOptionalExceptionGroups",
				but keep in mind that those options work differently and please read their comments.
				This option will be completely removed on the full release of the mod.""")
			.translation("gui.xaero_pac_config_block_protection_exception")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("blockProtectionExceptionList"), Lists::newArrayList, s -> s instanceof String);
		entityProtectionExceptionList = builder
			.comment("""
				No longer a working option. Please transfer anything you still have here to "forcedEntityProtectionExceptionList" or "entityProtectionOptionalExceptionGroups",
				but keep in mind that those options work differently and please read their comments.
				This option will be completely removed on the full release of the mod.""")
			.translation("gui.xaero_pac_config_entity_protection_exception")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entityProtectionExceptionList"), Lists::newArrayList, s -> s instanceof String);
		entityClaimBarrierList = builder
			.comment("""
				No longer a working option. Please transfer anything you still have here to "forcedEntityClaimBarrierList" or "entityClaimBarrierOptionalGroups",
				but keep in mind that those options work differently and please read their comments.
				This option will be completely removed on the full release of the mod.""")
			.translation("gui.xaero_pac_config_entity_claim_barrier_list")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entityClaimBarrierList"), Lists::newArrayList, s -> s instanceof String);

		forcedBlockProtectionExceptionList = builder
			.comment("""
					Blocks to partially exclude from chunk protection. Supports block tags.
					Just a block/tag ID in the list, e.g. "minecraft:lever" allows block interaction across the server if the item in the used hand isn't blocking it.
					Prefix "hand$" is the same as no prefix but enforces an empty hand requirement in protected chunks. Prefix "break$" allows breaking the block(s).
					Prefix "anything$" is the same as no prefix but allows interaction with any item held in the hand. Please make sure that no item does anything bad when used at a block with such an exception.
					Add the same block/tag multiple times to use multiple prefixes. Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:lever", "minecraft:*_button", "break$minecraft:*_button", "break$minecraft:(*_|)sand"]""")
			.translation("gui.xaero_pac_config_block_protection_forced_exception")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("forcedBlockProtectionExceptionList"), () -> Lists.newArrayList("minecraft:crafting_table"), s -> s instanceof String);

		blockProtectionOptionalExceptionGroups = builder
			.comment("""
					Custom groups of blocks that a player/claim config should be able to make protection exceptions for. Each group can consist of multiple blocks and block tags.
					A group without a prefix creates a player config option for the right-click interaction with the group blocks. The format for a block group is <group ID>{<blocks/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-DIRT35{minecraft:dirt, minecraft:grass_block, minecraft:(oak|spruce)_*}".
					A group can be prefixed with "hand$" to create an option for the right-click interaction with an enforced empty hand requirement or "break$" for breaking the group blocks.
					Moreover, prefix "anything$" creates an option for the right-click interaction with any item held in the hand, not just allowed items. Please make sure that no item does anything
					bad when used at a block with such an exception.
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_block_protection_exception_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("blockProtectionOptionalExceptionGroups"),
					() -> Lists.newArrayList(
							"Controls{minecraft:lever, #minecraft:buttons}",
							"Doors{#minecraft:doors, #minecraft:fence_gates, #forge:fence_gates, #minecraft:trapdoors}",
							"Chests{minecraft:chest, minecraft:trapped_chest, #forge:chests/wooden}",
							"Barrels{minecraft:barrel, #forge:barrels}",
							"Ender_Chests{minecraft:ender_chest, #forge:chests/ender}",
							"Shulker_Boxes{#minecraft:shulker_boxes}",
							"Furnaces{minecraft:furnace, minecraft:blast_furnace, minecraft:smoker}",
							"Hoppers{minecraft:hopper}",
							"Dispenser-like{minecraft:dispenser, minecraft:dropper}",
							"Anvils{#minecraft:anvil}",
							"Beds{#minecraft:beds}",
							"Beacons{minecraft:beacon}",
							"Enchanting_Tables{minecraft:enchanting_table}",
							"break$Crops{#minecraft:crops}"
					), s -> s instanceof String);

		forcedEntityProtectionExceptionList = builder
			.comment("""
					Entities to partially exclude from chunk protection. Supports entity type tags.
					Just an entity/tag ID in the list, e.g. "minecraft:horse" allows entity interaction across the server if the item in the used hand isn't blocking it.
					Prefix "hand$" is the same as no prefix but enforces an empty hand requirement in protected chunks. Prefix "break$" allows killing the entities across the server.
					Prefix "anything$" is the same as no prefix but allows interaction with any item held in the hand. Please make sure that no item does anything bad when used at an entity with such an exception.
					Add the same entity/tag multiple times to use multiple prefixes. Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:villager", "break$minecraft:villager", "break$minecraft:(trader_|)llama"]""")
			.translation("gui.xaero_pac_config_entity_protection_forced_exception")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("forcedEntityProtectionExceptionList"), () -> Lists.newArrayList("minecraft:minecart"), s -> s instanceof String);

		entityProtectionOptionalExceptionGroups = builder
			.comment("""
					Custom groups of entities that a player/claim config should be able to make protection exceptions for. Each group can consist of multiple entities and entity tags.
					A group without a prefix creates a player config option for the right-click interaction with the group entities. The format for an entity group is <group ID>{<entities/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-GUYS98{minecraft:pig, minecraft:c(ow|at), #minecraft:beehive_inhabitors}".
					A group can be prefixed with "hand$" to create an option for the right-click interaction with an enforced empty hand requirement or "break$" for destroying the group entities.
					Moreover, prefix "anything$" creates an option for the right-click interaction with any item held in the hand, not just allowed items. Please make sure that no item does anything
					bad when used at an entity with such an exception.
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_entity_protection_exception_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entityProtectionOptionalExceptionGroups"),
					() -> Lists.newArrayList(
							"Traders{minecraft:villager, minecraft:wandering_trader}",
							"hand$Item_Frames{minecraft:item_frame}",
							"break$Livestock{minecraft:cow, minecraft:mooshroom, minecraft:sheep, minecraft:chicken, minecraft:pig, minecraft:rabbit, minecraft:goat}",
							"Armor_Stands{minecraft:armor_stand}"
					), s -> s instanceof String);

		forcedEntityClaimBarrierList = builder
			.comment("""
					Entities that are prevented from entering the claim. Supports entity type tags. An entity/tag ID in the list, e.g. "minecraft:falling_block" prevents the entities from entering.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR. For example: "minecraft:zombie(_villager|)".""")
			.translation("gui.xaero_pac_config_entity_forced_claim_barrier_list")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("forcedEntityClaimBarrierList"), () -> Lists.newArrayList("minecraft:falling_block", "supplementaries:slingshot_projectile"), s -> s instanceof String);

		entityClaimBarrierOptionalGroups = builder
			.comment("""
					Custom groups of entities that a player/claim config should be able to enable a barrier for. Each group can consist of multiple entities and entity tags.
					Each group creates a player config option for controlling the entity barrier. The format for a entity group is <group ID>{<entities/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-GUYS98{#minecraft:raiders, minecraft:zombie(_villager|)}".
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_entity_claim_barrier_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entityClaimBarrierOptionalGroups"),
					() -> Lists.newArrayList(
							"Players{minecraft:player}",
							"Ender_Pearls{minecraft:ender_pearl}"
					), s -> s instanceof String);

		entitiesAllowedToGrief = builder
			.comment("""
					Entities that can bypass all block protection. Supports entity type tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(v|p)illager", "minecraft:*illager", "#minecraft:raiders"]""")
			.translation("gui.xaero_pac_config_entities_allowed_to_grief")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entitiesAllowedToGrief"), () -> Lists.newArrayList("minecraft:sheep"), s -> s instanceof String);
		entitiesAllowedToGriefEntities = builder
			.comment("""
					Entities that can bypass all protection of other entities. Supports entity type tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(v|p)illager", "minecraft:*illager", "#minecraft:raiders"]""")
			.translation("gui.xaero_pac_config_entities_allowed_to_grief_entities")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entitiesAllowedToGriefEntities"), Lists::newArrayList, s -> s instanceof String);
		entitiesAllowedToGriefDroppedItems = builder
			.comment("""
					Entities that can bypass all dropped item protection. Supports entity type tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(v|p)illager", "minecraft:*illager", "#minecraft:raiders"]""")
			.translation("gui.xaero_pac_config_entities_allowed_to_grief_items")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entitiesAllowedToGriefDroppedItems"), Lists::newArrayList, s -> s instanceof String);
		nonBlockGriefingMobs = builder
			.comment(
					"""
					(Forge-only option) Mobs that can grief entities/items but not blocks. This list is used when overriding the vanilla "mob griefing" game rule value.
					By default, the mod assumes that any "mob griefing" game rule check is meant for block protection.
					This means that the "Protect Blocks From Mobs" option might cause entity or item protection, if that's what the mob is trying to affect.
					By adding a mob to this list, you're removing the block protection check for it during the "mob griefing" game rule check.
					Supports entity type tags. Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:*illager", "minecraft:(v|p)illager", "#minecraft:raiders"]"""
			)
			.translation("gui.xaero_pac_config_non_block_griefers")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("nonBlockGriefingMobs"), Lists::newArrayList, s -> s instanceof String);
		entityGriefingMobs = builder
			.comment(
					"""
					(Forge-only option) Mobs that can grief entities in ways other than attacking them, e.g. how evokers can change the color of sheep. This list is used when overriding the vanilla "mob griefing" game rule value.
					By default, the mod assumes that any "mob griefing" game rule check is meant for block protection only. Add a mob to this list if you want the entity protection option to be checked as well when the rule is checked.
					Check out the "nonBlockGriefingMobs" option if you want to also remove the default block protection check for the mob.
					Supports entity type tags. Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(v|p)illager", "minecraft:*illager", "#minecraft:raiders"]"""
			)
			.translation("gui.xaero_pac_config_entity_griefers")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entityGriefingMobs"), Lists::newArrayList, s -> s instanceof String);
		droppedItemGriefingMobs = builder
			.comment(
					"""
					(Forge-only option) Mobs that can grief dropped items. This list is used when overriding the vanilla "mob griefing" game rule value.
					By default, the mod assumes that any "mob griefing" game rule check is meant for block protection only. Add a mob to this list if you want the item pickup protection option to be checked as well when the rule is checked.
					This mod should detect most mobs picking up items by default, but if it doesn't already detect a specific mob, this option might help.
					Check out the "nonBlockGriefingMobs" option if you want to also remove the default block protection check for the mob.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(v|p)illager", "minecraft:*illager", "#minecraft:raiders"]"""
			)
			.translation("gui.xaero_pac_config_gropped_item_griefers")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("droppedItemGriefingMobs"), Lists::newArrayList, s -> s instanceof String);
		blockAccessEntityGroups = builder
			.comment("""
					Custom groups of entities that a player/claim config should be able to make block access exceptions for (e.g. letting sheep eat grass or endermen take blocks). Each group can consist of multiple entities and entity tags.
					The format for an entity group is <group ID>{<entities/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-GUYS98{minecraft:pig, minecraft:c(ow|at), #minecraft:beehive_inhabitors}".
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_block_access_entity_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("blockAccessEntityGroups"),
					() -> Lists.newArrayList(
							"Villagers{minecraft:villager}"
					), s -> s instanceof String);
		entityAccessEntityGroups = builder
			.comment("""
					Custom groups of entities that a player/claim config should be able to make entity access exceptions for (e.g. letting zombies kill things).
					The groups should consist of entities that are the ones accessing other entities. The groups should not contain entities that are being accessed. Check out the "entityProtectionOptionalExceptionGroups" option for that.
					Each group can consist of multiple entities and entity tags. The format for an entity group is <group ID>{<entities/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-GUYS98{minecraft:pig, minecraft:c(ow|at), #minecraft:beehive_inhabitors}".
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_entity_access_entity_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("entityAccessEntityGroups"),
					() -> Lists.newArrayList(
							"Zombies{minecraft:zombie, minecraft:zombie_villager, minecraft:husk, minecraft:drowned}"
					), s -> s instanceof String);
		droppedItemAccessEntityGroups = builder
			.comment("""
					Custom groups of entities that a player/claim config should be able to make dropped item access exceptions for (e.g. letting piglins pick up gold).
					The groups should consist of entities that are the ones trying to pick up items, not consist of specific items.
					Each group can consist of multiple entities and entity tags. The format for an entity group is <group ID>{<entities/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-GUYS98{minecraft:pig, minecraft:c(ow|at), #minecraft:beehive_inhabitors}".
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_dropped_item_access_entity_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("droppedItemAccessEntityGroups"),
					() -> Lists.newArrayList(
							"Villagers{minecraft:villager}", "Piglins{minecraft:piglin}", "Foxes{minecraft:fox}"
					), s -> s instanceof String);
		staticFakePlayers = builder
			.comment("""
					A list of fake players (UUIDs or names) that shouldn't be affected by any chunk claim protection if they try to access a chunk with building protection compatible with
					the chunk that the fake player's origin block is positioned in, e.g. claims with the same owner and block protection option values.
					This works great for fake players that are bound to the position of a specific placed block (origin block). Moreover, the mod supports fake players placed at a block
					next to the origin block, even if that means entering another chunk, e.g. in the case of the Integrated Tunnels mod, or if the origin block is touching the target block.
					The mod will try all positions next to the target block and the fake player as the possible position of the fake player origin block.
					This will always protect the target block if it or the fake player touch a claim with incompatible build protection. Avoid building on such claim edges.
					However, some fake players' origin blocks can be nowhere near the fake player or the target block, e.g. in the case of the Create mod, or there might be no origin block at all,
					e.g. NPCs that can move around. In this case, the mods that use such fake players require explicit support to be implemented. Although they might also sometimes
					be supported by default, if the fake players use UUIDs of actual players.
					Explicit support exists for the Create mod (requires an extension on Fabric) and you are not required to add anything to this list.
					Make sure to always test that claim edges are protected from outside interaction by fake players that you add to this list.
					Wondering where to get the UUIDs or usernames of specific fake players? You can check the source code of the mods that use them or politely ask the mod authors.
					For example ["41C82C87-7AfB-4024-BB57-13D2C99CAE77", "FakePlayerName"]""")
			.translation("gui.xaero_pac_config_static_fake_players")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("staticFakePlayers"), () -> Lists.newArrayList("[IntegratedTunnels]"), s -> s instanceof String);
		staticFakePlayerClassExceptions = builder
				.comment("""
					A list of Java classes of fake players that should be excluded from claim protection exceptions given to fake players with the "staticFakePlayers" option
					or built-in fake player support, like in the case of Create mod deployers.
					This option is meant for fake players similar to ComputerCraft's turtles, which take the UUID of the player that places them. It becomes a problem when a turtle takes
					the UUID of a fake player from "staticFakePlayers" or a deployer because the turtle then gets the same privileges without actually being stationary itself nor a deployer.
					Adding classes here should not break support of fake players that take the UUID of their owner. It simply takes away privileges which aren't meant for them.
					For example ["dan200.computercraft.shared.turtle.core.TurtlePlayer"]""")
				.translation("gui.xaero_pac_config_static_fake_player_class_exceptions")
				.worldRestart()
				.defineListAllowEmpty(Lists.newArrayList("staticFakePlayerClassExceptions"), () -> Lists.newArrayList("dan200.computercraft.shared.turtle.core.TurtlePlayer"), s -> s instanceof String);

		additionalBannedItemsList = builder
			.comment("""
					By default, right-click use of some items is allowed in protected chunks, e.g. swords, pickaxes, bows, shield, tridents, splash potions, to let the players protect themselves or interact with some blocks/entities.
					To remove such exceptions for specific items, add them to this list. This list applies to both using an item at air and using it at a block/entity. Supports item tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:trident", "minecraft:shield", "minecraft:(oak|spruce)_boat", "#minecraft:boats"]""")
			.translation("gui.xaero_pac_config_banned_item_list")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("additionalBannedItemsList"), () -> Lists.newArrayList("supplementaries:slingshot"), s -> s instanceof String);

		itemUseProtectionExceptionList = builder
			.comment("""
					By default, most item right-click uses are disabled in protected chunks. To make an exception for a specific item, add it to this list. This option has a higher priority than "additionalBannedItemsList".
					This list applies to both using an item at air and using it at a block/entity. Supports item tags. Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:fishing_rod", "minecraft:ender_pearl", "minecraft:(red|green)_bed", "#minecraft:beds"]""")
			.translation("gui.xaero_pac_config_item_protection_exception")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("itemUseProtectionExceptionList"), () -> Lists.newArrayList("minecraft:firework_rocket"), s -> s instanceof String);

		itemUseProtectionOptionalExceptionGroups = builder
			.comment("""
					Custom groups of items that a player/claim config should be able to make protection exceptions for. Each group can consist of multiple items and item tags.
					Each group creates a player config option for the right-click use of the group items. The format for an item group is <group ID>{<items/tags/wildcards separated by ,>}.
					The group ID should consist of at most 32 characters that are letters A-Z, numbers 0-9 or the - and _ characters, e.g. "ePiC-stuff98{minecraft:(writable|written)_book, minecraft:*_book, #minecraft:compasses}".
					The player config options created for the groups, like regular options, must be added in the "playerConfigurablePlayerConfigOptions" list for players to have access to them.
					The exact paths of the added options can be found in the default player config file after you start the server.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR."""
			)
			.translation("gui.xaero_pac_config_item_protection_exception_groups")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("itemUseProtectionOptionalExceptionGroups"),
					() -> Lists.newArrayList(
							"Books{minecraft:written_book, minecraft:writable_book}"
					), s -> s instanceof String);

		completelyDisabledItemInteractions = builder
			.comment("""
					Items that are completely banned from right-click usage on the server, claimed or not. This list applies to both using an item at air and using it at a block/entity. Supports item tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:trident", "minecraft:shield", "minecraft:(oak|spruce)_boat", "#minecraft:boats"]""")
			.translation("gui.xaero_pac_config_completely_disabled_item_interactions")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("completelyDisabledItemInteractions"), Lists::newArrayList, s -> s instanceof String);

		completelyDisabledBlockInteractions = builder
			.comment("""
					Blocks that are completely banned from being interacted with on the server, claimed or not. Does not affect block breaking. Supports block tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:dirt", "minecraft:*_table", "minecraft:(cartography|fletching)_table", "#minecraft:buttons"]""")
			.translation("gui.xaero_pac_config_completely_disabled_block_interactions")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("completelyDisabledBlockInteractions"), Lists::newArrayList, s -> s instanceof String);

		completelyDisabledEntityInteractions = builder
			.comment("""
					Entities that are completely banned from being interacted with on the server, claimed or not. Does not affect killing the entities. Supports entity tags.
					Supports patterns with special characters *, (, ) and |, where * matches anything, ( ) are used for grouping and | means OR.
					For example ["minecraft:(v|p)illager", "minecraft:*illager", "#minecraft:raiders"]""")
			.translation("gui.xaero_pac_config_completely_disabled_entity_interactions")
			.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("completelyDisabledEntityInteractions"), Lists::newArrayList, s -> s instanceof String);

		completelyDisableFrostWalking = builder
				.comment("Whether to completely disable frost walking on the server. Use this if the regular frost walking protection doesn't work, since there is no game rule for it.")
				.translation("gui.xaero_pac_config_completely_disable_frost_walking")
				.worldRestart()
				.define("completelyDisableFrostWalking", false);

		builder.pop();

		builder.pop();
		
		playerConfigurablePlayerConfigOptions = builder
			.comment("""
					A list of options in the player config that individual players can reconfigure. If an option is in neither of the configurable option lists,
					then the value in the default player config is used across the server. Check the default player config .toml file for the option names.""")
			.translation("gui.xaero_pac_config_player_configurable_player_options")
			//.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("playerConfigurablePlayerConfigOptions"),
					() -> Lists.newArrayList(
							"claims.protectClaimedChunks",
							"claims.forceload.enabled",
							"claims.name",
							"claims.color",
							"claims.protection.fromParty",
							"claims.protection.fromAllyParties",
							"claims.protection.buttonsFromProjectiles",
							"claims.protection.targetsFromProjectiles",
							"claims.protection.platesFromPlayers",
							"claims.protection.platesFromMobs",
							"claims.protection.platesFromOther",
							"claims.protection.tripwireFromPlayers",
							"claims.protection.tripwireFromMobs",
							"claims.protection.tripwireFromOther",
							"claims.protection.cropTrample",
							"claims.protection.playerLightning",
							"claims.protection.fromFrostWalking",
							"claims.protection.entitiesFromPlayers",
							"claims.protection.entitiesFromMobs",
							"claims.protection.entitiesFromOther",
							"claims.protection.entitiesRedirect",
							"claims.protection.entitiesFromExplosions",
							"claims.protection.entitiesFromFire",
							"claims.protection.netherPortalsPlayers",
							"claims.protection.netherPortalsMobs",
							"claims.protection.netherPortalsOther",
							"claims.protection.fluidBarrier",
							"claims.protection.dispenserBarrier",
							"claims.protection.pistonBarrier",
							"claims.protection.itemTossPlayers",
							"claims.protection.itemTossMobs",
							"claims.protection.itemTossOther",
							"claims.protection.itemTossRedirect",
							"claims.protection.mobLoot",
							"claims.protection.playerDeathLoot",
							"claims.protection.itemPickupPlayers",
							"claims.protection.itemPickupMobs",
							"claims.protection.itemPickupRedirect",
							"claims.protection.xpPickup",
							"claims.protection.raids",
							"claims.protection.naturalSpawnHostile",
							"claims.protection.naturalSpawnFriendly",
							"claims.protection.spawnersHostile",
							"claims.protection.spawnersFriendly",
							"parties.name",
							"parties.shareLocationWithParty",
							"parties.shareLocationWithMutualAllyParties",
							"parties.receiveLocationsFromParty",
							"parties.receiveLocationsFromMutualAllyParties",
							"claims.protection.exceptionGroups.block.interact.Controls",
							"claims.protection.exceptionGroups.block.interact.Doors",
							"claims.protection.exceptionGroups.block.interact.Chests",
							"claims.protection.exceptionGroups.block.interact.Barrels",
							"claims.protection.exceptionGroups.block.interact.Ender_Chests",
							"claims.protection.exceptionGroups.block.interact.Shulker_Boxes",
							"claims.protection.exceptionGroups.block.interact.Furnaces",
							"claims.protection.exceptionGroups.block.interact.Hoppers",
							"claims.protection.exceptionGroups.block.interact.Dispenser-like",
							"claims.protection.exceptionGroups.block.interact.Anvils",
							"claims.protection.exceptionGroups.block.interact.Beds",
							"claims.protection.exceptionGroups.block.interact.Beacons",
							"claims.protection.exceptionGroups.block.interact.Enchanting_Tables",
							"claims.protection.exceptionGroups.block.break.Crops",
							"claims.protection.exceptionGroups.entity.interact.Traders",
							"claims.protection.exceptionGroups.entity.handInteract.Item_Frames",
							"claims.protection.exceptionGroups.entity.interact.Armor_Stands",
							"claims.protection.exceptionGroups.entity.break.Livestock",
							"claims.protection.exceptionGroups.entity.blockAccess.Villagers",
							"claims.protection.exceptionGroups.entity.entityAccess.Zombies",
							"claims.protection.exceptionGroups.entity.droppedItemAccess.Villagers",
							"claims.protection.exceptionGroups.entity.droppedItemAccess.Piglins",
							"claims.protection.exceptionGroups.entity.droppedItemAccess.Foxes",
							"claims.protection.exceptionGroups.item.interact.Books",
							"claims.protection.exceptionGroups.entity.barrier.Ender_Pearls",
							"/*remove comment to enable*/claims.protection.exceptionGroups.entity.barrier.Players"
							), s -> s instanceof String);

		opConfigurablePlayerConfigOptions = builder
			.comment("""
					A list of additional options in the player config that OPs can reconfigure for players.
					This is meant for options that should be configured per player but not by the players.
					If an option is in neither of the configurable option lists, then the value in the default player config is used across the server.
					Check the default player config .toml file for the option names.""")
			.translation("gui.xaero_pac_config_op_configurable_player_options")
			//.worldRestart()
			.defineListAllowEmpty(Lists.newArrayList("opConfigurablePlayerConfigOptions"), () -> Lists.newArrayList("claims.bonusChunkClaims", "claims.bonusChunkForceloads"), s -> s instanceof String);
		
		builder.pop();
	}

	public static final ForgeConfigSpec SPEC;
	public static final ServerConfig CONFIG;
	static {
		final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
		
	}
	
	public enum ConfigListType {
		ONLY,
		ALL_BUT
	}
	
	public enum ClaimsSyncType {
		NOT_SYNCED,
		OWNED_ONLY,
		ALL
	}

}
