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

package xaero.pac.client.parties.party;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPartyAllyInfoStorage implements IClientPartyAllyInfoStorage<ClientPartyAllyInfo> {

	private final Map<UUID, ClientPartyAllyInfo> cache;

	private ClientPartyAllyInfoStorage(Map<UUID, ClientPartyAllyInfo> cache) {
		super();
		this.cache = cache;
	}
	
	@Nullable
	@Override
	public ClientPartyAllyInfo get(@Nonnull UUID id) {
		return cache.get(id);
	}
	
	public void add(ClientPartyAllyInfo info) {
		cache.put(info.getAllyId(), info);
	}

	@Override
	public void remove(UUID id) {
		cache.remove(id);
	}

	@Override
	public void clear() {
		cache.clear();
	}
	
	public static final class Builder {
		
		private Builder() {}
		
		public ClientPartyAllyInfoStorage build() {
			return new ClientPartyAllyInfoStorage(new HashMap<>());
		}
		
		public static Builder begin() {
			return new Builder();
		}
		
	}
	
}
