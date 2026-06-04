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

package xaero.pac.client.claims.player.mode;

import net.minecraft.client.Minecraft;
import xaero.pac.client.claims.api.IClientClaimsManagerAPI;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.HashMap;
import java.util.Map;

public class ClaimingModeClientHandlers {

	private static final Map<IClaimingModeAPI, ClientClaimingModeHandler> ALL = new HashMap<>();

	public static final ClientClaimingModeHandler PLAYER = ClientClaimingModeHandler.Builder.begin()
			.setMode(ClaimingModes.PLAYER)
			.setClaimReflectionOwnerGetter(claimsManager ->
					Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getUUID()
			)
			.build(ALL);

	public static final ClientClaimingModeHandler PARTY = ClientClaimingModeHandler.Builder.begin()
			.setMode(ClaimingModes.PARTY)
			.setClaimReflectionOwnerGetter(IClientClaimsManagerAPI::getCurrentPartyOwner)
			.setVisibilityGetter((claimsManager, configs) ->
					claimsManager.usingPartyOwnedClaims())
			.build(ALL);

	public static final ClientClaimingModeHandler SERVER = ClientClaimingModeHandler.Builder.begin()
			.setMode(ClaimingModes.SERVER)
			.setClaimReflectionOwnerGetter(claimsManager -> PlayerConfig.SERVER_CLAIM_UUID)
			.setVisibilityGetter((claimsManager, configs) ->
					configs.getServerClaimsConfig().getPermissions().canClaimAs())
			.build(ALL);

	public static ClientClaimingModeHandler get(IClaimingModeAPI mode){
		return ALL.get(mode);
	}

}
