package com.k0.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

import com.k0.client.config.PickaxeSaverConfig;

public class PickaxeSaverClient implements ClientModInitializer {
	private static boolean protectionWasActive;

	@Override
	public void onInitializeClient() {
		PickaxeSaverConfig.load();
	}

	public static boolean shouldBlockMining() {
		Minecraft minecraft = Minecraft.getInstance();
		boolean shouldBlock = false;
		if (PickaxeSaverConfig.isEnabled() && minecraft.player != null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (stack.isDamageableItem() && isProtectedType(stack)) {
				double remainingPercent = (stack.getMaxDamage() - stack.getDamageValue()) * 100.0 / stack.getMaxDamage();
				shouldBlock = remainingPercent <= PickaxeSaverConfig.getDurabilityThreshold();
			}
		}

		if (shouldBlock && !protectionWasActive && minecraft.player != null) {
			minecraft.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
		}
		protectionWasActive = shouldBlock;
		return shouldBlock;
	}

	private static boolean isProtectedType(ItemStack stack) {
		return stack.is(ItemTags.PICKAXES)
			|| PickaxeSaverConfig.protectsCustomItem(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
	}
}