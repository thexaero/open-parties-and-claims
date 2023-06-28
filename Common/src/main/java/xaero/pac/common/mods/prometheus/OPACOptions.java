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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class OPACOptions {

	protected static final ResourceLocation resourceLocation = new ResourceLocation(OpenPartiesAndClaims.MOD_ID, "permissions");

	protected final Map<IPermissionNodeAPI, Object> values;

	public OPACOptions(){
		values = new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public <T> T getValueCast(IPermissionNodeAPI node){
		return (T)values.get(node);
	}

	public void setValue(IPermissionNodeAPI node, Object value){
		if(node.isInt() && value instanceof Integer || value instanceof Boolean) {
			values.put(node, value);
			return;
		}
		values.remove(node);
	}

	protected static <O extends OPACOptions> Encoder<O> getEncoder(){
		return new Encoder<>() {
			@Override
			public <T> DataResult<T> encode(O input, DynamicOps<T> ops, T prefix) {
				T map = ops.createMap(new LinkedHashMap<>());
				for (Map.Entry<IPermissionNodeAPI, Object> entry : input.values.entrySet()){
					IPermissionNodeAPI node = entry.getKey();
					Object value = entry.getValue();
					T encodedNode = ops.createString(node.getDefaultNodeString());
					T encodedValue = node.isInt() ? ops.createInt((Integer)value) : ops.createBoolean((Boolean)value);
					map = ops.mergeToMap(map, encodedNode, encodedValue).result().orElse(map);
				}
				return DataResult.success(map);
			}
		};
	}

	protected static <O extends OPACOptions> Decoder<O> getDecoder(Supplier<O> factory){
		return new Decoder<>() {
			@Override
			public <T> DataResult<Pair<O, T>> decode(DynamicOps<T> ops, T input) {
				O options = factory.get();
				ops.getMapEntries(input).get().ifLeft(c -> c.accept((encodedNode, encodedValue) -> {
					String key = ops.getStringValue(encodedNode).result().orElse(null);
					if(key == null)
						return;
					IPermissionNodeAPI node = UsedPermissionNodes.ALL.get(key);
					if(node == null)
						return;
					Object value;
					if(node.isInt()){
						Optional<Number> result = ops.getNumberValue(encodedValue).result();
						if(result.isEmpty()) {
							options.values.remove(node);
							return;
						}
						value = result.get().intValue();
					} else
						value = ops.getBooleanValue(encodedValue).result().orElse(false);
					options.values.put(node, value);
				}));
				return DataResult.success(Pair.of(options, input));
			}
		};
	}

}
