package create.foundation;

import cpw.mods.fml.common.registry.GameRegistry;
import create.core.kinetic.KineticTileEntity;
import create.core.kinetic.belt.BeltTileEntity;
import create.core.kinetic.source.CreativeMotorTileEntity;
import create.core.kinetic.source.WaterWheelTileEntity;
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

    // -- Kinetic sources (Phase 2 gap fill) --
    public static final Class<? extends TileEntity> CREATIVE_MOTOR = CreativeMotorTileEntity.class;
    public static final Class<? extends TileEntity> WATER_WHEEL = WaterWheelTileEntity.class;

    // -- Conveyor belt (Phase 3b) --
    public static final Class<? extends TileEntity> BELT = BeltTileEntity.class;

    // Placeholder for future TEs — append below this line

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        GameRegistry.registerTileEntity(KINETIC, "create:kinetic");
        GameRegistry.registerTileEntity(MILLSTONE, "create:millstone");
        GameRegistry.registerTileEntity(DRILL, "create:drill");
        GameRegistry.registerTileEntity(ENCASED_FAN, "create:encased_fan");
        GameRegistry.registerTileEntity(CREATIVE_MOTOR, "create:creative_motor");
        GameRegistry.registerTileEntity(WATER_WHEEL, "create:water_wheel");
        GameRegistry.registerTileEntity(BELT, "create:belt");
    }

    private AllTileEntities() {}
}
