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

package xaero.pac.common.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.common.event.CommonEventsFabric;
import xaero.pac.common.server.core.ServerCoreFabric;

@Mixin(GameRules.class)
public class MixinGameRules {

	@Inject(at = @At("HEAD"), method = "getBoolean", cancellable = true)
	public void onGetBoolean(GameRules.Key<GameRules.BooleanValue> key, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
		if(key == GameRules.RULE_MOBGRIEFING && ServerCoreFabric.MOB_GRIEFING_GAME_RULE_ENTITY != null) {
			OpenPartiesAndClaimsFabric modMain = (OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE;
			if(modMain == null)
				return;
			CommonEventsFabric commonEventsFabric = modMain.getCommonEvents();
			if(commonEventsFabric == null)
				return;
			MinecraftServer server = ServerCoreFabric.MOB_GRIEFING_GAME_RULE_ENTITY.getServer();
			if(server != null && server.isSameThread() && server.getGameRules() == (Object)this) {//making sure this is the server's game rules
				if (((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onMobGrief(ServerCoreFabric.MOB_GRIEFING_GAME_RULE_ENTITY))
					callbackInfoReturnable.setReturnValue(false);
				ServerCoreFabric.MOB_GRIEFING_GAME_RULE_ENTITY = null;
			}
		}
	}

}
