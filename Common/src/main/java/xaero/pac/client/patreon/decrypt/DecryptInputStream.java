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

package xaero.pac.client.patreon.decrypt;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;

public class DecryptInputStream extends InputStream {
	
	private InputStream src;
	private Cipher cipher;
	private byte[] encryptedBuffer = new byte[256];
	private byte[] currentBlock;
	private int blockCount;
	private int blockOffset;
	private boolean endReached;
	private long prevExpirationTime = -1;
	
	public DecryptInputStream(InputStream src, Cipher cipher) {
		this.src = src;
		this.cipher = cipher;
	}

	@Override
	public int read() throws IOException {
		if(endReached)
			return -1;
		//IOException are sometimes suppressed, so using RuntimeException
		if(currentBlock == null || currentBlock.length == blockOffset) {
			int offset = 0;
			while(offset < 256) {
				int read = src.read(encryptedBuffer, offset, 256 - offset);
				if(read == -1){
					endReached = true;
					if(offset == 0)
						throw new RuntimeException("Online mod data missing confirmation block!");
					else
						throw new RuntimeException("Encrypted block too short!");
				}
				offset += read;
			}
			try {
				currentBlock = cipher.doFinal(encryptedBuffer);

				long expirationTime = 0;
				int blockIndex = 0;//2 bytes int
				for(blockOffset = 0; blockOffset < 8; blockOffset++)
					expirationTime |= (long)(currentBlock[blockOffset] & 255) << (8 * blockOffset);
				for(int i = 0; i < 2; i++) {
					blockIndex |= (currentBlock[blockOffset] & 255) << (8 * i);
					blockOffset++;
				}
				if(System.currentTimeMillis() > expirationTime) {
					endReached = true;
					throw new RuntimeException("Online mod data expired! Date: " + new Date(expirationTime));
				}
				if(prevExpirationTime != -1 && expirationTime != prevExpirationTime) {
					endReached = true;
					throw new RuntimeException("Online mod data expiration date mismatch! Dates: " + new Date(expirationTime) + " VS " + new Date(prevExpirationTime));
				}
				if(blockIndex != blockCount) {
					endReached = true;
					throw new RuntimeException("Online mod data block index mismatch! " + blockIndex + " VS " + blockCount);
				}
				prevExpirationTime = expirationTime;
				blockCount++;
				if(blockOffset == currentBlock.length) {//last block
					endReached = true;
					return -1;
				}
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return currentBlock[blockOffset++];
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		src.close();
		encryptedBuffer = null;
		currentBlock = null;
	}

}
