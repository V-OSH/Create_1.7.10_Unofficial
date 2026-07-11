package create.foundation;

import cpw.mods.fml.common.registry.GameRegistry;
import create.core.kinetic.belt.BeltBlock;
import create.core.kinetic.CogwheelBlock;
import create.core.kinetic.ShaftBlock;
import create.core.kinetic.source.CreativeMotorBlock;
import create.core.kinetic.source.WaterWheelBlock;
import create.core.machinery.drill.DrillBlock;
import create.core.machinery.fan.EncasedFanBlock;
import create.core.machinery.millstone.MillstoneBlock;
import net.minecraft.block.Block;

/**
 * Central registry for all Create blocks.
 *
 * <p>Uses GTNH standard pattern: named constants with append-only ordering.
 * New blocks are always appended to the end to avoid ID drift.</p>
 */
public final class AllBlocks {

    // -- Kinetic nodes (Phase 2 fills in behavior) --
    public static final Block SHAFT = new ShaftBlock();
    public static final Block COGWHEEL = new CogwheelBlock(false);
    public static final Block LARGE_COGWHEEL = new CogwheelBlock(true);

    // -- Simple machinery (Phase 3a) --
    public static final Block MILLSTONE = new MillstoneBlock();
    public static final Block DRILL = new DrillBlock();
    public static final Block ENCASED_FAN = new EncasedFanBlock();

    // -- Kinetic sources (Phase 2 gap fill) --
    public static final Block CREATIVE_MOTOR = new CreativeMotorBlock();
    public static final Block WATER_WHEEL = new WaterWheelBlock();

    // -- Conveyor belt (Phase 3b) --
    public static final Block BELT = new BeltBlock();

    // Placeholder for future blocks — append below this line

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        // Register order is append-only — do NOT reorder existing entries
        GameRegistry.registerBlock(SHAFT, "shaft");
        GameRegistry.registerBlock(COGWHEEL, "cogwheel");
        GameRegistry.registerBlock(LARGE_COGWHEEL, "large_cogwheel");
        GameRegistry.registerBlock(MILLSTONE, "millstone");
        GameRegistry.registerBlock(DRILL, "drill");
        GameRegistry.registerBlock(ENCASED_FAN, "encased_fan");
        GameRegistry.registerBlock(CREATIVE_MOTOR, "creative_motor");
        GameRegistry.registerBlock(WATER_WHEEL, "water_wheel");
        GameRegistry.registerBlock(BELT, null, "belt");
    }

    private AllBlocks() {}
}
