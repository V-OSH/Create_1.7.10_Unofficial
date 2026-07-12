package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

@Mixin(BuiltInRegistries.class)
public interface BuiltInRegistriesAccessor {
	@Accessor("WRITABLE_REGISTRY")
	static WritableRegistry<WritableRegistry<?>> create$getWRITABLE_REGISTRY() {
		throw new AssertionError();
	}
}
