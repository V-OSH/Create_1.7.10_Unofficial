package create.shim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyDirectionTest {

    @Test
    void testOffsets() {
        assertEquals(0, MyDirection.DOWN.getXOffset());
        assertEquals(-1, MyDirection.DOWN.getYOffset());
        assertEquals(0, MyDirection.DOWN.getZOffset());

        assertEquals(1, MyDirection.EAST.getXOffset());
        assertEquals(0, MyDirection.EAST.getYOffset());
        assertEquals(0, MyDirection.EAST.getZOffset());

        assertEquals(0, MyDirection.SOUTH.getXOffset());
        assertEquals(0, MyDirection.SOUTH.getYOffset());
        assertEquals(1, MyDirection.SOUTH.getZOffset());
    }

    @Test
    void testGetOpposite() {
        assertEquals(MyDirection.DOWN, MyDirection.UP.getOpposite());
        assertEquals(MyDirection.UP, MyDirection.DOWN.getOpposite());
        assertEquals(MyDirection.NORTH, MyDirection.SOUTH.getOpposite());
        assertEquals(MyDirection.SOUTH, MyDirection.NORTH.getOpposite());
        assertEquals(MyDirection.WEST, MyDirection.EAST.getOpposite());
        assertEquals(MyDirection.EAST, MyDirection.WEST.getOpposite());
    }

    @Test
    void testOppositeRoundTrip() {
        for (MyDirection dir : MyDirection.values()) {
            assertEquals(dir, dir.getOpposite().getOpposite());
        }
    }

    @Test
    void testRotateY() {
        assertEquals(MyDirection.EAST, MyDirection.NORTH.rotateY());
        assertEquals(MyDirection.SOUTH, MyDirection.EAST.rotateY());
        assertEquals(MyDirection.WEST, MyDirection.SOUTH.rotateY());
        assertEquals(MyDirection.NORTH, MyDirection.WEST.rotateY());

        // Vertical directions unchanged by Y rotation
        assertEquals(MyDirection.UP, MyDirection.UP.rotateY());
        assertEquals(MyDirection.DOWN, MyDirection.DOWN.rotateY());
    }

    @Test
    void testRotateYFullCircle() {
        MyDirection dir = MyDirection.NORTH;
        for (int i = 0; i < 4; i++) {
            dir = dir.rotateY();
        }
        assertEquals(MyDirection.NORTH, dir);
    }

    @Test
    void testFromName() {
        assertEquals(MyDirection.UP, MyDirection.fromName("up"));
        assertEquals(MyDirection.DOWN, MyDirection.fromName("down"));
        assertEquals(MyDirection.NORTH, MyDirection.fromName("north"));
        assertEquals(MyDirection.EAST, MyDirection.fromName("east"));
    }

    @Test
    void testFromNameCaseInsensitive() {
        assertEquals(MyDirection.UP, MyDirection.fromName("UP"));
        assertEquals(MyDirection.NORTH, MyDirection.fromName("North"));
    }

    @Test
    void testFromNameInvalid() {
        assertThrows(IllegalArgumentException.class, () -> MyDirection.fromName("diagonal"));
    }

    @Test
    void testGetAxis() {
        assertEquals(MyDirection.Axis.Y, MyDirection.UP.getAxis());
        assertEquals(MyDirection.Axis.Y, MyDirection.DOWN.getAxis());
        assertEquals(MyDirection.Axis.X, MyDirection.EAST.getAxis());
        assertEquals(MyDirection.Axis.X, MyDirection.WEST.getAxis());
        assertEquals(MyDirection.Axis.Z, MyDirection.NORTH.getAxis());
        assertEquals(MyDirection.Axis.Z, MyDirection.SOUTH.getAxis());
    }

    @Test
    void testAxisProperties() {
        assertTrue(MyDirection.Axis.Y.isVertical());
        assertFalse(MyDirection.Axis.Y.isHorizontal());
        assertTrue(MyDirection.Axis.X.isHorizontal());
        assertFalse(MyDirection.Axis.X.isVertical());
    }
}
