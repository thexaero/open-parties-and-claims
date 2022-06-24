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

package xaero.pac.common.server.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.config.IForgeConfigSpec;
import xaero.pac.common.config.IForgeConfigSpecBuilder;
import xaero.pac.common.config.value.IForgeConfigBooleanValue;
import xaero.pac.common.config.value.IForgeConfigEnumValue;
import xaero.pac.common.config.value.IForgeConfigIntValue;
import xaero.pac.common.config.value.IForgeConfigValue;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {

	public final IForgeConfigBooleanValue partiesEnabled;
	public final IForgeConfigBooleanValue claimsEnabled;
	public final IForgeConfigIntValue autosaveInterval;
	public final IForgeConfigIntValue partyExpirationTime;
	public final IForgeConfigIntValue partyExpirationCheckInterval;
	public final IForgeConfigValue<List<? extends String>> opConfigurablePlayerConfigOptions;
	public final IForgeConfigValue<List<? extends String>> playerConfigurablePlayerConfigOptions;
	public final IForgeConfigEnumValue<ConfigListType> friendlyChunkProtectedEntityListType;
	public final IForgeConfigValue<List<? extends String>> friendlyChunkProtectedEntityList;
	public final IForgeConfigEnumValue<ConfigListType> hostileChunkProtectedEntityListType;
	public final IForgeConfigValue<List<? extends String>> hostileChunkProtectedEntityList;
	public final IForgeConfigValue<List<? extends String>> blockProtectionExceptionList;
	public final IForgeConfigIntValue maxClaimDistance;
	public final IForgeConfigValue<List<? extends String>> claimableDimensionsList;
	public final IForgeConfigEnumValue<ConfigListType> claimableDimensionsListType;
	public final IForgeConfigBooleanValue allowExistingClaimsInUnclaimableDimensions;
	public final IForgeConfigBooleanValue allowExistingForceloadsInUnclaimableDimensions;
	public final IForgeConfigIntValue maxPlayerClaims;
	public final IForgeConfigIntValue maxPlayerClaimForceloads;
	public final IForgeConfigIntValue maxPartyMembers;
	public final IForgeConfigIntValue maxPartyAllies;
	public final IForgeConfigIntValue maxPartyInvites;
	public final IForgeConfigIntValue playerClaimsExpirationTime;
	public final IForgeConfigIntValue playerClaimsExpirationCheckInterval;
	public final IForgeConfigBooleanValue playerClaimsConvertExpiredClaims;
	public final IForgeConfigEnumValue<ClaimsSyncType> claimsSynchronization;
	public final IForgeConfigValue<String> maxPlayerClaimsFTBPermission;
	public final IForgeConfigValue<String> maxPlayerClaimForceloadsFTBPermission;
	
	private ServerConfig(IForgeConfigSpecBuilder builder) {
		builder.push("serverConfig");

		autosaveInterval = builder
            .comment("How often to auto-save modified data, e.g. parties, claims, player configs (in minutes)")
            .translation("gui.xaero_pac_config_autosave_interval")
            .worldRestart()
            .defineInRange("autosaveInterval", 10, 1, Integer.MAX_VALUE);
		
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
            .comment("The maximum number of chunks that a player can claim. Additional claims can be configured in the player config.\nThis value can be overridden with a FTB Ranks permission.")
            .translation("gui.xaero_pac_config_max_player_claims")
            .worldRestart()
            .defineInRange("maxPlayerClaims", 500, 0, Integer.MAX_VALUE);
		
		maxPlayerClaimForceloads = builder
            .comment("The maximum number of claimed chunks that a player can forceload. Additional forceloads can be configured in the player config.\nThis value can be overridden with a FTB Ranks permission.")
            .translation("gui.xaero_pac_config_max_player_forceloads")
            .worldRestart()
            .defineInRange("maxPlayerClaimForceloads", 10, 0, Integer.MAX_VALUE);

		maxPlayerClaimsFTBPermission = builder
				.comment("The FTB Ranks permission that should override the default \"maxPlayerClaims\" value. Set it to an empty string to never check permissions.")
				.translation("gui.xaero_pac_config_max_claims_ftb_permission")
				.worldRestart()
				.define("maxPlayerClaimsFTBPermission", "xaero.pac_max_claims");

		maxPlayerClaimForceloadsFTBPermission = builder
				.comment("The FTB Ranks permission that should override the default \"maxPlayerClaimForceloads\" value. Set it to an empty string to never check permissions. The permission override only takes effect after the player logs in at least once after a server (re)launch, so it is recommended to keep all permission-based forceload limits equal to or greater than \"maxPlayerClaimForceloads\".")
				.translation("gui.xaero_pac_config_max_claims_ftb_permission")
				.worldRestart()
				.define("maxPlayerClaimForceloadsFTBPermission", "xaero.pac_max_forceloads");

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
	            .comment("Dimensions to include/exclude from being claimable, depending on the list type. For example [\"minecraft:overworld\", \"minecraft:the_nether\"]. By default the list is empty and of type ALL_BUT, meaning that all dimensions are claimable.")
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
            .comment("Whether to synchronize world chunk claims to the game clients. Enables client-side mods to access the claims data, e.g. to display it on a map. ALL - all claims are synced. OWNED_ONLY - only the claims that the client player owns and server claims are synced. NOT_SYNCED - claims are not synced.")
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
	            .comment("Friendly entities to include/exclude in chunk protection, depending on the list type. For example [\"minecraft:cow\", \"minecraft:rabbit\"]. By default the list is empty with the type set to ALL_BUT, which means that all friendly entities are included.")
	            .translation("gui.xaero_pac_config_friendly_protected_entities")
	            .worldRestart()
	            .defineListAllowEmpty(Lists.newArrayList("friendlyChunkProtectedEntityList"), ArrayList::new, s -> s instanceof String);

		hostileChunkProtectedEntityListType = builder
            .comment("The type of the list defined in \"hostileChunkProtectedEntityList\". ONLY - include only the listed entities. ALL_BUT - include all but the listed entities.")
            .translation("gui.xaero_pac_config_hostile_protected_entities_list_type")
            .worldRestart()
           	.defineEnum("hostileChunkProtectedEntityListType", ConfigListType.ONLY);
		
		hostileChunkProtectedEntityList = builder
	            .comment("Hostile entities to include/exclude in chunk protection, depending on the list type. For example [\"minecraft:creeper\", \"minecraft:zombie\"]")
	            .translation("gui.xaero_pac_config_hostile_protected_entities")
	            .worldRestart()
	            .defineListAllowEmpty(Lists.newArrayList("hostileChunkProtectedEntityList"), ArrayList::new, s -> s instanceof String);
		
		blockProtectionExceptionList = builder
	            .comment("Blocks to exclude from chunk protection on block interaction with an empty hand. Player config determines whether the list is actually used. For example [\"minecraft:lever\", \"minecraft:stone_button\"]")
	            .translation("gui.xaero_pac_config_block_protection_exception")
	            .worldRestart()
	            .defineListAllowEmpty(Lists.newArrayList("blockProtectionExceptionList"), () -> Lists.newArrayList("minecraft:lever", "minecraft:stone_button", "minecraft:polished_blackstone_button", "minecraft:oak_button", "minecraft:spruce_button", "minecraft:birch_button", "minecraft:jungle_button", "minecraft:acacia_button", "minecraft:dark_oak_button", "minecraft:crimson_button", "minecraft:warped_button"), s -> s instanceof String);
		
		builder.pop();

		builder.pop();
		
		playerConfigurablePlayerConfigOptions = builder
	            .comment("A list of options in the player config that individual players can reconfigure. If an option is in neither of the configurable option lists, then the value in the default player config is used across the server. Check the default player config .toml file for the option names.")
	            .translation("gui.xaero_pac_config_player_configurable_player_options")
	            //.worldRestart()
	            .defineListAllowEmpty(Lists.newArrayList("playerConfigurablePlayerConfigOptions"), 
	            		() -> Lists.newArrayList(
	            				"playerConfig.claims.protectClaimedChunks", 
	            				"playerConfig.claims.protection.fromParty", 
	            				"playerConfig.claims.protection.fromAllyParties", 
	            				"playerConfig.claims.forceload.enabled", 
	            				"playerConfig.claims.name", 
	            				"playerConfig.claims.color", 
	            				"playerConfig.parties.name", 
	            				"playerConfig.parties.shareLocationWithParty", 
	            				"playerConfig.parties.shareLocationWithMutualAllyParties", 
	            				"playerConfig.parties.receiveLocationsFromParty",
	            				"playerConfig.parties.receiveLocationsFromMutualAllyParties"
	            				), s -> s instanceof String);
		
		opConfigurablePlayerConfigOptions = builder
	            .comment("A list of additional options in the player config that OPs can reconfigure for players. This is meant for options that should be configured per player but not by the players. If an option is in neither of the configurable option lists, then the value in the default player config is used across the server. Check the default player config .toml file for the option names.")
	            .translation("gui.xaero_pac_config_op_configurable_player_options")
	            //.worldRestart()
	            .defineListAllowEmpty(Lists.newArrayList("opConfigurablePlayerConfigOptions"), () -> Lists.newArrayList("playerConfig.claims.bonusChunkClaims", "playerConfig.claims.bonusChunkForceloads"), s -> s instanceof String);
		
		builder.pop();
	}

	public static final IForgeConfigSpec SPEC;
    public static final ServerConfig CONFIG;
    static {
        final Pair<ServerConfig, IForgeConfigSpec> specPair = OpenPartiesAndClaims.INSTANCE.getForgeConfigHelper().beginSpecBuilding().configure(ServerConfig::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
        
    }
    
    public static enum ConfigListType {
    	ONLY,
    	ALL_BUT
    }
    
    public static enum ClaimsSyncType {
    	NOT_SYNCED,
    	OWNED_ONLY,
    	ALL
    }

}
