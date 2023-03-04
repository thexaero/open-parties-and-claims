/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims;

import xaero.pac.common.claims.IClaimsManager;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.claims.sync.IClaimsManagerSynchronizer;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;

public interface IServerClaimsManager
<
	C extends IPlayerChunkClaim, 
	PCI extends IServerPlayerClaimInfo<?>,
	WCM extends IServerDimensionClaimsManager<?>
> extends IClaimsManager<PCI, WCM>, IServerClaimsManagerAPI<C, PCI, WCM> {
	//internal API
	
	public IClaimsManagerSynchronizer getClaimsManagerSynchronizer();
	public ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> getClaimReplaceTaskHandler();
	public ServerClaimsPermissionHandler getPermissionHandler();

}
