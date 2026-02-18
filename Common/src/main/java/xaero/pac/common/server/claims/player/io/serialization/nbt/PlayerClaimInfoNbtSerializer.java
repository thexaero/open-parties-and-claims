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

package xaero.pac.common.server.claims.player.io.serialization.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.io.serialization.SimpleSerializer;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerClaimInfoNbtSerializer implements SimpleSerializer<CompoundTag, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> {
	
	private final PlayerDimensionClaimsNbtSerializer playerDimensionClaimsNbtSerializer;
	
	private PlayerClaimInfoNbtSerializer(PlayerDimensionClaimsNbtSerializer playerDimensionClaimsNbtSerializer) {
		super();
		this.playerDimensionClaimsNbtSerializer = playerDimensionClaimsNbtSerializer;
	}

	@Override
	public CompoundTag serialize(ServerPlayerClaimInfo object) {
		CompoundTag nbt = new CompoundTag();
		CompoundTag dimensions = new CompoundTag();
		object.getFullStream().forEach(e -> dimensions.put(e.getKey().toString(), playerDimensionClaimsNbtSerializer.serialize(e.getValue())));
		nbt.put("dimensions", dimensions);
		nbt.putString("username", object.getPlayerUsername());
		nbt.putLong("confirmedActivity", object.getRegisteredActivity());
		return nbt;
	}

	@Override
	public ServerPlayerClaimInfo deserialize(UUID id, ServerPlayerClaimInfoManager manager, CompoundTag nbt) {
		CompoundTag dimensionsTag = nbt.getCompoundOrEmpty("dimensions");
		String username = nbt.getStringOr("username", "");
		Map<Identifier, PlayerDimensionClaims> claims = new HashMap<>();
		dimensionsTag.keySet().forEach(key -> claims.put(Identifier.parse(key), playerDimensionClaimsNbtSerializer.deserialize(id, key, dimensionsTag.getCompoundOrEmpty(key))));
		ServerPlayerClaimInfo result = new ServerPlayerClaimInfo(manager.getConfig(id), username, id, claims, manager, new ArrayDeque<>());
		result.setRegisteredActivity(nbt.getLongOr("confirmedActivity", 0));
		return result;
	}
	
	public static final class Builder {

		private Builder() {
		}

		private Builder setDefault() {
			return this;
		}

		public PlayerClaimInfoNbtSerializer build() {
			return new PlayerClaimInfoNbtSerializer(new PlayerDimensionClaimsNbtSerializer(new PlayerChunkClaimNbtSerializer()));
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
