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

package xaero.pac.common.claims.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * API for a dimension claims manager
 */
public interface IDimensionClaimsManagerAPI
<
	WRC extends IRegionClaimsAPI
> {

	/**
	 * Gets the dimension ID.
	 *
	 * @return the dimension ID, not null
	 */
	@Nonnull
	public ResourceLocation getDimension();

	/**
	 * Gets the number of 512x512 regions that contain claims in this dimension.
	 *
	 * @return the region count
	 */
	public int getCount();

	/**
	 * Gets a {@link Stream} of all 512x512 regions that contain claims in this dimension.
	 *
	 * @return a stream of all regions, not null
	 */
	@Nonnull
	public Stream<WRC> getRegionStream();

	/**
	 * Gets region claims at a specified 512x512 region location in this dimension,
	 * or null if there are no claims stored for the specified location.
	 *
	 * @param x  the X coordinate of the 512x512 region
	 * @param z  the Z coordinate of the 512x512 region
	 * @return the read-only region claims manager, null if it doesn't exist
	 */
	@Nullable
	public WRC getRegion(int x, int z);
	
}
