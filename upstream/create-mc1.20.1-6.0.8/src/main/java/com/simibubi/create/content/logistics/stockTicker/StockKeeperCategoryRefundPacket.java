package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StockKeeperCategoryRefundPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {

	private ItemStack filter;

	public StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) {
		super(pos);
		this.filter = filter;
	}

	public StockKeeperCategoryRefundPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		filter = buffer.readItem();
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeItem(filter);
	}

	@Override
	protected void applySettings(StockTickerBlockEntity be) {}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
			player.getInventory()
				.placeItemBackInInventory(filter);
	}

}
