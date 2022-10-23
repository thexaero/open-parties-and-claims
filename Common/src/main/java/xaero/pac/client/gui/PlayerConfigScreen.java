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

package xaero.pac.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.widget.value.BooleanValueHolder;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.client.player.config.PlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.sub.PlayerSubConfigClientStorage;
import xaero.pac.common.misc.ListFactory;
import xaero.pac.common.server.player.config.*;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.dynamic.PlayerConfigExceptionDynamicOptionsLoader;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class PlayerConfigScreen extends WidgetListScreen {

	private static final Object NULL_PLACEHOLDER = new Comparable<Object>() {
		@Override
		public int compareTo(@Nonnull Object o) {
			return 0;
		}
	};
	public static final Component SYNCING_IN_PROGRESS = new TranslatableComponent("gui.xaero_pac_ui_player_config_syncing");
	public static final Component BEING_DELETED = new TranslatableComponent("gui.xaero_pac_ui_player_config_being_deleted");
	private final BiConsumer<PlayerConfigScreen, Button> refreshHandler;
	private Button refreshButton;
	private final PlayerConfigClientStorage data;
	private final PlayerConfigClientStorage optionValueSourceData;
	private final boolean shouldWaitForData;
	private final boolean beingDeletedStateOnOpen;

	private PlayerConfigScreen(List<WidgetListElement<?>> elements, List<EditBox> tickableBoxes, BiConsumer<PlayerConfigScreen, Button> refreshHandler, Screen escape, Screen parent, Component title, PlayerConfigClientStorage data, PlayerConfigClientStorage optionValueSourceData, boolean shouldWaitForData, boolean beingDeletedStateOnOpen) {
		super(elements, tickableBoxes, escape, parent, title);
		this.refreshHandler = refreshHandler;
		this.data = data;
		this.optionValueSourceData = optionValueSourceData;
		this.shouldWaitForData = shouldWaitForData;
		this.beingDeletedStateOnOpen = beingDeletedStateOnOpen;
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(refreshButton = new Button(5, 5, 60, 20, new TranslatableComponent("gui.xaero_pac_ui_player_config_refresh"), b -> refreshHandler.accept(this, b)));
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		super.render(poseStack, mouseX, mouseY, partial);
	}

	@Override
	protected void renderPreDropdown(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		super.renderPreDropdown(poseStack, mouseX, mouseY, partial);
		if(shouldWaitForData){
			if(!data.isSyncInProgress())
				refreshButton.onPress();
			else
				drawCenteredString(poseStack, font, SYNCING_IN_PROGRESS, width / 2, height / 6 + 64, -1);
		}
		if(beingDeletedStateOnOpen != optionValueSourceData.isBeingDeleted())
			refreshButton.onPress();
		else if(optionValueSourceData.isBeingDeleted())
			drawCenteredString(poseStack, font, BEING_DELETED, width / 2, height / 6 + 64, -1);
	}

	public static TextComponent getUICommentForOption(IPlayerConfigOptionSpecAPI<?> option){
		String commentTranslated = I18n.get(option.getCommentTranslation(), (Object[]) option.getCommentTranslationArgs());
		if(commentTranslated.equals("default"))
			commentTranslated = option.getComment();
		if(option.getTooltipPrefix() != null)
			commentTranslated = option.getTooltipPrefix() + "\n" + commentTranslated;
		return new TextComponent(commentTranslated);
	}

	public final static class Builder {
		
		private final ListFactory listFactory;
		private IPlayerConfigClientStorageManager<?> manager;
		private PlayerConfigClientStorage data;
		private PlayerConfigClientStorage defaultPlayerConfigData;
		private PlayerConfigClientStorage mainPlayerConfigData;
		private Screen escape;
		private Screen parent;
		private Component title;
		private String otherPlayerName;
		
		private Builder(ListFactory listFactory) {
			super();
			this.listFactory = listFactory;
		}

		public Builder setDefault() {
			setManager(null);
			setEscape(null);
			setParent(null);
			setData(null);
			setTitle(null);
			setMainPlayerConfigData(null);
			return this;
		}

		public Builder setManager(IPlayerConfigClientStorageManager<?> manager) {
			this.manager = manager;
			return this;
		}

		public Builder setData(PlayerConfigClientStorage data) {
			this.data = data;
			return this;
		}
		
		public Builder setDefaultPlayerConfigData(PlayerConfigClientStorage defaultPlayerConfigData) {
			this.defaultPlayerConfigData = defaultPlayerConfigData;
			return this;
		}

		public Builder setMainPlayerConfigData(PlayerConfigClientStorage mainPlayerConfigData) {
			this.mainPlayerConfigData = mainPlayerConfigData;
			return this;
		}

		public Builder setEscape(Screen escape) {
			this.escape = escape;
			return this;
		}
		
		public Builder setParent(Screen parent) {
			this.parent = parent;
			return this;
		}
		
		public Builder setTitle(Component title) {
			this.title = title;
			return this;
		}
		
		public Builder setOtherPlayerName(String otherPlayerName) {
			this.otherPlayerName = otherPlayerName;
			return this;
		}

		private <T extends Comparable<T>> T getOptionValue(PlayerConfigStringableOptionClientStorage<T> option){
			T value;
			if(option.isDefaulted() && data.getType() == PlayerConfigType.PLAYER)
				value = defaultPlayerConfigData.getOptionStorage(option.getOption()).getValue();
			else
				value = option.getValue();
			return value;
		}

		private <HT, T extends Comparable<T>> CycleButton.OnValueChange<HT> getRegularValueChangeListener(SimpleValueWidgetListElement.Final<T> el, PlayerConfigStringableOptionClientStorage<T> option, Function<HT, T> holderToValue, PlayerConfigClientStorage data) {
			return (b, vh) -> {
				if(!option.isMutable())
					return;
				//on value change
				T v = holderToValue.apply(vh);
				if(v == NULL_PLACEHOLDER)
					v = null;
				el.setDraftValue(v);
				option.setCastValue(v);
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().syncToServer(data, option);
			};
		}

		private <HT, T extends Comparable<T>> BiFunction<SimpleValueWidgetListElement.Final<T>, Vec3i, AbstractWidget> getIterationWidgetSupplierForValues(List<HT> values, PlayerConfigStringableOptionClientStorage<T> option, int elementWidth, int elementHeight, Component optionTitle, Function<T, HT> valueToHolder, Function<HT, T> holderToValue, PlayerConfigClientStorage data){
			return (el, xy) -> CycleButton.<HT>builder(v -> {
						Component defaultDisplay = option.getOption().getValueDisplayName(holderToValue.apply(v));
						if(option.getType() == Integer.class){
							String translationKey = option.getTranslation() + "_" + defaultDisplay.getString();
							String translatedText = I18n.get(translationKey);
							if(!translatedText.equals("default") && !translatedText.equals(translationKey))
								return new TranslatableComponent(translationKey);
						}
						return defaultDisplay;
					})
					.withValues(values)
					.withInitialValue(valueToHolder.apply(getOptionValue(option)))
					.create(xy.getX(), xy.getY(), elementWidth, elementHeight, optionTitle, getRegularValueChangeListener(el, option, holderToValue, data));
		}

		private <T extends Comparable<T>> BiFunction<SimpleValueWidgetListElement.Final<T>, Vec3i, AbstractWidget> getIterationWidgetSupplier(PlayerConfigStringableOptionClientStorage<T> option, int elementWidth, int elementHeight, Component optionTitle, T currentValue, PlayerConfigClientStorage data){
			PlayerConfigClientStorage valueSourceConfig;
			if(option.isDefaulted() && data.getType() == PlayerConfigType.PLAYER)
				valueSourceConfig = defaultPlayerConfigData;
			else
				valueSourceConfig = data;
			List<T> values;
			if(option.getOption() instanceof PlayerConfigListIterationOptionSpec<T> listIterationOptionSpec) {
				values = listIterationOptionSpec.getClientSideListGetter().apply(valueSourceConfig);
				boolean staticProtectionLevelOption = option.getOption() instanceof PlayerConfigStaticListIterationOptionSpec iterationOptionSpec && iterationOptionSpec.getList() == PlayerConfig.PROTECTION_LEVELS;//won't detect dynamic ones because their option list is recreated
				if(values == null)
					values = Lists.newArrayList(currentValue);
				else if(data.getType() != PlayerConfigType.PLAYER && data.getType() != PlayerConfigType.DEFAULT_PLAYER &&
						(option.getId().startsWith(PlayerConfigExceptionDynamicOptionsLoader.OPTION_ROOT) || staticProtectionLevelOption)) {
					boolean enablesProtection = option.getId().contains("." + PlayerConfigExceptionDynamicOptionsLoader.BARRIER + ".") || staticProtectionLevelOption;
					values = Lists.newArrayList(
							values.get(0),
							values.get(enablesProtection ? 1 : values.size() - 1)
					);
				}
				else
					values = Lists.newArrayList(values);
			} else
				values = Lists.newArrayList(currentValue);
			@SuppressWarnings("unchecked")
			T nullPlaceholder = (T) NULL_PLACEHOLDER;
			if(data instanceof PlayerSubConfigClientStorage) {
				values.add(0, nullPlaceholder);
			}
			return getIterationWidgetSupplierForValues(values, option, elementWidth, elementHeight, optionTitle, v -> v == null ? nullPlaceholder : v, h -> h == NULL_PLACEHOLDER ? null : h, data);
		}

		private BiFunction<SimpleValueWidgetListElement.Final<Boolean>, Vec3i, AbstractWidget> getOnOffWidgetSupplier(PlayerConfigStringableOptionClientStorage<Boolean> option, int elementWidth, int elementHeight, Component optionTitle, BooleanValueHolder currentValue, PlayerConfigClientStorage data){
			List<BooleanValueHolder> values = Lists.newArrayList(BooleanValueHolder.FALSE, BooleanValueHolder.TRUE);
			if(data instanceof PlayerSubConfigClientStorage)
				values.add(0, BooleanValueHolder.NULL);
			return getIterationWidgetSupplierForValues(values, option, elementWidth, elementHeight, optionTitle, BooleanValueHolder::of, BooleanValueHolder::getValue, data);
		}

		private <T extends Comparable<T>> SimpleValueWidgetListElement<T, ?> createIterationWidgetListElement(
				PlayerConfigStringableOptionClientStorage<T> option, int elementWidth, int elementHeight,
				Component optionTitle, List<FormattedCharSequence> tooltip, PlayerConfigClientStorage data){
			T value = getOptionValue(option);
			BiFunction<SimpleValueWidgetListElement.Final<T>, Vec3i, AbstractWidget> widgetSupplier = getIterationWidgetSupplier(option, elementWidth, elementHeight, optionTitle, value, data);
			return SimpleValueWidgetListElement.FinalBuilder.<T>begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setWidgetSupplier(widgetSupplier)
					.setTooltip(tooltip)
					.setMutable(option.isMutable())
					.setStartValue(value)
					.build();
		}

		private SimpleValueWidgetListElement<Boolean, ?> createOnOffWidgetListElement(
				PlayerConfigStringableOptionClientStorage<Boolean> option, int elementWidth, int elementHeight,
				Component optionTitle, List<FormattedCharSequence> tooltip, PlayerConfigClientStorage data){
			Boolean value = getOptionValue(option);
			BiFunction<SimpleValueWidgetListElement.Final<Boolean>, Vec3i, AbstractWidget> widgetSupplier = getOnOffWidgetSupplier(option, elementWidth, elementHeight, optionTitle, BooleanValueHolder.of(value), data);
			return SimpleValueWidgetListElement.FinalBuilder.<Boolean>begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setWidgetSupplier(widgetSupplier)
					.setTooltip(tooltip)
					.setMutable(option.isMutable())
					.setStartValue(value)
					.build();
		}

		private WidgetListElement<?> createSubConfigWidgetListElement(int elementWidth, int elementHeight, List<String> subConfigs, int indexOfSelected){
			List<FormattedCharSequence> tooltip = Minecraft.getInstance().font.split(new TranslatableComponent("gui.xaero_pac_ui_sub_config_dropdown_tooltip"), 200);
			return DropdownWidgetListElement.Builder.<String>begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setTooltip(tooltip)
					.setMutable(true)
					.setOptions(subConfigs)
					.setStartIndex(indexOfSelected)
					.setTitle(new TranslatableComponent("gui.xaero_pac_ui_sub_config_dropdown"))
					.setValueChangeConsumer(v-> {
						data.setSelectedSubConfig(v);
						PlayerConfigScreen recreatedScreen = build();
						Minecraft.getInstance().setScreen(recreatedScreen);
						recreatedScreen.changeFocus(true);//works while the sub-config menu is the first element
					})
					.build();
		}

		private void addSubConfigControls(List<WidgetListElement<?>> elements, int elementWidth, int elementHeight){
			Minecraft minecraft = Minecraft.getInstance();
			List<String> subConfigs = data.getSubConfigIds();
			if(subConfigs.isEmpty())
				throw new IllegalStateException();
			PlayerConfigClientStorage usedSubConfigSyncDest;
			PlayerConfigStringableOptionClientStorage<String> usedSubConfigOptionStorage;
			if(data.getType() == PlayerConfigType.SERVER){
				usedSubConfigSyncDest = mainPlayerConfigData;
				usedSubConfigOptionStorage = mainPlayerConfigData.getOptionStorage(PlayerConfigOptions.USED_SERVER_SUBCLAIM);
			} else {
				usedSubConfigSyncDest = data;
				usedSubConfigOptionStorage = data.getOptionStorage(PlayerConfigOptions.USED_SUBCLAIM);
			}

			String selected = data.getSelectedSubConfig();
			int indexOfSelectedSub = -1;
			if(selected != null)
				indexOfSelectedSub = subConfigs.indexOf(selected);
			if(indexOfSelectedSub < 0)
				indexOfSelectedSub = subConfigs.indexOf(usedSubConfigOptionStorage.getValue());
			if(indexOfSelectedSub < 0)
				indexOfSelectedSub = subConfigs.indexOf(PlayerConfig.MAIN_SUB_ID);
			if(indexOfSelectedSub < 0)
				indexOfSelectedSub = 0;
			data.setSelectedSubConfig(subConfigs.get(indexOfSelectedSub));
			PlayerConfigClientStorage subData = data.isSubConfigSelected() ?
					data.getOrCreateSubConfig(data.getSelectedSubConfig()) : data;

			elements.add(createSubConfigWidgetListElement(elementWidth, elementHeight, subConfigs, indexOfSelectedSub));
			boolean isCurrentlyUsed = Objects.equals(usedSubConfigOptionStorage.getValue(), data.getSelectedSubConfig());
			boolean canCreateSubs = data.getType() == PlayerConfigType.PLAYER || minecraft.player.hasPermissions(2);

			WidgetListElement<?> createSubConfigWidget = TextWidgetListElement.Builder.begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setTitle(new TranslatableComponent("gui.xaero_pac_ui_sub_config_create_widget"))
					.setTooltip(minecraft.font.split(new TranslatableComponent("gui.xaero_pac_ui_sub_config_create_widget_tooltip", new TranslatableComponent("gui.xaero_pac_config_create_sub_id_rules", PlayerConfig.MAX_SUB_ID_LENGTH)), 200))
					.setMutable(canCreateSubs && data.getSubCount() < data.getSubConfigLimit())
					.setStartValue("")
					.setFilter(Objects::nonNull)
					.setValidator(s -> PlayerConfig.isValidSubId(s) && !usedSubConfigOptionStorage.getValidator().test(data, s))
					.setResponder((el, s) -> {
						data.setSyncInProgress(true);
						data.setSelectedSubConfig(s);
						OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().requestCreateSubConfig(data, s);
						minecraft.setScreen(build());
					})
					.setMaxLength(PlayerConfig.MAX_SUB_ID_LENGTH)
					.setBoxWidth(75)
					.build();
			elements.add(createSubConfigWidget);

			WidgetListElement<?> useSubConfigButtonWidget = SimpleWidgetListElement.Builder.begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setMutable(!isCurrentlyUsed && !subData.isBeingDeleted())
					.setTooltip(minecraft.font.split(new TranslatableComponent(isCurrentlyUsed? "gui.xaero_pac_ui_sub_config_use_button_used_tooltip" : "gui.xaero_pac_ui_sub_config_use_button_tooltip"), 200))
					.setWidgetSupplier((el, xy) -> new Button(xy.getX(), xy.getY(), elementWidth, elementHeight, isCurrentlyUsed ? new TranslatableComponent("gui.xaero_pac_ui_sub_config_use_button_used") : new TranslatableComponent("gui.xaero_pac_ui_sub_config_use_button", data.getSelectedSubConfig()),
							b -> {
								usedSubConfigOptionStorage.setValue(data.getSelectedSubConfig());
								OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().syncToServer(usedSubConfigSyncDest, usedSubConfigOptionStorage);
								minecraft.setScreen(build());
							})).build();
			elements.add(useSubConfigButtonWidget);

			WidgetListElement<?> deleteSubConfigButtonWidget = SimpleWidgetListElement.Builder.begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setMutable(canCreateSubs && data.isSubConfigSelected() && !subData.isBeingDeleted())
					.setWidgetSupplier((el, xy) -> new Button(xy.getX(), xy.getY(), elementWidth, elementHeight, new TranslatableComponent("gui.xaero_pac_ui_sub_config_delete_button", data.getSelectedSubConfig()),
							b -> {
								String s = data.getSelectedSubConfig();
								minecraft.setScreen(new ConfirmScreen(result -> {
									if(result) {
										data.getOrCreateSubConfig(s).setBeingDeleted(true);
										OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().requestDeleteSubConfig(data, s);
									}
									minecraft.setScreen(build());
								}, new TranslatableComponent("gui.xaero_pac_ui_sub_config_delete_button_confirm1", s),
										new TranslatableComponent("gui.xaero_pac_ui_sub_config_delete_button_confirm2")));
							})).build();
			elements.add(deleteSubConfigButtonWidget);
		}

		public PlayerConfigScreen build() {
			if(manager == null || data == null
					|| data.getType() == PlayerConfigType.PLAYER && defaultPlayerConfigData == null
					|| data.getType() == PlayerConfigType.SERVER && mainPlayerConfigData == null)
				throw new IllegalStateException();
			boolean anotherPlayer = data.getType() == PlayerConfigType.PLAYER && data.getOwner() != null;
			if(anotherPlayer && otherPlayerName == null)
				throw new IllegalStateException();
			List<WidgetListElement<?>> elements = listFactory.get();
			int elementWidth = 200;
			int elementHeight = 20;
			BiConsumer<PlayerConfigScreen, Button> refreshHandler;
			if(anotherPlayer)
				refreshHandler = (s,b) -> s.minecraft.setScreen(new OtherPlayerConfigWaitScreen(s.escape, s.parent, otherPlayerName));
			else
				refreshHandler = (s,b) -> s.minecraft.setScreen(build());

			Component title = this.title;
			boolean syncInProgress = data.isSyncInProgress();
			if(!syncInProgress && (data.getType() == PlayerConfigType.PLAYER || data.getType() == PlayerConfigType.SERVER)) {
				addSubConfigControls(elements, elementWidth, elementHeight);
				if(title == null){
					if(data.getType() == PlayerConfigType.PLAYER)
						title = new TranslatableComponent("gui.xaero_pac_ui_my_player_config_sub", data.getSelectedSubConfig());
					else
						title = new TranslatableComponent("gui.xaero_pac_ui_server_claims_config_sub", data.getSelectedSubConfig());
				}
			}
			if(title == null)
				title = new TranslatableComponent("gui.xaero_pac_ui_player_config");
			boolean subConfigSelected = data.isSubConfigSelected();
			PlayerConfigClientStorage optionValueSourceData = subConfigSelected ? data.getOrCreateSubConfig(data.getSelectedSubConfig()) : data;
			Stream<PlayerConfigStringableOptionClientStorage<?>> optionStream = syncInProgress || optionValueSourceData.isBeingDeleted() ?
					Stream.empty() :
					optionValueSourceData.optionStream();
			optionStream.forEach(optionStorage -> {
				if(!optionStorage.getOption().getConfigTypeFilter().test(optionValueSourceData.getType())
						|| optionStorage.getOption() == PlayerConfigOptions.USED_SUBCLAIM
						|| optionStorage.getOption() == PlayerConfigOptions.USED_SERVER_SUBCLAIM)
					return;
				if(optionValueSourceData instanceof PlayerSubConfigClientStorage && !manager.getOverridableOptions().contains(optionStorage.getOption()))
					return;
				Class<?> type = optionStorage.getType();
				Component optionTitle = new TranslatableComponent(optionStorage.getTranslation(), optionStorage.getTranslationArgs());
				List<FormattedCharSequence> tooltip = Minecraft.getInstance().font.split(getUICommentForOption(optionStorage.getOption()), 200);
				if(type == Boolean.class) {
					@SuppressWarnings("unchecked")
					PlayerConfigStringableOptionClientStorage<Boolean> booleanOption = (PlayerConfigStringableOptionClientStorage<Boolean>) optionStorage;
					elements.add(createOnOffWidgetListElement(booleanOption, elementWidth,elementHeight, optionTitle, tooltip, optionValueSourceData));
				} else if(optionStorage.getOption() instanceof PlayerConfigListIterationOptionSpec) {
					elements.add(createIterationWidgetListElement(optionStorage, elementWidth, elementHeight, optionTitle, tooltip, optionValueSourceData));
				} else {
					Object value = getOptionValue(optionStorage);

					Predicate<String> filter = Objects::nonNull;
					if(type == Integer.class) {
						if(optionStorage.getOption() instanceof PlayerConfigHexOptionSpec)
							filter = s -> s != null && s.matches("^[0-9A-Fa-f]*$");
						else
							filter = s -> s != null && s.matches("^[-0-9]*$");
						
					} else if(type == Double.class || type == Float.class)
						filter = s -> s != null && s.matches("^[-\\.0-9]*$");
					
					TextWidgetListElement.Builder elementBuilder = TextWidgetListElement.Builder.begin()
							.setW(elementWidth)
							.setH(elementHeight)
							.setTitle(optionTitle)
							.setTooltip(tooltip)
							.setMutable(optionStorage.isMutable())
							.setStartValue(value == null ? "" : optionStorage.getCommandOutputWriterCast().apply(value).getString())
							.setFilter(filter)
							.setValidator(s -> subConfigSelected && s.isEmpty() || optionStorage.getStringValidator().test(optionValueSourceData, s))
							.setResponder((el, s) -> {
								if(!optionStorage.isMutable())
									return;
								//on value change
								Object newValue;
								if(subConfigSelected && s.isEmpty())
									newValue = null;
								else
									newValue = optionStorage.getCommandInputParser().apply(s);
								optionStorage.setCastValue(newValue);
								OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().syncToServer(optionValueSourceData, optionStorage);
							});
					if(optionStorage.getOption() instanceof PlayerConfigStringOptionSpec)
						elementBuilder.setMaxLength(((PlayerConfigStringOptionSpec) optionStorage.getOption()).getMaxLength());
					else if(optionStorage.getOption() instanceof PlayerConfigHexOptionSpec)
						elementBuilder.setMaxLength(8);
					elements.add(elementBuilder.build());
				}
			});
			
			return new PlayerConfigScreen(elements, listFactory.get(), refreshHandler, escape, parent, title, data, optionValueSourceData, syncInProgress, optionValueSourceData.isBeingDeleted());
		}

		public static Builder begin(ListFactory listFactory) {
			return new Builder(listFactory).setDefault();
		}
		
	}

}
