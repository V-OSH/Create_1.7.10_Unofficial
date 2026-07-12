package create.shim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyBlockPosTest {

    @Test
    void testCreateAndGetters() {
        MyBlockPos pos = new MyBlockPos(1, 2, 3);
        assertEquals(1, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(3, pos.getZ());
    }

    @Test
    void testOffsetByDirection() {
        MyBlockPos pos = new MyBlockPos(0, 0, 0);
        assertEquals(new MyBlockPos(0, 1, 0), pos.offset(MyDirection.UP));
        assertEquals(new MyBlockPos(0, -1, 0), pos.offset(MyDirection.DOWN));
        assertEquals(new MyBlockPos(0, 0, -1), pos.offset(MyDirection.NORTH));
        assertEquals(new MyBlockPos(0, 0, 1), pos.offset(MyDirection.SOUTH));
        assertEquals(new MyBlockPos(-1, 0, 0), pos.offset(MyDirection.WEST));
        assertEquals(new MyBlockPos(1, 0, 0), pos.offset(MyDirection.EAST));
    }

    @Test
    void testOffsetWithDistance() {
        MyBlockPos pos = new MyBlockPos(0, 0, 0);
        assertEquals(new MyBlockPos(0, 5, 0), pos.offset(MyDirection.UP, 5));
        assertEquals(new MyBlockPos(3, 0, 0), pos.offset(MyDirection.EAST, 3));
        assertEquals(new MyBlockPos(0, 0, -2), pos.offset(MyDirection.NORTH, 2));
    }

    @Test
    void testAdd() {
        MyBlockPos pos = new MyBlockPos(1, 2, 3);
        assertEquals(new MyBlockPos(4, 6, 8), pos.add(3, 4, 5));
        assertEquals(new MyBlockPos(-1, 2, -1), pos.add(-2, 0, -4));
        assertEquals(new MyBlockPos(5, 7, 9), pos.add(new MyBlockPos(4, 5, 6)));
    }

    @Test
    void testSubtract() {
        MyBlockPos a = new MyBlockPos(5, 7, 9);
        MyBlockPos b = new MyBlockPos(2, 3, 4);
        assertEquals(new MyBlockPos(3, 4, 5), a.subtract(b));
    }

    @Test
    void testDistance() {
        MyBlockPos a = new MyBlockPos(0, 0, 0);
        MyBlockPos b = new MyBlockPos(3, 4, 0);
        assertEquals(25.0, a.distanceSq(b), 0.0001);
        assertEquals(5.0, a.distance(b), 0.0001);
    }

    @Test
    void testEqualsAndHashCode() {
        MyBlockPos a = new MyBlockPos(1, 2, 3);
        MyBlockPos b = new MyBlockPos(1, 2, 3);
        MyBlockPos c = new MyBlockPos(1, 2, 4);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testImmutability() {
        MyBlockPos pos = new MyBlockPos(1, 2, 3);
        // offset returns new instance, original unchanged
        MyBlockPos offset = pos.offset(MyDirection.UP);
        assertNotSame(pos, offset);
        assertEquals(new MyBlockPos(1, 2, 3), pos);
    }

    @Test
    void testToString() {
        MyBlockPos pos = new MyBlockPos(1, 2, 3);
        assertTrue(pos.toString().contains("1"));
        assertTrue(pos.toString().contains("2"));
        assertTrue(pos.toString().contains("3"));
    }
}
