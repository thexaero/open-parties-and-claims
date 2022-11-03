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

package xaero.pac.common.server.claims.protection;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class ChunkProtectionEntityHelper {
	
	private static EntityDataAccessor<Optional<UUID>> FOX_TRUSTED_UUID_SECONDARY;
	private static EntityDataAccessor<Optional<UUID>> FOX_TRUSTED_UUID_MAIN;
	
	static {
		Field foxTrustSecondaryField = null;
		Field foxTrustMainField = null;
		try {
			foxTrustSecondaryField = Fox.class.getDeclaredField("f_28439_");
		} catch (Exception e) {
			try {
				foxTrustSecondaryField = Fox.class.getDeclaredField("field_17951");
			} catch (Exception e1) {
				try {
					foxTrustSecondaryField = Fox.class.getDeclaredField("DATA_TRUSTED_ID_0");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		try {
			foxTrustMainField = Fox.class.getDeclaredField("f_28440_");
		} catch (Exception e) {
			try {
				foxTrustMainField = Fox.class.getDeclaredField("field_17952");
			} catch (Exception e1) {
				try {
					foxTrustMainField = Fox.class.getDeclaredField("DATA_TRUSTED_ID_1");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		if(foxTrustSecondaryField != null)
			try {
				foxTrustSecondaryField.setAccessible(true);
				FOX_TRUSTED_UUID_SECONDARY = (EntityDataAccessor<Optional<UUID>>) foxTrustSecondaryField.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		if(foxTrustMainField != null)
			try {
				foxTrustMainField.setAccessible(true);
				FOX_TRUSTED_UUID_MAIN = (EntityDataAccessor<Optional<UUID>>) foxTrustMainField.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	boolean hostileException(Entity e) {
		if(e instanceof Piglin)
			return ((Piglin)e).isBaby();
		return false;
	}
	
	boolean isHostile(Entity e) {
		return (e.getLevel().getDifficulty() != Difficulty.PEACEFUL && !hostileException(e) && (e instanceof Enemy || e.getSoundSource() == SoundSource.HOSTILE));
	}
	
	boolean isTamed(Entity e, Player p) {
		UUID owner = getTamer(e);
		if(p.getUUID().equals(owner))
			return true;
		if(e instanceof Fox fox)
			return FOX_TRUSTED_UUID_MAIN != null && p.getUUID().equals(fox.getEntityData().get(FOX_TRUSTED_UUID_MAIN).orElse(null));
		return false;
	}

	UUID getTamer(Entity e){
		if(e instanceof TamableAnimal tameable)
			return tameable.isTame() ? tameable.getOwnerUUID() : null;
		else if(e instanceof AbstractHorse horse)
			return horse.isTamed() ? horse.getOwnerUUID() : null;
		else if(e instanceof Fox fox)
			return FOX_TRUSTED_UUID_SECONDARY != null ? fox.getEntityData().get(FOX_TRUSTED_UUID_SECONDARY).orElse(null) : null;
		return null;
	}

}
