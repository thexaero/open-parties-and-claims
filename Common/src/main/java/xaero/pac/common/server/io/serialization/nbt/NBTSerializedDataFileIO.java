/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.io.serialization.nbt;

import net.minecraft.nbt.NbtIo;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;

import java.io.*;

public class NBTSerializedDataFileIO
<
	S,
	I
>
implements SerializedDataFileIO<S, I> {
	
	private final NBTConverter<S, I> converter;
	
	public NBTSerializedDataFileIO(NBTConverter<S, I> converter){
		this.converter = converter;
	}

	@Override
	public S read(I id, BufferedInputStream fileInput) throws IOException {
		try(BufferedInputStream bufferedStream = new BufferedInputStream(fileInput); DataInputStream dataInput = new DataInputStream(bufferedStream)){
			return converter.convert(id, NbtIo.read(dataInput));
		}
	}

	@Override
	public void write(BufferedOutputStream fileOutput, S serializedData) throws IOException {
		try(BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput); DataOutputStream dataOutput = new DataOutputStream(bufferedOutput)){
			NbtIo.write(converter.convert(serializedData), dataOutput);
		}
	}

}
