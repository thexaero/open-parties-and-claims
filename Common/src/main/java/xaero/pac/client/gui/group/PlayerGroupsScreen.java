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

package xaero.pac.client.gui.group;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.OtherPlayerConfigWaitScreen;
import xaero.pac.client.gui.PlayerConfigScreen;
import xaero.pac.client.gui.XPACScreen;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.group.PlayerConfigGroupExistencePacket;
import xaero.pac.common.packet.config.group.PlayerConfigGroupGroupPacket;
import xaero.pac.common.packet.config.group.PlayerConfigGroupMemberPacket;
import xaero.pac.common.player.config.group.BuiltInPlayerConfigGroupNames;
import xaero.pac.common.player.config.group.api.PlayerConfigGroupActionError;
import xaero.pac.common.player.config.group.custom.CustomPlayerConfigGroupData;
import xaero.pac.common.player.config.group.custom.CustomPlayerGroupMember;
import xaero.pac.common.player.config.group.custom.ICustomPlayerGroupMember;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class PlayerGroupsScreen extends XPACScreen {

	private static final Component TITLE =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_screen_title");
	private static final Component INCLUDED_GROUPS =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_included_groups_label");
	private static final Component INCLUDED_PLAYERS =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_included_players_label");
	private static final Component PLEASE_SELECT_GROUP =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_please_select_group");
	private static final Component SYNCHRONIZING =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_synchronizing");
	private static final Component NO_GROUPS =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_no_custom_groups");
	private static final Component FIXING_DESYNC =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_fixing_desync");
	private static final Component DELETE_CONFIRMATION1 =
			Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_delete_group_confirmation1");

	private static final int HEADER_HEIGHT = 48;
	private static final int FOOTER_HEIGHT = 64;
	private static final int ROW_HEIGHT = 18;

	private static final int LIST_TITLE_LABEL_COLOR = 0xFFC8C8C8;
	private List<String> groupIds;
	private final Map<String, CustomPlayerConfigGroupData> editorData;
	private int maxGroups;
	private int groupSpace;
	private int usedGroupSpace;
	private GroupList groupList;
	private ContentsList contentsList;
	private boolean selectionWorksForGroupDeletion;
	private boolean selectionWorksForGroupExclusion;
	private final Map<Button, ContentsButtonInfo> contentsButtons;
	private Button createGroupButton;
	private Button deleteGroupButton;
	private final PlayerConfigClientStorage configData;
	private CustomPlayerConfigGroupData groupDataCopy;
	private boolean syncingOnInit;
	private PlayerConfigGroupActionError desyncErrorOnInit;
	private String selectedGroupBeforeResync;
	private PlayerConfigGroupActionError latestDesyncError;
	private long latestDesyncErrorTime;
	private Component configTitle;

	private PlayerGroupsScreen(
			Screen escape,
			Screen parent,
			Map<Button, ContentsButtonInfo> contentsButtons,
			PlayerConfigClientStorage configData,
			Component configTitle
	) {
		super(escape, parent, TITLE);
		this.contentsButtons = contentsButtons;
		this.configData = configData;
		this.configTitle = configTitle;
		this.editorData = new HashMap<>();
		this.syncingOnInit = true;//to trigger initial refreshGroups on init
	}

	@Override
	protected void init() {
		super.init();
		boolean wasEffectivelySyncingBeforeInit = wasEffectivelySyncingOnInit();
		syncingOnInit = configData.getPlayerGroups().isSyncInProgress();
		desyncErrorOnInit = syncingOnInit ? null : configData.getPlayerGroups().getDesyncError();
		if(wasEffectivelySyncingBeforeInit && !wasEffectivelySyncingOnInit())
			refreshGroups();
		if(desyncErrorOnInit != null)
			groupDataCopy = null;
		initLists(wasEffectivelySyncingBeforeInit);
		addRenderableWidget(
				Button.builder(
						Component.translatable("gui.xaero_pac_back"),
						b -> goBack()
				).bounds(width / 2 - 100, height - 30, 200, 20).build()
		);
		addRenderableWidget(
				createGroupButton = Button.builder(
						Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_create_group"),
						this::onCreateGroupButton
				).bounds(width / 2 - 205, 10, 80, 20).build()
		);
		addRenderableWidget(
				deleteGroupButton = Button.builder(
						Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_delete_group"),
						this::onDeleteGroupButton
				).bounds(width / 2 + 125, 10, 80, 20).build()
		);
		List<ContentsButtonInfo> contentsButtonInfoList = new ArrayList<>();
		contentsButtonInfoList.add(new ContentsButtonInfo(
				Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_include_player"),
				this::onIncludePlayerButton,
				this::getIncludePlayerButtonState
		));
		contentsButtonInfoList.add(new ContentsButtonInfo(
				Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_include_group"),
				this::onIncludeGroupButton,
				this::getIncludeGroupButtonState
		));
		contentsButtonInfoList.add(new ContentsButtonInfo(
				Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_exclude_player"),
				this::onExcludePlayerButton,
				this::getExcludePlayerButtonState
		));
		contentsButtonInfoList.add(new ContentsButtonInfo(
				Component.translatable("gui.xaero_pac_ui_player_config_player_groups_button_exclude_group"),
				this::onExcludeGroupButton,
				this::getExcludeGroupButtonState
		));
		addContentsButtons(contentsButtonInfoList, 85, 5);
		refreshButtonStates();
	}

	private boolean isEffectivelySyncing(){
		return configData.getPlayerGroups().isSyncInProgress() || configData.getPlayerGroups().isFixingDesync();
	}

	private boolean wasEffectivelySyncingOnInit(){
		return syncingOnInit || desyncErrorOnInit != null;
	}

	private void initLists(boolean wasEffectivelySyncingBeforeInit){
		GroupList.Entry selectedGroupEntry = groupList == null ? null : groupList.getSelected();
		String selectedGroupId = selectedGroupEntry == null ? null : selectedGroupEntry.groupId;
		if(!wasEffectivelySyncingBeforeInit && wasEffectivelySyncingOnInit())
			selectedGroupBeforeResync = selectedGroupId;
		else if(wasEffectivelySyncingBeforeInit && !wasEffectivelySyncingOnInit()) {
			refreshGroups();
			groupDataCopy = getGroupEditorData(selectedGroupBeforeResync, false);
			if(groupDataCopy != null)
				selectedGroupId = selectedGroupBeforeResync;
			selectedGroupBeforeResync = null;
		}
		double groupsScroll = groupList == null ? 0 : groupList.getScrollAmount();
		groupList = new GroupList(minecraft, width, height);
		groupList.setScrollAmount(groupsScroll);
		if(selectedGroupId != null)
			for (GroupList.Entry row : groupList.children())
				if (selectedGroupId.equals(row.groupId)) {
					groupList.setSelectedRaw(row);
					groupList.centerScrollOn(row);
				}
		int selectedContentsIndex = -1;
		double contentsScroll = 0;
		if(contentsList != null) {
			if(contentsList.source == groupDataCopy) {//still the same group data, so contents is the same
				ContentsList.AbstractEntry selectedContentsEntry = contentsList.getSelected();
				if (selectedContentsEntry != null)
					selectedContentsIndex = selectedContentsEntry.index;
			}
			contentsScroll = contentsList.getScrollAmount();
		}
		contentsList = new ContentsList(minecraft, width, height, groupDataCopy);
		if(selectedContentsIndex != -1)
			contentsList.children().get(selectedContentsIndex).select();
		contentsList.setScrollAmount(contentsScroll);
		addWidget(groupList);
		addWidget(contentsList);
		selectionWorksForGroupDeletion = false;
		selectionWorksForGroupExclusion = false;
	}

	private void addContentsButtons(List<ContentsButtonInfo> buttons, int buttonWidth, int betweenButtons){
		contentsButtons.clear();
		int footerButtonDifference = buttonWidth + betweenButtons;
		int footerButtonCount = buttons.size();
		int totalContentsButtons = footerButtonDifference * footerButtonCount - betweenButtons;
		int contentsButtonsStart = Math.min(width / 2 + 10, width - 10 - totalContentsButtons);
		for (int i = 0; i < buttons.size(); i++) {
			ContentsButtonInfo buttonInfo = buttons.get(i);
			Button button = Button.builder(
					buttonInfo.label,
					buttonInfo.action
			).bounds(contentsButtonsStart + i * footerButtonDifference, height - FOOTER_HEIGHT + 5, buttonWidth, 20)
					.build();
			addRenderableWidget(button);
			contentsButtons.put(button, buttonInfo);
		}
	}

	private ButtonState getIncludePlayerButtonState() {
		if(usedGroupSpace >= groupSpace)
			return ButtonState.OUT_OF_SPACE;
		return canEdit() && groupDataCopy != null ? ButtonState.ENABLED : ButtonState.DISABLED;
	}

	private ButtonState getIncludeGroupButtonState() {
		if(usedGroupSpace >= groupSpace)
			return ButtonState.OUT_OF_SPACE;
		return canEdit() && groupDataCopy != null ? ButtonState.ENABLED : ButtonState.DISABLED;
	}

	private ButtonState getExcludeGroupButtonState() {
		return canEdit() && groupDataCopy != null && selectionWorksForGroupExclusion &&
				contentsList.getSelected() instanceof ContentsList.GroupInclusionEntry ?
				ButtonState.ENABLED : ButtonState.DISABLED;
	}

	private ButtonState getExcludePlayerButtonState() {
		return canEdit() && groupDataCopy != null && contentsList.getSelected() instanceof ContentsList.PlayerEntry ?
				ButtonState.ENABLED : ButtonState.DISABLED;
	}

	private ButtonState getCreateGroupButtonState(){
		if(groupIds != null && groupIds.size() >= maxGroups)
			return ButtonState.MAX_GROUPS_REACHED;
		return canEdit() && !syncingOnInit && desyncErrorOnInit == null ? ButtonState.ENABLED : ButtonState.DISABLED;
	}

	private ButtonState getDeleteGroupButtonState(){
		return canEdit() && groupDataCopy != null && selectionWorksForGroupDeletion && desyncErrorOnInit == null ?
				ButtonState.ENABLED : ButtonState.DISABLED;
	}

	private boolean canEdit(){
		return configData == configData.getManager().getMyPlayerConfig() ||
				minecraft.player != null && minecraft.player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
	}

	private void refreshButtonStates(){
		if(createGroupButton == null)
			return;
		ButtonState createButtonState = getCreateGroupButtonState();
		createGroupButton.active = createButtonState == ButtonState.ENABLED;
		createGroupButton.setTooltip(createButtonState.tooltip);
		ButtonState deleteButtonState = getDeleteGroupButtonState();
		deleteGroupButton.active = deleteButtonState == ButtonState.ENABLED;
		deleteGroupButton.setTooltip(deleteButtonState.tooltip);
		contentsButtons.forEach((button, activeState) -> {
					ButtonState buttonState = activeState.stateSupplier.get();
					button.active = desyncErrorOnInit == null && buttonState == ButtonState.ENABLED;
					button.setTooltip(buttonState.tooltip);
				}
		);
	}

	private void onGroupCreated(String groupId){
		int destinationIndex = Collections.binarySearch(groupIds, groupId);
		if(destinationIndex >= 0)
			return;
		groupIds.add(-destinationIndex - 1, groupId);
		groupList.createEntry(groupId).select();//switch to the new group
	}

	private void onGroupDeleted(String groupId){
		groupIds.remove(groupId);
		editorData.remove(groupId);
		groupList.setSelectedRaw(null);
		usedGroupSpace -= groupDataCopy.getSize();
		groupDataCopy = null;
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(
				new PlayerConfigGroupExistencePacket(configData.getType(), configData.getOwnerForSync(), groupId, false)
		);
	}

	private void refreshGroups(){
		this.groupIds = configData.getPlayerGroups().getIds().stream()
				.sorted(Comparator.comparing(String::toLowerCase))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);//so that it's mutable
		this.editorData.clear();
		this.maxGroups = configData.getPlayerGroups().getMaxGroups();
		this.groupSpace = configData.getPlayerGroups().getGroupSpace();
		this.usedGroupSpace = configData.getPlayerGroups().getUsedSpace();
	}

	private void onCreateGroupButton(Button b){
		if(getCreateGroupButtonState() != ButtonState.ENABLED)
			return;
		minecraft.setScreen(
				CreatePlayerGroupScreen.Builder.begin()
						.setParent(this)
						.setConfigData(configData)
						.setExistingEditorGroups(groupIds)
						.setListener(this::onGroupCreated)
						.build()
		);
	}

	private void onDeleteGroupButton(Button b){
		if(getDeleteGroupButtonState() != ButtonState.ENABLED)
			return;
		String selectedGroupId = groupDataCopy.getId();
		minecraft.setScreen(
				new ConfirmScreen(response -> {
					if(response)
						onGroupDeleted(selectedGroupId);
					minecraft.setScreen(PlayerGroupsScreen.this);
				},
						DELETE_CONFIRMATION1,
						Component.translatable(
								"gui.xaero_pac_ui_player_config_player_groups_button_delete_group_confirmation2",
								Component.literal(selectedGroupId).withStyle(s -> s.withColor(ChatFormatting.DARK_RED))
						)
				)
		);
	}

	private void onGroupIncluded(String groupId){
		contentsList.setSelected(null);
		usedGroupSpace++;
	}

	private void onIncludeGroupButton(Button b){
		if(getIncludeGroupButtonState() != ButtonState.ENABLED)
			return;
		minecraft.setScreen(
				IncludeGroupScreen.Builder.begin()
						.setGroupData(groupDataCopy)
						.setConfigData(configData)
						.setListener(this::onGroupIncluded)
						.setParent(this)
						.build()
		);
	}

	private void onExcludeGroupButton(Button b){
		if(getExcludeGroupButtonState() != ButtonState.ENABLED)
			return;
		String selectedGroupId = ((ContentsList.GroupInclusionEntry)contentsList.getSelected()).groupId;
		groupDataCopy.excludeGroup(selectedGroupId);
		usedGroupSpace--;
		contentsList.setSelected(null);
		contentsList.reload(groupDataCopy);
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(
				new PlayerConfigGroupGroupPacket(
						configData.getType(), configData.getOwnerForSync(),
						groupDataCopy.getId(), PlayerConfigGroupGroupPacket.Action.EXCLUDE,
						selectedGroupId
				)
		);
	}

	private void onPlayerIncluded(String playerName){
		contentsList.setSelected(null);
		usedGroupSpace++;
	}

	private void onIncludePlayerButton(Button b){
		if(getIncludePlayerButtonState() != ButtonState.ENABLED)
			return;
		minecraft.setScreen(
				IncludePlayerScreen.Builder.begin()
						.setGroupData(groupDataCopy)
						.setConfigData(configData)
						.setListener(this::onPlayerIncluded)
						.setParent(this)
						.build()
		);
	}

	private void onExcludePlayerButton(Button b){
		if(getExcludePlayerButtonState() != ButtonState.ENABLED)
			return;
		ICustomPlayerGroupMember selectedPlayer = ((ContentsList.PlayerEntry)contentsList.getSelected()).getGroupMember();
		groupDataCopy.excludeMember((CustomPlayerGroupMember) selectedPlayer);
		usedGroupSpace--;
		contentsList.setSelected(null);
		contentsList.reload(groupDataCopy);
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(
				new PlayerConfigGroupMemberPacket(
						configData.getType(), configData.getOwnerForSync(),
						groupDataCopy.getId(), PlayerConfigGroupMemberPacket.Action.EXCLUDE,
						selectedPlayer.getId(), selectedPlayer.getDisplayName()
				)
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		groupList.render(guiGraphics, mouseX, mouseY, partial);
		contentsList.render(guiGraphics, mouseX, mouseY, partial);
		int messageCenterY = (height - FOOTER_HEIGHT + HEADER_HEIGHT) / 2 - 4;
		if(wasEffectivelySyncingOnInit()){
			if(desyncErrorOnInit != null)
				guiGraphics.drawCenteredString(
						font, FIXING_DESYNC, width / 2, HEADER_HEIGHT + 10,
						LIST_TITLE_LABEL_COLOR
				);
			else
				guiGraphics.drawCenteredString(
					font, SYNCHRONIZING, width / 2, messageCenterY,
					LIST_TITLE_LABEL_COLOR
				);
			if(!isEffectivelySyncing())
				refreshForResync();
		} else if(groupList.children().isEmpty())
			guiGraphics.drawCenteredString(
					font, NO_GROUPS, width / 2, messageCenterY,
					LIST_TITLE_LABEL_COLOR
			);
		super.render(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.drawCenteredString(font, TITLE, width / 2, 20, -1);
		guiGraphics.drawCenteredString(font, configTitle, width / 2, 31, -1);

		if(latestDesyncError != null && latestDesyncError.getDesyncScreenMessage() != null &&
				(System.currentTimeMillis() - latestDesyncErrorTime) < 3000) {
			guiGraphics.fill(0, messageCenterY - 18, width, messageCenterY + 6, 0xCC000000);
			guiGraphics.drawCenteredString(
					font, latestDesyncError.getDesyncScreenMessage(), width / 2, messageCenterY - 10,
					0xFFAA0000
			);
		}
	}

	@Override
	public void setFocused(@Nullable GuiEventListener element) {
		super.setFocused(element);
		refreshButtonStates();
	}

	public void onDesyncError(
			PlayerConfigClientStorage config
	) {
		if(config != configData)
			return;
		latestDesyncError = config.getPlayerGroups().getDesyncError();
		latestDesyncErrorTime = System.currentTimeMillis();
		refreshForResync();
	}

	private void refreshForResync(){
		if(desyncErrorOnInit != null && configData == configData.getManager().getOtherPlayerConfig()) {
			if(!(parent instanceof PlayerConfigScreen configScreenParent)) {
				minecraft.setScreen(null);
				return;
			}
			minecraft.setScreen(
					new OtherPlayerConfigWaitScreen(
							escape, configScreenParent.getParent(),
							configScreenParent.getOtherPlayerName(), true
					)
			);
			return;
		}
		resize(minecraft, width, height);
	}

	private CustomPlayerConfigGroupData getGroupEditorData(String groupId, boolean create){
		if(groupId == null)
			return null;
		return editorData.computeIfAbsent(groupId, g -> {
			CustomPlayerConfigGroupData result = configData.getPlayerGroups().getDataCopy(g);
			if(result == null && create)
				result = new CustomPlayerConfigGroupData(groupId);
			return result;
		});
	}

	@Override
	public void goBack() {
		super.goBack();
		if(minecraft.screen instanceof PlayerConfigScreen playerConfigScreen &&
				configData == configData.getManager().getOtherPlayerConfig())
			playerConfigScreen.refresh();
	}

	@Override
	public boolean keyPressed(int code, int $$1, int $$2) {
		if(code == GLFW.GLFW_KEY_DELETE){
			//only one button will actually do something
			onDeleteGroupButton(null);
			onExcludeGroupButton(null);
			onExcludePlayerButton(null);
			return true;
		}
		return super.keyPressed(code, $$1, $$2);
	}

	public class GroupList extends ObjectSelectionList<GroupList.Entry> {

		public GroupList(Minecraft minecraft, int screenW, int screenH) {
			super(minecraft, screenW / 2, screenH - FOOTER_HEIGHT - HEADER_HEIGHT, HEADER_HEIGHT, ROW_HEIGHT);
			createEntries();
		}

		@Override
		protected boolean isSelectedItem(int index) {
			return false;//not rendering the defeault selection indicator
		}

		private void createEntries(){
			if(syncingOnInit || desyncErrorOnInit != null)
				return;
			for (String groupId : groupIds)
				addEntry(createEntry(groupId));
		}

		public Entry createEntry(String groupId) {
			return new Entry(groupId);
		}

		public void setSelectedRaw(@Nullable Entry entry) {
			super.setSelected(entry);
		}

		@Override
		public void setSelected(@Nullable Entry entry) {
			if(entry != null) {
				selectionWorksForGroupDeletion = true;
				selectionWorksForGroupExclusion = false;
				refreshButtonStates();//here because on 1.20.1+ setSelected is called after setting focus, not before
			}
			if(entry == getSelected())
				return;
			super.setSelected(entry);
			if(entry == null)
				return;
			String selectedGroupId = entry.groupId;
			groupDataCopy = getGroupEditorData(selectedGroupId, true);
			contentsList.reload(groupDataCopy);
			contentsList.setScrollAmount(0);
		}

		@Override
		public void centerScrollOn(Entry $$0) {
			super.centerScrollOn($$0);
		}

		@Override
		public int getRowWidth() {
			return width;
		}

		@Override
		public int getRowLeft() {
			 return getX();
		}

		@Override
		public boolean isFocused() {
			return PlayerGroupsScreen.this.getFocused() == this;
		}

		@Override
		protected int getScrollbarPosition() {
			return width - 6;
		}

		public class Entry extends ObjectSelectionList.Entry<Entry> {

			private final String groupId;
			private final Component groupName;

			public Entry(String groupId) {
				this.groupId = groupId;
				this.groupName = BuiltInPlayerConfigGroupNames.apply(groupId);
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if(button == 0) {
					select();
					return true;
				}
				return false;
			}

			private void select(){
				GroupList.this.setSelected(this);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", groupName);
			}

			@Override
			public void render(
					GuiGraphics guiGraphics,
					int index,
					int y,
					int x,
					int rowWidth,
					int rowHeight,
					int mouseX,
					int mouseY,
					boolean hovered,
					float partialTicks
			){
				boolean isFirst = index == 0;
				boolean isLast = index == getItemCount() - 1;
				boolean isSelected = isSelectedItem(index);
				int separatorLineColor = isFocused() ? -1 : LIST_TITLE_LABEL_COLOR;
				int separatorLineX = x + rowWidth - 1;
				if(getMaxScroll() > 0)
					separatorLineX -= 6;//room for the scroll bar
				int labelColor = -1;
				if(isSelected){
					guiGraphics.hLine(x, separatorLineX, y, separatorLineColor);
					guiGraphics.hLine(x, separatorLineX, y + ROW_HEIGHT - 1, separatorLineColor);
				} else {
					labelColor = hovered ? LIST_TITLE_LABEL_COLOR : 0xFF808080;
					guiGraphics.vLine(separatorLineX, y - 1, y + ROW_HEIGHT, separatorLineColor);
				}
				if(isFirst)
					guiGraphics.vLine(separatorLineX, getY(), y, separatorLineColor);
				if(isLast)
					guiGraphics.vLine(separatorLineX, y + ROW_HEIGHT - 1, getBottom(), separatorLineColor);
				guiGraphics.drawString(
						font, groupName,
						separatorLineX - 7 - font.width(groupName), y + rowHeight / 2 - 2, labelColor
				);
			}

		}
	}

	public class ContentsList extends ObjectSelectionList<ContentsList.AbstractEntry> {

		private CustomPlayerConfigGroupData source;
		private long selectionTime;

		public ContentsList(Minecraft minecraft, int screenW, int screenH, CustomPlayerConfigGroupData source) {
			super(minecraft, screenW - screenW / 2, screenH - FOOTER_HEIGHT - HEADER_HEIGHT, HEADER_HEIGHT, ROW_HEIGHT);
			setX(screenW / 2);
			createEntries();
			reload(source);
		}

		@Override
		protected boolean isSelectedItem(int index) {
			return false;//not rendering the defeault selection indicator
		}

		private void reload(CustomPlayerConfigGroupData newSource){
			this.source = newSource;
			clearEntries();
			createEntries();
			setSelected(null);
			setScrollAmount(getScrollAmount());
		}

		private void createEntries(){
			if(syncingOnInit || desyncErrorOnInit != null)
				return;
			if(source == null){
				if(!groupList.children().isEmpty())
					addEntry(new TitleEntry(PLEASE_SELECT_GROUP, children().size()));
				return;
			}
			addEntry(new TitleEntry(INCLUDED_GROUPS, children().size()));
			List<String> sortedIncludedGroups = source.getDirectGroupIds().stream()
					.sorted(Comparator.comparing(String::toLowerCase))
					.toList();
			List<ICustomPlayerGroupMember> sortedIncludedMembers = source.getDirectMembersInternal().stream()
					.sorted(
							Comparator.comparing(m ->
									m.getDisplayLabel().getString().toLowerCase()
							)
					)
					.toList();
			for (String directGroup : sortedIncludedGroups)
				addEntry(new GroupInclusionEntry(directGroup, children().size()));
			addEntry(new TitleEntry(Component.literal(""), children().size()));
			addEntry(new TitleEntry(INCLUDED_PLAYERS, children().size()));
			for (ICustomPlayerGroupMember directMember : sortedIncludedMembers)
				addEntry(new PlayerEntry(directMember, children().size()));
			addEntry(new TitleEntry(Component.literal(""), children().size()));
		}

		@Override
		public boolean isFocused() {
			return PlayerGroupsScreen.this.getFocused() == this;
		}

		@Override
		protected int getScrollbarPosition() {
			return PlayerGroupsScreen.this.width - width / 8;
		}

		@Override
		public int getRowWidth() {
			return width - 32;
		}

		@Override
		public void setSelected(@Nullable AbstractEntry entry) {
			selectionTime = System.currentTimeMillis();
			if(entry != null){
				if(entry != getSelected() && entry instanceof ElementEntry)
					selectionWorksForGroupDeletion = false;//not always done so that the user can TAB-navigate to the group deletion button
				selectionWorksForGroupExclusion = true;
			}
			super.setSelected(entry);
			refreshButtonStates();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if(isMouseOver(mouseX, mouseY)) {
				selectionWorksForGroupDeletion = false;//not navigating with TAB, so can safely disable the group deletion button
				refreshButtonStates();
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		public abstract class AbstractEntry extends ObjectSelectionList.Entry<AbstractEntry> {

			protected final int index;

			protected AbstractEntry(int index) {
				this.index = index;
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if(button == 0) {
					select();
					return true;
				}
				return false;
			}

			private void select(){
				ContentsList.this.setSelected(this);
			}

			protected void renderSelectionIndicator(
					GuiGraphics guiGraphics,
					String indicator,
					int labelX,
					int labelY
			){
				if(isFocused() && (((System.currentTimeMillis() - selectionTime) / 500) & 1) == 1)
					return;
				int indicatorColor = isFocused() ? -1 : LIST_TITLE_LABEL_COLOR;
				guiGraphics.drawString(font, indicator, labelX - 2 - font.width(indicator), labelY, indicatorColor);
			}

		}

		public class TitleEntry extends AbstractEntry {

			private final Component title;

			public TitleEntry(Component title, int index) {
				super(index);
				this.title = title;
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", title);
			}

			@Override
			public void render(
					GuiGraphics guiGraphics,
					int index,
					int y,
					int x,
					int rowWidth,
					int rowHeight,
					int mouseX,
					int mouseY,
					boolean hovered,
					float partialTicks
			){
				boolean isSelected = getSelected() == this;
				int labelX = x + 1;
				int labelY = y + rowHeight / 2 - 2;
				if(isSelected)
					renderSelectionIndicator(guiGraphics, "-", labelX, labelY);
				guiGraphics.drawString(font, title, labelX, labelY, LIST_TITLE_LABEL_COLOR);
				if(hovered)
					guiGraphics.renderComponentHoverEffect(font, title.getStyle(), mouseX, mouseY);
			}

		}

		public abstract class ElementEntry extends AbstractEntry {

			private final Component label;

			public ElementEntry(Component label, int index) {
				super(index);
				this.label = label;
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", label);
			}

			@Override
			public void render(
					GuiGraphics guiGraphics,
					int index,
					int y,
					int x,
					int rowWidth,
					int rowHeight,
					int mouseX,
					int mouseY,
					boolean hovered,
					float partialTicks
			){
				boolean isSelected = getSelected() == this;
				int labelX = x + 10;
				int labelY = y + rowHeight / 2 - 2;
				int labelColor = -1;
//				if(!isSelected)
//					labelColor = hovered ? LIST_TITLE_LABEL_COLOR : 0xFF808080;
				if(isSelected)
					renderSelectionIndicator(guiGraphics, "→", labelX, labelY);
				else if(hovered)
					labelX -= 1;
				guiGraphics.drawString(font, label, labelX, labelY, labelColor);
				if(hovered)
					guiGraphics.renderComponentHoverEffect(font, label.getStyle(), mouseX, mouseY);
			}

		}

		public final class PlayerEntry extends ElementEntry {

			private final ICustomPlayerGroupMember groupMember;

			public PlayerEntry(ICustomPlayerGroupMember groupMember, int index) {
				super(groupMember.getDisplayLabel(), index);
				this.groupMember = groupMember;
			}

			public ICustomPlayerGroupMember getGroupMember() {
				return groupMember;
			}

		}

		public final class GroupInclusionEntry extends ElementEntry {

			private final String groupId;

			public GroupInclusionEntry(String groupId, int index) {
				super(BuiltInPlayerConfigGroupNames.apply(groupId), index);
				this.groupId = groupId;
			}

		}

	}

	private static final class ContentsButtonInfo {

		private final Component label;
		private final Button.OnPress action;
		private final Supplier<ButtonState> stateSupplier;

		private ContentsButtonInfo(Component label, Button.OnPress action, Supplier<ButtonState> stateSupplier) {
			this.label = label;
			this.action = action;
			this.stateSupplier = stateSupplier;
		}

	}

	public static final class Builder {

		private Screen escape;
		private Screen parent;
		private PlayerConfigClientStorage configData;
		private Component configTitle;

		private Builder(){}

		public Builder setDefault(){
			setEscape(null);
			setParent(null);
			setConfigData(null);
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

		public Builder setConfigData(PlayerConfigClientStorage configData){
			this.configData = configData;
			return this;
		}

		public Builder setConfigTitle(Component configTitle) {
			this.configTitle = configTitle;
			return this;
		}

		public PlayerGroupsScreen build(){
			if(configData == null)
				throw new IllegalStateException();
			return new PlayerGroupsScreen(escape, parent, new HashMap<>(), configData, configTitle);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}
	}

	public enum ButtonState {
		ENABLED(null),
		DISABLED(null),
		MAX_GROUPS_REACHED(
				PlayerConfigGroupActionError.GROUP_COUNT_LIMIT.getDesyncScreenMessage().copy()
						.withStyle(s -> s.withColor(ChatFormatting.DARK_RED))
		),
		OUT_OF_SPACE(
				PlayerConfigGroupActionError.OUT_OF_SPACE.getDesyncScreenMessage().copy()
						.withStyle(s -> s.withColor(ChatFormatting.DARK_RED))
		);
		final Tooltip tooltip;
		ButtonState(Component tooltip) {
			this.tooltip = tooltip == null ? null : Tooltip.create(tooltip);
		}
	}

}
