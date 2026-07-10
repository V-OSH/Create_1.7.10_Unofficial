package create.core.machinery.recipe;

import java.util.Arrays;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessingRecipeTest {

    // Use simple test items instead of Minecraft init blocks/items
    private static final Item TEST_INPUT = new Item();
    private static final Item TEST_OUTPUT = new Item();
    private static final Item OTHER_ITEM = new Item();

    static {
        TEST_INPUT.setUnlocalizedName("testInput");
        TEST_OUTPUT.setUnlocalizedName("testOutput");
        OTHER_ITEM.setUnlocalizedName("otherItem");
    }

    @BeforeEach
    void setUp() {
        ProcessingRecipeRegistry.reset();
    }

    private void registerTestMillingRecipe() {
        ProcessingRecipe recipe = new MillingRecipe(
                new ItemStack(TEST_INPUT, 1),
                Arrays.asList(new ProcessingOutput(new ItemStack(TEST_OUTPUT, 1), 1.0f)),
                100);
        ProcessingRecipeRegistry.register(recipe);
    }

    // --- Output rolling ---

    @Test
    void rollOutput_chanceOne_alwaysReturnsFullStack() {
        ProcessingOutput output = new ProcessingOutput(new ItemStack(TEST_OUTPUT, 3), 1.0f);
        for (int i = 0; i < 100; i++) {
            ItemStack result = output.rollOutput();
            assertNotNull(result);
            assertEquals(3, result.stackSize);
        }
    }

    @Test
    void rollOutput_chanceZero_alwaysReturnsNull() {
        ProcessingOutput output = new ProcessingOutput(new ItemStack(TEST_OUTPUT, 3), 0.0f);
        for (int i = 0; i < 100; i++) {
            assertNull(output.rollOutput());
        }
    }

    @Test
    void rollOutput_chanceHalf_averagesToHalf() {
        ProcessingOutput output = new ProcessingOutput(new ItemStack(TEST_OUTPUT, 100), 0.5f);
        int total = 0;
        for (int i = 0; i < 50; i++) {
            ItemStack result = output.rollOutput();
            if (result != null) total += result.stackSize;
        }
        assertTrue(total > 1500 && total < 3500,
                "expected ~2500, got " + total);
    }

    @Test
    void processingOutput_empty_isNullStack() {
        ItemStack result = ProcessingOutput.EMPTY.rollOutput();
        assertNull(result);
    }

    // --- Recipe matching ---

    @Test
    void millingRecipe_matchesCorrectInput() {
        registerTestMillingRecipe();
        InventoryBasic inv = new InventoryBasic("test", true, 1);
        inv.setInventorySlotContents(0, new ItemStack(TEST_INPUT, 1));

        ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.MILLING, inv);
        assertNotNull(recipe);
        assertEquals(RecipeType.MILLING, recipe.getRecipeType());
    }

    @Test
    void millingRecipe_doesNotMatchWrongInput() {
        registerTestMillingRecipe();
        InventoryBasic inv = new InventoryBasic("test", true, 1);
        inv.setInventorySlotContents(0, new ItemStack(OTHER_ITEM, 1));

        ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.MILLING, inv);
        assertNull(recipe);
    }

    @Test
    void millingRecipe_doesNotMatchEmptyInventory() {
        registerTestMillingRecipe();
        InventoryBasic inv = new InventoryBasic("test", true, 1);

        ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.MILLING, inv);
        assertNull(recipe);
    }

    @Test
    void millingRecipe_matchesWithSufficientStackSize() {
        registerTestMillingRecipe();
        InventoryBasic inv = new InventoryBasic("test", true, 1);
        inv.setInventorySlotContents(0, new ItemStack(TEST_INPUT, 5));

        ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.MILLING, inv);
        assertNotNull(recipe);
    }

    // --- Recipe results ---

    @Test
    void millingRecipe_rollResults_returnsCorrectOutput() {
        registerTestMillingRecipe();
        InventoryBasic inv = new InventoryBasic("test", true, 1);
        inv.setInventorySlotContents(0, new ItemStack(TEST_INPUT, 1));

        ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.MILLING, inv);
        assertNotNull(recipe);

        java.util.List<ItemStack> results = recipe.rollResults();
        assertEquals(1, results.size());
        assertEquals(TEST_OUTPUT, results.get(0).getItem());
    }

    @Test
    void multiOutputRecipe_rollsWithinRange() {
        ProcessingRecipe recipe = new MillingRecipe(
                new ItemStack(TEST_INPUT, 1),
                Arrays.asList(
                        new ProcessingOutput(new ItemStack(TEST_OUTPUT, 1), 0.5f),
                        new ProcessingOutput(new ItemStack(TEST_OUTPUT, 1), 0.5f)),
                100);
        ProcessingRecipeRegistry.register(recipe);

        InventoryBasic inv = new InventoryBasic("test", true, 1);
        inv.setInventorySlotContents(0, new ItemStack(TEST_INPUT, 1));

        ProcessingRecipe found = ProcessingRecipeRegistry.find(RecipeType.MILLING, inv);
        java.util.List<ItemStack> results = found.rollResults();
        assertTrue(results.size() >= 0 && results.size() <= 2,
                "expected 0-2 outputs, got " + results.size());
    }

    // --- Registry ---

    @Test
    void getAll_returnsAllRecipesOfType() {
        registerTestMillingRecipe();
        java.util.List<ProcessingRecipe> recipes = ProcessingRecipeRegistry.getAll(RecipeType.MILLING);
        assertEquals(1, recipes.size());
    }

    @Test
    void reset_clearsAllRecipes() {
        registerTestMillingRecipe();
        ProcessingRecipeRegistry.reset();
        java.util.List<ProcessingRecipe> recipes = ProcessingRecipeRegistry.getAll(RecipeType.MILLING);
        assertTrue(recipes.isEmpty());
    }

    @Test
    void findReturnsNullForUnknownType() {
        registerTestMillingRecipe();
        InventoryBasic inv = new InventoryBasic("test", true, 1);
        inv.setInventorySlotContents(0, new ItemStack(TEST_INPUT, 1));

        ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.CRUSHING, inv);
        assertNull(recipe);
    }
}
