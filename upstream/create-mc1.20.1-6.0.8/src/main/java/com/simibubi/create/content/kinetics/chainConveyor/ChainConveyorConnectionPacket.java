package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ChainConveyorConnectionPacket extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {

	private BlockPos targetPos;
	private boolean connect;
	private ItemStack chain;

	public ChainConveyorConnectionPacket(BlockPos pos, BlockPos targetPos, ItemStack chain, boolean connect) {
		super(pos);
		this.targetPos = targetPos;
		this.chain = chain;
		this.connect = connect;
	}

	public ChainConveyorConnectionPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(targetPos);
		buffer.writeBoolean(connect);
		buffer.writeItem(chain);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		targetPos = buffer.readBlockPos();
		connect = buffer.readBoolean();
		chain = buffer.readItem();
	}

	@Override
	protected int maxRange() {
		return AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
	}

	@Override
	protected void applySettings(ServerPlayer player, ChainConveyorBlockEntity be) {
		if (!be.getBlockPos()
			.closerThan(targetPos, maxRange() - 16 + 1))
			return;
		if (!(be.getLevel()
			.getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity clbe))
			return;

		if (connect && !player.isCreative()) {
			int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(be.getBlockPos()));
			boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, true);
			if (!hasEnough)
				return;
			ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, false);
		}

		if (!connect) {
			if (!player.isCreative()) {
				int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(pos));
				while (chainCost > 0) {
					player.getInventory()
						.placeItemBackInInventory(new ItemStack(Items.CHAIN, Math.min(chainCost, 64)));
					chainCost -= 64;
				}
			}
			be.chainDestroyed(targetPos.subtract(be.getBlockPos()), false, true);
			be.getLevel()
				.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS);
		}

		if (connect) {
			if (!clbe.addConnectionTo(be.getBlockPos()))
				return;
		} else
			clbe.removeConnectionTo(be.getBlockPos());

		if (connect) {
			if (!be.addConnectionTo(targetPos))
				clbe.removeConnectionTo(be.getBlockPos());
		} else
			be.removeConnectionTo(targetPos);
	}

	@Override
	protected void applySettings(ChainConveyorBlockEntity be) {}

}
