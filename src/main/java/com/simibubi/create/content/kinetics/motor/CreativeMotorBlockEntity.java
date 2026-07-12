/*
 * Adapted from Create 6.0.8's CreativeMotorBlockEntity for Minecraft Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.motor;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.legacy.kinetics.LegacyKineticWorldAdapter;

import net.minecraft.nbt.NBTTagCompound;

public class CreativeMotorBlockEntity extends KineticBlockEntity {

    public static final int DEFAULT_SPEED = 16;
    public static final int MAX_SPEED = 256;

    private int generatedSpeed = DEFAULT_SPEED;

    public int getGeneratedSpeed() {
        return generatedSpeed;
    }

    public void setGeneratedSpeed(int speed) {
        int clampedSpeed = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, speed));
        if (generatedSpeed == clampedSpeed) {
            return;
        }
        generatedSpeed = clampedSpeed;
        markDirty();
        LegacyKineticWorldAdapter.rebuildAt(worldObj, xCoord, yCoord, zCoord);
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        generatedSpeed = tag.hasKey("GeneratedSpeed") ? tag.getInteger("GeneratedSpeed") : DEFAULT_SPEED;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("GeneratedSpeed", generatedSpeed);
    }
}
