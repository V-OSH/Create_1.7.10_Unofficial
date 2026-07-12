package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class WiFiEffectPacket extends SimplePacketBase {

	private BlockPos pos;
	
	public static void send(Level level, BlockPos pos) {
		AllPackets.sendToNear(level, pos, 32, new WiFiEffectPacket(pos));
	}

	public WiFiEffectPacket(BlockPos pos) {
		this.pos = pos;
	}

	public WiFiEffectPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
			if (blockEntity instanceof PackagerLinkBlockEntity plbe)
				plbe.playEffect();
			if (blockEntity instanceof StockTickerBlockEntity plbe)
				plbe.playEffect();
		});
		return true;
	}

}
