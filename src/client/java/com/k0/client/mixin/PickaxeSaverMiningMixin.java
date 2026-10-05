package com.k0.client.mixin;

import com.k0.client.PickaxeSaverClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class PickaxeSaverMiningMixin {
	@Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void pickaxesaver$preventStartingMining(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (PickaxeSaverClient.shouldBlockMining()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void pickaxesaver$preventContinuingMining(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (PickaxeSaverClient.shouldBlockMining()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void pickaxesaver$preventAttacking(Player player, Entity target, CallbackInfo ci) {
		if (PickaxeSaverClient.shouldBlockMining()) {
			ci.cancel();
		}
	}
}