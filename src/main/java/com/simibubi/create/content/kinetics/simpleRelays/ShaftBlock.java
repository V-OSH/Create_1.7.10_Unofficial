/*
 * Adapted from Create 6.0.8's ShaftBlock for Minecraft Forge 1.7.10.
 * Upstream source: com/simibubi/create/content/kinetics/simpleRelays/ShaftBlock.java
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.simpleRelays;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

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
