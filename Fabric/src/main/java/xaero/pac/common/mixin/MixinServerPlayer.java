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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

@Mixin(value = ServerPlayer.class, priority = 1000001)
public class MixinServerPlayer implements IOpenPACServerPlayer {

	private ServerPlayerDataAPI xaero_OPAC_PlayerData;

	@Override
	public ServerPlayerDataAPI getXaero_OPAC_PlayerData() {
		return xaero_OPAC_PlayerData;
	}

	@Override
	public void setXaero_OPAC_PlayerData(ServerPlayerDataAPI data) {
		xaero_OPAC_PlayerData = data;
	}


	@Inject(method = "attack", at = @At("HEAD"))
	public void onAttackPre(Entity target, CallbackInfo info) {
		ServerCore.preResourcesDrop((Entity)(Object)this);
	}

	@Inject(method = "attack", at = @At("RETURN"))
	public void onAttack(Entity target, CallbackInfo info) {
		ServerCore.postResourcesDrop((Entity)(Object)this);
	}

}
