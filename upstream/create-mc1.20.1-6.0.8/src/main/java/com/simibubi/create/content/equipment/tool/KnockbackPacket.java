package com.simibubi.create.content.equipment.tool;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class KnockbackPacket extends SimplePacketBase {

	private float yRot;
	private float strength;

	public KnockbackPacket(float yRot, float strength) {
		this.yRot = yRot;
		this.strength = strength;
	}

	public KnockbackPacket(FriendlyByteBuf buffer) {
		strength = buffer.readFloat();
		yRot = buffer.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeFloat(strength);
		buffer.writeFloat(yRot);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean handle(Context context) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return true;
		CardboardSwordItem.knockback(player, strength, yRot);
		return true;
	}

}
