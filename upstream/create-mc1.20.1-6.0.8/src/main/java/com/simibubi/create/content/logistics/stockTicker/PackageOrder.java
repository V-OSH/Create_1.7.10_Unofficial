package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public record PackageOrder(List<BigItemStack> stacks) {

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.put("Entries", NBTHelper.writeCompoundList(stacks, BigItemStack::write));
		return tag;
	}

	public static PackageOrder empty() {
		return new PackageOrder(List.of());
	}

	public boolean isEmpty() {
		return stacks.isEmpty();
	}

	public static PackageOrder read(CompoundTag tag) {
		List<BigItemStack> stacks = new ArrayList<>();
		NBTHelper.iterateCompoundList(tag.getList("Entries", Tag.TAG_COMPOUND),
			entryTag -> stacks.add(BigItemStack.read(entryTag)));
		return new PackageOrder(stacks);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(stacks.size());
		for (BigItemStack entry : stacks)
			entry.send(buffer);
	}

	public static PackageOrder read(FriendlyByteBuf buffer) {
		int size = buffer.readVarInt();
		List<BigItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < size; i++)
			stacks.add(BigItemStack.receive(buffer));
		return new PackageOrder(stacks);
	}

}
