/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.protection.override;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.claims.protection.override.api.ChunkAccessOverride;
import xaero.pac.common.server.claims.protection.override.api.ChunkAccessOverrideType;
import xaero.pac.common.server.claims.protection.override.api.IChunkAccessOverriderAPI;
import xaero.pac.common.server.claims.protection.override.api.IChunkAccessOverriderManagerAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChunkAccessOverriderManager implements IChunkAccessOverriderManagerAPI {

	private final ChunkAccessOverride defaultOverride;
	private final List<IChunkAccessOverriderAPI> overriders;

	public ChunkAccessOverriderManager(ChunkAccessOverride defaultOverride, List<IChunkAccessOverriderAPI> overriders) {
		this.defaultOverride = defaultOverride;
		this.overriders = overriders;
	}

	@Override
	public void register(@Nonnull IChunkAccessOverriderAPI overrider){
		overriders.add(overrider);
		OpenPartiesAndClaims.LOGGER.info("Registered chunk access overrider: {}", overrider.getName());
	}

	public ChunkAccessOverride overrideChunkAccess(
			ResourceLocation dim,
			int x,
			int z,
			IPlayerConfigAPI claimConfig,
			Entity accessor,
			UUID accessorId,
			MinecraftServer server
	){
		ChunkAccessOverride currentOverride = defaultOverride;
		for (IChunkAccessOverriderAPI overrider : overriders) {
			ChunkAccessOverride overriderResult = overrider.overrideChunkAccess(
					dim, x, z, claimConfig, accessor, accessorId, server, currentOverride
			);
			if(overriderResult.getType() == ChunkAccessOverrideType.PROTECT)
				return overriderResult;
			if(currentOverride.getType() == ChunkAccessOverrideType.PASS &&
					overriderResult.getType() == ChunkAccessOverrideType.ALLOW)
				currentOverride = overriderResult;
		}
		return currentOverride;
	}

	public static final class Builder {

		private Builder(){}

		public Builder setDefault(){
			return this;
		}

		public ChunkAccessOverriderManager build(){
			ChunkAccessOverride defaultOverride = new ChunkAccessOverride(ChunkAccessOverrideType.PASS);
			return new ChunkAccessOverriderManager(defaultOverride, new ArrayList<>());
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}
}
