/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config;

import com.google.common.collect.Lists;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.player.config.group.BuiltInPlayerConfigGroupNames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerConfigOptionValueTypes {

	private static final Map<String, PlayerConfigOptionValueType<?>> BASIC_TYPES = new HashMap<>();

	public static final PlayerConfigOptionValueType<Boolean> BOOLEAN = PlayerConfigOptionValueType.FinalBuilder
			.begin(Boolean.class)
			.setId("b")
			.setSyncEncoder(b -> ByteTag.valueOf((byte)(b ? 1 : 0)))
			.setSyncDecoder(tag -> {
				if(!(tag instanceof ByteTag byteTag))
					return null;
				return byteTag.getAsByte() == 1;
			})
			.setStringWriter(b -> b ? "true" : "false")
			.setComponentWriter(b -> b ? PlayerConfigConstants.ON_COMPONENT : PlayerConfigConstants.OFF_COMPONENT)
			.setStringParser(str -> str.equals("true") || str.equals("on") || str.equals("1") || str.equals("+"))
			.setDefaultCommandSuggestions(Lists.newArrayList("false", "true", "off", "on", "0", "1", "-", "+"))
			.setAdder((b1, b2) -> b1 != b2)//makes it usable for toggling
			.setSubtracter((b1, b2) -> b1 != b2)//makes it usable for toggling
			.build(BASIC_TYPES);

	public static final PlayerConfigOptionValueType<Integer> INTEGER = PlayerConfigOptionValueType.FinalBuilder
			.begin(Integer.class)
			.setId("i")
			.setSyncEncoder(IntTag::valueOf)
			.setSyncDecoder(tag -> {
				if(!(tag instanceof IntTag intTag))
					return null;
				return intTag.getAsInt();
			})
			.setStringWriter(i -> "" + i)
			.setStringParser(Integer::parseInt)
			.setAdder(Integer::sum)
			.setSubtracter((i1, i2) -> i1 - i2)
			.build(BASIC_TYPES);

	public static final PlayerConfigOptionValueType<Integer> HEX_INTEGER = PlayerConfigOptionValueType.FinalBuilder
			.begin(Integer.class)
			.setId("h")
			.setSyncEncoder(IntTag::valueOf)
			.setSyncDecoder(tag -> {
				if(!(tag instanceof IntTag intTag))
					return null;
				return intTag.getAsInt();
			})
			.setStringWriter(i -> Integer.toUnsignedString(i, 16).toUpperCase())
			.setStringParser(s -> Integer.parseUnsignedInt(s, 16))
			.setAdder(Integer::sum)
			.setSubtracter((i1, i2) -> i1 - i2)
			.build(BASIC_TYPES);

	public static final PlayerConfigOptionValueType<Double> DOUBLE = PlayerConfigOptionValueType.FinalBuilder
			.begin(Double.class)
			.setId("d")
			.setSyncEncoder(DoubleTag::valueOf)
			.setSyncDecoder(tag -> {
				if(!(tag instanceof DoubleTag doubleTag))
					return null;
				return doubleTag.getAsDouble();
			})
			.setStringWriter(d -> "" + d)
			.setStringParser(Double::parseDouble)
			.setAdder(Double::sum)
			.setSubtracter((d1, d2) -> d1 - d2)
			.build(BASIC_TYPES);

	public static final PlayerConfigOptionValueType<Float> FLOAT = PlayerConfigOptionValueType.FinalBuilder
			.begin(Float.class)
			.setId("f")
			.setSyncEncoder(FloatTag::valueOf)
			.setSyncDecoder(tag -> {
				if(!(tag instanceof FloatTag floatTag))
					return null;
				return floatTag.getAsFloat();
			})
			.setStringWriter(f -> "" + f)
			.setStringParser(Float::parseFloat)
			.setAdder(Float::sum)
			.setSubtracter((f1, f2) -> f1 - f2)
			.build(BASIC_TYPES);

	public static final PlayerConfigOptionValueType<String> STRING = PlayerConfigOptionValueType.FinalBuilder
			.begin(String.class)
			.setId("s")
			.setSyncEncoder(StringTag::valueOf)
			.setSyncDecoder(tag -> {
				if(!(tag instanceof StringTag stringTag))
					return null;
				return stringTag.getAsString();
			})
			.setStringWriter(s -> s)
			.setStringParser(s -> s)
			.setShouldDisplayInQuotes(true)
			.setAdder((s1, s2) -> s1 + s2)
			.setSubtracter((s1, s2) -> s1.replace(s2, ""))
			.build(BASIC_TYPES);

	public static final PlayerConfigOptionValueType<String> GROUP_ID = PlayerConfigOptionValueType.FinalBuilder
			.begin(String.class)
			.setId("g")
			.setSyncEncoder(StringTag::valueOf)
			.setSyncDecoder(tag -> {
				if(!(tag instanceof StringTag stringTag))
					return null;
				return stringTag.getAsString();
			})
			.setStringWriter(s -> s)
			.setStringParser(s -> s)
			.setComponentWriter(groupId -> {
				Component builtInGroupName = BuiltInPlayerConfigGroupNames.get(groupId);
				if(builtInGroupName != null)
					return builtInGroupName;
				return Component.literal(groupId);
			})
			.build(BASIC_TYPES);

	public static <T> PlayerConfigOptionValueType<List<T>> getListType(PlayerConfigOptionValueType<T> elementType){
		return PlayerConfigOptionListValueType.Builder.<T>begin()
				.setElementType(elementType)
				.build(null);
	}

	public static PlayerConfigOptionValueType<?> withId(String id){
		if(id.startsWith(PlayerConfigOptionListValueType.ID_PREFIX))
			return getListType(withId(id.substring(PlayerConfigOptionListValueType.ID_PREFIX.length())));
		return BASIC_TYPES.get(id);
	}

}
