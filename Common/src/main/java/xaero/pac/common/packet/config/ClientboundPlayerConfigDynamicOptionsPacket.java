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

package xaero.pac.common.packet.config;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.player.config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ClientboundPlayerConfigDynamicOptionsPacket extends PlayerConfigPacket {

	private static final String DEFAULT_VALUE_KEY = "d";
	private final List<PlayerConfigOptionSpec<?>> entries;

	public ClientboundPlayerConfigDynamicOptionsPacket(List<PlayerConfigOptionSpec<?>> entries){
		this.entries = entries;
	}

	public static class Codec implements BiConsumer<ClientboundPlayerConfigDynamicOptionsPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundPlayerConfigDynamicOptionsPacket> {

		private <T extends Comparable<T>> PlayerConfigOptionSpec<T> getEntry(OptionType optionType, CompoundTag entryTag, Tag defaultValueTag, ValueType<T> type, String id, String translation, String[] translationArgs, String commentTranslation, String[] commentTranslationArgs, String comment, PlayerConfigOptionCategory category){
			return optionType.buildSpec(type, entryTag)
					.setId(id)
					.setTranslation(translation, translationArgs)
					.setCommentTranslation(commentTranslation, commentTranslationArgs)
					.setDefaultValue(type.valueUntagger.apply(defaultValueTag))
					.setComment(comment)
					.setCategory(category)
					.build(null);
		}

		@Override
		public ClientboundPlayerConfigDynamicOptionsPacket apply(FriendlyByteBuf friendlyByteBuf) {
			try {
				if(friendlyByteBuf.readableBytes() > 536870912)
					return null;
				CompoundTag tag = friendlyByteBuf.readAnySizeNbt();
				if(tag == null)
					return null;
				ListTag entryListTag = tag.getList("l", Tag.TAG_COMPOUND);
				List<PlayerConfigOptionSpec<?>> entries = new ArrayList<>(entryListTag.size());
				entryListTag.forEach(t -> {
					CompoundTag entryTag = (CompoundTag) t;
					int optionTypeIndex = entryTag.getInt("ot");
					OptionType optionType = OptionType.ALL.get(optionTypeIndex);
					String id = entryTag.getString("i");
					String translation = entryTag.getString("t");
					String commentTranslation = entryTag.getString("ct");
					String comment = entryTag.getString("c");
					ListTag translationArgsTag = entryTag.getList("ta", Tag.TAG_STRING);
					String[] translationArgs = new String[translationArgsTag.size()];
					for(int i = 0; i < translationArgs.length; i++)
						translationArgs[i] = translationArgsTag.getString(i);
					ListTag commentTranslationArgsTag = entryTag.getList("cta", Tag.TAG_STRING);
					String[] commentTranslationArgs = new String[commentTranslationArgsTag.size()];
					for(int i = 0; i < commentTranslationArgs.length; i++)
						commentTranslationArgs[i] = commentTranslationArgsTag.getString(i);
					PlayerConfigOptionCategory category = PlayerConfigOptionCategory.values()[entryTag.getInt("cat")];
					Tag defaultValueTag = entryTag.get(DEFAULT_VALUE_KEY);
					PlayerConfigOptionSpec<?> entry = null;
					for(ValueType<?> valueType : ValueType.ALL.values()){
						if(valueType.typeCheck.test(defaultValueTag)){
							entry = getEntry(optionType, entryTag, defaultValueTag, valueType, id, translation, translationArgs, commentTranslation, commentTranslationArgs, comment, category);
							break;
						}
					}
					if(entry != null)
						entries.add(entry);
				});
				return new ClientboundPlayerConfigDynamicOptionsPacket(entries);
			} catch(Throwable t) {
				return null;
			}
		}

		private <T extends Comparable<T>> void handleValueAndOptionTypes(PlayerConfigOptionSpec<T> entry, CompoundTag entryTag){
			ValueType<T> valueType = getEntryValueType(entry);
			Tag valueTag = valueType.valueTagger.apply(entry.getDefaultValue());
			entryTag.put(DEFAULT_VALUE_KEY, valueTag);
			entry.getSyncOptionType().serializeExtra(entry, valueType, entryTag);
		}

		@Override
		public void accept(ClientboundPlayerConfigDynamicOptionsPacket packet, FriendlyByteBuf friendlyByteBuf) {
			CompoundTag tag = new CompoundTag();
			ListTag entryListTag = new ListTag();
			for(PlayerConfigOptionSpec<?> entry : packet.entries){
				CompoundTag entryTag = new CompoundTag();
				entryTag.putInt("ot", entry.getSyncOptionType().index);
				entryTag.putString("i", entry.getId());
				entryTag.putString("t", entry.getTranslation());
				entryTag.putString("ct", entry.getCommentTranslation());
				entryTag.putString("c", entry.getComment());
				ListTag translationArgsTag = new ListTag();
				for(String translationArg : entry.getTranslationArgs())
					translationArgsTag.add(StringTag.valueOf(translationArg));
				entryTag.put("ta", translationArgsTag);
				ListTag commentTranslationArgsTag = new ListTag();
				for(String translationArg : entry.getCommentTranslationArgs())
					commentTranslationArgsTag.add(StringTag.valueOf(translationArg));
				entryTag.put("cta", commentTranslationArgsTag);
				entryTag.putInt("cat", entry.getCategory().ordinal());
				handleValueAndOptionTypes(entry, entryTag);
				entryListTag.add(entryTag);
			}
			tag.put("l", entryListTag);
			friendlyByteBuf.writeNbt(tag);
		}
	}

	public static class ClientHandler implements Consumer<ClientboundPlayerConfigDynamicOptionsPacket> {

		@Override
		public void accept(ClientboundPlayerConfigDynamicOptionsPacket packet) {
			PlayerConfigDynamicOptions.Builder dynamicOptionsBuilder = PlayerConfigDynamicOptions.Builder.begin();
			packet.entries.forEach(dynamicOptionsBuilder::addOption);
			PlayerConfigDynamicOptions dynamicOptions = dynamicOptionsBuilder.build();
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();
			playerConfigStorageManager.setDynamicOptions(dynamicOptions);
		}

	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> ValueType<T> getEntryValueType(PlayerConfigOptionSpec<T> entry){
		return (ValueType<T>)ValueType.ALL.get(entry.getType());
	}

	private static final class ValueType<T extends Comparable<T>> {

		private static final Map<Class<?>, ValueType<?>> ALL = new HashMap<>();

		private static final ValueType<Boolean> BOOLEAN = new ValueType<>(Boolean.class, t -> t instanceof ByteTag, t -> ((ByteTag)t).getAsByte() != 0, ByteTag::valueOf);
		private static final ValueType<Integer> INT = new ValueType<>(Integer.class, t -> t instanceof IntTag, t -> ((IntTag)t).getAsInt(), IntTag::valueOf);
		private static final ValueType<Double> DOUBLE = new ValueType<>(Double.class, t -> t instanceof DoubleTag, t -> ((DoubleTag)t).getAsDouble(), DoubleTag::valueOf);
		private static final ValueType<Float> FLOAT = new ValueType<>(Float.class, t -> t instanceof FloatTag, t -> ((FloatTag)t).getAsFloat(), FloatTag::valueOf);
		private static final ValueType<String> STRING = new ValueType<>(String.class, t -> t instanceof StringTag, Tag::getAsString, StringTag::valueOf);
		private final Class<T> jType;
		private final Predicate<Tag> typeCheck;
		private final Function<Tag, T> valueUntagger;
		private final Function<T, Tag> valueTagger;

		private ValueType(Class<T> jType, Predicate<Tag> typeCheck, Function<Tag, T> valueUntagger, Function<T, Tag> valueTagger) {
			this.jType = jType;
			this.typeCheck = typeCheck;
			this.valueUntagger = valueUntagger;
			this.valueTagger = valueTagger;
			ALL.put(jType, this);
		}

	}

	public static abstract class OptionType {

		private static final Int2ObjectMap<OptionType> ALL = new Int2ObjectOpenHashMap<>();

		public static final OptionType DEFAULT = new OptionType(0){
			@Override
			public <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> option, ValueType<T> type, CompoundTag entryTag) {
			}
			@Override
			public <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag) {
				return PlayerConfigOptionSpec.FinalBuilder.begin(type.jType);
			}
		};
		public static final OptionType HEX = new OptionType(1){
			@Override
			public <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> option, ValueType<T> type, CompoundTag entryTag) {
			}
			@SuppressWarnings("unchecked")
			@Override
			public <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag) {
				return (PlayerConfigOptionSpec.Builder<T, ?>) PlayerConfigHexOptionSpec.Builder.begin();
			}
		};
		public static final OptionType RANGED = new OptionType(2){
			@Override
			public <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> o, ValueType<T> type, CompoundTag entryTag) {
				PlayerConfigRangedOptionSpec<T> option = (PlayerConfigRangedOptionSpec<T>) o;
				T minValue = option.getMinValue();
				T maxValue = option.getMaxValue();
				Tag minTag = type.valueTagger.apply(minValue);
				Tag maxTag = type.valueTagger.apply(maxValue);
				entryTag.put("min", minTag);
				entryTag.put("max", maxTag);
			}
			@Override
			public <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag) {
				PlayerConfigRangedOptionSpec.Builder<T> builder = PlayerConfigRangedOptionSpec.Builder.begin(type.jType);
				Tag minValueTag = entryTag.get("min");
				Tag maxValueTag = entryTag.get("max");
				builder.setMinValue(type.valueUntagger.apply(minValueTag));
				builder.setMaxValue(type.valueUntagger.apply(maxValueTag));
				return builder;
			}
		};
		public static final OptionType STRING = new OptionType(3){
			@Override
			public <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> o, ValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStringOptionSpec option = (PlayerConfigStringOptionSpec) o;
				entryTag.putInt("ml", option.getMaxLength());
			}
			@Override
			public <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStringOptionSpec.Builder builder = PlayerConfigStringOptionSpec.Builder.begin();
				builder.setMaxLength(entryTag.getInt("ml"));
				@SuppressWarnings("unchecked")
				PlayerConfigOptionSpec.Builder<T, ?> result = (PlayerConfigOptionSpec.Builder<T, ?>) builder;
				return result;
			}
		};
		public static final OptionType STATIC_LIST = new OptionType(4){
			@Override
			public <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> o, ValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStaticListIterationOptionSpec<T> option = (PlayerConfigStaticListIterationOptionSpec<T>) o;
				ListTag iterationListTag = new ListTag();
				for(T el : option.getList())
					iterationListTag.add(type.valueTagger.apply(el));
				entryTag.put("il", iterationListTag);
			}
			@Override
			public <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStaticListIterationOptionSpec.Builder<T> builder = PlayerConfigStaticListIterationOptionSpec.Builder.begin(type.jType);
				ListTag iterationListTag = (ListTag) entryTag.get("il");
				List<T> list = new ArrayList<>(iterationListTag.size());
				for(Tag elTag : iterationListTag)
					list.add(type.valueUntagger.apply(elTag));
				builder.setList(list);
				return builder;
			}
		};
		public static final OptionType UNSYNCABLE = new OptionType(5){
			@Override
			public <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> option, ValueType<T> type, CompoundTag entryTag) {
			}
			@Override
			public <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag) {
				return null;
			}
		};

		private final int index;

		protected OptionType(int index) {
			this.index = index;
			ALL.put(index, this);
		}

		public abstract <T extends Comparable<T>> void serializeExtra(PlayerConfigOptionSpec<T> option, ValueType<T> type, CompoundTag entryTag);
		public abstract <T extends Comparable<T>> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(ValueType<T> type, CompoundTag entryTag);

	}

}
