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

package xaero.pac.common.server.player.data;

import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerSyncHandler;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

public class ServerPlayerData extends ServerPlayerDataAPI {
	
	//internal api


	private boolean claimsAdminMode;
	private boolean claimsNonallyMode;
	private IPlayerChunkClaim lastClaimCheck;
	private int lastBaseClaimLimitSync;//used for detecting limit changes based on FTB ranks
	private int lastBaseForceloadLimitSync;
	private boolean checkedBaseForceloadLimitOnce;
	private PartyMemberDynamicInfoSyncable oftenSyncedPartyMemberInfo;
	private final ClaimsManagerPlayerSyncHandler claimsManagerPlayerSyncHandler;
	private final PlayerClaimActionRequestHandler claimActionRequestHandler;
	
	public ServerPlayerData(ClaimsManagerPlayerSyncHandler claimsManagerPlayerSyncHandler, PlayerClaimActionRequestHandler claimActionRequestHandler) {
		super();
		this.claimsManagerPlayerSyncHandler = claimsManagerPlayerSyncHandler;
		this.claimActionRequestHandler = claimActionRequestHandler;
	}

	@Override
	public boolean isClaimsAdminMode() {
		return claimsAdminMode;
	}

	@Override
	public boolean isClaimsNonallyMode() {
		return claimsNonallyMode;
	}
	
	public void setOftenSyncedPartyMemberInfo(PartyMemberDynamicInfoSyncable oftenSyncedPartyMemberInfo) {
		this.oftenSyncedPartyMemberInfo = oftenSyncedPartyMemberInfo;
	}
	
	public void setClaimsAdminMode(boolean claimsAdminMode) {
		this.claimsAdminMode = claimsAdminMode;
	}
	
	public void setClaimsNonallyMode(boolean claimsNonallyMode) {
		this.claimsNonallyMode = claimsNonallyMode;
	}
	
	public void setLastClaimCheck(IPlayerChunkClaim lastClaimCheck) {
		this.lastClaimCheck = lastClaimCheck;
	}

	public IPlayerChunkClaim getLastClaimCheck() {
		return (IPlayerChunkClaim) lastClaimCheck;
	}
	
	public PartyMemberDynamicInfoSyncable getPartyMemberDynamicInfo() {
		return oftenSyncedPartyMemberInfo;
	}
	
	public ClaimsManagerPlayerSyncHandler getClaimsManagerPlayerSyncHandler() {
		return claimsManagerPlayerSyncHandler;
	}
	
	public PlayerClaimActionRequestHandler getClaimActionRequestHandler() {
		return claimActionRequestHandler;
	}

	public void setLastClaimLimitsSync(int lastBaseClaimLimitSync, int lastBaseForceloadLimitSync) {
		this.lastBaseClaimLimitSync = lastBaseClaimLimitSync;
		this.lastBaseForceloadLimitSync = lastBaseForceloadLimitSync;
	}

	public boolean checkBaseClaimLimitsSync(int currentBaseClaimLimit, int currentBaseForceloadLimit) {
		return lastBaseClaimLimitSync != currentBaseClaimLimit || lastBaseForceloadLimitSync != currentBaseForceloadLimit;
	}

	public boolean haveCheckedBaseForceloadLimitOnce() {
		return checkedBaseForceloadLimitOnce;
	}

	public void setCheckedBaseForceloadLimitOnce(){
		checkedBaseForceloadLimitOnce = true;
	}

}
