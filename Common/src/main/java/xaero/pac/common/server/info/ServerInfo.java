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

package xaero.pac.common.server.info;

import xaero.pac.common.server.io.ObjectManagerIOObject;

public final class ServerInfo implements ObjectManagerIOObject {

	public static final int CURRENT_VERSION = 5;
	private long totalUseTime;
	private boolean dirty;
	private final int loadedVersion;
	private int targetPlayerConfigVersion;

	public ServerInfo(long totalUseTime, int loadedVersion, int loadedTargetPlayerConfigVersion) {
		super();
		this.totalUseTime = totalUseTime;
		this.loadedVersion = loadedVersion;
		this.targetPlayerConfigVersion = loadedTargetPlayerConfigVersion;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public String getFileName() {
		return null;
	}
	
	public void setTotalUseTime(long totalUseTime) {
		this.totalUseTime = totalUseTime;
		setDirty(true);
	}
	
	public long getTotalUseTime() {
		return totalUseTime;
	}

	public int getLoadedVersion() {
		return loadedVersion;
	}

	public int getTargetPlayerConfigVersion() {
		return targetPlayerConfigVersion;
	}

	public void setTargetPlayerConfigVersion(int targetPlayerConfigVersion) {
		this.targetPlayerConfigVersion = targetPlayerConfigVersion;
		setDirty(true);
	}

}
