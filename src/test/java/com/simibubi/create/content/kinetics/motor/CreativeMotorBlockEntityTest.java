package com.simibubi.create.content.kinetics.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllBlockEntityTypes;

import net.minecraft.nbt.NBTTagCompound;

class CreativeMotorBlockEntityTest {

    @Test
    void generatedSpeedDefaultsToSixteenAndPersists() {
        AllBlockEntityTypes.register();
        CreativeMotorBlockEntity original = new CreativeMotorBlockEntity();
        assertEquals(16, original.getGeneratedSpeed());

        original.setGeneratedSpeed(-64);
        NBTTagCompound tag = new NBTTagCompound();
        original.writeToNBT(tag);

        CreativeMotorBlockEntity restored = new CreativeMotorBlockEntity();
        restored.readFromNBT(tag);
        assertEquals(-64, restored.getGeneratedSpeed());
    }
}
