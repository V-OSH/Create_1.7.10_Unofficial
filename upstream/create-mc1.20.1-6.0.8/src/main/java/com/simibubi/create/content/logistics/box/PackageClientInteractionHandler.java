package com.simibubi.create.content.logistics.box;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@EventBusSubscriber(value = Dist.CLIENT)
public class PackageClientInteractionHandler {

	// In vanilla, punching an entity doesnt reset the attack timer. This leads to
	// accidentally breaking blocks behind an armorstand or package when punching it
	// in creative mode

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onPlayerPunchPackage(AttackEntityEvent event) {
		Player attacker = event.getEntity();
		if (!attacker.level()
			.isClientSide())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (attacker != mc.player)
			return;
		if (!(event.getTarget() instanceof PackageEntity))
			return;
		ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, 10, "f_91078_");
	}

}
