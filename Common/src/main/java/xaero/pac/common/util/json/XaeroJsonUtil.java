/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2025-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.util.json;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.StrictJsonParser;
import xaero.pac.OpenPartiesAndClaims;

public class XaeroJsonUtil {

    public static Component fromJson(String jsonString){
        JsonElement jsonElement = StrictJsonParser.parse(jsonString);
        RegistryOps<JsonElement> serializationContext = RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE);
        return ComponentSerialization.CODEC.parse(serializationContext, jsonElement)
                .resultOrPartial(error ->
                        OpenPartiesAndClaims.LOGGER.warn("Failed to parse json string: {};;; {}", jsonString, error)
                ).orElse(null);
    }

    public static String toJson(Component component){
        RegistryOps<JsonElement> serializationContext = RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE);
        JsonElement jsonElement = ComponentSerialization.CODEC.encodeStart(serializationContext, component)
                .resultOrPartial(error ->
                        OpenPartiesAndClaims.LOGGER.warn("Failed to encode component into json: {};;; {}", component, error)
                ).orElse(null);
        if(jsonElement == null)
            return null;
        return jsonElement.toString();
    }

}
