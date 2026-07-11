package create;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import create.core.kinetic.BlockStressValues;
import create.core.kinetic.IKineticTile;
import create.core.kinetic.KineticNetworkManager;
import create.core.kinetic.KineticTileEntity;
import create.core.machinery.recipe.ProcessingRecipeRegistry;
import create.foundation.AllBlocks;
import create.foundation.AllItems;
import create.foundation.AllTileEntities;
import create.foundation.CreateCreativeTab;
import create.foundation.networking.PacketHandler;
import create.foundation.networking.PacketMotorSpeed;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
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

        // Register networking packets
        packetHandler.registerMessage(PacketMotorSpeed.class,
                new PacketMotorSpeed.Handler(), Side.SERVER);

        // Register GUI proxy
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new create.foundation.gui.CreateGuiProxy());

        AllBlocks.register();
        AllItems.register();
        AllTileEntities.register();

        // Assign creative tab to all blocks
        for (var block : new net.minecraft.block.Block[]{
                AllBlocks.SHAFT, AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL,
                AllBlocks.MILLSTONE, AllBlocks.DRILL, AllBlocks.ENCASED_FAN,
                AllBlocks.CREATIVE_MOTOR, AllBlocks.WATER_WHEEL}) {
            block.setCreativeTab(CreateCreativeTab.TAB);
        }
        AllItems.ANDESITE_ALLOY.setCreativeTab(CreateCreativeTab.TAB);
        AllItems.BELT_CONNECTOR.setCreativeTab(CreateCreativeTab.TAB);

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

        // Register stress impact values for machinery and belts
        BlockStressValues.registerImpact(AllBlocks.MILLSTONE, 4.0);
        BlockStressValues.registerImpact(AllBlocks.DRILL, 4.0);
        BlockStressValues.registerImpact(AllBlocks.ENCASED_FAN, 2.0);
        BlockStressValues.registerImpact(AllBlocks.BELT, 1.0);  // 1 SU per segment

        // Register stress capacity values for kinetic sources (at 1 RPM)
        BlockStressValues.registerCapacity(AllBlocks.CREATIVE_MOTOR, 16384.0);
        BlockStressValues.registerCapacity(AllBlocks.WATER_WHEEL, 512.0);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Create 1.7.10 Unofficial — postInit");
        // Phase 5: MineTweaker 3 integration point
    }

    private void clientPreInit() {
        // Register ISBRH renderer for all kinetic blocks
        create.foundation.render.KineticRenderer.RENDER_ID =
                cpw.mods.fml.client.registry.RenderingRegistry.getNextAvailableRenderId();
        cpw.mods.fml.client.registry.RenderingRegistry.registerBlockHandler(
                new create.foundation.render.KineticRenderer());

        // Register TESR for dynamic rotation
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                create.core.kinetic.KineticTileEntity.class,
                new create.foundation.render.KineticTileEntityRenderer());

        LOGGER.info("Create client-side preInit complete (ISBRH id={}, TESR registered)",
                create.foundation.render.KineticRenderer.RENDER_ID);
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

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.world.isRemote) return;

        for (Object value : event.getChunk().chunkTileEntityMap.values()) {
            if (!(value instanceof KineticTileEntity)) continue;

            KineticTileEntity kinetic = (KineticTileEntity) value;
            if (kinetic.isSource() || isChunkBoundary(kinetic)) {
                Long networkId = kinetic.getNetworkId();
                kinetic.updateSpeed = true;
                KineticNetworkManager.clearDirty(networkId, kinetic.getDimensionId());
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.world.isRemote) return;

        for (Object value : event.getChunk().chunkTileEntityMap.values()) {
            if (!(value instanceof IKineticTile)) continue;

            IKineticTile kinetic = (IKineticTile) value;
            KineticNetworkManager.markDirty(kinetic.getNetworkId(), kinetic.getDimensionId());
        }
    }

    private static boolean isChunkBoundary(TileEntity te) {
        int localX = te.xCoord & 15;
        int localZ = te.zCoord & 15;
        return localX == 0 || localX == 15 || localZ == 0 || localZ == 15;
    }
}
