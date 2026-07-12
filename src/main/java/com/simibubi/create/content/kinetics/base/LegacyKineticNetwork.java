/*
 * Legacy world adapter for the connectivity semantics of Create 6.0.8's TorquePropagator.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class LegacyKineticNetwork {

    public record Position(int x, int y, int z) {

        Position offset(ForgeDirection direction) {
            return new Position(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
        }
    }

    public interface NetworkView {

        boolean isKinetic(Position position);

        boolean connects(Position position, ForgeDirection direction);

        Float sourceSpeed(Position position);

        void setSpeed(Position position, float speed);
    }

    public static void rebuildAt(World world, int x, int y, int z) {
        if (world == null || world.isRemote) {
            return;
        }
        rebuildAt(new WorldNetworkView(world), new Position(x, y, z));
    }

    public static void rebuildAt(NetworkView view, Position changedPosition) {
        Set<Position> visited = new HashSet<>();
        rebuildComponent(view, changedPosition, visited);
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            rebuildComponent(view, changedPosition.offset(direction), visited);
        }
    }

    private static void rebuildComponent(NetworkView view, Position seed, Set<Position> visited) {
        if (!view.isKinetic(seed) || visited.contains(seed)) {
            return;
        }

        List<Position> component = new ArrayList<>();
        Queue<Position> open = new ArrayDeque<>();
        open.add(seed);
        visited.add(seed);
        Float resolvedSpeed = null;
        boolean conflictingSources = false;

        while (!open.isEmpty()) {
            Position current = open.remove();
            component.add(current);
            Float sourceSpeed = view.sourceSpeed(current);
            if (sourceSpeed != null) {
                if (resolvedSpeed != null && Float.compare(resolvedSpeed, sourceSpeed) != 0) {
                    conflictingSources = true;
                } else {
                    resolvedSpeed = sourceSpeed;
                }
            }

            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                Position neighbour = current.offset(direction);
                if (visited.contains(neighbour) || !view.isKinetic(neighbour)) {
                    continue;
                }
                if (!view.connects(current, direction) || !view.connects(neighbour, direction.getOpposite())) {
                    continue;
                }
                visited.add(neighbour);
                open.add(neighbour);
            }
        }

        float speed = resolvedSpeed == null || conflictingSources ? 0 : resolvedSpeed;
        for (Position position : component) {
            view.setSpeed(position, speed);
        }
    }

    private static final class WorldNetworkView implements NetworkView {

        private final World world;

        WorldNetworkView(World world) {
            this.world = world;
        }

        @Override
        public boolean isKinetic(Position position) {
            Block block = world.getBlock(position.x(), position.y(), position.z());
            TileEntity tileEntity = world.getTileEntity(position.x(), position.y(), position.z());
            return block instanceof IRotate && tileEntity instanceof KineticBlockEntity;
        }

        @Override
        public boolean connects(Position position, ForgeDirection direction) {
            Block block = world.getBlock(position.x(), position.y(), position.z());
            return block instanceof IRotate rotate
                && rotate.hasShaftTowards(world, position.x(), position.y(), position.z(), direction);
        }

        @Override
        public Float sourceSpeed(Position position) {
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
        public void setSpeed(Position position, float speed) {
            TileEntity tileEntity = world.getTileEntity(position.x(), position.y(), position.z());
            if (tileEntity instanceof KineticBlockEntity kinetic) {
                kinetic.setSpeed(speed);
            }
        }
    }

    private LegacyKineticNetwork() {}
}
