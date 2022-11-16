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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.core.ServerCoreFabric;
import xaero.pac.common.server.world.IServerLevel;

@Mixin(value = ServerLevel.class, priority = 1000001)
public class MixinServerLevel implements IServerLevel {

	private LongSet xaero_OPAC_forceloadTickets;

	@Override
	public LongSet getXaero_OPAC_forceloadTickets() {
		if(xaero_OPAC_forceloadTickets == null)
			xaero_OPAC_forceloadTickets = new LongOpenHashSet();
		return xaero_OPAC_forceloadTickets;
	}

	@Inject(method = "isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z", at = @At("HEAD"), cancellable = true)
	public void onIsNaturalSpawningAllowed(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> infoReturnable){
		LongSet forceloadTickets = getXaero_OPAC_forceloadTickets();
		if(forceloadTickets.contains(chunkPos.toLong()))
			infoReturnable.setReturnValue(true);
	}

	@Inject(method = "isPositionEntityTicking", at = @At("RETURN"), cancellable = true)
	public void onIsPositionEntityTicking(BlockPos pos, CallbackInfoReturnable<Boolean> infoReturnable){
		infoReturnable.setReturnValue(ServerCore.replaceIsPositionEntityTicking(infoReturnable.getReturnValue(), (ServerLevel)(Object)this, pos));
	}

	@Inject(method = "tickCustomSpawners", at = @At("HEAD"))
	public void preTickCustomSpawners(boolean b1, boolean b2, CallbackInfo ci){
		ServerCoreFabric.setMobSpawnTypeForNewEntities(MobSpawnType.NATURAL, ((ServerLevel)(Object)this).getServer());
	}

	@Inject(method = "tickCustomSpawners", at = @At("RETURN"))
	public void postTickCustomSpawners(boolean b1, boolean b2, CallbackInfo ci){
		ServerCoreFabric.resetMobSpawnTypeForNewEntities();
	}

}
