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

package xaero.pac.common.packet;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.LoadCommon;
import xaero.pac.common.packet.claims.*;
import xaero.pac.common.packet.config.PlayerConfigOptionValuePacket;
import xaero.pac.common.packet.config.ServerboundOtherPlayerConfigPacket;
import xaero.pac.common.packet.parties.ClientboundPartyAllyPacket;
import xaero.pac.common.packet.parties.ClientboundPartyNamePacket;
import xaero.pac.common.packet.parties.ClientboundPartyPacket;
import xaero.pac.common.packet.parties.ClientboundPartyPlayerPacket;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;

public class PacketRegister {
	
	public void register(LoadCommon context) {
		IPacketHandler packetHandler = OpenPartiesAndClaims.INSTANCE.getPacketHandler();

		ClientboundPacServerLoginResetPacket.Codec serverHandshakeCodec = new ClientboundPacServerLoginResetPacket.Codec();
		packetHandler.register(0, ClientboundPacServerLoginResetPacket.class, serverHandshakeCodec, serverHandshakeCodec, null, new ClientboundPacServerLoginResetPacket.ClientHandler());

		ClientboundPacDimensionHandshakePacket.Codec handshakeCodec = new ClientboundPacDimensionHandshakePacket.Codec();
		packetHandler.register(1, ClientboundPacDimensionHandshakePacket.class, handshakeCodec, handshakeCodec, null, new ClientboundPacDimensionHandshakePacket.ClientHandler());
		
		PlayerConfigOptionValuePacket.Codec playerConfigOptionValueCodec = new PlayerConfigOptionValuePacket.Codec();
		packetHandler.register(2, PlayerConfigOptionValuePacket.class, playerConfigOptionValueCodec, playerConfigOptionValueCodec, new PlayerConfigOptionValuePacket.ServerHandler(), new PlayerConfigOptionValuePacket.ClientHandler());
		
		ServerboundOtherPlayerConfigPacket.Codec serverboundOtherPlayerConfigPacketCodec = new ServerboundOtherPlayerConfigPacket.Codec();
		packetHandler.register(3, ServerboundOtherPlayerConfigPacket.class, serverboundOtherPlayerConfigPacketCodec, serverboundOtherPlayerConfigPacketCodec, new ServerboundOtherPlayerConfigPacket.ServerHandler(), null);

		packetHandler.register(4, ClientboundPartyPacket.class, ClientboundPartyPacket.CODEC, ClientboundPartyPacket.CODEC, null, new ClientboundPartyPacket.ClientHandler());
		
		packetHandler.register(5, ClientboundPartyPlayerPacket.class, ClientboundPartyPlayerPacket.CODEC, ClientboundPartyPlayerPacket.CODEC, null, new ClientboundPartyPlayerPacket.ClientHandler());
		
		packetHandler.register(6, ClientboundPartyNamePacket.class, ClientboundPartyNamePacket.ENCODER, new ClientboundPartyNamePacket.Decoder(), null, new ClientboundPartyNamePacket.ClientHandler());
		
		ClientboundPartyAllyPacket.Decoder partyPartyAllyPacketDecoder = new ClientboundPartyAllyPacket.Decoder();
		packetHandler.register(7, ClientboundPartyAllyPacket.class, ClientboundPartyAllyPacket.ENCODER, partyPartyAllyPacketDecoder, null, new ClientboundPartyAllyPacket.ClientHandler());
		
		PartyMemberDynamicInfoSyncable.Codec partyPartyMemberOftenSyncedInfoPacketCodec = new PartyMemberDynamicInfoSyncable.Codec();
		packetHandler.register(8, PartyMemberDynamicInfoSyncable.class, partyPartyMemberOftenSyncedInfoPacketCodec, partyPartyMemberOftenSyncedInfoPacketCodec, null, new PartyMemberDynamicInfoSyncable.ClientHandler());
	
		packetHandler.register(9, ClientboundLoadingPacket.class, ClientboundLoadingPacket.ENCODER, new ClientboundLoadingPacket.Decoder(), null, new ClientboundLoadingPacket.ClientHandler());
		
		packetHandler.register(10, ClientboundPlayerClaimsDimensionPacket.class, ClientboundPlayerClaimsDimensionPacket.ENCODER, new ClientboundPlayerClaimsDimensionPacket.Decoder(), null, new ClientboundPlayerClaimsDimensionPacket.ClientHandler());
		
		packetHandler.register(12, ClientboundClaimStatesPacket.class, ClientboundClaimStatesPacket.ENCODER, new ClientboundClaimStatesPacket.Decoder(), null, new ClientboundClaimStatesPacket.ClientHandler());
		
		packetHandler.register(13, ClientboundClaimsRegionPacket.class, ClientboundClaimsRegionPacket.ENCODER, new ClientboundClaimsRegionPacket.Decoder(), null, new ClientboundClaimsRegionPacket.ClientHandler());
		
		packetHandler.register(14, ClientboundClaimsClaimUpdatePacket.class, ClientboundClaimsClaimUpdatePacket.ENCODER, new ClientboundClaimsClaimUpdatePacket.Decoder(), null, new ClientboundClaimsClaimUpdatePacket.ClientHandler());
		
		packetHandler.register(15, ClientboundClaimPropertiesPacket.class, ClientboundClaimPropertiesPacket.ENCODER, new ClientboundClaimPropertiesPacket.Decoder(), null, new ClientboundClaimPropertiesPacket.ClientHandler());
		
		packetHandler.register(17, ClientboundClaimLimitsPacket.class, ClientboundClaimLimitsPacket.ENCODER, new ClientboundClaimLimitsPacket.Decoder(), null, new ClientboundClaimLimitsPacket.ClientHandler());
		

		LazyPacketsConfirmationPacket.Codec lazyPacketsConfirmCodec = new LazyPacketsConfirmationPacket.Codec();
		packetHandler.register(18, LazyPacketsConfirmationPacket.class, lazyPacketsConfirmCodec, lazyPacketsConfirmCodec, new LazyPacketsConfirmationPacket.ServerHandler(), new LazyPacketsConfirmationPacket.ClientHandler());
		
		packetHandler.register(19, ClaimRegionsStartPacket.class, ClaimRegionsStartPacket.ENCODER, new ClaimRegionsStartPacket.Decoder(), new ClaimRegionsStartPacket.ServerHandler(), new ClaimRegionsStartPacket.ClientHandler());
		
		ClientboundClaimResultPacket.Codec claimResultPacketCodec = new ClientboundClaimResultPacket.Codec();
		packetHandler.register(20, ClientboundClaimResultPacket.class, claimResultPacketCodec, claimResultPacketCodec, null, new ClientboundClaimResultPacket.ClientHandler());
		
		ServerboundClaimActionRequestPacket.Codec claimActionRequestPacketCodec = new ServerboundClaimActionRequestPacket.Codec();
		packetHandler.register(21, ServerboundClaimActionRequestPacket.class, claimActionRequestPacketCodec, claimActionRequestPacketCodec, new ServerboundClaimActionRequestPacket.ServerHandler(), null);

		ClientboundModesPacket.Codec modesCodec = new ClientboundModesPacket.Codec();
		packetHandler.register(22, ClientboundModesPacket.class, modesCodec, modesCodec, null, new ClientboundModesPacket.ClientHandler());

	}

}
