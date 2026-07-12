/*
 * Adapted from Create 6.0.8's AbstractShaftBlock for Minecraft Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.simpleRelays;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class AbstractShaftBlock extends KineticBlock {

    protected AbstractShaftBlock() {
        super(Material.rock);
        setHardness(1.5f);
        setResistance(6.0f);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new KineticBlockEntity();
    }

    @Override
    public LegacyAxis getRotationAxis(IBlockAccess world, int x, int y, int z) {
        return LegacyAxis.fromMetadata(world.getBlockMetadata(x, y, z));
    }

    public LegacyAxis getRotationAxis(int metadata) {
        return LegacyAxis.fromMetadata(metadata);
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return hasShaftTowards(world.getBlockMetadata(x, y, z), face);
    }

    public boolean hasShaftTowards(int metadata, ForgeDirection face) {
        if (face == null || face == ForgeDirection.UNKNOWN) {
            return false;
        }
        LegacyAxis faceAxis = LegacyAxis.fromPlacementSide(face.ordinal());
        return faceAxis == getRotationAxis(metadata);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setShaftBounds(getRotationAxis(world, x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setShaftBounds(LegacyAxis.Y);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return createInteractionBounds(getRotationAxis(world, x, y, z), x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        return createInteractionBounds(getRotationAxis(world, x, y, z), x, y, z);
    }

    static AxisAlignedBB createInteractionBounds(LegacyAxis axis, int x, int y, int z) {
        double min = 5.0 / 16.0;
        double max = 11.0 / 16.0;
        return switch (axis) {
            case X -> AxisAlignedBB.getBoundingBox(x, y + min, z + min, x + 1, y + max, z + max);
            case Y -> AxisAlignedBB.getBoundingBox(x + min, y, z + min, x + max, y + 1, z + max);
            case Z -> AxisAlignedBB.getBoundingBox(x + min, y + min, z, x + max, y + max, z + 1);
        };
    }

    private void setShaftBounds(LegacyAxis axis) {
        float min = 6.0f / 16.0f;
        float max = 10.0f / 16.0f;
        switch (axis) {
            case X -> setBlockBounds(0, min, min, 1, max, max);
            case Y -> setBlockBounds(min, 0, min, max, 1, max);
            case Z -> setBlockBounds(min, min, 0, max, max, 1);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }
}
