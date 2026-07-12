/*
 * Adapted from Create 6.0.8's CogWheelBlock for Minecraft Forge 1.7.10.
 * Upstream source: com/simibubi/create/content/kinetics/simpleRelays/CogWheelBlock.java
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.simpleRelays;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import net.minecraft.world.World;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;

public class CogWheelBlock extends AbstractSimpleShaftBlock {

    private static int renderType = -1;
    @SideOnly(Side.CLIENT)
    private IIcon axisIcon;
    @SideOnly(Side.CLIENT)
    private IIcon axisTopIcon;
    @SideOnly(Side.CLIENT)
    private IIcon cogwheelIcon;

    public CogWheelBlock() {
        setBlockName(Create.ID + ".cogwheel");
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ,
        int metadata) {
        return LegacyAxis.fromPlacementSide(side).getMetadata();
    }

    public static LegacyAxis resolvePlacementAxis(LegacyAxis clickedAxis, LegacyAxis placedAgainstCogAxis,
        LegacyAxis preferredAxis, boolean sneaking) {
        if (sneaking) {
            return clickedAxis;
        }
        if (placedAgainstCogAxis != null) {
            return placedAgainstCogAxis;
        }
        return preferredAxis != null ? preferredAxis : clickedAxis;
    }

    public static void setRenderType(int id) {
        renderType = id;
    }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        setSelectionBounds(getRotationAxis(world, x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setSelectionBounds(LegacyAxis.Y);
    }

    private void setSelectionBounds(LegacyAxis axis) {
        float gearMin = 2.0f / 16;
        float gearMax = 14.0f / 16;
        switch (axis) {
            case X -> setBlockBounds(0, gearMin, gearMin, 1, gearMax, gearMax);
            case Y -> setBlockBounds(gearMin, 0, gearMin, gearMax, 1, gearMax);
            case Z -> setBlockBounds(gearMin, gearMin, 0, gearMax, gearMax, 1);
        }
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        return createSelectionBounds(getRotationAxis(world, x, y, z), x, y, z);
    }

    static AxisAlignedBB createSelectionBounds(LegacyAxis axis, int x, int y, int z) {
        double min = 2.0 / 16;
        double max = 14.0 / 16;
        return switch (axis) {
            case X -> AxisAlignedBB.getBoundingBox(x, y + min, z + min, x + 1, y + max, z + max);
            case Y -> AxisAlignedBB.getBoundingBox(x + min, y, z + min, x + max, y + 1, z + max);
            case Z -> AxisAlignedBB.getBoundingBox(x + min, y + min, z, x + max, y + max, z + 1);
        };
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> boxes, Entity entity) {
        LegacyAxis axis = getRotationAxis(world, x, y, z);
        double gearMin = 2.0 / 16;
        double gearMax = 14.0 / 16;
        double thicknessMin = 6.0 / 16;
        double thicknessMax = 10.0 / 16;
        double shaftMin = 5.0 / 16;
        double shaftMax = 11.0 / 16;
        switch (axis) {
            case X -> {
                addCollisionBox(x, y, z, mask, boxes, 0, shaftMin, shaftMin, 1, shaftMax, shaftMax);
                addCollisionBox(x, y, z, mask, boxes, thicknessMin, gearMin, gearMin, thicknessMax, gearMax,
                    gearMax);
            }
            case Y -> {
                addCollisionBox(x, y, z, mask, boxes, shaftMin, 0, shaftMin, shaftMax, 1, shaftMax);
                addCollisionBox(x, y, z, mask, boxes, gearMin, thicknessMin, gearMin, gearMax, thicknessMax,
                    gearMax);
            }
            case Z -> {
                addCollisionBox(x, y, z, mask, boxes, shaftMin, shaftMin, 0, shaftMax, shaftMax, 1);
                addCollisionBox(x, y, z, mask, boxes, gearMin, gearMin, thicknessMin, gearMax, gearMax,
                    thicknessMax);
            }
        }
    }

    private static void addCollisionBox(int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> boxes,
        double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY,
            z + maxZ);
        if (mask.intersectsWith(box)) {
            boxes.add(box);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        axisIcon = register.registerIcon(Create.ID + ":cogwheel_axis");
        axisTopIcon = register.registerIcon(Create.ID + ":axis_top");
        cogwheelIcon = register.registerIcon(Create.ID + ":cogwheel");
        blockIcon = cogwheelIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getAxisIcon() {
        return axisIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getAxisTopIcon() {
        return axisTopIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getCogwheelIcon() {
        return cogwheelIcon;
    }

    @Override
    public int getRenderType() {
        return renderType;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int metadata) {
        super.breakBlock(world, x, y, z, block, metadata);
        LegacyKineticWorldAdapter.rebuildAt(world, x, y, z);
    }
}
