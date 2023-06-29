/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.mods.prometheus;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import earth.terrarium.prometheus.api.roles.options.RoleOption;
import earth.terrarium.prometheus.api.roles.options.RoleOptionSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class OPACOptions implements RoleOption<OPACOptions> {

	protected static final ResourceLocation resourceLocation = new ResourceLocation(OpenPartiesAndClaims.MOD_ID, "permissions");

	protected final Map<IPermissionNodeAPI<?>, Object> values;

	public OPACOptions(){
		values = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(IPermissionNodeAPI<T> node){
		return (T)values.get(node);
	}

	@SuppressWarnings("unchecked")
	public <T> T getValueCast(IPermissionNodeAPI<?> node){
		return (T)getValue(node);
	}

	public <T> void setValue(IPermissionNodeAPI<T> node, T value){
		values.put(node, value);
	}

	public void setValueCast(IPermissionNodeAPI<?> node, Object value){
		if(value != null && !node.getType().isAssignableFrom(value.getClass()))
			throw new IllegalArgumentException(node.getType() + " is not assignable from " + value.getClass());
		values.put(node, value);
	}

	@Override
	public RoleOptionSerializer<OPACOptions> serializer() {
		return SERIALIZER;
	}

	private static <T> BiFunction<DynamicOps<T>, Object, T> getValueEncoder(Class<?> type){
		if(type == Boolean.class) return (ops, o) -> ops.createBoolean((Boolean)o);
		if(type == Integer.class) return (ops, o) -> ops.createInt((Integer)o);
		if(type == Double.class) return (ops, o) -> ops.createDouble((Double)o);
		if(type == Float.class) return (ops, o) -> ops.createFloat((Float)o);
		if(type == Long.class) return (ops, o) -> ops.createLong((Long)o);
		if(type == Short.class) return (ops, o) -> ops.createShort((Short)o);
		if(type == Byte.class) return (ops, o) -> ops.createByte((Byte)o);
		if(type == String.class) return (ops, o) -> ops.createString((String)o);
		if(type == Component.class) return (ops, o) -> ops.createString(Component.Serializer.toJson((Component)o));
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T, V> BiFunction<DynamicOps<T>, T, Optional<V>> getValueDecoder(Class<V> type){
		if(type == Boolean.class) return (ops, o) -> (Optional<V>)ops.getBooleanValue(o).result();
		if(type == Integer.class) return (ops, o) -> (Optional<V>)ops.getNumberValue(o).result().map(Number::intValue);
		if(type == Double.class) return (ops, o) -> (Optional<V>)ops.getNumberValue(o).result().map(Number::doubleValue);
		if(type == Float.class) return (ops, o) -> (Optional<V>)ops.getNumberValue(o).result().map(Number::floatValue);
		if(type == Long.class) return (ops, o) -> (Optional<V>)ops.getNumberValue(o).result().map(Number::longValue);
		if(type == Short.class) return (ops, o) -> (Optional<V>)ops.getNumberValue(o).result().map(Number::shortValue);
		if(type == Byte.class) return (ops, o) -> (Optional<V>)ops.getNumberValue(o).result().map(Number::byteValue);
		if(type == String.class) return (ops, o) -> (Optional<V>)ops.getStringValue(o).result();
		if(type == Component.class) return (ops, o) -> (Optional<V>)ops.getStringValue(o).result().map(Component.Serializer::fromJson);
		return null;
	}

	protected static Encoder<OPACOptions> getEncoder(){
		return new Encoder<>() {
			@Override
			public <T> DataResult<T> encode(OPACOptions input, DynamicOps<T> ops, T prefix) {
				T map = ops.createMap(new LinkedHashMap<>());
				for (Map.Entry<IPermissionNodeAPI<?>, Object> entry : input.values.entrySet()){
					IPermissionNodeAPI<?> node = entry.getKey();
					Object value = entry.getValue();
					T encodedNode = ops.createString(node.getDefaultNodeString());
					T encodedValue = encodeValue(ops, node, value);
					if(encodedValue == null)
						continue;
					map = ops.mergeToMap(map, encodedNode, encodedValue).result().orElse(map);
				}
				return DataResult.success(map);
			}
		};
	}

	protected static Decoder<OPACOptions> getDecoder(Supplier<OPACOptions> factory){
		return new Decoder<>() {
			@Override
			public <T> DataResult<Pair<OPACOptions, T>> decode(DynamicOps<T> ops, T input) {
				OPACOptions options = factory.get();
				ops.getMapEntries(input).get().ifLeft(c -> c.accept((encodedNode, encodedValue) -> {
					String key = ops.getStringValue(encodedNode).result().orElse(null);
					if(key == null)
						return;
					IPermissionNodeAPI<?> node = UsedPermissionNodes.ALL.get(key);
					if(node == null)
						return;
					Optional<?> result = decodeValue(ops, node, encodedValue);
					if(result.isEmpty()) {
						options.values.remove(node);
						return;
					}
					options.values.put(node, result.get());
				}));
				return DataResult.success(Pair.of(options, input));
			}
		};
	}

	private static <T> T encodeValue(DynamicOps<T> ops, IPermissionNodeAPI<?> node, Object value){
		BiFunction<DynamicOps<T>, Object, T> valueEncoder = getValueEncoder(node.getType());
		if(valueEncoder == null)
			return null;
		return valueEncoder.apply(ops, value);
	}

	private static <T, V> Optional<V> decodeValue(DynamicOps<T> ops, IPermissionNodeAPI<V> node, T encodedValue){
		BiFunction<DynamicOps<T>, T, Optional<V>> decoder = getValueDecoder(node.getType());
		if(decoder == null)
			return Optional.empty();
		return decoder.apply(ops, encodedValue);
	}

	public static final RoleOptionSerializer<OPACOptions> SERIALIZER = RoleOptionSerializer.of(
			resourceLocation,
			1,
			Codec.of(getEncoder(), getDecoder(OPACOptions::new)),
			new OPACOptions()
	);

}
