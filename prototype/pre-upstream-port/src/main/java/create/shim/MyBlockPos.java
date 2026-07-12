package create.shim;

/**
 * Immutable 3D integer coordinate, mirroring net.minecraft.util.BlockPos from 1.20.1.
 */
public final class MyBlockPos {

    private final int x;
    private final int y;
    private final int z;

    public MyBlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    public MyBlockPos offset(MyDirection direction) {
        return new MyBlockPos(x + direction.getXOffset(), y + direction.getYOffset(), z + direction.getZOffset());
    }

    public MyBlockPos offset(MyDirection direction, int distance) {
        return new MyBlockPos(
            x + direction.getXOffset() * distance,
            y + direction.getYOffset() * distance,
            z + direction.getZOffset() * distance
        );
    }

    public MyBlockPos add(int dx, int dy, int dz) {
        return new MyBlockPos(x + dx, y + dy, z + dz);
    }

    public MyBlockPos add(MyBlockPos other) {
        return new MyBlockPos(x + other.x, y + other.y, z + other.z);
    }

    public MyBlockPos subtract(MyBlockPos other) {
        return new MyBlockPos(x - other.x, y - other.y, z - other.z);
    }

    public double distanceSq(MyBlockPos other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(MyBlockPos other) {
        return Math.sqrt(distanceSq(other));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyBlockPos)) return false;
        MyBlockPos that = (MyBlockPos) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return ((x * 31 + y) * 31 + z);
    }

    /** Pack into a 64-bit long: X(26 bits) | Y(12 bits) | Z(26 bits). */
    public long asLong() {
        return ((long) (getX() & 0x3FFFFFF) << 38)
             | ((long) (getY() & 0xFFF) << 26)
             | (long) (getZ() & 0x3FFFFFF);
    }

    /** Unpack a 64-bit long back into a MyBlockPos. */
    public static MyBlockPos fromLong(long packed) {
        int x = (int) (packed >> 38);
        int y = (int) ((packed >> 26) & 0xFFF);
        int z = (int) (packed << 38 >> 38);
        return new MyBlockPos(x, y, z);
    }

    @Override
    public String toString() {
        return "BlockPos{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
