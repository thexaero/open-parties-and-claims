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

package xaero.pac.common.packet.parties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.parties.party.ClientParty;
import xaero.pac.common.parties.party.Party;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPartyPacket extends LazyPacket<ClientboundPartyPacket.Codec, ClientboundPartyPacket> {
	
	public static final Codec CODEC = new Codec(new PartyPlayerInfoCodec());
	private final UUID partyId; 
	private final IPartyMember owner;
	private final int memberCount;
	private final int inviteCount; 
	private final int allyCount;
	private final int memberLimit;
	private final int inviteLimit; 
	private final int allyLimit;

	public ClientboundPartyPacket(UUID partyId, IPartyMember owner, int memberCount, int inviteCount, int allyCount, int memberLimit, int inviteLimit, int allyLimit) {
		super();
		this.partyId = partyId;
		this.owner = owner;
		this.memberCount = memberCount;
		this.inviteCount = inviteCount;
		this.allyCount = allyCount;
		this.memberLimit = memberLimit;
		this.inviteLimit = inviteLimit;
		this.allyLimit = allyLimit;
	}

	@Override
	protected Codec getEncoder() {
		return CODEC;
	}

	@Override
	protected void writeOnPrepare(Codec encoder, FriendlyByteBuf u) {
		CompoundTag partyTag = new CompoundTag();
		if(partyId == null) {
			u.writeNbt(partyTag);
			return;
		}
		partyTag.putUUID("i", partyId);
		CompoundTag ownerTag = encoder.playerInfoCodec.toMemberTag((PartyMember) owner);
		partyTag.put("o", ownerTag);
		partyTag.putInt("mc", memberCount);
		partyTag.putInt("ic", inviteCount);
		partyTag.putInt("ac", allyCount);
		partyTag.putInt("ml", memberLimit);
		partyTag.putInt("il", inviteLimit);
		partyTag.putInt("al", allyLimit);
		
		u.writeNbt(partyTag);
	}
	
	protected static class Codec extends LazyPacket.Encoder<ClientboundPartyPacket> implements Function<FriendlyByteBuf, ClientboundPartyPacket> {
		
		private final PartyPlayerInfoCodec playerInfoCodec;
		
		public Codec(PartyPlayerInfoCodec playerInfoCodec) {
			this.playerInfoCodec = playerInfoCodec;
		}

		@Override
		public ClientboundPartyPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 102400)
					return null;
				CompoundTag partyTag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(partyTag == null)
					return null;
				if(partyTag.isEmpty())
					return new ClientboundPartyPacket(null, null, 0, 0, 0, 0, 0, 0);
				UUID partyId = partyTag.getUUID("i");
				CompoundTag ownerTag = partyTag.getCompound("o");
				if(ownerTag.isEmpty()) {
					OpenPartiesAndClaims.LOGGER.info("Received party packet with no owner info.");
					return null;
				}
				PartyMember owner = playerInfoCodec.fromMemberTag(ownerTag, true);
				if(owner == null) {
					OpenPartiesAndClaims.LOGGER.info("Received party packet with invalid owner info data.");
					return null;
				}
				int memberCount = partyTag.getInt("mc");
				int inviteCount = partyTag.getInt("ic");
				int allyCount = partyTag.getInt("ac");
				int memberLimit = partyTag.getInt("ml");
				int inviteLimit = partyTag.getInt("il");
				int allyLimit = partyTag.getInt("al");
				return new ClientboundPartyPacket(partyId, owner, memberCount, inviteCount, allyCount, memberLimit, inviteLimit, allyLimit);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPartyPacket> {
		
		@Override
		public void accept(ClientboundPartyPacket t) {
			Party party = t.partyId == null ? null : ClientParty.Builder.begin().setId(t.partyId).setOwner((PartyMember) t.owner).build();
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setPartyCast(party);

			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setLoadingMemberCount(t.memberCount);
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setLoadingInviteCount(t.inviteCount);
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setLoadingAllyCount(t.allyCount);

			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setMemberLimit(t.memberLimit);
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setInviteLimit(t.inviteLimit);
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setAllyLimit(t.allyLimit);
		}
		
	}
	
}
