/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import xaero.pac.OpenPartiesAndClaimsNeoForge;
import xaero.pac.client.controls.XPACKeyBindings;
import xaero.pac.client.event.ClientEventsNeoForge;
import xaero.pac.common.LoadCommonNeoForge;
import xaero.pac.common.packet.PacketHandlerNeoForge;

public class LoadClientNeoForge extends LoadCommonNeoForge<LoadClient> {

	public LoadClientNeoForge(OpenPartiesAndClaimsNeoForge modMain) {
		super(modMain, new LoadClient(modMain));
	}

	@SubscribeEvent
	public void loadClient(final FMLClientSetupEvent event) {
		loader.loadClient();
		ClientEventsNeoForge clientEventsNeoForge = ClientEventsNeoForge.Builder.begin().setClientData(modMain.getClientDataInternal()).build();
		modMain.setClientEventsNeoForge(clientEventsNeoForge);
		NeoForge.EVENT_BUS.register(clientEventsNeoForge);
	}

	@SubscribeEvent
	public void loadCommon(final FMLCommonSetupEvent event) {
		super.loadCommon(event);
	}

	@SubscribeEvent
	public void registerKeyBindings(final RegisterKeyMappingsEvent event){
		loader.getKeyBindings().register(event::register, event::registerCategory);
	}

	@SubscribeEvent
	public void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event){
		super.onRegisterPayloadHandler(event);
	}

	@SubscribeEvent
	public void onRegisterClientPayloadHandler(RegisterClientPayloadHandlersEvent event){
		PacketHandlerNeoForge.registerClientPayloadHandler(event);
	}

}
