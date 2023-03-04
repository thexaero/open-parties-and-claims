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

package xaero.pac.client;

import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.sync.ClientClaimsSyncHandler;
import xaero.pac.client.controls.XPACKeyBindings;
import xaero.pac.client.parties.party.api.IClientPartyStorageAPI;
import xaero.pac.client.player.config.PlayerConfigClientSynchronizer;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageManagerAPI;

import javax.annotation.Nonnull;

public interface IClientData
<
	PCSM extends IPlayerConfigClientStorageManagerAPI<?>, 
	CPS extends IClientPartyStorageAPI<?, ?>,
	CM extends IClientClaimsManager<?, ?, ?>
> extends IClientDataAPI<PCSM, CPS, CM> {
	//internal api

	@Nonnull
	@Override
	public XPACKeyBindings getKeyBindings();
	
	public ClientTickHandler getClientTickHandler();
	public ClientWorldLoadHandler getClientWorldLoadHandler();
	public PlayerConfigClientSynchronizer getPlayerConfigClientSynchronizer();
	public void reset();
	public ClientClaimsSyncHandler getClientClaimsSyncHandler();
	
}
