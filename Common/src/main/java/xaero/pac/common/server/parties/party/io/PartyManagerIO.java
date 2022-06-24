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

package xaero.pac.common.server.parties.party.io;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.ObjectManagerIOObject;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

public final class PartyManagerIO<S> extends ObjectManagerIO<S, String, ServerParty, PartyManager, PartyManagerIO<S>> {
	
	private PartyManagerIO(String extension, SerializationHandler<S, String, ServerParty, PartyManager> serializationHandler, SerializedDataFileIO<S, String> serializedDataFileIO, IOThreadWorker ioThreadWorker,
			MinecraftServer server, PartyManager manager, FileIOHelper fileIOHelper) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, extension, manager, fileIOHelper);
	}

	@Override
	protected Path getObjectFolderPath() {
		return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("openpartiesandclaims").resolve("parties");
	}
	
	@Override
	public void load() {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		OpenPartiesAndClaims.LOGGER.info("Loading parties...");
		super.load();
		manager.setLoaded(true);
		OpenPartiesAndClaims.LOGGER.info("Loaded parties!");
	}

	@Override
	public boolean save() {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return true;
		OpenPartiesAndClaims.LOGGER.info("Saving parties...");
		return super.save();
	}

	@Override
	public void delete(ObjectManagerIOObject object) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		super.delete(object);
	}

	@Override
	public void onServerTick() {
		super.onServerTick();
	}

	@Override
	protected String getObjectId(String fileNameNoExtension, Path file) {
		return fileNameNoExtension;
	}

	@Override
	protected void onObjectLoad(ServerParty loadedObject) {
		Iterator<UUID> allyPartyIterator = loadedObject.getAllyPartiesIteratorModifiable();
		while(allyPartyIterator.hasNext()) {
			ServerParty allyParty = manager.getPartyById(allyPartyIterator.next());
			if(allyParty == null) {
				allyPartyIterator.remove();
				loadedObject.setDirty(true);
			}
		}
		manager.addParty(loadedObject);
	}
	
	public static final class Builder<S> extends ObjectManagerIO.Builder<S, String, ServerParty, PartyManager, PartyManagerIO<S>>{

		private Builder() {
		}
		
		@Override
		public Builder<S> setDefault() {
			super.setDefault();
			return this;
		}
		
		@Override
		protected PartyManagerIO<S> buildInternally() {
			return new PartyManagerIO<>(fileExtension, serializationHandler, serializedDataFileIO, ioThreadWorker, server, manager, fileIOHelper);
		}

		public static <S> Builder<S> begin() {
			return new Builder<S>().setDefault();
		}

	}

}
