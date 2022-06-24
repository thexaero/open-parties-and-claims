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

package xaero.pac.client;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.client.claims.sync.ClientClaimsSyncHandler;
import xaero.pac.client.controls.XPACKeyBindings;
import xaero.pac.client.parties.party.ClientPartyStorage;
import xaero.pac.client.patreon.Patreon;
import xaero.pac.client.player.config.PlayerConfigClientStorageManager;
import xaero.pac.client.player.config.PlayerConfigClientSynchronizer;
import xaero.pac.common.LoadCommon;

public class LoadClient extends LoadCommon {
	
	public LoadClient(OpenPartiesAndClaims modMain) {
		super(modMain);
	}

	public void loadClient() {
		XPACKeyBindings keyBindings = new XPACKeyBindings();
		keyBindings.register();
		ClientClaimsManager claimsManager = ClientClaimsManager.Builder.begin().build();
		ClientClaimsSyncHandler claimsSyncHandler = new ClientClaimsSyncHandler(claimsManager);
		modMain.setClientData(
				ClientData.Builder.begin()
				.setClientTickHandler(new ClientTickHandler())
				.setClientWorldLoadHandler(new ClientWorldLoadHandler())
				.setPlayerConfigClientSynchronizer(new PlayerConfigClientSynchronizer())
				.setKeyBindings(keyBindings)
				.setPlayerConfigStorageManager(PlayerConfigClientStorageManager.Builder.begin().build())
				.setClientPartyStorage(ClientPartyStorage.Builder.begin().build())
				.setClaimsManager(claimsManager)
				.setClientClaimsSyncHandler(claimsSyncHandler)
				.build()
				);
		
		Patreon.checkPatreon();
	}

}
