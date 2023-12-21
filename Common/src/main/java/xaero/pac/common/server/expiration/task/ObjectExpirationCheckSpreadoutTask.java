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

package xaero.pac.common.server.expiration.task;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.expiration.ObjectExpirationHandler;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObject;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.task.IServerSpreadoutQueuedTask;

import java.util.Iterator;
import java.util.List;

public class ObjectExpirationCheckSpreadoutTask<T extends ObjectManagerIOExpirableObject> implements IServerSpreadoutQueuedTask<ObjectExpirationCheckSpreadoutTask<?>> {

	private final ObjectExpirationHandler<T, ?> expirationHandler;
	private final Iterator<T> iterator;
	private boolean finished;

	public ObjectExpirationCheckSpreadoutTask(ObjectExpirationHandler<T, ?> expirationHandler, Iterator<T> iterator) {
		this.expirationHandler = expirationHandler;
		this.iterator = iterator;
	}

	@Override
	public void onQueued(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {

	}

	@Override
	public boolean shouldWork(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ObjectExpirationCheckSpreadoutTask<?> holder) {
		return !shouldDrop(serverData, holder);
	}

	@Override
	public boolean shouldDrop(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ObjectExpirationCheckSpreadoutTask<?> holder) {
		return finished;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ObjectExpirationCheckSpreadoutTask<?> holder, int perTick, List<ObjectExpirationCheckSpreadoutTask<?>> tasksToAdd) {
		int stepsLeft = perTick;
		ServerInfo serverInfo = expirationHandler.getServerInfo();
		long expirationTime = expirationHandler.getExpirationTime();
		while(!expirationHandler.isExpiringAnElement() && iterator.hasNext() && stepsLeft > 0){
			T object = iterator.next();

			expirationHandler.preExpirationCheck(object);

			boolean hasBeenActive = object.hasBeenActive();//since last check
			if(!hasBeenActive)
				hasBeenActive = expirationHandler.checkIfActive(object);
			if(object.getLastConfirmedActivity() > serverInfo.getUseTime()) {//last active in the future!
				OpenPartiesAndClaims.LOGGER.warn("Mod use time seems to have been reset! This could happen due to the data/server-info.nbt file corruption, with a backup being likely created. Defaulting to the time of a confirmed activity...");
				serverInfo.setUseTime(object.getLastConfirmedActivity());
				serverData.getServerInfoIO().save();
			}
			if(hasBeenActive) {
				object.confirmActivity(serverInfo);
				object.setDirty(true);
			} else if(serverInfo.getUseTime() - object.getLastConfirmedActivity() > expirationTime) {
				OpenPartiesAndClaims.LOGGER.debug("Object expired and is being removed: " + object);
				expirationHandler.onElementExpirationBegin();
				if(expirationHandler.expire(object, serverData)) {
					expirationHandler.onElementExpirationDone();//otherwise called when expiration finishes later
					stepsLeft -= 2;//actual expiration counts as extra steps
				}
			}
			stepsLeft--;
		}
		if(!iterator.hasNext()) {
			expirationHandler.onIterationDone();
			finished = true;
		}
	}
}
