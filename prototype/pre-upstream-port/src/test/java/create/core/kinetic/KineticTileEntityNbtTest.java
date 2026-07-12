package create.core.kinetic;

import create.shim.MyBlockPos;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KineticTileEntityNbtTest {

    @BeforeAll
    static void registerTileEntity() {
        GameRegistry.registerTileEntity(KineticTileEntity.class, "create:test_kinetic_nbt");
    }

    @Test
    void persistentNbtSkipsTransientNetworkState() {
        KineticTileEntity tile = new KineticTileEntity();
        tile.setConnected(ForgeDirection.UP, true);
        tile.setSpeed(64);
        tile.setNetworkId(42L);
        tile.setSourcePosition(new MyBlockPos(1, 2, 3));
        tile.angle = 1.25f;

        NBTTagCompound tag = new NBTTagCompound();
        tile.writeToNBT(tag);

        assertTrue(tag.getBoolean("conn_" + ForgeDirection.UP.ordinal()));
        assertEquals(1.25f, tag.getFloat("angle"), 0.001f);
        assertFalse(tag.hasKey("speed"));
        assertFalse(tag.hasKey("networkId"));
        assertFalse(tag.hasKey("sourceX"));
    }

    @Test
    void persistentNbtReadClearsStaleTransientNetworkState() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("conn_" + ForgeDirection.NORTH.ordinal(), true);
        tag.setFloat("angle", 0.5f);
        tag.setFloat("speed", 128);
        tag.setLong("networkId", 99L);
        tag.setInteger("sourceX", 4);
        tag.setInteger("sourceY", 5);
        tag.setInteger("sourceZ", 6);

        KineticTileEntity tile = new KineticTileEntity();
        tile.readFromNBT(tag);

        assertTrue(tile.isConnected(ForgeDirection.NORTH));
        assertEquals(0.5f, tile.angle, 0.001f);
        assertEquals(0, tile.getSpeed(), 0.001f);
        assertNull(tile.getNetworkId());
        assertTrue(tile.getSourcePosition().isEmpty());
    }

    @Test
    void transientSyncDataCarriesClientRenderSpeed() {
        KineticTileEntity tile = new KineticTileEntity();
        tile.setSpeed(96);

        NBTTagCompound tag = new NBTTagCompound();
        tile.writeTransientSyncData(tag);

        KineticTileEntity clientTile = new KineticTileEntity();
        clientTile.readTransientSyncData(tag);

        assertEquals(96, clientTile.getSpeed(), 0.001f);
    }
}
