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

package xaero.pac;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLLoader;
import xaero.pac.client.LoadClientNeoForge;
import xaero.pac.client.event.ClientEventsNeoForge;
import xaero.pac.common.LoadCommonNeoForge;
import xaero.pac.common.capability.CapabilityHelper;
import xaero.pac.common.config.ForgeConfigHelperNeoForge;
import xaero.pac.common.event.CommonEventsNeoForge;
import xaero.pac.common.mods.ModSupportNeoForge;
import xaero.pac.common.packet.PacketHandlerNeoForge;
import xaero.pac.server.LoadDedicatedServerNeoForge;

@Mod(OpenPartiesAndClaims.MOD_ID)
public class OpenPartiesAndClaimsNeoForge extends OpenPartiesAndClaims {

	private ClientEventsNeoForge clientEventsNeoForge;
	private CommonEventsNeoForge commonEventsNeoForge;

	public OpenPartiesAndClaimsNeoForge() {
		super(new CapabilityHelper(), new PacketHandlerNeoForge(), new ForgeConfigHelperNeoForge(), new ModSupportNeoForge());
		LoadCommonNeoForge<?> loader = FMLLoader.getDist() == Dist.CLIENT ? new LoadClientNeoForge(this) : new LoadDedicatedServerNeoForge(this);
		FMLJavaModLoadingContext.get().getModEventBus().register(loader);
	}

	public void setClientEventsNeoForge(ClientEventsNeoForge clientEventsNeoForge) {
		this.clientEventsNeoForge = clientEventsNeoForge;
	}

	public void setCommonEventsForge(CommonEventsNeoForge commonEventsNeoForge) {
		this.commonEventsNeoForge = commonEventsNeoForge;
	}

	@Override
	public ClientEventsNeoForge getClientEvents() {
		return clientEventsNeoForge;
	}

	@Override
	public CommonEventsNeoForge getCommonEvents() {
		return commonEventsNeoForge;
	}

}
