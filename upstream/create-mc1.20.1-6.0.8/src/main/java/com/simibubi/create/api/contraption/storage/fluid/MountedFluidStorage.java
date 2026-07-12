package com.simibubi.create.api.contraption.storage.fluid;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;

public abstract class MountedFluidStorage implements IFluidHandler {
	public static final Codec<MountedFluidStorage> CODEC = MountedFluidStorageType.CODEC.dispatch(
		storage -> storage.type, type -> type.codec
	);

	public final MountedFluidStorageType<? extends MountedFluidStorage> type;

	protected MountedFluidStorage(MountedFluidStorageType<?> type) {
		this.type = Objects.requireNonNull(type);
	}

	/**
	 * Un-mount this storage back into the world. The expected storage type of the target
	 * block has already been checked to make sure it matches this storage's type.
	 */
	public abstract void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
