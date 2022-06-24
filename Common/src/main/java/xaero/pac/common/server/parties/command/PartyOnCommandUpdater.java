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

package xaero.pac.common.server.parties.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Predicate;

public class PartyOnCommandUpdater {

	private void onOnlineMember(UUID commandCasterId, MinecraftServer server, IPartyMember mi, ServerPlayer onlineMember, Predicate<IPartyMember> shouldUpdateCommandsForMember, Component massMessage) {
		if(shouldUpdateCommandsForMember.test(mi))
			server.getCommands().sendCommands(onlineMember);
		if(massMessage != null)
			onlineMember.sendMessage(massMessage, commandCasterId);
	}
	
	public 
	<
		M extends IPartyMember, I extends IPartyPlayerInfo
	> void update(UUID commandCasterId, MinecraftServer server, IServerParty<M, I> party, IPlayerConfigManager<IServerParty<IPartyMember, IPartyPlayerInfo>> configs, Predicate<IPartyMember> shouldUpdateCommandsForMember, Component massMessageContent) {
		String partyName = party.getDefaultName();
		String partyCustomName = configs.getLoadedConfig(party.getOwner().getUUID()).getEffective(PlayerConfig.PARTY_NAME);
		if(!partyCustomName.isEmpty())
			partyName = partyCustomName;
		Component partyNameComponent = new TextComponent("[" + partyName + "] ").withStyle(s -> s.withColor(ChatFormatting.GOLD).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(party.getDefaultName()))));
		Component massMessage = new TextComponent("");
		massMessage.getSiblings().add(partyNameComponent);
		massMessage.getSiblings().add(massMessageContent);
		
		PlayerList playerList = server.getPlayerList();
		if(playerList.getPlayerCount() > party.getMemberCount()) {
			Iterator<M> iterator = party.getMemberInfoStream().iterator();
			while(iterator.hasNext()) {
				M memberInfo = iterator.next();
				ServerPlayer onlinePlayer = playerList.getPlayer(memberInfo.getUUID());
				if(onlinePlayer != null)
					onOnlineMember(commandCasterId, server, memberInfo, onlinePlayer, shouldUpdateCommandsForMember, massMessage);
			}
		} else {
			for (ServerPlayer onlinePlayer : playerList.getPlayers()) {
				M memberInfo = party.getMemberInfo(onlinePlayer.getUUID());
				if(memberInfo != null)
					onOnlineMember(commandCasterId, server, memberInfo, onlinePlayer, shouldUpdateCommandsForMember, massMessage);
			}
		}
	}

}
