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

package xaero.pac.common.server.parties.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PartyOnCommandUpdater {
	
	public <M extends IPartyMember, I extends IPartyPlayerInfo, A extends IPartyAlly> void update(
			UUID commandCasterId,
			IServerData<?,?> serverData,
			IServerParty<M, I, A> party,
			IPlayerConfigManager configs,
			Predicate<IPartyMember> shouldUpdateCommandsForMember,
			Component massMessageContent
	) {
		String partyName = party.getDefaultName();
		String partyCustomName = configs.getLoadedConfig(party.getOwner().getUUID()).getEffective(PlayerConfigOptions.PARTY_NAME);
		if(!partyCustomName.isEmpty())
			partyName = partyCustomName;
		Component partyNameComponent = new TextComponent("[" + partyName + "] ").withStyle(s ->
				s.withColor(ChatFormatting.GOLD).withHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(party.getDefaultName()))
				)
		);

		if(ServerConfig.CONFIG.partyChatLogging.get()) {
			String logMessage = partyNameComponent.getString() + massMessageContent.getString();
			OpenPartiesAndClaims.LOGGER.info(logMessage);
		}
		MinecraftServer server = serverData.getServer();
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		Consumer<ServerPlayer> messageSender = memberPlayer -> {
			Component memberMessage = new TextComponent("");//can't reuse because onlineMember.sendMessage might not encode the message immediately, which can cause a race condition
			memberMessage.getSiblings().add(partyNameComponent);
			memberMessage.getSiblings().add(adaptiveLocalizer.getFor(memberPlayer, massMessageContent));
			memberPlayer.sendMessage(memberMessage, commandCasterId);
		};
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			M memberInfo = party.getMemberInfo(player.getUUID());
			if(memberInfo != null) {
				if(shouldUpdateCommandsForMember.test(memberInfo))
					serverData.getPlayerPermissionChangeHandler().sendCommandsAndUpdatePermissions(player, serverData, false);
				messageSender.accept(player);
				continue;
			}
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
			if(playerData.isPartiesAdminMode())
				messageSender.accept(player);
		}
	}

}
