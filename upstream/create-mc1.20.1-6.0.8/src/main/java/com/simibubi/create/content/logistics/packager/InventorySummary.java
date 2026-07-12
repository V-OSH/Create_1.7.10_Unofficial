package com.simibubi.create.content.logistics.packager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.Lists;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

public class InventorySummary {

	public static final InventorySummary EMPTY = new InventorySummary();

	private Map<Item, List<BigItemStack>> items = new IdentityHashMap<>();
	private List<BigItemStack> stacksByCount;
	private int totalCount;

	public int contributingLinks;

	public void add(InventorySummary summary) {
		summary.items.forEach((i, list) -> list.forEach(this::add));
		contributingLinks += summary.contributingLinks;
	}

	public void add(ItemStack stack) {
		add(stack, stack.getCount());
	}

	public void add(BigItemStack entry) {
		add(entry.stack, entry.count);
	}

	public Map<Item, List<BigItemStack>> getItemMap() {
		return items;
	}

	public InventorySummary copy() {
		InventorySummary inventorySummary = new InventorySummary();
		items.forEach((i, list) -> list.forEach(entry -> inventorySummary.add(entry.stack, entry.count)));
		return inventorySummary;
	}

	public void add(ItemStack stack, int count) {
		if (count == 0 || stack.isEmpty())
			return;

		if (totalCount < BigItemStack.INF)
			totalCount += count;

		List<BigItemStack> stacks = items.computeIfAbsent(stack.getItem(), $ -> Lists.newArrayList());
		for (BigItemStack existing : stacks) {
			ItemStack existingStack = existing.stack;
			if (ItemHandlerHelper.canItemStacksStack(existingStack, stack)) {
				if (existing.count < BigItemStack.INF)
					existing.count += count;
				return;
			}
		}

		if (stack.getCount() > stack.getMaxStackSize())
			stack = stack.copyWithCount(1);

		BigItemStack newEntry = new BigItemStack(stack, count);
		stacks.add(newEntry);
	}

	public boolean erase(ItemStack stack) {
		List<BigItemStack> stacks = items.get(stack.getItem());
		if (stacks == null)
			return false;
		for (Iterator<BigItemStack> iterator = stacks.iterator(); iterator.hasNext();) {
			BigItemStack existing = iterator.next();
			ItemStack existingStack = existing.stack;
			if (!ItemHandlerHelper.canItemStacksStack(existingStack, stack))
				continue;
			totalCount -= existing.count;
			iterator.remove();
			return true;
		}
		return false;
	}

	public int getCountOf(ItemStack stack) {
		List<BigItemStack> list = items.get(stack.getItem());
		if (list == null)
			return 0;
		for (BigItemStack entry : list)
			if (ItemHandlerHelper.canItemStacksStack(entry.stack, stack))
				return entry.count;
		return 0;
	}

	public int getTotalOfMatching(Predicate<ItemStack> filter) {
		MutableInt sum = new MutableInt();
		items.forEach(($, list) -> {
			for (BigItemStack entry : list)
				if (filter.test(entry.stack))
					sum.add(entry.count);
		});
		return sum.getValue();
	}

	public List<BigItemStack> getStacks() {
		if (stacksByCount == null) {
			List<BigItemStack> stacks = new ArrayList<>();
			items.forEach((i, list) -> list.forEach(stacks::add));
			return stacks;
		}
		return stacksByCount;
	}

	public List<BigItemStack> getStacksByCount() {
		if (stacksByCount == null) {
			stacksByCount = new ArrayList<>();
			items.forEach((i, list) -> list.forEach(stacksByCount::add));
			Collections.sort(stacksByCount, BigItemStack.comparator());
		}
		return stacksByCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void divideAndSendTo(ServerPlayer player, BlockPos pos) {
		List<BigItemStack> stacks = getStacksByCount();
		int remaining = stacks.size();

		List<BigItemStack> currentList = null;
		PacketTarget target = PacketDistributor.PLAYER.with(() -> player);

		if (stacks.isEmpty())
			AllPackets.getChannel()
				.send(target, new LogisticalStockResponsePacket(true, pos, Collections.emptyList()));

		for (BigItemStack entry : stacks) {
			if (currentList == null)
				currentList = new ArrayList<>(Math.min(100, remaining));

			currentList.add(entry);
			remaining--;

			if (remaining == 0)
				break;
			if (currentList.size() < 100)
				continue;

			AllPackets.getChannel()
				.send(target, new LogisticalStockResponsePacket(false, pos, currentList));
			currentList = null;
		}

		if (currentList != null)
			AllPackets.getChannel()
				.send(target, new LogisticalStockResponsePacket(true, pos, currentList));
	}

	public CompoundTag write() {
		List<BigItemStack> all = new ArrayList<>();
		items.forEach((key, list) -> all.addAll(list));
		CompoundTag tag = new CompoundTag();
		tag.put("List", NBTHelper.writeCompoundList(all, BigItemStack::write));
		return tag;
	}

	public static InventorySummary read(CompoundTag tag) {
		InventorySummary summary = new InventorySummary();
		NBTHelper.iterateCompoundList(tag.getList("List", Tag.TAG_COMPOUND), c -> summary.add(BigItemStack.read(c)));
		return summary;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

}
