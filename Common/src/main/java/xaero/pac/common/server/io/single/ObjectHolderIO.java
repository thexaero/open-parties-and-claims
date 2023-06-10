/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.io.single;

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.server.io.*;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;

import java.nio.file.Path;
import java.util.stream.Stream;

public class ObjectHolderIO
<
	S,
	T extends ObjectManagerIOObject,
	M extends ObjectHolderIOHolder<T, M>
> extends ObjectManagerIO<S, Object, T, M> {

	private final Path filePath;

	protected ObjectHolderIO(Path filePath, SerializationHandler<S, Object, T, M> serializationHandler, SerializedDataFileIO<S, Object> serializedDataFileIO, IOThreadWorker ioThreadWorker, MinecraftServer server, String fileExtension, M manager, FileIOHelper fileIOHelper) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
		this.filePath = filePath;
	}

	@Override
	public void load() {
		T object = loadFile(filePath, null, true);
		if(object != null)
			manager.setObject(object);
	}

	@Override
	protected Stream<FilePathConfig> getObjectFolderPaths() {
		throw new IllegalArgumentException("this shouldn't be used!");
	}

	@Override
	protected void onObjectLoad(T loadedObject) {
		throw new IllegalArgumentException("this shouldn't be used!");
	}

	@Override
	protected Object getObjectId(String fileNameNoExtension, Path file, FilePathConfig filePathConfig) {
		return null;
	}

	@Override
	protected Path getFilePath(T object, String fileName) {
		if(fileName != null)
			throw new IllegalArgumentException("fileName isn't null but that's pointless!");
		return filePath;
	}

	public static abstract class Builder
	<
		S,
		T extends ObjectManagerIOObject,
		M extends ObjectHolderIOHolder<T, M>,
		B extends Builder<S,T,M,B>
	> extends ObjectManagerIO.Builder<S, Object, T, M, B> {

		protected Path filePath;

		@Override
		public B setDefault() {
			setFilePath(null);
			return super.setDefault();
		}

		public B setFilePath(Path filePath) {
			this.filePath = filePath;
			if(filePath == null)
				setFileExtension(null);
			else {
				String fileName = filePath.getFileName().toString();
				setFileExtension(fileName.substring(fileName.lastIndexOf(".")));
			}
			return self;
		}

		@Override
		public ObjectHolderIO<S,T,M> build() {
			if(filePath == null)
				throw new IllegalStateException();
			if(!filePath.getFileName().toString().endsWith(fileExtension))
				throw new IllegalStateException();
			return (ObjectHolderIO<S, T, M>) super.build();
		}

	}

	public static final class FinalBuilder
	<
		S,
		T extends ObjectManagerIOObject,
		M extends ObjectHolderIOHolder<T, M>
	> extends Builder<S, T, M, FinalBuilder<S,T,M>> {

		@Override
		protected ObjectHolderIO<S,T,M> buildInternally() {
			return new ObjectHolderIO<>(filePath, serializationHandler, serializedDataFileIO, ioThreadWorker, server, fileExtension, manager, fileIOHelper);
		}

	}

}
