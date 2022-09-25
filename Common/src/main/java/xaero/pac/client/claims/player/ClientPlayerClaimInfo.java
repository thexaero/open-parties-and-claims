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

package xaero.pac.client.claims.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.client.claims.player.sub.ClientPlayerSubClaimInfo;
import xaero.pac.common.claims.player.PlayerClaimInfo;
import xaero.pac.common.claims.player.PlayerDimensionClaims;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

public final class ClientPlayerClaimInfo extends PlayerClaimInfo<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager> implements IClientPlayerClaimInfo<PlayerDimensionClaims> {

	private final Int2ObjectMap<ClientPlayerSubClaimInfo> subClaimInfo;
	
	public ClientPlayerClaimInfo(String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims,
								 ClientPlayerClaimInfoManager manager, Int2ObjectMap<ClientPlayerSubClaimInfo> subClaimInfo) {
		super(username, playerId, claims, manager);
		this.subClaimInfo = subClaimInfo;
	}

	@Override
	protected Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionClaimCountStream() {
		return claims.entrySet().stream();
	}

	@Override
	protected Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionForceloadCountStream() {
		return getDimensionClaimCountStream();
	}

	@Nullable
	@Override
	public String getClaimsName(int subConfigIndex) {
		ClientPlayerSubClaimInfo sub = subClaimInfo.get(subConfigIndex);
		if(sub == null)
			return null;
		return sub.getClaimsName();
	}

	@Nullable
	@Override
	public Integer getClaimsColor(int subConfigIndex) {
		ClientPlayerSubClaimInfo sub = subClaimInfo.get(subConfigIndex);
		if(sub == null)
			return null;
		return sub.getClaimsColor();
	}

	public void ensureSubClaim(int subConfigIndex){
		if(!subClaimInfo.containsKey(subConfigIndex))
			subClaimInfo.put(subConfigIndex, new ClientPlayerSubClaimInfo(subConfigIndex));
	}

	public void removeSubClaim(int subConfigIndex){
		subClaimInfo.remove(subConfigIndex);
	}

	public void setClaimsName(int subConfigIndex, String name){
		ClientPlayerSubClaimInfo sub = subClaimInfo.get(subConfigIndex);
		if(sub == null)
			return;
		sub.setClaimsName(name);
	}

	public void setClaimsColor(int subConfigIndex, Integer color){
		ClientPlayerSubClaimInfo sub = subClaimInfo.get(subConfigIndex);
		if(sub == null)
			return;
		sub.setClaimsColor(color);
	}

}
