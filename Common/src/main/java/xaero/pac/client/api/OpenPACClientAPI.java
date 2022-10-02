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

package xaero.pac.client.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.IClientDataAPI;
import xaero.pac.client.claims.api.IClientClaimsManagerAPI;
import xaero.pac.client.claims.api.IClientDimensionClaimsManagerAPI;
import xaero.pac.client.claims.api.IClientRegionClaimsAPI;
import xaero.pac.client.claims.player.api.IClientPlayerClaimInfoAPI;
import xaero.pac.client.controls.api.OPACKeyBindingsAPI;
import xaero.pac.client.parties.party.api.IClientPartyAPI;
import xaero.pac.client.parties.party.api.IClientPartyMemberDynamicInfoSyncableStorageAPI;
import xaero.pac.client.parties.party.api.IClientPartyStorageAPI;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageManagerAPI;
import xaero.pac.client.player.config.api.IPlayerConfigStringableOptionClientStorageAPI;
import xaero.pac.client.world.api.IClientWorldDataAPI;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;
import xaero.pac.common.capability.api.ICapabilityHelperAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.claims.player.api.IPlayerDimensionClaimsAPI;
import xaero.pac.common.parties.party.ally.api.IPartyAllyAPI;
import xaero.pac.common.parties.party.api.IPartyMemberDynamicInfoSyncableAPI;
import xaero.pac.common.parties.party.api.IPartyPlayerInfoAPI;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is the main client API access point. You can get the instance with {@link #get()}.
 * <p>
 * Make sure to check whether the server side has the mod installed with
 * <p>
 * {@code
 *   ClientWorldMainCapabilityAPI capability = api.getCapabilityHelper().getCapability(Minecraft.getInstance().level, ClientWorldCapabilityTypes.MAIN_CAP);
 *   IClientWorldDataAPI worldData = capability.getClientWorldData();
 *   boolean serverHasMod = worldData.serverHasMod();
 * }
 * <p>
 * for each client world instance ({@code Minecraft.getInstance().level}).
 */
public final class OpenPACClientAPI {

	@SuppressWarnings("unchecked")
	private IClientDataAPI
			<
			IPlayerConfigClientStorageManagerAPI<IPlayerConfigClientStorageAPI<IPlayerConfigStringableOptionClientStorageAPI<?>>>,
			IClientPartyStorageAPI<IClientPartyAPI<IPartyMemberAPI, IPartyPlayerInfoAPI, IPartyAllyAPI>, IClientPartyMemberDynamicInfoSyncableStorageAPI<IPartyMemberDynamicInfoSyncableAPI>>,
			IClientClaimsManagerAPI<IClientPlayerClaimInfoAPI<IPlayerDimensionClaimsAPI<IPlayerClaimPosListAPI>>, IClientDimensionClaimsManagerAPI<IClientRegionClaimsAPI>>
		> getClientData() {
		return (IClientDataAPI<
				IPlayerConfigClientStorageManagerAPI<IPlayerConfigClientStorageAPI<IPlayerConfigStringableOptionClientStorageAPI<?>>>,
				IClientPartyStorageAPI<IClientPartyAPI<IPartyMemberAPI, IPartyPlayerInfoAPI, IPartyAllyAPI>, IClientPartyMemberDynamicInfoSyncableStorageAPI<IPartyMemberDynamicInfoSyncableAPI>>,
				IClientClaimsManagerAPI<IClientPlayerClaimInfoAPI<IPlayerDimensionClaimsAPI<IPlayerClaimPosListAPI>>, IClientDimensionClaimsManagerAPI<IClientRegionClaimsAPI>>>
		)
				(Object) OpenPartiesAndClaims.INSTANCE.getClientDataInternal();
	}

	/**
	 * Gets the API for the client-side config data updated by the server.
	 *
	 * @return instance of the client-side player config API, not null
	 */
	@Nonnull
	public IPlayerConfigClientStorageManagerAPI<IPlayerConfigClientStorageAPI<IPlayerConfigStringableOptionClientStorageAPI<?>>>
	getPlayerConfigClientStorageManager(){
		return getClientData().getPlayerConfigStorageManager();
	}

	/**
	 * Gets the API for the client-side party data updated by the server.
	 *
	 * @return instance of the client-side party API, not null
	 */
	@Nonnull
	public IClientPartyStorageAPI<IClientPartyAPI<IPartyMemberAPI, IPartyPlayerInfoAPI, IPartyAllyAPI>, IClientPartyMemberDynamicInfoSyncableStorageAPI<IPartyMemberDynamicInfoSyncableAPI>>
	getClientPartyStorage(){
		return getClientData().getClientPartyStorage();
	}

	/**
	 * Gets the API for the client-side claims manager updated by the server.
	 *
	 * @return instance of the client-side claims manager API, not null
	 */
	@Nonnull
	public IClientClaimsManagerAPI<IClientPlayerClaimInfoAPI<IPlayerDimensionClaimsAPI<IPlayerClaimPosListAPI>>, IClientDimensionClaimsManagerAPI<IClientRegionClaimsAPI>>
	getClaimsManager(){
		return getClientData().getClaimsManager();
	}

	/**
	 * Gets the API for the mod's keybindings.
	 *
	 * @return instance of the mod's keybindings API, not null
	 */
	@Nonnull
	public OPACKeyBindingsAPI getKeyBindings() {
		return getClientData().getKeyBindings();
	}

	/**
	 * Opens the main menu screen of the Parties and Claims mod.
	 *
	 * @param escape  the screen to switch to when the escape key is hit, can be null
	 * @param parent  the screen to switch to when the screen is exited normally, can be null
	 */
	public void openMainMenuScreen(@Nullable Screen escape, @Nullable Screen parent) {
		getClientData().openMainMenuScreen(escape, parent);
	}

	/**
	 * Gets the capability API for accessing the data attached to some objects, e.g. game worlds.
	 * <p>
	 * For example, {@code MAIN_CAP} in {@link ClientWorldCapabilityTypes} is attached to all {@link ClientLevel}
	 * instances and lets you access the {@link IClientWorldDataAPI} of a client world instance.
	 * <p>
	 * Uses Forge capabilities when possible.
	 *
	 * @return instance of the capability API, not null
	 */
	@Nonnull
	public ICapabilityHelperAPI getCapabilityHelper(){
		return OpenPartiesAndClaims.INSTANCE.getCapabilityHelper();
	}

	/**
	 * Gets the client-side Open Parties and Claims API instance.
	 *
	 * @return instance of the client-side API, not null
	 */
	@Nonnull
	public static OpenPACClientAPI get() {
		return OpenPartiesAndClaims.INSTANCE.getClientAPI();
	}

}
