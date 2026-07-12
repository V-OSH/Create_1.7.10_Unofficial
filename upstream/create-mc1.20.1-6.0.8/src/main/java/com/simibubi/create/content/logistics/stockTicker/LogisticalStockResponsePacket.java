package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class LogisticalStockResponsePacket extends SimplePacketBase {

	private BlockPos pos;
	private List<BigItemStack> items;
	private boolean lastPacket;

	public LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos, List<BigItemStack> items) {
		this.lastPacket = lastPacket;
		this.pos = pos;
		this.items = items;
	}

	public LogisticalStockResponsePacket(FriendlyByteBuf buffer) {
		lastPacket = buffer.readBoolean();
		pos = buffer.readBlockPos();
		int count = buffer.readVarInt();
		items = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			items.add(BigItemStack.receive(buffer));
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(lastPacket);
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(items.size());
		items.forEach(stack -> stack.send(buffer));
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(this::handleClient);
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void handleClient() {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe)
			stbe.receiveStockPacket(items, lastPacket);
	}

}
