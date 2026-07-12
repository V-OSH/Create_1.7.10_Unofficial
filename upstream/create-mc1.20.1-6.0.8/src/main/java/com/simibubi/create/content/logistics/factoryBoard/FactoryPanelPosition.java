package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public record FactoryPanelPosition(BlockPos pos, PanelSlot slot) {

	public static FactoryPanelPosition read(CompoundTag nbt) {
		return new FactoryPanelPosition(NbtUtils.readBlockPos(nbt),
			PanelSlot.values()[Mth.positiveModulo(nbt.getInt("Slot"), 4)]);
	}

	public CompoundTag write() {
		CompoundTag nbt = NbtUtils.writeBlockPos(pos);
		nbt.putInt("Slot", slot.ordinal());
		return nbt;
	}

	public void send(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(slot.ordinal());
	}

	public static FactoryPanelPosition receive(FriendlyByteBuf buffer) {
		return new FactoryPanelPosition(buffer.readBlockPos(),
			PanelSlot.values()[Mth.positiveModulo(buffer.readVarInt(), 4)]);
	}

}
