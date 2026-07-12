package create.shim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyVec3Test {

    private static final double EPSILON = 1.0E-9;

    @Test
    void testZero() {
        MyVec3 v = MyVec3.zero();
        assertEquals(0.0, v.x, EPSILON);
        assertEquals(0.0, v.y, EPSILON);
        assertEquals(0.0, v.z, EPSILON);
    }

    @Test
    void testAdd() {
        MyVec3 a = new MyVec3(1.0, 2.0, 3.0);
        MyVec3 b = new MyVec3(4.0, 5.0, 6.0);
        a.add(b);
        assertEquals(5.0, a.x, EPSILON);
        assertEquals(7.0, a.y, EPSILON);
        assertEquals(9.0, a.z, EPSILON);
    }

    @Test
    void testAddScalar() {
        MyVec3 v = new MyVec3(1.0, 2.0, 3.0);
        v.add(0.5, -1.0, 2.0);
        assertEquals(1.5, v.x, EPSILON);
        assertEquals(1.0, v.y, EPSILON);
        assertEquals(5.0, v.z, EPSILON);
    }

    @Test
    void testSubtract() {
        MyVec3 a = new MyVec3(5.0, 7.0, 9.0);
        MyVec3 b = new MyVec3(2.0, 3.0, 4.0);
        a.subtract(b);
        assertEquals(3.0, a.x, EPSILON);
        assertEquals(4.0, a.y, EPSILON);
        assertEquals(5.0, a.z, EPSILON);
    }

    @Test
    void testScale() {
        MyVec3 v = new MyVec3(1.0, 2.0, 3.0);
        v.scale(2.0);
        assertEquals(2.0, v.x, EPSILON);
        assertEquals(4.0, v.y, EPSILON);
        assertEquals(6.0, v.z, EPSILON);
    }

    @Test
    void testDotProduct() {
        MyVec3 a = new MyVec3(1.0, 2.0, 3.0);
        MyVec3 b = new MyVec3(4.0, 5.0, 6.0);
        assertEquals(32.0, a.dot(b), EPSILON); // 1*4 + 2*5 + 3*6 = 32

        // Orthogonal vectors
        MyVec3 c = new MyVec3(1.0, 0.0, 0.0);
        MyVec3 d = new MyVec3(0.0, 1.0, 0.0);
        assertEquals(0.0, c.dot(d), EPSILON);
    }

    @Test
    void testCrossProduct() {
        // X cross Y = Z
        MyVec3 a = new MyVec3(1.0, 0.0, 0.0);
        MyVec3 b = new MyVec3(0.0, 1.0, 0.0);
        a.cross(b);
        assertEquals(0.0, a.x, EPSILON);
        assertEquals(0.0, a.y, EPSILON);
        assertEquals(1.0, a.z, EPSILON);
    }

    @Test
    void testLength() {
        MyVec3 v = new MyVec3(3.0, 4.0, 0.0);
        assertEquals(25.0, v.lengthSq(), EPSILON);
        assertEquals(5.0, v.length(), EPSILON);
    }

    @Test
    void testZeroLength() {
        MyVec3 v = MyVec3.zero();
        assertEquals(0.0, v.length(), EPSILON);
        assertEquals(0.0, v.lengthSq(), EPSILON);
    }

    @Test
    void testNormalize() {
        MyVec3 v = new MyVec3(3.0, 0.0, 4.0);
        v.normalize();
        assertEquals(1.0, v.length(), EPSILON);
        assertEquals(0.6, v.x, EPSILON);
        assertEquals(0.0, v.y, EPSILON);
        assertEquals(0.8, v.z, EPSILON);
    }

    @Test
    void testNormalizeZeroVector() {
        MyVec3 v = MyVec3.zero();
        v.normalize(); // should not throw or produce NaN from division
        assertFalse(Double.isNaN(v.x));
        assertFalse(Double.isNaN(v.y));
        assertFalse(Double.isNaN(v.z));
    }

    @Test
    void testDistanceTo() {
        MyVec3 a = new MyVec3(0.0, 0.0, 0.0);
        MyVec3 b = new MyVec3(3.0, 4.0, 0.0);
        assertEquals(5.0, a.distanceTo(b), EPSILON);
        assertEquals(5.0, b.distanceTo(a), EPSILON);
    }

    @Test
    void testEqualsAndHashCode() {
        MyVec3 a = new MyVec3(1.0, 2.0, 3.0);
        MyVec3 b = new MyVec3(1.0, 2.0, 3.0);
        MyVec3 c = new MyVec3(1.0, 2.0, 3.0001);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testMutability() {
        MyVec3 original = new MyVec3(1.0, 2.0, 3.0);
        MyVec3 reference = original;
        original.add(1.0, 1.0, 1.0);
        // Mutable -- both reference and original point to same object
        assertEquals(reference.x, original.x, EPSILON);
        assertEquals(2.0, original.x, EPSILON);
    }
}
