package create;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import create.core.kinetic.BlockStressValues;
import create.core.kinetic.KineticNetworkManager;
import create.core.machinery.recipe.ProcessingRecipeRegistry;
import create.foundation.AllBlocks;
import create.foundation.AllItems;
import create.foundation.AllTileEntities;
import create.foundation.CreateCreativeTab;
import create.foundation.networking.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = CreateMod.MODID, name = CreateMod.NAME, version = CreateMod.VERSION)
public class CreateMod {

    public static final String MODID = "create";
    public static final String NAME = "Create 1.7.10 Unofficial";
    public static final String VERSION = "0.1.0";

    public static final Logger LOGGER = LogManager.getLogger("Create");

    @Mod.Instance(MODID)
    public static CreateMod instance;

    /** Network channel for custom packets (Phase 2+). */
    public static PacketHandler packetHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Create 1.7.10 Unofficial — preInit");

        packetHandler = new PacketHandler();

        AllBlocks.register();
        AllItems.register();
        AllTileEntities.register();

        // Assign creative tab to all blocks
        for (var block : new net.minecraft.block.Block[]{
                AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL,
                AllBlocks.MILLSTONE, AllBlocks.DRILL, AllBlocks.ENCASED_FAN}) {
            block.setCreativeTab(CreateCreativeTab.TAB);
        }
        AllItems.ANDESITE_ALLOY.setCreativeTab(CreateCreativeTab.TAB);

        // Register world event handlers for kinetic network lifecycle
        MinecraftForge.EVENT_BUS.register(this);

        if (event.getSide() == Side.CLIENT) {
            clientPreInit();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Create 1.7.10 Unofficial — init");

        // Register default processing recipes
        ProcessingRecipeRegistry.registerDefaultRecipes();

        // Register stress impact values for machinery
        BlockStressValues.registerImpact(AllBlocks.MILLSTONE, 4.0);
        BlockStressValues.registerImpact(AllBlocks.DRILL, 4.0);
        BlockStressValues.registerImpact(AllBlocks.ENCASED_FAN, 2.0);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Create 1.7.10 Unofficial — postInit");
        // Phase 5: MineTweaker 3 integration point
    }

    private void clientPreInit() {
        // Phase 3a: register ISBRH renderers
        // Phase 3a: register TESR renderers
        LOGGER.info("Create client-side preInit complete");
    }

    // --- World lifecycle (kinetic network) ---

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        int dimId = event.world.provider.dimensionId;
        LOGGER.info("Create: initializing kinetic network for dimension {}", dimId);
        KineticNetworkManager.onWorldLoad(dimId);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        int dimId = event.world.provider.dimensionId;
        LOGGER.info("Create: discarding kinetic network for dimension {}", dimId);
        KineticNetworkManager.onWorldUnload(dimId);
    }
}
