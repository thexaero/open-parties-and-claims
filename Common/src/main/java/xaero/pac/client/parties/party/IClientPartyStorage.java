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

import xaero.pac.client.parties.party.api.IClientPartyStorageAPI;
import xaero.pac.common.parties.party.IParty;

import javax.annotation.Nonnull;

public interface IClientPartyStorage
<
	A extends IClientPartyAllyInfo, 
	P extends IClientParty<?,?>,
	MISS extends IClientPartyMemberDynamicInfoSyncableStorage<?>
> extends IClientPartyStorageAPI<P, MISS> {
	
	//internal api

	public void setParty(P party);
	
	public void setPartyCast(IParty<?,?> party);
	
	public void setPartyName(String partyName);
	
	@Nonnull
	@Override
	public IClientPartyAllyInfoStorage<A> getAllyInfoStorage();
	
	public void setLoading(boolean loading);
	
	public void setLoadingMemberCount(int loadingMemberCount);
	
	public void setLoadingAllyCount(int loadingAllyCount);
	
	public void setLoadingInviteCount(int loadingInviteCount);
	
	public void setMemberLimit(int memberLimit);
	
	public void setAllyLimit(int allyLimit);
	
	public void setInviteLimit(int inviteLimit);
	
}
