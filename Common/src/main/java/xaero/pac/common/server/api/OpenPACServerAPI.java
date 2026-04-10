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

package xaero.pac.common.server.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.IOpenPACMinecraftServer;
import xaero.pac.common.server.IServerDataAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;
import xaero.pac.common.server.claims.protection.api.IChunkProtectionAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigManagerAPI;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.api.IAdaptiveLocalizerAPI;

import javax.annotation.Nonnull;

/**
 * This is the main server-side API access point. You can get the instance with {@link #get(MinecraftServer)}.
 * <p>
 * For functionality that requires registering handlers/listeners, it is often required or recommended to
 * do so during the OPACServerAddonRegister.EVENT on Fabric or the OPACServerAddonRegisterEvent on Forge.
 * <p>
 * Additionally, to access some data attached to online server players,
 * use {@link ServerPlayerDataAPI#from(ServerPlayer)}. You probably won't need that though.
 */
public class OpenPACServerAPI {
	
	private final IServerDataAPI serverData;

	/**
	 * Constructor for internal usage.
	 *
	 * @param serverData  the server data
	 */
	@SuppressWarnings("unchecked")
	public OpenPACServerAPI(IServerDataAPI serverData) {
		super();
		this.serverData = serverData;
	}

	/**
	 * Gets the API for the server-side player party manager.
	 *
	 * @return instance of the server-side player party manager API, not null
	 */
	@Nonnull
	public IPartyManagerAPI getPartyManager(){
		return serverData.getPartyManager();
	}

	/**
	 * Gets the API for the server-side claims manager.
	 *
	 * @return instance of the server-side claims manager API, not null
	 */
	@Nonnull
	public IServerClaimsManagerAPI getServerClaimsManager(){
		return serverData.getServerClaimsManager();
	}

	/**
	 * Gets the API for the server-side player config manager.
	 *
	 * @return instance of the server-side player config manager API, not null
	 */
	@Nonnull
	public IPlayerConfigManagerAPI getPlayerConfigManager() {
		return serverData.getPlayerConfigManager();
	}

	/**
	 * @deprecated use {@link #getPlayerConfigManager()} instead!
	 */
	@Deprecated
	@Nonnull
	public xaero.pac.common.server.player.config.api.v1.IPlayerConfigManagerAPI getPlayerConfigs() {
		return serverData.getPlayerConfigs();
	}

	/**
	 * Gets the API for the server-side adaptive text localizer that lets you create translated text components
	 * that are readable even by players that don't have the mod installed.
	 *
	 * @return the adaptive text localizer API, not null
	 */
	public IAdaptiveLocalizerAPI getAdaptiveTextLocalizer(){
		return serverData.getAdaptiveLocalizer();
	}

	/**
	 * Gets the API for the chunk protection with some basic methods you can use.
	 *
	 * @return the chunk protection API
	 */
	public IChunkProtectionAPI getChunkProtection(){
		return serverData.getChunkProtection();
	}

	/**
	 * Gets the server-side Open Parties and Claims API instance.
	 *
	 * @param server  the server instance, not null
	 * @return instance of the server-side API, not null
	 */
	@Nonnull
	public static OpenPACServerAPI get(@Nonnull MinecraftServer server) {
		return ((IOpenPACMinecraftServer)server).getXaero_OPAC_ServerData().getAPI();
	}
	
}
