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

package xaero.pac.common.server.claims.action.listener.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.server.claims.action.listener.override.api.ClaimActionPermissionOverride;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * API for implementation of claim action listeners by addons
 */
public interface IClaimActionListenerAPI {

	/**
	 * Gets the display name of the listener.
	 *
	 * @return the name String of the listener, not null
	 */
	@Nonnull
	String getName();

	/**
	 * Can be used to override usage permission for a claiming action. For example, stop a player from claiming unless
	 * they have enough of some sort of currency.
	 * <p>
	 * When you have a reason to override the permission, you must create an instance of {@link ClaimActionPermissionOverride}
	 * and return it.
	 * <p>
	 * When you don't have a reason to override the permission, it is recommended to simply return currentOverride instead of
	 * creating a new instance.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to handle such claims differently.
	 *
	 * @param playerId  the UUID of the player who the action is being performed as, not null
	 * @param dim  the dimension ID of the chunk being affected
	 * @param x  the X chunk coordinate of the chunk being affected
	 * @param z  the Z chunk coordinate of the chunk being affected
	 * @param action  the claiming action type, not null
	 * @param claimsManagerAPI  the claims manager API, not null
	 * @param currentOverride  the current candidate to override the action permission with, not null
	 * @param server  the server, not null
	 * @return the claim action permission override instance, not null
	 */
	@Nonnull
	ClaimActionPermissionOverride overrideClaimingActionPermission(
			@Nonnull
			UUID playerId,
			@Nonnull
			Identifier dim,
			int x,
			int z,
			@Nonnull
			ClaimingAction action,
			@Nonnull
			IServerClaimsManagerAPI claimsManagerAPI,
			@Nonnull
			ClaimActionPermissionOverride currentOverride,
			@Nonnull
			MinecraftServer server);

	/**
	 * Can be used to handle successful claiming actions.
	 * <p>
	 * For example, if your addon is implementing a claiming cost system, this is where you would deduct the cost from
	 * the player.
	 * <p>
	 * Special claim owners can be found at {@link xaero.pac.common.claims.api.SpecialClaimOwners}, such as for server claims,
	 * in case you wish to handle such claims differently.
	 *
	 * @param playerId  the UUID of the player who the action is being performed as, not null
	 * @param dim  the dimension ID of the chunk that was affected
	 * @param x  the X in chunk coordinate space of the chunk that was affected
	 * @param z  the Z in chunk coordinate space of the chunk that was affected
	 * @param action  the claiming action type, not null
	 * @param claimsManagerAPI  the claim manager, not null
	 * @param server  the server, not null
	 */
	void handleSuccessfulClaimingAction(
			@Nonnull
			UUID playerId,
			@Nonnull
			Identifier dim,
			int x,
			int z,
			@Nonnull
			ClaimingAction action,
			@Nonnull
			IServerClaimsManagerAPI claimsManagerAPI,
			@Nonnull
			MinecraftServer server
	);

}
