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

package xaero.pac.common.event.api;

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerRegisterAPI;
import xaero.pac.common.server.claims.action.listener.api.IClaimActionListenerManagerAPI;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemRegisterAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemRegisterAPI;

import javax.annotation.Nonnull;

/**
 * The context for the addon register event on the server side
 */
public class OPACServerAddonRegisterEventContext {

	private final MinecraftServer server;
	private final IPlayerPermissionSystemRegisterAPI permissionSystemManagerAPI;
	private final IPlayerPartySystemRegisterAPI partySystemManagerAPI;
	private final IClaimsManagerTrackerRegisterAPI claimsManagerTrackerAPI;
	private final IClaimActionListenerManagerAPI claimActionListenerManagerAPI;

	/**
	 * The constructor for internal use.
	 *
	 * @param server  the server, not null
	 * @param permissionSystemManagerAPI  the permission system manager API, not null
	 * @param partySystemManagerAPI  the party system manager API, not null
	 * @param claimsManagerTrackerAPI  the claims manager tracker API, not null
	 * @param claimActionListenerManagerAPI  the claim action listener manager API, not null
	 */
	public OPACServerAddonRegisterEventContext(
			@Nonnull MinecraftServer server,
			@Nonnull IPlayerPermissionSystemRegisterAPI permissionSystemManagerAPI,
			@Nonnull IPlayerPartySystemRegisterAPI partySystemManagerAPI,
			@Nonnull IClaimsManagerTrackerRegisterAPI claimsManagerTrackerAPI,
			@Nonnull IClaimActionListenerManagerAPI claimActionListenerManagerAPI
	) {
		this.server = server;
		this.permissionSystemManagerAPI = permissionSystemManagerAPI;
		this.partySystemManagerAPI = partySystemManagerAPI;
		this.claimsManagerTrackerAPI = claimsManagerTrackerAPI;
		this.claimActionListenerManagerAPI = claimActionListenerManagerAPI;
	}

	/**
	 * Gets the Minecraft server.
	 *
	 * @return the server.
	 */
	@Nonnull
	public MinecraftServer getServer() {
		return server;
	}

	/**
	 * Gets the permission system manager API.
	 *
	 * @return the permission system manager API
	 */
	@Nonnull
	public IPlayerPermissionSystemRegisterAPI getPermissionSystemManagerAPI() {
		return permissionSystemManagerAPI;
	}

	/**
	 * Gets the party system manager API.
	 *
	 * @return the party system manager API
	 */
	@Nonnull
	public IPlayerPartySystemRegisterAPI getPartySystemManagerAPI() {
		return partySystemManagerAPI;
	}

	/**
	 * Gets the claims manager tracker API.
	 *
	 * @return the claims manager tracker API
	 */
	@Nonnull
	public IClaimsManagerTrackerRegisterAPI getClaimsManagerTrackerAPI() {
		return claimsManagerTrackerAPI;
	}

	/**
	 * Gets the claim action listener manager API.
	 *
	 * @return the claim action listener manager API
	 */
	@Nonnull
	public IClaimActionListenerManagerAPI getClaimActionListenerManagerAPI() {
		return claimActionListenerManagerAPI;
	}

}
