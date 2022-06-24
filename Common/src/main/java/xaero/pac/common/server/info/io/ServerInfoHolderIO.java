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

package xaero.pac.common.server.info.io;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.info.ServerInfoHolder;
import xaero.pac.common.server.info.io.serialization.nbt.ServerInfoSerializationHandler;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;
import xaero.pac.common.server.io.serialization.nbt.SimpleNBTSerializedDataFileIO;

import java.nio.file.Path;

public final class ServerInfoHolderIO extends ObjectManagerIO<CompoundTag, String, ServerInfo, ServerInfoHolder, ServerInfoHolderIO>{
	
	private final Path folderPath;

	private ServerInfoHolderIO(
			SerializationHandler<CompoundTag, String, ServerInfo, ServerInfoHolder> serializationHandler,
			SerializedDataFileIO<CompoundTag, String> serializedDataFileIO, IOThreadWorker ioThreadWorker,
			MinecraftServer server, String fileExtension, ServerInfoHolder manager, FileIOHelper fileIOHelper) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
		folderPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("openpartiesandclaims");
	}

	@Override
	public void load() {
		OpenPartiesAndClaims.LOGGER.info("Loading server info!");
		super.load();
	}

	@Override
	public boolean save() {
		OpenPartiesAndClaims.LOGGER.info("Saving server info!");
		return super.save();
	}

	@Override
	protected Path getObjectFolderPath() {
		return folderPath;
	}

	@Override
	protected void onObjectLoad(ServerInfo loadedObject) {
		manager.setServerInfo(loadedObject);
	}

	@Override
	protected String getObjectId(String fileNameNoExtension, Path file) {
		return fileNameNoExtension;
	}
	
	public static final class Builder extends ObjectManagerIO.Builder<CompoundTag, String, ServerInfo, ServerInfoHolder, ServerInfoHolderIO>{

		@Override
		public Builder setDefault() {
			super.setDefault();
			setFileExtension(".nbt");
			return this;
		}
		
		public ServerInfoHolderIO build() {
			if(serializationHandler == null)
				setSerializationHandler(new ServerInfoSerializationHandler());
			if(serializedDataFileIO == null)
				setSerializedDataFileIO(new SimpleNBTSerializedDataFileIO<>());
			return super.build();
		}

		@Override
		protected ServerInfoHolderIO buildInternally() {
			return new ServerInfoHolderIO(serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
