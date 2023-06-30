/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2023, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraftforge.eventbus.api.Event;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerRegisterAPI;
import xaero.pac.common.server.parties.system.api.IPlayerPartySystemRegisterAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemRegisterAPI;

public class OPACServerAddonRegisterEvent extends Event {

	private final MinecraftServer server;
	private final IPlayerPermissionSystemRegisterAPI permissionSystemManagerAPI;
	private final IPlayerPartySystemRegisterAPI partySystemManagerAPI;
	private final IClaimsManagerTrackerRegisterAPI claimsManagerTrackerAPI;

	public OPACServerAddonRegisterEvent(MinecraftServer server, IPlayerPermissionSystemRegisterAPI permissionSystemManagerAPI, IPlayerPartySystemRegisterAPI partySystemManagerAPI, IClaimsManagerTrackerRegisterAPI claimsManagerTrackerAPI) {
		this.server = server;
		this.permissionSystemManagerAPI = permissionSystemManagerAPI;
		this.partySystemManagerAPI = partySystemManagerAPI;
		this.claimsManagerTrackerAPI = claimsManagerTrackerAPI;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public IPlayerPermissionSystemRegisterAPI getPermissionSystemManager() {
		return permissionSystemManagerAPI;
	}

	public IPlayerPartySystemRegisterAPI getPartySystemManagerAPI() {
		return partySystemManagerAPI;
	}

	public IClaimsManagerTrackerRegisterAPI getClaimsManagerTrackerAPI() {
		return claimsManagerTrackerAPI;
	}

}
