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

package xaero.pac.common.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.client.core.ClientCore;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target="Lnet/minecraft/client/player/LocalPlayer;resetPos()V"), method = "handleLogin")
	public void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo info) {
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getClientEvents().onPlayerLogin(Minecraft.getInstance().player);
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V"), method = "handleInitializeBorder")
	public void onHandleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket, CallbackInfo info){
		ClientCore.onInitializeWorldBorder(clientboundInitializeBorderPacket);
	}

}
