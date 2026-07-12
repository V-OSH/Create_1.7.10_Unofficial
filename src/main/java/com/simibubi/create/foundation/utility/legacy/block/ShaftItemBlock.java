/*
 * Legacy ItemBlock bridge for Create 6.0.8's RotatedPillarKineticBlock placement policy.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.foundation.utility.legacy.block;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ShaftItemBlock extends ItemBlock {

    public ShaftItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        LegacyAxis clickedAxis = LegacyAxis.fromMetadata(metadata);
        LegacyAxis lookAxis = ShaftBlock.getLookAxis(player, clickedAxis);
        LegacyAxis preferredAxis = ShaftBlock.getPreferredAxis(world, x, y, z);
        LegacyAxis axis = ShaftBlock.resolvePlacementAxis(clickedAxis, lookAxis, preferredAxis,
            player != null && player.isSneaking());
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ,
            axis.getMetadata());
        if (placed) {
            LegacyKineticWorldAdapter.rebuildAt(world, x, y, z);
        }
        return placed;
    }
}
