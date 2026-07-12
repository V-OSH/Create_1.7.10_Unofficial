package com.simibubi.create.impl.contraption.storage;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class FallbackMountedStorageType extends SimpleMountedStorageType<FallbackMountedStorage> {
	public FallbackMountedStorageType() {
		super(FallbackMountedStorage.CODEC);
	}

	@Override
	protected IItemHandler getHandler(BlockEntity be) {
		IItemHandler handler = super.getHandler(be);
		return handler != null && FallbackMountedStorage.isValid(handler) ? handler : null;
	}
}
