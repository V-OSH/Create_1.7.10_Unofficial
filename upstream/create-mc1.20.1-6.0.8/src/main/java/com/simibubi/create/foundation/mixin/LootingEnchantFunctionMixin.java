package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllDamageTypes;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

@Mixin(LootingEnchantFunction.class)
public abstract class LootingEnchantFunctionMixin {
	@Shadow
	@Final
	NumberProvider value;

	@Shadow
	@Final
	int limit;

	@Shadow
	abstract boolean hasLimit();

	@Inject(method = "run", at = @At("TAIL"))
	private void create$crushingWheelsHaveLooting(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
		DamageSource damageSource = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
		if (damageSource != null && damageSource.is(AllDamageTypes.CRUSH)) {
			int lootingLevel = 2;

			float f = (float) lootingLevel * this.value.getFloat(context);
			stack.grow(Math.round(f));
			if (this.hasLimit() && stack.getCount() > this.limit)
				stack.setCount(this.limit);
		}
	}
}
