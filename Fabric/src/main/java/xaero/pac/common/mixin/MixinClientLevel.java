/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import xaero.pac.client.world.capability.ClientWorldCapabilityProviderFabric;
import xaero.pac.common.capability.IFabricCapableObject;
import xaero.pac.common.capability.IFabricCapabilityProvider;

@Mixin(ClientLevel.class)
public class MixinClientLevel implements IFabricCapableObject {

	private ClientWorldCapabilityProviderFabric xaero_OPAC_CapabilityProvider;

	@Override
	public IFabricCapabilityProvider getXaero_OPAC_CapabilityProvider() {
		if(xaero_OPAC_CapabilityProvider == null)
			xaero_OPAC_CapabilityProvider = new ClientWorldCapabilityProviderFabric();
		return xaero_OPAC_CapabilityProvider;
	}

}
