/*
 * Adapted from Create 6.0.8's ShaftBlock for Minecraft Forge 1.7.10.
 * Upstream source: com/simibubi/create/content/kinetics/simpleRelays/ShaftBlock.java
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.simpleRelays;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ShaftBlock extends AbstractSimpleShaftBlock {

    @SideOnly(Side.CLIENT)
    private IIcon axisIcon;
    @SideOnly(Side.CLIENT)
    private IIcon axisTopIcon;

    public ShaftBlock() {
        setBlockName(Create.ID + ".shaft");
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ,
        int metadata) {
        return LegacyAxis.fromPlacementSide(side).getMetadata();
    }

    public static LegacyAxis resolvePlacementAxis(LegacyAxis clickedAxis, LegacyAxis lookAxis,
        LegacyAxis preferredAxis,
        boolean sneaking) {
        if (preferredAxis != null) {
            return sneaking ? clickedAxis : preferredAxis;
        }
        return lookAxis;
    }

    static LegacyAxis mergePreferredAxis(LegacyAxis current, LegacyAxis candidate) {
        if (current == null || current == candidate) {
            return candidate;
        }
        return null;
    }

    public static LegacyAxis getPreferredAxis(IBlockAccess world, int x, int y, int z) {
        LegacyAxis preferredAxis = null;
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            int neighbourX = x + direction.offsetX;
            int neighbourY = y + direction.offsetY;
            int neighbourZ = z + direction.offsetZ;
            if (!(world.getBlock(neighbourX, neighbourY, neighbourZ) instanceof IRotate rotate)) {
                continue;
            }
            if (!rotate.hasShaftTowards(world, neighbourX, neighbourY, neighbourZ, direction.getOpposite())) {
                continue;
            }

            LegacyAxis candidate = LegacyAxis.fromPlacementSide(direction.ordinal());
            LegacyAxis merged = mergePreferredAxis(preferredAxis, candidate);
            if (preferredAxis != null && merged == null) {
                return null;
            }
            preferredAxis = merged;
        }
        return preferredAxis;
    }

    public static LegacyAxis getLookAxis(EntityLivingBase placer, LegacyAxis fallback) {
        if (placer == null) {
            return fallback;
        }
        Vec3 look = placer.getLookVec();
        double x = Math.abs(look.xCoord);
        double y = Math.abs(look.yCoord);
        double z = Math.abs(look.zCoord);
        if (x >= y && x >= z) {
            return LegacyAxis.X;
        }
        if (z >= y) {
            return LegacyAxis.Z;
        }
        return LegacyAxis.Y;
    }

    @Override
    public int getRenderType() {
        // Vanilla's log renderer rotates side UVs for metadata axis bits 0, 4, and 8.
        return 31;
    }

    @Override
    public int damageDropped(int metadata) {
        return 0;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int metadata) {
        super.breakBlock(world, x, y, z, block, metadata);
        LegacyKineticWorldAdapter.rebuildAt(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        axisIcon = register.registerIcon(Create.ID + ":axis");
        axisTopIcon = register.registerIcon(Create.ID + ":axis_top");
        blockIcon = axisIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return LegacyAxis.fromPlacementSide(side) == getRotationAxis(metadata) ? axisTopIcon : axisIcon;
    }
}
