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
import xaero.pac.common.capability.api.ICapabilityHelperAPI;

import javax.annotation.Nonnull;

public class CapabilityHelper implements ICapabilityHelperAPI {

	@Nullable
	@Override
	public <T, C extends ICapability<T>> T getCapability(@Nonnull Object object, @Nonnull C capability) {
		//only supports ClientLevel instances as of writing this
		//can be extended to other classes with mixins implementing IFabricCapableObject
		if(!(object instanceof ICapableObject capableObject))
			return null;
		ICapabilityProvider provider = capableObject.getXaero_OPAC_CapabilityProvider();
		if(provider == null)
			capableObject.setXaero_OPAC_CapabilityProvider(provider = ((ICapabilityType<?>)capability).createProvider());
		return provider.getCapability(capability);
	}

}
