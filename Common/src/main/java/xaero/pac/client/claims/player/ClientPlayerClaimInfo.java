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

package xaero.pac.client.claims.player;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.PlayerClaimInfo;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

public final class ClientPlayerClaimInfo extends PlayerClaimInfo<ClientPlayerClaimInfo, ClientPlayerClaimInfoManager> implements IClientPlayerClaimInfo<PlayerDimensionClaims> {

	private String claimsName;
	private int claimsColor;
	
	public ClientPlayerClaimInfo(String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims,
			ClientPlayerClaimInfoManager manager) {
		super(username, playerId, claims, manager);
	}

	@Override
	protected void onForceloadUnclaim(IPlayerConfigManager<?> configManager, ResourceLocation dimension, int x, int z) {
	}

	@Override
	protected Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionClaimCountStream() {
		return claims.entrySet().stream();
	}

	@Override
	protected Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionForceloadCountStream() {
		return getDimensionClaimCountStream();
	}

	@Override
	public String getClaimsName() {
		return claimsName;
	}

	@Override
	public int getClaimsColor() {
		return claimsColor;
	}
	
	public void setClaimsName(String claimsName) {
		this.claimsName = claimsName;
	}
	
	public void setClaimsColor(int claimsColor) {
		this.claimsColor = claimsColor;
	}

}
