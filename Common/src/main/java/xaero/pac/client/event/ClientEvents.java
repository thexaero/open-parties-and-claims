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

package xaero.pac.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import xaero.pac.client.IClientData;
import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.IClientDimensionClaimsManager;
import xaero.pac.client.claims.IClientRegionClaims;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.client.parties.party.IClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientPartyMemberDynamicInfoSyncableStorage;
import xaero.pac.client.parties.party.IClientPartyStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;

public class ClientEvents {
	
	private final IClientData
	<
		IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>,
		IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>,
		IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
	> clientData;
	
	protected ClientEvents(IClientData
		<
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>,
			IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>,
			IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
		> clientData) {
		super();
		this.clientData = clientData;
	}

	public void onClientTick(boolean isTickStart) {
		if(isTickStart) {
			clientData.getClientTickHandler().tick(clientData);
		}
	}

	public void onClientWorldLoaded(ClientLevel world) {
		clientData.getClientWorldLoadHandler().handle(world, Minecraft.getInstance().player);
	}

	public void onPlayerLogout(LocalPlayer player) {
		clientData.reset();
	}

	public void onPlayerLogin(LocalPlayer player) {
		clientData.getClientWorldLoadHandler().handle(player.clientLevel, player);
	}

	public static abstract class Builder<B extends Builder> {

		protected IClientData
		<
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>,
			IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>,
			IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
		> clientData;

		private final B self;

		@SuppressWarnings("unchecked")
		protected Builder(){
			this.self = (B) this;
		}

		public B setDefault() {
			setClientData(null);
			return self;
		}
		
		public B setClientData(IClientData
			<
				IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>,
				IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>,
				IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
			> clientData) {
			this.clientData = clientData;
			return self;
		}
		
		public ClientEvents build(){
			if(clientData == null)
				throw new IllegalStateException();
			return buildInternally();
		}

		protected abstract ClientEvents buildInternally();
		
	}

}
