package com.simibubi.create.foundation.utility.legacy;

/**
 * Minecraft 1.7.10 metadata representation of modern {@code Direction.Axis}.
 */
public enum LegacyAxis {
    Y(0),
    X(4),
    Z(8);

    private final int metadata;

    LegacyAxis(int metadata) {
        this.metadata = metadata;
    }

    public int getMetadata() {
        return metadata;
    }

    public static LegacyAxis fromMetadata(int metadata) {
        // Development builds before the axis-aware UV bridge stored X and Z as 1 and 2.
        if (metadata == 1) {
            return X;
        }
        if (metadata == 2) {
            return Z;
        }
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
