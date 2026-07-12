/*
 * Adapted from Create 6.0.8's IRotate contract for Minecraft Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.foundation.utility.legacy.LegacyAxis;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public interface IRotate {

    LegacyAxis getRotationAxis(IBlockAccess world, int x, int y, int z);

    boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, ForgeDirection face);
}
