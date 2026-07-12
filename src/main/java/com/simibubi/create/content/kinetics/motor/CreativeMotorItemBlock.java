/*
 * Legacy ItemBlock bridge for Create 6.0.8's directional motor placement policy.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.motor;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.simibubi.create.content.kinetics.base.LegacyKineticNetwork;

public class CreativeMotorItemBlock extends ItemBlock {

    public CreativeMotorItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        ForgeDirection fallback = ForgeDirection.getOrientation(side);
        ForgeDirection look = CreativeMotorBlock.getLookFacing(player, fallback);
        ForgeDirection preferred = CreativeMotorBlock.getPreferredFacing(world, x, y, z);
        ForgeDirection facing = CreativeMotorBlock.resolvePlacementFacing(look, preferred,
            player != null && player.isSneaking());
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ,
            facing.ordinal());
        if (placed) {
            LegacyKineticNetwork.rebuildAt(world, x, y, z);
        }
        return placed;
    }
}
