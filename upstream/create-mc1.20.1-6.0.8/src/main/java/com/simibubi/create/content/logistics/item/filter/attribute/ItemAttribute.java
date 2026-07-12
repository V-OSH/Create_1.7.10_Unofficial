package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ItemAttribute {
	static CompoundTag saveStatic(ItemAttribute attribute) {
		CompoundTag nbt = new CompoundTag();
		ResourceLocation id = CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.getKey(attribute.getType());

		if (id == null)
			throw new IllegalArgumentException("Cannot get " + attribute.getType() + " as it does not exist in AllRegistries.ITEM_ATTRIBUTE_TYPES");

		nbt.putString("attributeId", id.toString());
		attribute.save(nbt);
		return nbt;
	}

	@Nullable
	static ItemAttribute loadStatic(CompoundTag nbt) {
		for (LegacyDeserializer deserializer : LegacyDeserializer.ALL) {
			if (deserializer.canRead(nbt)) {
				return deserializer.readNBT(nbt.getCompound(deserializer.getNBTKey()));
			}
		}

		ResourceLocation id = ResourceLocation.tryParse(nbt.getString("attributeId"));
		if (id == null)
			return null;

		ItemAttributeType type = CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.get(id);
		if (type == null)
			return null;

		ItemAttribute attribute = type.createAttribute();
		attribute.load(nbt);
		return attribute;
	}

	static List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
		List<ItemAttribute> attributes = new ArrayList<>();
		for (ItemAttributeType type : CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE) {
			attributes.addAll(type.getAllAttributes(stack, level));
		}
		return attributes;
	}

	boolean appliesTo(ItemStack stack, Level world);

	ItemAttributeType getType();

	void save(CompoundTag nbt);

	void load(CompoundTag nbt);

	@OnlyIn(value = Dist.CLIENT)
	default MutableComponent format(boolean inverted) {
		return CreateLang.translateDirect("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
			getTranslationParameters());
	}

	String getTranslationKey();

	default Object[] getTranslationParameters() {
		return new String[0];
	}

	@Deprecated
	interface LegacyDeserializer {
		List<LegacyDeserializer> ALL = new ArrayList<>();

		default boolean canRead(CompoundTag nbt) {
			return nbt.contains(getNBTKey());
		}

		String getNBTKey();

		ItemAttribute readNBT(CompoundTag nbt);
	}
}
