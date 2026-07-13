/*
 * Cogwheel propagation rules adapted from Create 6.0.8's RotationPropagator.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

/** Upstream kinetic gameplay rules expressed through the 1.7.10 axis bridge. */
public final class RotationPropagator {

    public record Offset(int x, int y, int z) {}

    public static List<Offset> additionalCogwheelNeighbours(boolean large, LegacyAxis axis) {
        List<Offset> offsets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x * x + y * y + z * z != 2 || !large && component(axis, x, y, z) != 0) {
                        continue;
                    }
                    offsets.add(new Offset(x, y, z));
                }
            }
        }
        return List.copyOf(offsets);
    }

    public static float getCogwheelSpeedModifier(boolean fromLarge, LegacyAxis fromAxis, boolean toLarge,
        LegacyAxis toAxis, int xOffset, int yOffset, int zOffset) {
        if (fromLarge && toLarge) {
            if (fromAxis == toAxis || Math.abs(component(fromAxis, xOffset, yOffset, zOffset)) != 1
                || Math.abs(component(toAxis, xOffset, yOffset, zOffset)) != 1) {
                return 0;
            }
            LegacyAxis thirdAxis = thirdAxis(fromAxis, toAxis);
            if (component(thirdAxis, xOffset, yOffset, zOffset) != 0) {
                return 0;
            }
            return component(fromAxis, xOffset, yOffset, zOffset) > 0
                ^ component(toAxis, xOffset, yOffset, zOffset) > 0 ? -1 : 1;
        }
        if (fromLarge != toLarge) {
            if (fromAxis != toAxis || component(fromAxis, xOffset, yOffset, zOffset) != 0) {
                return 0;
            }
            for (LegacyAxis axis : LegacyAxis.values()) {
                if (axis != fromAxis && Math.abs(component(axis, xOffset, yOffset, zOffset)) != 1) {
                    return 0;
                }
            }
            return fromLarge ? -2 : -.5f;
        }
        int manhattan = Math.abs(xOffset) + Math.abs(yOffset) + Math.abs(zOffset);
        return manhattan == 1 && fromAxis == toAxis && component(fromAxis, xOffset, yOffset, zOffset) == 0 ? -1 : 0;
    }

    private static int component(LegacyAxis axis, int x, int y, int z) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    private static LegacyAxis thirdAxis(LegacyAxis first, LegacyAxis second) {
        for (LegacyAxis axis : LegacyAxis.values()) {
            if (axis != first && axis != second) {
                return axis;
            }
        }
        throw new IllegalArgumentException("Cogwheel axes must differ");
    }

    private RotationPropagator() {}
}
