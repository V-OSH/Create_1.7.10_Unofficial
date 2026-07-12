/*
 * Adapted from Create 6.0.8's ICogWheel for Minecraft Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.simpleRelays;

import com.simibubi.create.content.kinetics.base.IRotate;

import net.minecraft.block.Block;

public interface ICogWheel extends IRotate {

    static boolean isSmallCog(Block block) {
        return block instanceof ICogWheel cogwheel && cogwheel.isSmallCog();
    }

    default boolean isLargeCog() {
        return false;
    }

    default boolean isSmallCog() {
        return !isLargeCog();
    }

    default boolean isDedicatedCogWheel() {
        return false;
    }
}
