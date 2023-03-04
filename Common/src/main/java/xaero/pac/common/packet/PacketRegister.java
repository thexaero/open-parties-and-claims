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

package xaero.pac.common.packet;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.LoadCommon;
import xaero.pac.common.packet.claims.*;
import xaero.pac.common.packet.config.*;
import xaero.pac.common.packet.parties.ClientboundPartyAllyPacket;
import xaero.pac.common.packet.parties.ClientboundPartyNamePacket;
import xaero.pac.common.packet.parties.ClientboundPartyPacket;
import xaero.pac.common.packet.parties.ClientboundPartyPlayerPacket;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;

public class PacketRegister {
	
	public void register(LoadCommon context) {
		IPacketHandler packetHandler = OpenPartiesAndClaims.INSTANCE.getPacketHandler();

		ServerLoginHandshakePacket.Codec serverHandshakeCodec = new ServerLoginHandshakePacket.Codec();
		packetHandler.register(0, ServerLoginHandshakePacket.class, serverHandshakeCodec, serverHandshakeCodec, new ServerLoginHandshakePacket.ServerHandler(), new ServerLoginHandshakePacket.ClientHandler());

		ClientboundPacDimensionHandshakePacket.Codec handshakeCodec = new ClientboundPacDimensionHandshakePacket.Codec();
		packetHandler.register(1, ClientboundPacDimensionHandshakePacket.class, handshakeCodec, handshakeCodec, null, new ClientboundPacDimensionHandshakePacket.ClientHandler());
		
		ClientboundPlayerConfigOptionValuePacket.Codec clientPlayerConfigOptionValueCodec = new ClientboundPlayerConfigOptionValuePacket.Codec();
		packetHandler.register(2, ClientboundPlayerConfigOptionValuePacket.class, clientPlayerConfigOptionValueCodec, clientPlayerConfigOptionValueCodec, null, new ClientboundPlayerConfigOptionValuePacket.ClientHandler());
		
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
		
		packetHandler.register(15, ClientboundSubClaimPropertiesPacket.class, ClientboundSubClaimPropertiesPacket.ENCODER, new ClientboundSubClaimPropertiesPacket.Decoder(), null, new ClientboundSubClaimPropertiesPacket.ClientHandler());
		
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

		ClientboundPlayerConfigSyncStatePacket.Codec playerConfigSyncCodec = new ClientboundPlayerConfigSyncStatePacket.Codec();
		packetHandler.register(23, ClientboundPlayerConfigSyncStatePacket.class, playerConfigSyncCodec, playerConfigSyncCodec, null, new ClientboundPlayerConfigSyncStatePacket.ClientHandler());

		ClientboundPlayerConfigRemoveSubPacket.Codec playerConfigSubCodec = new ClientboundPlayerConfigRemoveSubPacket.Codec();
		packetHandler.register(24, ClientboundPlayerConfigRemoveSubPacket.class, playerConfigSubCodec, playerConfigSubCodec, null, new ClientboundPlayerConfigRemoveSubPacket.ClientHandler());

		ServerboundSubConfigExistencePacket.Codec createSubConfigCodec = new ServerboundSubConfigExistencePacket.Codec();
		packetHandler.register(25, ServerboundSubConfigExistencePacket.class, createSubConfigCodec, createSubConfigCodec, new ServerboundSubConfigExistencePacket.ServerHandler(), null);

		packetHandler.register(26, ClientboundClaimOwnerPropertiesPacket.class, ClientboundClaimOwnerPropertiesPacket.ENCODER, new ClientboundClaimOwnerPropertiesPacket.Decoder(), null, new ClientboundClaimOwnerPropertiesPacket.ClientHandler());

		packetHandler.register(27, ClientboundRemoveClaimStatePacket.class, ClientboundRemoveClaimStatePacket.ENCODER, new ClientboundRemoveClaimStatePacket.Decoder(), null, new ClientboundRemoveClaimStatePacket.ClientHandler());

		packetHandler.register(28, ClientboundRemoveSubClaimPacket.class, ClientboundRemoveSubClaimPacket.ENCODER, new ClientboundRemoveSubClaimPacket.Decoder(), null, new ClientboundRemoveSubClaimPacket.ClientHandler());

		packetHandler.register(29, ClientboundClaimsClaimUpdatePosPacket.class, ClientboundClaimsClaimUpdatePosPacket.ENCODER, new ClientboundClaimsClaimUpdatePosPacket.Decoder(), null, new ClientboundClaimsClaimUpdatePosPacket.ClientHandler());

		ClientboundPlayerConfigGeneralStatePacket.Codec playerConfigGeneralStateCodec = new ClientboundPlayerConfigGeneralStatePacket.Codec();
		packetHandler.register(30, ClientboundPlayerConfigGeneralStatePacket.class, playerConfigGeneralStateCodec, playerConfigGeneralStateCodec, null, new ClientboundPlayerConfigGeneralStatePacket.ClientHandler());

		packetHandler.register(31, ClientboundCurrentSubClaimPacket.class, ClientboundCurrentSubClaimPacket.ENCODER, new ClientboundCurrentSubClaimPacket.Decoder(), null, new ClientboundCurrentSubClaimPacket.ClientHandler());

		ServerboundPlayerConfigOptionValuePacket.Codec serverPlayerConfigOptionValueCodec = new ServerboundPlayerConfigOptionValuePacket.Codec();
		packetHandler.register(32, ServerboundPlayerConfigOptionValuePacket.class, serverPlayerConfigOptionValueCodec, serverPlayerConfigOptionValueCodec, new ServerboundPlayerConfigOptionValuePacket.ServerHandler(), null);

		ClientboundPlayerConfigDynamicOptionsPacket.Codec playerConfigDynamicOptionsCodec = new ClientboundPlayerConfigDynamicOptionsPacket.Codec();
		packetHandler.register(33, ClientboundPlayerConfigDynamicOptionsPacket.class, playerConfigDynamicOptionsCodec, playerConfigDynamicOptionsCodec, null, new ClientboundPlayerConfigDynamicOptionsPacket.ClientHandler());

		ClientboundPlayerConfigHelpPacket.Codec playerConfigHelpCodec = new ClientboundPlayerConfigHelpPacket.Codec();
		packetHandler.register(34, ClientboundPlayerConfigHelpPacket.class, playerConfigHelpCodec, playerConfigHelpCodec, null, new ClientboundPlayerConfigHelpPacket.ClientHandler());

	}

}
