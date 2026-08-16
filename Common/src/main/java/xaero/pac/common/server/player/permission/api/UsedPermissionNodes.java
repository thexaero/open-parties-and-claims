/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.permission.api;

import net.minecraft.network.chat.Component;
import com.google.common.collect.ImmutableMap;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.permission.PermissionNode;
import xaero.pac.common.server.player.permission.value.type.PermissionValueTypes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Access point to all player permission nodes used by the mod.
 */
public class UsedPermissionNodes {
	private static final Map<String, IPermissionNodeAPI<?>> ALL_BUILDER = new LinkedHashMap<>();
	/*IntelliJ is wrong about the lambdas... Method references cause a crash here.*/

	/**
	 * The maximum claim number int "permission".
	 */
	public static final IPermissionNodeAPI<Integer> MAX_PLAYER_CLAIMS = new PermissionNode<>(
			"xaero.pac_max_claims",
			PermissionValueTypes.INTEGER,
			() -> ServerConfig.CONFIG.maxPlayerClaimsPermission.get(),
			Component.translatable("gui.xaero_pac_permission_max_claims"),
			Component.translatable("gui.xaero_pac_permission_comment_max_claims"),
			ALL_BUILDER);

	/**
	 * The maximum forceload number int "permission".
	 */
	public static final IPermissionNodeAPI<Integer> MAX_PLAYER_FORCELOADS = new PermissionNode<>(
			"xaero.pac_max_forceloads",
			PermissionValueTypes.INTEGER,
			() -> ServerConfig.CONFIG.maxPlayerClaimForceloadsPermission.get(),
			Component.translatable("gui.xaero_pac_permission_max_forceloads"),
			Component.translatable("gui.xaero_pac_permission_comment_max_forceloads"),
			ALL_BUILDER);

	/**
	 * The permission to make/remove server claims and use server claim mode.
	 */
	public static final IPermissionNodeAPI<Boolean> SERVER_CLAIMS = new PermissionNode<>(
			"xaero.pac_server_claims",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.serverClaimPermission.get(),
			Component.translatable("gui.xaero_pac_permission_server_claims"),
			Component.translatable("gui.xaero_pac_permission_comment_server_claims"),
			ALL_BUILDER);

	/**
	 * The permission to enter moderator mode for claims.
	 */
	public static final IPermissionNodeAPI<Boolean> CLAIMS_MODERATOR_MODE = new PermissionNode<>(
			"xaero.pac_claims_moderator_mode",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.claimsModeratorModePermission.get(),
			Component.translatable("gui.xaero_pac_permission_claims_moderator_mode"),
			Component.translatable("gui.xaero_pac_permission_comment_claims_moderator_mode"),
			ALL_BUILDER);

	/**
	 * The permission to enter admin mode for claims.
	 */
	public static final IPermissionNodeAPI<Boolean> CLAIMS_ADMIN_MODE = new PermissionNode<>(
			"xaero.pac_claims_admin_mode",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.claimsAdminModePermission.get(),
			Component.translatable("gui.xaero_pac_permission_claims_admin_mode"),
			Component.translatable("gui.xaero_pac_permission_comment_claims_admin_mode"),
			ALL_BUILDER);

	/**
	 * The maximum player group count int "permission".
	 */
	public static final PermissionNode<Integer> MAX_PLAYER_GROUPS = new PermissionNode<>(
			"xaero.pac_max_player_groups",
			PermissionValueTypes.INTEGER,
			() -> ServerConfig.CONFIG.maxPlayerGroupsPermission.get(),
			Component.translatable("gui.xaero_pac_permission_max_player_groups"),
			Component.translatable("gui.xaero_pac_permission_comment_max_player_groups"),
			ALL_BUILDER);

	/**
	 * The player group space int "permission".
	 */
	public static final PermissionNode<Integer> PLAYER_GROUP_SPACE = new PermissionNode<>(
			"xaero.pac_player_group_space",
			PermissionValueTypes.INTEGER,
			() -> ServerConfig.CONFIG.playerGroupSpacePermission.get(),
			Component.translatable("gui.xaero_pac_permission_player_group_space"),
			Component.translatable("gui.xaero_pac_permission_comment_player_group_space"),
			ALL_BUILDER);

	/**
	 * The permission to enter admin mode for parties.
	 */
	public static final IPermissionNodeAPI<Boolean> PARTIES_ADMIN_MODE = new PermissionNode<>(
			"xaero.pac_parties_admin_mode",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.partiesAdminModePermission.get(),
			Component.translatable("gui.xaero_pac_permission_parties_admin_mode"),
			Component.translatable("gui.xaero_pac_permission_comment_parties_admin_mode"),
			ALL_BUILDER);

	/**
	 * The permission to impersonate another player regarding claims.
	 */
	public static final IPermissionNodeAPI<Boolean> CLAIMS_IMPERSONATION = new PermissionNode<>(
			"xaero.pac_claims_impersonation",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.claimsImpersonationPermission.get(),
			Component.translatable("gui.xaero_pac_permission_claims_impersonation"),
			Component.translatable("gui.xaero_pac_permission_comment_claims_impersonation"),
			ALL_BUILDER);

	/**
	 * The permission to impersonate another player regarding built in party commands.
	 */
	public static final IPermissionNodeAPI<Boolean> PARTIES_IMPERSONATION = new PermissionNode<>(
			"xaero.pac_parties_impersonation",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.partiesImpersonationPermission.get(),
			Component.translatable("gui.xaero_pac_permission_parties_impersonation"),
			Component.translatable("gui.xaero_pac_permission_comment_parties_impersonation"),
			ALL_BUILDER);

	/**
	 * The permission to teleport to any player's claims.
	 */
	public static final IPermissionNodeAPI<Boolean> CLAIMS_TELEPORTATION = new PermissionNode<>(
			"xaero.pac_claims_teleport",
			PermissionValueTypes.BOOLEAN,
			() -> ServerConfig.CONFIG.claimsTeleportationPermission.get(),
			Component.translatable("gui.xaero_pac_permission_claims_teleport"),
			Component.translatable("gui.xaero_pac_permission_comment_claims_teleport"),
			ALL_BUILDER);

	/**
	 * A (default node string)->(node instance) map of all player permission nodes.
	 */
	public static final ImmutableMap<String, IPermissionNodeAPI<?>> ALL = ImmutableMap.copyOf(ALL_BUILDER);

}
