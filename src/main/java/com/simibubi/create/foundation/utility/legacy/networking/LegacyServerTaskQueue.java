package com.simibubi.create.foundation.utility.legacy.networking;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class LegacyServerTaskQueue {

    public static final LegacyServerTaskQueue INSTANCE = new LegacyServerTaskQueue();

    private static final Queue<Runnable> PENDING = new ConcurrentLinkedQueue<>();

    public static void enqueue(Runnable task) {
        PENDING.add(task);
    }

    public static void runPending() {
        Runnable task;
        while ((task = PENDING.poll()) != null) {
            task.run();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            runPending();
        }
    }

    private LegacyServerTaskQueue() {}
}
