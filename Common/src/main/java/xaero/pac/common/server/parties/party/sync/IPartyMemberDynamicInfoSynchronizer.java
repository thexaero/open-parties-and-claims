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

package xaero.pac.common.server.parties.party.sync;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;

public interface IPartyMemberDynamicInfoSynchronizer
<
	P extends IServerParty<?, ?>
> {

	//internal api
	
	void syncToPartyDynamicInfo(P party, IPartyMemberDynamicInfoSyncable syncedInfo, P fromParty);

	void syncToPartyMutualAlliesDynamicInfo(P party, IPartyMemberDynamicInfoSyncable syncedInfo);

	void syncToClientAllDynamicInfo(ServerPlayer onlinePlayer, P party, boolean b);

	void syncToClientMutualAlliesDynamicInfo(ServerPlayer onlinePlayer, P party, boolean b);
	
	void handlePlayerLeave(P playerParty, ServerPlayer player);

	void syncToPartiesDynamicInfo(P playerParty, IPartyMemberDynamicInfoSyncable oftenSyncedPartyMemberInfo);

	void onPlayerTick(ServerPlayerData mainCap, ServerPlayer player);
	
}
