package com.simibubi.create.foundation.utility.legacy;

/**
 * Minecraft 1.7.10 metadata representation of modern {@code Direction.Axis}.
 */
public enum LegacyAxis {
    Y(0),
    X(1),
    Z(2);

    private final int metadata;

    LegacyAxis(int metadata) {
        this.metadata = metadata;
    }

    public int getMetadata() {
        return metadata;
    }

    public static LegacyAxis fromMetadata(int metadata) {
        for (LegacyAxis axis : values()) {
            if (axis.metadata == metadata) {
                return axis;
            }
        }
        return Y;
    }

    public static LegacyAxis fromPlacementSide(int side) {
        return switch (side) {
            case 2, 3 -> Z;
            case 4, 5 -> X;
            default -> Y;
        };
    }
}
