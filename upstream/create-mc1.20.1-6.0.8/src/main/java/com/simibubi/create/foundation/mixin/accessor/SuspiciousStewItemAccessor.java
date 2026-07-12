package com.simibubi.create.foundation.mixin.accessor;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;

@Mixin(SuspiciousStewItem.class)
public interface SuspiciousStewItemAccessor {
	@Invoker("listPotionEffects")
	static void create$listPotionEffects(ItemStack stack, Consumer<MobEffectInstance> output) {
		throw new AssertionError();
	}
}
