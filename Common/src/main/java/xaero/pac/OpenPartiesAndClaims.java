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

package xaero.pac;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xaero.pac.client.ClientData;
import xaero.pac.client.IClientData;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.IClientDimensionClaimsManager;
import xaero.pac.client.claims.IClientRegionClaims;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.event.ClientEvents;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.client.parties.party.IClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientPartyMemberDynamicInfoSyncableStorage;
import xaero.pac.client.parties.party.IClientPartyStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.capability.CapabilityHelper;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.config.IForgeConfigHelper;
import xaero.pac.common.event.CommonEvents;
import xaero.pac.common.mods.ModSupport;
import xaero.pac.common.packet.IPacketHandler;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.CrashHandler;

/**
 * For internal use only.
 */
public abstract class OpenPartiesAndClaims {

	public static final String MOD_ID = "openpartiesandclaims";

	public static OpenPartiesAndClaims INSTANCE;
	public static final Logger LOGGER = LogManager.getLogger();
	
	public final CrashHandler startupCrashHandler;
	private ClientData clientData;
	private OpenPACClientAPI clientAPI;
	private final CapabilityHelper capabilityHelper;
	private final IPacketHandler packetHandler;
	private final IForgeConfigHelper forgeConfigHelper;
	private final ModSupport modSupport;
	public static final ResourceLocation MAIN_CHANNEL_LOCATION = new ResourceLocation(MOD_ID, "main");

	public OpenPartiesAndClaims(CapabilityHelper capabilityHelper, IPacketHandler packetHandler, IForgeConfigHelper forgeConfigHelper, ModSupport modSupport) {
		this.capabilityHelper = capabilityHelper;
		this.packetHandler = packetHandler;
		this.forgeConfigHelper = forgeConfigHelper;
		this.modSupport = modSupport;
		INSTANCE = this;
		startupCrashHandler = new CrashHandler();
	}
	
	@SuppressWarnings("unchecked")
	public IClientData
	<
		IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>,
		IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>,
		IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
	> getClientDataInternal() {
		return (IClientData
				<
				IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>,
				IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>,
				IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
			>
		)(Object)clientData;
	}
	
	public void setClientData(ClientData clientData) {
		if(this.clientData != null)
			throw new IllegalAccessError();
		this.clientData = clientData;
		this.clientAPI = new OpenPACClientAPI();
	}
	
	public OpenPACClientAPI getClientAPI() {
		return clientAPI;
	}

	public CapabilityHelper getCapabilityHelper() {
		return capabilityHelper;
	}

	public IPacketHandler getPacketHandler() {
		return packetHandler;
	}

	public IForgeConfigHelper getForgeConfigHelper() {
		return forgeConfigHelper;
	}

	public ModSupport getModSupport() {
		return modSupport;
	}

	public abstract ClientEvents getClientEvents();

	public abstract CommonEvents getCommonEvents();

}
