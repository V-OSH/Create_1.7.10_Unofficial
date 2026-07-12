package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class LogisticalStockRequestPacket extends BlockEntityConfigurationPacket<StockCheckingBlockEntity> {

	public LogisticalStockRequestPacket(BlockPos pos) {
		super(pos);
	}

	public LogisticalStockRequestPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void applySettings(StockCheckingBlockEntity be) {}

	@Override
	protected void applySettings(ServerPlayer player, StockCheckingBlockEntity be) {
		be.getRecentSummary()
			.divideAndSendTo(player, pos);
	}
	
	@Override
	protected int maxRange() {
		return 4096;
	}

}
