package com.simibubi.create.foundation.utility.legacy.kinetics;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.LegacyKineticNetwork;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

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
    public float speedModifier(LegacyKineticNetwork.Position position, ForgeDirection direction) {
        LegacyKineticNetwork.Position neighbour = position.offset(direction);
        Block block = world.getBlock(position.x(), position.y(), position.z());
        Block neighbourBlock = world.getBlock(neighbour.x(), neighbour.y(), neighbour.z());
        if (!(block instanceof IRotate rotate) || !(neighbourBlock instanceof IRotate neighbourRotate)) {
            return 0;
        }
        if (rotate.hasShaftTowards(world, position.x(), position.y(), position.z(), direction)
            && neighbourRotate.hasShaftTowards(world, neighbour.x(), neighbour.y(), neighbour.z(),
                direction.getOpposite())) {
            return 1;
        }
        if (!ICogWheel.isSmallCog(block) || !ICogWheel.isSmallCog(neighbourBlock)) {
            return 0;
        }
        LegacyAxis axis = rotate.getRotationAxis(world, position.x(), position.y(), position.z());
        LegacyAxis neighbourAxis = neighbourRotate.getRotationAxis(world, neighbour.x(), neighbour.y(), neighbour.z());
        return axis == neighbourAxis && LegacyAxis.fromPlacementSide(direction.ordinal()) != axis ? -1 : 0;
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
