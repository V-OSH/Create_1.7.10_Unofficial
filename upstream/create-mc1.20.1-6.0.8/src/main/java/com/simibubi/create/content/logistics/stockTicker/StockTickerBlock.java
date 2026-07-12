package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class StockTickerBlock extends HorizontalDirectionalBlock implements IBE<StockTickerBlockEntity>, IWrenchable {

	public StockTickerBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction facing = pContext.getHorizontalDirection()
			.getOpposite();
		boolean reverse = pContext.getPlayer() != null && pContext.getPlayer()
			.isShiftKeyDown();
		return super.getStateForPlacement(pContext).setValue(FACING, reverse ? facing.getOpposite() : facing);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACING));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		if (pPlayer != null && pPlayer.getItemInHand(pHand)
			.getItem() instanceof LogisticallyLinkedBlockItem)
			return InteractionResult.PASS;

		return onBlockEntityUse(pLevel, pPos, stbe -> {
			if (!stbe.behaviour.mayInteractMessage(pPlayer))
				return InteractionResult.SUCCESS;

			if (!pLevel.isClientSide() && !stbe.receivedPayments.isEmpty()) {
				for (int i = 0; i < stbe.receivedPayments.getSlots(); i++)
					pPlayer.getInventory()
						.placeItemBackInInventory(
							stbe.receivedPayments.extractItem(i, stbe.receivedPayments.getStackInSlot(i)
								.getCount(), false));
				AllSoundEvents.playItemPickup(pPlayer);
				return InteractionResult.SUCCESS;
			}

			if (pPlayer instanceof ServerPlayer sp) {
				if (stbe.isKeeperPresent())
					NetworkHooks.openScreen(sp, stbe.new CategoryMenuProvider(), stbe.getBlockPos());
				else
					CreateLang.translate("stock_ticker.keeper_missing")
						.sendStatus(pPlayer);
			}

			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.STOCK_TICKER;
	}

	@OnlyIn(Dist.CLIENT)
	public PartialModel getHat(LevelAccessor level, BlockPos pos, LivingEntity keeper) {
		return AllPartialModels.LOGISTICS_HAT;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public Class<StockTickerBlockEntity> getBlockEntityClass() {
		return StockTickerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends StockTickerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.STOCK_TICKER.get();
	}
	
	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return false;
	}

}
