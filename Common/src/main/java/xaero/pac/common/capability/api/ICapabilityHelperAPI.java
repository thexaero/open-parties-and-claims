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

package xaero.pac.common.capability.api;

import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;
import xaero.pac.common.capability.ICapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper API for getting capability values of objects
 */
public interface ICapabilityHelperAPI {

    /**
     * Gets the capability value of a specified type for a specified object.
     * <p>
     * Client world capability types can be found in {@link ClientWorldCapabilityTypes}
     *
     * @param object  the object that the capability is attached to, not null
     * @param capability  the capability type, not null
     * @return the capability value attached to the object,
     *              null when a capability of the specified type isn't attached to the object
     * @param <T>  the type of the capability value
     * @param <C>  the type of capability types
     */
    @Nullable
    public <T, C extends ICapability<T>> T getCapability(@Nonnull Object object, @Nonnull C capability);

}
