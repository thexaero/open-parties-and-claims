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

package xaero.pac.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import xaero.pac.OpenPartiesAndClaimsNeoForge;
import xaero.pac.common.LoadCommonNeoForge;
import xaero.pac.server.event.DedicatedServerEventsForge;

public class LoadDedicatedServerNeoForge extends LoadCommonNeoForge<LoadDedicatedServer> {

	public LoadDedicatedServerNeoForge(OpenPartiesAndClaimsNeoForge modMain) {
		super(modMain, new LoadDedicatedServer(modMain));
		NeoForge.EVENT_BUS.register(new DedicatedServerEventsForge());
	}

	@SubscribeEvent
	public void loadServer(final FMLDedicatedServerSetupEvent event) {
		loader.loadServer();
	}

	@SubscribeEvent
	public void loadCommon(final FMLCommonSetupEvent event) {
		super.loadCommon(event);
	}

	@SubscribeEvent
	public void onRegisterPayloadHandler(RegisterPayloadHandlerEvent event){
		super.onRegisterPayloadHandler(event);
	}

}
