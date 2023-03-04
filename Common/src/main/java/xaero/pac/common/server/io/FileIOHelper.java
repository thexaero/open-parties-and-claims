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

package xaero.pac.common.server.io;

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

}
