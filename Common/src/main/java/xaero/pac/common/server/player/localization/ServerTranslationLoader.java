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

import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.config.ServerConfig;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerTranslationLoader {

	public Map<String, String> loadFromResources(MinecraftServer server){
		Map<String, String> result = new HashMap<>();
		try {
			Resource enUSLanguageFileResource = server.getResourceManager().getResourceOrThrow(new ResourceLocation(OpenPartiesAndClaims.MOD_ID, "lang/en_us.json"));

			try(BufferedInputStream inputStream = new BufferedInputStream(enUSLanguageFileResource.open())){
				Language.loadFromJson(inputStream, result::put);
			}
			String configuredLanguage = ServerConfig.CONFIG.defaultLanguage.get();
			if(!configuredLanguage.equalsIgnoreCase("en_us")) {
				Resource languageFileResource = server.getResourceManager().getResourceOrThrow(new ResourceLocation(OpenPartiesAndClaims.MOD_ID, "lang/" + configuredLanguage + ".json"));
				try (BufferedInputStream inputStream = new BufferedInputStream(languageFileResource.open())) {
					Language.loadFromJson(inputStream, result::put);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error loading the default OPAC server localization!", e);
		}
		return result;
	}

}
