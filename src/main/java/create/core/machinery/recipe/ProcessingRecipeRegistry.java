package create.core.machinery.recipe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/** Static registry for all Create processing recipes, keyed by {@link RecipeType}. */
public class ProcessingRecipeRegistry {

    private static final Map<RecipeType, List<ProcessingRecipe>> RECIPES = new EnumMap<>(RecipeType.class);

    /** Register a recipe. Call during init and from MineTweaker (postInit). */
    public static void register(ProcessingRecipe recipe) {
        RECIPES.computeIfAbsent(recipe.getRecipeType(), k -> new ArrayList<>()).add(recipe);
    }

    /** Find the first recipe of the given type that matches the inventory, or null. */
    public static ProcessingRecipe find(RecipeType type, IInventory inv) {
        List<ProcessingRecipe> list = RECIPES.get(type);
        if (list == null) return null;
        for (ProcessingRecipe recipe : list) {
            if (recipe.matches(inv)) {
                return recipe;
            }
        }
        return null;
    }

    /** All recipes of a given type (defensive copy). */
    public static List<ProcessingRecipe> getAll(RecipeType type) {
        List<ProcessingRecipe> list = RECIPES.get(type);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

    /** Register all hardcoded default recipes. Called from {@code CreateMod.init()}. */
    public static void registerDefaultRecipes() {
        registerMillingDefaults();
        registerSplashingDefaults();
        registerHauntingDefaults();
    }

    private static void registerMillingDefaults() {
        register(new MillingRecipe(
                new ItemStack(net.minecraft.init.Blocks.cobblestone, 1),
                listOf(new ProcessingOutput(new ItemStack(net.minecraft.init.Blocks.gravel, 1), 1.0f)),
                200));
        register(new MillingRecipe(
                new ItemStack(net.minecraft.init.Blocks.gravel, 1),
                listOf(new ProcessingOutput(new ItemStack(net.minecraft.init.Blocks.sand, 1), 1.0f)),
                200));
        register(new MillingRecipe(
                new ItemStack(net.minecraft.init.Items.bone, 1),
                listOf(
                        new ProcessingOutput(new ItemStack(net.minecraft.init.Items.dye, 1, 15), 0.5f),
                        new ProcessingOutput(new ItemStack(net.minecraft.init.Items.dye, 1, 15), 0.5f),
                        new ProcessingOutput(new ItemStack(net.minecraft.init.Items.dye, 1, 15), 0.5f),
                        new ProcessingOutput(new ItemStack(net.minecraft.init.Items.dye, 1, 15), 0.5f)),
                200));
        register(new MillingRecipe(
                new ItemStack(net.minecraft.init.Items.wheat, 1),
                listOf(
                        new ProcessingOutput(new ItemStack(net.minecraft.init.Items.wheat, 1), 0.25f)),
                150));
    }

    private static void registerSplashingDefaults() {
        // Sand washing: concrete powder → sand + bonus nugget chance (via fan over water)
        // These are minimal defaults — full recipes are added later or via MT3.
    }

    private static void registerHauntingDefaults() {
        // Soul sand conversion and similar — minimal for now, full set later.
    }

    @SafeVarargs
    private static <T> List<T> listOf(T... items) {
        List<T> list = new ArrayList<>();
        for (T item : items) list.add(item);
        return list;
    }

    /** Clear all recipes. For unit tests. */
    public static void reset() {
        RECIPES.clear();
    }
}
