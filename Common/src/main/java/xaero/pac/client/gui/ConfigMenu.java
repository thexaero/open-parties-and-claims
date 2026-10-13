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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.IClientDimensionClaimsManager;
import xaero.pac.client.claims.IClientRegionClaims;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.claims.player.mode.ClaimingModeClientHandlers;
import xaero.pac.client.claims.player.mode.ClientClaimingModeHandler;
import xaero.pac.client.gui.widget.FixedEditBox;
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
	
	private static final TranslatableComponent ANOTHER_PLAYER_TITLE = new TranslatableComponent("gui.xaero_pac_ui_other_player_config_name_title");
	private static final TranslatableComponent MY_PLAYER_CONFIG_TOOLTIP = new TranslatableComponent("gui.xaero_pac_ui_my_player_config_tooltip");
	private static final TranslatableComponent PARTY_CLAIMS_CONFIG_TOOLTIP = new TranslatableComponent("gui.xaero_pac_ui_party_claims_config_tooltip");
	private static final TranslatableComponent DEFAULT_PLAYER_CONFIG_TOOLTIP = new TranslatableComponent("gui.xaero_pac_ui_default_player_config_tooltip");
	private static final TranslatableComponent SERVER_CLAIMS_CONFIG_TOOLTIP = new TranslatableComponent("gui.xaero_pac_ui_server_claims_config_tooltip");
	private static final TranslatableComponent EXPIRED_CLAIMS_CONFIG_TOOLTIP = new TranslatableComponent("gui.xaero_pac_ui_expired_claims_config_tooltip");
	private static final TranslatableComponent WILDERNESS_CONFIG_TOOLTIP = new TranslatableComponent("gui.xaero_pac_ui_wilderness_config_tooltip");
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
		super(escape, parent, new TranslatableComponent("gui.xaero_pac_ui_config_menu"));
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(myPlayerConfigButton = new Button(width / 2 - 205, height / 7, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_my_player_config"), this::onPlayerConfigButton, (b, s, x, y) -> onButtonTooltip(MY_PLAYER_CONFIG_TOOLTIP, b, s, x, y)));
		addRenderableWidget(partyClaimsConfigButton = new Button(width / 2 - 205, height / 7 + 24, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_party_claims_config"), this::onPartyClaimsConfigButton, (b, s, x, y) -> onButtonTooltip(PARTY_CLAIMS_CONFIG_TOOLTIP, b, s, x, y)));
		addRenderableWidget(defaultConfigButton = new Button(width / 2 - 205, height / 7 + 48, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_default_player_config"), this::onDefaultConfigButton, (b, s, x, y) -> onButtonTooltip(DEFAULT_PLAYER_CONFIG_TOOLTIP, b, s, x, y)));
		addRenderableWidget(serverClaimsConfigButton = new Button(width / 2 + 5, height / 7, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_server_claims_config"), this::onServerClaimsConfigButton, (b, s, x, y) -> onButtonTooltip(SERVER_CLAIMS_CONFIG_TOOLTIP, b, s, x, y)));
		addRenderableWidget(expiredClaimsConfigButton = new Button(width / 2 + 5, height / 7 + 24, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_expired_claims_config"), this::onExpiredClaimsConfigButton, (b, s, x, y) -> onButtonTooltip(EXPIRED_CLAIMS_CONFIG_TOOLTIP, b, s, x, y)));
		addRenderableWidget(wildernessConfigButton = new Button(width / 2 + 5, height / 7 + 48, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_wilderness_config"), this::onWildernessConfigButton, (b, s, x, y) -> onButtonTooltip(WILDERNESS_CONFIG_TOOLTIP, b, s, x, y)));
		addRenderableWidget(otherPlayerNameBox = new FixedEditBox(font, width / 2 - 99, height / 7 + 92, 98, 20, new TranslatableComponent("gui.xaero_pac_ui_other_player_config_name_field")));
		addRenderableWidget(otherPlayerConfigButton = new Button(width / 2, height / 7 + 92, 100, 20, new TranslatableComponent("gui.xaero_pac_ui_other_player_config_button"), this::onOtherPlayerConfigButton));
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
		addRenderableWidget(new Button(width / 2 - 100, this.height / 7 + 171, 200, 20, new TranslatableComponent("gui.xaero_pac_back"), this::onBackButton));

		serverHasMod = mainCap.getClientWorldData().serverHasMod();
		myPlayerConfigButton.active = serverHasMod && ClaimingModeClientHandlers.PLAYER.isConfigScreenViewable(configStorage);
		serverClaimsConfigButton.active = serverHasMod && ClaimingModeClientHandlers.SERVER.isConfigScreenViewable(configStorage);
		expiredClaimsConfigButton.active = serverHasMod && configStorage.getExpiredClaimsConfig().getPermissions().canView();
		wildernessConfigButton.active = serverHasMod && configStorage.getWildernessConfig().getPermissions().canView();
		defaultConfigButton.active = serverHasMod && configStorage.getDefaultPlayerConfig().getPermissions().canView();
		partyClaimsConfigButton.active = serverHasMod && ClaimingModeClientHandlers.PARTY.isConfigScreenViewable(configStorage);
		minecraft.keyboardHandler.setSendRepeatsToGui(true);
	}

	private void onButtonTooltip(Component tooltip, Button b, PoseStack stack, int x, int y){
		if(b.isMouseOver(x, y) && (openDropdown == null || !openDropdown.onDropDown(x, y, height)))
			nextTooltip = font.split(tooltip, 200);
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
		return !otherPlayerNameString.isEmpty() && !otherPlayerNameString.equalsIgnoreCase(minecraft.player.getGameProfile().getName());
	}
	
	private void onOtherPlayerConfigButton(Button b) {
		if(isPlayerNameAllowed())
			minecraft.setScreen(new OtherPlayerConfigWaitScreen(escape, this, otherPlayerNameString));
	}
	
	private void updateOtherPlayerButton() {
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		otherPlayerConfigButton.active = mainCap.getClientWorldData().serverHasMod() && minecraft.player.hasPermissions(2) && isPlayerNameAllowed();
	}
	
	@Override
	public void tick() {
		super.tick();
		otherPlayerNameBox.tick();
	}
	
	@Override
	public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
		if(otherPlayerNameBox.isFocused() && (p_96552_ == GLFW.GLFW_KEY_ENTER || p_96552_ == GLFW.GLFW_KEY_KP_ENTER) && isPlayerNameAllowed()) {
			setFocused(null);
			otherPlayerConfigButton.onPress();
			return true;
		}
		return super.keyPressed(p_96552_, p_96553_, p_96554_);
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, height / 7 - 17, -1);
		drawCenteredString(poseStack, font, ANOTHER_PLAYER_TITLE, width / 2, height / 7 + 77, -1);
		super.render(poseStack, mouseX, mouseY, partial);
		if(!serverHasMod)
			drawCenteredString(poseStack, font, MainMenu.NO_HANDSHAKE, width / 2, height / 7 - 6, 0xFFFF5555);
	}
	
	@Override
	public void setFocused(GuiEventListener l) {//setFocused
		GuiEventListener currentFocused = getFocused();
		if(currentFocused != null && currentFocused != l && currentFocused instanceof EditBox)
			((EditBox) currentFocused).setFocus(false);
		super.setFocused(l);
	}

}
