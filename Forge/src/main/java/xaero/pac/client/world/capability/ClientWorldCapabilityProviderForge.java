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

package xaero.pac.client.world.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import xaero.pac.client.world.ClientWorldData;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;
import xaero.pac.client.world.capability.api.ClientWorldMainCapabilityAPI;
import xaero.pac.common.capability.ForgeCapabilityWrapper;

import javax.annotation.Nonnull;

public class ClientWorldCapabilityProviderForge extends ClientWorldCapabilityProvider implements ICapabilityProvider {

	public static Capability<ClientWorldMainCapabilityAPI> MAIN_CAP = null;
	private final LazyOptional<ClientWorldMainCapabilityAPI> lazyMainCapability;
	private static boolean registered;

	public ClientWorldCapabilityProviderForge() {
		super();
		lazyMainCapability = LazyOptional.of(() -> mainCapability);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		if(registered)
			throw new RuntimeException(new IllegalAccessException());
		event.register(ClientWorldMainCapabilityAPI.class);
		MAIN_CAP = CapabilityManager.get(new CapabilityToken<>(){});
		if(MAIN_CAP == null)
			throw new RuntimeException("Couldn't fetch the client world main capability!");
		ClientWorldCapabilityTypes.MAIN_CAP = new ForgeCapabilityWrapper<>(MAIN_CAP);
		registered = true;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		if(cap == MAIN_CAP)
			return lazyMainCapability.cast();
		return LazyOptional.empty();
	}

	public void invalidateCaps() {
		lazyMainCapability.invalidate();
	}

}
