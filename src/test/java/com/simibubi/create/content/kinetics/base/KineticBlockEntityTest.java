package com.simibubi.create.content.kinetics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllBlockEntityTypes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

class KineticBlockEntityTest {

    @Test
    void speedSurvivesAnNbtRoundTrip() {
        AllBlockEntityTypes.register();
        KineticBlockEntity original = new KineticBlockEntity();
        original.setSpeed(32);

        NBTTagCompound tag = new NBTTagCompound();
        original.writeToNBT(tag);

        TileEntity restoredTileEntity = TileEntity.createAndLoadEntity(tag);
        KineticBlockEntity restored = assertInstanceOf(KineticBlockEntity.class, restoredTileEntity);

        assertEquals(32.0f, restored.getSpeed());
    }
}
