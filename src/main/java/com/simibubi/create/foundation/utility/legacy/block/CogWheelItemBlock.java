/*
 * Legacy placement bridge adapted from Create 6.0.8's CogwheelBlockItem.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.foundation.utility.legacy.block;

import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CogWheelItemBlock extends ItemBlock {

    public CogWheelItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        ForgeDirection clickedFace = ForgeDirection.getOrientation(side);
        LegacyAxis clickedAxis = LegacyAxis.fromPlacementSide(side);
        Block placedAgainst = world.getBlock(x - clickedFace.offsetX, y - clickedFace.offsetY,
            z - clickedFace.offsetZ);
        LegacyAxis placedAgainstAxis = placedAgainst instanceof CogWheelBlock cogwheel
            ? cogwheel.getRotationAxis(world.getBlockMetadata(x - clickedFace.offsetX, y - clickedFace.offsetY,
                z - clickedFace.offsetZ))
            : null;
        LegacyAxis preferredAxis = ShaftBlock.getPreferredAxis(world, x, y, z);
        LegacyAxis axis = CogWheelBlock.resolvePlacementAxis(clickedAxis, placedAgainstAxis, preferredAxis,
            player != null && player.isSneaking());
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ,
            axis.getMetadata());
        if (placed) {
            LegacyKineticWorldAdapter.rebuildAt(world, x, y, z);
        }
        return placed;
    }
}
