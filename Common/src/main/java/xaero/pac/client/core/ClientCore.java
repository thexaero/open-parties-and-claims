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

package xaero.pac.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.patreon.Patreon;
import xaero.pac.client.world.IClientWorldData;
import xaero.pac.client.world.capability.ClientWorldMainCapability;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;

public class ClientCore {
	
	public static ResourceLocation getPlayerCape(AbstractClientPlayer player) {
		if(Patreon.optifine)
			return null;
		return Patreon.getPlayerCape(player);
	}
	
	public static Boolean isWearing(Player player, PlayerModelPart part) {
		if(Patreon.optifine)
			return null;
		if(part != PlayerModelPart.CAPE || !(player instanceof AbstractClientPlayer))
			return null;
		return Patreon.isWearingCape((AbstractClientPlayer) player);
	}

	public static void onInitializeWorldBorder(ClientboundInitializeBorderPacket packet){
		ClientWorldMainCapability capability = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(Minecraft.getInstance().level, ClientWorldCapabilityTypes.MAIN_CAP);
		IClientWorldData worldData = capability.getClientWorldDataInternal();
		boolean serverHasMod = worldData.serverHasMod();
		if(!serverHasMod) {
			//the border packet is sent after the handshake, so if we didn't get a handshake up until this point, then there is no mod on the server side
			OpenPartiesAndClaims.LOGGER.info("No Open Parties and Claims on the server! Resetting.");
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().reset();
		}
	}
	
}
