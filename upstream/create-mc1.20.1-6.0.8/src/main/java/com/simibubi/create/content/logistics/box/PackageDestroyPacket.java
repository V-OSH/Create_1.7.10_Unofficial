package com.simibubi.create.content.logistics.box;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class PackageDestroyPacket extends SimplePacketBase {

	protected Vec3 location;
	private ItemStack box;

	public PackageDestroyPacket(FriendlyByteBuf buffer) {
		location = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		box = buffer.readItem();
	}

	public PackageDestroyPacket(Vec3 location, ItemStack box) {
		this.location = location;
		this.box = box.copy();
		this.box.setTag(null);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeDouble(location.x);
		buffer.writeDouble(location.y);
		buffer.writeDouble(location.z);
		buffer.writeItem(box);
	}

	@Override
	public boolean handle(Context ctx) {
		ctx.enqueueWork(() -> {
			for (int i = 0; i < 20; i++) {
				ClientLevel level = Minecraft.getInstance().level;
				Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .125f);
				Vec3 pos = location.add(motion.scale(4));
				level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, box), pos.x, pos.y,
					pos.z, motion.x, motion.y, motion.z);
			}
		});
		return true;
	}

}
