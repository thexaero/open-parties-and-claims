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

package xaero.pac.common.server.claims.action.listener;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.server.claims.action.listener.api.IClaimActionListenerAPI;
import xaero.pac.common.server.claims.action.listener.api.IClaimActionListenerManagerAPI;
import xaero.pac.common.server.claims.action.listener.override.api.ClaimActionPermissionOverride;
import xaero.pac.common.server.claims.action.listener.override.api.ClaimActionPermissionOverrideType;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimActionListenerManager implements IClaimActionListenerManagerAPI {

	private final List<IClaimActionListenerAPI> listeners;
	private final ClaimActionPermissionOverride defaultOverride;

	private ClaimActionListenerManager(List<IClaimActionListenerAPI> listeners, ClaimActionPermissionOverride defaultOverride) {
		this.listeners = listeners;
		this.defaultOverride = defaultOverride;
	}

	@Override
	public void register(@Nonnull IClaimActionListenerAPI listener){
		listeners.add(listener);
		OpenPartiesAndClaims.LOGGER.info("Registered claim action listener: {}", listener.getName());
	}

	public ClaimActionPermissionOverride overrideClaimingActionPermission(
			UUID playerId,
			Identifier dim,
			int x,
			int z,
			ClaimingAction action,
			IServerClaimsManagerAPI claimsManagerAPI,
			MinecraftServer server
	){
		ClaimActionPermissionOverride current = defaultOverride;
		for (IClaimActionListenerAPI listener : listeners) {
			ClaimActionPermissionOverride listenerResult =
					listener.overrideClaimingActionPermission(playerId, dim, x, z, action, claimsManagerAPI, current, server);
			if(listenerResult == current)
				continue;
			if(listenerResult.getType() == ClaimActionPermissionOverrideType.INTERRUPT)
				return listenerResult;
			if(listenerResult.getType() == ClaimActionPermissionOverrideType.FORBID)
				current = listenerResult;
			if(current.getType() == ClaimActionPermissionOverrideType.PASS &&
					listenerResult.getType() == ClaimActionPermissionOverrideType.ALLOW)
				current = listenerResult;
		}
		return current;
	}

	public void handleSuccessfulClaimingAction(
			UUID playerId,
			Identifier dim,
			int x,
			int z,
			ClaimingAction action,
			IServerClaimsManagerAPI claimsManagerAPI,
			MinecraftServer server
	){
		for (IClaimActionListenerAPI listener : listeners)
			listener.handleSuccessfulClaimingAction(playerId, dim, x, z, action, claimsManagerAPI, server);
	}

	public static final class Builder {

		private Builder(){}

		public Builder setDefault(){
			return this;
		}

		public ClaimActionListenerManager build(){
			ClaimActionPermissionOverride defaultOverride = new ClaimActionPermissionOverride(
					ClaimActionPermissionOverrideType.PASS, null
			);
			return new ClaimActionListenerManager(new ArrayList<>(), defaultOverride);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
