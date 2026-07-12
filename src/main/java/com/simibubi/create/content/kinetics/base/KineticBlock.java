/*
 * Adapted from Create 6.0.8's KineticBlock for Minecraft Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

public abstract class KineticBlock extends BlockContainer implements IRotate {

    protected KineticBlock(Material material) {
        super(material);
    }
}
