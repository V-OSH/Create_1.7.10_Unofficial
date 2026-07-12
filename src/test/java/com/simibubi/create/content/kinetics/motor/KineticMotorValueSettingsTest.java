package com.simibubi.create.content.kinetics.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KineticMotorValueSettingsTest {

    @Test
    void selectionMapsDirectionRowsAndClampsMagnitude() {
        assertEquals(-64, KineticMotorValueSettings.toSignedSpeed(0, 64));
        assertEquals(64, KineticMotorValueSettings.toSignedSpeed(1, 64));
        assertEquals(-1, KineticMotorValueSettings.toSignedSpeed(0, 0));
        assertEquals(256, KineticMotorValueSettings.toSignedSpeed(1, 999));
    }

    @Test
    void directionRowsMatchUpstreamArrowSemanticsFromTheOutputFace() {
        assertEquals(-32,
            KineticMotorValueSettings.toSignedSpeed(KineticMotorValueSettings.CLOCKWISE_ROW, 32));
        assertEquals(32,
            KineticMotorValueSettings.toSignedSpeed(KineticMotorValueSettings.COUNTER_CLOCKWISE_ROW, 32));
    }
}
