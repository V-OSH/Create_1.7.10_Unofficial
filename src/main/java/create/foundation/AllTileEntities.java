package create.foundation;

import cpw.mods.fml.common.registry.GameRegistry;
import create.core.kinetic.KineticTileEntity;
import create.core.machinery.drill.DrillTileEntity;
import create.core.machinery.fan.EncasedFanTileEntity;
import create.core.machinery.millstone.MillstoneTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Central registry for all Create TileEntities.
 *
 * <p>Order is append-only to keep the registration ID stable across versions.</p>
 */
public final class AllTileEntities {

    // -- Kinetic --
    public static final Class<? extends TileEntity> KINETIC = KineticTileEntity.class;

    // -- Simple machinery (Phase 3a) --
    public static final Class<? extends TileEntity> MILLSTONE = MillstoneTileEntity.class;
    public static final Class<? extends TileEntity> DRILL = DrillTileEntity.class;
    public static final Class<? extends TileEntity> ENCASED_FAN = EncasedFanTileEntity.class;

    // Placeholder for future TEs — append below this line

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        GameRegistry.registerTileEntity(KINETIC, "create:kinetic");
        GameRegistry.registerTileEntity(MILLSTONE, "create:millstone");
        GameRegistry.registerTileEntity(DRILL, "create:drill");
        GameRegistry.registerTileEntity(ENCASED_FAN, "create:encased_fan");
    }

    private AllTileEntities() {}
}
