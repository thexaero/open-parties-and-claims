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

package xaero.pac.client.parties.party;

import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ClientPartyMemberDynamicInfoSyncableStorage implements IClientPartyMemberDynamicInfoSyncableStorage<PartyMemberDynamicInfoSyncable> {
	
	private Map<UUID, PartyMemberDynamicInfoSyncable> storage;

	private ClientPartyMemberDynamicInfoSyncableStorage(Map<UUID, PartyMemberDynamicInfoSyncable> storage) {
		super();
		this.storage = storage;
	}

	@Override
	public PartyMemberDynamicInfoSyncable getForPlayer(@Nonnull UUID playerId) {
		return storage.get(playerId);
	}

	public PartyMemberDynamicInfoSyncable getOrSetForPlayer(UUID playerId, PartyMemberDynamicInfoSyncable defaultInfo) {
		return storage.computeIfAbsent(playerId, i -> defaultInfo == null ? new PartyMemberDynamicInfoSyncable(i, true) : defaultInfo);
	}

	@Override
	public boolean removeForPlayer(UUID playerId) {
		return storage.remove(playerId) != null;
	}

	@Override
	public Stream<PartyMemberDynamicInfoSyncable> getAllStream() {
		return storage.values().stream();
	}

	@Override
	public void clear() {
		storage.clear();
	}
	
	public static final class Builder {
		
		private Builder() {}
		
		public ClientPartyMemberDynamicInfoSyncableStorage build() {
			return new ClientPartyMemberDynamicInfoSyncableStorage(new HashMap<>());
		}
		
		public static Builder begin() {
			return new Builder();
		}
		
	}

}
