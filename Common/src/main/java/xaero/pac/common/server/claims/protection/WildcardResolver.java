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

package xaero.pac.common.server.claims.protection;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class WildcardResolver {

	private static final Pattern WILDCARD_FORMAT = Pattern.compile("[\\(\\)\\*\\|a-zA-Z0-9_\\-\\.\\/\\:]+");
	private static final Pattern WILDCARD_TO_REGEX_REPLACE_PATTERN = Pattern.compile("([\\.\\-\\:\\/])");
	private static final Function<String, String> WILDCARD_TO_REGEX = s -> WILDCARD_TO_REGEX_REPLACE_PATTERN.matcher(s).replaceAll("\\\\$1").replace("*", ".*");

	public <T> List<T> resolveResourceLocations(Function<ResourceLocation, T> getter, Iterable<T> iterable, Function<T, ResourceLocation> keyGetter, String string){
		boolean validResourceLocation = isValidResourceLocation(string) && !containsWildcardCharacters(string);//additional char check because of mods (e.g. AAA Particles)
		if(validResourceLocation) {
			T object = getter.apply(ResourceLocation.parse(string));
			return object == null ? List.of() : List.of(object);
		}
		if(!WILDCARD_FORMAT.matcher(string).matches()){
			OpenPartiesAndClaims.LOGGER.error("Invalid resource location or wildcard in the server config file: " + string + ". Additional characters allowed for wildcards are (, ), | and *.");
			return null;
		}
		ArrayList<T> result = new ArrayList<>();
		try {
			Pattern regexPattern = Pattern.compile(WILDCARD_TO_REGEX.apply(string));
			for (T element : iterable) {
				ResourceLocation key = keyGetter.apply(element);
				if (regexPattern.matcher(key.toString()).matches())
					result.add(element);
			}
		} catch(PatternSyntaxException pse){
			OpenPartiesAndClaims.LOGGER.error("Invalid wildcard format in the server config file: " + string + ". Additional characters allowed for wildcards are (, ), | and *.", pse);
			return null;
		}
		return result;
	}

	private boolean isValidResourceLocation(String string){
		int separatorIndex = string.indexOf(':');
		String path = string.substring(separatorIndex + 1);
		if(!ResourceLocation.isValidPath(path))
			return false;
		if(separatorIndex == -1)
			return true;
		return ResourceLocation.isValidNamespace(string.substring(0, separatorIndex));
	}

	private boolean containsWildcardCharacters(String string){
		return string.contains("(") || string.contains(")") || string.contains("|") || string.contains("*");
	}

}
