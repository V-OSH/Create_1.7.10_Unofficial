package com.simibubi.create.foundation.utility.legacy.kinetics;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.LegacyKineticNetwork;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class LegacyKineticWorldAdapter implements LegacyKineticNetwork.NetworkView {

    private final World world;

    private LegacyKineticWorldAdapter(World world) {
        this.world = world;
    }

    public static void rebuildAt(World world, int x, int y, int z) {
        if (world == null || world.isRemote) {
            return;
        }
        LegacyKineticNetwork.rebuildAt(new LegacyKineticWorldAdapter(world),
            new LegacyKineticNetwork.Position(x, y, z));
    }

    @Override
    public boolean isKinetic(LegacyKineticNetwork.Position position) {
        Block block = world.getBlock(position.x(), position.y(), position.z());
        TileEntity tileEntity = world.getTileEntity(position.x(), position.y(), position.z());
        return block instanceof IRotate && tileEntity instanceof KineticBlockEntity;
    }

    @Override
    public boolean connects(LegacyKineticNetwork.Position position, ForgeDirection direction) {
        Block block = world.getBlock(position.x(), position.y(), position.z());
        return block instanceof IRotate rotate
            && rotate.hasShaftTowards(world, position.x(), position.y(), position.z(), direction);
    }

    @Override
    public Float sourceSpeed(LegacyKineticNetwork.Position position) {
        TileEntity tileEntity = world.getTileEntity(position.x(), position.y(), position.z());
        Block block = world.getBlock(position.x(), position.y(), position.z());
        if (!(tileEntity instanceof CreativeMotorBlockEntity motor) || !(block instanceof CreativeMotorBlock)) {
            return null;
        }
        ForgeDirection facing = ((CreativeMotorBlock) block)
            .getFacing(world.getBlockMetadata(position.x(), position.y(), position.z()));
        int directionSign = facing.offsetX + facing.offsetY + facing.offsetZ;
        return (float) motor.getGeneratedSpeed() * (directionSign < 0 ? -1 : 1);
    }

    @Override
    public void setSpeed(LegacyKineticNetwork.Position position, float speed) {
        TileEntity tileEntity = world.getTileEntity(position.x(), position.y(), position.z());
        if (tileEntity instanceof KineticBlockEntity kinetic) {
            kinetic.setSpeed(speed);
        }
    }
}
