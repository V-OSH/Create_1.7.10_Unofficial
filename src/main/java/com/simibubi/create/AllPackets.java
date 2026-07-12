/*
 * Network registration shape adapted from Create 6.0.8's AllPackets.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import com.simibubi.create.foundation.utility.legacy.networking.LegacyServerTaskQueue;
import com.simibubi.create.foundation.utility.legacy.networking.MotorSpeedPacket;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class AllPackets {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Create.ID);

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        FMLCommonHandler.instance().bus().register(LegacyServerTaskQueue.INSTANCE);
        CHANNEL.registerMessage(MotorSpeedPacket.Handler.class, MotorSpeedPacket.class, 0, Side.SERVER);
    }

    private AllPackets() {}
}
