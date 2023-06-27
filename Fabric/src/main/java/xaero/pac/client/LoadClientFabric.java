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

import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.client.event.ClientEventsFabric;
import xaero.pac.common.LoadCommonFabric;
import xaero.pac.common.packet.PacketHandlerFabric;

public class LoadClientFabric extends LoadCommonFabric<LoadClient> {

	public LoadClientFabric(OpenPartiesAndClaimsFabric modMain) {
		super(modMain, new LoadClient(modMain));
	}

	public void loadClient(){
		loader.loadClient();
		((PacketHandlerFabric)modMain.getPacketHandler()).registerOnClient();
		ClientEventsFabric clientEvents = ClientEventsFabric.Builder.begin().setClientData(modMain.getClientDataInternal()).build();
		clientEvents.registerFabricAPIEvents();
		modMain.setClientEvents(clientEvents);
	}

}
