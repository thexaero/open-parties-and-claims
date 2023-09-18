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
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.PartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPartyPlayerPacket extends LazyPacket<ClientboundPartyPlayerPacket.Codec, ClientboundPartyPlayerPacket>{

	public static final Codec CODEC = new Codec(new PartyPlayerInfoCodec());
	
	private final Type type;
	private final Action action;
	private final IPartyPlayerInfo playerInfo;
	
	public ClientboundPartyPlayerPacket(Type type, Action action, IPartyPlayerInfo playerInfo) {
		super();
		this.type = type;
		this.action = action;
		this.playerInfo = playerInfo;
	}

	@Override
	protected Codec getEncoder() {
		return CODEC;
	}

	@Override
	protected void writeOnPrepare(Codec encoder, FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putString("t", type.toString());
		tag.putString("a", action.toString());
		CompoundTag playerTag = type == Type.INVITE ? encoder.playerInfoCodec.toPartyInviteTag((PartyInvite) playerInfo) : encoder.playerInfoCodec.toMemberTag((PartyMember) playerInfo);
		tag.put("pi", playerTag);
		u.writeNbt(tag);
	}
	
	protected static class Codec extends LazyPacket.Encoder<ClientboundPartyPlayerPacket> implements Function<FriendlyByteBuf, ClientboundPartyPlayerPacket> {
		
		private final PartyPlayerInfoCodec playerInfoCodec;
		
		public Codec(PartyPlayerInfoCodec playerInfoCodec) {
			this.playerInfoCodec = playerInfoCodec;
		}

		@Override
		public ClientboundPartyPlayerPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 102400)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				String typeString = tag.getString("t");
				if(typeString.isEmpty() || typeString.length() > 128)
					return null;
				Type type = Type.valueOf(typeString);
				String actionString = tag.getString("a");
				if(actionString.isEmpty() || actionString.length() > 128)
					return null;
				Action action = Action.valueOf(actionString);
				CompoundTag playerTag = tag.getCompound("pi");
				PartyPlayerInfo<?> playerInfo = type == Type.INVITE ? playerInfoCodec.fromPartyInviteTag(playerTag) : playerInfoCodec.fromMemberTag(playerTag, type == Type.OWNER);
				if(playerInfo == null) {
					OpenPartiesAndClaims.LOGGER.info("Received party player packet with invalid data.");
					return null;
				}
				return new ClientboundPartyPlayerPacket(type, action, playerInfo);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPartyPlayerPacket> {
		
		@Override
		public void accept(ClientboundPartyPlayerPacket t) {
			IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> party = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty();
			if(party == null)
				return;
			if(t.type == Type.MEMBER) {
				IPartyMember memberInfo = (IPartyMember) t.playerInfo;
				if(t.action == Action.ADD)
					party.addMember(memberInfo.getUUID(), memberInfo.getRank(), memberInfo.getUsername());
				else
					party.removeMember(memberInfo.getUUID());
				if(t.action == Action.UPDATE)
					party.addMember(memberInfo.getUUID(), memberInfo.getRank(), memberInfo.getUsername());
			} else if(t.type == Type.OWNER) {
				if(t.action != Action.UPDATE) {
					OpenPartiesAndClaims.LOGGER.info("Received invalid party owner update packet!");
					return;
				}
				IPartyMember newInfo = (IPartyMember) t.playerInfo;
				IPartyMember owner = party.getOwner();
				if(owner.getUUID() != newInfo.getUUID())
					party.changeOwner(newInfo.getUUID(), newInfo.getUsername());
				else
					((PartyMember) party.getOwner()).setUsername(newInfo.getUsername());
			} else {
				if(t.action == Action.ADD) {
					party.invitePlayer(t.playerInfo.getUUID(), t.playerInfo.getUsername());
				} else if(t.action == Action.REMOVE) {
					party.uninvitePlayer(t.playerInfo.getUUID());
				} else {
					OpenPartiesAndClaims.LOGGER.info("Received invalid party invites update packet!");
					return;
				}
			}
		}
		
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s, %s]", type, action, playerInfo.getUsername());
	}
	
	public enum Type {
		MEMBER,
		INVITE,
		OWNER
	}
	
	public enum Action {
		ADD,
		REMOVE,
		UPDATE
	}
	
}
