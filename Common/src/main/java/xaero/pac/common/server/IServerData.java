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

package xaero.pac.common.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.expiration.task.ObjectExpirationCheckSpreadoutTask;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.info.io.ServerInfoHolderIO;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerLiveSaver;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.PartyPlayerInfoUpdater;
import xaero.pac.common.server.parties.party.PlayerLogInPartyAssigner;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.player.*;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;

public interface IServerData
<
	CM extends IServerClaimsManager<?, ?, ?>,
	P extends IServerParty<?, ?, ?>
>
extends IServerDataAPI<CM,P> {
	
	//internal API

	@Override
	public IPartyManager<P> getPartyManager();
	@Override
	public CM getServerClaimsManager();
	@Override
	public IPlayerConfigManager getPlayerConfigs();
	@Override
	public AdaptiveLocalizer getAdaptiveLocalizer();
	@Override
	public ChunkProtection<CM,?,?,P> getChunkProtection();

	public PlayerWorldJoinHandler getPlayerWorldJoinHandler();
	public PlayerLoginHandler getPlayerLoginHandler();
	public PlayerLogoutHandler getPlayerLogoutHandler();
	public PlayerPermissionChangeHandler getPlayerPermissionChangeHandler();
	public ForceLoadTicketManager getForceLoadManager();
	public ServerTickHandler getServerTickHandler();
	public PlayerTickHandler getPlayerTickHandler();
	public IOThreadWorker getIoThreadWorker();
	public PartyExpirationHandler getPartyExpirationHandler();
	public PartyManagerIO<?> getPartyManagerIO();
	public PlayerConfigIO<P, CM> getPlayerConfigsIO();
	public ObjectManagerLiveSaver getPartyLiveSaver();
	public ObjectManagerLiveSaver getPlayerConfigLiveSaver();
	public ObjectManagerLiveSaver getPlayerClaimInfoLiveSaver();
	public MinecraftServer getServer();
	public PlayerLogInPartyAssigner getPlayerPartyAssigner();
	public PartyPlayerInfoUpdater getPartyMemberInfoUpdater();
	public ServerStartingCallback getServerLoadCallback();
	public ServerInfo getServerInfo();
	public ServerInfoHolderIO getServerInfoIO();
	public ServerPlayerClaimsExpirationHandler getServerPlayerClaimsExpirationHandler();
	public ServerSpreadoutQueuedTaskHandler<ObjectExpirationCheckSpreadoutTask<?>> getObjectExpirationCheckTaskHandler();
	public void onStop();
	public void onServerResourcesReload(ResourceManager resourceManager);

}
