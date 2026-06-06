/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.parties.system.api;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @deprecated use {@link xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI} instead
 * <p>
 * The interface to be overridden by addons that wish to implement additional party systems to be used
 * by Open Parties and Claims (just the claiming feature as of writing this).
 * <p>
 * Player position synchronization is a part of the default party system. Similar functionality should
 * be implemented in other party systems and supported separately by the map mods that display party
 * members. OPAC does not synchronize or provide party member/ally positions from other party systems,
 * even if they're implemented using this interface.
 * <p>
 * Party system implementations must be registered in {@link IPlayerPartySystemRegisterAPI}.
 *
 * @param <P> the type of parties in the implemented system
 */
@Deprecated
public interface IPlayerPartySystemAPI<P> extends xaero.pac.common.server.parties.system.api.v2.IPlayerPartySystemAPI<P> {

	@Override
	default boolean canEditPartyConfig(@Nonnull UUID playerId){
		return false;
	}

	@Override
	default boolean canCreatePartyConfigGroups(@Nonnull UUID playerId){
		return false;
	}

	@Override
	default boolean canIncludeGroupsInPartyConfigGroups(@Nonnull UUID playerId){
		return false;
	}

	@Override
	default boolean canIncludePlayersInPartyConfigGroups(@Nonnull UUID playerId){
		return false;
	}

	@Override
	@Nullable
	default UUID getOwner(@Nonnull P party){
		return null;
	}

	@Override
	@Nullable
	default Component getName(@Nonnull P party){
		return null;
	}

	@Override
	default int getMemberCount(@Nonnull P party){
		return 0;
	}

}
