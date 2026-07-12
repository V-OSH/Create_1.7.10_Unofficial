package com.simibubi.create.impl.registry;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.registry.CreateRegistries;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(bus = Bus.MOD)
public class CreateRegistriesImpl {
	@Internal
	@SubscribeEvent
	public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(
			CreateRegistries.POTATO_PROJECTILE_TYPE,
			PotatoCannonProjectileType.CODEC,
			PotatoCannonProjectileType.CODEC
		);
	}
}
