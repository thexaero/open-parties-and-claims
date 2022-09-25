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

package xaero.pac.common.server.claims.player.api;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.api.IPlayerClaimInfoAPI;
import xaero.pac.common.claims.player.api.IPlayerDimensionClaimsAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for claim info of a player on the server side
 */
public interface IServerPlayerClaimInfoAPI<DC extends IPlayerDimensionClaimsAPI<?>> extends IPlayerClaimInfoAPI<DC> {
	
	@Override
	public int getClaimCount();
	
	@Override
	public int getForceloadCount();
	
	@Nonnull
	@Override
	public UUID getPlayerId();
	
	@Nonnull
	@Override
	public String getPlayerUsername();

	@Override
	public String getClaimsName();

	@Override
	public int getClaimsColor();

	@Nullable
	@Override
	String getClaimsName(int subConfigIndex);

	@Nullable
	@Override
	Integer getClaimsColor(int subConfigIndex);

	@Nonnull
	@Override
	public Stream<Entry<ResourceLocation, DC>> getStream();

	@Nullable
	public DC getDimension(@Nonnull ResourceLocation id);

	/**
	 * Gets the currently configured custom name of the player's sub-claim
	 * with a specified string ID.
	 * <p>
	 * Returns null if no such sub-claim exists or the name is inherited from
	 * the main config.
	 *
	 * @param subId  the string ID of the sub-config used by the sub-claim, not null
	 * @return the custom name of the sub-claim
	 */
	@Nullable
	public String getClaimsName(@Nonnull String subId);

	/**
	 * Gets the currently configured color of the player's sub-claim with
	 * a specified string ID.
	 * <p>
	 * Returns null if no such sub-claim exists or the color is inherited from
	 * the main config.
	 *
	 * @param subId  the string ID of the sub-config used by the sub-claim, not null
	 * @return the sub-claim color Integer, null if no such sub-claim exists
	 */
	@Nullable
	public Integer getClaimsColor(@Nonnull String subId);

}
