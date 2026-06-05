/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for claim info of a player on the server side
 */
public interface IServerPlayerClaimInfoAPI extends IPlayerClaimInfoAPI {
	
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

	/**
	 * Gets a stream of all dimension claim info entries for the player.
	 *
	 * @return the stream of all dimension claim info entries, not null
	 */
	@Nonnull
	public Stream<Entry<ResourceLocation, IPlayerDimensionClaimsAPI>> getStream();

	@Override
	@Nullable
	public IPlayerDimensionClaimsAPI getDimension(@Nonnull ResourceLocation id);

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

	/**
	 * Resyncs the name of the party that owns the claims stored in this player claim info, which is displayed when the
	 * mod is in party-owned-claims mode. The party that owns the claims is the party owned by this player.
	 * <p>
	 * If the specified party system argument is not the primary system, then nothing will happen, as only the primary
	 * party system is used in party-owned-claims mode. So if a party is renamed in any of the party systems, then this
	 * method can be safely called, whether it's the primary system or not.
	 * <p>
	 * Calling this method is optional because the mod automatically detects party name changes every second, if at
	 * least one party member is online. This method can be used to speed it up or to update the name for parties that
	 * don't have anyone online. The default built-in party system does so.
	 *
	 * @param partySystem  the party system in which the party owned by this player was renamed and might require
	 *                        resync, not null
	 */
	public void resyncPartyName(@Nonnull IPlayerPartySystemAPI<?> partySystem);

	/**
	 * @deprecated Use {@link #resyncPartyName(IPlayerPartySystemAPI)} instead.
	 * <p>
	 * Resyncs the name of the party that owns the claims stored in this player claim info, which is displayed when the
	 * mod is in party-owned-claims mode. The party that owns the claims is the party owned by this player.
	 * <p>
	 * If the specified party system argument is not the primary system, then nothing will happen, as only the primary
	 * party system is used in party-owned-claims mode. So if a party is renamed in any of the party systems, then this
	 * method can be safely called, whether it's the primary system or not.
	 * <p>
	 * Calling this method is optional because the mod automatically detects party name changes every second, if at
	 * least one party member is online. This method can be used to speed it up or to update the name for parties that
	 * don't have anyone online. The default built-in party system does so.
	 *
	 * @param partySystem  the party system in which the party owned by this player was renamed and might require
	 *                        resync, not null
	 */
	@Deprecated
	default void resyncPartyName(@Nonnull xaero.pac.common.server.parties.system.api.IPlayerPartySystemAPI<?> partySystem){
		resyncPartyName((IPlayerPartySystemAPI<?>)partySystem);
	}

}
