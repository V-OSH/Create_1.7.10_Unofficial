package com.simibubi.create.content.equipment.armor;

import java.util.function.Consumer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CardboardHelmetItem extends CardboardArmorItem {

	public CardboardHelmetItem(Type type, Properties properties) {
		super(type, properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(new CardboardArmorStealthOverlay());
	}

}
