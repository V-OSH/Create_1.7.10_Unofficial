package com.simibubi.create.api.contraption.storage.item.simple;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;

import com.simibubi.create.foundation.utility.CreateCodecs;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Widely-applicable mounted storage implementation.
 * Gets an item handler from the mounted block, copies it to an ItemStackHandler,
 * and then copies the inventory back to the target when unmounting.
 * All blocks for which this mounted storage is registered must provide an
 * {@link IItemHandlerModifiable} to {@link ForgeCapabilities#ITEM_HANDLER}.
 * <br>
 * To use this implementation, either register {@link AllMountedStorageTypes#SIMPLE} to your block
 * manually, or add your block to the {@link AllTags.AllBlockTags#SIMPLE_MOUNTED_STORAGE} tag.
 * It is also possible to extend this class to create your own implementation.
 */
public class SimpleMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final Codec<SimpleMountedStorage> CODEC = codec(SimpleMountedStorage::new);

	public SimpleMountedStorage(MountedItemStorageType<?> type, IItemHandler handler) {
		super(type, copyToItemStackHandler(handler));
	}

	public SimpleMountedStorage(IItemHandler handler) {
		this(AllMountedStorageTypes.SIMPLE.get(), handler);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be == null)
			return;

		be.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().flatMap(this::validate).ifPresent(handler -> {
			for (int i = 0; i < handler.getSlots(); i++) {
				handler.setStackInSlot(i, this.getStackInSlot(i));
			}
		});
	}

	/**
	 * Make sure the targeted handler is valid for copying items back into.
	 * It is highly recommended to call super in overrides.
	 */
	protected Optional<IItemHandlerModifiable> validate(IItemHandler handler) {
		if (handler.getSlots() == this.getSlots() && handler instanceof IItemHandlerModifiable modifiable) {
			return Optional.of(modifiable);
		} else {
			return Optional.empty();
		}
	}

	public static <T extends SimpleMountedStorage> Codec<T> codec(Function<IItemHandler, T> factory) {
		return CreateCodecs.ITEM_STACK_HANDLER.xmap(factory, storage -> storage.wrapped);
	}
}
