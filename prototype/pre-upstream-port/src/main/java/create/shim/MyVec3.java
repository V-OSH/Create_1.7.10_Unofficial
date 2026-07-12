package create.shim;

/**
 * Mutable 3D floating-point vector, mirroring net.minecraft.world.phys.Vec3 from 1.20.1.
 */
public class MyVec3 {

    public double x;
    public double y;
    public double z;

    public MyVec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Returns a new zero vector. */
    public static MyVec3 zero() {
        return new MyVec3(0.0, 0.0, 0.0);
    }

    public MyVec3 add(MyVec3 other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public MyVec3 add(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
        return this;
    }

    public MyVec3 subtract(MyVec3 other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        return this;
    }

    public MyVec3 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        return this;
    }

    public double dot(MyVec3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public MyVec3 cross(MyVec3 other) {
        double nx = this.y * other.z - this.z * other.y;
        double ny = this.z * other.x - this.x * other.z;
        double nz = this.x * other.y - this.y * other.x;
        this.x = nx;
        this.y = ny;
        this.z = nz;
        return this;
    }

    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    /** Normalizes this vector in-place. Returns this vector even if it was zero-length. */
    public MyVec3 normalize() {
        double len = length();
        if (len > 1.0E-9) {
            double inv = 1.0 / len;
            this.x *= inv;
            this.y *= inv;
            this.z *= inv;
        }
        return this;
    }

    public double distanceTo(MyVec3 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyVec3)) return false;
        MyVec3 other = (MyVec3) o;
        return Double.compare(other.x, x) == 0
            && Double.compare(other.y, y) == 0
            && Double.compare(other.z, z) == 0;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(x);
        bits = bits * 31 + Double.doubleToLongBits(y);
        bits = bits * 31 + Double.doubleToLongBits(z);
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public String toString() {
        return "Vec3(" + x + ", " + y + ", " + z + ")";
    }
}
