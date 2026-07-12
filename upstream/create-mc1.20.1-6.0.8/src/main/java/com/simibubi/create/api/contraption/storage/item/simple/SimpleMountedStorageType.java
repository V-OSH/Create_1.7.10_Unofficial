package com.simibubi.create.api.contraption.storage.item.simple;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraftforge.common.capabilities.ForgeCapabilities;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SimpleMountedStorageType<T extends SimpleMountedStorage> extends MountedItemStorageType<SimpleMountedStorage> {
	protected SimpleMountedStorageType(Codec<T> codec) {
		super(codec);
	}

	@Override
	@Nullable
	public SimpleMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return Optional.ofNullable(be)
			.map(this::getHandler)
			.map(this::createStorage)
			.orElse(null);
	}

	protected IItemHandler getHandler(BlockEntity be) {
		IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		// make sure the handler is modifiable so new contents can be moved over on disassembly
		return handler instanceof IItemHandlerModifiable modifiable ? modifiable : null;
	}

	protected SimpleMountedStorage createStorage(IItemHandler handler) {
		return new SimpleMountedStorage(this, handler);
	}

	public static final class Impl extends SimpleMountedStorageType<SimpleMountedStorage> {
		public Impl() {
			super(SimpleMountedStorage.CODEC);
		}
	}
}
