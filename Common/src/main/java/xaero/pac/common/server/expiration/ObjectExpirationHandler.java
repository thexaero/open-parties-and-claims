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

package xaero.pac.common.server.expiration;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.io.ObjectManagerIOManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ObjectExpirationHandler
<
	T extends ObjectManagerIOExpirableObject, 
	M extends ObjectManagerIOManager<T, M>
> {
	
	private final ServerInfo serverInfo;
	protected final M manager;
	private final long liveCheckInterval;
	private long lastCheck;
	private final String checkingMessage;
	private final int expirationTime;

	protected ObjectExpirationHandler(ServerInfo serverInfo, M manager,
			long liveCheckInterval, int expirationTime, String checkingMessage) {
		super();
		this.serverInfo = serverInfo;
		this.manager = manager;
		this.liveCheckInterval = liveCheckInterval;
		this.expirationTime = expirationTime;
		this.checkingMessage = checkingMessage;
		this.lastCheck = serverInfo.getUseTime();
	}
	
	protected abstract void preExpirationCheck(T object);
	protected abstract boolean checkExpiration(T object);
	protected abstract void expire(T object);

	public void handle() {
		Iterator<T> objects = manager.getAllStream().iterator();
		lastCheck = serverInfo.getUseTime();
		List<T> toExpire = null;
		OpenPartiesAndClaims.LOGGER.info(checkingMessage);
		while(objects.hasNext()) {
			T object = objects.next();
			preExpirationCheck(object);
			
			boolean hasBeenActive = object.hasBeenActive();//since last check
			if(!hasBeenActive)
				hasBeenActive = checkExpiration(object);
			if(hasBeenActive) {
				object.confirmActivity(serverInfo);
				object.setDirty(true);
				continue;
			} else if(serverInfo.getUseTime() > object.getLastConfirmedActivity() + expirationTime) {
				OpenPartiesAndClaims.LOGGER.info("Object expired and is being removed: " + object);
				if(toExpire == null)
					toExpire = new ArrayList<>();
				toExpire.add(object);
			}
		}
		if(toExpire != null)
			for(T object : toExpire)
				expire(object);
	}
	
	public boolean onServerTick() {
		if(serverInfo.getUseTime() > lastCheck + liveCheckInterval) {
			handle();
			return true;
		}
		return false;
	}
	
	public ServerInfo getServerInfo() {
		return serverInfo;
	}
	
	public static abstract class Builder
	<
		T extends ObjectManagerIOExpirableObject, 
		M extends ObjectManagerIOManager<T, M>,
		B extends Builder<T, M, B>
	> {

		protected final B self;
		protected ServerInfo serverInfo;
		protected M manager;
		protected long liveCheckInterval;
		protected String checkingMessage;
		protected int expirationTime;
		
		@SuppressWarnings("unchecked")
		protected Builder() {
			this.self = (B) this;
		}
		
		public B setDefault() {
			setServerInfo(null);
			setManager(null);
			setLiveCheckInterval(0);
			setCheckingMessage(null);
			setExpirationTime(0);
			return self;
		}
		
		public B setServerInfo(ServerInfo serverInfo) {
			this.serverInfo = serverInfo;
			return self;
		}
		
		public B setManager(M manager) {	
			this.manager = manager;
			return self;
		}

		public B setLiveCheckInterval(long liveCheckInterval) {
			this.liveCheckInterval = liveCheckInterval;
			return self;
		}
		
		public B setCheckingMessage(String checkingMessage) {
			this.checkingMessage = checkingMessage;
			return self;
		}
		
		public B setExpirationTime(int expirationTime) {
			this.expirationTime = expirationTime;
			return self;
		}

		public ObjectExpirationHandler<T, M> build() {
			if(serverInfo == null || manager == null || liveCheckInterval <= 0 || checkingMessage == null || expirationTime <= 0)
				throw new IllegalStateException();
			return buildInternally();
		}
		
		protected abstract ObjectExpirationHandler<T, M> buildInternally();
		
	}

}
