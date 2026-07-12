package com.simibubi.create.content.kinetics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllBlockEntityTypes;

import net.minecraft.nbt.NBTTagCompound;

class KineticBlockEntityTest {

    @Test
    void speedSurvivesAnNbtRoundTrip() {
        AllBlockEntityTypes.register();
        KineticBlockEntity original = new KineticBlockEntity();
        original.setSpeed(32);

        NBTTagCompound tag = new NBTTagCompound();
        original.writeToNBT(tag);

        KineticBlockEntity restored = new KineticBlockEntity();
        restored.readFromNBT(tag);

        assertEquals(32.0f, restored.getSpeed());
    }
}
