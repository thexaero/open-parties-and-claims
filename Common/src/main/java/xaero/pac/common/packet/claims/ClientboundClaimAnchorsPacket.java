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

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ClientboundClaimAnchorsPacket extends LazyPacket<ClientboundClaimAnchorsPacket> {

	public static final int MAX_ANCHORS = 128;
	public static final Encoder<ClientboundClaimAnchorsPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final UUID claimOwner;
	private final Identifier dimensionId;
	private final List<ChunkPos> anchors;
	private final boolean add;

	public ClientboundClaimAnchorsPacket(UUID claimOwner, Identifier dimensionId, List<ChunkPos> anchors, boolean add) {
		super();
		this.claimOwner = claimOwner;
		this.dimensionId = dimensionId;
		this.anchors = anchors;
		this.add = add;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimAnchorsPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf dest) {
		CompoundTag nbt = new CompoundTag();
		XaeroNbtUtil.putUUID(nbt, "o", claimOwner);
		nbt.putString("d", dimensionId.toString());
		ListTag anchorListTag = new ListTag();
		for (int i = 0; i < this.anchors.size(); i++) {
			ChunkPos anchorEntry = this.anchors.get(i);
			CompoundTag anchorTag = new CompoundTag();
			anchorTag.putInt("x", anchorEntry.x);
			anchorTag.putInt("z", anchorEntry.z);
			anchorListTag.add(anchorTag);
		}
		nbt.put("l", anchorListTag);
		nbt.putBoolean("a", add);
		dest.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimAnchorsPacket> {

		@Override
		public ClientboundClaimAnchorsPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 65536)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				UUID claimOwner = XaeroNbtUtil.getUUID(nbt, "o").orElse(null);
				Identifier dimensionId = Identifier.parse(nbt.getStringOr("d", ""));
				ListTag anchorListTag = nbt.getListOrEmpty("l");
				if(anchorListTag.size() > MAX_ANCHORS) {
					OpenPartiesAndClaims.LOGGER.info("Received claim anchor list is too large!");
					return null;
				}
				List<ChunkPos> anchors = new ArrayList<>(anchorListTag.size());
				for (int i = 0; i < anchorListTag.size(); i++) {
					CompoundTag anchorTag = anchorListTag.getCompoundOrEmpty(i);
					int x = anchorTag.getIntOr("x", 0);
					int z = anchorTag.getIntOr("z", 0);
					anchors.add(new ChunkPos(x, z));
				}
				boolean add = nbt.getBooleanOr("a", false);
				return new ClientboundClaimAnchorsPacket(claimOwner, dimensionId, anchors, add);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundClaimAnchorsPacket> {
		
		@Override
		public void handle(ClientboundClaimAnchorsPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().
					onClaimAnchors(t.claimOwner, t.dimensionId, t.anchors, t.add);
		}
		
	}

}
