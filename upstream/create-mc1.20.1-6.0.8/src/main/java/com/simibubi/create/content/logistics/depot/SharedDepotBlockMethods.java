package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

public class SharedDepotBlockMethods {

	protected static DepotBehaviour get(BlockGetter worldIn, BlockPos pos) {
		return BlockEntityBehaviour.get(worldIn, pos, DepotBehaviour.TYPE);
	}

	public static InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult ray) {
		if (ray.getDirection() != Direction.UP)
			return InteractionResult.PASS;
		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		DepotBehaviour behaviour = get(world, pos);
		if (behaviour == null)
			return InteractionResult.PASS;
		if (!behaviour.canAcceptItems.get())
			return InteractionResult.SUCCESS;

		ItemStack heldItem = player.getItemInHand(hand);
		boolean wasEmptyHanded = heldItem.isEmpty();
		boolean shouldntPlaceItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem);

		ItemStack mainItemStack = behaviour.getHeldItemStack();
		if (!mainItemStack.isEmpty()) {
			player.getInventory()
				.placeItemBackInInventory(mainItemStack);
			behaviour.removeHeldItem();
			world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
				1f + Create.RANDOM.nextFloat());
		}
		ItemStackHandler outputs = behaviour.processingOutputBuffer;
		for (int i = 0; i < outputs.getSlots(); i++)
			player.getInventory()
				.placeItemBackInInventory(outputs.extractItem(i, 64, false));

		if (!wasEmptyHanded && !shouldntPlaceItem) {
			TransportedItemStack transported = new TransportedItemStack(heldItem);
			transported.insertedFrom = player.getDirection();
			transported.prevBeltPosition = .25f;
			transported.beltPosition = .25f;
			behaviour.setHeldItem(transported);
			player.setItemInHand(hand, ItemStack.EMPTY);
			AllSoundEvents.DEPOT_SLIDE.playOnServer(world, pos);
		}

		behaviour.blockEntity.notifyUpdate();
		return InteractionResult.SUCCESS;
	}

	public static void onLanded(BlockGetter worldIn, Entity entityIn) {
		ItemStack asItem = ItemHelper.fromItemEntity(entityIn);
		if (asItem.isEmpty())
			return;
		if (entityIn.level().isClientSide)
			return;

		BlockPos pos = entityIn.blockPosition();
		DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(worldIn, pos, DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour == null)
			return;
		Vec3 targetLocation = VecHelper.getCenterOf(pos)
			.add(0, 5 / 16f, 0);
		if (!PackageEntity.centerPackage(entityIn, targetLocation))
			return;

		ItemStack remainder = inputBehaviour.handleInsertion(asItem, Direction.DOWN, false);
		if (entityIn instanceof ItemEntity)
			((ItemEntity) entityIn).setItem(remainder);
		if (remainder.isEmpty())
			entityIn.discard();
	}

	public static int getComparatorInputOverride(BlockState blockState, Level worldIn, BlockPos pos) {
		DepotBehaviour depotBehaviour = get(worldIn, pos);
		if (depotBehaviour == null)
			return 0;
		float f = depotBehaviour.getPresentStackSize();
		Integer max = depotBehaviour.maxStackSize.get();
		f = f / (max == 0 ? 64 : max);
		return Mth.clamp(Mth.floor(f * 14.0F) + (f > 0 ? 1 : 0), 0, 15);
	}

}
