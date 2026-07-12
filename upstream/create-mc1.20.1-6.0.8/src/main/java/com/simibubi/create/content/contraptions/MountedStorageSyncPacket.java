package com.simibubi.create.content.contraptions;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import net.minecraftforge.network.NetworkEvent.Context;

public class MountedStorageSyncPacket extends SimplePacketBase {
	public final int contraptionId;
	public final Map<BlockPos, MountedItemStorage> items;
	public final Map<BlockPos, MountedFluidStorage> fluids;

	public MountedStorageSyncPacket(int contraptionId, Map<BlockPos, MountedItemStorage> items, Map<BlockPos, MountedFluidStorage> fluids) {
		this.contraptionId = contraptionId;
		this.items = items;
		this.fluids = fluids;
	}

	public MountedStorageSyncPacket(FriendlyByteBuf buf) {
		this.contraptionId = buf.readVarInt();
		this.items = read(buf, MountedItemStorage.CODEC);
		this.fluids = read(buf, MountedFluidStorage.CODEC);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.contraptionId);
		write(buffer, this.items, MountedItemStorage.CODEC);
		write(buffer, this.fluids, MountedFluidStorage.CODEC);
	}

	@SuppressWarnings("deprecation")
	private static <T> void write(FriendlyByteBuf buf, Map<BlockPos, T> map, Codec<T> codec) {
		buf.writeMap(map, FriendlyByteBuf::writeBlockPos, (b, t) -> b.writeWithCodec(NbtOps.INSTANCE, codec, t));
	}

	@SuppressWarnings("deprecation")
	private static <T> Map<BlockPos, T> read(FriendlyByteBuf buf, Codec<T> codec) {
		return buf.readMap(FriendlyByteBuf::readBlockPos, (b) -> b.readWithCodec(NbtOps.INSTANCE, codec));
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Entity entity = Minecraft.getInstance().level.getEntity(this.contraptionId);
			if (!(entity instanceof AbstractContraptionEntity contraption))
				return;

			contraption.getContraption().getStorage().handleSync(this, contraption);
		});
		return true;
	}
}
