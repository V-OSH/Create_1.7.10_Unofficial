package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.Container;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
	@ModifyExpressionValue(method = "getAnalogOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
	private BlockEntity create$backportCCESuppressionFix(BlockEntity original) {
		return original instanceof Container ? original : null;
	}
}
