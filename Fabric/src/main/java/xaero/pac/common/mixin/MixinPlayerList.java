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

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.common.server.core.ServerCore;

@Mixin(PlayerList.class)
public class MixinPlayerList {

	@Inject(at = @At("HEAD"), method = "sendLevelInfo")
	public void onSendLevelInfo(ServerPlayer player, ServerLevel world, CallbackInfo info){
		ServerCore.onServerWorldInfo(player);
	}

	@Inject(at = @At("TAIL"), method = "placeNewPlayer")
	public void onPlaceNewPlayer(Connection connection, ServerPlayer serverPlayer, CallbackInfo info){
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onPlayerLogIn(serverPlayer);
	}
	@Inject(at = @At("HEAD"), method = "remove")
	public void onRemove(ServerPlayer serverPlayer, CallbackInfo info){
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onPlayerLogOut(serverPlayer);
	}

	@Inject(at = @At("HEAD"), method = "op")
	public void onOp(GameProfile profile, CallbackInfo info){
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onPermissionsChanged((PlayerList)(Object)this, profile);
	}

	@Inject(at = @At("HEAD"), method = "deop")
	public void onDeop(GameProfile profile, CallbackInfo info){
		((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onPermissionsChanged((PlayerList)(Object)this, profile);
	}

}
