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

package xaero.pac.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

public final class ClientEventsFabric extends ClientEvents {

	protected ClientEventsFabric(IClientData<IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>, IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>, IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>> clientData) {
		super(clientData);
	}

	public void registerFabricAPIEvents(){
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTickStart);
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);

	}

	public void onClientTickStart(Minecraft minecraft) {
		super.onClientTick(true);
	}

	public void onClientTickEnd(Minecraft minecraft) {
		super.onClientTick(false);
	}

	public void onClientWorldLoaded(ClientLevel world) {
		super.onClientWorldLoaded(world);
	}

	public void onPlayerLogout(LocalPlayer player){
		super.onPlayerLogout(player);
	}
	
	public void onPlayerLogin(LocalPlayer player) {
		super.onPlayerLogin(player);
	}
	
	public static final class Builder extends ClientEvents.Builder<Builder> {

		@Override
		public Builder setDefault() {
			super.setDefault();
			return this;
		}

		@Override
		public ClientEventsFabric build() {
			return (ClientEventsFabric) super.build();
		}

		@Override
		protected ClientEvents buildInternally() {
			return new ClientEventsFabric(clientData);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
