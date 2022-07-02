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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.OpenPartiesAndClaimsFabric;
import xaero.pac.common.entity.ILivingEntity;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.core.ServerCoreFabric;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements ILivingEntity {

	private CompoundTag xaero_OPAC_persistentData;

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	public void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo info) {
		if(xaero_OPAC_persistentData != null && !xaero_OPAC_persistentData.isEmpty())
			tag.put("xaero_OPAC_PersistentData", xaero_OPAC_persistentData.copy());
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	public void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo info) {
		xaero_OPAC_persistentData = tag.getCompound("xaero_OPAC_PersistentData");
	}

	@Override
	public CompoundTag getXaero_OPAC_PersistentData() {
		if(xaero_OPAC_persistentData == null)
			xaero_OPAC_persistentData = new CompoundTag();
		return xaero_OPAC_persistentData;
	}

	@Inject(at = @At("HEAD"), method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
	public void onAddEffect(MobEffectInstance mobEffectInstance, Entity entity, CallbackInfoReturnable<Boolean> info){
		if(!ServerCore.canAddLivingEntityEffect((LivingEntity)(Object)this, mobEffectInstance, entity))
			info.setReturnValue(false);
	}

	@Inject(method = "createWitherRose", at = @At("HEAD"))
	public void onCreateWitherRose(CallbackInfo callbackInfo){
		ServerCoreFabric.tryToSetMobGriefingEntity((Entity)(Object)this);
	}

	@Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
	public void onHurt(DamageSource source, float f, CallbackInfoReturnable<Boolean> info) {
		if(((OpenPartiesAndClaimsFabric) OpenPartiesAndClaims.INSTANCE).getCommonEvents().onLivingHurt(source, (Entity)(Object)this))
			info.setReturnValue(false);
	}

}
