package com.simibubi.create.content.equipment.tool;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum AllToolMaterials implements Tier {

	CARDBOARD(Create.asResource("cardboard")
		.toString(), 0, 1, 2, 1, () -> Ingredient.of(AllItems.CARDBOARD.asItem()));

	public String name;

	private int uses;
	private float speed;
	private float damageBonus;
	private int enchantValue;
	private Supplier<Ingredient> repairMaterial;

	private AllToolMaterials(String name, int uses, float speed, float damageBonus, int enchantValue,
							 Supplier<Ingredient> repairMaterial) {
		this.name = name;
		this.uses = uses;
		this.speed = speed;
		this.damageBonus = damageBonus;
		this.enchantValue = enchantValue;
		this.repairMaterial = repairMaterial;
	}

	@Override
	public int getUses() {
		return uses;
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	@Override
	public float getAttackDamageBonus() {
		return damageBonus;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public int getEnchantmentValue() {
		return enchantValue;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return repairMaterial.get();
	}

}
