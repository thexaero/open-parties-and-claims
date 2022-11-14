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

package xaero.pac.common.server.player.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Map;

public class AdaptiveLocalizer {

	private final Map<String, String> defaultTranslations;

	public AdaptiveLocalizer(Map<String, String> defaultTranslations) {
		this.defaultTranslations = defaultTranslations;
	}

	public MutableComponent getFor(ServerPlayer player, String key, Object... args){
		ServerPlayerData playerDataAPI = (ServerPlayerData) ServerPlayerData.from(player);
		if(playerDataAPI.hasMod())
			return new TranslatableComponent(key, args);
		return getServerLocalizedComponent(key, args);
	}

	public Component getFor(ServerPlayer player, Component component){
		if(!(component instanceof TranslatableComponent translatableComponent))
			return component;
		ServerPlayerData playerDataAPI = (ServerPlayerData) ServerPlayerData.from(player);
		if(playerDataAPI.hasMod())
			return translatableComponent;
		String key = translatableComponent.getKey();
		Object[] args = translatableComponent.getArgs();
		return getServerLocalizedComponent(key, args).setStyle(component.getStyle());
	}

	private MutableComponent getServerLocalizedComponent(String key, Object... args){
		for(int i = 0; i < args.length; i++)
			if(args[i] instanceof TranslatableComponent translatableComponent)
				args[i] = getServerLocalizedComponent(translatableComponent.getKey(), translatableComponent.getArgs());
		return new TranslatableComponent(defaultTranslations.getOrDefault(key, key), args);
	}

}
