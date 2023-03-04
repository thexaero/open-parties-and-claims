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

package xaero.pac.common.claims.player;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class PlayerClaimInfo
<
	PCI extends PlayerClaimInfo<PCI, M>,
	M extends PlayerClaimInfoManager<PCI, M>
> implements IPlayerClaimInfo<PlayerDimensionClaims>, ILinkedChainNode<PCI> {

	protected final PCI self;
	protected final M manager;
	private String playerUsername;
	protected final UUID playerId;
	protected final Map<ResourceLocation, PlayerDimensionClaims> claims;

	private boolean destroyed;
	private PCI nextInChain;
	private PCI previousInChain;
	
	@SuppressWarnings("unchecked")
	public PlayerClaimInfo(String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims, M manager) {
		this.self = (PCI) this;
		this.playerUsername = username;
		this.playerId = playerId;
		this.manager = manager;
		this.claims = claims;
	}
	
	private PlayerDimensionClaims ensureDimension(ResourceLocation dimension) {
		return claims.computeIfAbsent(dimension, d -> new PlayerDimensionClaims(playerId, d, new HashMap<>()));
	}

	private void removeDimension(ResourceLocation dimension){
		claims.remove(dimension);
	}

	@Nullable
	@Override
	public PlayerDimensionClaims getDimension(@Nonnull ResourceLocation dimension) {
		return claims.get(dimension);
	}
	
	public void onClaim(IPlayerConfigManager configManager, ResourceLocation dimension, PlayerChunkClaim claim, int x, int z) {
		PlayerDimensionClaims dimensionClaims = ensureDimension(dimension);
		dimensionClaims.addClaim(x, z, claim);
	}
	
	public void onUnclaim(IPlayerConfigManager configManager, ResourceLocation dimension, PlayerChunkClaim claim, int x, int z) {
		PlayerDimensionClaims dimensionClaims = ensureDimension(dimension);
		if(!dimensionClaims.removeClaim(x, z, claim))
			throw new IllegalStateException();
		if(dimensionClaims.getCount() <= 0)
			removeDimension(dimension);
	}
	
	protected abstract Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionClaimCountStream();

	protected abstract Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getDimensionForceloadCountStream();

	@Override
	public int getClaimCount() {
		return getDimensionClaimCountStream().mapToInt(e -> e.getValue().getCount()).sum();
	}

	@Override
	public int getForceloadCount() {
		return getDimensionForceloadCountStream().mapToInt(e -> e.getValue().getForceloadableCount()).sum();
	}

	@Nonnull
	@Override
	public UUID getPlayerId() {
		return playerId;
	}
	
	@Nonnull
	@Override
	public Stream<Entry<ResourceLocation, PlayerDimensionClaims>> getStream(){
		return claims.entrySet().stream();
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %d, %d]:%s", playerId, getClaimCount(), this.getForceloadCount(), super.toString());
	}

	@Nonnull
	@Override
	public String getPlayerUsername() {
		return playerUsername;
	}
	
	public void setPlayerUsername(String playerUsername) {
		this.playerUsername = playerUsername;
	}

	@Override
	public String getClaimsName() {
		return getClaimsName(-1);
	}

	@Override
	public int getClaimsColor() {
		return getClaimsColor(-1);
	}

	@Override
	public void setNext(PCI element){
		this.nextInChain = element;
	}

	@Override
	public void setPrevious(PCI element){
		this.previousInChain = element;
	}

	@Override
	public PCI getNext(){
		return nextInChain;
	}

	@Override
	public PCI getPrevious(){
		return previousInChain;
	}

	@Override
	public boolean isDestroyed(){
		return destroyed;
	}

	@Override
	public void onDestroyed(){
		destroyed = true;
	}

	
}
