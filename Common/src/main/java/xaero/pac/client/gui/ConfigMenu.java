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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.IClientDimensionClaimsManager;
import xaero.pac.client.claims.IClientRegionClaims;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.world.capability.ClientWorldMainCapability;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.config.ServerboundOtherPlayerConfigPacket;

public class ConfigMenu extends XPACScreen {
	
	private static final Component ANOTHER_PLAYER_TITLE = Component.translatable("gui.xaero_pac_ui_other_player_config_name_title");
	private boolean serverHasMod;
	private Button partyClaimsConfigButton;
	private Button myPlayerConfigButton;
	private Button serverClaimsConfigButton;
	private Button expiredClaimsConfigButton;
	private Button wildernessConfigButton;
	private Button defaultConfigButton;
	private Button otherPlayerConfigButton;
	private EditBox otherPlayerNameBox;
	private static String otherPlayerNameString = "";

	public ConfigMenu(Screen escape, Screen parent) {
		super(escape, parent, Component.translatable("gui.xaero_pac_ui_config_menu"));
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(myPlayerConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_my_player_config"), this::onPlayerConfigButton).bounds(width / 2 - 205, height / 7 + 8, 200, 20).build());
		addRenderableWidget(partyClaimsConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_party_claims_config"), this::onPartyClaimsConfigButton).bounds(width / 2 - 205, height / 7 + 32, 200, 20).build());
		addRenderableWidget(defaultConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_default_player_config"), this::onDefaultConfigButton).bounds(width / 2 - 205, height / 7 + 56, 200, 20).build());
		addRenderableWidget(serverClaimsConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_server_claims_config"), this::onServerClaimsConfigButton).bounds(width / 2 + 5, height / 7 + 8, 200, 20).build());
		addRenderableWidget(expiredClaimsConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_expired_claims_config"), this::onExpiredClaimsConfigButton).bounds(width / 2 + 5, height / 7 + 32, 200, 20).build());
		addRenderableWidget(wildernessConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_wilderness_config"), this::onWildernessConfigButton).bounds(width / 2 + 5, height / 7 + 56, 200, 20).build());
		addRenderableWidget(otherPlayerNameBox = new EditBox(font, width / 2 - 99, height / 7 + 100, 98, 20, Component.translatable("gui.xaero_pac_ui_other_player_config_name_field")));
		addRenderableWidget(otherPlayerConfigButton = Button.builder(Component.translatable("gui.xaero_pac_ui_other_player_config_button"), this::onOtherPlayerConfigButton).bounds(width / 2, height / 7 + 100, 100, 20).build());
		IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
				claimsManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager();
		updateOtherPlayerButton();
		IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
				configStorage = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		otherPlayerNameBox.setValue(otherPlayerNameString);
		otherPlayerNameBox.setFilter(s -> s.matches(ServerboundOtherPlayerConfigPacket.OWNER_NAME_REGEX));
		otherPlayerNameBox.setResponder(s -> {otherPlayerNameString = s; updateOtherPlayerButton();});
		otherPlayerNameBox.setEditable(mainCap.getClientWorldData().serverHasMod() && configStorage.isAdmin());
		addRenderableWidget(Button.builder(Component.translatable("gui.xaero_pac_back"), this::onBackButton).bounds(width / 2 - 100, this.height / 6 + 168, 200, 20).build());

		serverHasMod = mainCap.getClientWorldData().serverHasMod();
		myPlayerConfigButton.active = serverHasMod && configStorage.getMyPlayerConfig().getPermissions().canView();
		serverClaimsConfigButton.active = serverHasMod && configStorage.getServerClaimsConfig().getPermissions().canView();
		expiredClaimsConfigButton.active = serverHasMod && configStorage.getExpiredClaimsConfig().getPermissions().canView();
		wildernessConfigButton.active = serverHasMod && configStorage.getWildernessConfig().getPermissions().canView();
		defaultConfigButton.active = serverHasMod && configStorage.getDefaultPlayerConfig().getPermissions().canView();
		partyClaimsConfigButton.active = serverHasMod && claimsManager.usingPartyOwnedClaims() && claimsManager.isInParty()
				&& configStorage.getPartyClaimsConfig().getPermissions().canView();
	}

	private void onBackButton(Button b) {
		goBack();
	}
	
	private void onPlayerConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openMyPlayerConfigScreen(escape, this);
	}

	private void onPartyClaimsConfigButton(Button button) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openPartyClaimsConfigScreen(escape, this);
	}
	
	private void onServerClaimsConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openServerClaimsConfigScreen(escape, this);
	}
	
	private void onExpiredClaimsConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openExpiredClaimsConfigScreen(escape, this);
	}
	
	private void onWildernessConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openWildernessConfigScreen(escape, this);
	}
	
	private void onDefaultConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openDefaultPlayerConfigScreen(escape, this);
	}
	
	private boolean isPlayerNameAllowed() {
		return !otherPlayerNameString.isEmpty() && !otherPlayerNameString.equalsIgnoreCase(minecraft.player.getGameProfile().name());
	}
	
	private void onOtherPlayerConfigButton(Button b) {
		if(isPlayerNameAllowed())
			minecraft.setScreen(new OtherPlayerConfigWaitScreen(escape, this, otherPlayerNameString));
	}
	
	private void updateOtherPlayerButton() {
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		otherPlayerConfigButton.active = mainCap.getClientWorldData().serverHasMod() && Commands.LEVEL_GAMEMASTERS.check(minecraft.player.permissions()) && isPlayerNameAllowed();
	}
	
	@Override
	public void tick() {
		super.tick();
//		otherPlayerNameBox.tick();
	}
	
	@Override
	public boolean keyPressed(KeyEvent event) {
		if(otherPlayerNameBox.isFocused() && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && isPlayerNameAllowed()) {
			setFocused(null);
			otherPlayerConfigButton.onPress(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)));
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.centeredText(font, title, width / 2, 16, -1);
		guiGraphics.centeredText(font, ANOTHER_PLAYER_TITLE, width / 2, height / 7 + 85, -1);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partial);
		if (!serverHasMod)
			guiGraphics.centeredText(font, MainMenu.NO_HANDSHAKE, width / 2, 27, 0xFFFF5555);
	}
	
}
