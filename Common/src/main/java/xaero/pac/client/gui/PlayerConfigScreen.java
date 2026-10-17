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

package xaero.pac.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.group.PlayerGroupsScreen;
import xaero.pac.client.gui.widget.dropdown.DropDownWidget;
import xaero.pac.client.gui.widget.dropdown.IDropDownContainer;
import xaero.pac.client.gui.widget.value.BooleanValueHolder;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.client.player.config.PlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.sub.PlayerSubConfigClientStorage;
import xaero.pac.common.misc.ListFactory;
import xaero.pac.common.server.player.config.*;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

public final class PlayerConfigScreen extends WidgetListScreen<PlayerConfigOptionCategory> {

	private static final Component SUB_CONFIG_TITLE = Component.translatable("gui.xaero_pac_ui_sub_config_dropdown");
	private static final Object NULL_PLACEHOLDER = new Comparable<Object>() {
		@Override
		public int compareTo(@Nonnull Object o) {
			return 0;
		}
	};
	public static final Component SYNCING_IN_PROGRESS = Component.translatable("gui.xaero_pac_ui_player_config_syncing");
	public static final Component BEING_DELETED = Component.translatable("gui.xaero_pac_ui_player_config_being_deleted");
	public static final Component SEARCH_NAME = Component.translatable("gui.xaero_pac_ui_player_config_search_box");
	public static final Component NO_OPTIONS = Component.translatable("gui.xaero_pac_ui_player_config_no_options");
	public static final Component MAIN_ONLY_OPTION = Component.translatable("gui.xaero_pac_ui_player_config_main_only_option")
			.withStyle(ChatFormatting.YELLOW);
	private final BiConsumer<PlayerConfigScreen, Button> refreshHandler;
	private Button refreshButton;
	private Button playerGroupsButton;
	private final PlayerConfigClientStorage data;
	private final PlayerConfigClientStorage subData;
	private final PlayerConfigClientStorage mainPlayerConfigData;
	private final List<String> subConfigOptions;
	private final boolean shouldWaitForData;
	private final boolean beingDeletedStateOnOpen;
	private final String otherPlayerName;
	private final Builder recreator;
	private final String selectedSubConfig;
	private final boolean canCreateSubs;
	private DropDownWidget subConfigDropdown;

	private PlayerConfigScreen(
			List<WidgetListElement<PlayerConfigOptionCategory, ?>> elements,
			List<WidgetListElement<PlayerConfigOptionCategory, ?>> filteredElements,
			List<EditBox> tickableBoxes,
			BiConsumer<PlayerConfigScreen, Button> refreshHandler,
			Screen escape,
			Screen parent,
			Component title,
			PlayerConfigClientStorage data,
			PlayerConfigClientStorage subData,
			PlayerConfigClientStorage mainPlayerConfigData,
			List<String> subConfigOptions,
			String selectedSubConfig,
			boolean shouldWaitForData,
			boolean beingDeletedStateOnOpen,
			String otherPlayerName,
			Builder recreator, boolean canCreateSubs
	) {
		super(
				elements, filteredElements, tickableBoxes, escape, parent,
				title, SEARCH_NAME, NO_OPTIONS, 1, PlayerConfigOptionCategory.values()
		);
		this.refreshHandler = refreshHandler;
		this.data = data;
		this.subData = subData;
		this.mainPlayerConfigData = mainPlayerConfigData;
		this.subConfigOptions = subConfigOptions;
		this.selectedSubConfig = selectedSubConfig;
		this.shouldWaitForData = shouldWaitForData;
		this.beingDeletedStateOnOpen = beingDeletedStateOnOpen;
		this.otherPlayerName = otherPlayerName;
		this.recreator = recreator;
		this.canCreateSubs = canCreateSubs;
	}
	
	@Override
	protected void init() {
		super.init();
		int xAnchor = width / 2;
		int yAnchor = height / 7;
		int topButtonsWidth = 84;
		addRenderableWidget(refreshButton = Button.builder(
						Component.translatable("gui.xaero_pac_ui_player_config_refresh"),
						b -> refreshHandler.accept(this, b)
				).bounds(xAnchor - 205, yAnchor - ROW_HEIGHT - 3, topButtonsWidth, 20).build()
		);
		if(data.isSyncInProgress())
			return;
		Component subConfigTooltipText = Component.translatable("gui.xaero_pac_ui_sub_config_dropdown_tooltip");
		List<FormattedCharSequence> subConfigTooltip = Minecraft.getInstance().font.split(subConfigTooltipText, 200);
		int subConfigTitleWidth = font.width(SUB_CONFIG_TITLE);
		int subConfigMargin = 6;
		String[] subConfigOptionArray;
		if(subConfigOptions == null)
			subConfigOptionArray = new String[]{"§8" + I18n.get("gui.xaero_pac_ui_sub_config_dropdown_not_supported")};
		else {
			subConfigOptionArray = new String[subConfigOptions.size() + (canCreateSubs ? 1 : 0)];
			subConfigOptionArray = subConfigOptions.toArray(subConfigOptionArray);
			if(canCreateSubs)
				subConfigOptionArray[subConfigOptionArray.length - 1] = "§8" + I18n.get("gui.xaero_pac_ui_sub_config_dropdown_create");
		}
		subConfigDropdown = DropDownWidget.Builder.begin()
				.setX(xAnchor - 205 + subConfigTitleWidth + subConfigMargin)
				.setY(yAnchor + 4)
				.setW(200 - subConfigTitleWidth - subConfigMargin - 1 - 20)
				.setOptions(subConfigOptionArray)
				.setNarrationTitle(subConfigTooltipText)
				.setCallback((dd, index) -> {
					if(subConfigOptions == null)
						return false;
					if(index >= subConfigOptions.size()) {
						minecraft.setScreen(
								CreateSubConfigScreen.Builder.begin()
										.setConfigData(data)
										.setTargetScreenSupplier(recreator::build)
										.setParent(this)
										.build()
						);
						return false;
					}
					data.setSelectedSubConfig(subConfigOptions.get(index));
					PlayerConfigScreen recreatedScreen = recreator.build();
					recreatedScreen.preservePageAndSearch(this);
					minecraft.setScreen(recreatedScreen);
					recreatedScreen.setFocused(recreatedScreen.subConfigDropdown);
					return true;
				})
				.setContainer((IDropDownContainer) Minecraft.getInstance().screen)
				.setSelected(subConfigOptions == null ? 0 : subConfigOptions.indexOf(selectedSubConfig))
				.setTooltip(subConfigTooltip)
				.build();
		addRenderableWidget(subConfigDropdown);

		Component deleteButtonName = Component.literal("❌").withStyle(ChatFormatting.DARK_RED);
		Component deleteButtonTooltipText = subConfigOptions == null ?
				Component.translatable("gui.xaero_pac_ui_sub_config_dropdown_not_supported") :
				Component.translatable("gui.xaero_pac_ui_sub_config_delete_button", selectedSubConfig);
		Tooltip deleteButtonTooltip = Tooltip.create(deleteButtonTooltipText);
		Button deleteSubButton = Button.builder(deleteButtonName,
				b -> {
					String s = selectedSubConfig;
					minecraft.setScreen(new ConfirmScreen(result -> {
						if(result) {
							subData.setBeingDeleted(true);
							OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().requestDeleteSubConfig(data, s);
						}
						minecraft.setScreen(recreator.build());
					}, Component.translatable("gui.xaero_pac_ui_sub_config_delete_button_confirm1", s),
							Component.translatable("gui.xaero_pac_ui_sub_config_delete_button_confirm2")));
				}
		).bounds(xAnchor - 25, yAnchor, 20, 20).tooltip(deleteButtonTooltip).build();
		deleteSubButton.active = subData != data && canCreateSubs && selectedSubConfig != null && !subData.isBeingDeleted();
		addRenderableWidget(deleteSubButton);

		addRenderableWidget(playerGroupsButton = Button.builder(
						Component.translatable("gui.xaero_pac_ui_player_config_player_groups"),
						b -> openGroupsScreen()
				).bounds(xAnchor + 205 - topButtonsWidth, yAnchor - ROW_HEIGHT - 3, topButtonsWidth, 20).build()
		);
	}

	public void openGroupsScreen(){
		minecraft.setScreen(
				PlayerGroupsScreen.Builder
						.begin()
						.setConfigData(data)
						.setEscape(escape)
						.setParent(this)
						.setConfigTitle(title)
						.build()
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partial);
	}

	@Override
	protected void renderPreDropdown(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
		super.renderPreDropdown(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.text(font, SUB_CONFIG_TITLE, width / 2 - 205 + 2, height / 7 + 6, subConfigOptions != null ? -1 : 0xFFE0E0E0/*copied from editbox class*/);
		if(shouldWaitForData){
			if(!data.isSyncInProgress())
				refresh();
			else
				guiGraphics.centeredText(font, SYNCING_IN_PROGRESS, width / 2, height / 7 + 64, -1);
		}
		if(beingDeletedStateOnOpen != subData.isBeingDeleted())
			refresh();
		else if(subData.isBeingDeleted())
			guiGraphics.centeredText(font, BEING_DELETED, width / 2, height / 7 + 84, -1);
	}

	public static MutableComponent getUICommentForOption(IPlayerConfigOptionSpecAPI<?> option){
		String commentTranslated = I18n.get(option.getCommentTranslation(), (Object[]) option.getCommentTranslationArgs());
		if(commentTranslated.equals("default"))
			commentTranslated = option.getComment();
		if(option.getTooltipPrefix() != null)
			commentTranslated = option.getTooltipPrefix() + "\n" + commentTranslated;
		return Component.literal(commentTranslated);
	}

	@Override
	protected Component getCategoryName(PlayerConfigOptionCategory category) {
		return category.getDisplayName();
	}

	public String getOtherPlayerName() {
		return otherPlayerName;
	}

	public void refresh(){
		refreshButton.onPress(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)));
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

		private <T> T getOptionValue(PlayerConfigStringableOptionClientStorage<T> option){
			return getOptionValue(data, defaultPlayerConfigData, option);
		}

		private static <T> T getOptionValue(
				PlayerConfigClientStorage data,
				PlayerConfigClientStorage defaultPlayerConfigData,
				PlayerConfigStringableOptionClientStorage<T> option){
			T value;
			if(data instanceof PlayerSubConfigClientStorage && !isOverridable(data, option))
				value = null;
			else if(option.isDefaulted())
				value = defaultPlayerConfigData.getOption(option.getOption()).getValue();
			else
				value = option.getValue();
			return value;
		}

		private <HT, T> CycleButton.OnValueChange<HT> getRegularValueChangeListener(SimpleValueWidgetListElement.Final<PlayerConfigOptionCategory, T> el, PlayerConfigStringableOptionClientStorage<T> option, Function<HT, T> holderToValue, PlayerConfigClientStorage data) {
			return (b, vh) -> {
				if(!el.mutable)
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

		private <T> Component getOptionValueDisplay(PlayerConfigStringableOptionClientStorage<T> option, T v){
			Component defaultDisplay = option.getOption().getValueDisplayName(v);
			if(option.getType() == Integer.class){
				String translationKey = option.getTranslation() + "_" + defaultDisplay.getString();
				String translatedText = I18n.get(translationKey);
				if(!translatedText.equals("default") && !translatedText.equals(translationKey))
					return Component.translatable(translationKey).setStyle(defaultDisplay.getStyle());
			}
			return defaultDisplay;
		}

		private <HT, T> BiFunction<SimpleValueWidgetListElement.Final<PlayerConfigOptionCategory, T>, Vec3i, AbstractWidget> getIterationWidgetSupplierForValues(Supplier<List<HT>> valuesSupplier, PlayerConfigStringableOptionClientStorage<T> option, int elementWidth, int elementHeight, Component optionTitle, Function<T, HT> valueToHolder, Function<HT, T> holderToValue, PlayerConfigClientStorage data){
			final PlayerConfigClientStorage finalDefaultPlayerConfigData = defaultPlayerConfigData;
			return (el, xy) -> CycleButton
					.<HT>builder(
						v -> getOptionValueDisplay(option, holderToValue.apply(v)),
						() -> valueToHolder.apply(getOptionValue(data, finalDefaultPlayerConfigData, option))
					)
					.withValues(valuesSupplier.get())
					.create(xy.getX(), xy.getY(), elementWidth, elementHeight, optionTitle, getRegularValueChangeListener(el, option, holderToValue, data));
		}

		private <T> BiFunction<SimpleValueWidgetListElement.Final<PlayerConfigOptionCategory, T>, Vec3i, AbstractWidget> getIterationWidgetSupplier(PlayerConfigStringableOptionClientStorage<T> option, int elementWidth, int elementHeight, Component optionTitle, T currentValue, PlayerConfigClientStorage data){
			PlayerConfigClientStorage valueSourceConfig;
			if(option.isDefaulted() && data.getType() == PlayerConfigType.PLAYER)
				valueSourceConfig = defaultPlayerConfigData;
			else
				valueSourceConfig = data;
			@SuppressWarnings("unchecked")
			T nullPlaceholder = (T) NULL_PLACEHOLDER;
			Supplier<List<T>> valuesSupplier = () -> {
				List<T> values;
				if (option.getOption() instanceof PlayerConfigListIterationOptionSpec<T> listIterationOptionSpec) {
					values = listIterationOptionSpec.getClientSideListGetter().apply(valueSourceConfig);
					if (values == null)
						values = Lists.newArrayList(currentValue);
					else
						values = Lists.newArrayList(values);
				} else
					values = Lists.newArrayList(currentValue);
				if (data instanceof PlayerSubConfigClientStorage) {
					values.add(0, nullPlaceholder);
				}
				return values;
			};
			return getIterationWidgetSupplierForValues(valuesSupplier, option, elementWidth, elementHeight, optionTitle, v -> v == null ? nullPlaceholder : v, h -> h == NULL_PLACEHOLDER ? null : h, data);
		}

		private BiFunction<SimpleValueWidgetListElement.Final<PlayerConfigOptionCategory, Boolean>, Vec3i, AbstractWidget> getOnOffWidgetSupplier(PlayerConfigStringableOptionClientStorage<Boolean> option, int elementWidth, int elementHeight, Component optionTitle, BooleanValueHolder currentValue, PlayerConfigClientStorage data){
			List<BooleanValueHolder> values = Lists.newArrayList(BooleanValueHolder.FALSE, BooleanValueHolder.TRUE);
			if(data instanceof PlayerSubConfigClientStorage)
				values.add(0, BooleanValueHolder.NULL);
			return getIterationWidgetSupplierForValues(() -> values, option, elementWidth, elementHeight, optionTitle, BooleanValueHolder::of, BooleanValueHolder::getValue, data);
		}

		private <T> SimpleValueWidgetListElement<PlayerConfigOptionCategory, T, ?> createIterationWidgetListElement(
				PlayerConfigStringableOptionClientStorage<T> option, int elementWidth, int elementHeight,
				Component optionTitle, List<FormattedCharSequence> tooltip, PlayerConfigClientStorage data){
			T value = getOptionValue(option);
			BiFunction<SimpleValueWidgetListElement.Final<PlayerConfigOptionCategory, T>, Vec3i, AbstractWidget> widgetSupplier = getIterationWidgetSupplier(option, elementWidth, elementHeight, optionTitle, value, data);
			return SimpleValueWidgetListElement.FinalBuilder.<PlayerConfigOptionCategory, T>begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setWidgetSupplier(widgetSupplier)
					.setSearchName(() -> optionTitle.getString() + " " + option.getId())
					.setCategory(option.getOption()::getCategory)
					.setTooltip(tooltip)
					.setMutable(isMutable(data, option))
					.setStartValue(value)
					.setValueNamer(v -> getOptionValueDisplay(option, v).getString())
					.build();
		}

		private SimpleValueWidgetListElement<PlayerConfigOptionCategory, Boolean, ?> createOnOffWidgetListElement(
				PlayerConfigStringableOptionClientStorage<Boolean> option, int elementWidth, int elementHeight,
				Component optionTitle, List<FormattedCharSequence> tooltip, PlayerConfigClientStorage data){
			Boolean value = getOptionValue(option);
			BiFunction<SimpleValueWidgetListElement.Final<PlayerConfigOptionCategory, Boolean>, Vec3i, AbstractWidget> widgetSupplier = getOnOffWidgetSupplier(option, elementWidth, elementHeight, optionTitle, BooleanValueHolder.of(value), data);
			return SimpleValueWidgetListElement.FinalBuilder.<PlayerConfigOptionCategory, Boolean>begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setWidgetSupplier(widgetSupplier)
					.setSearchName(() -> optionTitle.getString() + " " + option.getId())
					.setCategory(option.getOption()::getCategory)
					.setTooltip(tooltip)
					.setMutable(isMutable(data, option))
					.setStartValue(value)
					.setValueNamer(v -> getOptionValueDisplay(option, v).getString())
					.build();
		}

		private void addSubConfigControls(
				List<WidgetListElement<PlayerConfigOptionCategory, ?>> elements,
				int elementWidth,
				int elementHeight,
				List<String> subConfigs
		){
			Minecraft minecraft = Minecraft.getInstance();
			if(subConfigs.isEmpty())
				throw new IllegalStateException();
			PlayerConfigClientStorage usedSubConfigSyncDest;
			PlayerConfigStringableOptionClientStorage<String> usedSubConfigOptionStorage;
			if(data.getType() != PlayerConfigType.PLAYER && data.getType().getSubClaimOption() != null){
				usedSubConfigSyncDest = mainPlayerConfigData;
				usedSubConfigOptionStorage = mainPlayerConfigData.getOption(data.getType().getSubClaimOption());
			} else {
				usedSubConfigSyncDest = data;
				usedSubConfigOptionStorage = data.getOption(PlayerConfigOptions.USED_SUBCLAIM);
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
			if(subData.isBeingDeleted())
				return;
			if(data.getType().hasDimensionSubConfigs())
				return;
			boolean isCurrentlyUsed = Objects.equals(usedSubConfigOptionStorage.getValue(), data.getSelectedSubConfig());
			final String useSubConfigButtonNameId = isCurrentlyUsed? "gui.xaero_pac_ui_sub_config_use_button_used_tooltip" : "gui.xaero_pac_ui_sub_config_use_button_tooltip";
			WidgetListElement<PlayerConfigOptionCategory, ?> useSubConfigButtonWidget = SimpleWidgetListElement.Builder.<PlayerConfigOptionCategory>begin()
					.setW(elementWidth)
					.setH(elementHeight)
					.setMutable(!isCurrentlyUsed && !subData.isBeingDeleted())
					.setSearchName(() -> I18n.get(useSubConfigButtonNameId))
					.setTooltip(minecraft.font.split(Component.translatable(useSubConfigButtonNameId), 200))
					.setWidgetSupplier((el, xy) -> Button.builder(isCurrentlyUsed ? Component.translatable("gui.xaero_pac_ui_sub_config_use_button_used") : Component.translatable("gui.xaero_pac_ui_sub_config_use_button", data.getSelectedSubConfig()),
							b -> {
								usedSubConfigOptionStorage.setValue(data.getSelectedSubConfig());
								OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().syncToServer(usedSubConfigSyncDest, usedSubConfigOptionStorage);
								minecraft.setScreen(build());
							}).bounds(xy.getX(), xy.getY(), elementWidth, elementHeight).build()).build();
			elements.add(useSubConfigButtonWidget);
		}

		private boolean isMutable(PlayerConfigClientStorage data, PlayerConfigStringableOptionClientStorage<?> option){
			if(!data.getPermissions().canEdit() ||
					!option.isPlayerMutable() && (!manager.isAdmin() || !option.isAdminMutable()))
				return false;
			return !(data instanceof PlayerSubConfigClientStorage) || isOverridable(data, option);
		}

		private static boolean isOverridable(PlayerConfigClientStorage data, PlayerConfigStringableOptionClientStorage<?> option){
			return option.getOption().isOverridable() ||
					data.getType().hasDimensionSubConfigs() && option.getOption().isOnlyDimensionOverridable();
		}

		public PlayerConfigScreen build() {
			if(manager == null || data == null)
				throw new IllegalStateException();
			if((data.getType() == PlayerConfigType.PLAYER || data.getType() == PlayerConfigType.PARTY_CLAIMS)
					&& defaultPlayerConfigData == null)
				throw new IllegalStateException();
			if((data.getType() == PlayerConfigType.SERVER || data.getType() == PlayerConfigType.PARTY_CLAIMS)
					&& mainPlayerConfigData == null)
				throw new IllegalStateException();
			boolean anotherPlayer = data.getType() == PlayerConfigType.PLAYER && data.getOwner() != null;
			if(anotherPlayer && otherPlayerName == null)
				throw new IllegalStateException();
			List<WidgetListElement<PlayerConfigOptionCategory, ?>> elements = listFactory.get();
			int elementWidth = 200;
			int elementHeight = 20;
			BiConsumer<PlayerConfigScreen, Button> refreshHandler;
			if(anotherPlayer)
				refreshHandler = (s,b) -> s.minecraft.setScreen(new OtherPlayerConfigWaitScreen(s.escape, s.parent, otherPlayerName));
			else
				refreshHandler = (s,b) -> {
					PlayerConfigScreen recreatedScreen = build();
					recreatedScreen.preservePageAndSearch(s);
					s.minecraft.setScreen(recreatedScreen);
				};

			Component title = this.title;
			if(title == null) {
				if(data.getType() == PlayerConfigType.PLAYER)
					title = Component.translatable("gui.xaero_pac_ui_my_player_config");
				else if(data.getType() == PlayerConfigType.SERVER)
					title = Component.translatable("gui.xaero_pac_ui_server_claims_config");
				else if(data.getType() == PlayerConfigType.PARTY_CLAIMS)
					title = Component.translatable("gui.xaero_pac_ui_party_claims_config");
				else
					title = Component.translatable("gui.xaero_pac_ui_player_config");
			}
			boolean syncInProgress = data.isSyncInProgress();
			boolean hasSubConfigs = data.getType().supportsSubConfigs();
			String selectedSubConfig = data.getSelectedSubConfig();
			List<String> subConfigs = hasSubConfigs ? new ArrayList<>(data.getSubConfigIds()) : null;
			if(!syncInProgress && hasSubConfigs) {
				addSubConfigControls(elements, elementWidth, elementHeight, subConfigs);
				selectedSubConfig = data.getSelectedSubConfig();
			}

			boolean subConfigSelected = data.isSubConfigSelected();
			PlayerConfigClientStorage optionValueSourceData = subConfigSelected ? data.getOrCreateSubConfig(selectedSubConfig) : data;
			boolean beingDeleted = optionValueSourceData.isBeingDeleted();
			Stream<PlayerConfigStringableOptionClientStorage<?>> optionStream = syncInProgress || beingDeleted ?
					Stream.empty() :
					optionValueSourceData.typedOptionStream();
			optionStream.forEach(optionStorage -> {
				if(!optionStorage.isSyncable()
						|| !optionStorage.isDirectlyConfigurable()
						|| !optionStorage.getOption().getConfigTypeFilter().test(optionValueSourceData.getType())
						|| optionStorage.getOption() == PlayerConfigOptions.USED_SUBCLAIM
						|| optionStorage.getOption() == PlayerConfigOptions.USED_SERVER_SUBCLAIM
						|| optionStorage.getOption() == PlayerConfigOptions.USED_PARTY_SUBCLAIM
				)
					return;
				final boolean mutable = isMutable(optionValueSourceData, optionStorage);
				Class<?> type = optionStorage.getType();
				Component optionTitle = Component.translatable(optionStorage.getTranslation(), optionStorage.getTranslationArgs());
				List<FormattedCharSequence> tooltip;
				if(!mutable && subConfigSelected && !isOverridable(optionValueSourceData, optionStorage))
					tooltip = Minecraft.getInstance().font.split(MAIN_ONLY_OPTION, 200);
				else
					tooltip = Minecraft.getInstance().font.split(getUICommentForOption(optionStorage.getOption()), 200);
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
					final Predicate<String> finalFilter = filter;
					TextWidgetListElement.Builder<PlayerConfigOptionCategory> elementBuilder = TextWidgetListElement.Builder.<PlayerConfigOptionCategory>begin()
							.setW(elementWidth)
							.setH(elementHeight)
							.setTitle(optionTitle)
							.setSearchName(() -> optionTitle.getString() + " " + optionStorage.getId())
							.setCategory(optionStorage.getOption()::getCategory)
							.setTooltip(tooltip)
							.setMutable(mutable)
							.setStartValue(value == null ? "" : optionStorage.getStringWriterCast().apply(value))
							.setValidator(s -> subConfigSelected && s.isEmpty() || finalFilter.test(s) && optionStorage.getStringValidator().test(optionValueSourceData, s))
							.setResponder((el, s) -> {
								if(!mutable)
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
			boolean canCreateSubs = data.getPermissions().canEdit();
			return new PlayerConfigScreen(
					elements, listFactory.get(), listFactory.get(), refreshHandler, escape, parent,
					title, data, optionValueSourceData, mainPlayerConfigData, subConfigs,
					selectedSubConfig, syncInProgress, optionValueSourceData.isBeingDeleted(),
					otherPlayerName, this,
					canCreateSubs
			);
		}

		public static Builder begin(ListFactory listFactory) {
			return new Builder(listFactory).setDefault();
		}
		
	}

}
