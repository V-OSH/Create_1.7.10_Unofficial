package com.simibubi.create.foundation.utility.legacy.kinetics;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.kinetics.RotationPropagator;
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

    public static void rebuildAfterCogwheelRemoval(World world, int x, int y, int z, boolean large,
        LegacyAxis axis) {
        if (world == null || world.isRemote) {
            return;
        }
        LegacyKineticWorldAdapter view = new LegacyKineticWorldAdapter(world);
        LegacyKineticNetwork.Position removed = new LegacyKineticNetwork.Position(x, y, z);
        for (LegacyKineticNetwork.Position neighbour : cogwheelNeighbours(removed, large, axis)) {
            LegacyKineticNetwork.rebuildAt(view, neighbour);
        }
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
        return speedModifier(position, position.offset(direction));
    }

    @Override
    public Iterable<LegacyKineticNetwork.Position> neighbours(LegacyKineticNetwork.Position position) {
        Block block = world.getBlock(position.x(), position.y(), position.z());
        if (!(block instanceof ICogWheel cogwheel) || !(block instanceof IRotate rotate)) {
            return LegacyKineticNetwork.NetworkView.super.neighbours(position);
        }
        LegacyAxis axis = rotate.getRotationAxis(world, position.x(), position.y(), position.z());
        return cogwheelNeighbours(position, cogwheel.isLargeCog(), axis);
    }

    @Override
    public float speedModifier(LegacyKineticNetwork.Position position, LegacyKineticNetwork.Position neighbour) {
        Block block = world.getBlock(position.x(), position.y(), position.z());
        Block neighbourBlock = world.getBlock(neighbour.x(), neighbour.y(), neighbour.z());
        if (!(block instanceof IRotate rotate) || !(neighbourBlock instanceof IRotate neighbourRotate)) {
            return 0;
        }
        ForgeDirection cardinal = cardinalDirection(position, neighbour);
        if (cardinal != null
            && rotate.hasShaftTowards(world, position.x(), position.y(), position.z(), cardinal)
            && neighbourRotate.hasShaftTowards(world, neighbour.x(), neighbour.y(), neighbour.z(),
                cardinal.getOpposite())) {
            return 1;
        }
        if (!(block instanceof ICogWheel cogwheel) || !(neighbourBlock instanceof ICogWheel neighbourCogwheel)) {
            return 0;
        }
        LegacyAxis axis = rotate.getRotationAxis(world, position.x(), position.y(), position.z());
        LegacyAxis neighbourAxis = neighbourRotate.getRotationAxis(world, neighbour.x(), neighbour.y(), neighbour.z());
        return RotationPropagator.getCogwheelSpeedModifier(cogwheel.isLargeCog(), axis,
            neighbourCogwheel.isLargeCog(), neighbourAxis, neighbour.x() - position.x(),
            neighbour.y() - position.y(), neighbour.z() - position.z());
    }

    static List<LegacyKineticNetwork.Position> cogwheelNeighbours(LegacyKineticNetwork.Position position,
        boolean large, LegacyAxis axis) {
        List<LegacyKineticNetwork.Position> neighbours = new ArrayList<>();
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            neighbours.add(position.offset(direction));
        }
        for (RotationPropagator.Offset offset : RotationPropagator.additionalCogwheelNeighbours(large, axis)) {
            neighbours.add(position.offset(offset.x(), offset.y(), offset.z()));
        }
        return neighbours;
    }

    private static ForgeDirection cardinalDirection(LegacyKineticNetwork.Position from,
        LegacyKineticNetwork.Position to) {
        int x = to.x() - from.x();
        int y = to.y() - from.y();
        int z = to.z() - from.z();
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            if (direction.offsetX == x && direction.offsetY == y && direction.offsetZ == z) {
                return direction;
            }
        }
        return null;
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
