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

package xaero.pac.common.server.io;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIOHelper {
	
	public Path quickFileBackupMove(Path file) throws IOException {
		Path backupPath = null;
		int backupNumber = 0;
		while(Files.exists(backupPath = file.resolveSibling(file.getFileName().toString() + ".backup" + backupNumber))) {
			backupNumber++;
		}
		Files.move(file, backupPath);
		return backupPath;
	}
	
	public void safeMoveAndReplace(Path from, Path to, boolean backupFrom) throws IOException {
		//just using REPLACE_EXISTING seems to bug out for some people and clear "to" and not moving "from"
		Path backupPath = null;
		Path fromBackupPath = null;
		if(backupFrom) {
			while(true) {//keep trying until we succeed
				try {
					fromBackupPath = quickFileBackupMove(from);
					break;
				} catch(IOException ioe2) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}
		} else
			fromBackupPath = from;
		if(Files.exists(to))
			backupPath = quickFileBackupMove(to);
		Files.move(fromBackupPath, to);
		if(backupPath != null)
			Files.delete(backupPath);
	}

	public String replaceTrailingDots(String string, char replacement){
		StringBuilder path = new StringBuilder(string);
		int dotCount = 0;
		while(!path.isEmpty() && path.charAt(path.length() - 1) == '.') {
			path.deleteCharAt(path.length() - 1);
			dotCount++;
		}
		for (int i = 0; i < dotCount; i++)
			path.append(replacement);
		return path.toString();
	}

	public String convertDimensionToFileName(ResourceLocation dim, boolean replaceTrailingDots) {
		String path = dim.getPath().replace('/', '%');
		if(replaceTrailingDots)
			path = replaceTrailingDots(path, ',');//removes trailing dots because it's not supported on windows/other
		return dim.getNamespace() + "$" + path;
	}

	public ResourceLocation convertFileNameToDimension(String fileName, boolean restoreTrailingDots) {
		String[] idArgs = fileName.split("\\$");
		if(idArgs.length < 2)
			return null;
		String path = idArgs[1].replace('%', '/');
		if(restoreTrailingDots)
			path = path.replace(',', '.');
		try {
			return ResourceLocation.fromNamespaceAndPath(idArgs[0], path);
		} catch(ResourceLocationException rle){
			return null;
		}
	}

}
