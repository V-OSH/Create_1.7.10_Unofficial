/*
 * Registry shape adapted from Create 6.0.8's AllBlocks.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import cpw.mods.fml.common.registry.GameRegistry;

public final class AllBlocks {

    public static final ShaftBlock SHAFT = new ShaftBlock();

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        GameRegistry.registerBlock(SHAFT, "shaft");
        SHAFT.setCreativeTab(AllCreativeModeTabs.BASE);
    }

    private AllBlocks() {}
}
