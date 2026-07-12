package create.shim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyMathHelperTest {

    private static final double EPSILON = 1.0E-9;

    @Test
    void testWrapDegrees() {
        assertEquals(0.0, MyMathHelper.wrapDegrees(0.0), EPSILON);
        assertEquals(90.0, MyMathHelper.wrapDegrees(90.0), EPSILON);
        assertEquals(-90.0, MyMathHelper.wrapDegrees(-90.0), EPSILON);
        assertEquals(0.0, MyMathHelper.wrapDegrees(360.0), EPSILON);
        assertEquals(-180.0, MyMathHelper.wrapDegrees(180.0), EPSILON);
    }

    @Test
    void testWrapDegreesEdgeCases() {
        assertEquals(0.0, MyMathHelper.wrapDegrees(720.0), EPSILON);
        assertEquals(0.0, MyMathHelper.wrapDegrees(-360.0), EPSILON);
        assertEquals(179.0, MyMathHelper.wrapDegrees(539.0), EPSILON); // 539 - 360 = 179
    }

    @Test
    void testWrapRadians() {
        assertEquals(0.0, MyMathHelper.wrapRadians(0.0), EPSILON);
        assertEquals(Math.PI / 2, MyMathHelper.wrapRadians(Math.PI / 2), EPSILON);
        assertEquals(0.0, MyMathHelper.wrapRadians(2.0 * Math.PI), EPSILON);
        assertEquals(-Math.PI, MyMathHelper.wrapRadians(Math.PI), EPSILON);
    }

    @Test
    void testLerp() {
        assertEquals(0.0, MyMathHelper.lerp(0.0, 10.0, 0.0), EPSILON);
        assertEquals(10.0, MyMathHelper.lerp(0.0, 10.0, 1.0), EPSILON);
        assertEquals(5.0, MyMathHelper.lerp(0.0, 10.0, 0.5), EPSILON);
        assertEquals(2.5, MyMathHelper.lerp(0.0, 10.0, 0.25), EPSILON);
    }

    @Test
    void testLerpExtrapolation() {
        assertEquals(-10.0, MyMathHelper.lerp(0.0, 10.0, -1.0), EPSILON);
        assertEquals(20.0, MyMathHelper.lerp(0.0, 10.0, 2.0), EPSILON);
    }

    @Test
    void testClampDouble() {
        assertEquals(0.5, MyMathHelper.clamp(0.5, 0.0, 1.0), EPSILON);
        assertEquals(0.0, MyMathHelper.clamp(-1.0, 0.0, 1.0), EPSILON);
        assertEquals(1.0, MyMathHelper.clamp(2.0, 0.0, 1.0), EPSILON);
        assertEquals(0.5, MyMathHelper.clamp(0.5, 0.0, 1.0), EPSILON);
    }

    @Test
    void testClampInt() {
        assertEquals(5, MyMathHelper.clamp(5, 0, 10));
        assertEquals(0, MyMathHelper.clamp(-5, 0, 10));
        assertEquals(10, MyMathHelper.clamp(15, 0, 10));
    }

    @Test
    void testClampFloat() {
        assertEquals(0.5f, MyMathHelper.clamp(0.5f, 0.0f, 1.0f), EPSILON);
        assertEquals(0.0f, MyMathHelper.clamp(-0.5f, 0.0f, 1.0f), EPSILON);
        assertEquals(1.0f, MyMathHelper.clamp(1.5f, 0.0f, 1.0f), EPSILON);
    }

    @Test
    void testFloor() {
        assertEquals(3, MyMathHelper.floor(3.7));
        assertEquals(3, MyMathHelper.floor(3.0));
        assertEquals(-4, MyMathHelper.floor(-3.1));
        assertEquals(-3, MyMathHelper.floor(-3.0));
        assertEquals(0, MyMathHelper.floor(0.9));
    }

    @Test
    void testCeil() {
        assertEquals(4, MyMathHelper.ceil(3.1));
        assertEquals(3, MyMathHelper.ceil(3.0));
        assertEquals(-3, MyMathHelper.ceil(-3.1));
        assertEquals(-3, MyMathHelper.ceil(-3.0));
        assertEquals(1, MyMathHelper.ceil(0.1));
    }

    @Test
    void testToRadians() {
        assertEquals(Math.PI, MyMathHelper.toRadians(180.0), EPSILON);
        assertEquals(Math.PI / 2, MyMathHelper.toRadians(90.0), EPSILON);
        assertEquals(0.0, MyMathHelper.toRadians(0.0), EPSILON);
    }

    @Test
    void testToDegrees() {
        assertEquals(180.0, MyMathHelper.toDegrees(Math.PI), EPSILON);
        assertEquals(90.0, MyMathHelper.toDegrees(Math.PI / 2), EPSILON);
        assertEquals(0.0, MyMathHelper.toDegrees(0.0), EPSILON);
    }

    @Test
    void testSign() {
        assertEquals(1, MyMathHelper.sign(5.0));
        assertEquals(1, MyMathHelper.sign(0.001));
        assertEquals(-1, MyMathHelper.sign(-5.0));
        assertEquals(-1, MyMathHelper.sign(-0.001));
        assertEquals(0, MyMathHelper.sign(0.0));
    }

    @Test
    void testSmoothstep() {
        assertEquals(0.0, MyMathHelper.smoothstep(0.0, 1.0, 0.0), EPSILON);
        assertEquals(1.0, MyMathHelper.smoothstep(0.0, 1.0, 1.0), EPSILON);
        assertEquals(0.5, MyMathHelper.smoothstep(0.0, 1.0, 0.5), EPSILON);
        // smoothstep is monotonic
        double mid = MyMathHelper.smoothstep(0.0, 1.0, 0.5);
        assertTrue(mid > 0.0 && mid < 1.0);
    }

    @Test
    void testSmoothstepOutsideRange() {
        assertEquals(0.0, MyMathHelper.smoothstep(0.0, 1.0, -0.5), EPSILON);
        assertEquals(1.0, MyMathHelper.smoothstep(0.0, 1.0, 1.5), EPSILON);
    }
}
