package com.simibubi.create.content.logistics.redstoneRequester;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class RedstoneRequesterEffectPacket extends SimplePacketBase {

	private BlockPos pos;
	private boolean success;

	public RedstoneRequesterEffectPacket(BlockPos pos, boolean success) {
		this.pos = pos;
		this.success = success;
	}

	public RedstoneRequesterEffectPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		success = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeBoolean(success);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof RedstoneRequesterBlockEntity plbe)
				plbe.playEffect(success);
		});
		return true;
	}

}
