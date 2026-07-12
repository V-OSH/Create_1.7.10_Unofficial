package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllPackets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public class PackagePortItem extends BlockItem {

	public PackagePortItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, Player player, ItemStack p_195943_4_,
		BlockState p_195943_5_) {
		if (!world.isClientSide && player instanceof ServerPlayer sp)
			AllPackets.getChannel()
				.send(PacketDistributor.PLAYER.with(() -> sp), new PackagePortPlacementPacket.ClientBoundRequest(pos));
		return super.updateCustomBlockEntityTag(pos, world, player, p_195943_4_, p_195943_5_);
	}

}
