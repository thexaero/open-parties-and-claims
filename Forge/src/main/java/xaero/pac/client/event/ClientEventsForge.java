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

package xaero.pac.client.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xaero.pac.OpenPartiesAndClaimsForge;
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
import xaero.pac.client.world.capability.ClientWorldCapabilityProvider;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;

public final class ClientEventsForge extends ClientEvents {

	protected ClientEventsForge(IClientData<IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>, IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>, IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>> clientData) {
		super(clientData);
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		super.onClientTick(event.phase == Phase.START);
	}

	@SubscribeEvent
	public void onWorldLoaded(WorldEvent.Load event) {
		super.onWorldLoaded(event.getWorld());
	}

	@SubscribeEvent
	public void onPlayerLogout(ClientPlayerNetworkEvent.LoggedOutEvent event){
		super.onPlayerLogout(event.getPlayer());
	}
	
	@SubscribeEvent
	public void onPlayerLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
		super.onPlayerLogin(event.getPlayer());
	}

	@SubscribeEvent
	public void handleRenderPlayerEventPost(RenderPlayerEvent.Post event){
		super.handleRenderPlayerEventPost((AbstractClientPlayer) event.getPlayer(), event.getRenderer(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick(), event.getPoseStack());
	}

	@SubscribeEvent
	public void worldCapabilities(AttachCapabilitiesEvent<Level> event) {
		if(event.getObject() instanceof ClientLevel) {
			ClientWorldCapabilityProvider capProvider = new ClientWorldCapabilityProvider();
			event.addCapability(new ResourceLocation(OpenPartiesAndClaimsForge.class.getAnnotation(Mod.class).value(), "client_world_main_capability"), capProvider);
			event.addListener(capProvider::invalidateCaps);
		}
	}
	
	public static final class Builder extends ClientEvents.Builder {

		@Override
		public Builder setDefault() {
			super.setDefault();
			return this;
		}

		@Override
		protected ClientEvents buildInternally() {
			return new ClientEventsForge(clientData);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
