/*
 * Registry shape adapted from Create 6.0.8's AllBlocks.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.foundation.utility.legacy.block.CreativeMotorItemBlock;
import com.simibubi.create.foundation.utility.legacy.block.ShaftItemBlock;

import cpw.mods.fml.common.registry.GameRegistry;

public final class AllBlocks {

    public static final ShaftBlock SHAFT = new ShaftBlock();
    public static final CreativeMotorBlock CREATIVE_MOTOR = new CreativeMotorBlock();

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        GameRegistry.registerBlock(SHAFT, ShaftItemBlock.class, "shaft");
        GameRegistry.registerBlock(CREATIVE_MOTOR, CreativeMotorItemBlock.class, "creative_motor");
        SHAFT.setCreativeTab(AllCreativeModeTabs.BASE);
        CREATIVE_MOTOR.setCreativeTab(AllCreativeModeTabs.BASE);
    }

    private AllBlocks() {}
}
