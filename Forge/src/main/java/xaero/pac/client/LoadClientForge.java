/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2025, Xaero <xaero1996@gmail.com> and contributors
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

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xaero.pac.OpenPartiesAndClaimsForge;
import xaero.pac.client.event.ClientEventsForge;
import xaero.pac.common.LoadCommonForge;

public class LoadClientForge extends LoadCommonForge<LoadClient> {
	
	public LoadClientForge(OpenPartiesAndClaimsForge modMain, FMLJavaModLoadingContext context) {
		super(modMain, context, new LoadClient(modMain));
	}

	public void loadClient(final FMLClientSetupEvent event) {
		loader.loadClient();
		ClientEventsForge clientEventsForge = ClientEventsForge.Builder.begin().setClientData(modMain.getClientDataInternal()).build();
		modMain.setClientEventsForge(clientEventsForge);
		MinecraftForge.EVENT_BUS.register(clientEventsForge);
	}

	public void registerKeyBindings(final RegisterKeyMappingsEvent event){
		loader.getKeyBindings().register(event::register, c -> {});
	}

	@Override
	public void registerEvents() {
		super.registerEvents();
		FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(this::loadClient);
		RegisterKeyMappingsEvent.BUS.addListener(this::registerKeyBindings);
	}

}
