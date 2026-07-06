package create.shim;

/**
 * Six cardinal directions and rotation, mirroring net.minecraft.core.Direction from 1.20.1.
 */
public enum MyDirection {

    DOWN(0, -1, 0, "down"),
    UP(0, 1, 0, "up"),
    NORTH(0, 0, -1, "north"),
    SOUTH(0, 0, 1, "south"),
    WEST(-1, 0, 0, "west"),
    EAST(1, 0, 0, "east");

    private final int xOffset;
    private final int yOffset;
    private final int zOffset;
    private final String name;

    MyDirection(int xOffset, int yOffset, int zOffset, String name) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.name = name;
    }

    public int getXOffset() { return xOffset; }
    public int getYOffset() { return yOffset; }
    public int getZOffset() { return zOffset; }
    public String getName() { return name; }

    /** Returns the opposite direction. */
    public MyDirection getOpposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }

    /** Rotate around the Y axis (clockwise). */
    public MyDirection rotateY() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> this;
        };
    }

    /** Rotate around the X axis (clockwise looking from positive X). */
    public MyDirection rotateX() {
        return switch (this) {
            case UP -> NORTH;
            case NORTH -> DOWN;
            case DOWN -> SOUTH;
            case SOUTH -> UP;
            default -> this;
        };
    }

    /** Rotate around the Z axis (clockwise looking from positive Z). */
    public MyDirection rotateZ() {
        return switch (this) {
            case UP -> EAST;
            case EAST -> DOWN;
            case DOWN -> WEST;
            case WEST -> UP;
            default -> this;
        };
    }

    public static MyDirection fromName(String name) {
        for (MyDirection dir : values()) {
            if (dir.name.equalsIgnoreCase(name)) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Unknown direction: " + name);
    }

    /** Returns the axis this direction lies on. */
    public Axis getAxis() {
        return switch (this) {
            case DOWN, UP -> Axis.Y;
            case NORTH, SOUTH -> Axis.Z;
            case WEST, EAST -> Axis.X;
        };
    }

    public enum Axis {
        X, Y, Z;

        public boolean isHorizontal() { return this != Y; }
        public boolean isVertical() { return this == Y; }
    }
}
