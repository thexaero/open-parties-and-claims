/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import xaero.pac.common.packet.config.group.*;
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
		
		packetHandler.register(6, ClientboundPartyNamePacket.class, ClientboundPartyNamePacket.ENCODER, ClientboundPartyNamePacket.DECODER, null, new ClientboundPartyNamePacket.ClientHandler());

		packetHandler.register(7, ClientboundPartyAllyPacket.class, ClientboundPartyAllyPacket.ENCODER, ClientboundPartyAllyPacket.DECODER, null, new ClientboundPartyAllyPacket.ClientHandler());
		
		PartyMemberDynamicInfoSyncable.Codec partyPartyMemberOftenSyncedInfoPacketCodec = new PartyMemberDynamicInfoSyncable.Codec();
		packetHandler.register(8, PartyMemberDynamicInfoSyncable.class, partyPartyMemberOftenSyncedInfoPacketCodec, partyPartyMemberOftenSyncedInfoPacketCodec, null, new PartyMemberDynamicInfoSyncable.ClientHandler());
	
		packetHandler.register(9, ClientboundLoadingPacket.class, ClientboundLoadingPacket.ENCODER, ClientboundLoadingPacket.DECODER, null, new ClientboundLoadingPacket.ClientHandler());
		
		packetHandler.register(10, ClientboundPlayerClaimsDimensionPacket.class, ClientboundPlayerClaimsDimensionPacket.ENCODER, ClientboundPlayerClaimsDimensionPacket.DECODER, null, new ClientboundPlayerClaimsDimensionPacket.ClientHandler());
		
		packetHandler.register(12, ClientboundClaimStatesPacket.class, ClientboundClaimStatesPacket.ENCODER, ClientboundClaimStatesPacket.DECODER, null, new ClientboundClaimStatesPacket.ClientHandler());
		
		packetHandler.register(13, ClientboundClaimsRegionPacket.class, ClientboundClaimsRegionPacket.ENCODER, ClientboundClaimsRegionPacket.DECODER, null, new ClientboundClaimsRegionPacket.ClientHandler());
		
		packetHandler.register(14, ClientboundClaimsClaimUpdatePacket.class, ClientboundClaimsClaimUpdatePacket.ENCODER, ClientboundClaimsClaimUpdatePacket.DECODER, null, new ClientboundClaimsClaimUpdatePacket.ClientHandler());
		
		packetHandler.register(15, ClientboundSubClaimPropertiesPacket.class, ClientboundSubClaimPropertiesPacket.ENCODER, ClientboundSubClaimPropertiesPacket.DECODER, null, new ClientboundSubClaimPropertiesPacket.ClientHandler());
		
		packetHandler.register(17, ClientboundClaimLimitsPacket.class, ClientboundClaimLimitsPacket.ENCODER, ClientboundClaimLimitsPacket.DECODER, null, new ClientboundClaimLimitsPacket.ClientHandler());

		LazyPacketsConfirmationPacket.Codec lazyPacketsConfirmCodec = new LazyPacketsConfirmationPacket.Codec();
		packetHandler.register(18, LazyPacketsConfirmationPacket.class, lazyPacketsConfirmCodec, lazyPacketsConfirmCodec, new LazyPacketsConfirmationPacket.ServerHandler(), new LazyPacketsConfirmationPacket.ClientHandler());
		
		packetHandler.register(19, ClaimRegionsStartPacket.class, ClaimRegionsStartPacket.ENCODER, ClaimRegionsStartPacket.DECODER, new ClaimRegionsStartPacket.ServerHandler(), new ClaimRegionsStartPacket.ClientHandler());
		
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

		packetHandler.register(26, ClientboundClaimOwnerPropertiesPacket.class, ClientboundClaimOwnerPropertiesPacket.ENCODER, ClientboundClaimOwnerPropertiesPacket.DECODER, null, new ClientboundClaimOwnerPropertiesPacket.ClientHandler());

		packetHandler.register(27, ClientboundRemoveClaimStatePacket.class, ClientboundRemoveClaimStatePacket.ENCODER, ClientboundRemoveClaimStatePacket.DECODER, null, new ClientboundRemoveClaimStatePacket.ClientHandler());

		packetHandler.register(28, ClientboundRemoveSubClaimPacket.class, ClientboundRemoveSubClaimPacket.ENCODER, ClientboundRemoveSubClaimPacket.DECODER, null, new ClientboundRemoveSubClaimPacket.ClientHandler());

		packetHandler.register(29, ClientboundClaimsClaimUpdatePosPacket.class, ClientboundClaimsClaimUpdatePosPacket.ENCODER, ClientboundClaimsClaimUpdatePosPacket.DECODER, null, new ClientboundClaimsClaimUpdatePosPacket.ClientHandler());

		ClientboundPlayerConfigGeneralStatePacket.Codec playerConfigGeneralStateCodec = new ClientboundPlayerConfigGeneralStatePacket.Codec();
		packetHandler.register(30, ClientboundPlayerConfigGeneralStatePacket.class, playerConfigGeneralStateCodec, playerConfigGeneralStateCodec, null, new ClientboundPlayerConfigGeneralStatePacket.ClientHandler());

		packetHandler.register(31, ClientboundCurrentSubClaimPacket.class, ClientboundCurrentSubClaimPacket.ENCODER, ClientboundCurrentSubClaimPacket.DECODER, null, new ClientboundCurrentSubClaimPacket.ClientHandler());

		ServerboundPlayerConfigOptionValuePacket.Codec serverPlayerConfigOptionValueCodec = new ServerboundPlayerConfigOptionValuePacket.Codec();
		packetHandler.register(32, ServerboundPlayerConfigOptionValuePacket.class, serverPlayerConfigOptionValueCodec, serverPlayerConfigOptionValueCodec, new ServerboundPlayerConfigOptionValuePacket.ServerHandler(), null);

		ClientboundPlayerConfigDynamicOptionsPacket.Codec playerConfigDynamicOptionsCodec = new ClientboundPlayerConfigDynamicOptionsPacket.Codec();
		packetHandler.register(33, ClientboundPlayerConfigDynamicOptionsPacket.class, playerConfigDynamicOptionsCodec, playerConfigDynamicOptionsCodec, null, new ClientboundPlayerConfigDynamicOptionsPacket.ClientHandler());

		ClientboundPlayerConfigHelpPacket.Codec playerConfigHelpCodec = new ClientboundPlayerConfigHelpPacket.Codec();
		packetHandler.register(34, ClientboundPlayerConfigHelpPacket.class, playerConfigHelpCodec, playerConfigHelpCodec, null, new ClientboundPlayerConfigHelpPacket.ClientHandler());

		packetHandler.register(35, ClientboundClaimsClaimUpdateNextXPosPacket.class, ClientboundClaimsClaimUpdateNextXPosPacket.ENCODER, ClientboundClaimsClaimUpdateNextXPosPacket.DECODER, null, new ClientboundClaimsClaimUpdateNextXPosPacket.ClientHandler());

		packetHandler.register(36, ClientboundClaimsClaimUpdateNextZPosPacket.class, ClientboundClaimsClaimUpdateNextZPosPacket.ENCODER, ClientboundClaimsClaimUpdateNextZPosPacket.DECODER, null, new ClientboundClaimsClaimUpdateNextZPosPacket.ClientHandler());

		PlayerConfigGroupExistencePacket.Codec playerConfigGroupExistencePacketCodec = new PlayerConfigGroupExistencePacket.Codec();
		packetHandler.register(37, PlayerConfigGroupExistencePacket.class,
				playerConfigGroupExistencePacketCodec, playerConfigGroupExistencePacketCodec,
				new PlayerConfigGroupExistencePacket.ServerHandler(), new PlayerConfigGroupExistencePacket.ClientHandler()
		);

		ClientboundPlayerConfigGroupsSyncStatePacket.Codec playerConfigGroupsSyncCodec = new ClientboundPlayerConfigGroupsSyncStatePacket.Codec();
		packetHandler.register(38, ClientboundPlayerConfigGroupsSyncStatePacket.class, playerConfigGroupsSyncCodec, playerConfigGroupsSyncCodec, null, new ClientboundPlayerConfigGroupsSyncStatePacket.ClientHandler());

		ClientboundGroupActionErrorPacket.Codec groupActionErrorPacketCodec = new ClientboundGroupActionErrorPacket.Codec();
		packetHandler.register(39, ClientboundGroupActionErrorPacket.class, groupActionErrorPacketCodec, groupActionErrorPacketCodec, null, new ClientboundGroupActionErrorPacket.ClientHandler());

		PlayerConfigGroupMemberPacket.Codec playerConfigGroupMemberPacketCodec = new PlayerConfigGroupMemberPacket.Codec();
		packetHandler.register(40, PlayerConfigGroupMemberPacket.class,
				playerConfigGroupMemberPacketCodec, playerConfigGroupMemberPacketCodec,
				new PlayerConfigGroupMemberPacket.ServerHandler(), new PlayerConfigGroupMemberPacket.ClientHandler()
		);

		PlayerConfigGroupGroupPacket.Codec playerConfigGroupGroupPacketCodec = new PlayerConfigGroupGroupPacket.Codec();
		packetHandler.register(41, PlayerConfigGroupGroupPacket.class,
				playerConfigGroupGroupPacketCodec, playerConfigGroupGroupPacketCodec,
				new PlayerConfigGroupGroupPacket.ServerHandler(), new PlayerConfigGroupGroupPacket.ClientHandler()
		);

		ClientboundPlayerConfigResetGroupsPacket.Codec resetGroupsPacketCodec =
				new ClientboundPlayerConfigResetGroupsPacket.Codec();
		packetHandler.register(42, ClientboundPlayerConfigResetGroupsPacket.class,
				resetGroupsPacketCodec, resetGroupsPacketCodec,
				null, new ClientboundPlayerConfigResetGroupsPacket.ClientHandler()
		);

		PlayerConfigGroupDesyncPacket.Codec groupsDesyncCodec =
				new PlayerConfigGroupDesyncPacket.Codec();
		packetHandler.register(43, PlayerConfigGroupDesyncPacket.class,
				groupsDesyncCodec, groupsDesyncCodec,
				new PlayerConfigGroupDesyncPacket.ServerHandler(), new PlayerConfigGroupDesyncPacket.ClientHandler()
		);

		ClientboundPlayerConfigGroupLimitsPacket.Codec groupLimitsCodec =
				new ClientboundPlayerConfigGroupLimitsPacket.Codec();
		packetHandler.register(44, ClientboundPlayerConfigGroupLimitsPacket.class,
				groupLimitsCodec, groupLimitsCodec,
				null, new ClientboundPlayerConfigGroupLimitsPacket.ClientHandler()
		);

		packetHandler.register(45, ClientboundClaimPartyGeneralPacket.class,
				ClientboundClaimPartyGeneralPacket.ENCODER, ClientboundClaimPartyGeneralPacket.DECODER,
				null, new ClientboundClaimPartyGeneralPacket.ClientHandler()
		);

		ClientboundPlayerConfigPermissionsPacket.Codec playerConfigPermissionsCodec =
				new ClientboundPlayerConfigPermissionsPacket.Codec();
		packetHandler.register(46, ClientboundPlayerConfigPermissionsPacket.class,
				playerConfigPermissionsCodec, playerConfigPermissionsCodec,
				null, new ClientboundPlayerConfigPermissionsPacket.ClientHandler()
		);

		ClientboundPlayerConfigAdminPacket.Codec playerConfigAdminCodec =
				new ClientboundPlayerConfigAdminPacket.Codec();
		packetHandler.register(47, ClientboundPlayerConfigAdminPacket.class,
				playerConfigAdminCodec, playerConfigAdminCodec,
				null, new ClientboundPlayerConfigAdminPacket.ClientHandler()
		);

		packetHandler.register(48, ClientboundClaimsResetPacket.class,
				ClientboundClaimsResetPacket.ENCODER, ClientboundClaimsResetPacket.DECODER,
				null, new ClientboundClaimsResetPacket.ClientHandler()
		);

	}

}
