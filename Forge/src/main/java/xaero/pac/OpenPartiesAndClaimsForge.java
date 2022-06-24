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

package xaero.pac;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import xaero.pac.client.LoadClientForge;
import xaero.pac.client.world.capability.ClientWorldCapabilityProvider;
import xaero.pac.common.LoadCommonForge;
import xaero.pac.common.capability.CapabilityHelperForge;
import xaero.pac.common.config.ForgeConfigHelperForge;
import xaero.pac.common.mods.ModSupportForge;
import xaero.pac.common.packet.PacketHandlerForge;
import xaero.pac.server.LoadDedicatedServerForge;

@Mod("openpartiesandclaims")
public class OpenPartiesAndClaimsForge extends OpenPartiesAndClaims {

    public OpenPartiesAndClaimsForge() {
		super(new CapabilityHelperForge(), new PacketHandlerForge(), new ForgeConfigHelperForge(), new ModSupportForge());
    	LoadCommonForge<?> loader = FMLLoader.getDist() == Dist.CLIENT ? new LoadClientForge(this) : new LoadDedicatedServerForge(this);
    	FMLJavaModLoadingContext.get().getModEventBus().register(loader);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientWorldCapabilityProvider::registerCapabilities);
    }
    
}
