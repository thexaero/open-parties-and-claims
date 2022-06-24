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

package xaero.pac.common.parties.party;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PartyMemberDynamicInfoSyncable implements IPartyMemberDynamicInfoSyncable {

	private ResourceLocation dimension;
	private double x;
	private double y;
	private double z;
	private final UUID playerId;
	private boolean dirty;
	private final boolean active;
	private UUID partyId;
	private final PartyMemberDynamicInfoSyncable remover;
	
	public PartyMemberDynamicInfoSyncable(UUID playerId, boolean active) {
		super();
		this.playerId = playerId;
		this.active = active;
		if(active)
			remover = new PartyMemberDynamicInfoSyncable(playerId, false);
		else
			remover = null;
	}
	
	@Nonnull
    @Override
	public UUID getPlayerId() {
		return playerId;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Nullable
	@Override
	public ResourceLocation getDimension() {
		return dimension;
	}
	
	public void setDimension(ResourceLocation dimension) {
		if(!Objects.equals(dimension, this.dimension))
			dirty = true;
		this.dimension = dimension;
	}
	
	public void setX(double x) {
		if(this.x != x)
			dirty = true;
		this.x = x;
	}
	
	public void setY(double y) {
		if(this.y != y)
			dirty = true;
		this.y = y;
	}
	
	public void setZ(double z) {
		if(this.z != z)
			dirty = true;
		this.z = z;
	}

	@Override
	public boolean isActive() {
		return active;
	}
	
	public void setPartyId(UUID partyId) {
		if(!Objects.equals(this.partyId, partyId))
			dirty = true;
		this.partyId = partyId;
	}
	
	public PartyMemberDynamicInfoSyncable getRemover() {
		return remover;
	}

	@Override
	public void update(ResourceLocation dimension, double x, double y, double z) {
		setDimension(dimension);
		setX(x);
		setY(y);
		setZ(z);
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public String toString() {
		return String.format("[%f, %f, %f, %s, %s]", x, y, z, dimension, active);
	}
	
	public static class Codec implements BiConsumer<PartyMemberDynamicInfoSyncable, FriendlyByteBuf>, Function<FriendlyByteBuf, PartyMemberDynamicInfoSyncable> {
		
		@Override
		public PartyMemberDynamicInfoSyncable apply(FriendlyByteBuf input) {
			try {
				CompoundTag tag = input.readNbt(new NbtAccounter(16384));
				UUID playerId = tag.getUUID("i");
				boolean active = tag.getBoolean("a");
				if(!active)
					return new PartyMemberDynamicInfoSyncable(playerId, false);
				String dimensionSpace = tag.getString("ds");
				String dimensionPath = tag.getString("dp");
				ResourceLocation dimension = new ResourceLocation(dimensionSpace, dimensionPath);
				double x = tag.getDouble("x");
				double y = tag.getDouble("y");
				double z = tag.getDouble("z");
				PartyMemberDynamicInfoSyncable result = new PartyMemberDynamicInfoSyncable(playerId, true);
				result.update(dimension, x, y, z);
				result.dirty = false;
				return result;
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(PartyMemberDynamicInfoSyncable t, FriendlyByteBuf u) {
			t.dirty = false;
			CompoundTag tag = new CompoundTag();
			tag.putUUID("i", t.playerId);
			tag.putBoolean("a", t.active);
			if(t.active) {
				tag.putString("ds", t.dimension.getNamespace());
				tag.putString("dp", t.dimension.getPath());
				tag.putDouble("x", t.x);
				tag.putDouble("y", t.y);
				tag.putDouble("z", t.z);
			}
			u.writeNbt(tag);
		}
		
	}
	
	public static class ClientHandler implements Consumer<PartyMemberDynamicInfoSyncable> {
		
		@Override
		public void accept(PartyMemberDynamicInfoSyncable t) {
			if(t.active) {
				IPartyMemberDynamicInfoSyncable clientInfo = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getPartyMemberDynamicInfoSyncableStorage().getOrSetForPlayer(t.getPlayerId(), t);
				if(clientInfo != t)
					clientInfo.update(t.dimension, t.x, t.y, t.z);
				PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(t.getPlayerId());
				String playerName = playerInfo == null ? "unknown" : playerInfo.getProfile().getName();
			} else {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getPartyMemberDynamicInfoSyncableStorage().removeForPlayer(t.playerId);
			}
		}
		
	}
	
}
