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

package xaero.pac.common.server.player.localization;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.api.IAdaptiveLocalizerAPI;

import javax.annotation.Nonnull;
import java.util.Map;

public class AdaptiveLocalizer implements IAdaptiveLocalizerAPI {

	private final Map<String, String> defaultTranslations;

	public AdaptiveLocalizer(Map<String, String> defaultTranslations) {
		this.defaultTranslations = defaultTranslations;
	}

	@Override
	@Nonnull
	public MutableComponent getFor(@Nonnull ServerPlayer player, @Nonnull String key, @Nonnull Object... args){
		ServerPlayerData playerDataAPI = (ServerPlayerData) ServerPlayerData.from(player);
		if(playerDataAPI.hasMod())
			return new TranslatableComponent(key, args);
		return getServerLocalizedComponent(key, args);
	}

	@Override
	@Nonnull
	public Component getFor(@Nonnull ServerPlayer player, @Nonnull Component component){
		if(!(component instanceof TranslatableComponent translatableComponent))
			return component;
		ServerPlayerData playerDataAPI = (ServerPlayerData) ServerPlayerData.from(player);
		if(playerDataAPI.hasMod())
			return translatableComponent;
		String key = translatableComponent.getKey();
		Object[] args = translatableComponent.getArgs();
		Component result = getServerLocalizedComponent(key, args).setStyle(component.getStyle());
		if(component.getSiblings() != null)
			result.getSiblings().addAll(component.getSiblings());
		return result;
	}

	private MutableComponent getServerLocalizedComponent(String key, Object... args){
		for(int i = 0; i < args.length; i++)
			if(args[i] instanceof TranslatableComponent translatableComponent)
				args[i] = getServerLocalizedComponent(translatableComponent.getKey(), translatableComponent.getArgs());
		return new TranslatableComponent(defaultTranslations.getOrDefault(key, key), args);
	}

	public String getDefaultTranslation(String key, Object... args){
		return I18n.get(defaultTranslations.getOrDefault(key, key), args);
	}

}
