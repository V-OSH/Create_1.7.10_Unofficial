/*
 * Minimal connectivity and source arbitration adapted from Create 6.0.8's TorquePropagator.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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
                if (resolvedSpeed == null || Math.abs(sourceSpeed) > Math.abs(resolvedSpeed)) {
                    resolvedSpeed = sourceSpeed;
                    conflictingSources = false;
                } else if (Math.abs(sourceSpeed) == Math.abs(resolvedSpeed)
                    && Float.compare(resolvedSpeed, sourceSpeed) != 0) {
                    conflictingSources = true;
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

    private LegacyKineticNetwork() {}
}
