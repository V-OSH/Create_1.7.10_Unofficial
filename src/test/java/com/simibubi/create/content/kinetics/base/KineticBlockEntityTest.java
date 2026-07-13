package com.simibubi.create.content.kinetics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllBlockEntityTypes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

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

    @Test
    void largeCogwheelRenderBoundsIncludeUpstreamToothOverhang() {
        AxisAlignedBB bounds = KineticBlockEntity.createRenderBoundingBox(4, 5, 6, true);

        assertEquals(3, bounds.minX);
        assertEquals(4, bounds.minY);
        assertEquals(5, bounds.minZ);
        assertEquals(6, bounds.maxX);
        assertEquals(7, bounds.maxY);
        assertEquals(8, bounds.maxZ);
    }
}
