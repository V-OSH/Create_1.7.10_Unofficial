/*
 * Adapted from Create 6.0.8's CreativeMotorBlock for Minecraft Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.motor;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CreativeMotorBlock extends KineticBlock {

    @SideOnly(Side.CLIENT)
    private IIcon casingIcon;
    @SideOnly(Side.CLIENT)
    private IIcon motorIcon;

    public CreativeMotorBlock() {
        super(Material.rock);
        setBlockName(Create.ID + ".creative_motor");
        setHardness(1.5f);
        setResistance(6.0f);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new CreativeMotorBlockEntity();
    }

    @Override
    public LegacyAxis getRotationAxis(IBlockAccess world, int x, int y, int z) {
        return getRotationAxis(world.getBlockMetadata(x, y, z));
    }

    public LegacyAxis getRotationAxis(int metadata) {
        return LegacyAxis.fromPlacementSide(getFacing(metadata).ordinal());
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return hasShaftTowards(world.getBlockMetadata(x, y, z), face);
    }

    public boolean hasShaftTowards(int metadata, ForgeDirection face) {
        return face != null && face != ForgeDirection.UNKNOWN && face == getFacing(metadata);
    }

    public ForgeDirection getFacing(int metadata) {
        ForgeDirection facing = ForgeDirection.getOrientation(metadata);
        return facing == ForgeDirection.UNKNOWN ? ForgeDirection.SOUTH : facing;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        casingIcon = register.registerIcon(Create.ID + ":creative_casing");
        motorIcon = register.registerIcon(Create.ID + ":creative_motor");
        blockIcon = casingIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return side == getFacing(metadata).ordinal() ? motorIcon : casingIcon;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setMotorBounds(getFacing(world.getBlockMetadata(x, y, z)));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setMotorBounds(ForgeDirection.SOUTH);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setMotorBounds(getFacing(world.getBlockMetadata(x, y, z)));
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        setMotorBounds(getFacing(world.getBlockMetadata(x, y, z)));
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    void setMotorBounds(ForgeDirection facing) {
        float min = 3.0f / 16.0f;
        float max = 13.0f / 16.0f;
        float inset = 2.0f / 16.0f;
        switch (facing) {
            case DOWN -> setBlockBounds(min, inset, min, max, 1, max);
            case UP -> setBlockBounds(min, 0, min, max, 1 - inset, max);
            case NORTH -> setBlockBounds(min, min, inset, max, max, 1);
            case SOUTH -> setBlockBounds(min, min, 0, max, max, 1 - inset);
            case WEST -> setBlockBounds(inset, min, min, 1, max, max);
            case EAST -> setBlockBounds(0, min, min, 1 - inset, max, max);
            default -> setBlockBounds(min, min, 0, max, max, 1 - inset);
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

    public static ForgeDirection resolvePlacementFacing(ForgeDirection look, ForgeDirection preferred,
        boolean sneaking) {
        if (preferred != null && !sneaking) {
            return preferred;
        }
        return sneaking ? look : look.getOpposite();
    }

    public static ForgeDirection getPreferredFacing(IBlockAccess world, int x, int y, int z) {
        ForgeDirection preferred = null;
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
            if (preferred != null && preferred.ordinal() >> 1 != direction.ordinal() >> 1) {
                return null;
            }
            preferred = direction;
        }
        return preferred;
    }

    public static ForgeDirection getLookFacing(EntityLivingBase placer, ForgeDirection fallback) {
        if (placer == null) {
            return fallback;
        }
        Vec3 look = placer.getLookVec();
        double x = Math.abs(look.xCoord);
        double y = Math.abs(look.yCoord);
        double z = Math.abs(look.zCoord);
        if (x >= y && x >= z) {
            return look.xCoord >= 0 ? ForgeDirection.EAST : ForgeDirection.WEST;
        }
        if (z >= y) {
            return look.zCoord >= 0 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
        }
        return look.yCoord >= 0 ? ForgeDirection.UP : ForgeDirection.DOWN;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int metadata) {
        super.breakBlock(world, x, y, z, block, metadata);
        LegacyKineticWorldAdapter.rebuildAt(world, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote && world.getTileEntity(x, y, z) instanceof CreativeMotorBlockEntity motor) {
            Create.PROXY.openMotorSpeedScreen(x, y, z, motor.getGeneratedSpeed());
        }
        return true;
    }
}
