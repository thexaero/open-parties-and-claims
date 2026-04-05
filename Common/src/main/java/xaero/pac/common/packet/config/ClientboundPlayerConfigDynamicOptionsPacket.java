/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.player.config.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ClientboundPlayerConfigDynamicOptionsPacket extends PlayerConfigPacket {

	private static final String DEFAULT_VALUE_KEY = "d";
	private final List<PlayerConfigOptionSpec<?>> entries;

	public ClientboundPlayerConfigDynamicOptionsPacket(List<PlayerConfigOptionSpec<?>> entries){
		this.entries = entries;
	}

	public static class Codec implements BiConsumer<ClientboundPlayerConfigDynamicOptionsPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundPlayerConfigDynamicOptionsPacket> {

		private <T> PlayerConfigOptionSpec<T> getEntry(
				OptionType optionType,
				CompoundTag entryTag,
				Tag defaultValueTag,
				PlayerConfigOptionValueType<T> valueType,
				String id,
				String translation,
				String[] translationArgs,
				String commentTranslation,
				String[] commentTranslationArgs,
				String comment,
				PlayerConfigOptionCategory category
		){
			PlayerConfigOptionSpec.Builder<T, ?> builder = optionType.buildSpec(valueType, entryTag);
			if(builder == null)
				return null;
			return builder.setId(id)
					.setTranslation(translation, translationArgs)
					.setCommentTranslation(commentTranslation, commentTranslationArgs)
					.setDefaultValue(valueType.getSyncDecoder().apply(defaultValueTag))
					.setComment(comment)
					.setCategory(category)
					.setDynamic(true)
					.build(null);
		}

		@Override
		public ClientboundPlayerConfigDynamicOptionsPacket apply(FriendlyByteBuf friendlyByteBuf) {
			try {
				if(friendlyByteBuf.readableBytes() > 536870912)
					return null;
				CompoundTag tag = (CompoundTag) friendlyByteBuf.readNbt(NbtAccounter.unlimitedHeap());
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
					String valueTypeId = entryTag.getString("vt");
					PlayerConfigOptionValueType<?> valueType = PlayerConfigOptionValueTypes.withId(valueTypeId);
					PlayerConfigOptionSpec<?> entry = getEntry(
							optionType, entryTag, defaultValueTag, valueType,
							id, translation, translationArgs, commentTranslation,
							commentTranslationArgs, comment, category
					);
					if(entry != null)
						entries.add(entry);
				});
				return new ClientboundPlayerConfigDynamicOptionsPacket(entries);
			} catch(Throwable t) {
				return null;
			}
		}

		private <T> void handleValueAndOptionTypes(PlayerConfigOptionSpec<T> entry, CompoundTag entryTag){
			PlayerConfigOptionValueType<T> valueType = entry.getValueType();
			Tag valueTag = valueType.getSyncEncoder().apply(entry.getDefaultValue());
			entryTag.putString("vt", entry.getValueType().getId());
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

	public static abstract class OptionType {

		private static final Int2ObjectMap<OptionType> ALL = new Int2ObjectOpenHashMap<>();

		public static final OptionType DEFAULT = new OptionType(0){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> option, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
			}
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				return PlayerConfigOptionSpec.FinalBuilder.begin(type);
			}
		};
		public static final OptionType HEX = new OptionType(1){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> option, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
			}
			@SuppressWarnings("unchecked")
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				return (PlayerConfigOptionSpec.Builder<T, ?>) PlayerConfigHexOptionSpec.Builder.begin();
			}
		};
		public static final OptionType RANGED = new OptionType(2){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> o, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				PlayerConfigRangedOptionSpec<T> option = (PlayerConfigRangedOptionSpec<T>) o;
				T minValue = option.getMinValue();
				T maxValue = option.getMaxValue();
				Tag minTag = type.getSyncEncoder().apply(minValue);
				Tag maxTag = type.getSyncEncoder().apply(maxValue);
				entryTag.put("min", minTag);
				entryTag.put("max", maxTag);
			}
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				PlayerConfigRangedOptionSpec.Builder<T> builder = PlayerConfigRangedOptionSpec.Builder.begin(type);
				Tag minValueTag = entryTag.get("min");
				Tag maxValueTag = entryTag.get("max");
				builder.setMinValue(type.getSyncDecoder().apply(minValueTag));
				builder.setMaxValue(type.getSyncDecoder().apply(maxValueTag));
				return builder;
			}
		};
		public static final OptionType STRING = new OptionType(3){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> o, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStringOptionSpec option = (PlayerConfigStringOptionSpec) o;
				entryTag.putInt("ml", option.getMaxLength());
			}
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStringOptionSpec.Builder builder = PlayerConfigStringOptionSpec.Builder.begin();
				builder.setMaxLength(entryTag.getInt("ml"));
				@SuppressWarnings("unchecked")
				PlayerConfigOptionSpec.Builder<T, ?> result = (PlayerConfigOptionSpec.Builder<T, ?>) builder;
				return result;
			}
		};
		public static final OptionType STATIC_LIST = new OptionType(4){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> o, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStaticListIterationOptionSpec<T> option = (PlayerConfigStaticListIterationOptionSpec<T>) o;
				ListTag iterationListTag = new ListTag();
				for(T el : option.getList())
					iterationListTag.add(type.getSyncEncoder().apply(el));
				entryTag.put("il", iterationListTag);
			}
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				PlayerConfigStaticListIterationOptionSpec.Builder<T> builder = PlayerConfigStaticListIterationOptionSpec.Builder.begin(type);
				ListTag iterationListTag = (ListTag) entryTag.get("il");
				List<T> list = new ArrayList<>(iterationListTag.size());
				for(Tag elTag : iterationListTag)
					list.add(type.getSyncDecoder().apply(elTag));
				builder.setList(list);
				return builder;
			}
		};
		public static final OptionType GROUP_ITERATION = new OptionType(5){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> o, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
			}
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				if(type != PlayerConfigOptionValueTypes.GROUP_ID)
					return null;
				PlayerConfigPlayerGroupOptionSpec.Builder builder = PlayerConfigPlayerGroupOptionSpec.Builder.begin();
				@SuppressWarnings("unchecked")
				PlayerConfigOptionSpec.Builder<T, ?> castBuilder = (PlayerConfigOptionSpec.Builder<T, ?>) builder;
				return castBuilder;
			}
		};
		public static final OptionType UNSYNCABLE = new OptionType(6){
			@Override
			public <T> void serializeExtra(PlayerConfigOptionSpec<T> option, PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
			}
			@Override
			public <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag) {
				return null;
			}
		};

		private final int index;

		protected OptionType(int index) {
			this.index = index;
			ALL.put(index, this);
		}

		public abstract <T> void serializeExtra(PlayerConfigOptionSpec<T> option, PlayerConfigOptionValueType<T> type, CompoundTag entryTag);
		public abstract <T> PlayerConfigOptionSpec.Builder<T, ?> buildSpec(PlayerConfigOptionValueType<T> type, CompoundTag entryTag);

	}

}
