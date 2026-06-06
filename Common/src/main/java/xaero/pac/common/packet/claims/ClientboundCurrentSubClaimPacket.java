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

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.mode.ClaimingModeSubInfo;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class ClientboundCurrentSubClaimPacket extends LazyPacket<ClientboundCurrentSubClaimPacket> {

	public static final Encoder<ClientboundCurrentSubClaimPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final Collection<ClaimingModeSubInfo> subInfoCollection;

	public ClientboundCurrentSubClaimPacket(Collection<ClaimingModeSubInfo> subInfoCollection) {
		super();
		this.subInfoCollection = subInfoCollection;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		CompoundTag subInfoTag = new CompoundTag();
		for (ClaimingModeSubInfo modeSubInfo : subInfoCollection) {
			CompoundTag modeSubInfoTag = new CompoundTag();
			modeSubInfoTag.putInt("i", modeSubInfo.getIndex());
			modeSubInfoTag.putString("s", modeSubInfo.getId());
			subInfoTag.put(modeSubInfo.getMode().getId(), modeSubInfoTag);
		}
		tag.put("s", subInfoTag);
		u.writeNbt(tag);
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundCurrentSubClaimPacket> getDecoder() {
		return DECODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundCurrentSubClaimPacket> {
		
		@Override
		public ClientboundCurrentSubClaimPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 4096 * ClaimingModes.ALL_IMMUTABLE.size())
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				CompoundTag subInfoTag = tag.getCompound("s");
				if(subInfoTag.isEmpty())
					return null;
				Collection<ClaimingModeSubInfo> subInfoCollection = new ArrayList<>();
				for (String modeId : subInfoTag.getAllKeys()) {
					if(modeId.length() > 100){
						OpenPartiesAndClaims.LOGGER.info("Claiming mode ID string is too long!");
						return null;
					}
					IClaimingModeAPI mode = ClaimingModes.get(modeId);
					CompoundTag modeSubInfoTag = subInfoTag.getCompound(modeId);
					int currentSubConfigIndex = modeSubInfoTag.getInt("i");
					String currentSubConfigId = modeSubInfoTag.getString("s");
					if(currentSubConfigId.length() > 100){
						OpenPartiesAndClaims.LOGGER.info("Player config sub ID string is too long!");
						return null;
					}
					subInfoCollection.add(new ClaimingModeSubInfo(mode, currentSubConfigIndex, currentSubConfigId));
				}
				return new ClientboundCurrentSubClaimPacket(subInfoCollection);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundCurrentSubClaimPacket> {
		
		@Override
		public void handle(ClientboundCurrentSubClaimPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onSubConfigIndices(
					t.subInfoCollection
			);
		}
		
	}

}
