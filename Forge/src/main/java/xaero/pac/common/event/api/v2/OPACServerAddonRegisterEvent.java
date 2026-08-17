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

package xaero.pac.common.event.api.v2;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerRegisterAPI;
import xaero.pac.common.event.api.OPACServerAddonRegisterEventContext;
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemRegisterAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemRegisterAPI;

public class OPACServerAddonRegisterEvent extends Event {

	private final OPACServerAddonRegisterEventContext context;

	public OPACServerAddonRegisterEvent(OPACServerAddonRegisterEventContext context) {
		this.context = context;
	}

	/**
	 * @deprecated Use {@link #getContext()} instead
	 * @return the minecraft server
	 */
	@Deprecated
	public MinecraftServer getServer() {
		return context.getServer();
	}

	/**
	 * @deprecated Use {@link #getContext()} instead
	 * @return the permission system manager api
	 */
	@Deprecated
	public IPlayerPermissionSystemRegisterAPI getPermissionSystemManager() {
		return context.getPermissionSystemManagerAPI();
	}

	/**
	 * @deprecated Use {@link #getContext()} instead
	 * @return the party system manager api
	 */
	@Deprecated
	public IPlayerPartySystemRegisterAPI getPartySystemManagerAPI() {
		return context.getPartySystemManagerAPI();
	}

	/**
	 * @deprecated Use {@link #getContext()} instead
	 * @return the claims manager tracker api
	 */
	@Deprecated
	public IClaimsManagerTrackerRegisterAPI getClaimsManagerTrackerAPI() {
		return context.getClaimsManagerTrackerAPI();
	}

	/**
	 * Gets the event context.
	 *
	 * @return the event context
	 */
	public OPACServerAddonRegisterEventContext getContext() {
		return context;
	}

}
