/*
 * Minimal connectivity and source arbitration adapted from Create 6.0.8's TorquePropagator.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

public final class LegacyKineticNetwork {

    public record Position(int x, int y, int z) {

        public Position offset(ForgeDirection direction) {
            return new Position(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
        }
    }

    public interface NetworkView {

        boolean isKinetic(Position position);

        boolean connects(Position position, ForgeDirection direction);

        default float speedModifier(Position position, ForgeDirection direction) {
            return connects(position, direction) ? 1 : 0;
        }

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
        Map<Position, Float> factors = new HashMap<>();
        open.add(seed);
        visited.add(seed);
        factors.put(seed, 1f);
        Float resolvedRootSpeed = null;
        boolean conflictingSources = false;

        while (!open.isEmpty()) {
            Position current = open.remove();
            component.add(current);
            float currentFactor = factors.get(current);
            Float sourceSpeed = view.sourceSpeed(current);
            if (sourceSpeed != null) {
                float candidateRootSpeed = sourceSpeed / currentFactor;
                if (resolvedRootSpeed == null || Math.abs(candidateRootSpeed) > Math.abs(resolvedRootSpeed)) {
                    resolvedRootSpeed = candidateRootSpeed;
                    conflictingSources = false;
                } else if (Math.abs(candidateRootSpeed) == Math.abs(resolvedRootSpeed)
                    && Float.compare(resolvedRootSpeed, candidateRootSpeed) != 0) {
                    conflictingSources = true;
                }
            }

            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                Position neighbour = current.offset(direction);
                if (visited.contains(neighbour) || !view.isKinetic(neighbour)) {
                    continue;
                }
                float forwardModifier = view.speedModifier(current, direction);
                float backwardModifier = view.speedModifier(neighbour, direction.getOpposite());
                if (forwardModifier == 0 || backwardModifier == 0
                    || Math.abs(forwardModifier * backwardModifier - 1) > .0001f) {
                    continue;
                }
                visited.add(neighbour);
                factors.put(neighbour, currentFactor * forwardModifier);
                open.add(neighbour);
            }
        }

        float rootSpeed = resolvedRootSpeed == null || conflictingSources ? 0 : resolvedRootSpeed;
        for (Position position : component) {
            view.setSpeed(position, rootSpeed * factors.get(position));
        }
    }

    private LegacyKineticNetwork() {}
}
