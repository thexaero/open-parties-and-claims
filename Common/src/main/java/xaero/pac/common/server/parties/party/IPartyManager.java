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

package xaero.pac.common.server.parties.party;

import net.minecraft.world.entity.player.Player;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;
import xaero.pac.common.server.parties.party.sync.IPartySynchronizer;
import xaero.pac.common.server.parties.system.impl.DefaultPlayerPartySystem;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

public interface IPartyManager<
	P extends IServerParty<?, ?, ?>
> extends IPartyManagerAPI {

	//internal API
	@Nullable
	@Override
	public P getPartyByOwner(@Nonnull UUID owner);

	@Nullable
	@Override
	public P getPartyById(@Nonnull UUID id);

	@Nullable
	@Override
	public P getPartyByMember(@Nonnull UUID member);

	@Nullable
	@Override
	P createPartyForOwner(@Nonnull Player owner);

	public IPartySynchronizer<P> getPartySynchronizer();
	public IPlayerConfigManager getPlayerConfigs();

	public void removeTypedParty(@Nonnull P party);
	@Nonnull
	public Stream<P> getTypedAllStream();
	@Nonnull
	public Stream<P> getTypedPartiesThatAlly(@Nonnull UUID allyId);

	@Override
	@SuppressWarnings("unchecked")
	default void removeParty(@Nonnull IServerPartyAPI party) {
		removeTypedParty((P)party);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IServerPartyAPI> getAllStream() {
		return (Stream<IServerPartyAPI>)(Object)getTypedAllStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IServerPartyAPI> getPartiesThatAlly(@Nonnull UUID allyId) {
		return (Stream<IServerPartyAPI>)(Object)getTypedPartiesThatAlly(allyId);
	}

	DefaultPlayerPartySystem getPartySystem();

}
