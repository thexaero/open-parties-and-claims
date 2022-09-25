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

package xaero.pac.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.client.claims.sync.ClientClaimsSyncHandler;
import xaero.pac.client.controls.XPACKeyBindings;
import xaero.pac.client.gui.MainMenu;
import xaero.pac.client.parties.party.ClientPartyStorage;
import xaero.pac.client.player.config.PlayerConfigClientStorageManager;
import xaero.pac.client.player.config.PlayerConfigClientSynchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClientData implements IClientData<PlayerConfigClientStorageManager, ClientPartyStorage, ClientClaimsManager> {
	
	private final ClientTickHandler clientTickHandler;
	private final ClientWorldLoadHandler clientWorldLoadHandler;
	private final PlayerConfigClientSynchronizer playerConfigClientSynchronizer;
	private final XPACKeyBindings keyBindings;
	private final PlayerConfigClientStorageManager playerConfigStorageManager;
	private final ClientPartyStorage clientPartyStorage;
	private final ClientClaimsManager claimsManager;
	private final ClientClaimsSyncHandler clientClaimsSyncHandler;

	public ClientData(ClientTickHandler clientTickHandler, ClientWorldLoadHandler clientWorldLoadHandler,
			PlayerConfigClientSynchronizer playerConfigClientSynchronizer, XPACKeyBindings keyBindings,
			PlayerConfigClientStorageManager playerConfigStorageManager, ClientPartyStorage clientPartyStorage,
			ClientClaimsManager claimsManager, ClientClaimsSyncHandler clientClaimsSyncHandler) {
		super();
		this.clientTickHandler = clientTickHandler;
		this.clientWorldLoadHandler = clientWorldLoadHandler;
		this.playerConfigClientSynchronizer = playerConfigClientSynchronizer;
		this.keyBindings = keyBindings;
		this.playerConfigStorageManager = playerConfigStorageManager;
		this.clientPartyStorage = clientPartyStorage;
		this.claimsManager = claimsManager;
		this.clientClaimsSyncHandler = clientClaimsSyncHandler;
	}

	@Override
	public ClientTickHandler getClientTickHandler() {
		return clientTickHandler;
	}

	@Override
	public ClientWorldLoadHandler getClientWorldLoadHandler() {
		return clientWorldLoadHandler;
	}

	@Override
	public PlayerConfigClientSynchronizer getPlayerConfigClientSynchronizer() {
		return playerConfigClientSynchronizer;
	}

	@Nonnull
	@Override
	public XPACKeyBindings getKeyBindings() {
		return keyBindings;
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorageManager getPlayerConfigStorageManager() {
		return playerConfigStorageManager;
	}

	@Nonnull
	@Override
	public ClientPartyStorage getClientPartyStorage() {
		return clientPartyStorage;
	}

	@Override
	public void reset() {
		clientPartyStorage.reset();
		claimsManager.reset();
		clientClaimsSyncHandler.reset();
		playerConfigStorageManager.reset();
	}

	@Override
	@Nonnull
	public ClientClaimsManager getClaimsManager() {
		return claimsManager;
	}
	
	public ClientClaimsSyncHandler getClientClaimsSyncHandler() {
		return clientClaimsSyncHandler;
	}

	public final static class Builder {

		private ClientTickHandler clientTickHandler;
		private ClientWorldLoadHandler clientWorldLoadHandler;
		private PlayerConfigClientSynchronizer playerConfigClientSynchronizer;
		private XPACKeyBindings keyBindings;
		private PlayerConfigClientStorageManager playerConfigStorageManager;
		private ClientPartyStorage clientPartyStorage;
		private ClientClaimsManager claimsManager;
		private ClientClaimsSyncHandler clientClaimsSyncHandler;

		private Builder setDefault() {
			setClientTickHandler(null);
			setPlayerConfigClientSynchronizer(null);
			setKeyBindings(null);
			setPlayerConfigStorageManager(null);
			setClientPartyStorage(null);
			setClaimsManager(null);
			return this;
		}
		
		public Builder setClientTickHandler(ClientTickHandler clientTickHandler) {
			this.clientTickHandler = clientTickHandler;
			return this;
		}
		
		public Builder setClientWorldLoadHandler(ClientWorldLoadHandler clientWorldLoadHandler) {
			this.clientWorldLoadHandler = clientWorldLoadHandler;
			return this;
		}
		
		public Builder setPlayerConfigClientSynchronizer(PlayerConfigClientSynchronizer playerConfigClientSynchronizer) {
			this.playerConfigClientSynchronizer = playerConfigClientSynchronizer;
			return this;
		}
		
		public Builder setKeyBindings(XPACKeyBindings keyBindings) {
			this.keyBindings = keyBindings;
			return this;
		}
		
		public Builder setPlayerConfigStorageManager(PlayerConfigClientStorageManager playerConfigStorageManager) {
			this.playerConfigStorageManager = playerConfigStorageManager;
			return this;
		}
		
		public Builder setClientPartyStorage(ClientPartyStorage clientPartyStorage) {
			this.clientPartyStorage = clientPartyStorage;
			return this;
		}
		
		public Builder setClaimsManager(ClientClaimsManager claimsManager) {
			this.claimsManager = claimsManager;
			return this;
		}
		
		public Builder setClientClaimsSyncHandler(ClientClaimsSyncHandler clientClaimsSyncHandler) {
			this.clientClaimsSyncHandler = clientClaimsSyncHandler;
			return this;
		}
		
		public ClientData build() {
			if(clientTickHandler == null || keyBindings == null || clientWorldLoadHandler == null || 
					playerConfigClientSynchronizer == null || playerConfigStorageManager == null || clientPartyStorage == null || claimsManager == null || clientClaimsSyncHandler == null)
				throw new IllegalStateException();
			return new ClientData(clientTickHandler, clientWorldLoadHandler, playerConfigClientSynchronizer, keyBindings, playerConfigStorageManager, clientPartyStorage, claimsManager, clientClaimsSyncHandler);
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

	@Override
	public void openMainMenuScreen(@Nullable Screen escape, @Nullable Screen parent) {
		Minecraft.getInstance().setScreen(new MainMenu(escape, parent));
	}
	
}
