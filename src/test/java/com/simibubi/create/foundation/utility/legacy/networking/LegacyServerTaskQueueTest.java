package com.simibubi.create.foundation.utility.legacy.networking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class LegacyServerTaskQueueTest {

    @Test
    void networkWorkWaitsForTheServerTick() {
        AtomicInteger value = new AtomicInteger();

        LegacyServerTaskQueue.enqueue(value::incrementAndGet);
        assertEquals(0, value.get());
        LegacyServerTaskQueue.runPending();
        assertEquals(1, value.get());
    }
}
