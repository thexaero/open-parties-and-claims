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

package xaero.pac.common.packet.parties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.parties.party.ClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPartyAllyPacket extends LazyPacket<LazyPacket.Encoder<ClientboundPartyAllyPacket>, ClientboundPartyAllyPacket> {
	
	public static final Encoder<ClientboundPartyAllyPacket> ENCODER = new Encoder<>();
	
	private final Action action;
	private final ClientPartyAllyInfo allyInfo;
	
	public ClientboundPartyAllyPacket(Action action, ClientPartyAllyInfo allyInfo) {
		super();
		this.action = action;
		this.allyInfo = allyInfo;
	}

	@Override
	protected Encoder<ClientboundPartyAllyPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundPartyAllyPacket> encoder, FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("i", allyInfo.getAllyId());
		tag.putString("n", allyInfo.getAllyName());
		tag.putString("dn", allyInfo.getAllyDefaultName());
		tag.putString("a", action.toString());
		u.writeNbt(tag);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundPartyAllyPacket> {

		@Override
		public ClientboundPartyAllyPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag tag = input.readNbt(new NbtAccounter(102400));
				if(tag == null)
					return null;
				UUID allyId = tag.getUUID("i");
				String allyName = tag.getString("n");
				if(allyName.length() > 512)
					return null;
				String allyDefaultName = tag.getString("dn");
				if(allyDefaultName.isEmpty() || allyDefaultName.length() > 512)
					return null;
				String actionString = tag.getString("a");
				if(actionString.isEmpty() || actionString.length() > 128)
					return null;
				Action action = Action.valueOf(actionString);
				return new ClientboundPartyAllyPacket(action, new ClientPartyAllyInfo(allyId, allyName, allyDefaultName));
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPartyAllyPacket> {
		
		@Override
		public void accept(ClientboundPartyAllyPacket t) {
			IClientParty<?, ?, ?> party = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty();
			if(party == null)
				return;
			if(t.action == Action.ADD)
				party.addAllyParty(t.allyInfo.getAllyId());
			else
				party.removeAllyParty(t.allyInfo.getAllyId());
			if(t.action == Action.UPDATE)
				party.addAllyParty(t.allyInfo.getAllyId());

			if(t.action == Action.REMOVE)
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getAllyInfoStorage().remove(t.allyInfo.getAllyId());
			else
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getAllyInfoStorage().add(t.allyInfo);

		}
		
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s, %s]", action, allyInfo.getAllyName(), allyInfo.getAllyDefaultName());
	}
	
	public enum Action {
		ADD,
		REMOVE,
		UPDATE
	}
	
}
