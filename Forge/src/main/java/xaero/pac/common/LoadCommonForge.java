/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.event.CommonEventsForge;

public class LoadCommonForge<L extends LoadCommon> {
	
	protected final OpenPartiesAndClaims modMain;
	protected final L loader;
	
	public LoadCommonForge(OpenPartiesAndClaims modMain, L loader) {
		this.modMain = modMain;
		this.loader = loader;
		MinecraftForge.EVENT_BUS.register(new CommonEventsForge(modMain));
	}
	
	@SubscribeEvent
	public void loadCommon(final FMLCommonSetupEvent event) {
		loader.loadCommon();
	}

}
