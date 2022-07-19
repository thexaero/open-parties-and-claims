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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.IClientDimensionClaimsManager;
import xaero.pac.client.claims.IClientRegionClaims;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.controls.keybinding.IKeyBindingHelper;
import xaero.pac.client.gui.component.CachedComponentSupplier;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.client.parties.party.IClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientPartyMemberDynamicInfoSyncableStorage;
import xaero.pac.client.parties.party.IClientPartyStorage;
import xaero.pac.client.world.capability.ClientWorldMainCapability;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.LazyPacketsConfirmationPacket;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.claims.command.ClaimsCommandRegister;
import xaero.pac.common.server.parties.command.PartyCommandRegister;

import java.util.function.Consumer;

public class MainMenu extends XPACScreen {
	
	public static final TranslatableComponent NO_HANDSHAKE = new TranslatableComponent("gui.xaero_pac_ui_handshake_not_received");
	public static final TranslatableComponent NO_PARTIES = new TranslatableComponent("gui.xaero_pac_ui_parties_disabled");
	public static final TranslatableComponent NO_CLAIMS = new TranslatableComponent("gui.xaero_pac_ui_claims_disabled");
	public static final TranslatableComponent PARTY_SYNCING = new TranslatableComponent("gui.xaero_pac_ui_party_syncing");
	public static final TranslatableComponent CLAIMS_SYNCING = new TranslatableComponent("gui.xaero_pac_ui_claims_syncing");
	
	private static final Component ABOUT_PARTY_COMMAND = new TextComponent("/" + PartyCommandRegister.COMMAND_PREFIX + " about");

	public static final TranslatableComponent CLAIM = new TranslatableComponent("gui.xaero_pac_ui_claim");
	public static final TranslatableComponent UNCLAIM = new TranslatableComponent("gui.xaero_pac_ui_unclaim");
	private static final Component CLAIM_COMMAND = new TextComponent("/" + ClaimsCommandRegister.COMMAND_PREFIX + " claim");
	private static final Component UNCLAIM_COMMAND = new TextComponent("/" + ClaimsCommandRegister.COMMAND_PREFIX + " unclaim");
	
	public static final TranslatableComponent FORCELOAD = new TranslatableComponent("gui.xaero_pac_ui_forceload");
	public static final TranslatableComponent UNFORCELOAD = new TranslatableComponent("gui.xaero_pac_ui_unforceload");
	private static final Component FORCELOAD_COMMAND = new TextComponent("/" + ClaimsCommandRegister.COMMAND_PREFIX + " forceload");
	private static final Component UNFORCELOAD_COMMAND = new TextComponent("/" + ClaimsCommandRegister.COMMAND_PREFIX + " unforceload");
	private static final CachedComponentSupplier partyNameSupplier = new CachedComponentSupplier(args -> {
		String currentPartyName = (String) args[0];
		return new TranslatableComponent("gui.xaero_pac_ui_party_name", new TextComponent(currentPartyName).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier ownerNameSupplier = new CachedComponentSupplier(args -> {
		String currentOwnerName = (String) args[0];
		return new TranslatableComponent("gui.xaero_pac_ui_party_owner", new TextComponent(currentOwnerName).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier memberCountSupplier = new CachedComponentSupplier(args -> {
		int currentMemberCount = (Integer) args[0];
		int currentMemberLimit = (Integer) args[1];
		return new TranslatableComponent("gui.xaero_pac_ui_party_member_count", new TextComponent(currentMemberCount + " / " + currentMemberLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier allyCountSupplier = new CachedComponentSupplier(args -> {
		int currentAllyCount = (Integer) args[0];
		int currentAllyLimit = (Integer) args[1];
		return new TranslatableComponent("gui.xaero_pac_ui_party_ally_count", new TextComponent(currentAllyCount + " / " + currentAllyLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier inviteCountSupplier = new CachedComponentSupplier(args -> {
		int currentInviteCount = (Integer) args[0];
		int currentInviteLimit = (Integer) args[1];
		return new TranslatableComponent("gui.xaero_pac_ui_party_invite_count", new TextComponent(currentInviteCount + " / " + currentInviteLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});

	private static final CachedComponentSupplier claimsNameSupplier = new CachedComponentSupplier(args -> {
		String currentClaimsName = (String) args[0];
		Component nameComponent = new TextComponent(currentClaimsName).withStyle(s -> s.withColor(0xFFAAAAAA));
		return new TranslatableComponent("gui.xaero_pac_ui_claims_name", nameComponent);
	});
	private static final CachedComponentSupplier claimCountSupplier = new CachedComponentSupplier(args -> {
		int currentClaimCount = (Integer) args[0];
		int currentClaimLimit = (Integer) args[1];
		Component numbers = new TextComponent(currentClaimCount + " / " + currentClaimLimit).withStyle(s -> s.withColor(0xFFAAAAAA));
		return new TranslatableComponent("gui.xaero_pac_ui_claim_count", numbers);
	});
	private static final CachedComponentSupplier forceloadCountSupplier = new CachedComponentSupplier(args -> {
		int currentForceloadCount = (Integer) args[0];
		int currentForceloadLimit = (Integer) args[1];
		return new TranslatableComponent("gui.xaero_pac_ui_forceload_count", new TextComponent(currentForceloadCount + " / " + currentForceloadLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier claimsColorSupplier = new CachedComponentSupplier(args -> {
		int currentClaimColor = (Integer) args[0];
		Component colorComponent = new TextComponent(Integer.toUnsignedString(currentClaimColor, 16).toUpperCase()).withStyle(s -> s.withColor(currentClaimColor));
		return new TranslatableComponent("gui.xaero_pac_ui_claims_color", colorComponent);
	});

	private boolean serverHasMod;
	private boolean serverHasClaimsEnabled;
	private boolean serverHasPartiesEnabled;
	private Button configsButton;
	private Button aboutPartyButton;
	private Button claimButton;
	private Button forceloadButton;
	public static boolean TEST_TOGGLE;

	public MainMenu(Screen escape, Screen parent) {
		super(escape, parent, new TranslatableComponent("gui.xaero_pac_ui_main_menu"));
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(configsButton = new Button(width / 2 - 100, height / 7 + 8, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_config_menu"), this::onConfigsButton));
		
		aboutPartyButton = new Button(width / 2 - 100, height / 7 + 40, 70, 20, new TranslatableComponent("gui.xaero_pac_ui_about_party"), this::onAboutPartyButton, new Button.OnTooltip() {
			public void onTooltip(Button p_170019_, PoseStack p_170020_, int p_170021_, int p_170022_) {
				 MainMenu.this.renderTooltip(p_170020_, ABOUT_PARTY_COMMAND, p_170021_, p_170022_);
			}

			public void narrateTooltip(Consumer<Component> p_170017_) {
				p_170017_.accept(ABOUT_PARTY_COMMAND);
			}
		});
		
		claimButton = new Button(width / 2 - 100, height / 7 + 112, 70, 20, CLAIM, this::onClaimButton, new Button.OnTooltip() {
			public void onTooltip(Button p_170019_, PoseStack p_170020_, int p_170021_, int p_170022_) {
				MainMenu.this.renderTooltip(p_170020_, p_170019_.getMessage() == CLAIM ? CLAIM_COMMAND : UNCLAIM_COMMAND, p_170021_, p_170022_);
			}

			public void narrateTooltip(Consumer<Component> p_170017_) {
				p_170017_.accept(claimButton.getMessage() == CLAIM ? CLAIM_COMMAND : UNCLAIM_COMMAND);
			}
		});
		
		forceloadButton = new Button(width / 2 - 100, height / 7 + 136, 70, 20, FORCELOAD, this::onForceloadButton, new Button.OnTooltip() {
			public void onTooltip(Button p_170019_, PoseStack p_170020_, int p_170021_, int p_170022_) {
				MainMenu.this.renderTooltip(p_170020_, p_170019_.getMessage() == FORCELOAD ? FORCELOAD_COMMAND : UNFORCELOAD_COMMAND, p_170021_, p_170022_);
			}

			public void narrateTooltip(Consumer<Component> p_170017_) {
				p_170017_.accept(claimButton.getMessage() == FORCELOAD ? FORCELOAD_COMMAND : UNFORCELOAD_COMMAND);
			}
		});
		
		addRenderableWidget(new Button(width / 2 - 100, this.height / 6 + 168, 200, 20, new TranslatableComponent("gui.xaero_pac_back"), this::onBackButton));

		//addRenderableWidget(new Button(0, 0, 40, 20, new TextComponent("test toggle"), this::onTestToggle));

		updateButtons();

		if(serverHasPartiesEnabled){
			addRenderableWidget(aboutPartyButton);
		}
		if(serverHasClaimsEnabled){
			addRenderableWidget(claimButton);
			addRenderableWidget(forceloadButton);
		}

		minecraft.keyboardHandler.setSendRepeatsToGui(true);
	}

	private void onTestToggle(Button button) {
		TEST_TOGGLE = !TEST_TOGGLE;
		if(!TEST_TOGGLE)
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new LazyPacketsConfirmationPacket());
		OpenPartiesAndClaims.LOGGER.info("test toggle set to " + TEST_TOGGLE);
	}

	private void updateButtons() {
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		serverHasMod = configsButton.active = mainCap.getClientWorldData().serverHasMod();
		serverHasClaimsEnabled = mainCap.getClientWorldData().serverHasClaimsEnabled();
		serverHasPartiesEnabled = mainCap.getClientWorldData().serverHasPartiesEnabled();
		aboutPartyButton.active = serverHasMod && OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty() != null;
		
		claimButton.active = forceloadButton.active = false;
		if(serverHasMod && !OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isLoading()) {
			IPlayerChunkClaim currentClaim = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().get(minecraft.level.dimension().location(), minecraft.player.chunkPosition().x, minecraft.player.chunkPosition().z);
			boolean adminMode = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isAdminMode();
			claimButton.active = adminMode || (currentClaim == null || currentClaim.getPlayerId().equals(minecraft.player.getUUID()));
			claimButton.setMessage(currentClaim == null ? CLAIM : UNCLAIM);
			
			forceloadButton.active = adminMode || currentClaim != null && currentClaim.getPlayerId().equals(minecraft.player.getUUID());
			forceloadButton.setMessage(currentClaim == null || !currentClaim.isForceloadable() ? FORCELOAD : UNFORCELOAD);
		}
	}
	
	private void onConfigsButton(Button b) {
		minecraft.setScreen(new ConfigMenu(escape, this));
	}
	
	private void onAboutPartyButton(Button b) {
		sendMessage(ABOUT_PARTY_COMMAND.getString());
		minecraft.setScreen(null);
	}
	
	private void onClaimButton(Button b) {
		IPlayerChunkClaim currentClaim = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().get(minecraft.level.dimension().location(), minecraft.player.chunkPosition().x, minecraft.player.chunkPosition().z);
		if(currentClaim == null)
			sendMessage(CLAIM_COMMAND.getString());
		else
			sendMessage(UNCLAIM_COMMAND.getString());
		onClose();
	}
	
	private void onForceloadButton(Button b) {
		IPlayerChunkClaim currentClaim = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().get(minecraft.level.dimension().location(), minecraft.player.chunkPosition().x, minecraft.player.chunkPosition().z);
		if(currentClaim == null)
			return;
		if(!currentClaim.isForceloadable())
			sendMessage(FORCELOAD_COMMAND.getString());
		else
			sendMessage(UNFORCELOAD_COMMAND.getString());
		onClose();
	}
	
	private void onBackButton(Button b) {
		goBack();
	}

	private void drawPartyInfo(PoseStack poseStack, int mouseX, int mouseY, float partial){
		IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>
				partyStorage = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage();
		String actualPartyName = partyStorage.getPartyName();
		if(actualPartyName == null || actualPartyName.isEmpty())
			actualPartyName = "N/A";
		drawString(poseStack, font, partyNameSupplier.get(actualPartyName), width / 2 - 24, height / 7 + 42, -1);
		if(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty() != null) {
			String actualOwnerName = partyStorage.getParty().getOwner().getUsername();
			drawString(poseStack, font, ownerNameSupplier.get(actualOwnerName), width / 2 - 24, height / 7 + 54, -1);
			drawString(poseStack, font, memberCountSupplier.get(partyStorage.getUIMemberCount(), partyStorage.getMemberLimit()), width / 2 - 24, height / 7 + 66, -1);
			drawString(poseStack, font, allyCountSupplier.get(partyStorage.getUIAllyCount(), partyStorage.getAllyLimit()), width / 2 - 24, height / 7 + 78, -1);
			drawString(poseStack, font, inviteCountSupplier.get(partyStorage.getUIInviteCount(), partyStorage.getInviteLimit()), width / 2 - 24, height / 7 + 90, -1);
		}
	}

	private void drawClaimsInfo(PoseStack poseStack, int mouseX, int mouseY, float partial){
		IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
				claimsManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager();
		if(claimsManager.hasPlayerInfo(minecraft.player.getUUID())) {
			IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = claimsManager.getPlayerInfo(minecraft.player.getUUID());

			boolean shouldUseLoadingValues = claimsManager.isLoading() || claimsManager.getAlwaysUseLoadingValues();

			int claimCount = shouldUseLoadingValues ? claimsManager.getLoadingClaimCount() : playerInfo.getClaimCount();
			int claimLimit = claimsManager.getClaimLimit();
			int forceloadCount = shouldUseLoadingValues ? claimsManager.getLoadingForceloadCount() : playerInfo.getForceloadCount();
			int forceloadLimit = claimsManager.getForceloadLimit();
			String claimsName = playerInfo.getClaimsName();
			if(claimsName == null || claimsName.isEmpty())
				claimsName = "N/A";
			int claimsColor = playerInfo.getClaimsColor();

			drawString(poseStack, font, claimCountSupplier.get(claimCount, claimLimit), width / 2 - 24, height / 7 + 114, -1);
			drawString(poseStack, font, forceloadCountSupplier.get(forceloadCount, forceloadLimit), width / 2 - 24, height / 7 + 126, -1);
			drawString(poseStack, font, claimsNameSupplier.get(claimsName), width / 2 - 24, height / 7 + 138, -1);
			drawString(poseStack, font, claimsColorSupplier.get(claimsColor), width / 2 - 24, height / 7 + 150, -1);
		}

	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		updateButtons();
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 16, -1);
		super.render(poseStack, mouseX, mouseY, partial);
		if(!serverHasMod)
			drawCenteredString(poseStack, font, NO_HANDSHAKE, width / 2, 27, 0xFFFF5555);
		else {
			if(serverHasPartiesEnabled) {
				if (OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().isLoading())
					drawString(poseStack, font, PARTY_SYNCING, width / 2 - 104 - font.width(PARTY_SYNCING), height / 7 + 42, -1);
				drawPartyInfo(poseStack, mouseX, mouseY, partial);
			} else
				drawCenteredString(poseStack, font, NO_PARTIES, width / 2, height / 7 + 42, 0xFFAAAAAA);

			if(serverHasClaimsEnabled) {
				if (OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isLoading())
					drawString(poseStack, font, CLAIMS_SYNCING, width / 2 - 104 - font.width(CLAIMS_SYNCING), height / 7 + 114, -1);
				drawClaimsInfo(poseStack, mouseX, mouseY, partial);
			} else
				drawCenteredString(poseStack, font, NO_CLAIMS, width / 2, height / 7 + 114, 0xFFAAAAAA);
		}
	}
	
	@Override
	public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
		IKeyBindingHelper keyBindingHelper = Services.PLATFORM.getKeyBindingHelper();
		if(getFocused() == null && keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getType() == InputConstants.Type.KEYSYM
				&&
				p_96552_ == keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getValue()
				) {
			onClose();
			return true;
		}
		return super.keyPressed(p_96552_, p_96553_, p_96554_);
	}
	
	@Override
	public boolean mouseClicked(double p_94695_, double p_94696_, int p_94697_) {
		IKeyBindingHelper keyBindingHelper = Services.PLATFORM.getKeyBindingHelper();
		if(getFocused() == null && keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getType() == InputConstants.Type.MOUSE
				&&
				p_94697_ == keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getValue()
				) {
			onClose();
			return true;
		}
		return super.mouseClicked(p_94695_, p_94696_, p_94697_);
	}
	
	@Override
	public void setFocused(GuiEventListener l) {//setFocused
		GuiEventListener currentFocused = getFocused();
		if(currentFocused != null && currentFocused != l && currentFocused instanceof EditBox)
			((EditBox) currentFocused).setFocus(false);
		super.setFocused(l);
	}
	
}
