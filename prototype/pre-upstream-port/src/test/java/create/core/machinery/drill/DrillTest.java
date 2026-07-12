package create.core.machinery.drill;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DrillTest {

    @Test
    void getDamage_zeroSpeed_returnsOne() {
        assertEquals(1.0f, DrillBlock.getDamage(0), 0.001f);
    }

    @Test
    void getDamage_lowSpeed_returnsSmallDamage() {
        // At 16 RPM: min(1,2)=1 + min(0.5,4)=0.5 + min(0.25,4)=0.25 = 1.75
        float damage = DrillBlock.getDamage(16.0f);
        assertTrue(damage >= 1.0f && damage <= 2.0f,
                "expected ~1.75, got " + damage);
    }

    @Test
    void getDamage_mediumSpeed_returnsMediumDamage() {
        // At 64 RPM: min(4,2)=2 + min(2,4)=2 + min(1,4)=1 = 5
        float damage = DrillBlock.getDamage(64.0f);
        assertEquals(5.0f, damage, 0.01f);
    }

    @Test
    void getDamage_at256_sumsToMax() {
        // At 256 RPM: min(16,2)=2 + min(8,4)=4 + min(4,4)=4 = 10 (max)
        float damage = DrillBlock.getDamage(256.0f);
        assertEquals(10.0f, damage, 0.001f);
    }

    @Test
    void getDamage_superHighSpeed_cappedAtTen() {
        float damage = DrillBlock.getDamage(1024.0f);
        assertEquals(10.0f, damage, 0.001f);
    }

    @Test
    void getDamage_negativeSpeed_sameAsPositive() {
        float pos = DrillBlock.getDamage(64.0f);
        float neg = DrillBlock.getDamage(-64.0f);
        assertEquals(pos, neg, 0.001f);
    }
}
