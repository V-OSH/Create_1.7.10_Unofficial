package com.simibubi.create.api.equipment.potatoCannon;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface PotatoProjectileRenderMode {
	Codec<PotatoProjectileRenderMode> CODEC = CreateBuiltInRegistries.POTATO_PROJECTILE_RENDER_MODE.byNameCodec()
		.dispatch(PotatoProjectileRenderMode::codec, Function.identity());

	@OnlyIn(Dist.CLIENT)
	void transform(PoseStack ms, PotatoProjectileEntity entity, float pt);

	Codec<? extends PotatoProjectileRenderMode> codec();
}
