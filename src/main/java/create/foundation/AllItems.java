package create.foundation;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

/**
 * Central registry for all Create items (non-block items).
 */
public final class AllItems {

    // -- Crafting materials --
    public static final Item ANDESITE_ALLOY = new Item()
            .setUnlocalizedName("create.andesite_alloy")
            .setTextureName("create:andesite_alloy");

    // Placeholder for future items — append below this line

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        // Register order is append-only — do NOT reorder existing entries
        GameRegistry.registerItem(ANDESITE_ALLOY, "andesite_alloy");
    }

    private AllItems() {}
}
