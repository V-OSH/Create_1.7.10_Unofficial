package com.simibubi.create.foundation.networking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class MotorSpeedPacketTest {

    @Test
    void roundTripPreservesTargetAndAppliesValueSettings() {
        MotorSpeedPacket original = new MotorSpeedPacket(3, 4, 5, 0, 64);
        ByteBuf buffer = Unpooled.buffer();
        original.toBytes(buffer);

        MotorSpeedPacket restored = new MotorSpeedPacket();
        restored.fromBytes(buffer);
        CreativeMotorBlockEntity motor = new CreativeMotorBlockEntity();
        restored.applyTo(motor);

        assertEquals(3, restored.getX());
        assertEquals(4, restored.getY());
        assertEquals(5, restored.getZ());
        assertEquals(-64, motor.getGeneratedSpeed());
    }
}
