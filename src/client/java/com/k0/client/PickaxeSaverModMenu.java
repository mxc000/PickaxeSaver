package com.k0.client;

import com.k0.client.config.PickaxeSaverConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public class PickaxeSaverModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return PickaxeSaverConfigScreen::new;
	}

	private static final class PickaxeSaverConfigScreen extends Screen {
		private final Screen parent;
		private int selectedCustomItem;
		private Button selectedItemButton;
		private Button thresholdDownButton;
		private Button thresholdUpButton;
		private String feedback = "";

		private PickaxeSaverConfigScreen(Screen parent) {
			super(Component.literal("PickaxeSaver"));
			this.parent = parent;
		}

		@Override
		protected void init() {
			int centerX = this.width / 2;
			int firstY = this.height / 2 - 76;
			this.addRenderableWidget(Button.builder(enabledLabel(), button -> {
				PickaxeSaverConfig.setEnabled(!PickaxeSaverConfig.isEnabled());
				button.setMessage(enabledLabel());
			}).bounds(centerX - 110, firstY, 220, 20).build());

			this.thresholdDownButton = this.addRenderableWidget(Button.builder(Component.literal("-"), button -> {
				PickaxeSaverConfig.setDurabilityThreshold(PickaxeSaverConfig.getDurabilityThreshold() - 1);
				refreshThresholdButtons();
			}).bounds(centerX - 110, firstY + 29, 24, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Lower threshold (min " + PickaxeSaverConfig.MIN_DURABILITY_THRESHOLD + "%)"))).build());
			this.thresholdUpButton = this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
				PickaxeSaverConfig.setDurabilityThreshold(PickaxeSaverConfig.getDurabilityThreshold() + 1);
				refreshThresholdButtons();
			}).bounds(centerX + 86, firstY + 29, 24, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Raise threshold (max " + PickaxeSaverConfig.MAX_DURABILITY_THRESHOLD + "%)"))).build());
			refreshThresholdButtons();

			this.addRenderableWidget(Button.builder(Component.literal("+ Add held item"), button -> {
				if (this.minecraft != null && this.minecraft.player != null) {
					ItemStack heldItem = this.minecraft.player.getMainHandItem();
					if (!heldItem.isEmpty() && heldItem.isDamageableItem()) {
						String itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
						if (PickaxeSaverConfig.addCustomItem(itemId)) {
							selectedCustomItem = PickaxeSaverConfig.getCustomItemCount() - 1;
							feedback = "Added " + heldItem.getHoverName().getString() + ".";
						} else {
							feedback = "That item is already saved.";
						}
					} else {
						feedback = "Hold a damageable item first.";
					}
				}
				refreshSelectedItemButton();
			}).bounds(centerX - 110, firstY + 87, 106, 20).build());

			this.selectedItemButton = this.addRenderableWidget(Button.builder(selectedItemLabel(), button -> {
				if (PickaxeSaverConfig.getCustomItemCount() > 0) {
					selectedCustomItem = (selectedCustomItem + 1) % PickaxeSaverConfig.getCustomItemCount();
					refreshSelectedItemButton();
				}
			}).bounds(centerX - 110, firstY + 57, 220, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Remove selected"), button -> {
				String itemId = PickaxeSaverConfig.getCustomItem(selectedCustomItem);
				if (itemId != null) {
					PickaxeSaverConfig.removeCustomItem(itemId);
					selectedCustomItem = Math.max(0, Math.min(selectedCustomItem, PickaxeSaverConfig.getCustomItemCount() - 1));
					feedback = "Removed " + readableItemName(itemId) + ".";
					refreshSelectedItemButton();
				} else {
					feedback = "No saved item to remove.";
				}
			}).bounds(centerX + 4, firstY + 87, 106, 20).build());

			this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button ->
				this.minecraft.setScreen(this.parent)
			).bounds(centerX - 110, firstY + 126, 220, 20).build());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			super.render(graphics, mouseX, mouseY, partialTick);
			int centerX = this.width / 2;
			int firstY = this.height / 2 - 76;
			int threshold = PickaxeSaverConfig.getDurabilityThreshold();
			graphics.drawCenteredString(this.font, Component.literal("Protect below " + threshold + "% (" + PickaxeSaverConfig.MIN_DURABILITY_THRESHOLD + "-" + PickaxeSaverConfig.MAX_DURABILITY_THRESHOLD + "%)"), centerX, firstY + 34, 0xFFFFFF);
			graphics.drawString(this.font, Component.literal("CUSTOM ITEMS"), centerX - 110, firstY + 44, 0xA0A0A0);
			if (!feedback.isEmpty()) {
				graphics.drawCenteredString(this.font, Component.literal(feedback), centerX, firstY + 112, 0xA0A0A0);
			}
		}

		private static Component enabledLabel() {
			return Component.literal("Protection: " + (PickaxeSaverConfig.isEnabled() ? "On" : "Off"));
		}

		private Component selectedItemLabel() {
			String itemId = PickaxeSaverConfig.getCustomItem(selectedCustomItem);
			if (itemId == null) return Component.literal("No custom items saved");
			return Component.literal(readableItemName(itemId) + " (" + (selectedCustomItem + 1) + "/" + PickaxeSaverConfig.getCustomItemCount() + ")");
		}

		private static String readableItemName(String itemId) {
			String path = itemId.substring(itemId.indexOf(':') + 1);
			String[] words = path.replace('_', ' ').split(" ");
			StringBuilder name = new StringBuilder();
			for (String word : words) {
				if (!name.isEmpty()) name.append(' ');
				name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
			}
			return name.toString();
		}

		private void refreshSelectedItemButton() {
			this.selectedItemButton.setMessage(selectedItemLabel());
		}

		private void refreshThresholdButtons() {
			int threshold = PickaxeSaverConfig.getDurabilityThreshold();
			this.thresholdDownButton.active = threshold > PickaxeSaverConfig.MIN_DURABILITY_THRESHOLD;
			this.thresholdUpButton.active = threshold < PickaxeSaverConfig.MAX_DURABILITY_THRESHOLD;
		}
	}
}