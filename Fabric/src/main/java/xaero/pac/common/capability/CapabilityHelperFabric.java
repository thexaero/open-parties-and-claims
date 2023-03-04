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

package xaero.pac.common.capability;
import org.jetbrains.annotations.Nullable;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;

import javax.annotation.Nonnull;

public class CapabilityHelperFabric implements ICapabilityHelper {

	public static void createCapabilities(){
		ClientWorldCapabilityTypes.MAIN_CAP = new FabricCapabilityType<>();
	}

	@Nullable
	@Override
	public <T, C extends ICapability<T>> T getCapability(@Nonnull Object object, @Nonnull C capability) {
		//only supports ClientLevel instances as of writing this
		//can be extended to other classes with mixins implementing IFabricCapableObject
		if(!(object instanceof IFabricCapableObject capableObject))
			return null;
		return capableObject.getXaero_OPAC_CapabilityProvider().getCapability(capability);
	}

}
