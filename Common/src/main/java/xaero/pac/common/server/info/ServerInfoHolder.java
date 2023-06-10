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

package xaero.pac.common.server.info;

import xaero.pac.common.server.io.single.ObjectHolderIOHolder;

public final class ServerInfoHolder extends ObjectHolderIOHolder<ServerInfo, ServerInfoHolder> {
	
	private ServerInfo serverInfo;

	@Override
	public ServerInfo getObject() {
		return serverInfo;
	}

	@Override
	public void setObject(ServerInfo serverInfo) {
		if(this.serverInfo != null)
			throw new IllegalStateException();
		this.serverInfo = serverInfo;
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

}
