package com.simibubi.create.impl.effect;

import java.util.List;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.fluids.FluidStack;

public class MilkEffectHandler implements OpenPipeEffectHandler {
	@Override
	public void apply(Level level, AABB area, FluidStack fluid) {
		if (level.getGameTime() % 5 != 0)
			return;

		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions);
		ItemStack curativeItem = new ItemStack(Items.MILK_BUCKET);
		for (LivingEntity entity : entities) {
			entity.curePotionEffects(curativeItem);
		}
	}
}
