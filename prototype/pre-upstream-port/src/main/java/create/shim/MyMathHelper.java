package create.shim;

/**
 * Math utility methods, mirroring net.minecraft.util.Mth from 1.20.1.
 */
public final class MyMathHelper {

    private MyMathHelper() {}

    /** Wraps an angle in degrees to [-180, 180). */
    public static double wrapDegrees(double degrees) {
        double d = degrees % 360.0;
        if (d >= 180.0) {
            d -= 360.0;
        }
        if (d < -180.0) {
            d += 360.0;
        }
        return d;
    }

    /** Wraps an angle in radians to [-PI, PI). */
    public static double wrapRadians(double radians) {
        double r = radians % (2.0 * Math.PI);
        if (r >= Math.PI) {
            r -= 2.0 * Math.PI;
        }
        if (r < -Math.PI) {
            r += 2.0 * Math.PI;
        }
        return r;
    }

    /** Linear interpolation between a and b by t in [0, 1]. */
    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /** Clamp value between min and max (double). */
    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /** Clamp value between min and max (int). */
    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /** Clamp value between min and max (float). */
    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static int ceil(double value) {
        int i = (int) value;
        return value > (double) i ? i + 1 : i;
    }

    /** Convert degrees to radians. */
    public static double toRadians(double degrees) {
        return degrees * (Math.PI / 180.0);
    }

    /** Convert radians to degrees. */
    public static double toDegrees(double radians) {
        return radians * (180.0 / Math.PI);
    }

    /** Returns the sign of value: -1, 0, or 1. */
    public static int sign(double value) {
        if (value > 0) return 1;
        if (value < 0) return -1;
        return 0;
    }

    /** Smoothstep: maps t in [edge0, edge1] onto a smooth [0, 1] curve. */
    public static double smoothstep(double edge0, double edge1, double t) {
        double x = clamp((t - edge0) / (edge1 - edge0), 0.0, 1.0);
        return x * x * (3.0 - 2.0 * x);
    }
}
