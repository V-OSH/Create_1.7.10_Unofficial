package com.simibubi.create.content.logistics.tableCloth;

import java.util.List;

import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class TableClothBlockItem extends BlockItem {

	public TableClothBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		CompoundTag tag = pStack.getTag();
		return tag != null && tag.contains("TargetOffset");
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
		if (!isFoil(pStack))
			return;

		CreateLang.translate("table_cloth.shop_configured")
			.style(ChatFormatting.GOLD)
			.addTo(pTooltip);

		RedstoneRequesterBlock.appendRequesterTooltip(pStack, pTooltip);
	}

}
