package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class RedstoneRequesterBlockItem extends LogisticallyLinkedBlockItem {

	public RedstoneRequesterBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		if (!isTuned(pStack))
			return;

		if (!pStack.getTag()
			.contains("EncodedRequest", Tag.TAG_COMPOUND)) {
			super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
			return;
		}

		CreateLang.translate("logistically_linked.tooltip")
			.style(ChatFormatting.GOLD)
			.addTo(pTooltip);
		RedstoneRequesterBlock.appendRequesterTooltip(pStack, pTooltip);
	}

}
